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

import java.io.Serializable;

/**
 * The QuerySQL represents a single sql statement to be executed in a given
 * {@link QueryTest Test}. The {@link #rowCnt} and {@link #updateCnt}, when set,
 * provide validation checks after the execution of the query.
 * 
 * @author vanhalbert
 * 
 */
public class QuerySQL {

	private String sql = null;
	private Object[] parms;
	private int updateCnt = -1;
	private int rowCnt = -1;
	private int runtimes = 1;
	
	// payload is set on the statement before execution
	private Serializable payload;
	
	public boolean isSelect() {
		if (sql.toLowerCase().startsWith("select")) return true;
		
		return false;
	}
	
	public int getRowCnt() {
		return rowCnt;
	}

	public void setRowCnt(int rowCnt) {
		this.rowCnt = rowCnt;
	}
	
	public int getRunTimes() {
		return this.runtimes;
	}
	
	public void setRunTimes(int times) {
		this.runtimes = times;
	}

	public int getUpdateCnt() {
		return updateCnt;
	}

	public void setUpdateCnt(int updateCnt) {
		this.updateCnt = updateCnt;
	}

	public QuerySQL(String sql, Object[] parms) {
		this.sql = sql.trim();
		this.parms = parms;
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParms() {
		return parms;
	}
	
	public void setPayLoad(Serializable payLoad) {
		this.payload = payLoad;
	}
	
	public Serializable getPayLoad() {
		return this.payload;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Query: ");
		sb.append(sql);
		sb.append("\nExpects: ");
		sb.append("Rowcount: ");
		sb.append(rowCnt);
		sb.append("UpdateCnt: ");
		sb.append(updateCnt);
		sb.append("Run#Times: ");
		sb.append(runtimes);

		return sb.toString();
	}

}
