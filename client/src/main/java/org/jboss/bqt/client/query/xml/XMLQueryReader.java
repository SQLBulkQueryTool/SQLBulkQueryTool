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

package org.jboss.bqt.client.query.xml;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.QueryReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.client.xml.XMLQueryVisitationStrategy;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.core.util.FileUtils;

public class XMLQueryReader extends QueryReader {

	private String query_dir_loc = null;

	private Map<String, File> querySetIDToFileMap = new HashMap<String, File>();

	public XMLQueryReader(QueryScenario scenario, Properties props) {
		super(scenario, props);
		
		query_dir_loc = props.getProperty(TestProperties.PROP_QUERY_FILES_DIR_LOC);
		if (query_dir_loc == null) {
				BQTUtil.throwInvalidProperty(TestProperties.PROP_QUERY_FILES_DIR_LOC);
		}

		loadQuerySetIDtoFileMap();
	}
	
	

	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.client.api.QueryReader#getQueryFilesLocation()
	 */
	@Override
	public String getQueryFilesLocation() {
		return query_dir_loc;
	}


	@Override
	public List<QueryTest> getQueries(String querySetID) {
		File queryFile = querySetIDToFileMap.get(querySetID);

		try {
			return loadQueries(querySetID, queryFile);
		} catch (IOException e) {
			throw new FrameworkRuntimeException((new StringBuilder())
					.append("Failed to load queries from file: ")
					.append(queryFile).append(" error:").append(e.getMessage())
					.toString());
		}

	}

	@Override
	public Collection<String> getQuerySetIDs() {
		return new HashSet<String>(querySetIDToFileMap.keySet());
	}

	private void loadQuerySetIDtoFileMap() {

		File files[] = BQTUtil.getQuerySetFiles(query_dir_loc);

		for (int i = 0; i < files.length; i++) {
			// Get query set name
			String querySet = getQuerySetName(files[i].getName()); //$NON-NLS-1$
			querySetIDToFileMap.put(querySet, files[i]);
		}

	}

	@SuppressWarnings("unchecked")
	private List<QueryTest> loadQueries(String querySetID, File queryFile)
			throws IOException {

		if (!queryFile.exists() || !queryFile.canRead()) {
			String msg = "Query file doesn't exist or cannot be read: "
					+ queryFile.getName() + ", ignoring and continuing";
			ClientPlugin.LOGGER.error(msg);
			throw new IOException(msg); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Get query set name
		//			String querySet = getQuerySetName(queryFileName) ; //$NON-NLS-1$

		XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();
		List<QueryTest> tests = null;
		try {
			tests = jstrat.parseXMLQueryFile(this.getQueryScenario().getQueryScenarioIdentifier(),
					queryFile, querySetID);
		} catch (Exception e) {
			String msg = "Error reading query file: " + queryFile.getName(); //$NON-NLS-1$ //$NON-NLS-2$
			ClientPlugin.LOGGER.error(e, msg);
			throw new IOException(msg, e); //$NON-NLS-1$ //$NON-NLS-2$
		}			
		// perform logic to test for duplicate queries with the same name
		 Set<String> s = new HashSet<String>();

		 for (QueryTest t : tests)  {
			 if (s.contains(t.getQueryID())) {
						throw new FrameworkRuntimeException((new StringBuilder())
								.append("Duplicate queries with the same name of: ")
								.append(t.getQueryID())
								.toString());
			 }
			 s.add(t.getQueryID());
		 }
		 return tests;

	}

	/* 
	 * Used to parse the file name to get the query set name 
	 */
	private static String getQuerySetName(String queryFileName) {
		// Get query set name
		String querySet =  FileUtils.getFilenameWithoutExtension(queryFileName);
			
//			queryFileName;
//		String[] nameParts = StringUtils.split(queryFileName, "./\\"); //$NON-NLS-1$
//		if (nameParts != null && nameParts.length > 1) {
//			querySet = nameParts[nameParts.length - 2];
//		} 
		return querySet;
	}
}
