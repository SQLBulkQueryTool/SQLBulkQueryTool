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

package org.jboss.bqt.test.client;

import java.util.Collection;
import java.util.List;

import org.jboss.bqt.core.exception.QueryTestFailedException;

/**
 * The QueryReader is responsible for providing a set queries for a given
 * <code>querySetID</code>.
 * 
 * The querySetID identifies a set of queries to executed. The Map structure is
 * as follows:
 * 
 * Map: Key - Value QueryIdentifier SQL Query
 * 
 * 
 * Where: - QueryIdentifier uniquely identifies a sql query in the set. SQL
 * Query is the query to be executed.
 * 
 * @author vanhalbert
 * 
 */
public interface QueryReader extends QueryProperties {

	/**
	 * Return the <code>querySetID</code>s that identifies all the query sets
	 * that are available to execute during this test.
	 * 
	 * @return QuerySetIDs
	 */
	Collection<String> getQuerySetIDs();

	/**
	 * Return a <code>List</code> containing {@link QueryTest}
	 * 
	 * @return List
	 * @throws QueryTestFailedException
	 * 
	 * @since
	 */
	List<QueryTest> getQueries(String querySetID)
			throws QueryTestFailedException;


}
