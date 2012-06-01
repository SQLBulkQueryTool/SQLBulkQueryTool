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
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.jboss.bqt.core.util.PropertiesUtils;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.FrameworkPlugin;

public class DataSourceConnection extends ConnectionStrategy {

	// the driver is only used for making direct connections to the source, the
	// connector type will provide the JDBCPropertyNames.CONNECTION_SOURCE
	// driver class
	public static final String DS_DRIVER = "driver"; //$NON-NLS-1$

	public static final String DS_DATASOURCE = "datasource"; //$NON-NLS-1$

	public static final String DS_SERVERNAME = "ServerName"; //$NON-NLS-1$
	public static final String DS_SERVERPORT = "PortNumber"; //$NON-NLS-1$
	public static final String DS_JNDINAME = "ds-jndiname"; //$NON-NLS-1$
	public static final String DS_DATABASENAME = "DatabaseName"; //$NON-NLS-1$
	public static final String DS_APPLICATION_NAME = "application-name"; //$NON-NLS-1$
	public static final String DS_URL = "URL"; //$NON-NLS-1$
	public static final String DS_USER = "User"; //$NON-NLS-1$
	// need both user variables because Teiid uses 'user' and connectors use
	// 'username'
	public static final String DS_USERNAME = "User"; //$NON-NLS-1$
	public static final String DS_PASSWORD = "Password"; //$NON-NLS-1$

	private String driver = null;
	private String username = null;
	private String pwd = null;
	private String applName = null;
	private String databaseName = null;
	private String serverName = null;
	private String portNumber = null;
	private String url = null;

	private XAConnection xaConnection;

	public DataSourceConnection(Properties props)
			throws QueryTestFailedException {
		super(props);
	}

	public void validate() {
		databaseName = this.getEnvironment().getProperty(DS_DATABASENAME);
		if (databaseName == null || databaseName.length() == 0) {
			throw new FrameworkRuntimeException("Property " + DS_DATABASENAME
					+ " was not specified");
		}

		serverName = this.getEnvironment().getProperty(DS_SERVERNAME);
		if (serverName == null || serverName.length() == 0) {
			throw new FrameworkRuntimeException("Property " + DS_SERVERNAME
					+ " was not specified");
		}

		this.portNumber = this.getEnvironment().getProperty(DS_SERVERPORT);

		this.applName = this.getEnvironment().getProperty(DS_APPLICATION_NAME);

		if (this.getEnvironment().getProperty(DS_DATASOURCE) != null) {
			this.driver = this.getEnvironment().getProperty(DS_DATASOURCE);
			if (this.driver == null || this.driver.length() == 0) {
				throw new FrameworkRuntimeException("Property "
						+ DS_DATASOURCE + " was null");
			}

		} else {
			this.driver = this.getEnvironment().getProperty(DS_DRIVER);
			if (this.driver == null || this.driver.length() == 0) {
				throw new FrameworkRuntimeException("Property " + DS_DRIVER
						+ " was not specified");
			}
		}

		this.url = this.getEnvironment().getProperty(DS_URL);

		this.username = this.getEnvironment().getProperty(DS_USER);
		if (username == null) {
			this.username = this.getEnvironment().getProperty(DS_USERNAME);
		}
		this.pwd = this.getEnvironment().getProperty(DS_PASSWORD);

	}

	public Connection getConnection() throws QueryTestFailedException {
		try {
			return getXAConnection().getConnection();
		} catch (QueryTestFailedException qtf) {
			throw qtf;
		} catch (Exception e) {
			e.printStackTrace();
			throw new QueryTestFailedException(e);
		}
	}

	public synchronized XAConnection getXAConnection()
			throws QueryTestFailedException {
		if (xaConnection == null) {
			validate();
			try {
				xaConnection = createConnection();
			} catch (Exception e) {
				throw new QueryTestFailedException(e);
			}
		}
		return xaConnection;
	}

	private XAConnection createConnection() throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {

		FrameworkPlugin.LOGGER
				.info("Creating Datasource Connection: \"" + this.serverName + " - " + this.databaseName + "\""); //$NON-NLS-1$ //$NON-NLS-2$

		DataSource ds = (DataSource) Class.forName(this.driver).newInstance();

		// if (ds instanceof DataSource) {
		//
		// DataSource dataSource = (DataSource) ds;
		//
		// dataSource.setDatabaseName(this.databaseName);
		// if (this.applName != null) {
		// dataSource.setApplicationName(this.applName);
		// }
		//
		// ((TeiidDataSource) dataSource).setServerName(this.serverName);
		// ((TeiidDataSource)
		// dataSource).setPortNumber(Integer.parseInt(this.portNumber));
		//
		// if (this.username != null) {
		// dataSource.setUser(this.username);
		// dataSource.setPassword(this.pwd);
		// }
		//
		// return ((XADataSource) dataSource).getXAConnection(this.username,
		// this.pwd);
		// }
		Properties props = new Properties();
		props.setProperty(DS_DATABASENAME, this.databaseName);
		props.setProperty(DS_SERVERPORT, this.portNumber);
		props.setProperty(DS_URL, this.url);
		props.setProperty(DS_SERVERNAME, this.serverName);
		if (this.username != null) {
			props.setProperty(DS_USERNAME, this.username);
			props.setProperty(DS_USER, this.username);
			props.setProperty(DS_PASSWORD, this.pwd);
		}

		PropertiesUtils.setBeanProperties(ds, props, null);
		return ((XADataSource) ds).getXAConnection();
	}

	public void shutdown() {
		super.shutdown();
		try {

			if (this.xaConnection != null) {
				this.xaConnection.close();
			}
		} catch (Exception e) {
			// ignore..
		}
		this.xaConnection = null;
	}
}
