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

package org.jboss.bqt.client.testcase;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QuerySQL;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.TestResultsSummary;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.AbstractQuery;
import org.jboss.bqt.framework.Test;
import org.jboss.bqt.framework.TestCaseLifeCycle;
import org.jboss.bqt.framework.TransactionAPI;
import org.jboss.bqt.framework.util.AssertResults;

/**
 * ProcessResults is a TestCase that will process the results of a query test.  
 * For which the QueryScenario will be asked to handle any results generated
 * by the test case.
 * 
 */
public class ProcessResults implements TestCaseLifeCycle {

	private QueryScenario scenario = null;
	
	private TransactionAPI trans;

	public ProcessResults(QueryScenario scenario) {
		super();
		this.scenario = scenario;
		
	}
	
	public String getTestName() {
		return scenario.getQuerySetName() + ":" + scenario.getQueryScenarioIdentifier();
	}
	
	
	public void setup(TransactionAPI transaction) {
		this.trans = transaction;
		
	}
	
	public void runTestCase() {
		
		Iterator<String> qsetIt = scenario.getQuerySetIDs().iterator();
		
		TestResultsSummary summary = this.scenario.getTestResultsSummary();

		FrameworkRuntimeException fre = null;
		try {

			// iterate over the query set ID's, which there
			// should be 1 for each file to be processed
			while (qsetIt.hasNext()) {
				String querySetID = null;
				querySetID = qsetIt.next();

				ClientPlugin.LOGGER.info("Start Test:  QuerySetID [" + querySetID + "]");

				final List<QueryTest> queryTests = scenario.getQueries(querySetID);

				// the iterator to process the query tests
				Iterator<QueryTest> queryTestIt = queryTests.iterator();

				long beginTS = System.currentTimeMillis();

				while (queryTestIt.hasNext()) {
					QueryTest q = queryTestIt.next();
					
					Test test = new Test(q.getQuerySetID(), q.getQueryID());
					
					ClientPlugin.LOGGER.debug("New Test: QuerySetID [" + test.getQuerySetID() + "-" + test.getQueryID() +"]");

					test.setResultMode(this.scenario.getResultsMode());
					test.setStatus(Test.RESULT_STATE.TEST_PRERUN);
					
					try {					
						executeTest(q, test);
						
					} catch (Throwable rme) {
						if (ClientPlugin.LOGGER.isDebugEnabled()) {
							rme.printStackTrace();
						}

						test.setException(rme);
						test.setStatus(Test.RESULT_STATE.TEST_EXCEPTION);						
					} 
					
					after(test);
					
					trans.cleanup();
				
				}

				long endTS = System.currentTimeMillis();

				ClientPlugin.LOGGER.info("End Test: QuerySetID [" + querySetID + "]");

				try {
					summary.printResults(querySetID, beginTS, endTS);
				} catch (Exception e) {
					fre = new FrameworkRuntimeException(e);
					throw fre;
				}

			}

		} finally {
			try {
				summary.printTotals();
				summary.cleanup();	
			} catch (Throwable t) {
				if (fre == null) {
					throw new FrameworkRuntimeException(t);
				}
				throw fre;
			}
		}
	}
	
	public void cleanup() {
		if (trans != null) {
			trans.cleanup();
		}
		
		trans = null;

		this.scenario = null;
	}


	public void executeTest(QueryTest qt, Test test) throws Throwable {
		
		QuerySQL[] queries = qt.getQueries();
		
		int l = queries.length;
		
		boolean resultModeNone = scenario.isNone();
		
		AbstractQuery abQuery = ((AbstractQuery) trans);

		
		// multiple queries cannot be processed as a single result
		// therefore, only the NONE result mode is valid
		if (l > 1) {
			resultModeNone = true;
			test.setResultMode(TestProperties.RESULT_MODES.NONE);
			if (!scenario.isNone()) {
				ClientPlugin.LOGGER.info("Overriding ResultMode to NONE for QueryID [" + test.getQueryID() + "]");
			}

		} else if (scenario.isCompare()) {
			test.setExceptionExpected( scenario.exceptionExpected(test));
		}
		
		abQuery.before(test);		


		for (int i = 0; i < l; i++) {
			QuerySQL qsql = queries[i];
			test.setQuery(qsql.getSql());
			
			// if runtimes or rowcounts are greater than 1, then no expected results will
			// be processed, therefore, resultmode is set to NONE for this query
			if (qsql.getRunTimes() > 1 || qsql.getRowCnt() > 0) {
				resultModeNone = true;
				test.setResultMode(TestProperties.RESULT_MODES.NONE);
			}
			
			ClientPlugin.LOGGER.debug("Expecting - ID: " + qt.getQuerySetID() + "  -  "
					+ qt.getQueryID() + "ResultMode: " + (resultModeNone ? "NONE" : scenario.getResultsMode()) + ", numtimes: " +
					qsql.getRunTimes() + " rowcount: "  + qsql.getRowCnt() + " updatecnt: " + 
					qsql.getUpdateCnt());

						
			for (int r = 0; r < qsql.getRunTimes(); r++) {

				abQuery.execute(test.getQuery(), qsql.getParms(), qsql.getPayLoad());
				// check for NONE first, because it can be changed based on conditions
				// NOTE: isSQL() isn't processed in this class and therefore isn't looked for
				if (resultModeNone) {
						if (qsql.getRowCnt() >= 0) {
							test.setRowCount(abQuery.getRowCount());					
							AssertResults.assertRowCount(test, qsql.getRowCnt());
						} else if (qsql.getUpdateCnt() >= 0) {
							AssertResults.assertUpdateCount(test, qsql.getUpdateCnt());
						}		

				} else if (scenario.isCompare()) {
					// on single queries, row count checks can still be specified and checked
						if (qsql.getRowCnt() >= 0) {
							test.setRowCount(abQuery.getRowCount());
							AssertResults.assertRowCount(test, qsql.getRowCnt());
						} else if (qsql.getUpdateCnt() >= 0) {
							AssertResults.assertUpdateCount(test, qsql.getUpdateCnt());
						}
				} else if (scenario.isGenerate()) {
					// do nothing
				}	
			}			
		}
		
		abQuery.after();

	}
	
	private void after(Test test) {
		
		FrameworkRuntimeException lastT = null;
		try {
			final Throwable resultException = test.getException();
	
			if (resultException != null) {
				if (test.isExceptionExpected()) {
					test.setStatus(Test.RESULT_STATE.TEST_EXPECTED_EXCEPTION);
				} else {
					test.setStatus(Test.RESULT_STATE.TEST_EXCEPTION);
				}
			} else {
				test.setStatus(Test.RESULT_STATE.TEST_SUCCESS);
			}
			
			this.scenario.getTestResultsSummary().addTest(test.getQuerySetID(), test);
	
			// if the test was NOT originally resultMode = NONE, but was changed because 
			// of certain conditions, then need to handle the test results if an error occurs
			if (test.getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.NONE) && 
					! this.scenario.isNone()) {
				if (test.getStatus() == Test.RESULT_STATE.TEST_EXCEPTION) {
						this.scenario.getErrorWriter().generateErrorFile(test, (ResultSet) null, (Object) null);
				}
			} else {
				this.scenario.handleTestResult(test, ((AbstractQuery) trans).getResultSet());
			}
		} catch (FrameworkRuntimeException t) {
			lastT = t;
		} catch (Throwable t) {
			t.printStackTrace();
			lastT = new FrameworkRuntimeException(t.getMessage());
		} finally {
		
		// call at the end to close resultset and statements
			if (lastT != null) throw lastT;
		}

	}

}
