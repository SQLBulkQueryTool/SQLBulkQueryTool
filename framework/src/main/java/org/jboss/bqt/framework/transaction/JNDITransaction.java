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

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.jboss.bqt.framework.TestCase;

import org.jboss.bqt.core.exception.TransactionRuntimeException;
import org.jboss.bqt.framework.AbstractQuery;
import org.jboss.bqt.framework.ConfigPropertyNames;

public class JNDITransaction extends AbstractQuery  {
	UserTransaction userTxn = null;

	public JNDITransaction() {
		super();
	}
	
	
	@Override
	public void before(TestCase testCase) {
		super.before(testCase);
		
		String jndi = getConnectionStrategy()
				.getEnvironment()
				.getProperty(
						ConfigPropertyNames.CONNECTION_STRATEGY_PROPS.JNDINAME_USERTXN);
		if (jndi == null) {
			throw new TransactionRuntimeException(
					"No JNDI name found for the User Transaction to look up in application server");
		}
		
		debug("JNDITransaction - UserTxn: " + jndi);


		try {

			// begin the transaction
			InitialContext ctx = new InitialContext();
			this.userTxn = (UserTransaction) ctx.lookup(jndi);
			this.userTxn.begin();
		} catch (Exception e) {
			throw new TransactionRuntimeException(e);
		}
	}

	@Override
	public void after() {
		try {
			if (this.userTxn != null) {
				if (getTestCase().getActualTest().rollbackAlways() || getTestCase().getTestResult().isFailure()) {
					this.userTxn.rollback();
				} else {
					this.userTxn.commit();
				}
				this.userTxn = null;
			}
		} catch (Exception e) {
			this.setApplicationException(e);
		} finally {
			super.after();
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 * @see org.jboss.bqt.framework.AbstractQuery#cleanup()
	 */
	@Override
	public void cleanup() {
		super.cleanup();
		
		// if not null, an exception might have occurred in which after(..) method was not called
		// therefore, force rollback
		if (this.userTxn != null) {
			try {
				this.userTxn.rollback();
			} catch (Exception e) {
				
			}
		}
		this.userTxn=null;
	}
	
	
}
