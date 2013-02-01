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

package org.jboss.bqt.client.api;

import java.util.Properties;

import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.framework.ActualTest;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TransactionAPI;

/**
 * An ExpectedResultsReader represents one set of expected results (referred to as the
 * queryset) that will be read when expected results are needed in the testing process.  
 * The query files should be found in the {@link TestProperties#PROP_EXPECTED_RESULTS_DIR_LOC executedResultsDir}. The
 * <code>queryID</code> identify a unique query and corresponds to the
 * expected results file.
 * 
 * 
 * @author vanhalbert
 * 
 */
public abstract class ExpectedResultsReader {
	
	private Properties properties;
	private QueryScenario scenario;
	private String querySetID;
	private String results_dir_loc = null;

	
	public ExpectedResultsReader(QueryScenario scenario, String querySetID, Properties props) {
		this.properties = props;
		this.scenario = scenario;
		this.querySetID = querySetID;
		
		this.results_dir_loc = props.getProperty(TestProperties.PROP_EXPECTED_RESULTS_DIR_LOC);
		if (this.results_dir_loc == null) {
			BQTUtil.throwInvalidProperty(TestProperties.PROP_EXPECTED_RESULTS_DIR_LOC);
		}

	}
	
	protected Properties getProperties() {
		return properties;
	}
	
	protected QueryScenario getQueryScenario() {
		return scenario;
	}
	
	/**
	 * Return the unique identifier for this query set.
	 * 
	 * @return QuerySetID
	 */
	public String getQuerySetID() {
		return this.querySetID;
	}

	/**
	 * Returns the full path to the expected results location.
	 * @return String full directory path
	 * 
	 * @see TestProperties#PROP_EXPECTED_RESULTS_DIR_LOC
	 */
	public String getExpectResultsLocation() {
		return this.results_dir_loc ;
	}

	/**
	 * Returns the <code>File</code> location for the actual results for the
	 * specified query identifier.
	 * 
	 * @param queryTest
	 * @return ExpectedResults
	 * @throws FrameworkRuntimeException
	 * 
	 * @since
	 */
	public abstract ExpectedResults getExpectedResults(ActualTest queryTest) throws FrameworkRuntimeException;

	/**
	 * Called to compare the <code>ResultSet</code> from the executed query to
	 * the expected results and return the errors.
	 * @param testCase 
	 * @param transaction 
	 * @param expectedResults 
	 * @param isOrdered 
	 * @throws QueryTestFailedException is thrown if actual don't match expected
	 */
	
	public abstract void compareResults(final TestCase testCase,
			final TransactionAPI transaction, ExpectedResults expectedResults, final boolean isOrdered) throws QueryTestFailedException;

	

}
