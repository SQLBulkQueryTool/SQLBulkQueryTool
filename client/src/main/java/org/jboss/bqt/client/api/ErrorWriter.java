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

import java.sql.ResultSet;

import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.QueryTestFailedException;


/**
 * The ErrorWriter is responsible for writing out an error file when a query error 
 * occurs.  The file should be written to the location based on  {@link TestProperties#PROP_ERRORS_DIR errorDirectory}.
 *
 * @author vhalbert
 *
 */
public interface ErrorWriter {
	
	/**
	 * Returns the full path to the current location that error files
	 * should be written to.
	 * @return String full directory path
	 * 
	 * @see TestProperties#PROP_ERRORS_DIR
	 */
	String getErrorDirectory();

	/**
	 * Call to generate an error file as the result of incompatibilities in the
	 * comparison of the expected results to the actual results.
	 * @param testResult 
	 * @param resultSet 
	 * @param results 
	 * @return String name for the error file
	 * @throws QueryTestFailedException could be seen if problems occur accessing resultSet
	 * @throws FrameworkException could be seen if problems occur creating error file
	 */
	
	String generateErrorFile(final TestResult testResult,
			final ResultSet resultSet, 
			final Object results) throws QueryTestFailedException, FrameworkException;
	
	/**
	 * Call to generate an error file for a query that failed comparison. File should
	 * have the SQL, the actual results returned from the server and the results
	 * that were expected.
	 * @param querySetID 
	 * @param queryID
	 * @param sql
	 * @param resultSet 
	 * @param queryError 
	 * @param expectedResultsFile 
	 * @return String name for the error file
	 * @throws QueryTestFailedException could be seen if problems occur accessing resultSet
	 * @throws FrameworkException could be seen if problems occur creating error file
	 */
	String generateErrorFile(final String querySetID,
			final String queryID, final String sql, final ResultSet resultSet,
			final Throwable queryError, final Object expectedResultsFile)
			throws QueryTestFailedException, FrameworkException;

	
	/**
	 * Call to generate an error file based on an error that occurred.  
	 * @param querySetID 
	 * @param queryID
	 * @param error
	 * @return String name for the error file
	 * @throws FrameworkException could be seen if problems occur creating error file
	 */
	String generateErrorFile(final String querySetID,
			final String queryID, final Throwable error)
			throws FrameworkException;
	
}
