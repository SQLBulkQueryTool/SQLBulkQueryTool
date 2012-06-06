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
package org.jboss.bqt.framework.connection;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.XAConnection;

import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.PropertiesUtils;

/**
 * The ConnectionStrategy is the base class for the different types of connections that are supported for executing queries.
 * @author vhalbert
 *
 */
public abstract class ConnectionStrategy {

	private Properties env = null;

	public ConnectionStrategy(Properties props) {
		this.env = PropertiesUtils.clone(props);

	}

	/*
	 * Lifecycle methods for managing the connection
	 */

	/**
	 * Returns a connection
	 * 
	 * @return Connection
	 * @throws QueryTestFailedException
	 */
	public abstract Connection getConnection() throws QueryTestFailedException;


	/**
	 * @since
	 */
	public void shutdown() {

	}

	/**
	 * @return Connection
	 * @throws QueryTestFailedException
	 */
	public Connection getAdminConnection() throws QueryTestFailedException {
		return null;
	}

	private boolean autoCommit = true;

	public boolean getAutocommit() {
		return autoCommit;
	}
	
	public void setAutocommit(boolean autocommit) {
		this.autoCommit = autocommit;
	}

	/**
	 * @return XAConnection
	 * @throws QueryTestFailedException
	 */
	public XAConnection getXAConnection() throws QueryTestFailedException {
		return null;
	}

	public Properties getEnvironment() {
		return env;
	}

	public void setEnvironmentProperty(String key, String value) {
		this.env.setProperty(key, value);
	}

	/**
	 * @throws QueryTestFailedException
	 */
	void configure() throws QueryTestFailedException {

	}
}
