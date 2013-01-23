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
package org.jboss.bqt.framework.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.ResultSet;

import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.core.util.ArgCheck;
import org.jboss.bqt.framework.resultsreaders.ResultSetReader;

/**
 * @author vhalbert
 *
 */
public class PrintResults {
	public static final String DELIMITER = "    "; //$NON-NLS-1$ 
	

	public static void print(String msg) {
		System.out.println(msg);
	}

	public static void print(Throwable e) {
		e.printStackTrace();
	}
	

	public static void printResults(ResultSet results) {
		printResults(results, false);
	}	

	public static void printResults(ResultSet results, boolean comparePrint) {
		if (results == null) {
			System.out.println("ResultSet is null"); //$NON-NLS-1$
			return;
		}
		int row;
		try {
			row = -1;
			BufferedReader in = new BufferedReader(new ResultSetReader(results,
					DELIMITER));
			String line = in.readLine();
			while (line != null) {
				row++;
				if (comparePrint) {
					line = line.replaceAll("\"", "\\\\\""); //$NON-NLS-1$ //$NON-NLS-2$
					System.out.println("\"" + line + "\","); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					System.out.println(line);
				}
				line = in.readLine();
			}
			System.out.println("Fetched " + row + " rows\n"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			throw new TransactionRuntimeException(e);
		}
	}	

}
