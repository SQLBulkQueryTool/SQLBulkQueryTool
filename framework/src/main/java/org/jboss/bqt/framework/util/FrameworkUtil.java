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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author vhalbert
 *
 */
public class FrameworkUtil {
	
	public static void writeResultSet(File expected, BufferedReader resultReader, boolean actual)
			throws IOException {
		if (actual) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(expected));
			String s = null;
			while ((s = resultReader.readLine()) != null) {
				bw.write(s);
				bw.write("\n"); //$NON-NLS-1$
			}
			bw.close();
		}
	}
	

	public static String read(BufferedReader r, boolean casesensitive)
			throws IOException {
		StringBuffer result = new StringBuffer();
		String s = null;
		try {
			while ((s = r.readLine()) != null) {
				result.append((casesensitive ? s.trim() : s.trim()
						.toLowerCase()));
				result.append("\n"); //$NON-NLS-1$
			}
		} finally {
			r.close();
		}
		return result.toString();
	}

}
