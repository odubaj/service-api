/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.core.item.MergeTestItemHandler;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

@Service
public class MergeTestItemHandlerImpl implements MergeTestItemHandler {

//	@Autowired
//	private TestItemRepository testItemRepository;
//
//	@Autowired
//	private StatisticsFacadeFactory statisticsFacadeFactory;
//
//	@Autowired
//	private UserRepository userRepository;
//
//	@Autowired
//	private ProjectRepository projectRepository;
//
//	@Autowired
//	private LaunchRepository launchRepository;
//
//	@Autowired
//	private MergeStrategyFactory mergeStrategyFactory;

	@Override
	public OperationCompletionRS mergeTestItem(ReportPortalUser.ProjectDetails projectDetails, Long item, MergeTestItemRQ rq, String userName) {
//		TestItem testItemTarget = validateTestItem(item);
//		validateTestItemIsSuite(testItemTarget);
//		Launch launchTarget = validateLaunch(testItemTarget.getLaunchRef());
//		Project project = validateProject(launchTarget.getProjectRef());
//		validateLaunchInProject(launchTarget, project);
//
//		List<TestItem> itemsToMerge = new ArrayList<>(rq.getItems().size());
//		Set<String> sourceLaunches = new HashSet<>();
//		for (String id : rq.getItems()) {
//			TestItem itemToMerge = validateTestItem(id);
//			sourceLaunches.add(itemToMerge.getLaunchRef());
//			validateTestItemIsSuite(itemToMerge);
//			validateTestItemInProject(itemToMerge, project);
//			itemsToMerge.add(itemToMerge);
//		}
//
//		MergeStrategyType mergeStrategyType = MergeStrategyType.fromValue(rq.getMergeStrategyType());
//		expect(mergeStrategyType, Predicates.notNull()).verify(ErrorType.UNSUPPORTED_MERGE_STRATEGY_TYPE, rq.getMergeStrategyType());
//		MergeStrategy mergeStrategy = mergeStrategyFactory.getStrategy(mergeStrategyType);
//		mergeStrategy.mergeTestItems(testItemTarget, itemsToMerge);
//
//		StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(
//				project.getConfiguration().getStatisticsCalculationStrategy());
//		for (String launchID : sourceLaunches) {
//			Launch launch = launchRepository.findOne(launchID);
//			statisticsFacade.recalculateStatistics(launch);
//		}
//		statisticsFacade.recalculateStatistics(launchTarget);
//
//		return new OperationCompletionRS("TestItem with ID = '" + item + "' successfully merged.");
		throw new UnsupportedOperationException("No implementation");
	}

//	private void validateLaunchInProject(Launch launch, Project project) {
//		expect(launch.getProjectRef(), equalTo(project.getId())).verify(ACCESS_DENIED);
//	}
//
//	private void validateTestItemInProject(TestItem testItem, Project project) {
//		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
//		expect(launch.getProjectRef(), equalTo(project.getId())).verify(ACCESS_DENIED);
//	}
//
//	private TestItem validateTestItem(String testItemId) {
//		TestItem testItem = testItemRepository.findOne(testItemId);
//		expect(testItem, notNull()).verify(TEST_ITEM_NOT_FOUND, testItemId);
//		return testItem;
//	}
//
//	private Launch validateLaunch(String launchId) {
//		Launch launch = launchRepository.findOne(launchId);
//		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launchId);
//		return launch;
//	}
//
//	private Project validateProject(String projectId) {
//		Project project = projectRepository.findOne(projectId);
//		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectId);
//		return project;
//	}
//
//	private void validateTestItemIsSuite(TestItem testItem) {
//		expect(true, equalTo(testItem.getType().sameLevel(TestItemType.SUITE))).verify(ErrorType.INCORRECT_REQUEST, testItem.getId());
//	}
}