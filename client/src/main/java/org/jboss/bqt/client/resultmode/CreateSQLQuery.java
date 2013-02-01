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

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.QueryReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.testcase.CreateSQLQueryFile;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TestCaseLifeCycle;
import org.jboss.bqt.framework.TransactionAPI;

/**
 * The SQL Result Mode controls the process for generating query files based on the
 * metadata from the connection.
 * 
 * @author vhalbert
 *
 */
public class CreateSQLQuery extends QueryScenario {

	/**
	 * @param scenarioName
	 * @param queryProperties
	 */
	public CreateSQLQuery(String scenarioName, Properties queryProperties) {
		super(scenarioName, queryProperties);

	}
	
	/**
	 * Call to obtain the {@link TestCaseLifeCycle testcase} thata will run
	 * this scenario
	 * @return TransactionQueryTestCase
	 */
	@Override
	public TestCaseLifeCycle getTestCase() {
		return new CreateSQLQueryFile(this);
	}
	
	@Override
	public boolean isSQL() {
		return true;
	}
	
	@Override
	public String getResultsMode()
	{
		return TestProperties.RESULT_MODES.SQL;
	}
	
	@Override
	public synchronized QueryReader getQueryReader() {
		return null;
	}

	@Override
	public Collection<String> getQuerySetIDs() {
		return null;
	}
	
	@Override
	public List<QueryTest> getQueries(String querySetID) {
		return null;
	}


	@Override
	public void handleTestResult(TestCase tr, TransactionAPI transaction) {
	}
	

}
