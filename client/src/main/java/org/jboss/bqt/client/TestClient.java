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

import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.core.util.ArgCheck;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.PropertiesUtils;
import org.jboss.bqt.core.util.StringUtil;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.ConfigPropertyNames;
import org.jboss.bqt.framework.TransactionContainer;
import org.jboss.bqt.framework.TransactionFactory;
import org.jboss.bqt.framework.connection.DataSourceConnection;
import org.jboss.bqt.framework.connection.DriverConnection;

/**
 * TestClient is the starter class for running bulk sql testing against a Teiid
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

	private Properties overrides = new Properties();
	
	private String scenario_name;

	static {
		if (System.getProperty(ConfigPropertyNames.CONFIG_FILE) == null) {
			System.setProperty(ConfigPropertyNames.CONFIG_FILE,
					"./ctc_tests/ctc-test.properties");
		} else {
			System.out.println("Config file property is set to: "
					+ System.getProperty(ConfigPropertyNames.CONFIG_FILE));
		}

		// the project.loc is used
		if (System.getProperty("project.loc") == null) {
			System.setProperty("project.loc", ".");
		}

	}

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
		String scenario_file = ConfigPropertyLoader.getInstance().getProperty(
				TestProperties.PROP_SCENARIO_FILE);
		if (scenario_file == null) {
			throw new FrameworkRuntimeException(
					TestProperties.PROP_SCENARIO_FILE
							+ " property was not defined");
		}

		this.scenario_name = FileUtils
				.getBaseFileNameWithoutExtension(scenario_file);

		Properties sc_props = PropertiesUtils.load(scenario_file);

		// 1st perform substitution on the scenario file based on the config and
		// system properties file
		// because the next substitution is based on the scenario file
		Properties sc_updates = getSubstitutedProperties(sc_props);
		if (!sc_updates.isEmpty()) {
			sc_props.putAll(sc_updates);
			this.overrides.putAll(sc_props);

		}
		ConfigPropertyLoader.getInstance().setProperties(sc_props);

		// 2nd perform substitution on current configuration - which will be
		// based on the config properties file
		Properties config_updates = getSubstitutedProperties(ConfigPropertyLoader
				.getInstance().getProperties());
		if (!config_updates.isEmpty()) {
			this.overrides.putAll(config_updates);
			ConfigPropertyLoader.getInstance().setProperties(config_updates);
		}

		// update the URL with the vdb that is to be used
		String url = ConfigPropertyLoader.getInstance().getProperty(
				DriverConnection.DS_URL);
		String vdb_name = ConfigPropertyLoader.getInstance().getProperty(
				DataSourceConnection.DS_DATABASENAME);

		ArgCheck.isNotNull(vdb_name, DataSourceConnection.DS_DATABASENAME
				+ " property not set, need it for the vdb name");

		url = StringUtil.replace(url, "${vdb}", vdb_name);

		ConfigPropertyLoader.getInstance().setProperty(DriverConnection.DS_URL,
				url);

	}

	private void runScenario() throws Exception {
		ClientPlugin.LOGGER.info("Starting scenario " + this.scenario_name);

		QueryScenario queryset = ClassFactory.createQueryScenario(this.scenario_name);
		
		if (queryset.isSQL()) {
			this.createSQL(queryset);
			return;
		}
		

		TransactionContainer tc = getTransactionContainter();

		String querySetID = null;
		List<QueryTest> queryTests = null;

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

				queryTests = queryset.getQueries(querySetID);

				// the iterator to process the query tests
				Iterator<QueryTest> queryTestIt = null;
				queryTestIt = queryTests.iterator();

				ExpectedResults expectedResults = queryset
						.getExpectedResults(querySetID);

				long beginTS = System.currentTimeMillis();
				long endTS = 0;

				while (queryTestIt.hasNext()) {
					QueryTest q = queryTestIt.next();

					userTxn.init(summary, expectedResults, q);

					// run test
					try {
						tc.runTransaction(userTxn);
					} catch (Throwable t) {
						t.printStackTrace();
					}

				}

				endTS = System.currentTimeMillis();

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
		ClientPlugin.LOGGER.info("Start creating sql");

		try {
			TestCreateSQLQueryFile createsqltrans = new TestCreateSQLQueryFile(queryset);
		
			createsqltrans.testCase();
		
		} finally {


			// userTxn.getConnectionStrategy().shutdown();
			ConfigPropertyLoader.reset();
		}
		
		ClientPlugin.LOGGER.info("Completed create sql " );
	
	}

	protected TransactionContainer getTransactionContainter() {
		try {
			return TransactionFactory
					.create(ConfigPropertyLoader.getInstance());
		} catch (QueryTestFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new TransactionRuntimeException(e);
		}

	}

	private Properties getSubstitutedProperties(Properties props) {
		Properties or = new Properties();

		Properties configprops = ConfigPropertyLoader.getInstance()
				.getProperties();

		configprops.putAll(props);

		or = PropertiesUtils.resolveNestedProperties(configprops);

		return or;

	}

}
