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
package org.jboss.bqt.client.impl;

import java.sql.ResultSet;
import java.util.Properties;

import org.jboss.bqt.client.QueryScenario;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.TestResult;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;

/**
 * The QueryScenarioImpl extends the QueryScenerio handle the testresults for
 * defaults settings.
 * 
 * 
 */
public class QueryScenarioImpl extends QueryScenario {

	public QueryScenarioImpl(String scenarioName, Properties queryProperties) {
		super(scenarioName, queryProperties);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.bqt.test.client.QueryScenario#handleTestResult(org.jboss.bqt.test.client
	 * .TestResult, java.lang.String)
	 */
	@Override
	public void handleTestResult(TestResult tr, ResultSet resultSet,
			 boolean resultFromQuery) {
		Throwable resultException = tr.getException();
		if (tr.getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.COMPARE)) {

			Object results = null;
			try {
				results = this.getExpectedResults(tr.getQuerySetID())
						.compareResults(tr, resultSet,
								isOrdered(tr.getQuery()), resultFromQuery);

				if (results == null) {
					tr.setStatus(TestResult.RESULT_STATE.TEST_SUCCESS);
				} else {
					tr.setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);
					tr.setExceptionMessage("Results did not compare to expected results");
				}

			} catch (QueryTestFailedException qtf) {
				resultException = (resultException != null ? resultException
						: qtf);
				tr.setException(resultException);
				tr.setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);

			}

			if (tr.getStatus() == TestResult.RESULT_STATE.TEST_EXCEPTION) {
				try {
					this.getResultsGenerator().generateErrorFile(tr, resultSet, results);

				} catch (QueryTestFailedException qtfe) {
					throw new FrameworkRuntimeException(qtfe.getMessage());
				}
			}

		} else if (tr.getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.GENERATE)) { //$NON-NLS-1$

			try {

				this.getResultsGenerator().generateQueryResultFile(tr, resultSet); 

			} catch (QueryTestFailedException qtfe) {
				throw new FrameworkRuntimeException(qtfe.getMessage());
			}

		} else {
			// just create the error file for any failures
			if (tr.getException() != null) {
				tr.setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);
				try {
					this.getResultsGenerator().generateErrorFile(tr, resultSet, null);

				} catch (QueryTestFailedException qtfe) {
					throw new FrameworkRuntimeException(qtfe.getMessage());
				}
			}
		}

	}

	private boolean isOrdered(String sql) {

		if (sql.toLowerCase().indexOf(" order by ") > 0) {
			return true;
		}
		return false;

	}
	
	public void writeQueryTests(QueryTest queryTest) throws Exception {

	}

}
