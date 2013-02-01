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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.results.ExpectedResultsHolder;
import org.jboss.bqt.core.util.UnitTestUtil;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.ConfigPropertyNames;
import org.junit.Ignore;
import org.junit.Test;

/**
 * These test verify XMLQueryVisitationStrategy is parsing correctly
 */
public class TestXMLQueryVisitationStrategy {

	public TestXMLQueryVisitationStrategy() {

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
		
		XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();
		final ExpectedResultsHolder expectedResult;

		QueryTest test = new QueryTest(set.getQueryScenarioIdentifier(), "samplefiles", "expectedresult_noerror", null);
		String filename = UnitTestUtil.getTestDataPath() + File.separator + "samplefiles" + File.separator + "expectedresult_noerror.xml";
		File resultsFile = new File(filename);
		expectedResult = jstrat.parseXMLResultsFile(test, set.getQueryScenarioIdentifier(), resultsFile);
		
		assertFalse(expectedResult.isExceptionExpected());
		
		assertTrue(expectedResult.getExceptionMsg() == null);
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
		
		XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();
		final ExpectedResultsHolder expectedResult;
		QueryTest test = new QueryTest(set.getQueryScenarioIdentifier(), "samplefiles", "expectedresult_error", null);

		String filename = UnitTestUtil.getTestDataPath() + File.separator + "samplefiles" + File.separator + "expectedresult_error.xml";
		File resultsFile = new File(filename);
		expectedResult = jstrat.parseXMLResultsFile(test, set.getQueryScenarioIdentifier(), resultsFile);
		
		assertTrue(expectedResult.isExceptionExpected());
		
		assertFalse(expectedResult.getExceptionMsg() == null);

	}
	
    @Test( expected = java.lang.AssertionError.class )
	public void testIsExceptionExpected_Failure() throws Exception {
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
		
		XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();
		final ExpectedResultsHolder expectedResult;
		QueryTest test = new QueryTest(set.getQueryScenarioIdentifier(), "samplefiles", "expectedresult_error", null);

		String filename = UnitTestUtil.getTestDataPath() + File.separator + "samplefiles" + File.separator + "expectedresult_error.xml";
		File resultsFile = new File(filename);
		expectedResult = jstrat.parseXMLResultsFile(test, set.getQueryScenarioIdentifier(), resultsFile);
		
		assertFalse(expectedResult.isExceptionExpected());

	}
 
}
