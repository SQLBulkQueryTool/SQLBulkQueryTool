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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.FrameworkPlugin;

/**
 * The DriverConnection strategy that can get connections in standalone mode or
 * embedded mode.
 */
public class DriverConnection extends ConnectionStrategy {

	public static final String DS_USER = "user"; //$NON-NLS-1$

	// need both user variables because Teiid uses 'user' and connectors use
	// 'username'
	public static final String DS_USERNAME = "User"; //$NON-NLS-1$
	public static final String DS_PASSWORD = "Password"; //$NON-NLS-1$

	// the driver is only used for making direct connections to the source, the
	// connector type will provide the JDBCPropertyNames.CONNECTION_SOURCE
	// driver class
	public static final String DS_DRIVER = "driver"; //$NON-NLS-1$

	public static final String DS_URL = "URL"; //$NON-NLS-1$
	public static final String DS_APPLICATION_NAME = "application-name"; //$NON-NLS-1$

	private String url = null;
	private String driver = null;
	private String username = null;
	private String pwd = null;

	private Connection connection;

	public DriverConnection(Properties props) throws QueryTestFailedException {
		super(props);
		validate();
	}

	public void validate() {

		String urlProp = this.getEnvironment().getProperty(DS_URL);
		if (urlProp == null || urlProp.length() == 0) {
			throw new FrameworkRuntimeException("Property " + DS_URL
					+ " was not specified");
		}
		StringBuffer urlSB = new StringBuffer(urlProp);

		String appl = this.getEnvironment().getProperty(DS_APPLICATION_NAME);
		if (appl != null) {
			urlSB.append(";");
			urlSB.append("ApplicationName").append("=").append(appl);
		}

		url = urlSB.toString();

		driver = this.getEnvironment().getProperty(DS_DRIVER);
		if (driver == null || driver.length() == 0) {
			throw new FrameworkRuntimeException("Property " + DS_DRIVER
					+ " was not specified");
		}

		// need both user variables because Teiid uses 'user' and connectors use
		// 'username'

		this.username = this.getEnvironment().getProperty(DS_USER);
		if (username == null) {
			this.username = this.getEnvironment().getProperty(DS_USERNAME);
		}
		this.pwd = this.getEnvironment().getProperty(DS_PASSWORD);

		try {
			// Load jdbc driver
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new FrameworkRuntimeException(e);
		}

	}

	public synchronized Connection getConnection()
			throws QueryTestFailedException {
		if (this.connection != null) {
			try {
				if (!this.connection.isClosed()) {
					return this.connection;
				}
			} catch (SQLException e) {

			}

		}

		this.connection = getJDBCConnection(this.driver, this.url,
				this.username, this.pwd);
		return this.connection;
	}

	private Connection getJDBCConnection(String driver, String url,
			String user, String passwd) throws QueryTestFailedException {

		FrameworkPlugin.LOGGER.info("Creating Driver Connection: \"" + url + "\"" + " user:password - " + (user != null ? user : "NA") + ":" + (passwd != null ? passwd : "NA")); //$NON-NLS-1$ //$NON-NLS-2$

		Connection conn = null;
		try {
			// Create a connection
			if (user != null && user.length() > 0) {
				conn = DriverManager.getConnection(url, user, passwd);
			} else {
				conn = DriverManager.getConnection(url);
			}

		} catch (Throwable t) {
			t.printStackTrace();
			throw new QueryTestFailedException(t.getMessage());
		}
		return conn;

	}

	@Override
	public void shutdown() {
		super.shutdown();
		if (this.connection != null) {
			try {
				this.connection.close();
			} catch (Exception e) {
				// ignore
			}
		}

		this.connection = null;

	}
}
