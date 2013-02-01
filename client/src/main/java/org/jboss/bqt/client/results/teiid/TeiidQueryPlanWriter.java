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

package org.jboss.bqt.client.results.teiid;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.api.ExpectedResultsWriter;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.results.ExpectedResultsHolder;
import org.jboss.bqt.client.xml.TagNames;
import org.jboss.bqt.client.xml.TagNames.Elements;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.TestResultSetUtil;
import org.jboss.bqt.framework.AbstractQuery;
import org.jboss.bqt.framework.TestCase;
import org.jboss.bqt.framework.TransactionAPI;
import org.teiid.jdbc.TeiidStatement;

/**
 * XMLTeiidQueryPlanWriter is expecting the test is using the Teiid Driver and that
 * a Teiid Statement will provide the NodePlan from the Statement.
 * 
 * @author vhalbert
 *
 */
public class TeiidQueryPlanWriter extends ExpectedResultsWriter{


	public TeiidQueryPlanWriter(QueryScenario scenario, Properties props) {
		super(scenario, props);

		// NOTE:  this will not delete the directory if it exists, like the XMLExpectedResultsWriter.
		// 	  The expectation is this is an add-on function, to be done in conjunction with the primary
		//    function of handling sql results.
		File d = new File(getGenerateDir());

		if (!d.exists()) {
			d.mkdirs();
		}

		ClientPlugin.LOGGER.info("XMLTeiidQueryPlanWriter: will be written to " + d.getAbsolutePath());
	}

	@Override
	public ExpectedResults generateExpectedResultFile(TestCase testcase,
				TransactionAPI transaction) throws FrameworkException {
		
		// no processing of exceptions, will already have been handled
		if (testcase.getTestResult().isFailure()) {
			return null;
		}
		ExpectedResults rh = new ExpectedResultsHolder(TagNames.Elements.QUERY, (QueryTest) testcase.getActualTest() );
		
		String filename = this.getQueryScenario().getFileType().getExpectedResultsFileName(this.getQueryScenario(), (QueryTest)testcase.getActualTest(), ".pln");		
		File resultsFile = createNewResultsFile(testcase.getTestResult().getQuerySetID(), getGenerateDir(), filename);
		
		rh.setExpectedResultsFile(resultsFile);
		
		TeiidStatement statement = (TeiidStatement)   ((AbstractQuery) transaction).getStatement();
		
		if (statement.getPlanDescription() == null) {
			throw new FrameworkRuntimeException("QueryPlan is not available, check that SHOWPLAN=ON|DEBUG is specified on the URL");
		}

		String xml = statement.getPlanDescription().toXml();
		
		TeiidUtil.printResults(xml, testcase.getTestResult().getQuery(), resultsFile);
		
		return rh;
	}



	private File createNewResultsFile(String querySetID, String genDir, String filename) {

		String targetDirname = genDir + File.separator + querySetID; //$NON-NLS-1$

		File dir = new File(targetDirname);
		if (!dir.exists()) {
			dir.mkdirs();
			ClientPlugin.LOGGER.info("XMLTeiidQueryPlanWriter: creating query set directory " + dir.getAbsolutePath());
		}
		
		return new File(targetDirname, filename);
	}

}
