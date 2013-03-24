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

package org.jboss.bqt.framework;

import static org.junit.Assert.*;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests primarily the various cloning scenarios available with PropertiesUtils
 */
public class TestConfigPropertyLoader {    

 
    public TestConfigPropertyLoader() {
    
    }
    
    @Before
    public void setUp() throws Exception {
        
    	ConfigPropertyLoader.reset();
    }
	
    //===================================================================
    //ACTUAL TESTS
    //===================================================================

    
    /**
     * Tests {@link org.jboss.bqt.core.util.PropertiesUtils}
     * @throws Exception 
     */
    @Test
    public void test1() throws Exception {
    	// add this step of removing the property because the order of
    	// running is not guaranteed, and if this doesn't run first,
    	// this property is set by other tests
    	Properties props = System.getProperties();
    	props.remove(ConfigPropertyNames.CONFIG_FILE);
    	
    	System.setProperties(props);

    	System.setProperty("test", "value");

		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
		Properties p = _instance.getProperties();
		assertNotNull(p);
		assertTrue(! p.isEmpty());
		
		assertEquals("value", p.getProperty("test")); //$NON-NLS-1$ //$NON-NLS-2$

		_instance.setProperty("override", "ovalue");

		assertEquals("ovalue", _instance.getProperty("override")); //$NON-NLS-1$ //$NON-NLS-2$

		// confirm the loader actually loaded the default-config.properties file
		assertEquals("driver", p.getProperty(ConfigPropertyNames.CONNECTION_TYPE)); //$NON-NLS-1$ //$NON-NLS-2$

		System.setProperty("username", "");
		
		
		ConfigPropertyLoader.reset();
				
		_instance = ConfigPropertyLoader.getInstance();
		 p = _instance.getProperties();
		
		assertNull("should be null after reset", _instance.getProperties().getProperty("override"));

		assertEquals("failed to pickup system property", "value", p.getProperty("test")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// confirm the loader actually loaded the default-config.properties file
		assertEquals("failed to correctly pickup the User ", "", _instance.getProperty("conn.user")); //$NON-NLS-1$ //$NON-NLS-2$

    }
    
    /**
     * Tests {@link org.jboss.bqt.core.util.PropertiesUtils}
     * @throws Exception 
     */
    @Test
    public void testEachConfigProperty() throws Exception {
    	System.setProperty(ConfigPropertyNames.CONFIG_FILE, "configtest.properties");
 
		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
		assertNotNull(_instance);

		assertEquals("testmode", _instance.getProperty("bqt.result.mode")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("querysetdirname", _instance.getProperty("bqt.queryset.dirname")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("OFF", _instance.getProperty("bqt.transaction.option")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//bqt.expectedresults.loc=${queryset.artifacts.dir}/${queryset.dir}/${expected.results.dir}
		assertEquals("/queryset/artifacts/dir/querysetdirname/expectedresultsdir", _instance.getProperty("bqt.expectedresults.loc")); //$NON-NLS-1$ //$NON-NLS-2$

		//bqt.queryfiles.loc=${queryset.artifacts.dir}/${queryset.dir}/${test.queries.dir}
		assertEquals("/queryset/artifacts/dir/querysetdirname/testqueriesdir", _instance.getProperty("bqt.queryfiles.loc")); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("myscenariofile.properties", _instance.getProperty("bqt.scenario.file")); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("/output/dir", _instance.getProperty("bqt.output.dir")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("/output/dir/myscenariofile/testmode/querysetdirname/expectedresultsdir", _instance.getProperty("bqt.generate.dir")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("/output/dir/myscenariofile/testmode", _instance.getProperty("bqt.compare.dir")); //$NON-NLS-1$ //$NON-NLS-2$		
		assertEquals("/output/dir/myscenariofile/testmode/querysetdirname/testqueriesdir", _instance.getProperty("bqt.sql.dir")); //$NON-NLS-1$ //$NON-NLS-2$	
		assertEquals("/output/dir/myscenariofile/errors_for_testmode", _instance.getProperty("bqt.errors.dir")); //$NON-NLS-1$ //$NON-NLS-2$

		

		assertEquals("myconntype", _instance.getProperty("conn.type")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("org.jdbc.oracle.OracleDriver", _instance.getProperty("conn.driver")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("jdbc:teiid:VDB@mm://localhost:31000", _instance.getProperty("conn.url")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("admin", _instance.getProperty("conn.user")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("adminpw", _instance.getProperty("conn.password")); //$NON-NLS-1$ //$NON-NLS-2$
    	
    }
    
    @Test
    public void testMetadataConfigProperties() throws Exception {
    	System.setProperty(ConfigPropertyNames.CONFIG_FILE, "configtest.properties");
 
		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
		assertNotNull(_instance);

		assertEquals("testmode", _instance.getProperty("bqt.result.mode")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("querysetdirname", _instance.getProperty("bqt.queryset.dirname")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("OFF", _instance.getProperty("bqt.transaction.option")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//bqt.expectedresults.loc=${queryset.artifacts.dir}/${queryset.dir}/${expected.results.dir}
		assertEquals("/queryset/artifacts/dir/querysetdirname/expectedresultsdir", _instance.getProperty("bqt.expectedresults.loc")); //$NON-NLS-1$ //$NON-NLS-2$

		//bqt.queryfiles.loc=${queryset.artifacts.dir}/${queryset.dir}/${test.queries.dir}
		assertEquals("/queryset/artifacts/dir/querysetdirname/testqueriesdir", _instance.getProperty("bqt.queryfiles.loc")); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("myscenariofile.properties", _instance.getProperty("bqt.scenario.file")); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("/output/dir", _instance.getProperty("bqt.output.dir")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("/output/dir/myscenariofile/testmode/querysetdirname/expectedresultsdir", _instance.getProperty("bqt.generate.dir")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("/output/dir/myscenariofile/testmode", _instance.getProperty("bqt.compare.dir")); //$NON-NLS-1$ //$NON-NLS-2$		
		assertEquals("/output/dir/myscenariofile/testmode/querysetdirname/testqueriesdir", _instance.getProperty("bqt.sql.dir")); //$NON-NLS-1$ //$NON-NLS-2$	
		assertEquals("/output/dir/myscenariofile/errors_for_testmode", _instance.getProperty("bqt.errors.dir")); //$NON-NLS-1$ //$NON-NLS-2$

		assertEquals("myconntype", _instance.getProperty("conn.type")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("org.jdbc.oracle.OracleDriver", _instance.getProperty("conn.driver")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("jdbc:teiid:VDB@mm://localhost:31000", _instance.getProperty("conn.url")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("admin", _instance.getProperty("conn.user")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("adminpw", _instance.getProperty("conn.password")); //$NON-NLS-1$ //$NON-NLS-2$
		
		assertEquals("bqt%", _instance.getProperty(ConfigPropertyNames.DATABASE_METADATA_OPTIONS.CATALOG_PATTERN)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("schema%", _instance.getProperty(ConfigPropertyNames.DATABASE_METADATA_OPTIONS.SCHEMA_PATTERN)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("mytables%", _instance.getProperty(ConfigPropertyNames.DATABASE_METADATA_OPTIONS.TABLENAME_PATTERN)); //$NON-NLS-1$ //$NON-NLS-2$

		String ttypes =  _instance.getProperty(ConfigPropertyNames.DATABASE_METADATA_OPTIONS.TABLE_TYPES);
		String[] table_types = StringUtils.split(ttypes, ","); //$NON-NLS-1$
		assertEquals(new String[] {"TABLE","VIEW","SYSTEM"}, table_types); //$NON-NLS-1$ //$NON-NLS-2$

		
		
    }

    
    
 
}
