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

import org.junit.Test;

/**
 * Tests primarily the various cloning scenarios available with PropertiesUtils
 */
public class TestConfigPropertyLoader {    

 
    public TestConfigPropertyLoader() {
    
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
		System.setProperty("test", "value");

		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
		Properties p = _instance.getProperties();
		assertNotNull(p);
		assertTrue(! p.isEmpty());
		
		assertEquals("value", p.getProperty("test")); //$NON-NLS-1$ //$NON-NLS-2$

		_instance.setProperty("override", "ovalue");

		assertEquals("ovalue", _instance.getProperties().getProperty("override")); //$NON-NLS-1$ //$NON-NLS-2$

		// confirm the loader actually loaded the default-config.properties file
		assertEquals("driver", _instance.getProperties().getProperty(ConfigPropertyNames.CONNECTION_TYPE)); //$NON-NLS-1$ //$NON-NLS-2$

		
		
		ConfigPropertyLoader.reset();
				
		_instance = ConfigPropertyLoader.getInstance();
		
		assertNull("should be null after reset", _instance.getProperties().getProperty("override"));

		assertEquals("failed to pickup system property", "value", p.getProperty("test")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// confirm the loader actually loaded the default-config.properties file
		assertEquals("failed to correctly pickup the User ", "", p.getProperty("User")); //$NON-NLS-1$ //$NON-NLS-2$

    }
    
    
 
}
