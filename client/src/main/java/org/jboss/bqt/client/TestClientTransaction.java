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

package org.jboss.bqt.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.api.TestResult;
import org.jboss.bqt.client.results.TestResultStat;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.AbstractQueryTransaction;

/**
 * TestClientTransaction
 * 
 */
public class TestClientTransaction extends AbstractQueryTransaction {

	private QueryScenario querySet = null;
	private ExpectedResultsReader expectedResults = null;
	private QueryTest query = null;

	private QuerySQL[] queries = null;

	private boolean errorExpected = false;

	private boolean resultFromQuery = false;
	
	private TestResult rs = null;

	private TestResultsSummary testResultsSummary;
	
	private String originalResultMode = null;

	public TestClientTransaction(QueryScenario querySet) {
		super(querySet.getQueryScenarioIdentifier());
		this.querySet = querySet;
		


	}

	public void init(TestResultsSummary testResultsSummary,
			ExpectedResultsReader expectedResults, QueryTest query) {
		this.query = query;
		this.testResultsSummary = testResultsSummary;
		this.expectedResults = expectedResults;
		
		rs = new TestResultStat(query.geQuerySetID(), query.getQueryID());
		rs.setResultMode(this.querySet.getResultsMode());
		rs.setStatus(TestResult.RESULT_STATE.TEST_SUCCESS);
		
		this.originalResultMode = this.querySet.getResultsMode();

		errorExpected = false;
		resultFromQuery = false;
		
		queries = query.getQueries();
	}

	@Override
	public String getTestName() {
		return query.getQueryScenarioID() + ":"
				+ (query.getQueryID() != null ? query.getQueryID() : "NA");

	}

	@Override
	public void before() {
		super.before();

			this.errorExpected = expectedResults.isExceptionExpected(query
					.getQueryID());
	}

	@Override
	public void testCase() throws FrameworkRuntimeException {
		if (this.getApplicationException() != null) return;
		
		ClientPlugin.LOGGER.debug("expected error: " + this.errorExpected);

		int l = queries.length;

		try {
			
			// multiple queries cannot be processed as a single result
			// therefore, the other options are not valid
			if (l > 1) {
				this.rs.setResultMode(TestProperties.RESULT_MODES.NONE);
			}

			for (int i = 0; i < l; i++) {
				QuerySQL qsql = queries[i];
				this.rs.setQuery(qsql.getSql());
				
				// if runtimes or rowcounts are greater than 1, then no expected results will
				// be processed, therefore, resultmode is set to NONE for this query
				if (qsql.getRunTimes() > 1 || qsql.getRowCnt() > 0) {
					this.rs.setResultMode(TestProperties.RESULT_MODES.NONE);

				}
				
				ClientPlugin.LOGGER.debug("ID: " + query.geQuerySetID() + "  -  "
						+ query.getQueryID() + "ResultMode: " + this.rs.getResultMode() + ", numtimes: " +
						qsql.getRunTimes() + " rowcount: "  + qsql.getRowCnt() + " updatecnt: " + 
						qsql.getUpdateCnt());

				
				for (int r = 0; r < qsql.getRunTimes(); r++) {
				
					resultFromQuery = execute(this.rs.getQuery(), qsql.getParms(), qsql.getPayLoad());
					if (! querySet.isGenerate()) {
						if (resultFromQuery) {
							if (qsql.getRowCnt() >= 0) {
								this.assertRowCount(qsql.getRowCnt());
							} else if (this.rs.getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.NONE)) {
								// for the NONE option, read all the rows
								this.getRowCount();
							}							
							
						} else {
							
							this.rs.setResultMode(TestProperties.RESULT_MODES.NONE);
							
							if (qsql.getUpdateCnt() >= 0) {
								this.assertUpdateCount(qsql.getUpdateCnt());
							}							
							
						}
//						if (qsql.isSelect()) {
//							if (qsql.getRowCnt() >= 0) {
//								this.assertRowCount(qsql.getRowCnt());
//							} else if (this.rs.getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.NONE)) {
//								// for the NONE option, read all the rows
//								this.getRowCount();
//							}
//						} else {
//							// for cases where a SET statement, or other type is called, but not an update,
//							// then allow it to continue
//							// anything other than select (set, update,delete or insert) must be set to NONE, because no resultset will exists 
//
//							this.rs.setResultMode(TestProperties.RESULT_MODES.NONE);
//							
//							if (qsql.getUpdateCnt() >= 0) {
//								this.assertUpdateCount(qsql.getUpdateCnt());
//							}							
//						}
//						if (qsql.getUpdateCnt() >= 0) {
//							this.assertUpdateCount(qsql.getUpdateCnt());
//							// any update,delete or insert must be set to NONE, because no resultset will exists 
//							this.resultMode = TestProperties.RESULT_MODES.NONE;
//		
//						} else if (qsql.getRowCnt() >= 0) {
//							this.assertRowCount(qsql.getRowCnt());
//		
//						} else if (this.resultMode.equalsIgnoreCase(TestProperties.RESULT_MODES.NONE)) {
//							// for the NONE option, read all the rows
//							this.getRowCount();
//						}
					}
				}
			}

		} catch (Throwable t) {
			if (ClientPlugin.LOGGER.isDebugEnabled()) {
				t.printStackTrace();
			}
			this.setApplicationException(t);
		} 
	}

	@Override
	public void after() {
		final Throwable resultException = (this.getLastException() != null ? this
				.getLastException() : this.getApplicationException());

		if (resultException != null || this.internalResultSet == null) {
			if (this.exceptionExpected()) {
				rs.setStatus(TestResult.RESULT_STATE.TEST_EXPECTED_EXCEPTION);
			} else {
				rs.setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);
			}

		}

		rs.setBeginTS(beginTS);
		rs.setEndTS(endTS);
		rs.setException(resultException);
		rs.setUpdateCount(this.updateCount);
		
		this.testResultsSummary.addTestResult(query.geQuerySetID(), rs);

		// if the test was NOT originally resultMode = NONE, but was changed because 
		// of certain conditions, then need to handle the test results if an error occurs
		if (this.rs.getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.NONE) && 
				! this.originalResultMode.equalsIgnoreCase(TestProperties.RESULT_MODES.NONE) )  {
			if (this.rs.getStatus() == TestResult.RESULT_STATE.TEST_EXCEPTION) {
				this.querySet.getErrorWriter().generateErrorFile(rs.getQuerySetID(), rs.getQueryID(),
						 rs.getQuery(), (ResultSet) null, rs.getException(), (Object) null);
			}
		} else {
			this.querySet.handleTestResult(rs, this.internalResultSet);
		}
		
		// call at the end to close resultset and statements
		super.after();

	}

	@Override
	protected Statement createStatement() throws SQLException {
		return this.internalConnection.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}

	// need to override this method because the abstract logic for throwing
	// exceptions depends on this
	@Override
	public boolean exceptionExpected() {
		return this.errorExpected;
	}



}
