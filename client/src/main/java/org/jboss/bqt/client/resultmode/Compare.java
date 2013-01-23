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

import java.sql.ResultSet;
import java.util.Properties;

import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.ExpectedResultsWriter;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.api.QueryWriter;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.framework.Test;

/**
 * The Compare Result Mode controls the process for comparing actual results against the expected
 * results.
 *  
 * @author vhalbert
 *
 */
public class Compare extends QueryScenario {

	/**
	 * @param scenarioName
	 * @param queryProperties
	 */
	public Compare(String scenarioName, Properties queryProperties) {
		super(scenarioName, queryProperties);

	}
	
	
	@Override
	public boolean isCompare() {
		return true;
	}
	
	@Override
	public String getResultsMode()
	{
		return TestProperties.RESULT_MODES.COMPARE;
	}
	
	@Override
	public synchronized ExpectedResultsWriter getExpectedResultsGenerator() {
		return null;
	}
	
	@Override
	public synchronized QueryWriter getQueryWriter() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @throws QueryTestFailedException 
	 * @throws  FrameworkException
	 *
	 * @see org.jboss.bqt.client.api.QueryScenario#handleTestResult(Test, java.sql.ResultSet)
	 */
	@Override
	public void handleTestResult(Test tr, ResultSet resultSet) throws FrameworkException, QueryTestFailedException {
		
		if (tr.isFailure()) {
			Throwable resultException = tr.getException();
			try {
				this.getExpectedResultsReader(tr.getQuerySetID())
						.compareResults(tr, resultSet,
								isOrdered(tr.getQuery()));

			} catch (QueryTestFailedException qtf) {
				resultException = (resultException != null ? resultException
						: qtf);
				tr.setException(resultException);

			}
		}

		// create an error file that also contains the expected results
		if (tr.isFailure()) {
			this.getErrorWriter().generateErrorFile(tr, resultSet,
					this.getExpectedResultsReader(tr.getQuerySetID()).getResultsFile(tr));

		}
	}
	
	private boolean isOrdered(String sql) {
		if (sql == null) return false;

		if (sql.toLowerCase().indexOf(" order by ") > 0) {
			return true;
		}
		return false;

	}
}
