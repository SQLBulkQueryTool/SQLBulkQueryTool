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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.PropertiesUtils;
import org.jboss.bqt.framework.ConfigPropertyNames.CONNECTION_STRATEGY_PROPS;
import org.jboss.bqt.framework.connection.ConnectionStrategy;
import org.jboss.bqt.framework.connection.ConnectionStrategyFactory;

/**
 * The AbstractQueryTransactionTest is the base implementation for the
 * {@link TransactionQueryTestCase}. This provides the default logic for perform
 * a testcase. The only method to implement in order to perform a basic, single
 * datasource, test is the {@link #testCase()} method.
 * 
 * AbstractQueryTransactionTest is the class that should be extended when a
 * testcase is being created to validate certain behavior
 * 
 * <br>
 * 
 */
public abstract class AbstractQueryTransactionTest extends
		AbstractQueryTest implements TransactionQueryTestCase {

	protected String testname = "NA";
	protected int fetchSize = -1;
	protected int queryTimeout = -1;

	protected ConnectionStrategy connStrategy;

	// because only a SQLException is accounted for in AbstractQueryTest,
	// the applicationException is used to when unaccounted for exceptions
	// occur. This could
	// unintentional errors from the driver or ctc client test code.
	private Throwable applicationException = null;

	public AbstractQueryTransactionTest() {
		super();

		this.connStrategy = ConnectionStrategyFactory
				.createConnectionStrategy();
	}

	public AbstractQueryTransactionTest(String testname) {
		this();
		this.testname = testname;
	}

	public String getTestName() {
		return this.testname;

	}

	public ConnectionStrategy getConnectionStrategy() {
		// TODO Auto-generated method stub
		return this.connStrategy;
	}

	protected void assignExecutionProperties(Statement stmt) {
		// if (stmt instanceof org.jboss.bqt.jdbc.TeiidStatement) {
		// org.jboss.bqt.jdbc.TeiidStatement statement =
		// (org.jboss.bqt.jdbc.TeiidStatement) stmt;

		Properties executionProperties = this.connStrategy.getEnvironment();
		if (executionProperties != null) {
			String txnautowrap = executionProperties
					.getProperty(CONNECTION_STRATEGY_PROPS.TXN_AUTO_WRAP);
			if (txnautowrap != null) {
				Properties props = new Properties();
				props.setProperty("ExecutionProperty", txnautowrap);

				PropertiesUtils.setBeanProperties(stmt, props, null);
			}

			// statement.setExecutionProperty(
			// CONNECTION_STRATEGY_PROPS.TXN_AUTO_WRAP,
			// txnautowrap);
			// }

			String fetchSizeStr = executionProperties
					.getProperty(CONNECTION_STRATEGY_PROPS.FETCH_SIZE);
			if (fetchSizeStr != null) {
				try {
					fetchSize = Integer.parseInt(fetchSizeStr);

					FrameworkPlugin.LOGGER.info("FetchSize = " + fetchSize, null);
				} catch (NumberFormatException e) {
					fetchSize = -1;
					// this.print("Invalid fetch size value: " + fetchSizeStr
					// + ", ignoring");
				}
			}

		}

		if (this.fetchSize > 0) {
			try {
				stmt.setFetchSize(this.fetchSize);
			} catch (SQLException e) {
				FrameworkPlugin.LOGGER.info(e.getMessage(), null);
			}
		}

		if (this.queryTimeout > 0) {
			try {
				stmt.setQueryTimeout(this.queryTimeout);
			} catch (SQLException e) {
				FrameworkPlugin.LOGGER.info(e.getMessage(), null);
			}
		}

	}

	/**
	 * Override <code>setupDataSource</code> if there is different mechanism for
	 * setting up the datasources for the testcase
	 * 
	 * @throws QueryTestFailedException
	 * @throws QueryTestFailedException
	 * 
	 * @since
	 */
	public void setup() throws QueryTestFailedException {

		this.applicationException = null;
		this.setConnection(connStrategy.getConnection());


	}

	/**
	 * The source connection must be asked from the connection strategy because
	 * only here is it known which model was mapped to which datasource. This is
	 * because each test could potentially use an include/exclude datasource
	 * option that could change the mappings between tests.
	 * 
	 * @param identifier
	 * @return Connection
	 * @throws QueryTestFailedException
	 */
//	public Connection getSource(String identifier)
//			throws QueryTestFailedException {
//
//		Connection conn = this.connStrategy.createDriverConnection(identifier);
//		// force autocommit back to true, just in case the last user didnt
////		try {
////			conn.setAutoCommit(true);
////		} catch (Exception sqle) {
////			throw new QueryTestFailedException(sqle);
////		}
//
//		return conn;
//
//	}

//	public XAConnection getXASource(String identifier)
//			throws QueryTestFailedException {
//
//		return this.connStrategy.createDataSourceConnection(identifier);
//
//	}

	/**
	 * Indicates what should be done when a failure occurs in
	 * {@link #testCase()}
	 * 
	 * @return boolean
	 * 
	 * @since
	 */
	public boolean rollbackAllways() {
		return false;
	}

	/**
	 * Override <code>before</code> if there is behavior that needs to be
	 * performed prior to {@link #testCase()} being called.
	 * 
	 * 
	 * @since
	 */
	public void before() {
	}

	/**
	 * Override <code>after</code> if there is behavior that needs to be
	 * performed after {@link #testCase()} being called.
	 * 
	 * 
	 * @since
	 */
	public void after() {
		super.after();
	}

	/**
	 * At end of each test, perform any cleanup that your test requires. Note:
	 * Do not cleanup any connections by calling
	 * {@link ConnectionStrategy#shutdown()}. That is performed by the
	 * {@link TransactionContainer#runTransaction(TransactionQueryTestCase)} at
	 * the end of the test.
	 */
	public void cleanup() {
		

	}

//	@Override
//	public XAConnection getXAConnection() {
//		return null;
//
//	}

	public void setApplicationException(Throwable t) {
		this.applicationException = t;

	}

	public boolean exceptionOccurred() {
		return (super.exceptionOccurred() ? super.exceptionOccurred()
				: this.applicationException != null);

	}

	public SQLException getLastException() {
		if (super.getLastException() != null) {
			return super.getLastException();
		}
		if (this.applicationException != null) {
			if (this.applicationException instanceof SQLException) {
				return (SQLException) this.applicationException;
			}

			SQLException mm = new SQLException(
					this.applicationException.getMessage());
			return mm;

		}

		return null;
	}

	public Throwable getApplicationException() {
		// TODO Auto-generated method stub
		return this.applicationException;
	}

}
