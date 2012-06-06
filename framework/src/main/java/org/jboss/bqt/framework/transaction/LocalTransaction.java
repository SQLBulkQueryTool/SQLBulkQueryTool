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
package org.jboss.bqt.framework.transaction;

import java.sql.SQLException;

import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.framework.TransactionContainer;
import org.jboss.bqt.framework.TransactionQueryTestCase;
import org.jboss.bqt.framework.ConfigPropertyNames.CONNECTION_STRATEGY_PROPS;
import org.jboss.bqt.framework.ConfigPropertyNames.TXN_AUTO_WRAP_OPTIONS;

/**
 * A transaction which is user controlled.
 */
public class LocalTransaction extends TransactionContainer {

	public LocalTransaction() {
		super();
	}

	@Override
	protected void before(TransactionQueryTestCase test) {
//		test.getConnectionStrategy().setEnvironmentProperty(
//				CONNECTION_STRATEGY_PROPS.TXN_AUTO_WRAP,
//				TXN_AUTO_WRAP_OPTIONS.AUTO_WRAP_OFF);

		try {
			debug("Autocommit: " + test.getConnectionStrategy().getAutocommit());
			test.getConnection().setAutoCommit(
					test.getConnectionStrategy().getAutocommit());
		} catch (SQLException e) {
			throw new TransactionRuntimeException(e);
		}
	}

	@Override
	protected void after(TransactionQueryTestCase test) {
		boolean exception = false;
		try {
			if (test.rollbackAllways() || test.exceptionOccurred()) {
				test.getConnection().rollback();

			} else {
				test.getConnection().commit();
			}
			
		} catch (SQLException se) {
			se.printStackTrace();
			exception = true;
			// if exception, try to trigger the rollback
			try {
				test.getConnection().rollback();
			} catch (Exception e) {
				// do nothing
			}
			throw new TransactionRuntimeException(se);

		} finally {
			// if an exception occurs and the autocommit is set to true - while
			// doing a transaction
			// will generate a new exception overriding the first exception
			if (!exception) {
				try {
					test.getConnection().setAutoCommit(true);
				} catch (SQLException e) {
					throw new TransactionRuntimeException(e);
				}
			}
		}
	}

}
