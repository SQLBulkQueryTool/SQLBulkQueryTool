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
package org.jboss.bqt.client;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.client.api.QueryReader;
import org.jboss.bqt.client.api.QueryScenario;
import org.jboss.bqt.client.api.ResultsGenerator;
import org.jboss.bqt.client.xml.XMLExpectedResults;
import org.jboss.bqt.client.xml.XMLGenerateResults;
import org.jboss.bqt.client.xml.XMLQueryReader;
import org.jboss.bqt.client.xml.XMLQueryScenario;
import org.jboss.bqt.core.util.ReflectionHelper;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.ConfigPropertyLoader;

/**
 *  The ClassFactory is responsible for creating the framework implementations to be used in executing
 *  the query tests.    The default implementations are from the {@link org.jboss.bqt.client.xml Xmlpackage}.
 */
public class ClassFactory {

	/**
	 * The {@link #QUERY_SCENARIO_CLASSNAME} property indicates the
	 * implementation of {@link QueryScenario} to use.
	 */
	public static final String QUERY_SCENARIO_CLASSNAME = "query.scenario.classname"; //$NON-NLS-1$

	/**
	 * The default scenario class to use when {@link #QUERY_SCENARIO_CLASSNAME}
	 * is not defined.
	 */

	// use xml expected results format
	public static final String QUERY_SCENARIO_DEFAULT_CLASSNAME = XMLQueryScenario.class.getName(); //$NON-NLS-1$
	
	/**
	 * The {@link #QUERY_READER_CLASSNAME} property indicates the implementation
	 * of {@link QueryReader} to use.
	 */
	public static final String QUERY_READER_CLASSNAME = "query.reader.classname"; //$NON-NLS-1$

	/**
	 * The default query reader class to use when
	 * {@link #QUERY_READER_CLASSNAME} is not defined.
	 */

	public static final String QUERY_READER_DEFAULT_CLASSNAME = XMLQueryReader.class.getName(); //$NON-NLS-1$

	/**
	 * The {@link #RESULTS_GENERATOR_CLASSNAME} property indicates the
	 * implementation of {@link ResultsGenerator} to use.
	 */
	public static final String RESULTS_GENERATOR_CLASSNAME = "results.generator.classname"; //$NON-NLS-1$

	/**
	 * The default query reader class to use when
	 * {@link #QUERY_READER_CLASSNAME} is not defined.
	 */

	public static final String RESULTS_GENERATOR_DEFAULT_CLASSNAME = XMLGenerateResults.class.getName(); //$NON-NLS-1$

	/**
	 * The {@link #EXPECTED_RESULTS_CLASSNAME} property indicates the
	 * implementation of {@link ExpectedResults} to use.
	 */
	public static final String EXPECTED_RESULTS_CLASSNAME = "expected.results.classname"; //$NON-NLS-1$

	/**
	 * The default query reader class to use when
	 * {@link #EXPECTED_RESULTS_CLASSNAME} is not defined.
	 */

	public static final String EXPECTED_RESULTS_DEFAULT_CLASSNAME = XMLExpectedResults.class.getName(); //$NON-NLS-1$

	public static QueryScenario createQueryScenario(String scenarioName) throws FrameworkException {

		String clzzname = ConfigPropertyLoader.getInstance().getProperty(
				QUERY_SCENARIO_CLASSNAME);
		if (clzzname == null || clzzname.trim().length() == 0 || clzzname.startsWith("${")) {
			clzzname = QUERY_SCENARIO_DEFAULT_CLASSNAME;
		}

		Collection<Object> args = new ArrayList<Object>(2);
		args.add(scenarioName);
		args.add(ConfigPropertyLoader.getInstance().getProperties());

		QueryScenario scenario;
		try {
			scenario = (QueryScenario) ReflectionHelper.create(clzzname, args,
					null);
		} catch (FrameworkException fe) {
			throw fe;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkRuntimeException(e);
		}
		return scenario;
	}

	public static QueryReader createQueryReader(Collection<?> args) {
		String clzzname = ConfigPropertyLoader.getInstance().getProperty(
				QUERY_READER_CLASSNAME);
		if (clzzname == null) {
			clzzname = QUERY_READER_DEFAULT_CLASSNAME;
		}

		QueryReader reader;
		try {
			reader = (QueryReader) ReflectionHelper
					.create(clzzname, args, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkRuntimeException(e);
		}

		return reader;
	}

	public static ResultsGenerator createResultsGenerator(Collection<?> args) {
		String clzzname = ConfigPropertyLoader.getInstance().getProperty(
				RESULTS_GENERATOR_CLASSNAME);
		if (clzzname == null) {
			clzzname = RESULTS_GENERATOR_DEFAULT_CLASSNAME;
		}

		ResultsGenerator resultsgen;
		try {
			resultsgen = (ResultsGenerator) ReflectionHelper.create(clzzname,
					args, null);
		} catch (Exception e) {
			throw new FrameworkRuntimeException(e.getMessage());
		}

		return resultsgen;
	}

	public static ExpectedResults createExpectedResults(Collection<?> args) {
		String clzzname = ConfigPropertyLoader.getInstance().getProperty(
				EXPECTED_RESULTS_CLASSNAME);
		if (clzzname == null) {
			clzzname = EXPECTED_RESULTS_DEFAULT_CLASSNAME;
		}

		ExpectedResults expResults;
		try {
			expResults = (ExpectedResults) ReflectionHelper.create(clzzname,
					args, null);
		} catch (Exception e) {
			throw new FrameworkRuntimeException(e.getMessage());
		}

		return expResults;
	}
}
