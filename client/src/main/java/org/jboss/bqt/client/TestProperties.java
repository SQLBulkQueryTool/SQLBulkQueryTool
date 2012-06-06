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

import java.io.File;
import java.util.Properties;

import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.FileUtils;

/**
 * The TestProperties are those properties that define a test scenario.  
 * 
 * Note:  The scenario and query set files are 
 * <p>
 * A {@link #PROP_SCENARIO_FILE scenarioFile} consist of:
 * <li>{@link #QUERY_SET_NAME querySet} - which queries to execute</li>
 * <li>{@link #PROP_RESULT_MODE resultMode} - how to process the queries {@link RESULT_MODES modes}</li>
 * <li>{@link #PROP_OUTPUT_DIR outputDirectory} - where to write the output from the test</li> 
 * <p>
 * Options to validate query execution time:
 * <li>{@link #PROP_EXECUTE_EXCEED_PERCENT exceedPct} - flag the query when it deviates greater than this percent</li>
 * <li>{@link #PROP_EXECUTE_TIME_MINEMUM minTime} - indicates a query must meet this minimum execution threshold, before 
 * it will be checked for {@link #PROP_EXECUTE_EXCEED_PERCENT}.</li>  
 * 
 * 
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
	 * {@link #PROP_QUERY_FILES_DIR_LOC} indicates the location to find the
	 * query files
	 */
	public static final String PROP_QUERY_FILES_DIR_LOC = "queryfiles.loc";

	/**
	 * {@link #PROP_QUERY_FILES_ROOT_DIR}, if specified, indicates the root
	 * directory to be prepended to the {@link #PROP_QUERY_FILES_DIR_LOC} to
	 * create the full directory to find the query files.
	 * 
	 * This property is normally used during the nightly builds so that the
	 * query files will coming from other projects.
	 */
	public static final String PROP_QUERY_FILES_ROOT_DIR = "queryfiles.root.dir";

	/**
	 * The results location is where expected result files can be found
	 */
	public static final String PROP_EXPECTED_RESULTS_DIR_LOC = "results.loc";

	/**
	 * Optional, {@link #PROP_EXPECTED_RESULTS_ROOT_DIR}, if specified, indicates the root
	 * directory to be prepended to the {@link #PROP_EXPECTED_RESULTS_DIR_LOC}
	 * to create the full directory to find the expected results files.
	 * 
	 * This property is normally used during the nightly builds so that the
	 * query files will coming from other projects.
	 */

	public static final String PROP_EXPECTED_RESULTS_ROOT_DIR = "results.root.dir";

	/**
	 * {@link #PROP_SUMMARY_PRT_DIR} is the directory where summary reports
	 * will be written to.
	 */
	public static final String PROP_SUMMARY_PRT_DIR = "summarydir";

	/**
	 * PROP_RESULT_MODE controls what to do with the execution results.
	 * 
	 */
	public static final String PROP_RESULT_MODE = "resultmode";

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
	
	/**
	 * The {@link #PROP_GENERATE_DIR} property indicates where newly generated
	 * results files will be written to. The newly generated files should be
	 * written to a different location than the existing expected results.
	 */
	public static final String PROP_GENERATE_DIR = "generatedir"; //$NON-NLS-1$

	/**
	 * The {@link #PROP_SQL_DIR} property indicates where newly generated
	 * test query files will be written to. The newly created sql files should be
	 * written to a different location than the existing test queries.
	 */
	public static final String PROP_SQL_DIR = "sqldir"; //$NON-NLS-1$
	
	
	public static String getResultMode(Properties props) {
		// Determine from property what to do with query results
		String resultModeStr = props.getProperty(
				TestProperties.PROP_RESULT_MODE);
		// No need to check for null prop here since we've just checked for this
		// required property
		
		if (resultModeStr == null) {
			BQTUtil.throwInvalidProperty(PROP_RESULT_MODE);
		}
		
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
	
	/**
	 * Call to obtain all the query {@link File files} defined for this tests
	 * that will be executed.
	 *
	 * @param props
	 * @return File[] that is all the queries to be executed.
	 * @throws QueryTestFailedException
	 * 
	 * @see #PROP_QUERY_FILES_DIR_LOC
	 * @see #PROP_QUERY_FILES_ROOT_DIR
	 */
	public static File[] loadQuerySets(Properties props) throws QueryTestFailedException {
		String query_dir_loc = props.getProperty(PROP_QUERY_FILES_DIR_LOC);
		if (query_dir_loc == null) {
				BQTUtil.throwInvalidProperty(PROP_QUERY_FILES_DIR_LOC);
		}

		String query_root_loc = props
				.getProperty(PROP_QUERY_FILES_ROOT_DIR);

		String loc = query_dir_loc;

		if (query_root_loc != null) {
			File dir = new File(query_root_loc, query_dir_loc);
			loc = dir.getAbsolutePath();
		}

		ClientPlugin.LOGGER.info("Loading queries from " + loc);

		File files[] = FileUtils.findAllFilesInDirectoryHavingExtension(loc,
				".xml");
		if (files == null || files.length == 0)
			throw new QueryTestFailedException((new StringBuilder())
					.append("No query files found in directory ").append(loc)
					.toString());
		
		return files;

	}


}
