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

import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.ExpectedResultsReader;
import org.jboss.bqt.client.api.TestResult;
import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.ExceptionUtil;
import org.jboss.bqt.core.util.ObjectConverterUtil;
import org.jdom.JDOMException;

import org.apache.commons.lang.StringUtils;

public class XMLExpectedResults implements ExpectedResultsReader {
	private static String newline = System.getProperty("line.separator"); //$NON-NLS-1$
	
	private Properties props;
	private String resultMode = TestProperties.RESULT_MODES.NONE;
	private String querySetIdentifier = null;
	private String results_dir_loc = null;
	
	private double exceed_percent = -0.99999;
	private long exec_minumin_time = -1;

	private Map<String, ResultsHolder> loadedResults = new HashMap<String, ResultsHolder>();

	public XMLExpectedResults(String querySetIdentifier, Properties properties) {
		this.props = properties;
		this.querySetIdentifier = querySetIdentifier;
				
		resultMode = BQTUtil.getResultMode(this.props);

		this.results_dir_loc = props.getProperty(TestProperties.PROP_EXPECTED_RESULTS_DIR_LOC);
		if (this.results_dir_loc == null) {
			BQTUtil.throwInvalidProperty(TestProperties.PROP_EXPECTED_RESULTS_DIR_LOC);
		}
		
		File dir = new File(results_dir_loc + File.separator + querySetIdentifier);
		if (!dir.exists()) {
			if (this.isExpectedResultsNeeded()) {			
				throw new FrameworkRuntimeException("Query results directory "
						+ dir.getAbsolutePath() + " does not exist");
			}
		} else if (dir.list() == null) {
			throw new FrameworkRuntimeException("Query results directory "
					+ dir.getAbsolutePath() + " does not contain any files");
		}
		
		String exceed_per = props.getProperty(TestProperties.PROP_EXECUTE_EXCEED_PERCENT);
		String exec_min = props.getProperty(TestProperties.PROP_EXECUTE_TIME_MINEMUM);
		
		if (exceed_per != null && exceed_per.trim().length() > 0) {
			ClientPlugin.LOGGER.debug(" ======== " + TestProperties.PROP_EXECUTE_EXCEED_PERCENT + " is set to " + exceed_per);
			exceed_percent =  Double.parseDouble(exceed_per);		
		}
		
		if (exec_min != null && exec_min.trim().length() > 0) {
			ClientPlugin.LOGGER.debug(" ======== " + TestProperties.PROP_EXECUTE_EXCEED_PERCENT + " is set to " + exceed_per);
			exec_minumin_time =  Long.parseLong(exec_min);
		}		
		// if exceed percent was set and exec time was not, set exec time to minimum of 1 mil
		if (exceed_percent > 0 && exec_minumin_time < 0) exec_minumin_time = 1;

		ClientPlugin.LOGGER.debug("Expected results loc: " + this.results_dir_loc);
	}
	

	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.client.api.ExpectedResultsReader#getExpectResultsLocation()
	 */
	@Override
	public String getExpectResultsLocation() {
		return this.results_dir_loc;
	}


	@Override
	public boolean isExpectedResultsNeeded() {
		return (resultMode
				.equalsIgnoreCase(TestProperties.RESULT_MODES.COMPARE));
	}

	@Override
	public boolean isExceptionExpected(String queryidentifier)
			throws FrameworkRuntimeException {
		if (isExpectedResultsNeeded()) {

			ResultsHolder expectedResults = getResults(queryidentifier);

			return (expectedResults.getExceptionMsg() == null ? false : true);
		}
		return false;
	}

	@Override
	public String getQuerySetID() {
		return this.querySetIdentifier;
	}

	@Override
	public synchronized File getResultsFile(String queryidentifier)
			throws FrameworkRuntimeException {
		return findExpectedResultsFile(queryidentifier, this.querySetIdentifier);

	}

	private ResultsHolder getResults(String queryidentifier)
			throws FrameworkRuntimeException {
		ResultsHolder rh = null;

		if (!loadedResults.containsKey(queryidentifier)) {
			File er = findExpectedResultsFile(queryidentifier, this.querySetIdentifier);
			rh = loadExpectedResults(er);
			
			loadedResults.put(queryidentifier, rh);
		} else {
			rh = loadedResults.get(queryidentifier);
		}

		return rh;
	}
	
	

	/**
	 * Compare the results of a query with those that were expected.
	 * @param actualTestResults 
	 * @param resultSet 
	 * @param isOrdered 
	 * 
	 * 
	 * @return The response time for comparing the first batch (sizes) of
	 *         resutls.
	 * @throws QueryTestFailedException
	 *             If comparison fails.
	 */
	@Override
	public Object compareResults(final TestResult actualTestResults,
			final ResultSet resultSet, final boolean isOrdered) throws QueryTestFailedException {
	
		final String eMsg = "CompareResults Error: "; //$NON-NLS-1$

		ResultsHolder expectedResults =  getResults(actualTestResults.getQueryID());

		ResultsHolder actualResults;

		switch (actualTestResults.getStatus()) {
		case TestResult.RESULT_STATE.TEST_EXCEPTION:
			throw new QueryTestFailedException(
					eMsg
							+ "Test resulted in unexpected exception " + actualTestResults.getExceptionMsg()); //$NON-NLS-1$

		case TestResult.RESULT_STATE.TEST_EXPECTED_EXCEPTION:

			if (!expectedResults.isException()) {
				// The actual exception was expected, but the expected results
				// was not
				throw new QueryTestFailedException(
						eMsg
								+ "The actual result was an exception, but the Expected results wasn't an exception.  Actual exception: '" //$NON-NLS-1$
								+ actualTestResults.getExceptionMsg() + "'"); //$NON-NLS-1$
			}
			// We got an exception that we expected - convert actual exception
			// to ResultsHolder
			actualResults = new ResultsHolder(TagNames.Elements.EXCEPTION, actualTestResults);
			actualResults.setQueryID(expectedResults.getQueryID());

			actualResults = convertException(actualTestResults.getException(), actualResults);
			actualResults.setExecutionTime(actualTestResults.getExecutionTime());

			compareExceptions(actualResults, expectedResults, eMsg);

			break;

		default:
			// DEBUG:
			// debugOut.println("*** Expected Results (holder): " +
			// expectedResults);
			// DEBUG:
			// debugOut.println("*** Actual Results (ResultSet): " +
			// printResultSet(results));

			// Convert results to ResultsHolder
			actualResults = new ResultsHolder(TagNames.Elements.QUERY_RESULTS, actualTestResults);
			actualResults.setQueryID(expectedResults.getQueryID());
			actualResults.setExecutionTime(actualTestResults.getExecutionTime());

			convertResults(resultSet, actualTestResults.getUpdateCount(), actualResults);

			if (expectedResults.getRows().size() > 0) {
				compareResults(actualResults, expectedResults, eMsg, isOrdered);
			} else if (actualResults.getRows() != null
					&& actualResults.getRows().size() > 0) {
				throw new QueryTestFailedException(
						eMsg
								+ "Expected results indicated no results, but actual shows " + actualResults.getRows().size() + " rows."); //$NON-NLS-1$	      		    		      		    
			}

			// DEBUG:
			// debugOut.println("*** Actual Results (holder): " +
			// actualResults);

			// Compare expected results with actual results, record by record

			break;

		}

		return null;

	}

	/**
	 * The use of REMOVE_PREFIX is a hack to strip down the message so that it matches
	 * what is currently in use.
	 * @param actualException 
	 * @param actualResults 
	 * @return ResultsHolder
	 */
	
	private ResultsHolder convertException(final Throwable actualException,
			final ResultsHolder actualResults) {
		actualResults.setExceptionClassName(actualException.getClass()
				.getName());
		
		actualResults.setExceptionMsg(ExceptionUtil.getExceptionMessage(actualException));
		
		return actualResults;
	}


	/**
	 * Helper to convert results into records and record first batch response
	 * time.
	 * 
	 * @param results
	 * @param batchSize
	 * @param resultsHolder
	 *            Modified - results added by this method.
	 * @return List of sorted results.
	 * @throws QueryTestFailedException
	 *             replaced SQLException.
	 */
	private final long convertResults(final ResultSet results,
			final long batchSize, ResultsHolder resultsHolder)
			throws QueryTestFailedException {

		long firstBatchResponseTime = 0;
		final List<List<Object>> records = new ArrayList<List<Object>>();
		final List<String> columnTypeNames = new ArrayList<String>();
		final List<String> columnTypes = new ArrayList<String>();

		final ResultSetMetaData rsMetadata;
		final int colCount;

		if (results != null) {
			// Get column info
			try {
				rsMetadata = results.getMetaData();
				colCount = rsMetadata.getColumnCount();
				// Read types of all columns
				for (int col = 1; col <= colCount; col++) {
					columnTypeNames.add(rsMetadata.getColumnName(col));
					columnTypes.add(rsMetadata.getColumnTypeName(col));
				}
			} catch (SQLException qre) {
				throw new QueryTestFailedException(
						"Can't get results metadata: " + qre.getMessage()); //$NON-NLS-1$
			}
	
			// Get rows
			try {
				// Read all the rows
				for (int row = 0; results.next(); row++) {
					final List<Object> currentRecord = new ArrayList<Object>(colCount);
					// Read values for this row
					for (int col = 1; col <= colCount; col++) {
						currentRecord.add(results.getObject(col));
					}
					records.add(currentRecord);
					// If this row is the (fetch size - 1)th row, record first batch
					// response time
					if (row == batchSize) {
						firstBatchResponseTime = System.currentTimeMillis();
					}
				}
			} catch (SQLException qre) {
				throw new QueryTestFailedException(
						"Can't get results: " + qre.getMessage()); //$NON-NLS-1$
			}
		}

		// Set info on resultsHolder
		resultsHolder.setRows(records);
		resultsHolder.setIdentifiers(columnTypeNames);
		resultsHolder.setTypes(columnTypes);

		return firstBatchResponseTime;
	}

	/**
	 * Added primarily for public access to the compare code for testing.
	 * 
	 * @param actualResults
	 * @param expectedResults
	 * @param eMsg
	 * @param isOrdered
	 * @throws QueryTestFailedException
	 */
	protected void compareResults(final ResultsHolder actualResults,
			final ResultsHolder expectedResults, final String eMsg,
			boolean isOrdered) throws QueryTestFailedException {
		// if (actualResults.isException() && expectedResults.isException()) {
		// // Compare exceptions
		// compareExceptions(actualResults, expectedResults, eMsg);
		// } else

		// if (actualResults.isResult() && expectedResults.isResult()) {
		// Compare results
		if (isOrdered == false && actualResults.hasRows()
				&& expectedResults.hasRows()) {
			// If the results are not ordered, we can sort both
			// results and expected results to compare record for record
			// Otherwise, actual and expected results are already assumed
			// to be in same order

			// sort the sortedResults in ascending order
			final List actualRows = actualResults.getRows();
			sortRecords(actualRows, true);
			actualResults.setRows(actualRows);

			// sort the expectedResults with ascending order
			final List expectedRows = expectedResults.getRows();
			sortRecords(expectedRows, true);
			expectedResults.setRows(expectedRows);
		}

		compareResultSets(actualResults.getRows(), actualResults.getTypes(),
				actualResults.getIdentifiers(), expectedResults.getRows(),
				expectedResults.getTypes(), expectedResults.getIdentifiers(),
				eMsg);
		
		long a =  actualResults.getExecutionTime();
		long e = expectedResults.getExecutionTime();

		
		if (this.exec_minumin_time > 0 && e > this.exec_minumin_time &&  a > e) {
				double allowediff = e * (exceed_percent / 100);
				ClientPlugin.LOGGER.info("EXEC MIN TIME: " + this.exec_minumin_time + "  EXEC PER: " + exceed_percent + "  expected exec time: " + expectedResults.getExecutionTime());
				ClientPlugin.LOGGER.info("   expected exec time: " + e + "  actual exec time: " + a);
				if ( ( a - allowediff) > e ) {
					String msg = "Actual: " + a + " Expected: " + e + 
					" Diff: " + (a - e) +
					" Allowed %: " + (exceed_percent / 100) + " (" + allowediff + ")";
					QueryTestFailedException f = new QueryTestFailedException(eMsg
							+ msg) ; //$NON-NLS-1$
					
					actualResults.getActualTestResult().setException(f);
					actualResults.getActualTestResult().setExceptionMessage(msg);							
					actualResults.getActualTestResult().setStatus(TestResult.RESULT_STATE.TEST_EXECUTION_TIME_EXCEEDED_EXCEPTION);
				}			
		
		}
	}

	/**
	 * sort one result that is composed of records of all columns
	 * @param records 
	 * @param ascending 
	 */
	private static void sortRecords(List records, boolean ascending) {
		// if record's size == 0, don't need to sort
		if (records.size() != 0) {
			int nFields = ((List) records.get(0)).size();
			int[] params = new int[  ( nFields > 3 ? 3 : nFields ) ];
			for (int k = 0, j = 0; k < params.length; k++, j++) {
				params[j] = k;

			}
			if (nFields > 0) {
				Collections.sort(records, new ListNestedSortComparator(
					params, ascending));
			}
		}
	}

	private void compareExceptions(final ResultsHolder actualResults,
			final ResultsHolder expectedResults, String eMsg)
			throws QueryTestFailedException {

		final String expectedExceptionClass = expectedResults
				.getExceptionClassName();
		final String expectedExceptionMsg = expectedResults.getExceptionMsg()
				.toLowerCase();
		final String actualExceptionClass = actualResults
				.getExceptionClassName();
		final String actualExceptionMsg = actualResults.getExceptionMsg()
				.toLowerCase();

		if (actualExceptionClass == null) {
			// We didn't get an actual exception, we should have
			throw new QueryTestFailedException(eMsg + "Expected exception: " //$NON-NLS-1$
					+ expectedExceptionClass + " but got none."); //$NON-NLS-1$
		}
		// Compare exception classes
		if (!expectedExceptionClass.equals(actualExceptionClass)) {
			throw new QueryTestFailedException(eMsg
					+ "Got wrong exception, expected \"" //$NON-NLS-1$
					+ expectedExceptionClass + "\" but got \"" + //$NON-NLS-1$
					actualExceptionClass + "\""); //$NON-NLS-1$
		}
		// Compare exception messages
		if (expectedResults.isExceptionContains() ) {
			if (actualExceptionMsg.indexOf(expectedExceptionMsg) > -1) {
				throw new QueryTestFailedException(
						eMsg
								+ "Expected exception message " + expectedExceptionMsg + " is not contained in actual exception of " + actualExceptionMsg); //$NON-NLS-1$				
			}
			
		} else if (expectedResults.isExceptionStartsWith()) {
			if (!actualExceptionMsg.startsWith(expectedExceptionMsg)) {
				throw new QueryTestFailedException(
						eMsg
								+ "Actual exception message " + actualExceptionMsg + " does not start with the expected exception of " + expectedExceptionMsg); //$NON-NLS-1$				
			}			
		} else {
			if (!expectedExceptionMsg.equals(actualExceptionMsg)) {

				// Give it another chance by comparing w/o line separators
				if (!compareStrTokens(expectedExceptionMsg, actualExceptionMsg)) {
					throw new QueryTestFailedException(
							eMsg
									+ "Got expected exception but with wrong message. Got " + actualExceptionMsg); //$NON-NLS-1$
				}
			}
		}
	}

	private boolean compareStrTokens(String expectedStr, String gotStr) {
		String[] expectedTokens = StringUtils.split(expectedStr, newline);
		String[] gotTokens = StringUtils.split(gotStr, newline);
		
		for (int i = 0; i < expectedTokens.length; i++) {
			String expected = expectedTokens[i];
			String got = gotTokens[i];
			if (!expected.equals(got)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compare actual results, identifiers and types with expected. <br>
	 * <strong>Note </strong>: result list are expected to match element for
	 * element.</br>
	 * 
	 * @param actualResults
	 * @param actualDatatypes
	 * @param actualIdentifiers
	 * @param expectedResults
	 * @param expectedDatatypes
	 * @param expectedIdentifiers
	 * @param eMsg
	 * @throws QueryTestFailedException
	 *             If comparison fails.
	 */
	protected void compareResultSets(final List actualResults,
			final List actualDatatypes, final List actualIdentifiers,
			final List expectedResults, final List expectedDatatypes,
			final List expectedIdentifiers, final String eMsg)
			throws QueryTestFailedException {
		// Compare column names and types
		compareIdentifiers(actualIdentifiers, expectedIdentifiers,
				actualDatatypes, expectedDatatypes);

		// Walk through records and compare actual against expected
		final int actualRowCount = actualResults.size();
		final int expectedRowCount = expectedResults.size();
		final int actualColumnCount = actualIdentifiers.size();

		// Check for less records than in expected results
		if (actualRowCount < expectedRowCount) {
			throw new QueryTestFailedException(eMsg
					+ "Expected " + expectedRowCount + //$NON-NLS-1$
					" records but received only " + actualRowCount); //$NON-NLS-1$
		} else if (actualRowCount > expectedRowCount) {
			// Check also for more records than expected
			throw new QueryTestFailedException(eMsg
					+ "Expected " + expectedRowCount + //$NON-NLS-1$
					" records but received " + actualRowCount); //$NON-NLS-1$
		}

		// DEBUG:
		// debugOut.println("================== Compariing Rows ===================");

		// Loop through rows
		for (int row = 0; row < actualRowCount; row++) {

			// Get actual record
			final List actualRecord = (List) actualResults.get(row);

			// Get expected record
			final List expectedRecord = (List) expectedResults.get(row);

			// DEBUG:
			// debugOut.println("Row: " + (row + 1));
			// debugOut.println(" expectedRecord: " + expectedRecord);
			// debugOut.println(" actualRecord: " + actualRecord);
			// Loop through columns
			// Compare actual elements with expected elements column by column
			// in this row
			for (int col = 0; col < actualColumnCount; col++) {
				// Get actual value
				Object actualValue = actualRecord.get(col);
				// Get expected value
				Object expectedValue = expectedRecord.get(col);

				// DEBUG:
				// debugOut.println(" Col: " +(col +1) + ": expectedValue:[" +
				// expectedValue + "] actualValue:[" + actualValue +
				// "]");

				// Compare these values
				if ((expectedValue == null && actualValue != null)
						|| (actualValue == null && expectedValue != null)) {
					// Compare nulls
					throw new QueryTestFailedException(
							eMsg + "Value mismatch at row " + (row + 1) //$NON-NLS-1$
									+ " and column " + (col + 1) //$NON-NLS-1$
									+ ": expected = [" //$NON-NLS-1$
									+ (expectedValue != null ? expectedValue
											: "null") + "], actual = [" //$NON-NLS-1$
									+ (actualValue != null ? actualValue
											: "null") + "]"); //$NON-NLS-1$

				}

				if (expectedValue == null && actualValue == null) {
					continue;
				}

				if (actualValue instanceof Blob || actualValue instanceof Clob
						|| actualValue instanceof SQLXML) {

					if (actualValue instanceof Clob) {
						Clob c = (Clob) actualValue;
						try {
							actualValue = ObjectConverterUtil.convertToString(c
									.getAsciiStream());

						} catch (Throwable e) {
							// TODO Auto-generated catch block
							throw new QueryTestFailedException(e);
						}
					} else if (actualValue instanceof Blob) {
						Blob b = (Blob) actualValue;
						try {
							byte[] ba = ObjectConverterUtil
									.convertToByteArray(b.getBinaryStream());

							actualValue = String.valueOf(ba.length);

							// actualValue =
							// ObjectConverterUtil.convertToString(b.getBinaryStream());

						} catch (Throwable e) {
							// TODO Auto-generated catch block
							throw new QueryTestFailedException(e);
						}
					} else if (actualValue instanceof SQLXML) {
						SQLXML s = (SQLXML) actualValue;
						try {
							actualValue = ObjectConverterUtil.convertToString(s
									.getBinaryStream());

						} catch (Throwable e) {
							// TODO Auto-generated catch block
							throw new QueryTestFailedException(e);
						}
					}

					if (!(expectedValue instanceof String)) {
						expectedValue = expectedValue.toString();
					}
				}

				// Compare values with equals
				if (!expectedValue.equals(actualValue)) {
					// DEBUG:
					

					if (expectedValue instanceof java.sql.Date) {
						expectedValue = expectedValue.toString();
						actualValue = actualValue.toString();
						
					} else if (expectedValue instanceof java.sql.Time) {
						expectedValue = expectedValue.toString();
						actualValue = actualValue.toString();
						
					}

					if (expectedValue instanceof String) {
						final String expectedString = (String) expectedValue;

						if (!(actualValue instanceof String)) {
							throw new QueryTestFailedException(eMsg
									+ "Value (types) mismatch at row " + (row + 1) //$NON-NLS-1$
									+ " and column " + (col + 1) //$NON-NLS-1$
									+ ": expected = [" //$NON-NLS-1$
									+ expectedValue + ", (String) ], actual = [" //$NON-NLS-1$
									+ actualValue + ", (" + actualValue.getClass().getName()+ ") ]"); //$NON-NLS-1$
						} 
											
							// Check for String difference
							assertStringsMatch(  expectedString,
									(String) actualValue, (row + 1), (col + 1),
									eMsg);

					} else {

						throw new QueryTestFailedException(eMsg
								+ "Value mismatch at row " + (row + 1) //$NON-NLS-1$
								+ " and column " + (col + 1) //$NON-NLS-1$
								+ ": expected = [" //$NON-NLS-1$
								+ expectedValue + "], actual = [" //$NON-NLS-1$
								+ actualValue + "]"); //$NON-NLS-1$

					}
				}

			} // end loop through columns
		} // end loop through rows
	}

	protected void compareIdentifiers(List actualIdentifiers,
			List expectedIdentifiers, List actualDataTypes,
			List expectedDatatypes) throws QueryTestFailedException {

		// Check sizes
		if (expectedIdentifiers.size() != actualIdentifiers.size()) {
			throw new QueryTestFailedException(
					"Got incorrect number of columns, expected = " + expectedIdentifiers.size() + ", actual = " //$NON-NLS-1$ //$NON-NLS-2$
							+ actualIdentifiers.size());
		}

		// Compare identifier lists only by short name
		for (int i = 0; i < actualIdentifiers.size(); i++) {
			String actualIdent = (String) actualIdentifiers.get(i);
			String expectedIdent = (String) expectedIdentifiers.get(i);
			String actualType = (String) actualDataTypes.get(i);
			String expectedType = (String) expectedDatatypes.get(i);

			// Get short name for each identifier
			String actualShort = getShortName(actualIdent);
			String expectedShort = getShortName(expectedIdent);

			if (!expectedShort.equalsIgnoreCase(actualShort)) {
				throw new QueryTestFailedException(
						"Got incorrect column name at column " + i + ", expected = " + expectedShort + " but got = " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								+ actualShort);
			}
//			if (actualType.equalsIgnoreCase("xml")) {//$NON-NLS-1$
//				actualType = "string";//$NON-NLS-1$
//			}
//			if (actualType.equalsIgnoreCase("clob")) {//$NON-NLS-1$
//				actualType = "string";//$NON-NLS-1$
//			}

			if (actualType.equalsIgnoreCase("blob")) {
				Class nodeType = (Class) TagNames.TYPE_MAP.get(actualType);
				actualType = nodeType.getSimpleName();
			}
			if (!expectedType.equalsIgnoreCase(actualType)) {
				throw new QueryTestFailedException(
						"Got incorrect column type at column " + i + ", expected = " + expectedType + " but got = " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								+ actualType);
			}
		}
	}

	protected String getShortName(String ident) {
		int index = ident.lastIndexOf("."); //$NON-NLS-1$
		if (index >= 0) {
			return ident.substring(index + 1);
		}
		return ident;
	}

	private static final int MISMATCH_OFFSET = 20;
	private static final int MAX_MESSAGE_SIZE = 50;

	protected void assertStringsMatch(final String expectedStr,
			final String actualStr, final int row, final int col,
			final String eMsg) throws QueryTestFailedException {
		// TODO: Replace stripCR() with XMLUnit comparison for XML results.
		// stripCR() is a workaround for comparing XML Queries
		// that have '\r'.
		String expected = stripCR(expectedStr).trim();
		String actual = stripCR(actualStr).trim();

		String locationText = ""; //$NON-NLS-1$
		int mismatchIndex = -1;

		boolean isequal = Arrays.equals(expected.toCharArray(),
				actual.toCharArray());

		// if (!expected.equals(actual)) {
		if (!isequal) {
			if (expected != null && actual != null) {
				int shortestStringLength = expected.length();
				if (actual.length() < expected.length()) {
					shortestStringLength = actual.length();
				}
				for (int i = 0; i < shortestStringLength; i++) {
					if (expected.charAt(i) != actual.charAt(i)) {
						locationText = "  Strings do not match at character: " + (i + 1) + //$NON-NLS-1$
								". Expected [" + expected.charAt(i)
								+ "] in " + expected + " - but got [" + actual.charAt(i) + "] in " + actual; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						mismatchIndex = i;
						break;
					}
				}
			}

			String expectedPartOfMessage = expected;
			String actualPartOfMessage = actual;
			if (expected.length() + actual.length() > MAX_MESSAGE_SIZE) {
				expectedPartOfMessage = safeSubString(expected, mismatchIndex
						- MISMATCH_OFFSET, mismatchIndex + MISMATCH_OFFSET);
				actualPartOfMessage = safeSubString(actual, mismatchIndex
						- MISMATCH_OFFSET, mismatchIndex + MISMATCH_OFFSET);
			}

			String message = eMsg + " String mismatch at row " + row + //$NON-NLS-1$
					" and column " + col + //$NON-NLS-1$
					". Expected: {0} but was: {1}" + locationText; //$NON-NLS-1$
			message = MessageFormat.format(message, new Object[] {
					expectedPartOfMessage, actualPartOfMessage });
			throw new QueryTestFailedException(message);
		}
	}

	private String safeSubString(String text, int startIndex, int endIndex) {
		String prefix = "...'"; //$NON-NLS-1$
		String suffix = "'..."; //$NON-NLS-1$

		int actualStartIndex = startIndex;
		if (actualStartIndex < 0) {
			actualStartIndex = 0;
			prefix = "'"; //$NON-NLS-1$
		}
		int actualEndIndex = endIndex;
		if (actualEndIndex > text.length() - 1) {
			actualEndIndex = text.length() - 1;
			if (actualEndIndex < 0) {
				actualEndIndex = 0;
			}
		}
		if (actualEndIndex == text.length() - 1 || text.length() == 0) {
			suffix = "'"; //$NON-NLS-1$
		}

		return prefix + text.substring(actualStartIndex, actualEndIndex)
				+ suffix;
	}

	private String stripCR(final String text) {
		if (text.indexOf('\r') >= 0) {
			StringBuffer stripped = new StringBuffer(text.length());
			int len = text.length();
			for (int i = 0; i < len; i++) {
				char current = text.charAt(i);
				if (current != '\r') {
					stripped.append(current);
				}
			}
			return stripped.toString();
		}
		return text;
	}

	private ResultsHolder loadExpectedResults(File resultsFile) {
		XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();
		final ResultsHolder expectedResult;
		try {
			expectedResult = jstrat.parseXMLResultsFile(resultsFile);
		} catch (IOException e) {
			throw new FrameworkRuntimeException(
					"Unable to load expected results: " + e.getMessage()); //$NON-NLS-1$
		} catch (JDOMException e) {
			throw new FrameworkRuntimeException(
					"Unable to load expected results: " + e.getMessage()); //$NON-NLS-1$
		}
		return expectedResult;
	}

	private File findExpectedResultsFile(String queryIdentifier,
			String querySetIdentifier)  {
		String resultFileName = queryIdentifier + ".xml"; //$NON-NLS-1$
		File file = new File(results_dir_loc + File.separator + querySetIdentifier,
				resultFileName);
		if (!file.exists() && this.isExpectedResultsNeeded()) {
			FrameworkRuntimeException fre = new FrameworkRuntimeException("Query results file "
					+ file.getAbsolutePath() + " cannot be found");
			throw fre;
		}

		return file;

	}
}
