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
import java.util.Properties;

import org.jboss.bqt.client.ClientPlugin;
import org.jboss.bqt.client.QuerySQL;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.QueryWriter;
import org.jboss.bqt.client.util.BQTUtil;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.util.FileUtils;
import org.jboss.bqt.core.util.StringUtil;
import org.jboss.bqt.core.xml.JdomHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class XMLQueryWriter implements QueryWriter {

	private Properties props = null;
	private String queryScenarioIdentifier;
	
	private String queryFileDir = null;

	public XMLQueryWriter(String queryScenarioID, Properties properties) {
		this.props = properties;
		this.queryScenarioIdentifier = queryScenarioID;
		init();
	}	
	
	private void init()  {	
		queryFileDir = this.props.getProperty(TestProperties.PROP_SQL_DIR);
		if (queryFileDir == null) {
			BQTUtil.throwInvalidProperty(TestProperties.PROP_SQL_DIR);
		}
		File d = new File(queryFileDir);
		if (d.exists()) {
			FileUtils.removeDirectoryAndChildren(d);
		}
		if (!d.exists()) {
			d.mkdirs();
		}
				
		
		ClientPlugin.LOGGER.debug("Directory to write queries to " + queryFileDir);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.client.api.QueryWriter#getSQlFileOutputLocation()
	 */
	public String getSQlFileOutputLocation() {
		return this.queryFileDir;
	}


	public void writeQueryTest(QueryTest tests) throws Exception {
		OutputStream outputStream=null;
		try {
			
			File targetDir = new File(queryFileDir);
			targetDir.mkdirs();
			File f = new File(targetDir, this.queryScenarioIdentifier + ".xml");
			
			ClientPlugin.LOGGER.info("XMLQueryWriter: Writing query file: " + f.getAbsolutePath());

			FileOutputStream fos = new FileOutputStream(f);
			outputStream = new BufferedOutputStream(fos);


		} catch (Exception e) {
		    e.printStackTrace();
		    throw new RuntimeException(e);
		} 
		
		try {

			// Create root JDOM element
			Element rootElement = new Element(TagNames.Elements.ROOT_ELEMENT);
			
			QuerySQL[] qsqls = tests.getQueries();
			for (int i=0; i<qsqls.length; i++) {
				QuerySQL qsql = qsqls[i];
				
				Element queryElement = new Element(TagNames.Elements.QUERY);
				queryElement.addContent(qsql.getSql());
				queryElement.setAttribute("name", "Query" + (i+1));
				rootElement.addContent(queryElement);
			}

			// Output xml
			XMLOutputter outputter = new XMLOutputter(JdomHelper.getFormat(
					"  ", true)); //$NON-NLS-1$
			outputter.output(new Document(rootElement), outputStream);

		} catch (Throwable e) {
			throw new QueryTestFailedException(
					"Failed to convert sql queries to JDOM: " + StringUtil.getStackTrace(e)); //$NON-NLS-1$
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
		ClientPlugin.LOGGER.debug("XMLQueryWriter: Completed writing query file");

		
	}




//	private static String getQuerySetName(String queryFileName) {
//		// Get query set name
//		String querySet = queryFileName;
//		List<String> nameParts = StringUtil.split(querySet, "./\\"); //$NON-NLS-1$
//		if (nameParts.size() > 1) {
//			querySet = (String) nameParts.get(nameParts.size() - 2);
//		}
//		return querySet;
//	}

//	public static void main(String[] args) {
//		System.setProperty(ConfigPropertyNames.CONFIG_FILE,
//				"ctc-bqt-test.properties");
//
//		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
//		Properties p = _instance.getProperties();
//		if (p == null || p.isEmpty()) {
//			throw new RuntimeException("Failed to load config properties file");
//
//		}
//
//		_instance.setProperty(PROP_QUERY_FILES_ROOT_DIR, new File(
//				"src/main/resources/").getAbsolutePath());
//
//		try {
//			XMLQueryWriter reader = new XMLQueryWriter("scenario_id",
//					_instance.getProperties());
//			Iterator<String> it = reader.getQuerySetIDs().iterator();
//			while (it.hasNext()) {
//				String querySetID = it.next();
//
//				List<QueryTest> queries = reader.getQueries(querySetID);
//
//				if (queries.size() == 0l) {
//					System.out.println("Failed, didn't load any queries ");
//				}
//			}
//		} catch (QueryTestFailedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

}
