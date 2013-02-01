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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.xml.JdomHelper;
import org.teiid.core.util.FileUtils;

/**
 * TestResultSetUtil was built in order to override the
 * {@link #printThrowable(Throwable, String, PrintStream)} method in order to call
 * out.print instead of out.println This is because the println adds a line
 * terminator, and when the result file is in turn used for comparison it fails
 * because of the line terminator.
 * 
 * @since
 */
public class TeiidUtil {

	public static final int DEFAULT_MAX_COL_WIDTH = 29;
	private static final String NULL = "<null>"; //$NON-NLS-1$
	private static final String MORE = "$ ";

	public static List compareThrowable(Throwable t, String query,
			File expectedResultsFile, boolean printToConsole)
			throws IOException, SQLException {
		BufferedReader expectedResultsReader = null;
		if (expectedResultsFile != null && expectedResultsFile.exists()
				&& expectedResultsFile.canRead()) {
			expectedResultsReader = new BufferedReader(new FileReader(
					expectedResultsFile));
		}

		PrintStream out = TeiidUtil.getPrintStream(null,
				expectedResultsReader, printToConsole ? System.out : null);

		printThrowable(t, query, out);
		return TeiidUtil.getUnequalLines(out);
	}
	
	
	
	public static void printResults(String result, String sql, File outputFile) throws FrameworkException {

		try {		
			
	//		JdomHelper.write(result, outputFile.getAbsolutePath());
			FileUtils.write(result.getBytes(), outputFile);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		} 		

	}

	public static void printThrowable(Throwable t, String sql, PrintStream out) {
		out.println(sql);

		Throwable answer = t;
		if (t instanceof SQLException) {
			SQLException sqle = (SQLException) t;
			SQLException se = sqle.getNextException();
			if (se != null) {
				SQLException s = null;
				while ((s = se.getNextException()) != null) {
					se = s;
				}

				answer = se;
			}
		} 

		out.print(t.getClass().getName() + " : " + answer.getMessage()); //$NON-NLS-1$

	}

	/**
	 * Gets a PrintStream implementation that uses the input parameters as
	 * underlying streams
	 * 
	 * @param resultsOutput
	 *            an output file for result data. If null, results will only be
	 *            written to the defaul stream.
	 * @param expectedResultsInput
	 *            the reader for expected data. If null, actual data is never
	 *            compared against expected results.
	 * @param defaultPrintStream
	 *            if not null, this utility will always write to this stream.
	 *            Typically this is System.out
	 * @return the single PrintStream that wraps all the input streams for
	 *         writing and comparison.
	 * @since 4.2
	 */
	public static PrintStream getPrintStream(OutputStream resultsOutput,
			BufferedReader expectedResultsInput, PrintStream defaultPrintStream) {
		PrintStream out = null;
		if (defaultPrintStream == null) {
			defaultPrintStream = new PrintStream(new OutputStream() {
				public void write(int b) throws IOException {
				}
			});
		}
		if (resultsOutput == null && expectedResultsInput == null) {
			out = defaultPrintStream;
		} else if (resultsOutput == null && expectedResultsInput != null) {
			out = new ComparingPrintStream(defaultPrintStream,
					expectedResultsInput);
		} else if (resultsOutput != null && expectedResultsInput == null) {
			PrintStream filePrintStream = new PrintStream(resultsOutput);
			out = new MuxingPrintStream(new PrintStream[] { defaultPrintStream,
					filePrintStream });
		} else {
			PrintStream filePrintStream = new PrintStream(resultsOutput);
			out = new ComparingPrintStream(new MuxingPrintStream(
					new PrintStream[] { defaultPrintStream, filePrintStream }),
					expectedResultsInput);
		}
		return out;
	}
	
	public static List compareToResults(String results, 
			File resultsFile, String query, File expectedResultsFile, boolean printToConsole) throws IOException {

		FileOutputStream resultsOutputStream = null;
		if (resultsFile != null) {
			resultsOutputStream = new FileOutputStream(resultsFile);
		}
		
		BufferedReader expectedResultsReader = null;
		if (expectedResultsFile != null && expectedResultsFile.exists()
				&& expectedResultsFile.canRead()) {
			expectedResultsReader = new BufferedReader(new FileReader(
					expectedResultsFile));
		}
		PrintStream out = getPrintStream(resultsOutputStream, expectedResultsReader,
				printToConsole ? System.out : null);

		/**
		 * @see printResults(String, String, File)
		 * because the out.println order must match so that, all things equal, they will match up when compared
		 */
		out.println(results);

		return getUnequalLines(out);
	}

	public static List getUnequalLines(PrintStream out) {
		if (out instanceof ComparingPrintStream) {
			return ((ComparingPrintStream) out).getUnequalLines();
		}
		return Collections.EMPTY_LIST;
	}

	private static String resizeString(Object obj, int size) {
		if (obj == null) {
			return resizeString(NULL, size); //$NON-NLS-1$
		}
		String str = obj.toString();
		if (str.length() == size) {
			return str;
		} else if (str.length() < size) {
			return pad(str, size - str.length());
		} else {
			return str.substring(0, size) + MORE;
		}
	}

	private static String pad(String str, int padding) {
		StringBuffer buf = new StringBuffer(str);
		for (int i = 0; i < padding; i++) {
			buf.append(' ');
		}
		return buf.toString();
	}

	/**
	 * Used to write the same data to more than one output stream.
	 * 
	 * @since 4.2
	 */
	private static final class MuxingPrintStream extends PrintStream {
		private PrintStream[] streams;

		private MuxingPrintStream(PrintStream[] streams) {
			super(streams[0]);
			this.streams = new PrintStream[streams.length];
			System.arraycopy(streams, 0, this.streams, 0, streams.length);
		}

		public void close() {
			for (int i = 0; i < streams.length; i++) {
				streams[i].close();
			}
		}

		public void flush() {
			for (int i = 0; i < streams.length; i++) {
				streams[i].close();
			}
		}

		public void print(boolean b) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].print(b);
			}
		}

		public void print(char c) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].print(c);
			}
		}

		public void print(char[] s) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].print(s);
			}
		}

		public void print(double d) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].print(d);
			}
		}

		public void print(float f) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].print(f);
			}
		}

		public void print(int b) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].print(b);
			}
		}

		public void print(long l) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].print(l);
			}
		}

		public void print(Object obj) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].print(obj);
			}
		}

		public void print(String s) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].print(s);
			}
		}

		public void println() {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println();
			}
		}

		public void println(boolean x) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println(x);
			}
		}

		public void println(char x) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println(x);
			}
		}

		public void println(char[] x) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println(x);
			}
		}

		public void println(double x) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println(x);
			}
		}

		public void println(float x) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println(x);
			}
		}

		public void println(int x) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println(x);
			}
		}

		public void println(long x) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println(x);
			}
		}

		public void println(Object x) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println(x);
			}
		}

		public void println(String x) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].println(x);
			}
		}

		public void write(byte[] buf, int off, int len) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].write(buf, off, len);
			}
		}

		public void write(int b) {
			for (int i = 0; i < streams.length; i++) {
				streams[i].write(b);
			}
		}

		public void write(byte[] b) throws IOException {
			for (int i = 0; i < streams.length; i++) {
				streams[i].write(b);
			}
		}
	}

	/**
	 * Used to compare (per line) the data being written to the output stream
	 * with some expected data read from an input stream
	 * 
	 * @since 4.2
	 */
	private static final class ComparingPrintStream extends PrintStream {
		private ByteArrayOutputStream byteStream = new ByteArrayOutputStream(
				2048);
		private PrintStream buf = new PrintStream(byteStream);
		private BufferedReader in;
		private int line = 0;
		
		
//			    new InputStreamReader( 
//			        new ByteArrayInputStream(writeString.getBytes()),
//			        "UTF-8"),
//			    1024);

		private ArrayList unequalLines = new ArrayList();

		private ComparingPrintStream(OutputStream out, BufferedReader in) {
			super(out);
			this.in = in;
		}

		public void print(boolean b) {
			super.print(b);
			buf.print(b);
		}

		public void print(char c) {
			super.print(c);
			buf.print(c);
		}

		public void print(char[] s) {
			super.print(s);
			buf.print(s);
		}

		public void print(double d) {
			super.print(d);
			buf.print(d);
		}

		public void print(float f) {
			super.print(f);
			buf.print(f);
		}

		public void print(int i) {
			super.print(i);
			buf.print(i);
		}

		public void print(long l) {
			super.print(l);
			buf.print(l);
		}

		public void print(Object obj) {
			super.print(obj);
			buf.print(obj);
		}

		public void print(String s) {
			super.print(s);
			buf.print(s);
		}

		public void println() {
			super.println();
			compareLines();
		}

		public void println(boolean x) {
			super.println(x);
			compareLines();
		}

		public void println(char x) {
			super.println(x);
			compareLines();
		}

		public void println(char[] x) {
			super.println(x);
			compareLines();
		}

		public void println(double x) {
			super.println(x);
			compareLines();
		}

		public void println(float x) {
			super.println(x);
			compareLines();
		}

		public void println(int x) {
			super.println(x);
			compareLines();
		}

		public void println(long x) {
			super.println(x);
			compareLines();
		}

		public void println(Object x) {
			super.println(x);
			compareLines();
		}

		public void println(String x) {
			super.println(x);
			compareLines();
		}
		
		private static final String NODE_PROCESS_TIME = "<value>Node Process Time";
		private static final String NODE_CUMULATIVE_PROCESS_TIME = "<value>Node Cumulative Process Time";
		private static final String NODE_CUMULATIVE_BATCH_PROCESS_TIME = "<value>Node Cumulative Next Batch Process Time";
		

		private void compareLines() {
			buf.flush();
			String bufferedLine = byteStream.toString();
			
			BufferedReader br = new BufferedReader(new StringReader(bufferedLine));
			try {
				byteStream.reset();
		
				String rl = br.readLine();
				while (rl != null) {
					
/*  The following lines are not compared due to could change between every execution and are meaningless if 
 *  change by a millisecond  
 			        <value>Node Process Time: 123</value>
			        <value>Node Cumulative Process Time: 123</value>
			        <value>Node Cumulative Next Batch Process Time: 0</value>
*/
					
					line++;
				
					String expectedLine = in.readLine();
										
					if (!rl.equals(expectedLine)) {
				// only if they dont compare, then check to see if they need bypassing
				// because once test are setup to run in batches, the number of failures should be fewer
						
						if (rl.indexOf(NODE_PROCESS_TIME) >= 0 && expectedLine.indexOf(NODE_PROCESS_TIME) >= 0) {
						} else if (rl.indexOf(NODE_CUMULATIVE_PROCESS_TIME) >= 0 && expectedLine.indexOf(NODE_CUMULATIVE_PROCESS_TIME) >= 0) {
						} else if (rl.indexOf(NODE_CUMULATIVE_BATCH_PROCESS_TIME) >= 0 && expectedLine.indexOf(NODE_CUMULATIVE_BATCH_PROCESS_TIME) >= 0) {
						} else {
							
							unequalLines.add("\n[" + new Integer(line) + "] "
									+ rl);
						}
					}
					
					rl = br.readLine();	
				}
			} catch (IOException e) {
				
			} finally {
				try {
					br.close();
				} catch (IOException e) {
				}
			}

		}

		public List getUnequalLines() {
			return unequalLines;
		}
	}

}
