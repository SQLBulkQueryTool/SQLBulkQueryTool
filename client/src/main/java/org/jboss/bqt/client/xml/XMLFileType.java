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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.FileType;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.ExpectedResultsWriter;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.query.xml.XMLQueryReader;
import org.jboss.bqt.client.query.xml.XMLQueryWriter;
import org.jboss.bqt.client.results.teiid.TeiidQueryPlanReader;
import org.jboss.bqt.client.results.teiid.TeiidQueryPlanWriter;
import org.jboss.bqt.client.results.xml.XMLExpectedResultsReader;
import org.jboss.bqt.client.results.xml.XMLExpectedResultsWriter;
import org.jboss.bqt.core.util.PropertiesUtils;
import org.jboss.bqt.core.util.ReflectionHelper;
import org.jboss.bqt.framework.TestResult;

/**
 * @author vhalbert
 *
 */
public class XMLFileType implements FileType {

	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.client.FileType#getErrorWriterClassName()
	 */
	public String getErrorWriterClassName() {
		return XMLErrorWriter.class.getName();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.client.FileType#getQueryWriterClassName()
	 */
	public String getQueryWriterClassName() {
		return XMLQueryWriter.class.getName();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.client.FileType#getQueryReaderClassName()
	 */
	public String getQueryReaderClassName() {
		return XMLQueryReader.class.getName();
	}
	
	public 	List<ExpectedResultsReader> getExpectedResultsReaders(QueryScenario scenario, Properties properties, String querySetID) {
		List<ExpectedResultsReader> resultsReaders = new ArrayList<ExpectedResultsReader>(2);
		
		Collection<Object> args = new ArrayList<Object>(3);
		args.add(scenario);
		args.add(querySetID);
		args.add(properties);

		resultsReaders.add( (ExpectedResultsReader) createInstance(XMLExpectedResultsReader.class.getName(), args) );

		boolean queryPlan = PropertiesUtils.getBooleanProperty(properties, TestProperties.QUERY_PLAN, false);

		if (queryPlan) {
			resultsReaders.add( (ExpectedResultsReader) createInstance(TeiidQueryPlanReader.class.getName(), args) );
		}
		
		return resultsReaders;
	}

	
	public List<ExpectedResultsWriter> getExpectedResultsWriters(QueryScenario scenario, Properties properties)  {
		List<ExpectedResultsWriter> resultsWriters = new ArrayList<ExpectedResultsWriter>(2);
		
		resultsWriters.add( createExpectedResultsWriter(scenario, properties, XMLExpectedResultsWriter.class.getName() ) );
		
		boolean queryPlan = PropertiesUtils.getBooleanProperty(properties, TestProperties.QUERY_PLAN, false);

		if (queryPlan) {
			resultsWriters.add( createExpectedResultsWriter(scenario, properties, TeiidQueryPlanWriter.class.getName() ) );
			
		}
		
		return resultsWriters;
	}

	
	/** 
	 * Returns the name of the query file (excluding path)
	 * @param scenario
	 * @param test
	 * @return String query file name
	 */
	public String getQueryFileName(QueryScenario scenario, QueryTest test) {
		return test.getQuerySetID() + ".xml";
	}

	
	/** 
	 * Returns the name of the file (excluding path)
	 * @param scenario
	 * @param test
	 * @param extension 
	 * @return String expected results file name
	 */
	
	public String getExpectedResultsFileName(QueryScenario scenario, QueryTest test, String extension) {
		return test.getQuerySetID() + "_" + test.getQueryID() + extension; //$NON-NLS-1$
	}
	
	/**
	 * Returns the name of the error file (excluding path)
	 * @param scenario
	 * @param testResult
	 * @return String error file name
	 */
	public String getErrorFileName(QueryScenario scenario, TestResult testResult) {
		return testResult.getQuerySetID() + "_" + testResult.getQueryID() + ".err";

	}
	
	private ExpectedResultsWriter createExpectedResultsWriter(QueryScenario scenario, Properties props, String fileName) {
		Collection<Object> args = new ArrayList<Object>(2);
		args.add(scenario);
		args.add(props);
		return (ExpectedResultsWriter) createInstance(fileName, args);
	}
	
	private Object createInstance(String clzzName, final Collection<?> args) {
		return ReflectionHelper.create(clzzName,args, null);
	}
	

}
