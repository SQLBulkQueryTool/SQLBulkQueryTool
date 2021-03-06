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
package org.jboss.bqt.client.resultmode;

import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.api.ExpectedResultsWriter;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.api.QueryWriter;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.ArgCheck;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestResult;
import org.jboss.bqt.framework.TransactionAPI;

/**
 * The Generate Result Mode controls the process for generating the expected results files based on the
 * queries that are executed.
 * 
 * @author vhalbert
 *
 */
public class GenerateExpectedResults extends QueryScenario {

	/**
	 * @param scenarioName
	 * @param queryProperties
	 */
	public GenerateExpectedResults(String scenarioName, Properties queryProperties) {
		super(scenarioName, queryProperties);
	}
	
	
	@Override
	public boolean isGenerate() {
		return true;
	}
	
	@Override
	public String getResultsMode()
	{
		return TestProperties.RESULT_MODES.GENERATE;
	}
	
	@Override
	public synchronized QueryWriter getQueryWriter() {
		return null;
	}		


	@Override
	public void handleTestResult(TestCase testCase, TransactionAPI transaction) throws  FrameworkException {
		ArgCheck.isNotNull(testCase, "testCase must be passed in");
		
		
		List<ExpectedResultsWriter> resultsWriters = this.getExpectedResultsWriters();
		
		 for (ExpectedResultsWriter writer : resultsWriters) {
			 try {
				 writer.generateExpectedResultFile(testCase, transaction);
				} catch (FrameworkException fe) {					
					this.getErrorWriter().generateErrorFile(testCase.getTestResult(), fe);
				}

		 }
	
		// If there was an exeception in the test results, create the error file
//		if (testCase.getTestResult().getStatus() == TestResult.RESULT_STATE.TEST_EXCEPTION) {
//				this.getErrorWriter().generateErrorFile(testCase.getTestResult(), testCase.getTestResult().getException());
//				//(testCase, null, transaction);	
//		}
	}

}
