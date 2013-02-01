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

package org.jboss.bqt.client.results.teiid;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.TestResultSetUtil;
import org.jboss.bqt.framework.AbstractQuery;
import org.jboss.bqt.framework.ActualTest;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TransactionAPI;
import org.teiid.jdbc.TeiidStatement;

public class TeiidQueryPlanReader extends ExpectedResultsReader {
	
	
	private static final String NO_DIFF = "[0]";
	
	private ExpectedResults lastResults;
	private ActualTest lastTest;

	
	public TeiidQueryPlanReader(QueryScenario scenario, String querySetID, Properties props) {
		super(scenario, querySetID, props);

		File dir = new File(this.getExpectResultsLocation() + File.separator + querySetID);
		if (!dir.exists()) {
			if (this.getQueryScenario().isExpectedResultsNeeded()) {			
				throw new FrameworkRuntimeException("Query expected results directory "
						+ dir.getAbsolutePath() + " does not exist");
			}
		} else if (dir.list() == null) {
			throw new FrameworkRuntimeException("Query expected results directory "
					+ dir.getAbsolutePath() + " does not contain any files");
		}

		ClientPlugin.LOGGER.debug("XMLTeiidQueryPlanReader expected results loc: " + dir.getAbsolutePath());
	}

	
	@Override
	public synchronized ExpectedResults getExpectedResults(ActualTest queryTest) {
		if (lastTest != null && lastTest.equals(queryTest)) {
			return lastResults;
		}
		
		QueryTest qt = (QueryTest) queryTest;
		ExpectedResults es = new QueryPlanExpectedResults(qt.getQuerySetID(), qt.getQueryID());
		
		File er = findExpectedResultsFile(qt, this.getQuerySetID());
		if (er != null) {
			es.setExpectedResultsFile(er);
		}
		return es;
	}
	
	/**
	 * Compare the results of a query with those that were expected.
	 * @param testcase 
	 * @param transaction 
	 * @param isOrdered 
	 * 
	 * 
	 * @throws QueryTestFailedException
	 *             If comparison fails.
	 */
	@Override
	public void compareResults(final TestCase testcase,
			final TransactionAPI transaction, final ExpectedResults expectedResults, final boolean isOrdered) throws QueryTestFailedException {

		// no processing of exceptions, will already have been handled
		if (testcase.getTestResult().isFailure()) {
			return;
		}
		
		if (expectedResults == null || expectedResults.getExpectedResultsFile() == null) return;

		TeiidStatement statement = (TeiidStatement)  ((AbstractQuery) transaction).getStatement();
		
		if (statement.getPlanDescription() == null) {
			throw new FrameworkRuntimeException("QueryPlan is not available, check that SHOWPLAN=ON|DEBUG is specified on the URL");
		}
		
		String xml = statement.getPlanDescription().toXml();
		
    	List differences = null;
		try {
			differences = TeiidUtil.compareToResults(xml,  null, testcase.getTestResult().getQuery(), expectedResults.getExpectedResultsFile(), false);

		} catch (IOException e) {
			e.printStackTrace();
			throw new FrameworkRuntimeException(e);
		} 

		if (differences != null && differences.size() > 0) {
			if (!differences.get(0).equals(NO_DIFF)) {
	    		ClientPlugin.LOGGER.info("**** LINE DIFFERENCES for " + testcase.getTestResult().getQuerySetID() + ":" + testcase.getTestResult().getQueryID());
	    		throw new QueryTestFailedException(testcase.getTestResult().getQuerySetID() + ":" + testcase.getTestResult().getQueryID() +
	    				" - Query Plans dont match: (lines) " + differences.toString());				
			}			
		}

	}

	private File findExpectedResultsFile(QueryTest test,
			String querySetIdentifier)  {
		String resultFileName = this.getQueryScenario().getFileType().getExpectedResultsFileName(this.getQueryScenario(), test, ".pln");
			//queryIdentifier + ".xml"; //$NON-NLS-1$
		File file = new File(this.getExpectResultsLocation() + File.separator + this.getQuerySetID(),
				resultFileName);
		if (!file.exists()) {
			// only a warning, because if there is an expected error, then there is no query plan created when resultmode = GENERATE
			ClientPlugin.LOGGER.warn("XMLTeiidQueryPlanReader expected results file: " + file.getAbsolutePath() + " cannot be found");
			return null;
		}

		return file;

	}
}

class QueryPlanExpectedResults extends ExpectedResults {
	QueryPlanExpectedResults(String querySetID, String queryID) {
		super(querySetID, queryID);
	}

}
