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

package org.jboss.bqt.client.api;

import java.io.File;
import java.sql.ResultSet;

import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;

/**
 * An ExpectedResultsReader represents one set of expected results (referred to as the
 * queryset) that will be read when the result mode is either {@link TestProperties.RESULT_MODES#NONE none} or
 * {@link TestProperties.RESULT_MODES#COMPARE compare} or {@link TestProperties.RESULT_MODES#GENERATE generate}.  
 * The query files should be found in the {@link TestProperties#PROP_EXPECTED_RESULTS_DIR_LOC executedResultsDir}
 * and identified by the {@link #getQuerySetID}. The
 * <code>queryidentifier</code> identify a unique query and corresponds to the
 * expected results file.
 * 
 * 
 * @author vanhalbert
 * 
 */
public interface ExpectedResultsReader {

	/**
	 * Returns the full path to the expected results location.
	 * @return String full directory path
	 * 
	 * @see TestProperties#PROP_EXPECTED_RESULTS_DIR_LOC
	 */
	String getExpectResultsLocation();

	/**
	 * Return the unique identifier for this query set.
	 * 
	 * @return QuerySetID
	 */
	String getQuerySetID();

	/**
	 * Returns the <code>File</code> location for the actual results for the
	 * specified query identifier.
	 * 
	 * @param queryidentifier
	 * @return File location for actual results for the specified query
	 * @throws FrameworkRuntimeException
	 * 
	 * @since
	 */
	File getResultsFile(String queryidentifier) throws FrameworkRuntimeException;

	/**
	 * 
	 *      Return true if the expected results file is needed in the test.
	 *      Either for comparison or generation. It will return false when the
	 *      option <code>TestProperties.RESULT_MODES.NONE</code>
	 * @return boolean true if expected results are needed for the test
	 */
	boolean isExpectedResultsNeeded();

	/**
	 * Indicates if a query expects to have an <code>Exception</code> to be
	 * thrown when the query is executed.
	 * 
	 * @param queryidentifier
	 * @return boolean true if the query expects an exception to be thrown
	 * @throws FrameworkRuntimeException
	 */
	boolean isExceptionExpected(String queryidentifier)
			throws FrameworkRuntimeException;

	/**
	 * Called to compare the <code>ResultSet</code> from the executed query to
	 * the expected results and return the errors.
	 * @param testresults 
	 * @param resultSet 
	 * @param isOrdered 
	 * @return Object identifying the errors in the comparison
	 * @throws QueryTestFailedException
	 */
	
	Object compareResults(final TestResult testresults,
			final ResultSet resultSet, final boolean isOrdered) throws QueryTestFailedException;

	

}
