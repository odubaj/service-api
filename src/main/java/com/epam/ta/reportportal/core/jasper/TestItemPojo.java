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
package com.epam.ta.reportportal.core.jasper;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.statistics.Statistics;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static com.epam.ta.reportportal.core.jasper.util.ExportUtils.*;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static java.util.Optional.ofNullable;

/**
 * Jasper Reports collection {@link TestItem} POJO
 *
 * @author Andrei_Ramanchuk
 */
public class TestItemPojo {
	private String type;
	private String name;
	private String status;
	private Double duration;
	private Integer total;
	private Integer passed;
	private Integer untested;
	private Integer running;
	private Integer failed;
	private Integer skipped;
	private Integer testBug;
	private Integer productBug;
	private Integer systemIssue;
	private Integer minorDefect;
	private Integer toInvestigate;

	public TestItemPojo(TestItem input) {
		this.type = input.getType().name();
		Optional<String> issueDescription = Optional.empty();
		if (input.getItemResults().getIssue() != null) {
			issueDescription = ofNullable(input.getItemResults().getIssue().getIssueDescription()).map(it -> COMMENT_PREFIX + it);
		}

		Optional<String> description = ofNullable(input.getDescription()).map(it -> DESCRIPTION_PREFIX + it);

		this.name = adjustName(input) + description.orElse(EMPTY_STRING) + issueDescription.orElse(EMPTY_STRING);
		this.status = input.getItemResults().getStatus().name();

		this.duration = Duration.between(input.getStartTime(), input.getItemResults().getEndTime()).toMillis()
				/ (double) org.apache.commons.lang3.time.DateUtils.MILLIS_PER_SECOND;

		Set<Statistics> statistics = input.getItemResults().getStatistics();

		this.total = getStatisticsCounter(statistics, EXECUTIONS_TOTAL);
		this.passed = getStatisticsCounter(statistics, EXECUTIONS_PASSED);
		this.failed = getStatisticsCounter(statistics, EXECUTIONS_FAILED);
		this.skipped = getStatisticsCounter(statistics, EXECUTIONS_SKIPPED);
		this.untested = getStatisticsCounter(statistics, EXECUTIONS_UNTESTED);
		this.running = getStatisticsCounter(statistics, EXECUTIONS_RUNNING);

		this.testBug = getStatisticsCounter(statistics, DEFECTS_TEST_BUG_TOTAL);
		this.productBug = getStatisticsCounter(statistics, DEFECTS_PRODUCT_BUG_TOTAL);
		this.systemIssue = getStatisticsCounter(statistics, DEFECTS_SYSTEM_ISSUE_TOTAL);
		this.minorDefect = getStatisticsCounter(statistics, DEFECTS_MINOR_DEFECT_TOTAL);
		this.toInvestigate = getStatisticsCounter(statistics, DEFECTS_TO_INVESTIGATE_TOTAL);
	}

	public void setType(String value) {
		this.type = value;
	}

	public String getType() {
		return type;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getName() {
		return name;
	}

	public void setStatus(String value) {
		this.status = value;
	}

	public String getStatus() {
		return status;
	}

	public void setTotal(Integer value) {
		this.total = value;
	}

	public Integer getTotal() {
		return total;
	}

	public void setPased(Integer value) {
		this.passed = value;
	}

	public Integer getPassed() {
		return passed;
	}

	public void setUntested(Integer value) {
		this.untested = value;
	}

	public Integer getUntested() {
		return untested;
	}

	public void setRunning(Integer value) {
		this.running = value;
	}

	public Integer getRunning() {
		return running;
	}

	public void setFailed(Integer value) {
		this.failed = value;
	}

	public Integer getFailed() {
		return failed;
	}

	public void setSkipped(Integer value) {
		this.skipped = value;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setTestBug(Integer value) {
		this.testBug = value;
	}

	public Integer getTestBug() {
		return testBug;
	}

	public void setProductBug(Integer value) {
		this.productBug = value;
	}

	public Integer getProductBug() {
		return productBug;
	}

	public void setSystemIssue(Integer value) {
		this.systemIssue = value;
	}

	public Integer getSystemIssue() {
		return systemIssue;
	}

	public void setMinorDefect(Integer value) {
		this.minorDefect = value;
	}

	public Integer getMinorDefect() {
		return minorDefect;
	}

	public void setToInvestigate(Integer value) {
		this.toInvestigate = value;
	}

	public Integer getToInvestigate() {
		return toInvestigate;
	}

	public Double getDuration() {
		return duration;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("TestItemPojo{");
		sb.append("type='").append(type).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", status='").append(status).append('\'');
		sb.append(", duration=").append(duration);
		sb.append(", total=").append(total);
		sb.append(", passed=").append(passed);
		sb.append(", failed=").append(failed);
		sb.append(", skipped=").append(skipped);
		sb.append(", untested=").append(untested);
		sb.append(", running=").append(running);
		sb.append(", testBug=").append(testBug);
		sb.append(", productBug=").append(productBug);
		sb.append(", systemIssue=").append(systemIssue);
		sb.append(", minorDefect=").append(minorDefect);
		sb.append(", toInvestigate=").append(toInvestigate);
		sb.append('}');
		return sb.toString();
	}
}
