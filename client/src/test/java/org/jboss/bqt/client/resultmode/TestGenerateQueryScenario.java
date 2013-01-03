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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.util.UnitTestUtil;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.ConfigPropertyNames;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests primarily the various cloning scenarios available with PropertiesUtils
 */
public class TestGenerateQueryScenario {

	public TestGenerateQueryScenario() {

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
	 * Using Generate result mode
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCoreSupport() throws Exception {
		System.setProperty("result.mode", "generate");
		
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

		assertTrue(set.getQuerySetIDs()!=null);
		
		assertTrue(set.getQueryReader()!=null);
		
		assertTrue(set.getExpectedResultsGenerator() != null);
		
		assertTrue(set.getQueryWriter()==null);

		assertTrue(set.getExpectedResults("") == null);
		

	}
	
	/**
	 * Should throw an exception when the query folder is empty or not found
	 * @throws Exception
	 */
    @Test( expected = FrameworkRuntimeException.class )
	public void testEmptyQueryFolder() throws Exception {
		System.setProperty("result.mode", "generate");
		
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

}
