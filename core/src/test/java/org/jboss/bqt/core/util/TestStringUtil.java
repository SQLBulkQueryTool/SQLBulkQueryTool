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
public class TestStringUtil extends TestCase {

    /**
     * Constructor for TestStringUtil.
     * @param name
     */
    public TestStringUtil(String name) {
        super(name);
    }

	//  ********* T E S T   S U I T E   M E T H O D S  *********

    public void testGetStackTrace() {
        final String expectedStackTrace = "java.lang.RuntimeException: Test"; //$NON-NLS-1$
        final Throwable t = new RuntimeException("Test"); //$NON-NLS-1$
        final String trace = StringUtil.getStackTrace(t);
        if ( !trace.startsWith(expectedStackTrace) ) {
            fail("Stack trace: \n" + trace + "\n did not match expected stack trace: \n" + expectedStackTrace); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void testToString() {
        final String[] input = new String[]{"string1","string2","string3"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final String output = StringUtil.toString(input);
        assertEquals("[string1,string2,string3]", output); //$NON-NLS-1$
    }

    public void testToUpperCase() {
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", StringUtil.toUpperCase("abcdefghijklmnopqrstuvwxyz1234567890")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("LATIN1_\u00c0", StringUtil.toUpperCase("Latin1_\u00e0")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testGetFirstLastToken(){
    	assertEquals("/foo/bar", StringUtil.getFirstToken("/foo/bar.vdb", "."));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	assertEquals("", StringUtil.getFirstToken("/foo/bar.vdb", "/"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	assertEquals("/foo", StringUtil.getFirstToken("/foo./bar.vdb", "."));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	assertEquals("bar", StringUtil.getFirstToken(StringUtil.getLastToken("/foo/bar.vdb", "/"), "."));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    	assertEquals("vdb", StringUtil.getLastToken("/foo/bar.vdb", "."));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public enum Test {
    	HELLO,
    	WORLD
    }
    
    public void testValueOf() throws Exception {
    	assertEquals(Integer.valueOf(21), StringUtil.valueOf("21", Integer.class)); //$NON-NLS-1$
    	assertEquals(Boolean.valueOf(true), StringUtil.valueOf("true", Boolean.class)); //$NON-NLS-1$    	
    	assertEquals("Foo", StringUtil.valueOf("Foo", String.class)); //$NON-NLS-1$ //$NON-NLS-2$
    	assertEquals(Float.valueOf(10.12f), StringUtil.valueOf("10.12", Float.class)); //$NON-NLS-1$
    	assertEquals(Double.valueOf(121.123), StringUtil.valueOf("121.123", Double.class)); //$NON-NLS-1$
    	assertEquals(Long.valueOf(12334567L), StringUtil.valueOf("12334567", Long.class)); //$NON-NLS-1$
    	assertEquals(Short.valueOf((short)21), StringUtil.valueOf("21", Short.class)); //$NON-NLS-1$
    	
    	List list = StringUtil.valueOf("foo,bar,x,y,z", List.class); //$NON-NLS-1$
    	assertEquals(5, list.size());
    	assertTrue(list.contains("foo")); //$NON-NLS-1$
    	assertTrue(list.contains("x")); //$NON-NLS-1$
    	
    	int[] values = StringUtil.valueOf("1,2,3,4,5", new int[0].getClass()); //$NON-NLS-1$
    	assertEquals(5, values.length);
    	assertEquals(5, values[4]);
    	
    	Map m = StringUtil.valueOf("foo=bar,x=,y=z", Map.class); //$NON-NLS-1$
    	assertEquals(3, m.size());
    	assertEquals(m.get("foo"), "bar"); //$NON-NLS-1$ //$NON-NLS-2$
    	assertEquals(m.get("x"), ""); //$NON-NLS-1$ //$NON-NLS-2$
    	assertEquals(Test.HELLO, StringUtil.valueOf("HELLO", Test.class)); //$NON-NLS-1$ 
    	
    	assertEquals(new URL("http://teiid.org"), StringUtil.valueOf("http://teiid.org", URL.class)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
