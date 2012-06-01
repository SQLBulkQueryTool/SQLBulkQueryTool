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
package org.jboss.bqt.core.util.log;

import org.jboss.bqt.core.util.Logger;

public abstract class LogFactory {

    private static LogFactory LOGFACTORY;

    static {
        try {
            loadClassStrict("org.apache.log4j.Logger");
            LOGFACTORY = new SLF4JLoggerFactory();
            
            

        } catch (ClassNotFoundException cnfe) {
            LOGFACTORY = new JdkLoggerFactory();
        }

    }
    
    /**
     * Loads the class using either the current thread's context class loader or, if that is null, the system class loader.
     * 
     * @param classname name of the class to load
     * @return the class
     * @throws ClassNotFoundException if the class could not be found
     */
    public static Class<?> loadClassStrict( String classname ) throws ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = ClassLoader.getSystemClassLoader();
        return cl.loadClass(classname);
    }

    public static LogFactory getLogFactory() {
        return LOGFACTORY;
    }

    /**
     * Return a logger named corresponding to the class passed as parameter.
     * 
     * @param clazz the returned logger will be named after clazz
     * @return logger
     */
    public abstract Logger getLogger( Class<?> clazz );

    /**
     * Return a logger named according to the name parameter.
     * 
     * @param name The name of the logger.
     * @return logger
     */
    public abstract Logger getLogger( String name );

}

final class SLF4JLoggerFactory extends LogFactory {

    @Override
    public Logger getLogger( Class<?> clazz ) {
        return getLogger(clazz.getName());
    }

    @Override
    public Logger getLogger( String name ) {
        return new SLF4JLoggerImpl(name);
    }

}

 final class JdkLoggerFactory extends LogFactory {
	    @Override
	    public Logger getLogger( Class<?> clazz ) {
	        return getLogger(clazz.getName());
	    }

	    @Override
	    public Logger getLogger( String name ) {
	        return new JdkLoggerImpl(name);
	    }
	}
