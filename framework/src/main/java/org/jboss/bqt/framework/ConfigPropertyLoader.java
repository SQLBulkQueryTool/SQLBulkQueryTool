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

import java.util.Properties;

import org.jboss.bqt.core.util.PropertiesUtils;

/**
 * The ConfigProperteryLoader will load the configuration properties to be used
 * by a test. Unless a different configuraton file is specified, subsequent
 * loading of the configuration fill will not occur. However,
 * <code>overrides</code> that are applied per test
 * 
 * 
 * @author vanhalbert
 * 
 */

public class ConfigPropertyLoader {
	private static final String CONFIG_TEMPLATE_FILE_NAME = "config-template.properties";

	/**
	 * The default config file to use when #CONFIG_FILE system property isn't
	 * set
	 */
	public static final String DEFAULT_CONFIG_FILE_NAME = "default-config.properties";

	private static ConfigPropertyLoader _instance = null;
	private static String LAST_CONFIG_FILE = null;

	/**
	 * Contains any overrides specified for the test
	 */
	private Properties overrides = new Properties();

	/**
	 * Contains the properties loaded from, first the template,
	 * and then the config file 
	 */
	private Properties props = null;

	private ConfigPropertyLoader() {
	}

	public static synchronized ConfigPropertyLoader getInstance() {
		boolean diff = differentConfigProp();

		if (_instance != null) {
			if (!diff) {
				return _instance;
			}

			reset();

		}

		_instance = new ConfigPropertyLoader();

		_instance.initialize();

		return _instance;
	}

	/**
	 * because a config file could be different for the subsequent test, check
	 * to see if the file is different.
	 * 
	 * @return boolean
	 */
	private static boolean differentConfigProp() {
		String filename = System.getProperty(ConfigPropertyNames.CONFIG_FILE);
		if (filename == null) {
			filename = DEFAULT_CONFIG_FILE_NAME;
		}

		if (LAST_CONFIG_FILE == null
				|| !LAST_CONFIG_FILE.equalsIgnoreCase(filename)) {
			LAST_CONFIG_FILE = filename;
			return true;
		}
		return false;

	}

	/**
	 * Called after each test to reset any per test settings.
	 */
	public static synchronized void reset() {
		if (_instance == null)
			return;

		_instance.overrides.clear();

		_instance.props.clear();

		_instance = null;
		LAST_CONFIG_FILE = null;

	}

	private void initialize() {

		Properties templateProps = PropertiesUtils.load(CONFIG_TEMPLATE_FILE_NAME, null);
		Properties defaultProps = PropertiesUtils.load(LAST_CONFIG_FILE, null);
		templateProps.putAll(defaultProps);
		props = PropertiesUtils.resolveNestedProperties(templateProps, false);
		
	}

	public String getProperty(String key) {
		String rtn = null;
		rtn = overrides.getProperty(key);
		if (rtn == null) {
			rtn = props.getProperty(key);

			if (rtn == null) {
				rtn = System.getProperty(key);
			}
		}
		return rtn;
	}

	public void setProperty(String key, String value) {
		overrides.setProperty(key, value);
	}

	public void setProperties(Properties props) {
		overrides.putAll(props);
	}

	public Properties getProperties() {

		Properties p = PropertiesUtils.clone(System.getProperties());
		if (props != null) {
			p.putAll(props);
		}
		if (overrides != null) {
			p.putAll(overrides);
		}

		return p;
	}

}
