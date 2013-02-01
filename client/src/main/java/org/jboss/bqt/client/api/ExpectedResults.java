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

import java.io.File;

import org.jboss.bqt.framework.ExpectedTestResults;


/**
 * ExpectedResults represent the expected outcome from the execution of one query.
 */
public abstract class ExpectedResults extends ExpectedTestResults {
	// Identifier
	private String queryID;
	
	private String querySetID;
	private File resultsFile;
	
	public ExpectedResults(String querySetID, String queryID) {
		this.querySetID = querySetID;
		this.queryID = queryID;
	}

	public String getQuerySetID() {
		return querySetID;
		
	}
	public String getQueryID() {
		return queryID;
	}	
	
	public File getExpectedResultsFile() {
		return this.resultsFile;
	}
	
	public void setExpectedResultsFile(File resultsFile) {
		this.resultsFile = resultsFile;
	}

}