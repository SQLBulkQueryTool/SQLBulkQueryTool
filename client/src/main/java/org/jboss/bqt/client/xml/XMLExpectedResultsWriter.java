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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.ExpectedResultsWriter;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.util.ExceptionUtil;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.xml.JdomHelper;
import org.jboss.bqt.framework.TestCase;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

public class XMLExpectedResultsWriter extends ExpectedResultsWriter{
//	private static final int MAX_COL_WIDTH = 65;


	public XMLExpectedResultsWriter(QueryScenario scenario, Properties props) {
		super(scenario, props);

		File d = new File(getGenerateDir());
		if (d.exists()) {
			FileUtils.removeDirectoryAndChildren(d);
		}
		if (!d.exists()) {
			d.mkdirs();
		}

		ClientPlugin.LOGGER.info("XMLExpectedResultsWriter: creating expected results " + d.getAbsolutePath());
	}

	@Override
	public void generateQueryResultFile(TestCase testcase,
			ResultSet result) throws FrameworkRuntimeException {
		
		String querySetID = testcase.getTestResult().getQuerySetID();
		String queryID = testcase.getTestResult().getQueryID();
		String query = testcase.getTestResult().getQuery();
		Throwable ex = testcase.getTestResult().getException();

		try {
			if (result != null)
				result.isClosed();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String filename = this.getQueryScenario().getFileType().getExpectedResultsFileName(this.getQueryScenario(), (QueryTest)testcase.getActualTest());
		
		File resultsFile = createNewResultsFile(querySetID, getGenerateDir(), filename);
		OutputStream outputStream;
		try {
			FileOutputStream fos = new FileOutputStream(resultsFile);
			outputStream = new BufferedOutputStream(fos);
		} catch (IOException e) {
			throw new FrameworkRuntimeException(
					"Failed to open new results file: " + resultsFile.getPath() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		try {
			XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();

			// Create root JDOM element
			Element rootElement = new Element(TagNames.Elements.ROOT_ELEMENT);

			// Create Query element
			Element queryElement = new Element(TagNames.Elements.QUERY);
			queryElement.addContent(new CDATA(query));
			rootElement.addContent(queryElement);

			// create a result attribute for the queryID
			Attribute resultsIDAttribute = new Attribute(
					TagNames.Attributes.NAME, queryID);

			if (result != null) {
				// produce a JDOM element from the results object
				Element resultsElement = jstrat.produceResults(result);
				// set the resultsIDAttribute on the results element
				resultsElement.setAttribute(resultsIDAttribute);
				
				String t = "";

				Attribute timeIDAttribute = new Attribute(
						TagNames.Attributes.EXECUTION_TIME, t);

				resultsElement.setAttribute(timeIDAttribute);

				
				// add the results elements to the root element
				rootElement.addContent(resultsElement);
				// debug:
				// System.out.println("\n Result: " + printResultSet(result));
			} else {
				// create a JDOM element from the exception object with the
				// results tag
				Element exceptionElement = new Element(
						TagNames.Elements.QUERY_RESULTS);
				// produce xml for the actualException and this to the
				// exceptionElement
				if (ex != null) {
					exceptionElement.addContent(jstrat.produceMsg(ex, null));
				}
				// set the resultsIDAttribute on the exception element
				exceptionElement.setAttribute(resultsIDAttribute);
				// add the results elements to the root element
				rootElement.addContent(exceptionElement);

			}

			// Output xml
			XMLOutputter outputter = new XMLOutputter(JdomHelper.getFormat(
					"  ", true)); //$NON-NLS-1$
			outputter.output(new Document(rootElement), outputStream);

		} catch (SQLException e) {
			throw new FrameworkRuntimeException(
					"Failed to convert results to JDOM: " + e.getMessage()); //$NON-NLS-1$
		} catch (JDOMException e) {
			throw new FrameworkRuntimeException(
					"Failed to convert results to JDOM: " + e.getMessage()); //$NON-NLS-1$
		} catch (IOException e) {
			throw new FrameworkRuntimeException(
					"Failed to output new results to " + resultsFile.getPath() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Throwable e) {
			throw new FrameworkRuntimeException(
					"Failed to convert results to JDOM: " + ExceptionUtil.getStackTrace(e)); //$NON-NLS-1$
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}



	private File createNewResultsFile(String querySetID, String genDir, String filename) {

		String targetDirname = genDir + File.separator + querySetID; //$NON-NLS-1$

		File dir = new File(targetDirname);
		if (!dir.exists()) {
			dir.mkdirs();
			ClientPlugin.LOGGER.info("XMLExpectedResultsWriter: creating query set directory " + dir.getAbsolutePath());
		}

		
		return new File(targetDirname, filename);
	}

}
