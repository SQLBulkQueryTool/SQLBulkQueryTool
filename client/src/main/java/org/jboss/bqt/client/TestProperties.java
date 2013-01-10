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



/**
 * The TestProperties are those properties that define a test scenario.  
 * 
 * Note:  The scenario and query set files are what make up the testing suite.
 * <p>The following are core properties:
 * </p>
 * <li><b>Properties used as INPUT to the process</b>
 * <li>{@link #PROP_SCENARIO_FILE scenarioFile} is the driver property file for controling which query sets and 
 * expected result files are used.  Additionally, other properties can be added to this file and picked to be
 * during execution.  The only requirement is either the configuration template.properties or
 * <p>
 * In the scenario file, it is recommended to control which {@link #QUERY_SET_NAME querySet}, 
 * </p>
 * </li>
 * 
 * 
 * 
 * </li>
 * 
 * 
 * A {@link #PROP_SCENARIO_FILE scenarioFile} consist of:
 * <li>{@link #QUERY_SET_NAME querySet} - which queries to execute
 * 	<p>The querySet will be loaded based on #PROP_QUERY_FILES_DIR_LOC.  From this location, 
 * 	the {@link #QUERY_SET_NAME querySetName} must exist as a sub-directory.
 *  </p>
 * </li>
 * <li>{@link #PROP_RESULT_MODE resultMode} - how to process the queries {@link RESULT_MODES modes}</li>
 * <li>{@link #PROP_OUTPUT_DIR outputDirectory} - where to write the output from the test</li> 
 * <li>{@link #PROP_EXPECTED_RESULTS_DIR_LOC} indicates the location that the expected result files should be found.</li>
 * 
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
public interface TestProperties {

	/**
	 * PROP_SCENARIO_FILE indicates the scenario properties file to load.
	 */
	public static final String PROP_SCENARIO_FILE = "bqt.scenario.file";
	
	/**
	 * PROP_TESTRUN_DIR will indicate the root location for everything 
	 * written out for a give scenario.
	 */
	public static final String PROP_TESTRUN_DIR = "bqt.testrun.dir";
	
	/**
	 * PROP_ERRORS_DIR indicates where error files will be written
	 * for a given run.
	 */
	public static final String PROP_ERRORS_DIR = "bqt.errors.dir";

	/**
	 * {@link #PROP_QUERY_FILES_DIR_LOC} indicates the location to find the
	 * query files
	 */
	public static final String PROP_QUERY_FILES_DIR_LOC = "bqt.queryfiles.loc";

	/**
	 * The {@link #QUERY_SET_NAME} property indicates the name of directory that
	 * contains the set of queries and expected results that will be used. This
	 * is referred to as the <b>query set</b>.
	 */
	public static final String QUERY_SET_NAME = "bqt.queryset.dirname"; //$NON-NLS-1$
	
	/**
	 * The results location is where expected result files can be found
	 */
	public static final String PROP_EXPECTED_RESULTS_DIR_LOC = "bqt.expectedresults.loc";

	/**
	 * PROP_RESULT_MODE controls what to do with the execution results.
	 * 
	 */
	public static final String PROP_RESULT_MODE = "bqt.result.mode";

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
	public static final String PROP_OUTPUT_DIR = "bqt.output.dir"; //$NON-NLS-1$
	
	
	/**
	 * The {@link #PROP_COMPARE_DIR} property indicates the root directory
	 * for which all files will be written to for a given scenario
	 */
	public static final String PROP_COMPARE_DIR = "bqt.compare.dir"; //$NON-NLS-1$	
	
	/**
	 * The {@link #PROP_GENERATE_DIR} property indicates where newly generated
	 * results files will be written to. The newly generated files should be
	 * written to a different location than the existing expected results.
	 */
	public static final String PROP_GENERATE_DIR = "bqt.generate.dir"; //$NON-NLS-1$

	/**
	 * The {@link #PROP_SQL_DIR} property indicates where newly generated
	 * test query files will be written to. The newly created sql files should be
	 * written to a different location than the existing test queries.
	 */
	public static final String PROP_SQL_DIR = "bqt.sql.dir"; //$NON-NLS-1$
	
	/**
	 * PROP_EXECUTE_EXCEED_PERCENT controls, when specified, at what percent execution time can
	 * exceed expected results.  When a query execution time is exceeded, an exception
	 * will be thrown.
	 */
	public static final String PROP_EXECUTE_EXCEED_PERCENT = "bqt.exceedpercent";	

	/**
	 * PROP_EXECUTE_TIME_MINEMUM indicates the minimum time at which a query will be validated if
	 * it exceeds the {@link #PROP_EXECUTE_EXCEED_PERCENT} above the expected results 
	 * execution time (i.e., exectime).  By increasing this time, it will filter out the short running
	 * queries and focus on the longer running queries.
	 */
	public static final String PROP_EXECUTE_TIME_MINEMUM = "bqt.exectimemin";	 // milliseconds


	public interface PRE1_0_SCENARIO_SUPPORT {
		public static final String SUPPORT_PRE1_0_SCENARIO = "support.pre1.0.scenario";
		
		public static final String OLD_QUERYSET_DIR = "queryset.dir";
		public static final String OLD_TEST_QUERIES_DIR = "test.queries.dir";
		public static final String OLD_EXPECTED_RESULTS_DIR = "expected.results.dir";
		
		public static final String NEW_QUERYSET_DIR = "queryset.dirname";
		public static final String NEW_TEST_QUERIES_DIR = "test.queries.dirname";
		public static final String NEW_EXPECTED_RESULTS_DIR = "expected.results.dirname";
		
		
	}
	
	
}
