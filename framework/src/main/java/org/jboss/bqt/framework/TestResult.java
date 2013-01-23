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
package org.jboss.bqt.framework;

import java.io.Serializable;
import java.util.Date;

/**
 * TestResult represents a single test and the state of it.
 * 
 * <p>
 * A per-query set of result stats.
 * </p>
 */
public class TestResult implements ConfigPropertyNames, Serializable {
	

	/**
	 * The RESULT_STATE is the value assigned based the result of the executed
	 * query test
	 * 
	 * @author vanhalbert
	 * 
	 */
	public interface RESULT_STATE {

		/**
		 * TEST_SUCCESS - indicates the executed query performed as expected
		 */
		public static final int TEST_SUCCESS = 0;
		/**
		 * TEST_EXCEPTION - indicates an unexpected exception occurred during
		 * the execution of the query
		 */
		public static final int TEST_EXCEPTION = 1;
		/**
		 * TEST_EXPECTED_EXCEPTION - indicates the expected result was suppose
		 * to an exception, how, the query executed without error
		 */
		public static final int TEST_EXPECTED_EXCEPTION = 4;
		
		/**
		 * TEST_EXECUTION_TIME_EXCEEDED_EXCEPTION - indicates the execution time
		 * exceeded the threshold specified
		 */
		public static final int TEST_EXECUTION_TIME_EXCEEDED_EXCEPTION = 8;
		
		/**
		 * TEST_PRERUN is used to indicate the test has not been executed yet.
		 */
		public static final int TEST_PRERUN = 16;
	}

	public interface RESULT_STATE_STRING {
		/**
		 * The string value for when a
		 * {@link RESULT_STATE#TEST_SUCCESS occurs}
		 */
		public static final String PASS = "pass";
		/**
		 * The string value for when a
		 * {@link RESULT_STATE#TEST_EXCEPTION occurs}
		 */
		public static final String FAIL = "fail";
		/**
		 * The string value for when a
		 * {@link RESULT_STATE#TEST_EXPECTED_EXCEPTION occurs}
		 */
		public static final String FAIL_EXPECTED_EXCEPTION = "fail-expected_exception";
		/**
		 * The string value for when a
		 * {@link RESULT_STATE#TEST_EXECUTION_TIME_EXCEEDED_EXCEPTION occurs}
		 */
		public static final String EXEEDED_EXECUTION_TIME = "fail-exceeded-time";

		/**
		 * The string value for when a status occurs that hasn't been defined
		 */
		public static final String UNKNOWN = "unknown";

	}

	/**
	 * @since
	 */
	private static final long serialVersionUID = 6670189391506288744L;
//	protected int resultStatus = -1;
	protected String queryID;
	protected String querySetID;
	protected String errorMsg;
	protected String query;
	protected Throwable error = null;
	
	private int status = RESULT_STATE.TEST_PRERUN;

	private long beginTS;
	private long endTS;

	private String errorFile;
	
	private long rowCount;
	private long updateCount;
	
private String resultMode = null;
	
	public TestResult(final String querySetID, final String queryID) {
		this.querySetID = querySetID;
		this.queryID = queryID;

	}

	public TestResult(final String querySetID, final String queryID,
			String query) {
		this.querySetID = querySetID;
		this.queryID = queryID;
		this.query = query;

	}

	public String getResultStatusString() {
		switch (getStatus()) {
		case RESULT_STATE.TEST_SUCCESS:
			return RESULT_STATE_STRING.PASS;
		case RESULT_STATE.TEST_EXCEPTION:
			return RESULT_STATE_STRING.FAIL;
		case RESULT_STATE.TEST_EXPECTED_EXCEPTION:
			return RESULT_STATE_STRING.FAIL_EXPECTED_EXCEPTION;
		case RESULT_STATE.TEST_EXECUTION_TIME_EXCEEDED_EXCEPTION:
			return RESULT_STATE_STRING.EXEEDED_EXECUTION_TIME;
		}
		return RESULT_STATE_STRING.UNKNOWN;
	}

	public String getQuerySetID() {
		return this.querySetID;
	}

	public String getQueryID() {
		return queryID;
	}
	
	public String getResultMode() {
		return this.resultMode;
	}
	
	public void setResultMode(String resultMode) {
		this.resultMode = resultMode;
	}

	public String getQuery() {
		return query;
	}

	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public boolean isFailure() {
		switch (getStatus()) {
		case RESULT_STATE.TEST_EXECUTION_TIME_EXCEEDED_EXCEPTION:
		case RESULT_STATE.TEST_EXCEPTION:
			return true;
		}
		return false;
	}
	
	public boolean isSuccess() {
		switch (getStatus()) {
		case TestResult.RESULT_STATE.TEST_SUCCESS:
		case TestResult.RESULT_STATE.TEST_EXPECTED_EXCEPTION:
			return true;
		}
		return false;
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
	



}
