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
package org.jboss.bqt.test.framework.datasource;

import java.sql.Connection;
import java.sql.Statement;

import org.jboss.bqt.test.framework.connection.ConnectionStrategy;
import org.jboss.bqt.core.exception.QueryTestFailedException;

/**
 * This class loads the data in the databases specified, to a known state
 */
public class DataStore {

	/**
	 * Called at the start of all the tests to initialize the database to ensure
	 * it's in the proper state.
	 * 
	 * @param connStrategy
	 */
	public static void initialize(ConnectionStrategy connStrategy) {

//		if (connStrategy.isDataStoreDisabled()) {
//			return;
//		}
//		try {
//			load(getConnection("pm1", connStrategy));
//
//			load(getConnection("pm2", connStrategy));
//
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
	}

	private static Connection getConnection(String identifier,
			ConnectionStrategy connStrategy) throws QueryTestFailedException {
		Connection conn = connStrategy.createDriverConnection(identifier);
		// force autocommit back to true, just in case the last user didnt
//		try {
//			conn.setAutoCommit(true);
//		} catch (Exception sqle) {
//			throw new QueryTestFailedException(sqle);
//		}

		return conn;
	}

//	private static void load(Connection c) throws Exception {
//		// DDL
//		// drop table g1;
//		// drop table g2;
//
//		// oracle
//		// create Table g1 (e1 number(5) PRIMARY KEY, e2 varchar2(50));
//		// create Table g2 (e1 number(5) REFERENCES g1, e2 varchar2(50));
//
//		// SQL Server
//		// create Table g1 (e1 int PRIMARY KEY, e2 varchar(50));
//		// create Table g2 (e1 int references g1, e2 varchar(50));
//
//		Statement stmt = c.createStatement();
//		try {
//			stmt.execute("delete from g2");
//			stmt.execute("delete from g1");
//
//			for (int i = 0; i < 100; i++) {
//				stmt.execute("insert into g1 (e1, e2) values(" + i + ",'" + i
//						+ "')");
//			}
//
//			for (int i = 0; i < 50; i++) {
//				stmt.execute("insert into g2 (e1, e2) values(" + i + ",'" + i
//						+ "')");
//			}
//
//		} finally {
//			stmt.close();
//		}
//
//	}

	/**
	 * Called as part of the setup for each test. This will set the database
	 * state as if {@link #initialize(ConnectionStrategy)} was called. However,
	 * for performance reasons, the process goes about removing what's not
	 * needed instead of cleaning out everything and reinstalling.
	 * 
	 * @param connStrategy
	 */
	public static void setup(ConnectionStrategy connStrategy) {
//		if (connStrategy.isDataStoreDisabled()) {
//			return;
//		}
//		try {
//			setUpTest(getConnection("pm1", connStrategy));
//
//			setUpTest(getConnection("pm2", connStrategy));
//
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}

	}

	private static void setUpTest(Connection c) throws Exception {

//		Statement stmt = c.createStatement();
//		try {
//			stmt.execute("delete from g2 where e1 >= 50"); //$NON-NLS-1$
//			stmt.execute("delete from g1 where e1 >= 100");
//
//		} finally {
//			stmt.close();
//		}
//
	}

}
