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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.FileType;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.TestProperties.RESULT_MODES;
import org.jboss.bqt.client.TestResultsSummary;
import org.jboss.bqt.client.resultmode.Compare;
import org.jboss.bqt.client.resultmode.CreateSQLQuery;
import org.jboss.bqt.client.resultmode.GenerateExpectedResults;
import org.jboss.bqt.client.resultmode.None;
import org.jboss.bqt.client.testcase.ProcessResults;
import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.ReflectionHelper;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestCaseLifeCycle;
import org.jboss.bqt.framework.TransactionAPI;
import org.jboss.bqt.core.util.ArgCheck;

/**
 * The QueryScenario manages all the information required to run one scenario of
 * tests. This includes the following: <li>The queryreader and its query sets to
 * be executed as a scenario</li> <li>Provides the expected results that
 * correspond to a query set</li> <li>The results generator that would be used
 * when {@link RESULT_MODES#GENERATE} is specified</li>
 * 
 * 
 * 
 * @author vanhalbert
 * 
 */
public abstract class QueryScenario {

	protected FileType fileType = null;
	
	private QueryReader reader = null;
	protected QueryWriter writer = null;
	protected ExpectedResultsReader resultsReader = null;
	
	private List<ExpectedResultsWriter> resultsWriters = null;
	private ErrorWriter errorWriter = null;
	private TestResultsSummary summary = null;

	private Properties props = null;
	private String scenarioName;
	private String querySetName;
	private String rootOutputDir = null;
	private String testrunDir = null;
		
	public static final QueryScenario createInstance(String scenarioName, Properties properties) {
		
		String resultModeStr = BQTUtil.getResultMode(properties);
		resultModeStr = (resultModeStr!=null?resultModeStr:"Null");
		
		QueryScenario scenario = null;
		if (resultModeStr.equals(TestProperties.RESULT_MODES.COMPARE)) {
			scenario = new Compare(scenarioName, properties);
		} else if (resultModeStr.equals(TestProperties.RESULT_MODES.GENERATE)) {
			scenario = new GenerateExpectedResults(scenarioName, properties);
		} else if (resultModeStr.equals(TestProperties.RESULT_MODES.NONE)) {
			scenario = new None(scenarioName, properties);
		} else {
			// default is SQL
			scenario = new CreateSQLQuery(scenarioName, properties);
		}
		
		scenario.setUp();
		
		ClientPlugin.LOGGER.info("Results mode: " +  resultModeStr + "-" + scenario.getClass().getSimpleName()); //$NON-NLS-1$
		
		return scenario;		
	}
	
	protected QueryScenario(String scenarioName, Properties queryProperties)  {
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
		
		fileType = BQTUtil.createFileType(queryProperties);
		ArgCheck.isNotNull(fileType);
		
		summary = new TestResultsSummary(this);

	}

	/**
	 * The implementation of setUp will create the appropriate readers/writers that
	 * will be required that instance of QueryScenario.
	 */
	protected void setUp() {
	}	
	
	
	/**
	 * Call to obtain the {@link TestCaseLifeCycle testcase} that will run
	 * this scenario
	 * @return TestCaseLifeCycle
	 */
	public TestCaseLifeCycle getTestCase() {
		return  new ProcessResults(this);
	}

	public String getOutputDir() {
		return this.rootOutputDir;
	}
	
	public String getTestRunDir() {
		return this.testrunDir;
	}
	
	public FileType getFileType() {
		return this.fileType;
	}
	 
	public boolean isNone() {
		return false;
	}	
	
	public boolean isGenerate() {
		return false;
	}

	public boolean isSQL() {
		return false;
	}
	
	public boolean isCompare() {
		return false;
	}
	

	public boolean isExpectedResultsNeeded() {
		return (isCompare());
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
			return getQueryReader().getQueries(querySetID);
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
		return getQueryReader().getQuerySetIDs();
	}

	/**
	 * Return the result mode that was defined by the property
	 * {@link TestProperties#PROP_RESULT_MODE}
	 * 
	 * @return String result mode
	 */
	public abstract String getResultsMode();
	
	/**
	 * Will be called to handle the {@link TestCase} and the implementor will be responsible
	 * for determining how to perform that task.
	 * @param testcase
	 * @param transaction
	 * @throws QueryTestFailedException 
	 * @throws FrameworkException 
	 */
	public abstract void handleTestResult(TestCase testcase, TransactionAPI transaction) throws QueryTestFailedException, FrameworkException;

	public List<ExpectedResultsReader> getExpectedResultsReaders(TestCase testCase) {
		return fileType.getExpectedResultsReaders(this, getProperties(), ( (QueryTest)testCase.getActualTest()).getQuerySetID());
	}

	
	public List<ExpectedResultsWriter> getExpectedResultsWriters() throws FrameworkException {
		if (resultsWriters == null) {
			resultsWriters = fileType.getExpectedResultsWriters(this, getProperties());
		}
		
		return resultsWriters;

	}

	/**
	 * Return the {@link QueryReader} that is to be used to obtain the queries
	 * to process.
	 * 
	 * @return QueryReader
	 */
	public synchronized QueryReader getQueryReader() {
		if (this.reader == null) {
			this.reader = createQueryReader();
		}
		return this.reader;
	}
	
	/**
	 * Return the {@link ErrorWriter} that is to be used to write
	 * the error files.
	 * 
	 * @return QueryReader
	 */
	public synchronized ErrorWriter getErrorWriter() {
		if (this.errorWriter == null) {
			this.errorWriter = createErrorWriter();
		}
		return this.errorWriter;
	}
	
	/**
	 * Return the {@link QueryWriter} that is to be used to writer 
	 * to process.
	 * 
	 * @return QueryWriter
	 */
	public synchronized QueryWriter getQueryWriter() {
		if (this.writer == null) {
			this.writer = createQueryWriter();
		}
		return this.writer;

	}	
	
	public TestResultsSummary getTestResultsSummary() {
		return this.summary;
	}
	
	/* ************
	 * 
	 * Helper Methods
	 * 
	 **************/
	
	protected QueryReader createQueryReader() {
		Collection<Object> args = new ArrayList<Object>(2);
		args.add(this);
		args.add(props);
		return (QueryReader) createInstance(fileType.getQueryReaderClassName(), args);
	}	
	 
	protected QueryWriter createQueryWriter() {
		Collection<Object> args = new ArrayList<Object>(2);
		args.add(this);
		args.add(props);
		return (QueryWriter) createInstance(fileType.getQueryWriterClassName(), args);
	}
	
	protected ErrorWriter createErrorWriter() {
		Collection<Object> args = new ArrayList<Object>(2);
		args.add(this);
		args.add(props);
		return (ErrorWriter) createInstance(fileType.getErrorWriterClassName(), args);
	}		

	protected Object createInstance(String clzzName, final Collection<?> args) {
		return ReflectionHelper.create(clzzName,args, null);
	}
}
