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

import org.jboss.bqt.client.FileType;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.QueryScenario;
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
	 * @see org.jboss.bqt.client.FileType#getExpectedResultsReaderClassName()
	 */
	public String getExpectedResultsReaderClassName() {
		return XMLExpectedResults.class.getName();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.client.FileType#getExpectedResultsWriterClassName()
	 */
	public String getExpectedResultsWriterClassName() {
		return XMLExpectedResultsWriter.class.getName();
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
	 * @return String expected results file name
	 */
	
	public String getExpectedResultsFileName(QueryScenario scenario, QueryTest test) {
		return test.getQuerySetID() + "_" + test.getQueryID() + ".xml"; //$NON-NLS-1$
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
	

}
