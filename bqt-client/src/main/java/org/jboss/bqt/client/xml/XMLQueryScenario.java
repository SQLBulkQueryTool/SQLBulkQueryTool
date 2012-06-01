/* JBoss, Home of Professional Open Source.
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
package org.jboss.bqt.client.xml;

import java.sql.ResultSet;
import java.util.Properties;

import org.jboss.bqt.client.ExpectedResults;
import org.jboss.bqt.client.QueryScenario;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.QueryWriter;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.TestResult;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.TransactionRuntimeException;

/**
 * The CTCQueryScenario represents the tests that were created using the xml
 * file formats.
 * 
 * @author vanhalbert
 * 
 */
public class XMLQueryScenario extends QueryScenario {

	public XMLQueryScenario(String scenarioName, Properties querySetProperties) {
		super(scenarioName, querySetProperties);
	}

	protected void setUp() {
		
		validateResultsMode(this.getProperties());


		if (!this.isSQL()) {
			try {
				reader = new XMLQueryReader(this.getQueryScenarioIdentifier(),
						this.getProperties());
			} catch (QueryTestFailedException e1) {
				throw new TransactionRuntimeException(e1);
			}
	
			resultsGen = new XMLGenerateResults(this.getQueryScenarioIdentifier(),
					this.getProperties());
	
			if (reader.getQuerySetIDs() == null
					|| reader.getQuerySetIDs().isEmpty()) {
				throw new TransactionRuntimeException(
						"The queryreader did not return any queryset ID's to process");
			}
		}
	}

	@Override
	public ExpectedResults getExpectedResults(String querySetID) {
		return new XMLExpectedResults(querySetID, this.getProperties());
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
			if (tr.getStatus() != TestResult.RESULT_STATE.TEST_EXCEPTION) {
				try {
					this.getExpectedResults(tr.getQuerySetID())
							.compareResults(tr, resultSet,
									isOrdered(tr.getQuery()), resultFromQuery);

				} catch (QueryTestFailedException qtf) {
					resultException = (resultException != null ? resultException
							: qtf);
					tr.setException(resultException);
					tr.setStatus(TestResult.RESULT_STATE.TEST_EXCEPTION);

				}
			}

		} else if (tr.getResultMode().equalsIgnoreCase(TestProperties.RESULT_MODES.GENERATE)) { //$NON-NLS-1$

			try {
				
				this.getResultsGenerator().generateQueryResultFile(tr, resultSet);

			} catch (QueryTestFailedException qtfe) {
				throw new TransactionRuntimeException(qtfe.getMessage());
			}

		} 
		
		// just create the error file for any failures
		// also, rechecck cause the status could have changed to exception during the
		// the processing of expected results (i.e, malformed xml file)

		if (tr.getStatus() == TestResult.RESULT_STATE.TEST_EXCEPTION) {

			try {
				this.getResultsGenerator().generateErrorFile(tr, resultSet, this.getExpectedResults(tr.getQuerySetID())
						.getResultsFile(tr.getQueryID()));

			} catch (QueryTestFailedException qtfe) {
				throw new TransactionRuntimeException(qtfe.getMessage());
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
		getQueryWriter().writeQueryTest(queryTest);

	}
	
	public 	QueryWriter getQueryWriter()  {
		return new XMLQueryWriter(this.getQueryScenarioIdentifier(), this.getProperties() );
	}

}
