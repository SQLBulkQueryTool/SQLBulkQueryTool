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

import org.jboss.bqt.framework.connection.ConnectionStrategy;


/**
 * The following properties can be set in 2 ways: <li>set as a System
 * property(..)</li> <li>specify it in the config properties file</li>
 * 
 * @author vanhalbert
 * 
 */
public interface ConfigPropertyNames {

	/**
	 * Specify this as a system property to set a specific configuration to use
	 * otherwise the {@link ConfigPropertyLoader#DEFAULT_CONFIG_FILE_NAME} will
	 * be loaded.
	 */
	public static final String CONFIG_FILE = "config";

	/**
	 * For Driver/Datasource connection related properties,
	 * {@link ConnectionStrategy}.
	 */

	/**
	 * Connection Type indicates the type of connection (strategy) to use when
	 * connecting to Teiid. Options are {@link CONNECTION_TYPES}
	 */
	public static final String CONNECTION_TYPE = "conn.type"; //$NON-NLS-1$

	/**
	 * Connection_Types indicates the method on connection to the source that 
	 * will be used.
	 * 
	 * @author vanhalbert
	 * 
	 */
	public interface CONNECTION_TYPES {

		// used to create the jdb driver
		public static final String DRIVER_CONNECTION = "driver"; //$NON-NLS-1$
		// used to create a datasource
		public static final String DATASOURCE_CONNECTION = "datasource"; //$NON-NLS-1$
		// used for when embedded is running in an appserver
		public static final String JNDI_CONNECTION = "jndi"; //$NON-NLS-1$

	}

	/**
	 * Connection Props are the {@link ConnectionStrategy} execution options
	 * 
	 * @author vanhalbert
	 * 
	 */
	public interface CONNECTION_STRATEGY_PROPS {

//		public static final String TXN_AUTO_WRAP = "autoCommitTxn";
//		public static final String AUTOCOMMIT = "autocommit"; //$NON-NLS-1$
//		public static final String FETCH_SIZE = "fetchSize";
		public static final String JNDINAME_USERTXN = "usertxn-jndiname"; //$NON-NLS-1$  

	}

	public interface TXN_AUTO_WRAP_OPTIONS {
		public static final String AUTO_WRAP_OFF = "OFF"; //$NON-NLS-1$	    
		public static final String AUTO_WRAP_ON = "ON"; //$NON-NLS-1$
		public static final String AUTO_WRAP_AUTO = "DETECT"; //$NON-NLS-1$

	}

}
