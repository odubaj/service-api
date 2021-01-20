/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.imprt.impl.junit;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
//import com.epam.ta.reportportal.ws.model.log.SaveLogRQ.File;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.URL;

// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.io.IOException;
// import java.io.InputStream;
// import java.net.URLConnection;
// import java.util.Arrays;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.http.MediaType;

import static com.epam.ta.reportportal.core.imprt.impl.DateUtils.toMillis;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class XunitImportHandler extends DefaultHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(XunitImportHandler.class);

	private final StartTestItemHandler startTestItemHandler;

	private final FinishTestItemHandler finishTestItemHandler;

	private final CreateLogHandler createLogHandler;

	@Autowired
	public XunitImportHandler(StartTestItemHandler startTestItemHandler, FinishTestItemHandler finishTestItemHandler,
			CreateLogHandler createLogHandler) {
		this.startTestItemHandler = startTestItemHandler;
		this.finishTestItemHandler = finishTestItemHandler;
		this.createLogHandler = createLogHandler;
	}

	//initial info
	private ReportPortalUser.ProjectDetails projectDetails;
	private ReportPortalUser user;
	private String launchUuid;

	//need to know item's id to attach System.out/System.err logs
	private String currentItemUuid;

	private LocalDateTime startSuiteTime;

	private Set<ItemAttributesRQ> attributes = new HashSet<ItemAttributesRQ>();
	private Set<ItemAttributesRQ> launchAttributes = new HashSet<ItemAttributesRQ>();
	private Set<ItemAttributesRQ> itemAttributes = new HashSet<ItemAttributesRQ>();
	private Set<ItemAttributesRQ> archAttributes = new HashSet<ItemAttributesRQ>();
	//private List<ParameterResource> parameters = new ArrayList<ParameterResource>();

	private long commonDuration;
	private long currentDuration;

	//items structure ids
	private Deque<String> itemUuids;
	private StatusEnum status;
	private StringBuilder message;
	private LocalDateTime startItemTime;

	@Override
	public void startDocument() {
		itemUuids = new ArrayDeque<>();
		message = new StringBuilder();
		startSuiteTime = LocalDateTime.now();
	}

	@Override
	public void endDocument() {
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (XunitReportTag.fromString(qName)) {
			case TESTSUITE_ARCH:
			case TESTSUITE:
				if (itemUuids.isEmpty()) {
					startRootItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()),
							attributes.getValue(XunitReportTag.TIMESTAMP.getValue()),
							attributes.getValue(XunitReportTag.ATTR_ID.getValue()),
							attributes.getValue(XunitReportTag.ATTR_HREF.getValue())
					);
				} else {
					startTestItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()),
							attributes.getValue(XunitReportTag.ATTR_ID.getValue()),
							attributes.getValue(XunitReportTag.ATTR_HREF.getValue())
					);
				}
				break;
			//case LOGS:
				//this.parameters.clear();
				//break;	
			case TESTCASE:
				startStepItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()),
						attributes.getValue(XunitReportTag.ATTR_TIME.getValue()),
						attributes.getValue(XunitReportTag.ATTR_ARCH.getValue()),
						attributes.getValue(XunitReportTag.ATTR_ID.getValue())
				);
				break;
			case ERROR:
			case FAILURE:
				message = new StringBuilder();
				status = StatusEnum.FAILED;
				break;
			case SKIPPED:
				message = new StringBuilder();
				status = StatusEnum.SKIPPED;
				break;
			case MANUAL_TEST:
				message = new StringBuilder();
				status = StatusEnum.MANUAL;
				break;
			case GLOBAL_PROPERTIES:
				this.attributes.clear();
				break;
			case GLOBAL_PROPERTY:
				ItemAttributesRQ attr = new ItemAttributesRQ(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()), attributes.getValue(XunitReportTag.ATTR_VALUE.getValue()));
				this.attributes.add(attr);
				this.launchAttributes.add(attr);
				break;
			case PARAMETERS:
				//this.parameters.clear();
				break;
			case PARAMETER:
				//ParameterResource par = new ParameterResource();
				//TODO treba upravit pre standard ktory neni este zavedeny
				//par.setKey(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()));
				//par.setValue(attributes.getValue(XunitReportTag.ATTR_VALUE.getValue()));
				//par.setKey(attributes.getValue(XunitReportTag.ATTR_VALUE.getValue()));
				//this.parameters.add(par);
				break;
			//case LOG:
			//	ParameterResource par = new ParameterResource();
			//	par.setKey(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()));
			//	par.setValue(attributes.getValue(XunitReportTag.ATTR_VALUE.getValue()));
			//	this.parameters.add(par);
			//	break;
			case PROPERTIES:
				this.itemAttributes.clear();
				break;
			case ARCH_PROPERTIES:
				this.archAttributes.clear();
				break;		
			case PROPERTY:
				ItemAttributesRQ itemAttr = new ItemAttributesRQ(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()), attributes.getValue(XunitReportTag.ATTR_VALUE.getValue()));
				this.itemAttributes.add(itemAttr);
				break;
			case ARCH_PROPERTY:
				ItemAttributesRQ archAttr = new ItemAttributesRQ(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()), attributes.getValue(XunitReportTag.ATTR_VALUE.getValue()));
				this.archAttributes.add(archAttr);
				break;
			case SYSTEM_OUT:
			case SYSTEM_ERR:
			case WARNING:
				message = new StringBuilder();
				break;
			case TESTSUITES:
				break;
			case UNKNOWN:
			default:
				LOGGER.warn("Unknown tag: {}", qName);
				break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (XunitReportTag.fromString(qName)) {
			case TESTSUITE:
				finishRootItem(false);
				this.itemAttributes.clear();
				//this.parameters.clear();
				break;
			case TESTSUITE_ARCH:
				finishRootItem(true);
				this.archAttributes.clear();
				//this.parameters.clear();
				break;
			//case LOGS:
				//startStepItem(new String("LOGS"), new String("1"), true);
				//finishTestItem();
				//break;
			case TESTCASE:
				finishTestItem();
				//this.parameters.clear();
				break;
			case SKIPPED:
			case ERROR:
			case FAILURE:
			case MANUAL_TEST:
			case SYSTEM_ERR:
				//v properties musi byt aj vo vyssich leveloch ako test a arch ze je test manualny aby sa dalo filtrovat
				attachLog(LogLevel.ERROR);
				break;
			case SYSTEM_OUT:
				attachLog(LogLevel.INFO);
				break;
			case WARNING:
				attachLog(LogLevel.WARN);
				break;
			case GLOBAL_PROPERTY:
			case GLOBAL_PROPERTIES:
			case PROPERTIES:
			case PROPERTY:
			case ARCH_PROPERTIES:
			case ARCH_PROPERTY:
			case TESTSUITES:
				break;
			case UNKNOWN:
			default:
				LOGGER.warn("Unknown tag: {}", qName);
				break;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String msg = new String(ch, start, length);
		if (!msg.isEmpty()) {
			message.append(msg);
		}
	}

	private void startRootItem(String name, String timestamp, String identifier, String code_ref) {
		if (null != timestamp) {
			startItemTime = parseTimeStamp(timestamp);
			if (startSuiteTime.isAfter(startItemTime)) {
				startSuiteTime = LocalDateTime.of(startItemTime.toLocalDate(), startItemTime.toLocalTime());
			}
		} else {
			startItemTime = LocalDateTime.now();
		}
		StartTestItemRQ rq = buildStartTestRq(name);
		rq.setTestCaseId(identifier);
		if(code_ref != null && !code_ref.isEmpty()) {
			rq.setCodeRef(code_ref);
		}
		String id = startTestItemHandler.startRootItem(user, projectDetails, rq).getId();
		currentItemUuid = id;
		itemUuids.push(id);
	}

	private LocalDateTime parseTimeStamp(String timestamp) {
		LocalDateTime localDateTime = null;
		try {
			localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(timestamp)), ZoneId.systemDefault());
		} catch (NumberFormatException ignored) {
			//ignored
		}
		if (null == localDateTime) {
			DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendOptional(DateTimeFormatter.RFC_1123_DATE_TIME)
					.appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
					.optionalStart()
					.appendZoneId()
					.optionalEnd()
					.toFormatter();
			localDateTime = LocalDateTime.parse(timestamp, formatter);
		}
		return localDateTime;
	}

	private void startTestItem(String name, String identifier, String code_ref) {
		StartTestItemRQ rq = buildStartTestRq(name);
		rq.setTestCaseId(identifier);
		if(code_ref != null && !code_ref.isEmpty()) {
			rq.setCodeRef(code_ref);
		}
		String id = startTestItemHandler.startChildItem(user, projectDetails, rq, itemUuids.peek()).getId();
		currentItemUuid = id;
		itemUuids.push(id);
	}

	private void startStepItem(String name, String duration, String arch, String identifier) {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchUuid(launchUuid);
		rq.setStartTime(EntityUtils.TO_DATE.apply(startItemTime));
		rq.setType(TestItemTypeEnum.STEP.name());
		rq.setName(name);
		List<ParameterResource> parameters = new ArrayList<ParameterResource>();
		ParameterResource par1 = new ParameterResource();
		par1.setKey(new String("name"));
		par1.setValue(name);
		parameters.add(par1);
		ParameterResource par2 = new ParameterResource();
		par2.setKey(new String("arch"));
		par2.setValue(arch);
		parameters.add(par2);
		rq.setParameters(parameters);
		rq.setTestCaseId(identifier);
		rq.setAttributes(this.attributes);
		String id = startTestItemHandler.startChildItem(user, projectDetails, rq, itemUuids.peek()).getId();
		currentDuration = toMillis(duration);
		currentItemUuid = id;
		itemUuids.push(id);
	}

	private void finishRootItem(boolean arch) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(EntityUtils.TO_DATE.apply(startItemTime));
		if(arch == true) {
			this.archAttributes.addAll(this.attributes);
			rq.setAttributes(this.archAttributes);
		} else {
			this.itemAttributes.addAll(this.attributes);
			rq.setAttributes(this.itemAttributes);
		}
		finishTestItemHandler.finishTestItem(user, projectDetails, itemUuids.poll(), rq);
		status = null;
	}

	private void finishTestItem() {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		startItemTime = startItemTime.plus(currentDuration, ChronoUnit.MILLIS);
		commonDuration += currentDuration;
		rq.setEndTime(EntityUtils.TO_DATE.apply(startItemTime));
		rq.setStatus(Optional.ofNullable(status).orElse(StatusEnum.PASSED).name());
		currentItemUuid = itemUuids.poll();
		finishTestItemHandler.finishTestItem(user, projectDetails, currentItemUuid, rq);
		status = null;
	}

	private void attachLog(LogLevel logLevel) {
		if (null != message && message.length() != 0) {
			SaveLogRQ saveLogRQ = new SaveLogRQ();
			saveLogRQ.setLevel(logLevel.name());
			saveLogRQ.setLogTime(EntityUtils.TO_DATE.apply(startItemTime));
			/*String out = "";
			try {
				out = new Scanner(new URL(message.toString()).openStream(), "UTF-8").useDelimiter("\\A").next();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}*/
			saveLogRQ.setMessage(message.toString().trim());
			saveLogRQ.setItemUuid(currentItemUuid);
			// File attachement = new File();
			// try {
				
			// 	attachement.setName("results");
			// 	String charset = "UTF-8";
			// 	String readLine;
			// 	//System.out.println(Paths.get("home/odubaj/data.xml").toAbsolutePath());
			// 	//byte[] encoded = Files.readAllBytes(Paths.get("home/odubaj/data.xml").toAbsolutePath());
			// 	//System.out.println(Arrays.toString(encoded));
			// 	URL url = new URL("https://kojipkgs.fedoraproject.org//packages/mysql-connector-java/8.0.23/1.fc34/data/logs/noarch/state.log");
			// 	URLConnection conn = url.openConnection();
			// 	conn.setRequestProperty("Accept-Charset", charset);
			// 	InputStream in = conn.getInputStream();
			// 	byte[] encoded = new byte[in.available()];
			// 	in.read(encoded);
			// 	MultipartFile file = new MockMultipartFile("name.txt", encoded);
			// 	System.out.println(Arrays.toString(encoded));
			// 	attachement.setContent(encoded);
			// 	attachement.setContentType("text/plain");
			// 	saveLogRQ.setFile(attachement);
			// 	System.out.println("mala by sa pridat attachement!!!!!!!!!!!!!!!!!!!!!!");
			// 	in.close();
			// } catch (IOException e) {
			// 	System.out.println("nepodarilo sa pridat attachement, exception "+ e);

			// }

			
			createLogHandler.createLog(saveLogRQ, null, projectDetails);
		}
	}

	XunitImportHandler withParameters(ReportPortalUser.ProjectDetails projectDetails, String launchId, ReportPortalUser user) {
		this.projectDetails = projectDetails;
		this.launchUuid = launchId;
		this.user = user;
		return this;
	}

	private StartTestItemRQ buildStartTestRq(String name) {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchUuid(launchUuid);
		rq.setStartTime(EntityUtils.TO_DATE.apply(startItemTime));
		rq.setType(TestItemTypeEnum.TEST.name());
		rq.setName(Strings.isNullOrEmpty(name) ? "no_name" : name);
		return rq;
	}

	LocalDateTime getStartSuiteTime() {
		return startSuiteTime;
	}

	long getCommonDuration() {
		return commonDuration;
	}

	public Set<ItemAttributesRQ> getAttributes() {
		return this.launchAttributes;
	}
}
