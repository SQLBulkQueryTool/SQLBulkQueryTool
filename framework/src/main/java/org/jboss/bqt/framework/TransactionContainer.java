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

import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.core.util.StringUtil;

public abstract class TransactionContainer {

	private String testClassName = null;

	protected TransactionContainer() {

	}

	protected void before(TransactionQueryTestCase test) {
	}

	protected void after(TransactionQueryTestCase test) {
	}

	public void runTransaction(TransactionQueryTestCase test) {

		this.testClassName = StringUtil.getLastToken(test.getClass().getName(),
				".");

		try {
			debug("Start transaction test: " + test.getTestName());

			try {

				test.setup();

			} catch (TransactionRuntimeException tre) {
				if (!test.exceptionExpected()) {
					tre.printStackTrace();
				}
				throw tre;
			} catch (Throwable e) {
				if (!test.exceptionExpected()) {
					e.printStackTrace();
				}
				throw new TransactionRuntimeException(e.getMessage());
			}

			runTest(test);

			debug("Completed transaction test: " + test.getTestName());

		} finally {
			debug("	test.cleanup");

			test.cleanup();

		}

	}

	protected void runTest(TransactionQueryTestCase test) {
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

//	protected boolean done() {
//		return true;
//	}

}
