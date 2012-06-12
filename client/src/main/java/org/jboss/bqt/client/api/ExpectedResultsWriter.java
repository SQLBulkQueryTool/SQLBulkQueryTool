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
import org.jboss.bqt.core.exception.FrameworkRuntimeException;

/**
 * The ExpectedResultsWriter represents how a new set of expected results will be written for
 * a given <code>querySetID</code> and <code>queryIdentifier</code>. The
 * implementor should write out one result file for each call that is made to
 * {@link #generateQueryResultFile(TestResult, ResultSet)  }
 * , however, it will control the format of the content.  The location should be
 * based on {@link TestProperties#PROP_GENERATE_DIR generateDirectory}.
 * 
 * The testing process will only generate a new result file when the result mode
 * is {@link TestProperties.RESULT_MODES#GENERATE}.  
 * 
 */
public interface ExpectedResultsWriter {

	/**
	 * Returns the full path to the newly created expected results location.
	 * @return String full directory path
	 * 
	 * @see TestProperties#PROP_GENERATE_DIR
	 */
	String getGenerateDir();

	/**
	 * Call to generate the results file from an executed query. If an exception
	 * occurred, it is considered the result from the query. The file created
	 * based on the result should be able to be used as the expected result when
	 * query tests are run with in the resultmode of "compare".
	 * 
	 * @param testResult 
	 * @param resultSet 
	 * @throws FrameworkRuntimeException is thrown to stop processing
	 */
	
	void generateQueryResultFile(final TestResult testResult,
			final ResultSet resultSet) throws FrameworkRuntimeException;	
	
	

}
