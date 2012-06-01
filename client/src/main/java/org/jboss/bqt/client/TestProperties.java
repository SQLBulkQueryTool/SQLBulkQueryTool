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
package org.jboss.bqt.client;

import java.util.Properties;

import org.jboss.bqt.core.exception.FrameworkRuntimeException;

/**
 * @author vanhalbert
 * 
 */
public class TestProperties {

	/**
	 * PROP_SCENARIO_FILE indicates the scenario properties file to load.
	 */
	public static final String PROP_SCENARIO_FILE = "scenariofile";

	/**
	 * The {@link #QUERY_SET_NAME} property indicates the name of directory that
	 * contains the set of queries and expected results that will be used. This
	 * is referred to as the <b>query set</b>
	 * 
	 * This property should be found in the {@link #PROP_SCENARIO_FILE}.
	 */
	public static final String QUERY_SET_NAME = "queryset.dir"; //$NON-NLS-1$

	/**
	 * PROP_RESULT_MODE controls what to do with the execution results.
	 * 
	 */
	public static final String PROP_RESULT_MODE = "resultmode";
	
	/**
	 * PROP_EXECUTE_EXCEED_PERCENT controls, when specified, at what percent execution time can
	 * exceed expected results.  When a query execution time is exceeded, an exception
	 * will be thrown.
	 */
	public static final String PROP_EXECUTE_EXCEED_PERCENT = "exceedpercent";	

	/**
	 * PROP_EXECUTE_TIME_MINEMUM indicates the minimum time at which a query will be validated if
	 * it exceeds the {@link #PROP_EXECUTE_EXCEED_PERCENT} above the expected results 
	 * execution time (i.e., exectime).  By increasing this time, it will filter out the short running
	 * queries and focus on the longer running queries.
	 */
	public static final String PROP_EXECUTE_TIME_MINEMUM = "exectimemin";	 // milliseconds


	/**
	 * All test options will produce the following basic information at the end
	 * of the test process: <li>how many queries were run</li> <li>how many were
	 * successfull</li> <li>how many errored</li> <li>the execution time for
	 * each query</li> <li>total time for all the tests to run</li>
	 */

	public interface RESULT_MODES {
		/**
		 * NONE - will provide the basic information
		 */
		static final String NONE = "NONE";
		/**
		 * COMPARE - will provide the following information, in addition to the
		 * basic information <li>compare actual results with expected results
		 * and produce error files where expected results were not accomplished</li>
		 */
		static final String COMPARE = "COMPARE";
		/**
		 * GENERATE - will provide the following information, in addition to the
		 * basic information <li>will generate a new set of expected results
		 * files to the defined PROP_GENERATAE_DIR directory.
		 */
		static final String GENERATE = "GENERATE";
		
		/**
		 * SQL - will generate a queries file that contains SQL queries based on the databasemetadata.
		 */
		static final String SQL = "SQL";
	}

	/**
	 * The {@link #PROP_OUTPUT_DIR} property indicates the root directory that
	 * output files will be written.
	 */
	public static final String PROP_OUTPUT_DIR = "outputdir"; //$NON-NLS-1$
	
	
	public static String getResultMode(Properties props) {
		// Determine from property what to do with query results
		String resultModeStr = props.getProperty(
				TestProperties.PROP_RESULT_MODE, "");
		// No need to check for null prop here since we've just checked for this
		// required property
		
		resultModeStr = resultModeStr.trim().toUpperCase();


		if (resultModeStr.startsWith(TestProperties.RESULT_MODES.COMPARE)) {
			return TestProperties.RESULT_MODES.COMPARE;
		} else if (resultModeStr.startsWith(TestProperties.RESULT_MODES.GENERATE)) {
			return TestProperties.RESULT_MODES.GENERATE;
		} else if (resultModeStr.startsWith(TestProperties.RESULT_MODES.SQL)) {
			return TestProperties.RESULT_MODES.SQL;
		} else if (resultModeStr.startsWith(TestProperties.RESULT_MODES.NONE)) {
			return TestProperties.RESULT_MODES.NONE;
		} else {
			throw new FrameworkRuntimeException(
			"Invalid results mode of "+ resultModeStr + " must be COMPARE, GENERATE, SQL or NONE");

		}

	}


}
