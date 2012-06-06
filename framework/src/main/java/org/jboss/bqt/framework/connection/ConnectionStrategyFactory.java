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

import java.util.Properties;

import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.ConfigPropertyNames;
import org.jboss.bqt.framework.FrameworkPlugin;

/**
 * The ConnectionStrategyFactory is responsible for creating a connection
 * strategy that is to be used to provide the type of connection.
 * 
 */
public class ConnectionStrategyFactory {

	public static ConnectionStrategy createConnectionStrategy() {

		ConfigPropertyLoader configLoader = ConfigPropertyLoader.getInstance();

		ConnectionStrategy strategy = null;
		Properties props = configLoader.getProperties();

		String type = props.getProperty(ConfigPropertyNames.CONNECTION_TYPE,
				ConfigPropertyNames.CONNECTION_TYPES.DRIVER_CONNECTION);
		if (type == null) {
			throw new FrameworkRuntimeException("Property "
					+ ConfigPropertyNames.CONNECTION_TYPE + " was specified");
		}

		try {

			if (type.equalsIgnoreCase(ConfigPropertyNames.CONNECTION_TYPES.DRIVER_CONNECTION)) {
				// pass in null to create new strategy
				strategy = new DriverConnection(props);
				FrameworkPlugin.LOGGER.debug("Created Driver Strategy");
			} else if (type
					.equalsIgnoreCase(ConfigPropertyNames.CONNECTION_TYPES.DATASOURCE_CONNECTION)) {
				strategy = new DataSourceConnection(props);
				FrameworkPlugin.LOGGER.debug("Created DataSource Strategy");
			} else if (type
					.equalsIgnoreCase(ConfigPropertyNames.CONNECTION_TYPES.JNDI_CONNECTION)) {
				strategy = new JEEConnection(props);
				FrameworkPlugin.LOGGER.debug("Created JEE Strategy");
			}

			if (strategy == null) {
				throw new FrameworkRuntimeException("Invalid property value for "
						+ ConfigPropertyNames.CONNECTION_TYPE + " is " + type);
			} 
			
			// call configure here because this is creating the connection to
			// Teiid
			// direct connections to the datasource use the static call directly
			// to create strategy and don't need to configure
			strategy.configure();
			return strategy;

		} catch (Exception e) {
			throw new FrameworkRuntimeException(e);
		}

	}

}
