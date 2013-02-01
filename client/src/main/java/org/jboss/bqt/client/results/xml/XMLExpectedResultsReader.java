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

package org.jboss.bqt.client.results.xml;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.results.ExpectedResultsHolder;
import org.jboss.bqt.client.xml.XMLQueryVisitationStrategy;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.framework.AbstractQuery;
import org.jboss.bqt.framework.ActualTest;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestResult;
import org.jboss.bqt.framework.TransactionAPI;
import org.jdom.JDOMException;
import org.teiid.core.util.ArgCheck;

public class XMLExpectedResultsReader extends ExpectedResultsReader {
	
	private XMLCompareResults compare;
	private ExpectedResults lastResults;
	private ActualTest lastTest;

	public XMLExpectedResultsReader(QueryScenario scenario, String querySetID, Properties props) {
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
	public synchronized ExpectedResults getExpectedResults(ActualTest queryTest) {
		if (lastTest != null && lastTest.equals(queryTest)) {
			return lastResults;
		}
		
		File er = findExpectedResultsFile((QueryTest) queryTest, this.getQuerySetID());
		ExpectedResultsHolder rh = loadExpectedResults((QueryTest)queryTest, er);

		this.lastResults = rh;
		this.lastTest = queryTest;
		
		return rh;
	}

	@Override
	public void compareResults(final TestCase testcase,
			final TransactionAPI transaction, final ExpectedResults expectedResults, final boolean isOrdered) throws QueryTestFailedException {

		ResultSet resultSet = ((AbstractQuery) transaction).getResultSet();
		compare.compareResults(testcase, expectedResults, resultSet, isOrdered);
	
	}

	private ExpectedResultsHolder loadExpectedResults(QueryTest test, File resultsFile) {
		ArgCheck.isNotNull(resultsFile);
		XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();
		final ExpectedResultsHolder expectedResult;
		try {
			expectedResult = jstrat.parseXMLResultsFile(test, this.getQueryScenario().getQueryScenarioIdentifier(), resultsFile);
			expectedResult.setExpectedResultsFile(resultsFile);
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
		String resultFileName = this.getQueryScenario().getFileType().getExpectedResultsFileName(this.getQueryScenario(), test, ".xml");
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
