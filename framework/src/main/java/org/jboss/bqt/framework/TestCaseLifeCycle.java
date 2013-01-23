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

/**
 * The TestCaseLifeCycle interface represents the life cycle of executing
 * a test case from which it will be called by the implementing test case.. 
 * <br>
 * The types of transactions that will be calling the test case 
 * will correspond to {@link TransactionFactory#TRANSACTION_TYPE TransactionType}.
 * <br>
 * QueryTest lifecycle:</br>
 * 
 * <br>
 * There are 3 phases or groupings of methods: <li>Setup</li> <li>Execution</li> <li>Cleanup</li>
 * 
 * <br>
 * <p>
 * <b>1. Setup phase</b>
 * <br>
 * @{link {@link #setup(TransactionAPI)} is about setting the global environment for the testing.
 * This is meant to be called once for one test case.
 * <br>
 * <p>
 * <b>2. Execution phase {@link #runTestCase()} is used for running a test.</b> 
 * <br>
 * <p>
 * <b>3. Cleanup</b> <br>
 * <br>
 * 
 * {@link #cleanup()} Called to allow the test case to perform any cleanup after
 * execution.
 * 
 * <br>
 * 
 * @author vanhalbert
 * 
 */
public interface TestCaseLifeCycle {
	
	/**
	 * Returns the name of the test so that better tracing of what tests are
	 * running/completing.
	 * 
	 * @return String is test name
	 */
	String getTestName();

	/**
	 * Called to setup any global environment
	 * @param transaction 
	 * @throws FrameworkException 
	 * 
	 * 
	 * @since
	 */
	void setup(TransactionAPI transaction) throws FrameworkException;

	/**
	 * Implement runTestCase(), it is the method that executes the test.
	 * 
	 * @since
	 */
	void runTestCase();

	/**
	 * Called at the end of the test so that the testcase can clean itself up by
	 * releasing any resources, closing any open connections, etc.
	 * 
	 * 
	 * @since
	 */
	public abstract void cleanup();
	
	
	

}