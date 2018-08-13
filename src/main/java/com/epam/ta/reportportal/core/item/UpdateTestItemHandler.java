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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRq;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;

import java.util.List;

/**
 * Handler to update test item issue type and issue statistics
 *
 * @author Dzianis Shlychkou
 */
public interface UpdateTestItemHandler {

	/**
	 * Define TestItem issue (or list of issues)
	 *
	 * @param projectDetails Project Details
	 * @param defineIssue    issues request data
	 * @param user           user
	 * @return list of defined issues for specified test items
	 */
	List<Issue> defineTestItemsIssues(ReportPortalUser.ProjectDetails projectDetails, DefineIssueRQ defineIssue, ReportPortalUser user);

	/**
	 * Update specified test item
	 *
	 * @param projectDetails Project Details
	 * @param itemId         test item ID
	 * @param rq             update test item request data
	 * @param user           request principal name
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS updateTestItem(ReportPortalUser.ProjectDetails projectDetails, Long itemId, UpdateTestItemRQ rq,
			ReportPortalUser user);

	/**
	 * Add external system issue link directly to test items
	 *
	 * @param projectDetails
	 * @param rq
	 * @param user
	 * @return
	 */
	List<OperationCompletionRS> linkExternalIssues(ReportPortalUser.ProjectDetails projectDetails, LinkExternalIssueRQ rq,
			ReportPortalUser user);

	List<OperationCompletionRS> unlinkExternalIssues(ReportPortalUser.ProjectDetails projectDetails, UnlinkExternalIssueRq rq,
			ReportPortalUser user);
}