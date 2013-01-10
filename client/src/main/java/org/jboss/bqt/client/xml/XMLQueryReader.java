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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.QueryReader;
import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;

import org.apache.commons.lang.StringUtils;

public class XMLQueryReader implements QueryReader {

	private Properties props = null;
	private String queryScenarioIdentifier;
	private String query_dir_loc = null;

	private Map<String, String> querySetIDToFileMap = new HashMap<String, String>();

	public XMLQueryReader(String queryScenarioID, Properties properties) {
		this.props = properties;
		this.queryScenarioIdentifier = queryScenarioID;
		
		query_dir_loc = props.getProperty(TestProperties.PROP_QUERY_FILES_DIR_LOC);
		if (query_dir_loc == null) {
				BQTUtil.throwInvalidProperty(TestProperties.PROP_QUERY_FILES_DIR_LOC);
		}

		loadQuerySets();
	}
	
	

	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.client.api.QueryReader#getQueryFilesLocation()
	 */
	public String getQueryFilesLocation() {
		return query_dir_loc;
	}


	public List<QueryTest> getQueries(String querySetID) {
		String queryFile = querySetIDToFileMap.get(querySetID);

		try {
			return loadQueries(querySetID, queryFile);
		} catch (IOException e) {
			throw new FrameworkRuntimeException((new StringBuilder())
					.append("Failed to load queries from file: ")
					.append(queryFile).append(" error:").append(e.getMessage())
					.toString());
		}

	}

	public Collection<String> getQuerySetIDs() {
		return new HashSet<String>(querySetIDToFileMap.keySet());
	}

	private void loadQuerySets() {

		File files[] = BQTUtil.getQuerySetFiles(query_dir_loc);

		for (int i = 0; i < files.length; i++) {
			String queryfile = files[i].getAbsolutePath();
			// Get query set name
			String querySet = getQuerySetName(queryfile); //$NON-NLS-1$
			querySetIDToFileMap.put(querySet, queryfile);
			// queryFiles.add(files[i].getAbsolutePath());
		}

	}

	@SuppressWarnings("unchecked")
	private List<QueryTest> loadQueries(String querySetID, String queryFileName)
			throws IOException {

		File queryFile = new File(queryFileName);
		if (!queryFile.exists() || !queryFile.canRead()) {
			String msg = "Query file doesn't exist or cannot be read: "
					+ queryFileName + ", ignoring and continuing";
			ClientPlugin.LOGGER.error(msg);
			throw new IOException(msg); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Get query set name
		//			String querySet = getQuerySetName(queryFileName) ; //$NON-NLS-1$

		XMLQueryVisitationStrategy jstrat = new XMLQueryVisitationStrategy();
		try {
			return jstrat.parseXMLQueryFile(this.queryScenarioIdentifier,
					queryFile, querySetID);
			// Iterator iter = queryMap.keySet().iterator();
			// while (iter.hasNext()) {
			// String queryID = (String) iter.next();
			// String query = (String) queryMap.get(queryID);
			//
			// String uniqueID = querySetID + "_" + queryID;
			// queries.put(uniqueID, query);
			// }

		} catch (Exception e) {
			String msg = "Error reading query file: " + queryFileName; //$NON-NLS-1$ //$NON-NLS-2$
			ClientPlugin.LOGGER.error(e, msg);
			throw new IOException(msg, e); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	private static String getQuerySetName(String queryFileName) {
		// Get query set name
		String querySet = queryFileName;
		String[] nameParts = StringUtils.split(querySet, "./\\"); //$NON-NLS-1$
		if (nameParts != null && nameParts.length > 1) {
			querySet = nameParts[nameParts.length - 2];
		} 
		return querySet;
	}
}
