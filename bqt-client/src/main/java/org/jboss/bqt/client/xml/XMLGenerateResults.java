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
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import org.jboss.bqt.client.ResultsGenerator;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.TestResult;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.ArgCheck;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.StringUtil;
import org.jboss.bqt.core.util.TestResultSetUtil;
import org.jboss.bqt.core.xml.JdomHelper;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

public class XMLGenerateResults implements ResultsGenerator {
	private static final int MAX_COL_WIDTH = 65;

	private String outputDir = "";
	private String generateDir = "";

	public XMLGenerateResults(String testname, Properties props) {

		outputDir = props.getProperty(TestProperties.PROP_OUTPUT_DIR, ".");

		ArgCheck.isNotNull(outputDir, "Property "
				+ TestProperties.PROP_OUTPUT_DIR + " was not specified");

		outputDir = outputDir + "/" + testname;

		File d = new File(this.outputDir);
		this.outputDir = d.getAbsolutePath();
		d = new File(this.outputDir);
		if (d.exists()) {
			FileUtils.removeDirectoryAndChildren(d);

		}
		if (!d.exists()) {
			d.mkdirs();
		}

		generateDir = props.getProperty(PROP_GENERATE_DIR, ".");

		ArgCheck.isNotNull(this.generateDir, "Property " + PROP_GENERATE_DIR
				+ " was not specified");

		d = new File(generateDir, testname);
		generateDir = d.getAbsolutePath();
		d = new File(generateDir);
		if (d.exists()) {
			FileUtils.removeDirectoryAndChildren(d);
		}
		if (!d.exists()) {
			d.mkdirs();
		}

	}

	@Override
	public String getGenerateDir() {
		// TODO Auto-generated method stub
		return this.generateDir;
	}

	@Override
	public String getOutputDir() {
		// TODO Auto-generated method stub
		return outputDir;
	}
	
	
	
	

	@Override
	public void generateQueryResultFile(TestResult testResult,
			ResultSet resultSet) throws QueryTestFailedException {
		// TODO Auto-generated method stub
		generateQueryResultFile(testResult.getQuerySetID(), testResult.getQueryID(),
				testResult.getQuery(),
				resultSet,
				testResult.getException(),
				testResult.getStatus(), testResult );

	}


	/**
	 * Generate query results. These are actual results from the server and may
	 * be used for comparing to results from a later test run.
	 * 
	 * @param queryID
	 * @param resultsFile
	 * @param result
	 * @param ex
	 * @throws QueryTestFailedException
	 */
	void generateQueryResultFile(String querySetID, String queryID,
			String query, ResultSet result, Throwable ex, int testStatus)
			throws QueryTestFailedException {

		generateQueryResultFile(querySetID, queryID, query, result, ex, testStatus, null);
	}
	

	private void generateQueryResultFile(String querySetID, String queryID,
			String query, ResultSet result, Throwable ex, int testStatus, TestResult testResult)
			throws QueryTestFailedException {

		try {
			if (result != null)
				result.isClosed();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		File resultsFile = createNewResultsFile(queryID, querySetID,
				getGenerateDir());
		OutputStream outputStream;
		try {
			FileOutputStream fos = new FileOutputStream(resultsFile);
			outputStream = new BufferedOutputStream(fos);
		} catch (IOException e) {
			throw new QueryTestFailedException(
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
				if (testResult != null) {
					
					Date starttest = new Date(testResult.getBeginTS());
					Date endtest = new Date(testResult.getEndTS());
					long diff = endtest.getTime() - starttest.getTime(); // diff in mills

					t = String.valueOf(diff);
					
				}
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
			throw new QueryTestFailedException(
					"Failed to convert results to JDOM: " + e.getMessage()); //$NON-NLS-1$
		} catch (JDOMException e) {
			throw new QueryTestFailedException(
					"Failed to convert results to JDOM: " + e.getMessage()); //$NON-NLS-1$
		} catch (IOException e) {
			throw new QueryTestFailedException(
					"Failed to output new results to " + resultsFile.getPath() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Throwable e) {
			throw new QueryTestFailedException(
					"Failed to convert results to JDOM: " + StringUtil.getStackTrace(e)); //$NON-NLS-1$
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}

	// Begin New from Impl

//	public String generateErrorFile_keep(final String querySetID,
//			final String queryID, final String sql, final ResultSet resultSet,
//			final Throwable queryError, final Object results)
//			throws QueryTestFailedException {
//
//		String errorFileName = null;
//		try {
//			// write actual results to error file
//			errorFileName = generateErrorFileName(queryID, querySetID);
//			// configID, queryID, Integer.toString(clientID));
//			//           CombinedTestClient.log("\t" + this.clientID + ": Writing error file with actual results: " + errorFileName); //$NON-NLS-1$ //$NON-NLS-2$
//			File errorFile = new File(getOutputDir(), errorFileName);
//
//			// the resultset will be passed in as null when
//			// the error was due to a thrown exception, and not based comparison
//			// issues
//			if (resultSet == null) {
//				FileOutputStream actualOut = null;
//				try {
//					actualOut = new FileOutputStream(errorFile);
//					PrintStream filePrintStream = new PrintStream(actualOut);
//
//					TestResultSetUtil.printThrowable(queryError, sql,
//							filePrintStream);
//
//					filePrintStream.flush();
//
//				} catch (Exception e) {
//					e.printStackTrace();
//					throw new QueryTestFailedException(e);
//				} finally {
//					if (actualOut != null) {
//						try {
//							actualOut.close();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//				return errorFileName;
//
//			}
//
//			// rewind resultset
//
//			resultSet.beforeFirst();
//
//			generateErrorResults(querySetID, queryID, sql, errorFile,
//					resultSet, (File) results);
//
//		} catch (Throwable e) {
//			throw new QueryTestFailedException(e.getMessage());
//			//           CombinedTestClient.logError("Error writing error file \"" + outputDir + "\"/" + errorFileName + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//		}
//		return errorFileName;
//	}

	/**
	 * Generate an error file for a query that failed comparison. File should
	 * have the SQL, the actual results returned from the server and the results
	 * that were expected.
	 * 
	 * @param queryID
	 * @param sql
	 * @param resultsFile
	 * @param actualResult
	 * @param expectedResultFile
	 * @param ex
	 * @throws QueryTestFailedException
	 */
	private void generateErrorResults(String querySetID, String queryID,
			String sql, File resultsFile, ResultSet actualResult, File results)
			throws QueryTestFailedException {

		FileOutputStream actualOut = null;
		try {
			actualOut = new FileOutputStream(resultsFile);
			PrintStream filePrintStream = new PrintStream(actualOut);

			TestResultSetUtil.printResultSet(actualResult, sql, MAX_COL_WIDTH,
					true, filePrintStream);

		} catch (Exception e) {
			e.printStackTrace();
			throw new QueryTestFailedException(e);
		} finally {
			if (actualOut != null) {
				try {
					actualOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public String generateErrorFile(TestResult testResult, ResultSet resultSet,
			Object results) throws QueryTestFailedException {
		return generateErrorFile(testResult.getQuerySetID(),
				testResult.getQueryID(), testResult.getQuery(), resultSet,
				testResult.getException(), results);
		
		}

	 String generateErrorFile(final String querySetID,
			final String queryID, final String sql, final ResultSet resultSet,
			final Throwable queryError, final Object expectedResultsFile)
			throws QueryTestFailedException {

		String errorFileName = null;
		try {
			// write actual results to error file
			errorFileName = generateErrorFileName(queryID, querySetID);
			// configID, queryID, Integer.toString(clientID));
			//           CombinedTestClient.log("\t" + this.clientID + ": Writing error file with actual results: " + errorFileName); //$NON-NLS-1$ //$NON-NLS-2$
			File errorFile = new File(getOutputDir(), errorFileName);

			// rewind resultset
			if (resultSet != null) {
				resultSet.beforeFirst();
			}
			generateErrorResults(querySetID, queryID, sql, errorFile,
					resultSet, (File) expectedResultsFile, queryError);

		} catch (Throwable e) {
			throw new QueryTestFailedException(e.getMessage());
			//           CombinedTestClient.logError("Error writing error file \"" + outputDir + "\"/" + errorFileName + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return errorFileName;
	}

	private File createNewResultsFile(String queryID, String querySetID,
			String genDir) {
		String resultFileName = queryID + ".xml"; //$NON-NLS-1$

		String targetDirname = genDir + File.separator + "expected_results" + File.separator + querySetID; //$NON-NLS-1$
		File targetDir = new File(targetDirname);
		targetDir.mkdirs();

		return new File(targetDir, resultFileName);
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

	/**
	 * Generate an error file for a query that failed comparison. File should
	 * have the SQL, the actual results returned from the server and the results
	 * that were expected.
	 * 
	 * @param queryID
	 * @param sql
	 * @param resultsFile
	 * @param actualResult
	 * @param expectedResultFile
	 * @param ex
	 * @throws QueryTestFailedException
	 */
	private void generateErrorResults(String querySetID, String queryID,
			String sql, File resultsFile, ResultSet actualResult,
			File expectedResultFile, Throwable ex)
			throws QueryTestFailedException {
		OutputStream outputStream;
		try {
			FileOutputStream fos = new FileOutputStream(resultsFile);
			outputStream = new BufferedOutputStream(fos);
		} catch (IOException e) {
			throw new QueryTestFailedException(
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
					sql));

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
			throw new QueryTestFailedException(
					"Failed to convert error results to JDOM: " + e.getMessage()); //$NON-NLS-1$
		} catch (JDOMException e) {
			throw new QueryTestFailedException(
					"Failed to convert error results to JDOM: " + e.getMessage()); //$NON-NLS-1$
		} catch (IOException e) {
			throw new QueryTestFailedException(
					"Failed to output error results to " + resultsFile.getPath() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Throwable e) {
			throw new QueryTestFailedException(
					"Failed to convert error results to JDOM: " + StringUtil.getStackTrace(e)); //$NON-NLS-1$
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}

}
