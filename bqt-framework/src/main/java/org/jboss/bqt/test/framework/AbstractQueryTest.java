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

package org.jboss.bqt.test.framework;


import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.core.util.ArgCheck;
//import org.teiid.jdbc.TeiidStatement;

/**
 * This class can be used as the base class to write Query tests for integration
 * testing. Just like the scripted one this one should provide all those
 * required flexibility in testing.
 * 
 * The connection is not closed by this class, only lent to this class for
 * query processing.
 */
public abstract class AbstractQueryTest {

	// NOTE not all tests will pass with this set to true, only those with
	// scrollable resultsets
	static boolean WRITE_ACTUAL = false;

	protected Connection internalConnection = null;
	protected ResultSet internalResultSet = null;
	protected Statement internalStatement = null;
	private SQLException internalException = null;
	protected int updateCount = -1;
	protected String DELIMITER = "    "; //$NON-NLS-1$ 
	
	protected long endTS = 0;
	protected long beginTS = 0;


	public AbstractQueryTest() {
		super();
	}

	public AbstractQueryTest(Connection conn) {
		super();
		this.internalConnection = conn;

	}

//	public void tearDown() throws Exception {
//		closeConnection();
//	}

	public void setConnection(Connection c) {
		this.internalConnection = c;
	}

	public Connection getConnection() {
		return this.internalConnection;
	}

	public boolean execute(String sql) {
		return execute(sql, new Object[] {});
	}

	public boolean execute(String sql, Object[] params) {
		return execute(sql, params, null);
		
	}
	
	public boolean execute(String sql, Object[] params, Serializable payload) {
		closeStatement();
		this.updateCount = -1;
		endTS = 0;
		beginTS = 0;

		try {
			ArgCheck.isNotNull(this.internalConnection, "Unable to execute, connection is null");
			ArgCheck.isTrue(!this.internalConnection.isClosed(), "Connection is closed");
			boolean result = false;
			if (params != null && params.length > 0) {
				if (sql.toLowerCase().startsWith("exec ")) { //$NON-NLS-1$
					sql = sql.substring(5);
					this.internalStatement = createPrepareCallStatement(sql);
				} else {
					this.internalStatement = createPrepareStatement(sql);
				}
				
				setParameters((PreparedStatement) this.internalStatement,
						params);
				assignExecutionProperties(this.internalStatement);
				this.setPayload(this.internalStatement, payload);
				
				beginTS = System.currentTimeMillis();

				result = ((PreparedStatement) this.internalStatement).execute();
				
				endTS = System.currentTimeMillis();

			} else {
				this.internalStatement = createStatement();
				assignExecutionProperties(this.internalStatement);
				this.setPayload(this.internalStatement, payload);

				beginTS = System.currentTimeMillis();

				result = this.internalStatement.execute(sql);
				
				endTS = System.currentTimeMillis();

			}
			
			if (result) {
				this.internalResultSet = this.internalStatement.getResultSet();
			} else {
				this.updateCount = this.internalStatement.getUpdateCount();
			}
			return result;
		} catch (SQLException e) {
			endTS = -1;
			beginTS = -1;

			this.internalException = e;
			if (!exceptionExpected()) {
				throw new TransactionRuntimeException(e);
			}
		}
		return false;
	}
	
	protected Statement createPrepareCallStatement(String sql)
			throws SQLException {
		return this.internalConnection.prepareCall("{?=call " + sql + "}"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected Statement createPrepareStatement(String sql) throws SQLException {
		return this.internalConnection.prepareStatement(sql);
	}

	protected Statement createStatement() throws SQLException {
		return this.internalConnection.createStatement();
	}

	private void setParameters(PreparedStatement stmt, Object[] params)
			throws SQLException {
		for (int i = 0; i < params.length; i++) {
			stmt.setObject(i + 1, params[i]);
		}
	}
	
	private void setPayload(Object stmt, Serializable payload) {
		if (payload == null) return;
		
//		if (stmt instanceof org.teiid.jdbc.TeiidStatement) {
//			 ((TeiidStatement) stmt).setPayload(payload);
//		} 
	}

	public int[] executeBatch(String[] sql) {
		return executeBatch(sql, -1);
	}

	public int[] executeBatch(String[] sql, int timeout) {
		closeStatement();

		try {
			ArgCheck.isNotNull(this.internalConnection, "Unable to execute batch, connection is null");
			ArgCheck.isTrue(!this.internalConnection.isClosed());

			for (int i = 0; i < sql.length; i++) {
				if (sql[i].indexOf("?") != -1) { //$NON-NLS-1$
					throw new TransactionRuntimeException(
							"no prepared statements allowed in the batch command"); //$NON-NLS-1$
				}
			}

			this.internalStatement = createStatement();
			assignExecutionProperties(this.internalStatement);

			if (timeout != -1) {
				this.internalStatement.setQueryTimeout(timeout);
			}
			for (int i = 0; i < sql.length; i++) {
				this.internalStatement.addBatch(sql[i]);
			}

			return this.internalStatement.executeBatch();

		} catch (SQLException e) {
			this.internalException = e;
			if (!exceptionExpected()) {
				throw new TransactionRuntimeException(e);
			}
		}

		return null;

	}

	/**
	 * Override when you need to set an execution property on the statement
	 * before execution.
	 * 
	 * <p>
	 * Example:
	 * <code>if (this.executionProperties.getProperty(ExecutionProperties.PROP_FETCH_SIZE) != null) {
	 *               statement.setExecutionProperty(ExecutionProperties.PROP_FETCH_SIZE, this.executionProperties.getProperty(ExecutionProperties.PROP_FETCH_SIZE));
	 *      }
	 * </code>
	 * </p>
	 * 
	 * @param stmt
	 * 
	 * @since
	 */

	protected void assignExecutionProperties(Statement stmt) {
	}

	public boolean exceptionOccurred() {
		return this.internalException != null;
	}

	public boolean exceptionExpected() {
		return false;
	}

	public SQLException getLastException() {
		return this.internalException;
	}

	public void assertResultsSetEquals(File expected) {
		assertResultsSetEquals(this.internalResultSet, expected);
	}

	public void assertResultsSetEquals(ResultSet resultSet, File expected) {
		ArgCheck.isNotNull(resultSet, "Unable to compare ResultSet to expected file results, ResultSet is null");
		try {
			writeResultSet(expected, new BufferedReader(new ResultSetReader(
					resultSet, DELIMITER)));
			if (resultSet.getType() != ResultSet.TYPE_FORWARD_ONLY) {
				resultSet.beforeFirst();
			}
			assertReaderEquals(new ResultSetReader(resultSet, DELIMITER),
					new FileReader(expected));
		} catch (IOException e) {
			throw new TransactionRuntimeException(e);
		} catch (SQLException e) {
			throw new TransactionRuntimeException(e);
		}
	}

	private void writeResultSet(File expected, BufferedReader resultReader)
			throws IOException {
		if (WRITE_ACTUAL) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(expected));
			String s = null;
			while ((s = resultReader.readLine()) != null) {
				bw.write(s);
				bw.write("\n"); //$NON-NLS-1$
			}
			bw.close();
		}
	}

	public void assertResultsSetEquals(String expected) {
		assertResultsSetEquals(this.internalResultSet, expected);
	}

	public void assertResultsSetEquals(ResultSet resultSet, String expected) {
		ArgCheck.isNotNull(resultSet, "Unable to compare ResultSet to expected results, ResultSet is null");
		assertReaderEquals(new ResultSetReader(resultSet, DELIMITER),
				new StringReader(expected));
	}

	public void assertResults(String[] expected) {
		assertResultsSetEquals(expected);
	}

	public void assertResultsSetEquals(String[] expected) {
		assertResultsSetEquals(this.internalResultSet, expected);
	}

	public void assertResultsSetEquals(ResultSet resultSet, String[] expected) {
		ArgCheck.isNotNull(resultSet, "Unable to compare ResultSet to expected results, ResultSet is null");
		assertReaderEquals(new ResultSetReader(resultSet, DELIMITER),
				new StringArrayReader(expected));
	}

	public void assertReaderEquals(Reader expected, Reader reader) {
		BufferedReader resultReader = new BufferedReader(expected);
		BufferedReader expectedReader = new BufferedReader(reader);
		try {
			compareResults(resultReader, expectedReader);
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

	public void assertResultsSetMetadataEquals(ResultSetMetaData metadata,
			File expected) {
		ArgCheck.isNotNull(metadata, "Unable to compare ResultSetMetaData to expected results, ResultSetMetaData is null");
		try {
			writeResultSet(expected, new BufferedReader(new MetadataReader(
					metadata, DELIMITER)));
			assertReaderEquals(new MetadataReader(metadata, DELIMITER),
					new FileReader(expected));
		} catch (IOException e) {
			throw new TransactionRuntimeException(e);
		}
	}

	public void assertResultsSetMetadataEquals(ResultSetMetaData metadata,
			String[] expected) {
		ArgCheck.isNotNull(metadata, "Unable to compare ResultSetMetaData to expected results, ResultSetMetaData is null");
		assertReaderEquals(new MetadataReader(metadata, DELIMITER),
				new StringArrayReader(expected));
	}

	protected static String read(BufferedReader r, boolean casesensitive)
			throws IOException {
		StringBuffer result = new StringBuffer();
		String s = null;
		try {
			while ((s = r.readLine()) != null) {
				result.append((casesensitive ? s.trim() : s.trim()
						.toLowerCase()));
				result.append("\n"); //$NON-NLS-1$
			}
		} finally {
			r.close();
		}
		return result.toString();
	}

	protected void compareResults(BufferedReader resultReader,
			BufferedReader expectedReader) throws IOException {
		assertEquals(read(expectedReader, compareCaseSensitive()),
				read(resultReader, compareCaseSensitive()));
	}

	protected boolean compareCaseSensitive() {
		return true;
	}

	public void printResults() {
		printResults(this.internalResultSet);
	}

	public void printResults(ResultSet results) {
		printResults(results, false);
	}

	public void printResults(boolean comparePrint) {
		ArgCheck.isNotNull(this.internalResultSet, "Unable to printResults, ResultSet it null");
		printResults(this.internalResultSet, comparePrint);
	}

	public void walkResults() {
		ArgCheck.isNotNull(this.internalResultSet, "Unable to walk results, ResultSet is null");

		try {
			int columnCount = this.internalResultSet.getMetaData()
					.getColumnCount();
			while (this.internalResultSet.next()) {
				for (int col = 1; col <= columnCount; col++) {
					this.internalResultSet.getObject(col);
				}
			}
			closeResultSet();
		} catch (SQLException e) {
			throw new TransactionRuntimeException(e);
		}
	}

	void printResults(ResultSet results, boolean comparePrint) {
		if (results == null) {
			System.out.println("ResultSet is null"); //$NON-NLS-1$
			return;
		}
		int row;
		try {
			row = -1;
			BufferedReader in = new BufferedReader(new ResultSetReader(results,
					DELIMITER));
			String line = in.readLine();
			while (line != null) {
				row++;
				if (comparePrint) {
					line = line.replaceAll("\"", "\\\\\""); //$NON-NLS-1$ //$NON-NLS-2$
					System.out.println("\"" + line + "\","); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					System.out.println(line);
				}
				line = in.readLine();
			}
			System.out.println("Fetched " + row + " rows\n"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			throw new TransactionRuntimeException(e);
		}
	}

	public void assertUpdateCount(int expected) {
		if (expected != updateCount) {
			throw new FrameworkRuntimeException("Expected row cnt " + expected + " does not match actual cnt " + updateCount);
		}
//		ArgCheck.equals(expected, updateCount, "Expected updated cnt " + expected + " does not match actual cnt " + updateCount);
	}

	public void assertRowCount(int expected) {
		int count = getRowCount();
		if (expected != count) {
			throw new FrameworkRuntimeException("Expected row cnt " + expected + " does not match actual cnt " + count);
		}
//		ArgCheck.equals(expected, count, "Expected row cnt " + expected + " does not match actual cnt " + count);
	}

	public int getRowCount() {
		if (this.internalResultSet == null) return 0;
		// Count all
		try {
			int count = 0;
			while (this.internalResultSet.next()) {
				count++;
			}
			return count;
		} catch (SQLException e) {
			throw new TransactionRuntimeException(e);
		}
	}

	public void closeStatement() {
		closeResultSet();

		if (this.internalStatement != null) {
			try {
				this.internalStatement.close();
			} catch (SQLException e) {
				throw new TransactionRuntimeException(e);
			} finally {
				this.internalStatement = null;
			}
		}
	}

	public void closeResultSet() {
		this.internalException = null;

		if (this.internalResultSet != null) {
			try {
				this.internalResultSet.close();
			} catch (SQLException e) {
				// ignore
			} finally {
				this.internalResultSet = null;
			}
		}
	}

	public void closeConnection() {
		closeStatement();
		try {
			if (this.internalConnection != null) {
				try {
					this.internalConnection.close();
				} catch (SQLException e) {
					throw new TransactionRuntimeException(e);
				}
			}
		} finally {
			this.internalConnection = null;
		}
	}

	public void cancelQuery() throws SQLException {
		ArgCheck.isNotNull(this.internalConnection, "Unable to cancel query, result set is null");
		ArgCheck.isTrue(!this.internalConnection.isClosed());
		ArgCheck.isNotNull(this.internalStatement, "Unable to close statement, its null");
		this.internalStatement.cancel();


	}

	public void print(String msg) {
		System.out.println(msg);
	}

	public void print(Throwable e) {
		e.printStackTrace();
	}

	protected void executeAndAssertResults(String query, String[] expected) {
		execute(query);
		if (expected != null) {
			assertResults(expected);
		} else {
			printResults(true);
		}
	}
	

	
	/**
	 * At end of each test, perform any post processing logic that your test requires.
	 */
	public void after() {
		closeStatement();

	}
	
}