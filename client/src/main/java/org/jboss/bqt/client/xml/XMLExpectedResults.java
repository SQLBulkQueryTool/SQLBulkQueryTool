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

package org.jboss.bqt.client.xml;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.framework.TestCase;
import org.jdom.JDOMException;

public class XMLExpectedResults extends ExpectedResultsReader {
	
	private XMLCompareResults compare;

	public XMLExpectedResults(QueryScenario scenario, String querySetID, Properties props) {
		super(scenario, querySetID, props);

		File dir = new File(this.getExpectResultsLocation() + File.separator + querySetID);
		if (!dir.exists()) {
			if (this.getQueryScenario().isExpectedResultsNeeded()) {			
				throw new FrameworkRuntimeException("Query expected results directory "
						+ dir.getAbsolutePath() + " does not exist");
			}
		} else if (dir.list() == null) {
			throw new FrameworkRuntimeException("Query expected results directory "
					+ dir.getAbsolutePath() + " does not contain any files");
		}
		
		compare = XMLCompareResults.create(props);

		ClientPlugin.LOGGER.debug("Expected results loc: " + dir.getAbsolutePath());
	}
	
	
	@Override
	public ExpectedResults getExpectedResults(QueryTest test) {
		File er = findExpectedResultsFile(test, this.getQuerySetID());
		ExpectedResultsHolder rh = loadExpectedResults(test, er);
		return rh;
	}


	@Override
	public synchronized File getResultsFile(QueryTest testResult)
			throws FrameworkRuntimeException {
		return findExpectedResultsFile(testResult, this.getQuerySetID());

	}
	

	/**
	 * Compare the results of a query with those that were expected.
	 * @param testcase 
	 * @param resultSet 
	 * @param isOrdered 
	 * 
	 * 
	 * @return The response time for comparing the first batch (sizes) of
	 *         resutls.
	 * @throws QueryTestFailedException
	 *             If comparison fails.
	 */
	@Override
	public Object compareResults(final TestCase testcase,
			final ResultSet resultSet, final boolean isOrdered) throws QueryTestFailedException {

		return compare.compareResults(testcase, resultSet, isOrdered);
	
	}

	private ExpectedResultsHolder loadExpectedResults(QueryTest test, File resultsFile) {
		XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();
		final ExpectedResultsHolder expectedResult;
		try {
			expectedResult = jstrat.parseXMLResultsFile(test, this.getQueryScenario().getQueryScenarioIdentifier(), resultsFile);
		} catch (IOException e) {
			throw new FrameworkRuntimeException(
					"Unable to load expected results: " + e.getMessage()); //$NON-NLS-1$
		} catch (JDOMException e) {
			throw new FrameworkRuntimeException(
					"Unable to load expected results: " + e.getMessage()); //$NON-NLS-1$
		}
		return expectedResult;
	}

	private File findExpectedResultsFile(QueryTest test,
			String querySetIdentifier)  {
		String resultFileName = this.getQueryScenario().getFileType().getExpectedResultsFileName(this.getQueryScenario(), test);
			//queryIdentifier + ".xml"; //$NON-NLS-1$
		File file = new File(this.getExpectResultsLocation() + File.separator + this.getQuerySetID(),
				resultFileName);
		if (!file.exists() && this.getQueryScenario().isExpectedResultsNeeded()) {
			FrameworkRuntimeException fre = new FrameworkRuntimeException("Query results file "
					+ file.getAbsolutePath() + " cannot be found");
			throw fre;
		}

		return file;

	}
}
