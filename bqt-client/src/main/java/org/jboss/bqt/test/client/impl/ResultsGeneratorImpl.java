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

package org.jboss.bqt.test.client.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.core.util.ArgCheck;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.TestResultSetUtil;
import org.jboss.bqt.test.client.ResultsGenerator;
import org.jboss.bqt.test.client.TestProperties;
import org.jboss.bqt.test.client.TestResult;
import org.jboss.bqt.core.exception.QueryTestFailedException;

public class ResultsGeneratorImpl implements ResultsGenerator {
	private static final SimpleDateFormat FILE_NAME_DATE_FORMATER = new SimpleDateFormat(
			"yyyyMMdd_HHmmss"); //$NON-NLS-1$
	private String outputDir = "";
	private String generateDir = "";

	private static final int MAX_COL_WIDTH = 65;

	public ResultsGeneratorImpl(String testname, Properties props) {

		outputDir = props.getProperty(TestProperties.PROP_OUTPUT_DIR, ".");

		ArgCheck.isNotNull(outputDir, "Property "
				+ TestProperties.PROP_OUTPUT_DIR + " was not specified");

		outputDir = outputDir + "/" + testname;

		generateDir = props.getProperty(PROP_GENERATE_DIR, ".");
		ArgCheck.isNotNull(this.generateDir, "Property " + PROP_GENERATE_DIR
				+ " was not specified");

		File d = new File(this.outputDir);
		this.outputDir = d.getAbsolutePath();
		d = new File(this.outputDir);
		if (d.exists()) {
			FileUtils.removeDirectoryAndChildren(d);

		}
		if (!d.exists()) {
			d.mkdirs();
		}

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
	
	

	public void generateQueryResultFile(String querySetID, String queryID,
			String query, ResultSet result, Throwable ex, int testStatus)
			throws QueryTestFailedException {

		File fos = createNewResultsFile(queryID, querySetID, getGenerateDir());

		FileOutputStream actualOut = null;
		try {
			actualOut = new FileOutputStream(fos);
			PrintStream filePrintStream = new PrintStream(actualOut);
			if (ex != null) {
				TestResultSetUtil.printThrowable(ex, query, filePrintStream);
			} else if (result != null) {
				result.beforeFirst();
				TestResultSetUtil.printResultSet(result, query, MAX_COL_WIDTH,
						true, filePrintStream);
			}

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

	@Override
	public void generateQueryResultFile(TestResult testResult,
			ResultSet resultSet) throws QueryTestFailedException {
		// TODO Auto-generated method stub
	}

	@Override
	public String generateErrorFile(TestResult testResult, ResultSet resultSet,
			Object results) throws QueryTestFailedException {
		// TODO Auto-generated method stub
		return null;
	}

	public String generateErrorFile(final String querySetID,
			final String queryID, final String sql, final ResultSet resultSet,
			final Throwable queryError, final Object results)
			throws QueryTestFailedException {

		String errorFileName = null;
		try {
			// write actual results to error file
			errorFileName = generateErrorFileName(queryID, querySetID);
			// configID, queryID, Integer.toString(clientID));
			//           CombinedTestClient.log("\t" + this.clientID + ": Writing error file with actual results: " + errorFileName); //$NON-NLS-1$ //$NON-NLS-2$
			File errorFile = new File(getOutputDir(), errorFileName);

			// the resultset will be passed in as null when
			// the error was due to a thrown exception, and not based comparison
			// issues
			if (resultSet == null) {
				FileOutputStream actualOut = null;
				try {
					actualOut = new FileOutputStream(errorFile);
					PrintStream filePrintStream = new PrintStream(actualOut);

					TestResultSetUtil.printThrowable(queryError, sql,
							filePrintStream);

					filePrintStream.flush();

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
				return errorFileName;

			}

			// rewind resultset

			resultSet.beforeFirst();

			generateErrorResults(querySetID, queryID, sql, errorFile,
					resultSet, (results != null ? (List) results : null));

		} catch (Throwable e) {
			throw new QueryTestFailedException(e.getMessage());
			//           CombinedTestClient.logError("Error writing error file \"" + outputDir + "\"/" + errorFileName + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return errorFileName;
	}

	private File createNewResultsFile(String queryID, String querySetID,
			String genDir) {
		String resultFileName = queryID + ".txt"; //$NON-NLS-1$

		String targetDirname = genDir + File.separator + querySetID; //$NON-NLS-1$
		File targetDir = new File(targetDirname);
		targetDir.mkdirs();

		return new File(targetDir, resultFileName);
	}

	private String generateErrorFileName(String queryID, String querySetID) {
		// String errorFileName = "ERROR_"
		//		// configID + "_" //$NON-NLS-1$ //$NON-NLS-2$
		//		//                               + querySetID + "_" //$NON-NLS-1$
		// + queryID
		//		+ "_" //$NON-NLS-1$
		// + FILE_NAME_DATE_FORMATER.format(new Date(System
		//			.currentTimeMillis())) + ".txt"; //$NON-NLS-1$
		// return errorFileName;

		return queryID + ".txt";

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
			List<String> results) throws QueryTestFailedException {

		FileOutputStream actualOut = null;
		try {
			actualOut = new FileOutputStream(resultsFile);
			PrintStream filePrintStream = new PrintStream(actualOut);

			TestResultSetUtil.printResultSet(actualResult, sql, MAX_COL_WIDTH,
					true, filePrintStream);

			// if (results != null) {
			// for (Iterator<String> it=results.iterator(); it.hasNext();) {
			// String line = it.next();
			// filePrintStream.print(line);
			// }
			// } else {
			//
			// ResultSetUtil.printResultSet(actualResult, MAX_COL_WIDTH, true,
			// filePrintStream);
			// }

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

}
