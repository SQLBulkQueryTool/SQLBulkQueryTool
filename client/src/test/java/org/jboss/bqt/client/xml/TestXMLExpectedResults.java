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

package org.jboss.bqt.client.xml;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.api.ExpectedResultsWriter;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.UnitTestUtil;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.ConfigPropertyNames;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestResult;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests primarily the various cloning scenarios available with PropertiesUtils
 */
public class TestXMLExpectedResults {

	public TestXMLExpectedResults() {

	}

	// ===================================================================
	// ACTUAL TESTS
	// ===================================================================
	

	/**
	 * Tests {@link org.jboss.bqt.core.util.PropertiesUtils}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmptyFolders() throws Exception {
		//  the following 3 properties are what's normally found in the scenario.properties file
		System.setProperty("queryset.dirname", "test_query_set");
		System.setProperty("test.queries.dirname", "test_queries");
		System.setProperty("expected.results.dirname", "expected_results");	
		
		System.setProperty("result.mode", "compare");	
	
		//
		System.setProperty("project.data.path", UnitTestUtil.getTestDataPath());
		
		System.setProperty("output.dir", UnitTestUtil.getTestOutputPath() + File.separator + "sqltest" );
		
		System.setProperty(ConfigPropertyNames.CONFIG_FILE, UnitTestUtil.getTestDataPath() + File.separator + "localconfig.properties");		

		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
		Properties p = _instance.getProperties();
		if (p == null || p.isEmpty()) {
			throw new RuntimeException("Failed to load config properties file");

		}

		QueryScenario set = QueryScenario.createInstance("testscenario",p);

//		_instance.setProperty(TestProperties.PROP_QUERY_FILES_DIR_LOC,
//				new File("src/main/resources/").getAbsolutePath());

		Iterator<String> it = set.getQuerySetIDs().iterator();
		while (it.hasNext()) {
			String querySetID = it.next();

			List<QueryTest> queries = set.getQueries(querySetID);
			if (queries.size() == 0l) {
				System.out.println("Failed, didn't load any queries ");
			}

			ExpectedResultsReader er = set.getExpectedResultsReader(querySetID);

			ExpectedResultsWriter gr = set.getExpectedResultsGenerator();

			Iterator<QueryTest> qIt = queries.iterator();
			while (qIt.hasNext()) {
				QueryTest qt = qIt.next();
				TestResult testResult = new TestResult(qt.getQuerySetID(), qt.getQueryID());
				
				TestCase testcase = new TestCase(qt);
				testcase.setTestResult(testResult);
				
				testcase.setExpectedResults(set.getExpectedResults(qt));

				testResult.setResultMode(set.getResultsMode());
				testResult.setStatus(TestResult.RESULT_STATE.TEST_PRERUN);
			
							
	//			org.jboss.bqt.framework.TestResult t = new org.jboss.bqt.framework.TestResult(q.getQuerySetID(), q.getQueryID());
				// String qId = (String) qIt.next();
				// String sql = (String) queries.get(qId);

				// System.out.println("SetID #: " + cnt + "  Qid: " + qId +
				// "   sql: " + sql);

				File resultsFile = er.getResultsFile(qt);
				if (resultsFile == null) {
					System.out
							.println("Failed to get results file for queryID "
									+ qt.getQueryID());
				}

			}

		}

		System.out.println("Completed TestResult");

	}
	
	@Test
	public void testIsExceptionExpected_No() throws Exception {
		//  the following 3 properties are what's normally found in the scenario.properties file
		System.setProperty("queryset.dirname", "test_query_set");
		System.setProperty("test.queries.dirname", "test_queries");
		System.setProperty("expected.results.dirname", "expected_results");	
		
		System.setProperty("result.mode", "compare");	
	
		//
		System.setProperty("project.data.path", UnitTestUtil.getTestDataPath());
		
		System.setProperty("output.dir", UnitTestUtil.getTestOutputPath() + File.separator + "sqltest" );
		
		System.setProperty(ConfigPropertyNames.CONFIG_FILE, UnitTestUtil.getTestDataPath() + File.separator + "localconfig.properties");		
		
		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
		Properties p = _instance.getProperties();
		if (p == null || p.isEmpty()) {
			throw new RuntimeException("Failed to load config properties file");

		}

		QueryScenario set = QueryScenario.createInstance("testscenario",p);
		
		XMLExpectedResults er = new XMLExpectedResults(set, "test_queries1",_instance.getProperties());
		
		QueryTest qt = new QueryTest(set.getQueryScenarioIdentifier(), "test_queries1", "Query1", null);
		
		assertFalse(set.getExpectedResults(qt).isExceptionExpected());
	}
	
	@Test
	public void testIsExceptionExpected_Yes() throws Exception {
		//  the following 3 properties are what's normally found in the scenario.properties file
		System.setProperty("queryset.dirname", "test_query_set");
		System.setProperty("test.queries.dirname", "test_queries");
		System.setProperty("expected.results.dirname", "expected_results");	
		
		System.setProperty("result.mode", "compare");	
	
		//
		System.setProperty("project.data.path", UnitTestUtil.getTestDataPath());
		
		System.setProperty("output.dir", UnitTestUtil.getTestOutputPath() + File.separator + "sqltest" );
		
		System.setProperty(ConfigPropertyNames.CONFIG_FILE, UnitTestUtil.getTestDataPath() + File.separator + "localconfig.properties");		
		
		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
		Properties p = _instance.getProperties();
		if (p == null || p.isEmpty()) {
			throw new RuntimeException("Failed to load config properties file");

		}

		QueryScenario set = QueryScenario.createInstance("testscenario",p);
		
		XMLExpectedResults er = new XMLExpectedResults(set, "test_queries1",_instance.getProperties());
		
		QueryTest qt = new QueryTest(set.getQueryScenarioIdentifier(), "test_queries1", "Query2", null);
		
		assertTrue(set.getExpectedResults(qt).isExceptionExpected());
	}
	
    @Test( expected = QueryTestFailedException.class )
	public void testCompareResults_ExpectedException_Yes() throws Exception {
		//  the following 3 properties are what's normally found in the scenario.properties file
		System.setProperty("queryset.dirname", "test_query_set");
		System.setProperty("test.queries.dirname", "test_queries");
		System.setProperty("expected.results.dirname", "expected_results");	
		
		System.setProperty("result.mode", "compare");	
	
		//
		System.setProperty("project.data.path", UnitTestUtil.getTestDataPath());
		
		System.setProperty("output.dir", UnitTestUtil.getTestOutputPath() + File.separator + "sqltest" );
		
		System.setProperty(ConfigPropertyNames.CONFIG_FILE, UnitTestUtil.getTestDataPath() + File.separator + "localconfig.properties");		
		
		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
		Properties p = _instance.getProperties();
		if (p == null || p.isEmpty()) {
			throw new RuntimeException("Failed to load config properties file");

		}

		QueryTestFailedException e = new QueryTestFailedException("AColumn \"SSN\" not found; SQL statement: " +
		"Select SSN, FIRSTNAME, LASTNAME, ST_ADDRESS, APT_NUMBER, CITY, STATE, ZIPCODE, PHONE [42122-124]");

		
		QueryScenario set = QueryScenario.createInstance("testscenario",p);
		QueryTest qt = new QueryTest(set.getQueryScenarioIdentifier(), "test_queries1", "Query2", null);
		TestResult testResult = new TestResult(qt.getQuerySetID(), qt.getQueryID());
		
		TestCase testcase = new TestCase(qt);
		testcase.setTestResult(testResult);
		
		testcase.setExpectedResults(set.getExpectedResults(qt));

		testResult.setResultMode(set.getResultsMode());
		testResult.setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);
		testResult.setException(e);
		
		XMLCompareResults compare = XMLCompareResults.create(set.getProperties());
		
		// this should throw an exception
		compare.compareResults(testcase, null, false);

	}
	

}
