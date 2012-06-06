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

import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.jboss.bqt.client.ClassFactory;
import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.api.ResultsGenerator;
import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jboss.bqt.framework.ConfigPropertyNames;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests primarily the various cloning scenarios available with PropertiesUtils
 */
public class TestXMLExpectedResults {

	public TestXMLExpectedResults() {

	}

	// ===================================================================
	// ACTUAL TESTS
	// ===================================================================

	/**
	 * Tests {@link org.jboss.bqt.core.util.PropertiesUtils}
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void test1() throws Exception {
		System.setProperty(ConfigPropertyNames.CONFIG_FILE,
				"ctc-bqt-test.properties");

		ConfigPropertyLoader _instance = ConfigPropertyLoader.getInstance();
		Properties p = _instance.getProperties();
		if (p == null || p.isEmpty()) {
			throw new RuntimeException("Failed to load config properties file");

		}

		QueryScenario set = ClassFactory.createQueryScenario("testscenario");

		_instance.setProperty(TestProperties.PROP_QUERY_FILES_ROOT_DIR,
				new File("src/main/resources/").getAbsolutePath());

		Iterator<String> it = set.getQuerySetIDs().iterator();
		while (it.hasNext()) {
			String querySetID = it.next();

			List<QueryTest> queries = set.getQueries(querySetID);
			if (queries.size() == 0l) {
				System.out.println("Failed, didn't load any queries ");
			}

			ExpectedResults er = set.getExpectedResults(querySetID);
			// new XMLExpectedResults(_instance.getProperties(),
			// querySetID);

			ResultsGenerator gr = set.getResultsGenerator();
			// new XMLGenerateResults(_instance.getProperties(), "testname",
			// set.getOutputDirectory());

			Iterator<QueryTest> qIt = queries.iterator();
			while (qIt.hasNext()) {
				QueryTest q = qIt.next();
				// String qId = (String) qIt.next();
				// String sql = (String) queries.get(qId);

				// System.out.println("SetID #: " + cnt + "  Qid: " + qId +
				// "   sql: " + sql);

				File resultsFile = er.getResultsFile(q.getQueryID());
				if (resultsFile == null) {
					System.out
							.println("Failed to get results file for queryID "
									+ q.getQueryID());
				}

			}

		}

		System.out.println("Completed Test");

	}

}
