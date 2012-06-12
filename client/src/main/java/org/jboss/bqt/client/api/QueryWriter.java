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

import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.TestProperties;

/**
 * The QueryWriter is responsible for persisting a {@link QueryTest query} to 
 * {@link TestProperties#PROP_SQL_DIR outputDirectory} when the result mode 
 * is {@link QueryScenario.RESULT_MODE#SQL sql}.
 * 
 * @author vhalbert
 *
 */
public interface QueryWriter {
	
	/**
	 * Returns the full path to the location the query files will be persisted to.
	 * @return String full directory path
	 * 
	 * @see TestProperties#PROP_SQL_DIR
	 */
	String getSQlFileOutputLocation();
	
	/**
	 * Called to write the <code>tests</code> to the {@link #getSQlFileOutputLocation() output} location.
	 * @param tests contains the SQL queries to write.
	 * @throws Exception
	 */
	void writeQueryTest(QueryTest tests) throws Exception;

}
