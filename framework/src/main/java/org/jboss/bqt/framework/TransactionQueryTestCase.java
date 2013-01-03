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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.PropertiesUtils;
import org.jboss.bqt.framework.connection.ConnectionStrategy;
import org.jboss.bqt.framework.connection.ConnectionStrategyFactory;

/**
 * The TransactionQueryTest interface represents the transaction test lifecycle
 * of execution from which the @link TransactionContainer operates. 
 * <br>
 * The types of transactions that will be calling the testcase 
 * will correspond to {@link TransactionFactory#TRANSACTION_TYPE TransactionType}.
 * <br>
 * QueryTest lifecycle:</br>
 * 
 * <br>
 * There are 4 phases or groupings of methods: <li>Setup</li> <li>Test</li> <li>
 * Validation</li> <li>Cleanup</li>
 * 
 * <br>
 * <p>
 * <b>1. Setup phase is about setting the global environment for the testing</b>
 * <br>
 * 
 * <li>{@link #getConnectionStrategy()} - called first to
 * obtain the environment (i.e, type of connection, parameters, etc) that the
 * test will be run under.
 * <li>{@link #setup()} - called to enable the test to perform any pretest
 * (i.e., global) setup. Example would be data source setup.
 * <li>{@link #setConnection(Connection)} - called to set the client driver
 * (i.e., Teiid) connection that will be used to execute queries against
 * <li>{@link AbstractQuery#assignExecutionProperties(Statement)} is called
 * prior to sql execution. (Example: set fetch size, batch time, or timeout)</li>
 * <br>
 * <p>
 * <b>2. Test phase are the methods for executing a test, including any
 * before/after test logic to support the test</b> <br>
 * <br>
 * 
 * <li>{@link #before()} called before the execution of the test so that the
 * transaction boundary can be set and any other pretest conditions
 * <li>{@link #testCase()} called to execute the specific test
 * <li>{@link #after()} called after the test is executed, which will
 * commit/rollback the transaction and perform any other post conditions</li>
 * <br>
 * <p>
 * <b>3. Validation phase is meant to enable data validation post transaction
 * completion. This is especially helpful when performing XA transactions
 * because the results are not completed and available until after the
 * {@link #after()} step is performed.</b> <br>
 * <br>
 * 
 * <p>
 * <b>4. Cleanup</b> <br>
 * <br>
 * 
 * {@link #cleanup()} Called to allow the testcase to perform any cleanup after
 * execution.
 * 
 * <br>
 * ================
 * <p>
 * <b>Other Notes:</b> <br>
 * <br>
 * 
 * The following methods were exposed from {@link AbstractQuery}:
 * 
 * <li>{@link #exceptionExpected()} - when an exception is expected to occur,
 * the underlying logic will treat the execution as if it succeeded.</li>
 * <li>{@link #exceptionOccurred()} - this method indicates when an exception
 * actually occurred outside of the normal expected results.</li>
 * <li>{@link #getConnection()}  - these
 * connection methods are exposed for {@link #before()} and {@link #after()}
 * methods</li>
 * <li>{@link #rollbackAllways()} - this is exposed for the {@link #after()}
 * method as to what behavior is expected after the execution of the test</li>
 * 
 * 
 * <br>
 * 
 * @author vanhalbert
 * 
 */

public abstract class TransactionQueryTestCase extends AbstractQuery {
	
	protected String testname = "NA";
	protected int fetchSize = -1;
	protected int queryTimeout = -1;

	protected ConnectionStrategy connStrategy;

	// because only a SQLException is accounted for in AbstractQueryTest,
	// the applicationException is used to when unaccounted for exceptions
	// occur. This could
	// unintentional errors from the driver or ctc client test code.
	private Throwable applicationException = null;
	
	public TransactionQueryTestCase() {
		super();

		this.connStrategy = ConnectionStrategyFactory
				.createConnectionStrategy();
	}
	
	public TransactionQueryTestCase(String testname) {
		this();
		this.testname = testname;
	}

	/**
	 * Returns the name of the test so that better tracing of what tests are
	 * running/completing.
	 * 
	 * @return String is test name
	 */
	public String getTestName() {
		return this.testname;
	}
	/**
	 * Called to get the current connection strategy being used.
	 * 
	 * @return connStrategy
	 * 
	 * @since
	 */
	public ConnectionStrategy getConnectionStrategy() {
		return this.connStrategy;
	}
	
//	@Override
//	protected void assignExecutionProperties(Statement stmt) {
//		// if (stmt instanceof org.jboss.bqt.jdbc.TeiidStatement) {
//		// org.jboss.bqt.jdbc.TeiidStatement statement =
//		// (org.jboss.bqt.jdbc.TeiidStatement) stmt;
//
//		Properties executionProperties = this.connStrategy.getEnvironment();
//		if (executionProperties != null) {
//			
//			List foop = PropertiesUtils.filter("statement.*", executionProperties);
//			
//			String txnautowrap = executionProperties
//					.getProperty(CONNECTION_STRATEGY_PROPS.TXN_AUTO_WRAP);
//			if (txnautowrap != null) {
//				Properties props = new Properties();
//				props.setProperty("ExecutionProperty", txnautowrap);
//
//				PropertiesUtils.setBeanProperties(stmt, props, null);
//			}

			// statement.setExecutionProperty(
			// CONNECTION_STRATEGY_PROPS.TXN_AUTO_WRAP,
			// txnautowrap);
			// }

//			String fetchSizeStr = executionProperties
//					.getProperty(CONNECTION_STRATEGY_PROPS.FETCH_SIZE);
//			if (fetchSizeStr != null) {
//				try {
//					fetchSize = Integer.parseInt(fetchSizeStr);
//
//					FrameworkPlugin.LOGGER.info("FetchSize = " + fetchSize, null);
//				} catch (NumberFormatException e) {
//					fetchSize = -1;
//					// this.print("Invalid fetch size value: " + fetchSizeStr
//					// + ", ignoring");
//				}
//			}

//		}
//		
//
//		if (this.fetchSize > 0) {
//			try {
//				stmt.setFetchSize(this.fetchSize);
//			} catch (SQLException e) {
//				FrameworkPlugin.LOGGER.info(e.getMessage());
//			}
//		}
//
//		if (this.queryTimeout > 0) {
//			try {
//				stmt.setQueryTimeout(this.queryTimeout);
//			} catch (SQLException e) {
//				FrameworkPlugin.LOGGER.info(e.getMessage());
//			}
//		}
//
//	}
	/**
	 * Called by the {@link TransactionContainer} prior to testcase processing
	 * so that the responsibility for performing an setup duties (ie..,
	 * datasource setup) can be done
	 * @throws QueryTestFailedException 
	 * 
	 * 
	 * @since
	 */
	public void setup() throws QueryTestFailedException {

		this.applicationException = null;
		this.setConnection(connStrategy.getConnection());


	}

	/**
	 * Override <code>before</code> if there is behavior that needs to be
	 * performed prior to {@link #testCase()} being called.
	 * @throws FrameworkException 
	 * 
	 * @since
	 */
	public void before()  throws FrameworkException {
	}

	/**
	 * Implement testCase(), it is the entry point to the execution of the test.
	 * If an exception occurs, other than {@link FrameworkRuntimeException}, then that
	 * exception can be found by calling {@link #getApplicationException()}.
	 * 
	 * @since
	 */
	public abstract void testCase();

	/**
	 * Override <code>after</code> if there is behavior that needs to be
	 * performed after {@link #testCase()} being called.
	 * 
	 * 
	 * @since
	 */
	public void after()  {
		super.closeStatement();
	}

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
	 * Called at the end of the test so that the testcase can clean itself up by
	 * releasing any resources, closing any open connections, etc.
	 * 
	 * 
	 * @since
	 */
	public void cleanup() {
	}

	/**
	 * Will indicate if an exception actually occurred.
	 * @return boolean
	 */
	@Override
	public boolean exceptionOccurred() {
		return (super.exceptionOccurred() ? super.exceptionOccurred()
				: this.applicationException != null);
	}
	
	@Override
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

	/**
	 * Called by the testing process to capture the exception to be exposed
	 * when {@link #getApplicationException()} is called.
	 * @param t
	 */
	public void setApplicationException(Throwable t) {
		this.applicationException = t;
	}
	/**
	 * Called to obtain the exception, if it occurred, to be indicated in the
	 * reporting of the results of the test.
	 * @return Throwable exception if the test failed when executing
	 */
	public Throwable getApplicationException() {
		return this.applicationException;
	}


}
