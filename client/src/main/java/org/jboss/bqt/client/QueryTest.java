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
package org.jboss.bqt.client;

import org.jboss.bqt.framework.ActualTest;
import org.jboss.bqt.framework.TestResult;

/**
 * The QueryTest represents a logical test execution.  This test can consist of one
 * or more {@link QuerySQL SQL} queries required to perform the test.
 * The QueryTest is what to run, where {@link TestResult} is the corresponding result of the test.
 * 
 * @author vanhalbert
 * 
 */
public class QueryTest extends ActualTest {

	private QuerySQL[] queries;
	private String querySetID;
	private String queryID;
	private String queryScenarioID;

	public QueryTest(String queryScenarioID, String querySetID, String queryID, 
			QuerySQL[] queries) {
		this.queryID = queryID;
		this.queries = queries;
		this.querySetID = querySetID;
		this.queryScenarioID = queryScenarioID;
	}

	public QuerySQL[] getQueries() {
		return queries;
	}

	public String getQueryID() {
		return queryID;
	}

	public String getQuerySetID() {
		return this.querySetID;
	}

	public String getQueryScenarioID() {
		return this.queryScenarioID;
	}
	
	
	
	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj instanceof QueryTest) {
			QueryTest that = (QueryTest) obj;
			if (! this.getQueryScenarioID().equals(that.getQueryScenarioID())) return false;
			if (! this.getQuerySetID().equals(that.getQuerySetID())) return false;
			if (! this.getQueryID().equals(that.getQueryID())) return false;
			
			return true;
		}
		
		
		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("QueryTest - Scenario: ");
		sb.append(queryScenarioID);
		sb.append("\nQuerySetID: ");
		sb.append(querySetID);
		sb.append("QueryID: ");
		sb.append(queryID);
		sb.append("#Queries: ");
		sb.append(queries.length);

		return sb.toString();
	}

}
