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

package org.jboss.bqt.test.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.bqt.test.client.results.TestResultStat;
import org.jboss.bqt.test.framework.AbstractQueryTransactionTest;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.core.util.TestLogger;

/**
 * TestClientTransaction
 * 
 */
public class TestClientTransaction extends AbstractQueryTransactionTest {

	private QueryScenario querySet = null;
	private ExpectedResults expectedResults = null;
	private QueryTest query = null;

	private QuerySQL[] queries = null;

	private int testStatus = TestResult.RESULT_STATE.TEST_SUCCESS;

	private boolean errorExpected = false;

	private String sql = null;
	private String resultMode = TestProperties.RESULT_MODES.NONE;

	private boolean resultFromQuery = false;

	private TestResultsSummary testResultsSummary;

	public TestClientTransaction(QueryScenario querySet) {
		super(querySet.getQueryScenarioIdentifier());
		this.querySet = querySet;
		this.resultMode = this.querySet.getResultsMode();

	}

	public void init(TestResultsSummary testResultsSummary,
			ExpectedResults expectedResults, QueryTest query) {
		this.query = query;
		this.testResultsSummary = testResultsSummary;
		this.expectedResults = expectedResults;

		testStatus = TestResult.RESULT_STATE.TEST_SUCCESS;

		errorExpected = false;
		resultFromQuery = false;
		
		queries = query.getQueries();
		// multiple queries cannot be processed as a single result
		// therefore, the other options are not valid
		if (queries.length > 1) {
			this.resultMode = TestProperties.RESULT_MODES.NONE;
		}


	}

	public String getTestName() {
		return query.getQueryScenarioID() + ":"
				+ (query.getQueryID() != null ? query.getQueryID() : "NA");

	}

	@Override
	public void before() {
		// TODO Auto-generated method stub
		super.before();

		try {
			this.errorExpected = expectedResults.isExceptionExpected(query
					.getQueryID());
		} catch (QueryTestFailedException e) {
			// TODO Auto-generated catch block
			throw new TransactionRuntimeException("ProgramError: "
					+ e.getMessage());
		}

	}

	@Override
	public void testCase() throws Exception {
		TestLogger.logDebug("expected error: " + this.errorExpected);
		TestLogger.logInfo("ID: " + query.geQuerySetID() + "  -  "
				+ query.getQueryID() );

		int l = queries.length;

		try {
			// need to set this so the underlying query execution handles an
			// error properly.

			for (int i = 0; i < l; i++) {
				QuerySQL qsql = queries[i];
				this.sql = qsql.getSql();
				
				// if runtimes or rowcounts are greater than 1, then no expected results will
				// be processed, therefore, resultmode is set to NONE for this query
				if (qsql.getRunTimes() > 1 || qsql.getRowCnt() > 0) {
					this.resultMode = TestProperties.RESULT_MODES.NONE;

				}
				
				TestLogger.logInfo("ResultMode: " + this.resultMode + ", numtimes: " + qsql.getRunTimes() + " rowcount: "  + qsql.getRowCnt() + " updatecnt: " + qsql.getUpdateCnt());

				
				for (int r = 0; r < qsql.getRunTimes(); r++) {
				
					resultFromQuery = execute(sql, qsql.getParms(), qsql.getPayLoad());
					if (! querySet.isGenerate()) {
						if (qsql.isSelect()) {
							if (qsql.getRowCnt() >= 0) {
								this.assertRowCount(qsql.getRowCnt());
							} else if (this.resultMode.equalsIgnoreCase(TestProperties.RESULT_MODES.NONE)) {
								// for the NONE option, read all the rows
								this.getRowCount();
							}
						} else {
							// for cases where a SET statement, or other type is called, but not an update,
							// then allow it to continue
							// anything other than select (set, update,delete or insert) must be set to NONE, because no resultset will exists 

							this.resultMode = TestProperties.RESULT_MODES.NONE;
							
							if (qsql.getUpdateCnt() >= 0) {
								this.assertUpdateCount(qsql.getUpdateCnt());
							}							
						}
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
			this.setApplicationException(t);
		} 
	}

	@Override
	public void after() {
		// TODO Auto-generated method stub
		TestResult rs = null;

		Throwable resultException = null;

		resultException = (this.getLastException() != null ? this
				.getLastException() : this.getApplicationException());

		if (resultException != null) {
			if (this.exceptionExpected()) {
				testStatus = TestResult.RESULT_STATE.TEST_EXPECTED_EXCEPTION;
			} else {
				testStatus = TestResult.RESULT_STATE.TEST_EXCEPTION;
			}

		}

		rs = new TestResultStat(query.geQuerySetID(), query.getQueryID(), sql,
				testStatus, beginTS, endTS, resultException, null);
		rs.setResultMode(this.resultMode);
		rs.setUpdateCount(this.updateCount);

		this.testResultsSummary.addTestResult(query.geQuerySetID(), rs);

		if (!this.resultMode.equalsIgnoreCase(TestProperties.RESULT_MODES.NONE))  {
			this.querySet.handleTestResult(rs, this.internalResultSet, resultFromQuery);
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
