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

package org.jboss.bqt.client.api;

import java.util.Collection;
import java.util.List;

import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;
import org.jboss.bqt.core.exception.QueryTestFailedException;

/**
 * The QueryReader is responsible for providing a set queries for a given
 * <code>querySetID</code>.
 * 
 * The querySetID identifies a list of {@link QueryTest test} to be executed.
 * 
 * See {@link TestProperties#QUERY_SET_NAME querySet} and {@link TestProperties#PROP_QUERY_FILES_DIR_LOC queryDir} 
 * for properties that define where the query sets can be found.
 * 
 * @author vanhalbert
 * 
 */
public interface QueryReader  {
	
	/**
	 * Returns the full path to the query files location.
	 * @return String full directory path
	 * 
	 * @see TestProperties#PROP_QUERY_FILES_DIR_LOC
	 */
	String getQueryFilesLocation();

	/**
	 * Return the <code>querySetID</code>s that identifies all the query sets
	 * that are available to execute during this test.
	 * 
	 * @return QuerySetIDs
	 */
	Collection<String> getQuerySetIDs();

	/**
	 * Return a <code>List</code> containing {@link QueryTest}
	 * @param querySetID 
	 * @return List
	 * @throws QueryTestFailedException
	 * 
	 * @since
	 */
	List<QueryTest> getQueries(String querySetID)
			throws QueryTestFailedException;


}
