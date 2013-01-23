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
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.QueryReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.util.ArgCheck;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.PropertiesUtils;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.TestCaseLifeCycle;
import org.jboss.bqt.framework.TransactionAPI;
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

				ConfigPropertyLoader.reset();
				
				CONFIG = ConfigPropertyLoader.getInstance();
				
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

	public void runScenario(File scenarioFile) throws Throwable {
		
		String scenario_name = init(scenarioFile);

		
		this.scenario = QueryScenario.createInstance(scenario_name, CONFIG.getProperties());
		
		if (scenario.isSQL()) {
			this.createSQL(scenario);
			return;
		}
		ClientPlugin.LOGGER.info("Starting scenario: " + scenario.getQueryScenarioIdentifier());
		
		TransactionAPI tc = getTransactionContainer(CONFIG.getProperties());

		TestCaseLifeCycle testCase = scenario.getTestCase();
		
		testCase.setup(tc);
		
		testCase.runTestCase();
		
		testCase.cleanup();
		
		ClientPlugin.LOGGER.info("Completed scenario: " + scenario.getQueryScenarioIdentifier());

	}
	
	protected QueryScenario getScenario() {
		return this.scenario;
	}
	
	protected TransactionAPI getTransactionContainer(Properties props) {
		return TransactionFactory.create(CONFIG.getProperties());
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
			if (sc_props.getProperty(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_QUERYSET_DIR) != null) {
				sc_props.put(TestProperties.PRE1_0_SCENARIO_SUPPORT.NEW_QUERYSET_DIR, sc_props.getProperty(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_QUERYSET_DIR));
				sc_props.put(TestProperties.PRE1_0_SCENARIO_SUPPORT.NEW_TEST_QUERIES_DIR, sc_props.getProperty(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_TEST_QUERIES_DIR));
				sc_props.put(TestProperties.PRE1_0_SCENARIO_SUPPORT.NEW_EXPECTED_RESULTS_DIR, sc_props.getProperty(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_EXPECTED_RESULTS_DIR));

				sc_props.remove(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_EXPECTED_RESULTS_DIR);
				sc_props.remove(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_QUERYSET_DIR);
				sc_props.remove(TestProperties.PRE1_0_SCENARIO_SUPPORT.OLD_TEST_QUERIES_DIR);

			}
		}
				
		CONFIG.setProperties(sc_props);
		
		return scenario_name;
	}
		
	private void createSQL(QueryScenario scenario) throws Throwable {
		ClientPlugin.LOGGER.info("Start creating sql for scenario: " + scenario.getQueryScenarioIdentifier());

		try {
			// NOTE: no transaction container is needed for running this type
			//		of test case, so the testcase is executed directly
			TestCaseLifeCycle createsqltrans = scenario.getTestCase();
			
			Properties props = new Properties();
			props.setProperty(TransactionFactory.TRANSACTION_TYPE, TransactionFactory.TRANSACTION_TYPES.USEDEFAULT_TRANSACTION);
			TransactionAPI tc = getTransactionContainer(props);
		
			createsqltrans.setup(tc);
			createsqltrans.runTestCase();
			createsqltrans.cleanup();
		
		} finally {


			// userTxn.getConnectionStrategy().shutdown();
			ConfigPropertyLoader.reset();
		}
		
		ClientPlugin.LOGGER.info("Completed creating sql: " + scenario.getQueryScenarioIdentifier() );
	
	}

}
