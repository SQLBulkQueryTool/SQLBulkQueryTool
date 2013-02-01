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

/**
 * The TestCase is the container for one logical test that is being executed.
 * <br>
 * It contains the {@link ActualTest actualTest} that is targeted to be performed and
 * the {@link ExpectedTestResults expectedResults} that the actual test should produce
 * when it is executed.  The {@link TestResult testResult} will contain the results
 * of the test execution, that can be used to compare against the  expected results
 * for success or failure.
 * 
 * @author vhalbert
 *
 */
public class TestCase {
	
	private ActualTest at = null;
	private TestResult tr = null;
	
	public TestCase(ActualTest test) {
		this.at = test;
	}
	
	/**
	 * Return the actual test to be performed in the testcase
	 * @return ActualTest
	 */
	public ActualTest getActualTest() {
		return this.at;
	}

	
//	/**
//	 * Return the expected results when the actual test is performed.
//	 * @return ExpectedResults
//	 */
//	public ExpectedTestResults getExpectedResults() {
//		return this.er;
//	}
	
	/**
	 * Return the test result for the performed actual test.
	 * @return TestResult;
	 */
	public TestResult getTestResult() {
		return this.tr;
	}
	
//	public void setExpectedResults(ExpectedTestResults expectedResults) {
//		this.er = expectedResults;
//	}
	
	public void setTestResult(TestResult testResult) {
		this.tr = testResult;
	}
}
