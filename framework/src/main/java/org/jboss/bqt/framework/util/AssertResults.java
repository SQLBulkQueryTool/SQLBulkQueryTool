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
package org.jboss.bqt.framework.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.core.util.ArgCheck;
import org.jboss.bqt.core.util.StringHelper;
import org.jboss.bqt.framework.FrameworkPlugin;
import org.jboss.bqt.framework.TestResult;
import org.jboss.bqt.framework.resultsreaders.MetadataReader;
import org.jboss.bqt.framework.resultsreaders.ResultSetReader;
import org.jboss.bqt.framework.resultsreaders.StringArrayReader;

/**
 * @author vhalbert
 *
 */
public class AssertResults {
	public static final String DELIMITER = "    "; //$NON-NLS-1$ 



	public static void assertResultsSetEquals(ResultSet resultSet, String expected, boolean compareCaseSensitivity) {
		ArgCheck.isNotNull(resultSet, "Unable to compare ResultSet to expected results, ResultSet is null");
		assertReaderEquals(new ResultSetReader(resultSet, DELIMITER),
				new StringReader(expected),compareCaseSensitivity);
	}

	public static void assertResultsSetEquals(ResultSet resultSet, String[] expected, boolean compareCaseSensitivity) {
		ArgCheck.isNotNull(resultSet, "Unable to compare ResultSet to expected results, ResultSet is null");
		assertReaderEquals(new ResultSetReader(resultSet, DELIMITER),
				new StringArrayReader(expected), compareCaseSensitivity);
	}

	public static void assertReaderEquals(Reader expected, Reader reader, boolean compareCaseSensitivity) {
		BufferedReader resultReader = new BufferedReader(expected);
		BufferedReader expectedReader = new BufferedReader(reader);
		try {
			compareResults(resultReader, expectedReader, compareCaseSensitivity );
		} catch (Exception e) {
			throw new TransactionRuntimeException(e);
		} finally {
			try {
				resultReader.close();
				expectedReader.close();
			} catch (IOException e) {
				throw new TransactionRuntimeException(e);
			}
		}
	}

	public static void assertResultsSetMetadataEquals(ResultSetMetaData metadata,
			File expected, boolean compareCaseSensitivity) {
		ArgCheck.isNotNull(metadata, "Unable to compare ResultSetMetaData to expected results, ResultSetMetaData is null");
		try {
			FrameworkUtil.writeResultSet(expected, new BufferedReader(new MetadataReader(
					metadata, DELIMITER)), false);
			assertReaderEquals(new MetadataReader(metadata, DELIMITER),
					new FileReader(expected), compareCaseSensitivity);
		} catch (IOException e) {
			throw new TransactionRuntimeException(e);
		}
	}

	public static void assertResultsSetMetadataEquals(ResultSetMetaData metadata,
			String[] expected, boolean compareCaseSensitivity) {
		ArgCheck.isNotNull(metadata, "Unable to compare ResultSetMetaData to expected results, ResultSetMetaData is null");
		assertReaderEquals(new MetadataReader(metadata, DELIMITER),
				new StringArrayReader(expected), compareCaseSensitivity);
	}
	
	public static void assertResultsSetEquals(ResultSet resultSet, File expected, boolean compareCaseSensitivity) throws QueryTestFailedException {
		ArgCheck.isNotNull(resultSet, "Unable to compare ResultSet to expected file results, ResultSet is null");
		try {
			FrameworkUtil.writeResultSet(expected, new BufferedReader(new ResultSetReader(
					resultSet, DELIMITER)), false);
			if (resultSet.getType() != ResultSet.TYPE_FORWARD_ONLY) {
				resultSet.beforeFirst();
			}
			assertReaderEquals(new ResultSetReader(resultSet, DELIMITER),
					new FileReader(expected), compareCaseSensitivity);
		} catch (IOException e) {
			throw new TransactionRuntimeException(e);
		} catch (SQLException e) {
			throw new QueryTestFailedException(e);
		}
	}

	protected static void compareResults(BufferedReader resultReader,
			BufferedReader expectedReader, boolean compareCaseSensitivity) throws IOException {
		ArgCheck.isTrue (StringHelper.isEqual(
				FrameworkUtil.read(expectedReader, compareCaseSensitivity),
				FrameworkUtil.read(resultReader, compareCaseSensitivity)
				                                )
			                );
		
	}
	
	
	
	public static void assertUpdateCount(TestResult testResult, int expected) throws QueryTestFailedException {
		if (testResult != null && expected != testResult.getUpdateCount()) {
			throw new QueryTestFailedException(FrameworkPlugin.Util.getString("AbstractQueryTest.updateCountNotCorrect", new Object[] { String.valueOf(expected), String.valueOf( testResult.getUpdateCount()) }));
		}
	}

	public static void assertRowCount(TestResult testResult, long expected) throws QueryTestFailedException {
		long count = testResult.getRowCount();
		if (expected != count) {
			throw new QueryTestFailedException(FrameworkPlugin.Util.getString("AbstractQueryTest.rowCountNotCorrect",  new Object[] { String.valueOf(expected), String.valueOf( count) }));
		}
	}	

}
