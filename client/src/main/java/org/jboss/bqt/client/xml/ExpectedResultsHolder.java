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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.bqt.client.QueryTest;
import org.jboss.bqt.client.api.ExpectedResults;
import org.jboss.bqt.core.util.ExceptionUtil;

/**
 * ResultsHolder
 * <p>
 * Data structure. Holder of expected results and metadata.
 * </p>
 */
public class ExpectedResultsHolder extends ExpectedResults {

	// either TagNames.Elements.QUERY_RESULTS or TagNames.Elements.EXCEPTION
	private String resultType;

	// The SQl query if available.
	private String query;

	// Query Results
	private List rows;
	private List types;
	private List identifiers;

	// Exception
	private String exceptionClassName;
	private String exceptionMsg;
	private boolean exceptionContains = false;
	private boolean exceptionStartsWith = false;
	
	private long executionTime = -1;
	
	public ExpectedResultsHolder(final String type, QueryTest test) {
		super(test.getQuerySetID(), test.getQueryID());
		this.resultType = type;
		
		if (resultType.equals(TagNames.Elements.EXCEPTION)) {
			this.setExceptionExpected(true);
		}
	}
	
	
	@Override
	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	

	/**
	 * @return Returns the query.
	 */
	@Override
	public String getQuery() {
		return query;
	}

	/**
	 * @param query
	 *            The query to set.
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isResult() {
		return resultType.equals(TagNames.Elements.QUERY_RESULTS);
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(final String resultType) {
		this.resultType = resultType;
	}

	public List getRows() {
		return (rows == null ? new ArrayList() : rows);
	}

	public void setRows(final List rows) {
		this.rows = rows;
	}

	public List getTypes() {
		return types;
	}

	public void setTypes(final List types) {
		this.types = types;
	}

	public List getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(final List identifiers) {
		this.identifiers = identifiers;
	}

	public String getExceptionClassName() {
		return exceptionClassName;
	}

	public void setExceptionClassName(final String className) {
		this.exceptionClassName = className;
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public void setExceptionMsg(final String msg) {
		this.exceptionMsg = ExceptionUtil.getExceptionMessage(msg);
	}
	
	public boolean isExceptionContains() {
		return exceptionContains;
	}

	public void setExceptionContains(boolean exceptionContains) {
		this.exceptionContains = exceptionContains;
	}

	public boolean isExceptionStartsWith() {
		return exceptionStartsWith;
	}

	public void setExceptionStartsWith(boolean exceptionStartsWith) {
		this.exceptionStartsWith = exceptionStartsWith;
	}

	public boolean hasRows() {
		return (rows != null && rows.size() > 0);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("ResultsHolder... \n"); //$NON-NLS-1$
		if (isResult()) {
			for (int i = 0; i < this.identifiers.size(); i++) {
				buf.append("["); //$NON-NLS-1$
				buf.append(this.identifiers.get(i));
				buf.append(" - "); //$NON-NLS-1$
				buf.append(this.types.get(i));
				buf.append("] "); //$NON-NLS-1$
			}
			buf.append("\n"); //$NON-NLS-1$
			Iterator rowItr = this.rows.iterator();
			int i = 1;
			while (rowItr.hasNext()) {
				buf.append(i++);
				buf.append(": "); //$NON-NLS-1$
				buf.append(rowItr.next());
				buf.append("\n"); //$NON-NLS-1$
			}
		} else {
			buf.append(getExceptionClassName())
					.append(":").append(getExceptionMsg()); //$NON-NLS-1$            
		}
		return buf.toString();
	}
}