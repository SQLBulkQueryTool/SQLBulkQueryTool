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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.DatabaseMetaDataReader;
import org.jboss.bqt.framework.TransactionQueryTestCase;

/**
 * CreateSQLQueryFile is used to create sql queries based on the database metadata.
 * 
 */
public class CreateSQLQueryFile extends TransactionQueryTestCase {

	private QueryScenario queryScenario = null;
	private QueryTest queries = null;

	CreateSQLQueryFile(QueryScenario querySet) {
		super(querySet.getQueryScenarioIdentifier());
		this.queryScenario = querySet;
	}

	@Override
	public String getTestName() {
		return queryScenario.getQueryScenarioIdentifier();
	}


	@Override
	public void testCase() throws FrameworkRuntimeException {

		try {
			// need to set this so the underlying query execution handles an
			// error properly.

			this.setup();
			
			DatabaseMetaDataReader reader = new DatabaseMetaDataReader(this.getConnection());
			
			
			List<String> querystrings = reader.getQueries();
			
			if (querystrings == null || querystrings.isEmpty()) {
				final String msg = ClientPlugin.Util.getString(
						"CreateSQLFile.noQueriesBuiltFromDatabase", this.getConnection().getMetaData().getDriverName()); //$NON-NLS-1$            

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
			
			this.queries = new QueryTest(queryScenario.getQueryScenarioIdentifier(),"sql", queryScenario.getQuerySetName(), querysqls,false);
			
			this.queryScenario.writeQueryTests(this.queries);
			ClientPlugin.LOGGER.info("# Queries written in the file: " + querystrings.size());

		} catch (FrameworkRuntimeException fre) {
			throw fre;
		} catch (Throwable t) {
			this.setApplicationException(t);

		} finally {
			// Capture resp time
		}

	}


	@SuppressWarnings("unused")
	@Override
	protected Statement createStatement() throws SQLException {
		return null;
	}

	// need to override this method because the abstract logic for throwing
	// exceptions depends on this
	@Override
	public boolean exceptionExpected() {
		return false;
	}

	/**
	 * Override the super cleanup() so that the connection is not
	 * cleaned up at this time.
	 * 
	 * This will be handled after all queries in the set have been executed.
	 * 
	 */
	@Override
	public void cleanup() {
		//
		// NOTE: do not cleanup TestResults because {@link #getTestResult} is
		// called
		// after cleanup

	}

}
