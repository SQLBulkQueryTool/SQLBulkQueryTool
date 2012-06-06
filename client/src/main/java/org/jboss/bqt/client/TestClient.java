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

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.api.QueryReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.PropertiesUtils;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.TransactionContainer;
import org.jboss.bqt.framework.TransactionFactory;

/**
 * TestClient is the starter class for running bulk sql testing against a JDBC database
 * server. The bulk testing is about testing a lot of queries against a
 * predefined set of expected results and providing error files when comparisons
 * don't match. The process The bulk testing, in its simplicity, will do the
 * following: <li>use a {@link QueryReader} to read the queries that it will
 * execute</li> <li>based on the results of each query executed, the process
 * will compare the results to the {@link ExpectedResults }.</li> <li>If the
 * {@link TestProperties#PROP_RESULT_MODE} option is set to
 * {@link TestProperties.RESULT_MODES#GENERATE} then the process will not
 * perform a comparison, but generate a new set of expected result files that
 * can in turn be used as the
 * 
 * @author vanhalbert
 * 
 */
public class TestClient {

	public static final SimpleDateFormat TSFORMAT = new SimpleDateFormat(
			"HH:mm:ss.SSS"); //$NON-NLS-1$
	
	private static final ConfigPropertyLoader CONFIG = ConfigPropertyLoader.getInstance();
	
	private String scenario_name;

	public TestClient() {

	}

	public static void main(String[] args) {

		TestClient tc = new TestClient();
		tc.runTest();

	}

	public void runTest() {

		try {

			init();
			runScenario();

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}
	
	private void init() throws Exception {
		String scenario_file = CONFIG.getProperty(TestProperties.PROP_SCENARIO_FILE);
		if (scenario_file == null) {
			BQTUtil.throwInvalidProperty(TestProperties.PROP_SCENARIO_FILE);
		}

		this.scenario_name = FileUtils
				.getBaseFileNameWithoutExtension(scenario_file);

		Properties sc_props = PropertiesUtils.load(scenario_file);
		
		if (sc_props.isEmpty()) {
			final String msg = ClientPlugin.Util.getString(
					"TestClient.emptyScenarioFile", scenario_file); //$NON-NLS-1$            
			throw new FrameworkRuntimeException(msg);

		}
		
		
		// 1st perform substitution on the scenario file, this will
		// substitute any System properties for variables=${..}
		Properties sc_updates = getSubstitutedProperties(sc_props);
//		if (!sc_updates.isEmpty()) {
//			sc_props.putAll(sc_updates);
//			this.overrides.putAll(sc_props);
//
//		}
		CONFIG.setProperties(sc_updates);

//		// 2nd perform substitution on current configuration - which will 
//		// substitute configuration properties 
//		// based on the config properties file
//		Properties config_updates = getSubstitutedProperties(CONFIG.getProperties());
//		if (!config_updates.isEmpty()) {
//			this.overrides.putAll(config_updates);
//			CONFIG.setProperties(config_updates);
//		}

		// update the URL with the vdb that is to be used
//		String url = CONFIG.getProperty(DriverConnection.DS_URL);
		
//		String vdb_name = CONFIG.getProperty(
//				DataSourceConnection.DS_DATABASENAME);
//
//		ArgCheck.isNotNull(vdb_name, DataSourceConnection.DS_DATABASENAME
//				+ " property not set, need it for the vdb name");
//
//		url = StringUtil.replace(url, "${vdb}", vdb_name);

//		CONFIG.setProperty(DriverConnection.DS_URL,
//				url);

	}

	private void runScenario() throws Exception {
		ClientPlugin.LOGGER.info("Starting scenario " + this.scenario_name);

		QueryScenario queryset = ClassFactory.createQueryScenario(this.scenario_name);
		
		if (queryset.isSQL()) {
			this.createSQL(queryset);
			return;
		}
		
		TransactionContainer tc = TransactionFactory.create(CONFIG);

		String querySetID = null;

		TestClientTransaction userTxn = new TestClientTransaction(queryset);

		Iterator<String> qsetIt = queryset.getQuerySetIDs().iterator();
		TestResultsSummary summary = new TestResultsSummary(
				queryset.getResultsMode());

		try {

			// iterate over the query set ID's, which there
			// should be 1 for each file to be processed
			while (qsetIt.hasNext()) {
				querySetID = qsetIt.next();

				ClientPlugin.LOGGER.info("Start Test Query ID [" + querySetID + "]");

				final List<QueryTest> queryTests = queryset.getQueries(querySetID);

				// the iterator to process the query tests
				Iterator<QueryTest> queryTestIt = queryTests.iterator();

				ExpectedResults expectedResults = queryset
						.getExpectedResults(querySetID);

				long beginTS = System.currentTimeMillis();

				while (queryTestIt.hasNext()) {
					QueryTest q = queryTestIt.next();

					userTxn.init(summary, expectedResults, q);

					// run test
					try {
						tc.runTransaction(userTxn);
					} catch (FrameworkRuntimeException rme) {
						throw rme;		
					} catch (Throwable t) {
						t.printStackTrace();
					}

				}

				long endTS = System.currentTimeMillis();

				ClientPlugin.LOGGER.info("End Test Query ID [" + querySetID + "]");

				summary.printResults(queryset, querySetID, beginTS, endTS);

			}

		} finally {
			try {
				summary.printTotals(queryset);
				summary.cleanup();	
			} catch (Throwable t) {
				t.printStackTrace();
			}

			// userTxn.getConnectionStrategy().shutdown();
			ConfigPropertyLoader.reset();
		}
		
		ClientPlugin.LOGGER.info("Completed scenario " + scenario_name);

	}
	
	protected void createSQL(QueryScenario queryset) throws Exception {
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

	private Properties getSubstitutedProperties(Properties props) {
		Properties configprops = CONFIG.getProperties();

		configprops.putAll(props);

		return PropertiesUtils.resolveNestedProperties(configprops);

	}

}
