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

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.ClassFactory;
import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.TestProperties.RESULT_MODES;
import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;

/**
 * The QueryScenario manages all the information required to run one scenario of
 * tests. This includes the following: <li>The queryreader and its query sets to
 * be executed as a scenario</li> <li>Provides the expected results that
 * correspond to a query set</li> <li>The results generator that would be used
 * when {@link RESULT_MODES#GENERATE} is specified</li>
 * 
 * @author vanhalbert
 * 
 */
public abstract class QueryScenario {
	
	enum RESULT_MODE {
		COMPARE,
		GENERATE,
		SQL,
		NONE
	}

	protected QueryReader reader = null;
	protected ExpectedResultsWriter resultsGen = null;
	protected ErrorWriter errorWriter = null;

	private Properties props = null;
	private String scenarioName;
	private String querySetName;
	private String rootOutputDir = null;
	private String testrunDir = null;
	
	private RESULT_MODE mode = RESULT_MODE.NONE;
	
	public QueryScenario(String scenarioName, Properties queryProperties) {
		this.props = queryProperties;
		this.scenarioName = scenarioName;

		this.querySetName = props.getProperty(TestProperties.QUERY_SET_NAME);
		
		if (this.querySetName == null) {
			BQTUtil.throwInvalidProperty(TestProperties.QUERY_SET_NAME);
		}
		
		this.rootOutputDir = props.getProperty(TestProperties.PROP_OUTPUT_DIR);
		if (this.rootOutputDir == null) {
			BQTUtil.throwInvalidProperty(TestProperties.PROP_OUTPUT_DIR);
		}
		
		// at the top directory level, only create if it doesn't exist,
		// do not delete, because it may contain other test results
		// for other result.mode run's
		File d = new File(this.rootOutputDir);
		if (!d.exists()) {
			d.mkdirs();
		}
		
		this.testrunDir = props.getProperty(TestProperties.PROP_TESTRUN_DIR);
		if (this.testrunDir == null) {
			BQTUtil.throwInvalidProperty(TestProperties.PROP_TESTRUN_DIR);
		}
		
		setUp();

	}

	protected void setUp() {
		validateResultsMode(this.props);
		
		Collection<Object> args = new ArrayList<Object>(2);
		args.add(scenarioName);
		args.add(props);
		
		if (!this.isSQL()) {			
			reader = ClassFactory.createQueryReader(args);
	
			if (reader.getQuerySetIDs() == null
					|| reader.getQuerySetIDs().isEmpty()) {
				throw new FrameworkRuntimeException(
						"The queryreader did not return any queryset ID's to process");
			}
		} 
		if (this.isGenerate()) {
			this.resultsGen = ClassFactory.createExpectedResultsWriter(args);
		}
		
		// NONE will not create error files
		if (!this.isNone()) {
			this.errorWriter = ClassFactory.createErrorWriter(args);
		}
	}
	
	public String getOutputDir() {
		return this.rootOutputDir;
	}
	
	public String getTestRunDir() {
		return this.testrunDir;
	}
	
	
	public boolean isNone() {
		return (mode == RESULT_MODE.NONE);
	}	
	
	public boolean isGenerate() {
		return (mode == RESULT_MODE.GENERATE);
	}

	public boolean isSQL() {
		return (mode == RESULT_MODE.SQL);
	}
	
	public boolean isCompare() {
		return (mode == RESULT_MODE.COMPARE);
	}
	

	public void setResultMode(String resultmode) {
		
		// default to NONE when its null
		resultmode = (resultmode != null ? resultmode : TestProperties.RESULT_MODES.NONE);
		
		if (resultmode.equalsIgnoreCase(TestProperties.RESULT_MODES.COMPARE)) {
			mode = RESULT_MODE.COMPARE;
		} else if (resultmode.equalsIgnoreCase(TestProperties.RESULT_MODES.GENERATE)) {
			mode = RESULT_MODE.GENERATE;
		} else if (resultmode.equalsIgnoreCase(TestProperties.RESULT_MODES.SQL)) {
			mode = RESULT_MODE.SQL;
		} else if (resultmode.equalsIgnoreCase(TestProperties.RESULT_MODES.NONE)) {
			mode = RESULT_MODE.NONE;
		} else {
			throw new FrameworkRuntimeException(
			"Invalid results mode of "+ resultmode + " must be COMPARE, GENERATE, SQL or NONE");

		}
	}
	protected void validateResultsMode(Properties props) {
		// Determine from property what to do with query results
		
		String resultModeStr = TestProperties.getResultMode(props);
		
		setResultMode( resultModeStr );
		// otherwise use default of NONE

		ClientPlugin.LOGGER.info("Results mode: " + mode); //$NON-NLS-1$

	}

	/**
	 * Return the name that identifies this query set. It should use the
	 * {@link TestProperties#QUERY_SET_NAME} property to obtain the name.
	 * 
	 * @return String query set name;
	 */
	public String getQuerySetName() {
		return this.querySetName;
	}

	/**
	 * Return the identifier for the current scenario
	 * 
	 * @return String name of scenario
	 */
	public String getQueryScenarioIdentifier() {
		return this.scenarioName;
	}

	/**
	 * Return the properties defined for this scenario
	 * 
	 * @return Properties
	 */
	public Properties getProperties() {
		return this.props;
	}

	/**
	 * Return a <code>Map</code> containing the query identifier as the key, and
	 * the value is the query. In most simple cases, the query will be a
	 * <code>String</code> However, complex types (i.e., to execute prepared
	 * statements or other arguments), it maybe some other type.
	 * 
	 * @param querySetID
	 *            identifies a set of queries
	 * @return Map<String, Object>
	 */

	public List<QueryTest> getQueries(String querySetID) {
		try {
			return reader.getQueries(querySetID);
		} catch (QueryTestFailedException e) {
			throw new FrameworkRuntimeException(e);
		}
	}

	/**
	 * Return a <code>Collection</code> of <code>querySetID</code>s that the
	 * {@link QueryReader} will be providing. The <code>querySetID</code> can be
	 * used to obtain it associated set of queries by call
	 * {@link #getQueries(String)}
	 * 
	 * @return Collection of querySetIDs
	 */
	public Collection<String> getQuerySetIDs() {
		return reader.getQuerySetIDs();
	}

	/**
	 * Return the result mode that was defined by the property
	 * {@link TestProperties#PROP_RESULT_MODE}
	 * 
	 * @return String result mode
	 */
	public String getResultsMode() {
		
		switch (mode) {
		case COMPARE:
			return TestProperties.RESULT_MODES.COMPARE;
		case GENERATE:
			return TestProperties.RESULT_MODES.GENERATE;
		case SQL:
			return TestProperties.RESULT_MODES.SQL;
		case NONE:
			return TestProperties.RESULT_MODES.NONE;
			
		default:
			break;
		}

		return this.mode.toString();
	}

	/**
	 * Return the {@link ExpectedResultsReader} for the specified
	 * <code>querySetID</code>. These expected results will be used to compare
	 * with the actual results in order to determine success or failure.
	 * 
	 * @param querySetID
	 * @return ExpectedResultsReader
	 */
	public ExpectedResultsReader getExpectedResults(String querySetID) {
		Collection<Object> args = new ArrayList<Object>(2);
		args.add(querySetID);
		args.add(props);

		return ClassFactory.createExpectedResults(args);

	}

	/**
	 * Return the {@link ExpectedResultsWriter} that is to be used to create new sets
	 * of expected results.
	 * 
	 * @return ExpectedResultsWriter
	 */
	public ExpectedResultsWriter getExpectedResultsGenerator() {
		return this.resultsGen;
	}

	/**
	 * Return the {@link QueryReader} that is to be used to obtain the queries
	 * to process.
	 * 
	 * @return QueryReader
	 */
	public QueryReader getQueryReader() {
		return this.reader;
	}
	
	/**
	 * Return the {@link ErrorWriter} that is to be used to write
	 * the error files.
	 * 
	 * @return QueryReader
	 */
	public ErrorWriter getErrorWriter() {
		return this.errorWriter;
	}
	
	/**
	 * Return the {@link QueryWriter} that is to be used to writer 
	 * to process.
	 * 
	 * @return QueryWriter
	 */
	public QueryWriter getQueryWriter() {
		final String msg = ClientPlugin.Util.getString(
				"QueryScenario.unsupportedMethod", "QueryScenario.getQueryWriter"); //$NON-NLS-1$            
		throw new FrameworkRuntimeException(msg);
	}	

	public abstract void handleTestResult(TestResult tr, ResultSet resultSet,
			boolean resultFromQuery);
	
	public void writeQueryTests(QueryTest queryTest) throws Exception {
		getQueryWriter().writeQueryTest(queryTest);

	}	
}
