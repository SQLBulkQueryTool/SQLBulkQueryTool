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
package org.jboss.bqt.test;

import static org.junit.Assert.*;

import java.io.File;

import org.jboss.bqt.client.TestClient;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.UnitTestUtil;
import org.jboss.bqt.framework.ConfigPropertyNames;
import org.junit.BeforeClass;
import org.junit.Test;


@SuppressWarnings("nls")
public class TestIntegrationWithLocalDB {
	
	

	@BeforeClass
    public static void beforeEach() throws Exception {  		
		System.setProperty("project.data.path", UnitTestUtil.getTestDataPath());
		
		System.setProperty("output.dir", UnitTestUtil.getTestOutputPath() + File.separator + "sqltest" );
		
		System.setProperty(ConfigPropertyNames.CONFIG_FILE, UnitTestUtil.getTestDataPath() + File.separator + "localconfig.properties");		
		
    }

	@Test
	public void testBQTClientExecutionResultSetModeSQL() {
		
		System.setProperty("result.mode", "sql" );
		
		TestClient tc = new TestClient();
		tc.runTest();
	
	}

	@Test
	public void testBQTClientExecutionResultSetModeGenerate() {
		
		System.setProperty("result.mode", "generate" );
		
		TestClient tc = new TestClient();
		tc.runTest();
		
		String outputdir = System.getProperty("output.dir");
		File other = new File(outputdir + File.separator + "h2_scenario" + File.separator + "generate" + File.separator +
				"h2_queries" + File.separator + "expected_results" + File.separator + "h2_other_scenario");

		assertFalse("No expected results should have been generated for these types of queriest", other.exists());
	}

	@Test
	public void testBQTClientExecutionResultSetModeCompare() {
		
		System.setProperty("result.mode", "compare" );
		
		TestClient tc = new TestClient();
		tc.runTest();

		String outputdir = System.getProperty("output.dir");
		
		File compareErrors = new File(outputdir + File.separator + "h2_scenario" + File.separator + "errors_for_compare" );
		
		File[] errorFiles = FileUtils.findAllFilesInDirectory(compareErrors.getAbsolutePath());
		
		assertTrue("Compare Has Error Files", (errorFiles == null || errorFiles.length == 0) );
		
	
	}	
	
	@Test
	public void testBQTClientExecutionResultSetModeNone() {
		
		System.setProperty("result.mode", "none" );
		
		TestClient tc = new TestClient();
		tc.runTest();
	
	}	
}
