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
package org.jboss.bqt.test.framework.connection;

import java.sql.Connection;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;

/**
 * JEE (JNDI) Connection Strategy, when the test is run inside an application
 * server. Make sure all the jndi names are set correctly in the properties
 * file.
 */
public class JEEConnection extends ConnectionStrategy {

	public static final String DS_JNDINAME = "ds-jndiname"; //$NON-NLS-1$

	private String jndi_name = null;

	public JEEConnection(Properties props) throws QueryTestFailedException {
		super(props);
	}

	public Connection getConnection() throws QueryTestFailedException {
		validate();
		try {
			InitialContext ctx = new InitialContext();
			DataSource source = (DataSource) ctx.lookup(jndi_name);

			if (source == null) {
				String msg = "Unable to find jndi source " + jndi_name;//$NON-NLS-1$

				QueryTestFailedException mme = new QueryTestFailedException(msg);//$NON-NLS-1$
				throw mme;
			}
			Connection conn = source.getConnection();
			return conn;
		} catch (QueryTestFailedException qtfe) {
			throw qtfe;
		} catch (Exception e) {
			throw new QueryTestFailedException(e);
		}
	}

	public void shutdown() {
		super.shutdown();
		// no connection management here; app server takes care of these..
	}

	public void validate() {
		// TODO Auto-generated method stub

		jndi_name = getEnvironment().getProperty(DS_JNDINAME);
		if (jndi_name == null || jndi_name.length() == 0) {
			throw new FrameworkRuntimeException("Property " + DS_JNDINAME
					+ " was not specified");
		}
	}
}
