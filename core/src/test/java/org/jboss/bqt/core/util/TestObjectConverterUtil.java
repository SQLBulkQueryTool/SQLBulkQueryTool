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

package org.jboss.bqt.core.util;

import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @version 	1.0
 * @author
 */
public class TestObjectConverterUtil extends TestCase {

    /**
     * Constructor for TestObjectConverterUtil.
     * @param name
     */
    public TestObjectConverterUtil(String name) {
        super(name);
    }

	//  ********* T E S T   S U I T E   M E T H O D S  *********


    
    public enum Test {
    	HELLO,
    	WORLD
    }
    
    public void testValueOf() throws Exception {
    	assertEquals(Integer.valueOf(21), ObjectConverterUtil.valueOf("21", Integer.class)); //$NON-NLS-1$
    	assertEquals(Boolean.valueOf(true), ObjectConverterUtil.valueOf("true", Boolean.class)); //$NON-NLS-1$    	
    	assertEquals("Foo", ObjectConverterUtil.valueOf("Foo", String.class)); //$NON-NLS-1$ //$NON-NLS-2$
    	assertEquals(Float.valueOf(10.12f), ObjectConverterUtil.valueOf("10.12", Float.class)); //$NON-NLS-1$
    	assertEquals(Double.valueOf(121.123), ObjectConverterUtil.valueOf("121.123", Double.class)); //$NON-NLS-1$
    	assertEquals(Long.valueOf(12334567L), ObjectConverterUtil.valueOf("12334567", Long.class)); //$NON-NLS-1$
    	assertEquals(Short.valueOf((short)21), ObjectConverterUtil.valueOf("21", Short.class)); //$NON-NLS-1$
    	
    	List list = ObjectConverterUtil.valueOf("foo,bar,x,y,z", List.class); //$NON-NLS-1$
    	assertEquals(5, list.size());
    	assertTrue(list.contains("foo")); //$NON-NLS-1$
    	assertTrue(list.contains("x")); //$NON-NLS-1$
    	
    	int[] values = ObjectConverterUtil.valueOf("1,2,3,4,5", new int[0].getClass()); //$NON-NLS-1$
    	assertEquals(5, values.length);
    	assertEquals(5, values[4]);
    	
    	Map m = ObjectConverterUtil.valueOf("foo=bar,x=,y=z", Map.class); //$NON-NLS-1$
    	assertEquals(3, m.size());
    	assertEquals(m.get("foo"), "bar"); //$NON-NLS-1$ //$NON-NLS-2$
    	assertEquals(m.get("x"), ""); //$NON-NLS-1$ //$NON-NLS-2$
    	assertEquals(Test.HELLO, ObjectConverterUtil.valueOf("HELLO", Test.class)); //$NON-NLS-1$ 
    	
    	assertEquals(new URL("http://teiid.org"), ObjectConverterUtil.valueOf("http://teiid.org", URL.class)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
