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

package org.jboss.bqt.client.testcase;

import java.util.List;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QuerySQL;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.AbstractQuery;
import org.jboss.bqt.framework.DatabaseMetaDataReader;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestResult;
import org.jboss.bqt.framework.TestCaseLifeCycle;
import org.jboss.bqt.framework.TransactionAPI;


/**
 * CreateSQLQueryFile is a test cast that uses the database metadatda from the data source connection
 * to generate SQL queries for all tables that it can see.
 * 
 */
public class CreateSQLQueryFile implements TestCaseLifeCycle {

	private QueryScenario scenario = null;
	
	
	private TransactionAPI trans = null;

	public CreateSQLQueryFile(QueryScenario scenario) {
		super();
		this.scenario = scenario;
	}
	

	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.framework.TestCaseLifeCycle#getTestName()
	 */
	public String getTestName() {
		return scenario.getQuerySetName();
	}



	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.framework.TestCaseLifeCycle#setup(org.jboss.bqt.framework.TransactionAPI)
	 */
	public void setup(TransactionAPI trans) throws FrameworkException {
		this.trans = trans;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.framework.TestCaseLifeCycle#cleanup()
	 */
	public void cleanup() {
		trans.cleanup();
		trans = null;
		scenario = null;
		
	}

	public void runTestCase()  {
		
		QueryTest queries = null;
		try {
			
			TestCase testCase = new TestCase(null);
			TestResult testResult = new TestResult(scenario.getQueryScenarioIdentifier(), scenario.getQuerySetName());
			
			testCase.setTestResult(testResult);
			trans.before(testCase);

			
			// need to set this so the underlying query execution handles an
			// error properly.
			
			DatabaseMetaDataReader reader = new DatabaseMetaDataReader( ( (AbstractQuery) trans).getConnection(), scenario.getProperties());
						
			List<String> querystrings = reader.getQueries();
			
			if (querystrings == null || querystrings.isEmpty()) {
				final String msg = ClientPlugin.Util.getString(
						"CreateSQLFile.noQueriesBuiltFromDatabase", ( (AbstractQuery) trans).getConnection().getMetaData().getDriverName()); //$NON-NLS-1$            

				ClientPlugin.LOGGER.error(msg);
				
				throw new FrameworkRuntimeException(msg);

			}
						
			QuerySQL[] querysqls = new QuerySQL[querystrings.size()];
			
			int i = 0;
			for (String query : querystrings) {
				QuerySQL qsql = new QuerySQL(query, null);
				querysqls[i] = qsql;
				i++;
				
			}
			
			queries = new QueryTest(scenario.getQueryScenarioIdentifier(), scenario.getQuerySetName(), "sql", querysqls);
			
			this.scenario.getQueryWriter().writeQueryTest(queries);

			ClientPlugin.LOGGER.info("# Queries written in the file: " + querystrings.size());

		} catch (Exception e) {
			throw new FrameworkRuntimeException(e);
		} finally {
			// Capture resp time
			trans.after();
		}

	}


















}
