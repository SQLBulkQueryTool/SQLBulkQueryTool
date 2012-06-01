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
import java.sql.Statement;
import java.util.Properties;

import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.framework.connection.ConnectionStrategy;

/**
 * The TransactionQueryTest interface represents the transaction test lifecycle
 * of execution from which the @link TransactionContainer operates. <br>
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
 * <li>{@link #setConnectionStrategy(ConnectionStrategy)} - called first to
 * provide the environment (i.e, type of connection, parameters, etc) that the
 * test will be run under.
 * <li>{@link #hasRequiredDataSources()} - called after the connection strategy
 * is set so the determination can be made if this test has the required
 * datasources defined and available in order to run the test. If not, then the
 * test is bypassed.
 * <li>{@link #setup()} - called to enable the test to perform any pretest
 * (i.e., global) setup. Example would be data source setup.
 * <li>{@link #setConnection(Connection)} - called to set the client driver
 * (i.e., Teiid) connection that will be used to execute queries against
 * <li>{@link #setExecutionProperties(Properties)} - called at this time so that
 * the overriding class can obtain / initialize settings that will be assigned
 * when
 * <li>{@link AbstractQueryTest#assignExecutionProperties(Statement)} is called
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
 * The following methods were exposed from {@link AbstractQueryTest}:
 * 
 * <li>{@link #exceptionExpected()} - when an exception is expected to occur,
 * the underlying logic will treat the execution as if it succeeded.</li>
 * <li>{@link #exceptionOccurred()} - this method indicates when an exception
 * actually occurred outside of the normal expected results.</li>
 * <li>{@link #getConnection()} and {@link #getXAConnection()} - these
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

public interface TransactionQueryTestCase {

	/**
	 * Returns the name of the test so that better tracing of what tests are
	 * running/completing.
	 * 
	 * @return String is test name
	 */
	String getTestName();

	/**
	 * Called to get the current connection strategy being used.
	 * 
	 * @return connStrategy
	 * 
	 * @since
	 */
	ConnectionStrategy getConnectionStrategy();

	/**
	 * Called by the {@link TransactionContainer} prior to testcase processing
	 * so that the responsibility for performing an setup duties (ie..,
	 * datasource setup) can be done
	 * 
	 * 
	 * @since
	 */
	void setup() throws QueryTestFailedException;

	/**
	 * Called by the @link TransactionContainer to set the Teiid connection to
	 * be used in the test.
	 * 
	 * @param conn
	 * 
	 * @since
	 */
	void setConnection(Connection conn);

	/**
	 * Override <code>before</code> if there is behavior that needs to be
	 * performed prior to {@link #testCase()} being called.
	 * 
	 * 
	 * @since
	 */
	void before();

	/**
	 * Implement testCase(), it is the entry point to the execution of the test.
	 * If an exception occurs, other than {@link FrameworkRuntimeException}, then that
	 * exception can be found by calling {@link #getApplicationException()}.
	 * 
	 * @throws FrameworkRuntimeException when a problem occurs that the whole process needs stopping.
	 * @throws  
	 * 
	 * @since
	 */
	void testCase() throws FrameworkRuntimeException;

	/**
	 * Override <code>after</code> if there is behavior that needs to be
	 * performed after {@link #testCase()} being called.
	 * 
	 * 
	 * @since
	 */
	void after();

	/**
	 * Indicates what should be done when a failure occurs in
	 * {@link #testCase()}
	 * 
	 * @return
	 * 
	 * @since
	 */
	boolean rollbackAllways();

	/**
	 * Called at the end of the test so that the testcase can clean itself up by
	 * releasing any resources, closing any open connections, etc.
	 * 
	 * 
	 * @since
	 */
	void cleanup();

	/**
	 * Returns the connection being used in the test.
	 * 
	 * @return
	 * 
	 * @since
	 */
	Connection getConnection();

//	XAConnection getXAConnection();

	/**
	 * Will indicate if the this specific test expects an exception to occur.
	 */
	boolean exceptionExpected();

	/**
	 * Will indicate if an exception actually occurred.
	 * @return
	 */
	boolean exceptionOccurred();

	/**
	 * Called by the testing process to capture the exception to be exposed
	 * when {@link #getApplicationException()} is called.
	 * @param t
	 */
	void setApplicationException(Throwable t);

	/**
	 * Called to obtain the exception, if it occurred, to be indicated in the
	 * reporting of the results of the test.
	 * @return Throwable exception if the test failed when executing
	 */
	Throwable getApplicationException();

}
