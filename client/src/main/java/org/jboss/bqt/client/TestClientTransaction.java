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
import org.jboss.bqt.framework.TransactionQueryTestCase;

/**
 * TestClientTransaction
 * 
 */
public class TestClientTransaction extends TransactionQueryTestCase {

	private QueryScenario queryScenario = null;
	
	private QueryTest query = null;

	private boolean resultFromQuery = false;
	
	private TestResult rs = null;

	private TestResultsSummary testResultsSummary;

	public TestClientTransaction(QueryScenario querySet) {
		super(querySet.getQueryScenarioIdentifier());
		this.queryScenario = querySet;
		
	}

	public void init(TestResultsSummary testResultsSummary,
			QueryTest query) {
		this.query = query;
		this.testResultsSummary = testResultsSummary;
		
		rs = new TestResultStat(query.getQuerySetID(), query.getQueryID());
		rs.setResultMode(this.queryScenario.getResultsMode());
		rs.setStatus(TestResult.RESULT_STATE.TEST_SUCCESS);

		resultFromQuery = false;
	}

	@Override
	public String getTestName() {
		return query.getQueryScenarioID() + ":"
				+ (query.getQueryID() != null ? query.getQueryID() : "NA");

	}

	@Override
	public void testCase()  {
		if (this.getApplicationException() != null) return;

		QuerySQL[] queries = query.getQueries();
		
		int l = queries.length;

		try {
			
			// multiple queries cannot be processed as a single result
			// therefore, only the NONE result mode is valid
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
				
				ClientPlugin.LOGGER.debug("ID: " + query.getQuerySetID() + "  -  "
						+ query.getQueryID() + "ResultMode: " + this.rs.getResultMode() + ", numtimes: " +
						qsql.getRunTimes() + " rowcount: "  + qsql.getRowCnt() + " updatecnt: " + 
						qsql.getUpdateCnt());

				
				for (int r = 0; r < qsql.getRunTimes(); r++) {
				
					resultFromQuery = execute(this.rs.getQuery(), qsql.getParms(), qsql.getPayLoad());
					// check for NONE first, because it can be changed based on conditions
					// NOTE: isSQL() isn't processed in this class and therefore isn't looked for
					if (this.rs.isResultModeNone()) {
						if (resultFromQuery) {
							if (qsql.getRowCnt() >= 0) {
								assertRowCount(qsql.getRowCnt());
							} else if (this.rs.isResultModeNone()) {
								// for the NONE option, read all the rows
								getRowCount();
							}	
						} else {
							if (qsql.getUpdateCnt() >= 0) {
								this.assertUpdateCount(qsql.getUpdateCnt());
							}		
						}

					} else if (queryScenario.isCompare()) {
						// on single queries, row count checks can still be specified and checked
							if (qsql.getRowCnt() >= 0) {
								this.assertRowCount(qsql.getRowCnt());
							} else if (qsql.getUpdateCnt() >= 0) {
								this.assertUpdateCount(qsql.getUpdateCnt());
							}
					} else if (queryScenario.isGenerate()) {
						// do nothing
					}

						
//					if (queryScenario.isNone() || queryScenario.isCompare() ) {
//						if (resultFromQuery) {
//							if (qsql.getRowCnt() >= 0) {
//								this.assertRowCount(qsql.getRowCnt());
//							} else if (this.rs.isResultModeNone()) {
//								// for the NONE option, read all the rows
//								this.getRowCount();
//							}							
//							
//						} else {
//							// if no results from query, and this isn't a GENERATE scenario
//							//  
//							this.rs.setResultMode(TestProperties.RESULT_MODES.NONE);
//							
//							if (qsql.getUpdateCnt() >= 0) {
//								this.assertUpdateCount(qsql.getUpdateCnt());
//							}							
//							
//						}
//
////						if (qsql.getUpdateCnt() >= 0) {
////							this.assertUpdateCount(qsql.getUpdateCnt());
////							// any update,delete or insert must be set to NONE, because no resultset will exists 
////							this.resultMode = TestProperties.RESULT_MODES.NONE;
//
//
//					}
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
		FrameworkRuntimeException lastT = null;
		try {
			final Throwable resultException = (this.getLastException() != null ? this
					.getLastException() : this.getApplicationException());
	
			if (resultException != null) {
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
			
			this.testResultsSummary.addTestResult(query.getQuerySetID(), rs);
	
			// if the test was NOT originally resultMode = NONE, but was changed because 
			// of certain conditions, then need to handle the test results if an error occurs
			if (this.rs.getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.NONE) && 
					! this.queryScenario.isNone()) {
				if (this.rs.getStatus() == TestResult.RESULT_STATE.TEST_EXCEPTION) {
						this.queryScenario.getErrorWriter().generateErrorFile(rs.getQuerySetID(), rs.getQueryID(),
								 rs.getQuery(), (ResultSet) null, rs.getException(), (Object) null);
				}
			} else {
				this.queryScenario.handleTestResult(rs, this.internalResultSet);
			}
		} catch (FrameworkRuntimeException t) {
			lastT = t;
			
		} catch (Throwable t) {
			
			lastT = new FrameworkRuntimeException(t.getMessage());
		} finally {
		
		// call at the end to close resultset and statements
			try {
				super.after();
			} catch (Throwable t) {
				
			}
			if (lastT != null) throw lastT;
		}

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
		boolean errorExpected = false;
		ExpectedResultsReader expectedResults = this.queryScenario.getExpectedResults(query.getQuerySetID());
		if (expectedResults != null) {
			errorExpected = expectedResults.isExceptionExpected(query
					.getQueryID());
		}

		return errorExpected;
	}



}
