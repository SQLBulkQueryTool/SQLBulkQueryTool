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

import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.ErrorWriter;
import org.jboss.bqt.client.api.TestResult;
import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.ExceptionUtil;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.xml.JdomHelper;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

/**
 * @author vhalbert
 *
 */
public class XMLErrorWriter implements ErrorWriter {
	
	private String errorDirectory = null;
	
	/**
	 * @param testname 
	 * @param props 
	 * 
	 */
	public XMLErrorWriter(String testname, Properties props) {

		errorDirectory = props.getProperty(TestProperties.PROP_ERRORS_DIR);
		if (errorDirectory == null) {
			BQTUtil.throwInvalidProperty(TestProperties.PROP_ERRORS_DIR);
		}

		File d = new File(this.errorDirectory);		
		if (d.exists()) {
			FileUtils.removeDirectoryAndChildren(d);
		}
		if (!d.exists()) {
			d.mkdirs();
		}	
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.client.api.ErrorWriter#getErrorDirectory()
	 */
	public String getErrorDirectory() {
		return errorDirectory;
	}
	
	public 	String generateErrorFile(final String querySetID,
			final String queryID, final Throwable error)
				throws FrameworkException {

		String errorFileName = null;
			// write actual results to error file
			errorFileName = generateErrorFileName(queryID, querySetID);
			// configID, queryID, Integer.toString(clientID));
			//           CombinedTestClient.log("\t" + this.clientID + ": Writing error file with actual results: " + errorFileName); //$NON-NLS-1$ //$NON-NLS-2$
			File errorFile = new File(getErrorDirectory(), errorFileName);
			
			generateErrorResults(querySetID, queryID,
					 (String) null, errorFile, (ResultSet) null, (File) null, error);
		
		return errorFileName;
	}

	public String generateErrorFile(TestResult testResult, ResultSet resultSet,
			Object results) throws QueryTestFailedException, FrameworkException {
		return generateErrorFile(testResult.getQuerySetID(),
				testResult.getQueryID(), testResult.getQuery(), resultSet,
				testResult.getException(), results);
		
		}

	public String generateErrorFile(final String querySetID,
			final String queryID, final String sql, final ResultSet resultSet,
			final Throwable queryError, final Object expectedResultsFile)
			throws QueryTestFailedException, FrameworkException {

		String errorFileName = null;
		try {
			// write actual results to error file
			errorFileName = generateErrorFileName(queryID, querySetID);
			// configID, queryID, Integer.toString(clientID));
			//           CombinedTestClient.log("\t" + this.clientID + ": Writing error file with actual results: " + errorFileName); //$NON-NLS-1$ //$NON-NLS-2$
			File errorFile = new File(getErrorDirectory(), errorFileName);

			// rewind resultset
			if (resultSet != null) {
				resultSet.beforeFirst();
			}
			generateErrorResults(querySetID, queryID, sql, errorFile,
					resultSet, (File) expectedResultsFile, queryError);

		} catch (SQLException sqle) {
			throw new QueryTestFailedException(sqle);
		} catch (FrameworkException fre) {
			throw fre;
		} catch (FrameworkRuntimeException e) {
			throw e;
		}
		return errorFileName;
	}

		/**
		 * GenerateExpectedResults an error file for a query that failed comparison. File should
		 * have the SQL, the actual results returned from the server and the results
		 * that were expected.
		 * @param querySetID 
		 * @param queryID
		 * @param sql
		 * @param resultsFile
		 * @param actualResult
		 * @param expectedResultFile
		 * @param ex
		 * @throws FrameworkException
		 */
		private void generateErrorResults(String querySetID, String queryID,
				String sql, File resultsFile, ResultSet actualResult,
				File expectedResultFile, Throwable ex)
				throws FrameworkException {
			OutputStream outputStream;
			try {
				FileOutputStream fos = new FileOutputStream(resultsFile);
				outputStream = new BufferedOutputStream(fos);
			} catch (IOException e) {
				throw new FrameworkRuntimeException(
						"Failed to open error results file: " + resultsFile.getPath() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			try {
				XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();

				// Create root JDOM element
				Element rootElement = new Element(TagNames.Elements.ROOT_ELEMENT);

				// create a JDOM element for the results
				Element resultElement = new Element(TagNames.Elements.QUERY_RESULTS);
				// set the queryIDAttr on the exception element
				resultElement.setAttribute(new Attribute(TagNames.Attributes.NAME,
						queryID));
				// set the querySQLAttr on the exception element
				resultElement.setAttribute(new Attribute(TagNames.Attributes.VALUE,
						(sql != null ? sql : "NULL")));

				// ---------------------
				// Actual Exception
				// ---------------------
				// create a JDOM element from the actual exception object
				// produce xml for the actualException and this to the
				// exceptionElement
				if (ex != null) {
					Element actualExceptionElement = new Element(
							TagNames.Elements.ACTUAL_EXCEPTION);

					actualExceptionElement = XMLQueryVisitationStrategy
							.jdomException(ex, actualExceptionElement);
					resultElement.addContent(actualExceptionElement);
				} else if (actualResult != null) {
					// ------------------------------
					// Got a ResultSet from server
					// error was in comparing results
					// ------------------------------

					// --------------------------
					// Actual Result - ResultSet
					// --------------------------
					// produce a JDOM element from the actual results object
					Element actualResultsElement = new Element(
							TagNames.Elements.ACTUAL_QUERY_RESULTS);
					actualResultsElement = jstrat.produceMsg(actualResult,
							actualResultsElement);

					// add the results elements to the root element
					resultElement.addContent(actualResultsElement);

				} 
				
				// ---------------------
				// Expected Results - ...
				// ---------------------
				// produce xml for the expected results
				// Get expected results
				Element expectedResult = new Element("bogus"); //$NON-NLS-1$
				
				try {
					expectedResult = jstrat.parseXMLResultsFile(expectedResultFile,
							expectedResult);
					
					if (expectedResult.getChild(TagNames.Elements.SELECT) != null) {
						// ----------------------------------------------------------
						// -
						// Expected result was a ResultSet set element name to
						// reflect
						// ----------------------------------------------------------
						// -
						expectedResult
								.setName(TagNames.Elements.EXPECTED_QUERY_RESULTS);
					} else {
						// ----------------------------------------------------------
						// --
						// Expected result was an exception set element name to
						// reflect
						// ----------------------------------------------------------
						// --
						expectedResult
								.setName(TagNames.Elements.EXPECTED_EXCEPTION);
					}
					
					resultElement.addContent(expectedResult);

				} catch (Throwable jdomerror) {
					jstrat.produceMsg(jdomerror, resultElement);
				}

				// ------------------------------
				// Got an exeption from the server
				// error was in comparing exceptions
				// ------------------------------

				// add the results elements to the root element
				rootElement.addContent(resultElement);

				// Output xml
				XMLOutputter outputter = new XMLOutputter(JdomHelper.getFormat(
						"  ", true)); //$NON-NLS-1$
				outputter.output(new Document(rootElement), outputStream);

			} catch (SQLException e) {
				throw new FrameworkException(
						"Failed to convert error results to JDOM: " + e.getMessage()); //$NON-NLS-1$
			} catch (JDOMException e) {
				throw new FrameworkException(
						"Failed to convert error results to JDOM: " + e.getMessage()); //$NON-NLS-1$
			} catch (IOException e) {
				throw new FrameworkException(
						"Failed to output error results to " + resultsFile.getPath() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Throwable e) {
				throw new FrameworkException(
						"Failed to convert error results to JDOM: " + ExceptionUtil.getStackTrace(e)); //$NON-NLS-1$
			} finally {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
		}


		private String generateErrorFileName(String queryID, String querySetID) {
			// String errorFileName = "ERROR_"
			// configID + "_" //$NON-NLS-1$ //$NON-NLS-2$
			//                               + querySetID + "_" //$NON-NLS-1$
			// String errorFileName = queryID +
			//		+ "_" //$NON-NLS-1$
			// + FILE_NAME_DATE_FORMATER.format(new Date(System
			//			.currentTimeMillis())) + ".xml"; //$NON-NLS-1$
			// return errorFileName;

			return queryID + ".err";
		}

}
