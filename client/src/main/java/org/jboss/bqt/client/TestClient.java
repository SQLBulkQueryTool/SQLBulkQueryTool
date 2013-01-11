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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.QueryReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.api.TestResult;
import org.jboss.bqt.client.results.TestResultStat;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.util.ArgCheck;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.PropertiesUtils;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.TransactionContainer;
import org.jboss.bqt.framework.TransactionFactory;

/**
 * TestClient is the starter class for running bulk sql testing against a JDBC database
 * data source. The bulk testing is about testing a lot of queries against a
 * predefined set of expected results and providing error files when comparisons
 * don't match. The process The bulk testing, in its simplicity, will do the
 * following: <li>use a {@link QueryReader} to read the queries that it will
 * execute</li> <li>based on the results of each query executed, the process
 * will compare the results to the {@link ExpectedResultsReader }.</li> <li>If the
 * {@link TestProperties#PROP_RESULT_MODE} option is set to
 * {@link TestProperties.RESULT_MODES#GENERATE} then the process will not
 * perform a comparison, but generate a new set of expected result files that
 * can in turn be used as the
 * 
 * @author vanhalbert
 * 
 */
public class TestClient {

	// if PRE1 scenario properties are supported, then map the old properties to the new.
	private static boolean PRE1_SUPPORTED = false;
	
	public static final SimpleDateFormat TSFORMAT = new SimpleDateFormat(
			"HH:mm:ss.SSS"); //$NON-NLS-1$
	
	private ConfigPropertyLoader CONFIG = null;
	
	private QueryScenario scenario;

	public TestClient() {

	}

	/**
	 * Arguments must be passed in as pairs, that will be 
	 * loaded as properties.
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		TestClient tc = new TestClient();
		
		if (args != null && args.length > 0) {
			int i = 0;
			Properties props = new Properties();
			
			while (i < args.length) {
				props.setProperty(args[i], args[i+1]);
				i=i+2;
			}
			tc.runTest(props);
			
		} else {
			tc.runTest();
		}
	}
	
	
	
	/**
	 * An alternate method for running one test run.  These properties will
	 * be added to the System properties.
	 * @param properties 
	 */
	public void runTest(Properties properties) {
		Properties props = System.getProperties();
		props.putAll(properties);
		
		System.setProperties(props);
		runTest();
	}

	public void runTest() {
		CONFIG = ConfigPropertyLoader.getInstance();
		
		PRE1_SUPPORTED = PropertiesUtils.getBooleanProperty(System.getProperties(), 
				TestProperties.PRE1_0_SCENARIO_SUPPORT.SUPPORT_PRE1_0_SCENARIO, false);
		try {

			List<File> scenarios = getScenarios();
			for (File f:scenarios) {
				runScenario(f);
				
				// reset for reloading cause there are points that call setProperty(..)
				// on the ConfigurationProperties
				CONFIG.clearOverrides();
			}


		} catch (Throwable t) {
			t.printStackTrace();
		}
	
		ConfigPropertyLoader.reset();

	}
	
	private List<File> getScenarios() throws Exception {

		String scenario_file = CONFIG.getProperty(TestProperties.PROP_SCENARIO_FILE);
		if (scenario_file == null) {
			final String msg = ClientPlugin.Util.getString(
					"TestClient.emptyScenarioFile", scenario_file); //$NON-NLS-1$            
			throw new FrameworkRuntimeException(msg);
			
		} 
		
		File sfile = new File(scenario_file);
		if (!sfile.exists()) {
			throw new FrameworkRuntimeException(ClientPlugin.Util.getString("TestClient.scenarioFileDoesntExist", scenario_file));
		}
		
		File[] sfiles = null;
		if (sfile.isDirectory()) {
			sfiles = FileUtils.findAllFilesInDirectoryHavingExtension(scenario_file, ".properties");
		} else {
			sfiles = new File[] {sfile};
		}
		
		return Arrays.asList(sfiles);

	}	

	public void runScenario(File scenarioFile) throws Exception {
		
		String scenario_name = init(scenarioFile);

		ClientPlugin.LOGGER.info("Starting scenario " + scenario_name);
		
		this.scenario = QueryScenario.createInstance(scenario_name, CONFIG.getProperties());
		
		if (scenario.isSQL()) {
			this.createSQL(scenario);
			return;
		}
		
		TransactionContainer tc = getTransactionContainer();

		String querySetID = null;

		TestClientTransaction userTxn = getClientTransaction(scenario);

		Iterator<String> qsetIt = scenario.getQuerySetIDs().iterator();
		TestResultsSummary summary = new TestResultsSummary(
				scenario.getResultsMode());

		try {

			// iterate over the query set ID's, which there
			// should be 1 for each file to be processed
			while (qsetIt.hasNext()) {
				querySetID = qsetIt.next();

				ClientPlugin.LOGGER.info("Start Test: " + new Date() + " - Query ID [" + querySetID + "]");

				final List<QueryTest> queryTests = scenario.getQueries(querySetID);

				// the iterator to process the query tests
				Iterator<QueryTest> queryTestIt = queryTests.iterator();

				long beginTS = System.currentTimeMillis();

				while (queryTestIt.hasNext()) {
					QueryTest q = queryTestIt.next();

					userTxn.init(summary, q);

					// run test
					try {
						tc.runTransaction(userTxn);
					} catch (Throwable rme) {
						ClientPlugin.LOGGER.error(rme,
								"Test Error: " + q.getQuerySetID() + ":" + q.getQueryID() + ":" + rme.getMessage());
						scenario.getErrorWriter().generateErrorFile(q.getQuerySetID(), q.getQueryID(), rme);
						
						TestResult tr = new TestResultStat(q.getQuerySetID(), q.getQueryID());
						tr.setException(rme);
						tr.setResultMode(scenario.getResultsMode());
						tr.setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);
						
						summary.addTestResult(q.getQuerySetID(), tr);
	
					} 

				}

				long endTS = System.currentTimeMillis();

				ClientPlugin.LOGGER.info("End Test: " + new Date() + " - Query ID [" + querySetID + "]");

				summary.printResults(scenario, querySetID, beginTS, endTS);

			}

		} finally {
			try {
				summary.printTotals(scenario);
				summary.cleanup();	
			} catch (Throwable t) {
				t.printStackTrace();
			}

			// userTxn.getConnectionStrategy().shutdown();
			ConfigPropertyLoader.reset();
		}
		
		ClientPlugin.LOGGER.info("Completed scenario " + scenario_name);

	}
	
	protected QueryScenario getScenario() {
		return this.scenario;
	}
	
	protected TransactionContainer getTransactionContainer() {
		return TransactionFactory.create(CONFIG);
	}
	
	protected TestClientTransaction getClientTransaction(QueryScenario scenario) {
		return  new TestClientTransaction(scenario);
	}
	
	private String init(File scenarioFile) throws Exception {
		
		Properties sc_props = PropertiesUtils.load(scenarioFile.getAbsolutePath());
		
		if (sc_props.isEmpty()) {
			final String msg = ClientPlugin.Util.getString(
					"TestClient.emptyScenarioFile", scenarioFile.getAbsoluteFile()); //$NON-NLS-1$            
			throw new FrameworkRuntimeException(msg);
		}
		
		String scenario_name = FileUtils.getBaseFileNameWithoutExtension(scenarioFile.getName());
		ArgCheck.isNotNull(scenario_name, "scenario_name came back null");
		ArgCheck.isNotEmpty(scenario_name);
		
		sc_props.setProperty("scenario.name", scenario_name);	
		
		// if PRE1 scenario properties are supported, then map the old properties to the new.
		if (PRE1_SUPPORTED) {
			ClientPlugin.LOGGER.debug("Support for PRE1.0 ScenarioProperties");
			sc_props.put(TestProperties.PRE1_0_SCENARIO_SUPPORT.NEW_QUERYSET_DIR, sc_props.getProperty(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_QUERYSET_DIR));
			sc_props.put(TestProperties.PRE1_0_SCENARIO_SUPPORT.NEW_TEST_QUERIES_DIR, sc_props.getProperty(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_TEST_QUERIES_DIR));
			sc_props.put(TestProperties.PRE1_0_SCENARIO_SUPPORT.NEW_EXPECTED_RESULTS_DIR, sc_props.getProperty(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_EXPECTED_RESULTS_DIR));

			sc_props.remove(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_EXPECTED_RESULTS_DIR);
			sc_props.remove(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_QUERYSET_DIR);
			sc_props.remove(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_TEST_QUERIES_DIR);
		}
				
		CONFIG.setProperties(sc_props);
		
		return scenario_name;
	}
		
	private void createSQL(QueryScenario queryset) throws Exception {
		ClientPlugin.LOGGER.debug("Start creating sql");

		try {
			CreateSQLQueryFile createsqltrans = new CreateSQLQueryFile(queryset);
		
			createsqltrans.testCase();
		
		} finally {


			// userTxn.getConnectionStrategy().shutdown();
			ConfigPropertyLoader.reset();
		}
		
		ClientPlugin.LOGGER.debug("Completed creating sql " );
	
	}

}
