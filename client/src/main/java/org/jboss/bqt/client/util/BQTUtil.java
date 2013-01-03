/*
 * ModeShape (http://www.modeshape.org)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors.
 *
 * ModeShape is free software. Unless otherwise indicated, all code in ModeShape
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * ModeShape is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.bqt.client.util;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.FileType;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.ReflectionHelper;

/**
 * @author vhalbert
 * 
 */
public class BQTUtil {

	public static void throwInvalidProperty(String property)
			throws FrameworkRuntimeException {
		final String msg = ClientPlugin.Util.getString(
				"BQTFramework.invalidProperty", property); //$NON-NLS-1$            
		throw new FrameworkRuntimeException(msg);
	}

	/**
	 * Call to obtain all the query {@link File files} defined for this tests
	 * that will be executed.
	 * 
	 * @param query_dir_location
	 *            to the query files
	 * @return File[] that is all the queries to be executed.
	 * 
	 */
	public static File[] getQuerySetFiles(String query_dir_location) {

		String loc = query_dir_location;

		ClientPlugin.LOGGER.info("Loading queries from " + loc);

		File files[] = FileUtils.findAllFilesInDirectoryHavingExtension(loc,
				".xml");
		if (files == null || files.length == 0) {
			final String msg = ClientPlugin.Util.getString(
					"QueryReader.noQueryFiles", loc); //$NON-NLS-1$     
			throw new FrameworkRuntimeException(msg);
		}

		return files;

	}

	public static String getResultMode(Properties props) {
		// Determine from property what to do with query results
		String resultModeStr = props
				.getProperty(TestProperties.PROP_RESULT_MODE);
		// No need to check for null prop here since we've just checked for this
		// required property

		if (resultModeStr == null || resultModeStr.startsWith("${")) {
			resultModeStr = TestProperties.RESULT_MODES.NONE;
		}

		resultModeStr = resultModeStr.trim().toUpperCase();

		if (resultModeStr.startsWith(TestProperties.RESULT_MODES.COMPARE)) {
			return TestProperties.RESULT_MODES.COMPARE;
		} else if (resultModeStr
				.startsWith(TestProperties.RESULT_MODES.GENERATE)) {
			return TestProperties.RESULT_MODES.GENERATE;
		} else if (resultModeStr.startsWith(TestProperties.RESULT_MODES.SQL)) {
			return TestProperties.RESULT_MODES.SQL;
		} else if (resultModeStr.startsWith(TestProperties.RESULT_MODES.NONE)) {
			return TestProperties.RESULT_MODES.NONE;
		} else {
			final String msg = ClientPlugin.Util.getString(
					"ResultMode.invalidResultMode", resultModeStr); //$NON-NLS-1$     
			throw new FrameworkRuntimeException(msg);
		}

	}

	public static FileType createFileType(Properties properties) {
		String clzzname = properties.getProperty(FileType.FILE_TYPE_CLASSNAME);
		if (clzzname == null || clzzname.trim().length() == 0
				|| clzzname.startsWith("${")) {
			clzzname = FileType.DEFAULT_FILE_TYPE_CLASSNAME;
		}

		return (FileType) ReflectionHelper.create(clzzname,
				Collections.EMPTY_LIST, null);

	}

}
