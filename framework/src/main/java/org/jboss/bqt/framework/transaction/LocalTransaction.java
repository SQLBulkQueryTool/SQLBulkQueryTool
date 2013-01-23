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

import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.framework.AbstractQuery;
import org.jboss.bqt.framework.Test;

/**
 * A transaction which is user controlled.
 */
public class LocalTransaction extends AbstractQuery {

	public LocalTransaction() {
		super();
	}

	@Override
	public void before(Test test) {
		super.before(test);

		try {
			debug("LocalTransaction - Autocommit: " + getConnectionStrategy().getAutocommit());
			getConnectionStrategy().getConnection().setAutoCommit(
					getConnectionStrategy().getAutocommit());
		} catch (Exception e) {
			throw new TransactionRuntimeException(e);
		}
	}

	@Override
	public void after() {
		try {
			if (getTest().rollbackAlways() || getTest().isExceptionExpected()) {
				getConnectionStrategy().getConnection().rollback();

			} else {
				getConnectionStrategy().getConnection().commit();
			}
			
		} catch (Exception se) {
			se.printStackTrace();
			// if exception, try to trigger the rollback
			try {
				getConnectionStrategy().getConnection().rollback();
			} catch (Exception e) {
				// do nothing
			}
			this.setApplicationException(se);

		} finally {
			// if an exception occurs and the autocommit is set to true - while
			// doing a transaction
			// will generate a new exception overriding the first exception
				try {
					getConnectionStrategy().getConnection().setAutoCommit(true);
				} catch (Exception e) {
					this.setApplicationException(e);
				}
				
			super.after();
		}
	}

}
