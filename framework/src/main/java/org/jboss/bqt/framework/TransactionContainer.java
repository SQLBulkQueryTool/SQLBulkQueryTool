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

import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.QueryTestFailedException;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * The TransactionContainer represents the controller for the transaction boundaries.  
 * This will enable different ways of controlling transaction behavior for testing
 * local transactions, xa transactions, and auto transactions, for example.
 * </p>
 * <p>
 * See {@link TransactionFactory.TRANSACTION_TYPES} for the types of transactions 
 * that are supported.  The {@link TransactionFactory#TRANSACTION_TYPE} property will
 * need to be set to control which transaction type to use.
 * </p>
 * <p>
 * The instance will be called {@link #before(TransactionQueryTestCase) before} the
 * transaction, so that any initialization can be done (i.e., set autocommit(..)) and then
 * called {@link #after(TransactionQueryTestCase) after} the execution of the 
 * {@link #runTest(TransactionQueryTestCase) logical} transaction is performed.
 * 
 * </p>
 */
public abstract class TransactionContainer {

	private String testClassName = null;

	protected TransactionContainer() {

	}

	protected void before(TransactionQueryTestCase test) {
	}

	protected void after(TransactionQueryTestCase test) {
	}

	public void runTransaction(TransactionQueryTestCase test)  throws FrameworkException, QueryTestFailedException  {

		this.testClassName = StringUtils.substringAfterLast(test.getClass().getName(),
				".");

		try {
			debug("Start transaction test: " + test.getTestName());

			test.setup();

			runTest(test);

			debug("Completed transaction test: " + test.getTestName());

		} finally {
			debug("	test.cleanup");

			test.cleanup();

		}

	}

	protected void runTest(TransactionQueryTestCase test) throws FrameworkException {
		debug("Start runTest: " + test.getTestName());

		debug("	before(test)");
		
		before(test);

		debug("	test.before");

		test.before();
		// run the test
		debug("	test.testcase");

		test.testCase();

		debug("	test.after");

		test.after();
		debug("	after(test)");

		after(test);

		debug("End runTest: " + test.getTestName());

	}

	protected void debug(String message) {
		FrameworkPlugin.LOGGER.debug("[" + this.testClassName + "] " + message);
	}

	protected void detail(String message) {
		FrameworkPlugin.LOGGER.info("[" + this.testClassName + "] " + message);
	}


}
