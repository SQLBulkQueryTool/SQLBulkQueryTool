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


import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.StringUtils;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.core.util.ArgCheck;
import org.jboss.bqt.framework.connection.ConnectionStrategy;
import org.jboss.bqt.framework.connection.ConnectionStrategyFactory;
import org.jboss.bqt.framework.util.AssertResults;
import org.jboss.bqt.framework.util.PrintResults;


/**
 * This class the base class that provides the core logic around using
 * the jdbc connection and its results.
 * 
 * The connection is not closed by this class, only lent to this class for
 * query processing.
 */
public abstract class AbstractQuery implements TransactionAPI {

	// NOTE not all tests will pass with this set to true, only those with
	// scrollable resultsets
	static boolean WRITE_ACTUAL = false;
	
	protected ConnectionStrategy connStrategy;


	protected Connection internalConnection = null;
	protected ResultSet internalResultSet = null;
	protected Statement internalStatement = null;
	private SQLException internalException = null;
	private Throwable applicationException = null;
	
	
	private String testClassName = "n/a";
	
	private TestCase testCase = null;

	public AbstractQuery() {
		
		this.testClassName = StringUtils.substringAfterLast(this.getClass().getName(),
		".");

		connStrategy = ConnectionStrategyFactory
					.createConnectionStrategy();

	}
	
	/*************************  
	 * LifeCycle Methods
	 *************************/

	/**
	 * @param testCase 
	 * 
	 */
	public void before(TestCase testCase) {
		this.testCase = testCase;
		
		this.applicationException = null;
		this.internalException = null;
		
		try {
			this.setConnection(this.connStrategy.getConnection());
		} catch (FrameworkException e) {
			throw new FrameworkRuntimeException(e.getMessage());
		}

	}
	
	public void after() {
		
		testCase.getTestResult().setException(this.getException());
		if (testCase.getTestResult().getException()!= null) {
			this.testCase.getTestResult().setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);
		}
	}

	

	/**
	 * Call after each query has completed and fully processed. Meaning all the
	 * rows have been read, that need to be, and any commit or rollback has been
	 * performed.
	 */
	public void cleanup()  {
		closeStatement();
		// don't clear connection, it will be reused,
		// just clear it out
		this.internalConnection = null;
		this.internalException = null;
		this.applicationException = null;
	}
	
	public ConnectionStrategy getConnectionStrategy() {
		return this.connStrategy;
	}
	
	/* ********** End Of LifeCycle Methods  ************ */

	public void setConnection(Connection c) {
		this.internalConnection = c;
	}

	public Connection getConnection() {
		return this.internalConnection;
	}
	
	public Statement getStatement() {
		return this.internalStatement;
	}
	
	public TestCase getTestCase() {
		return this.testCase;
	}
	
	public ResultSet getResultSet() {
		return this.internalResultSet;
	}
	
		
	/**
	 * @param sql 
	 * @return boolean
	 * @throws QueryTestFailedException 
	 * 
	 */
	public boolean execute(String sql) throws QueryTestFailedException {
		return execute(sql, new Object[] {});
	}

	public boolean execute(String sql, Object[] params) throws QueryTestFailedException{
		return execute(sql, params, null);
		
	}
	
	public boolean execute(String sql, Object[] params, Serializable payload) throws QueryTestFailedException {
		closeStatement();
		long endTS = 0;
		long beginTS = 0;

		boolean result = false;

		try {
			ArgCheck.isNotNull(this.internalConnection, "Unable to execute, connection is null");
			ArgCheck.isTrue(!this.internalConnection.isClosed(), "Connection is closed");
			
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
//				this.setPayload(this.internalStatement, payload);

				beginTS = System.currentTimeMillis();

				result = this.internalStatement.execute(sql);
				
				endTS = System.currentTimeMillis();
								
			}

			if (result) {
				this.internalResultSet = this.internalStatement.getResultSet();
				
			} else {
				
				this.testCase.getTestResult().setRowCount(0);
				this.testCase.getTestResult().setUpdateCount( this.internalStatement.getUpdateCount() );			
			}

			
		} catch (SQLException e) {
			endTS = -1;
			beginTS = -1;

			this.internalException = e;
			throw new QueryTestFailedException(e);
		}
		this.testCase.getTestResult().setBeginTS(beginTS);
		this.testCase.getTestResult().setEndTS(endTS);
		return result;
	}
	
	protected Statement createPrepareCallStatement(String sql)
			throws SQLException {
		return this.internalConnection.prepareCall("{?=call " + sql + "}"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected Statement createPrepareStatement(String sql) throws SQLException {
		return this.internalConnection.prepareStatement(sql);
	}

//	protected Statement createStatement() throws SQLException {
//		return this.internalConnection.createStatement();
//	}
//	

//	@Override
	protected Statement createStatement() throws SQLException {
		return this.internalConnection.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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

	public int[] executeBatch(String[] sql) throws QueryTestFailedException {
		return executeBatch(sql, -1);
	}

	public int[] executeBatch(String[] sql, int timeout) throws QueryTestFailedException {
		closeStatement();

		try {
			ArgCheck.isNotNull(this.internalConnection, "Unable to execute batch, connection is null");
			ArgCheck.isTrue(!this.internalConnection.isClosed());

			for (int i = 0; i < sql.length; i++) {
				if (sql[i].indexOf("?") != -1) { //$NON-NLS-1$
					throw new QueryTestFailedException(FrameworkPlugin.Util.getString("AbstractQueryTest.invalidPreparedStatementInBatch")); 
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
//			if (!exceptionExpected()) {
				throw new QueryTestFailedException(e);
//			}
		}

//		return null;

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
	
	/**
	 * Called by the testing process to capture the exception to be exposed
	 * when {@link #getException()} is called.
	 * @param t
	 */
	public void setApplicationException(Throwable t) {
		// keep the first one set, which is most like the exception closest
		// to the problem that we need to know about
		if (this.applicationException == null) this.applicationException = t;
	}

	private Throwable getException() {
		if (this.internalException != null) return this.internalException;
		
		if (this.applicationException != null) {
			if (this.applicationException instanceof SQLException) {
				return this.applicationException;
			}

			SQLException mm = new SQLException(
					this.applicationException.getMessage());
			return mm;

		}

		return null;
	}
	
	public void assertResultsSetEquals(Object expected) throws QueryTestFailedException {
		if (expected instanceof File) {
			AssertResults.assertResultsSetEquals(this.internalResultSet, (File) expected, this.compareCaseSensitive());
		} else 
		if (expected instanceof String) {
			AssertResults.assertResultsSetEquals(this.internalResultSet, (String) expected, this.compareCaseSensitive());
		} else {	
			AssertResults.assertResultsSetEquals(this.internalResultSet, (String[]) expected, this.compareCaseSensitive());
		}		
	}



	protected boolean compareCaseSensitive() {
		return true;
	}

	public void printResults() {
		PrintResults.printResults(this.internalResultSet);
	}
	
	public  void printResults(boolean comparePrint) {
		ArgCheck.isNotNull(this.internalResultSet, "Unable to printResults, ResultSet it null");
		PrintResults.printResults(this.internalResultSet, comparePrint);
	}	
	


	public void walkResults() throws QueryTestFailedException {
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
			throw new QueryTestFailedException(e);
		}
	}

	public int getRowCount() throws QueryTestFailedException {
		if (this.internalResultSet == null) return 0;
		// Count all
		try {
			int count = 0;
			while (this.internalResultSet.next()) {
				count++;
			}
			return count;
		} catch (SQLException e) {
			throw new QueryTestFailedException(e);
		}
	}

	public void cancelQuery() throws SQLException {
		ArgCheck.isNotNull(this.internalConnection, "Unable to cancel query, result set is null");
		ArgCheck.isTrue(!this.internalConnection.isClosed());
		ArgCheck.isNotNull(this.internalStatement, "Unable to close statement, its null");
		this.internalStatement.cancel();
	}


	protected void executeAndAssertResults(String query, String[] expected) throws QueryTestFailedException {
		execute(query);
		if (expected != null) {
			assertResultsSetEquals(expected);
		} else {
			printResults(true);
		}
	}
	

	private void closeStatement() {
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

	private void closeResultSet() {

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
	
	protected void debug(String message) {
		FrameworkPlugin.LOGGER.debug("[" + this.testClassName + "] " + message);
	}

	protected void detail(String message) {
		FrameworkPlugin.LOGGER.info("[" + this.testClassName + "] " + message);
	}

	
	
}