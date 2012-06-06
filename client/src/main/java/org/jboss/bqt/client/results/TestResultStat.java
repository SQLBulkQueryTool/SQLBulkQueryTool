/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.jboss.bqt.client.results;

import java.io.Serializable;
import java.util.Date;

import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.TestResult;

/**
 * ATestResultStat
 * 
 * <p>
 * A per-query set of result stats.
 * </p>
 */
public class TestResultStat implements TestResult, Serializable {

	/**
	 * @since
	 */
	private static final long serialVersionUID = 6670189391506288744L;
	protected int resultStatus = -1;
	protected String queryID;
	protected String querySetID;
	protected String errorMsg;
	protected String query;
	protected Throwable error = null;
	
	private String resultMode = TestProperties.RESULT_MODES.NONE;


	private long beginTS;
	private long endTS;

	private String errorFile;
	
	private long rowCount;
	private long updateCount;

	public TestResultStat(final String querySetID, final String queryID,
			String query) {
		this.querySetID = querySetID;
		this.queryID = queryID;
		this.query = query;

	}

	public TestResultStat(final String querySetID, final String queryID,
			String query, final int resultStatus, long beginTS, long endTS,
			final Throwable error) {
		this(querySetID, queryID, query);
		this.resultStatus = resultStatus;
		this.beginTS = beginTS;
		this.endTS = endTS;
		this.error = error;

	}

	public TestResultStat(final String querySetID, final String queryID,
			String query, final int resultStatus, long beginTS, long endTS,
			final Throwable error, String errorFile) {
		this(querySetID, queryID, query, resultStatus, beginTS, endTS, error);		
		this.errorFile = errorFile;
	}

	public String getResultStatusString() {
		switch (resultStatus) {
		case RESULT_STATE.TEST_SUCCESS:
			return RESULT_STATE_STRING.PASS;
		case RESULT_STATE.TEST_EXCEPTION:
			return RESULT_STATE_STRING.FAIL;
		case RESULT_STATE.TEST_EXPECTED_EXCEPTION:
			return RESULT_STATE_STRING.FAIL_EXPECTED_EXCEPTION;
		}
		return RESULT_STATE_STRING.UNKNOWN;
	}

	@Override
	public String getQuerySetID() {
		// TODO Auto-generated method stub
		return this.querySetID;
	}

	public String getQueryID() {
		return queryID;
	}

	public String getQuery() {
		return query;
	}

	public int getStatus() {
		return resultStatus;
	}

	public void setStatus(int endStatus) {
		resultStatus = endStatus;
	}

	public String getExceptionMsg() {
		return (this.errorMsg != null ? this.errorMsg : (error != null ? error
				.getMessage() : ""));
	}

	public void setException(Throwable error) {
		this.error = error;
	}

	public void setExceptionMessage(String errorMsg) {
		this.errorMsg = errorMsg;

	}

	public Throwable getException() {
		return this.error;
	}

	public long getBeginTS() {
		return beginTS;

	}

	public void setBeginTS(long beginTS) {
		this.beginTS = beginTS;
	}

	public long getEndTS() {
		return endTS;
	}

	public void setEndTS(long endts) {
		this.endTS = endts;
	}

	/**
	 * @return Returns the errorfile.
	 */
	public String getErrorfile() {
		return errorFile;
	}

	public void setErrorFile(String errorfile) {
		this.errorFile = errorfile;
	}

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public void setUpdateCount(long updateCount) {
		this.updateCount = updateCount;
	}

	public long getExecutionTime() {
		Date starttest = new Date(getBeginTS());
		Date endtest = new Date(getEndTS());
		return endtest.getTime() - starttest.getTime(); // diff in mills
	}

	@Override
	public void setResultMode(String mode) {
		this.resultMode = mode;
		
	}

	@Override
	public String getResultMode() {
		return this.resultMode;
	}


}
