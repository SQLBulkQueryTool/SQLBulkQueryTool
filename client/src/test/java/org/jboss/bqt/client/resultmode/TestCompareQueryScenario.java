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

package org.jboss.bqt.client.resultmode;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.util.UnitTestUtil;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.ConfigPropertyNames;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests primarily the various cloning scenarios available with PropertiesUtils
 */
public class TestCompareQueryScenario {

	public TestCompareQueryScenario() {

	}
	
    @Before
    public void setUp() throws Exception {
        
    	ConfigPropertyLoader.reset();
    }

	// ===================================================================
	// ACTUAL TESTS
	// ===================================================================
	
	/**
	 * Tests the supported reads and/or writes.
	 * 
	 * Using Compare result mode,  the results generator and query write is not used
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCoreSupport() throws Exception {
		System.setProperty("result.mode", "compare");
		
		//  the following 3 properties are what's normally found in the scenario.properties file
		System.setProperty("queryset.dirname", "test_query_set");
		System.setProperty("test.queries.dirname", "test_queries");
		System.setProperty("expected.results.dirname", "expected_results");	
	
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
		
		assertTrue(set instanceof Compare);

		assertTrue(set.getQuerySetIDs().size() == 1);
				
		assertTrue(set.getQueryReader().getQueries("test_queries1").size() == 2);
		
		assertTrue(set.getQueryWriter()==null);
		
		Collection<String> setIDs = set.getQuerySetIDs();
		assertTrue(setIDs.size() == 1);
		for (String id : setIDs) {
			assertTrue(set.getQueries(id).size() ==2);
		}
		
		QueryTest qt = new QueryTest(set.getQueryScenarioIdentifier(), "test_queries1", "Query1", null);
		TestResult testResult = new TestResult(qt.getQuerySetID(), qt.getQueryID());
		
		TestCase testcase = new TestCase(qt);
		testcase.setTestResult(testResult);

		List readers = set.getExpectedResultsReaders(testcase);
		assertNotNull(readers);
		assertEquals(1, readers.size());
		
		assertNotNull(set.getFileType());
		assertNotNull(set.getQueryReader());
		
		assertEquals(1, set.getQuerySetIDs().size());

		assertTrue(set.isCompare());

		testResult.setResultMode(set.getResultsMode());
		testResult.setStatus(TestResult.RESULT_STATE.TEST_SUCCESS);
		
	}
	
	/**
	 * Should throw an exception when the query folder is empty or not found
	 * @throws Exception
	 */
    @Test( expected = FrameworkRuntimeException.class )
	public void testEmptyQueryFolder() throws Exception {
		System.setProperty("result.mode", "compare");
		
		//  the following 3 properties are what's normally found in the scenario.properties file
		System.setProperty("queryset.dirname", "empty_query_set");
		System.setProperty("test.queries.dirname", "test_queries");
		System.setProperty("expected.results.dirname", "expected_results");	
	
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

		assertTrue(set.getQuerySetIDs()!=null);

	}

	/**
	 * Should throw an exception when the expectd results folder is empty or not found
	 * @throws Exception
	 */
    @Test( expected = FrameworkRuntimeException.class )
	public void testEmptyExpectedResultsFolder() throws Exception {
		System.setProperty("result.mode", "compare");
		
		//  the following 3 properties are what's normally found in the scenario.properties file
		System.setProperty("queryset.dirname", "empty_query_set");
		System.setProperty("test.queries.dirname", "test_queries");
		System.setProperty("expected.results.dirname", "expected_results");	
	
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
		assertNotNull(set.getFileType());
		
		QueryTest qt = new QueryTest(set.getQueryScenarioIdentifier(), "test_queries1", "Query1", null);
		
		TestCase testcase = new TestCase(qt);
		
		set.getExpectedResultsReaders(testcase);

	}
}
