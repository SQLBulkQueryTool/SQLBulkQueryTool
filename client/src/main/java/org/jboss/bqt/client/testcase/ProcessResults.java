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

import java.util.Iterator;
import java.util.List;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QuerySQL;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.TestResultsSummary;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.framework.AbstractQuery;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestCaseLifeCycle;
import org.jboss.bqt.framework.TestResult;
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
	
	private AbstractQuery abQuery;

	public ProcessResults(QueryScenario scenario) {
		super();
		this.scenario = scenario;
		
	}
	
	public String getTestName() {
		return scenario.getQuerySetName() + ":" + scenario.getQueryScenarioIdentifier();
	}
	
	
	public void setup(TransactionAPI transaction) {
		this.trans = transaction;
		abQuery = ((AbstractQuery) trans);
		
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

				ClientPlugin.LOGGER.info("Start TestResult:  QuerySetID [" + querySetID + "]");

				final List<QueryTest> queryTests = scenario.getQueries(querySetID);

				// the iterator to process the query tests
				Iterator<QueryTest> queryTestIt = queryTests.iterator();

				long beginTS = System.currentTimeMillis();

				while (queryTestIt.hasNext()) {
					QueryTest q = queryTestIt.next();
					
					TestResult testResult = new TestResult(q.getQuerySetID(), q.getQueryID());
					
					TestCase testcase = new TestCase(q);
					testcase.setTestResult(testResult);
					
					ClientPlugin.LOGGER.debug("Test: QuerySetID [" + testResult.getQuerySetID() + "-" + testResult.getQueryID() +"]");

					testResult.setResultMode(this.scenario.getResultsMode());
					testResult.setStatus(TestResult.RESULT_STATE.TEST_PRERUN);
					
					try {			
						abQuery.before(testcase);
						
						executeTest(testcase);
						
					} catch (QueryTestFailedException qtfe) {
						// dont set on testResult, handled in transactionAPI
						
					} catch (Exception rme) {
						if (ClientPlugin.LOGGER.isDebugEnabled()) {
							rme.printStackTrace();
						}
						abQuery.setApplicationException(rme);

					} finally {
						abQuery.after();
					}
						
					after(testcase);
					
					trans.cleanup();
				
				}

				long endTS = System.currentTimeMillis();

				ClientPlugin.LOGGER.info("End TestResult: QuerySetID [" + querySetID + "]");

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
			} catch (Exception e) {
				if (fre == null) {
					throw new FrameworkRuntimeException(e);
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


	public void executeTest(TestCase testcase) throws Exception {
		
		QueryTest test = (QueryTest) testcase.getActualTest();
		TestResult testResult = testcase.getTestResult();
		
		QuerySQL[] queries =  test.getQueries();
		
		int l = queries.length;
		
		boolean resultModeNone = scenario.isNone();
	
		
		// multiple queries cannot be processed as a single result
		// therefore, only the NONE result mode is valid
		if (l > 1) {
			resultModeNone = true;
			testResult.setResultMode(TestProperties.RESULT_MODES.NONE);
			if (!scenario.isNone()) {
				ClientPlugin.LOGGER.info("Overriding ResultMode to NONE, multiple queries for QueryID [" + testResult.getQueryID() + "]");
			}

		} 

		for (int i = 0; i < l; i++) {
			QuerySQL qsql = queries[i];
			testResult.setQuery(qsql.getSql());
			
			// if runtimes or rowcounts are greater than 1, then no expected results will
			// be processed, therefore, resultmode is set to NONE for this query
			if ( !resultModeNone && (qsql.getRunTimes() > 1 || qsql.getRowCnt() > 0)) {
				resultModeNone = true;
				testResult.setResultMode(TestProperties.RESULT_MODES.NONE);
				ClientPlugin.LOGGER.info("Overriding ResultMode to NONE due to runtimes or rowcount for QueryID [" + testResult.getQueryID() + "]");
			}
			
			ClientPlugin.LOGGER.debug("Expecting - ID: " + test.getQuerySetID() + "  -  "
					+ test.getQueryID() + "ResultMode: " + (resultModeNone ? "NONE" : scenario.getResultsMode()) + ", numtimes: " +
					qsql.getRunTimes() + " rowcount: "  + qsql.getRowCnt() + " updatecnt: " + 
					qsql.getUpdateCnt());
						
			for (int r = 0; r < qsql.getRunTimes(); r++) {

				abQuery.execute(testResult.getQuery(), qsql.getParms(), qsql.getPayLoad());
				// check for NONE first, because it can be changed based on conditions
				// NOTE: isSQL() isn't processed in this class and therefore isn't looked for
				if (resultModeNone) {
						if (qsql.getRowCnt() >= 0) {
							testResult.setRowCount(abQuery.getRowCount());					
							AssertResults.assertRowCount(testResult, qsql.getRowCnt());
						} else if (qsql.getUpdateCnt() >= 0) {
							AssertResults.assertUpdateCount(testResult, qsql.getUpdateCnt());
						}		

				} else if (scenario.isCompare()) {
					// no rowcount or update counts can be checked in compare
					// because if comparing expected results, the count will be done at that time
					// if its an update, then no expected results would exist, and therefore,
					// the NONE option should be used for update checks.
					
				} else if (scenario.isGenerate()) {
					// do nothing
				}	
			}			
		}		
	}
	
	private void after(TestCase testcase) {
		
		FrameworkRuntimeException lastT = null;
		try {
			
			if (testcase.getTestResult().isFailure()) {
				testcase.getTestResult().setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);
			} else {
				testcase.getTestResult().setStatus(TestResult.RESULT_STATE.TEST_SUCCESS);
			}
	
			// if the test was NOT originally resultMode = NONE, but was changed because 
			// of certain conditions, then need to handle the test results if an error occurs
			if (testcase.getTestResult().getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.NONE) && 
					! this.scenario.isNone()) {
				if (testcase.getTestResult().getStatus() == TestResult.RESULT_STATE.TEST_EXCEPTION) {
						this.scenario.getErrorWriter().generateErrorFile(testcase, null, (TransactionAPI) null, testcase.getTestResult().getException());
				}
			} else {
				this.scenario.handleTestResult(testcase, trans);
			}
						
			this.scenario.getTestResultsSummary().addTest(testcase.getTestResult().getQuerySetID(), testcase.getTestResult());

		} catch (FrameworkRuntimeException t) {
			lastT = t;
		} catch (Exception t) {
			t.printStackTrace();
			lastT = new FrameworkRuntimeException(t.getMessage());
		} finally {
		
		// call at the end to close resultset and statements
			if (lastT != null) throw lastT;
		}

	}

}
