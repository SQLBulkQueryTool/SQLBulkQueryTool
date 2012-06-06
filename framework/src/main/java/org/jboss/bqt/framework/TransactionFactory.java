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
package org.jboss.bqt.framework;

import org.jboss.bqt.core.exception.QueryTestFailedException;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;
import org.jboss.bqt.framework.transaction.*;


/**
 * TransactionFactory is used so that the type of {@link TransactionContainer }
 * can be dynamically loaded based on a property.
 * 
 * Specify the property {@link #TRANSACTION_TYPE} in order to set the
 * transaction type to use.
 * 
 * @author vanhalbert
 * 
 */
public class TransactionFactory {

	/**
	 * Transaction Type indicates the type of transaction container to use
	 * 
	 * @see TransactionFactory
	 */
	public static final String TRANSACTION_TYPE = "transaction-option"; //$NON-NLS-1$
	public static interface TRANSACTION_TYPES {
		public static final String LOCAL_TRANSACTION = "local"; //$NON-NLS-1$
		public static final String XATRANSACTION = "xa"; //$NON-NLS-1$
		public static final String JNDI_TRANSACTION = "jndi"; //$NON-NLS-1$
		public static final String OFFWRAP_TRANSACTION = "off"; //$NON-NLS-1$
		public static final String ONWRAP_TRANSACTION = "on"; //$NON-NLS-1$
		public static final String AUTOWRAP_TRANSACTION = "auto"; //$NON-NLS-1$
	}
	private TransactionFactory() {
	}

	public static TransactionContainer create(ConfigPropertyLoader config)  {
		TransactionContainer transacton = null;

		String type = config.getProperty(TRANSACTION_TYPE);
		if (type == null) {
            transacton = new TxnAutoTransaction();
    //			throw new FrameworkRuntimeException(TRANSACTION_TYPE
	//				+ " property was not specified");
            FrameworkPlugin.LOGGER.debug("====  Create Transaction-Option: not defined");
		} else {

            FrameworkPlugin.LOGGER.debug("====  Create Transaction-Option: " + type);

            if (type.equalsIgnoreCase(TRANSACTION_TYPES.LOCAL_TRANSACTION)) {
                transacton = new LocalTransaction();
    //		} else if (type.equalsIgnoreCase(TRANSACTION_TYPES.XATRANSACTION)) {
    //			transacton = new XATransaction();
            } else if (type.equalsIgnoreCase(TRANSACTION_TYPES.JNDI_TRANSACTION)) {
                transacton = new JNDITransaction();
            } else if (type.equalsIgnoreCase(TRANSACTION_TYPES.OFFWRAP_TRANSACTION)) {
                transacton = new TxnAutoTransaction(
                		ConfigPropertyNames.TXN_AUTO_WRAP_OPTIONS.AUTO_WRAP_OFF);
            } else if (type.equalsIgnoreCase(TRANSACTION_TYPES.ONWRAP_TRANSACTION)) {
                transacton = new OnWrapTransaction();
                //new TxnAutoTransaction(
                //		TXN_AUTO_WRAP_OPTIONS.AUTO_WRAP_ON);
            } else if (type
                    .equalsIgnoreCase(TRANSACTION_TYPES.AUTOWRAP_TRANSACTION)) {
                transacton = new TxnAutoTransaction(
                		ConfigPropertyNames.TXN_AUTO_WRAP_OPTIONS.AUTO_WRAP_AUTO);

            } else {
                throw new FrameworkRuntimeException("Invalid property value of "
                        + type + " for " + TRANSACTION_TYPE);
            }
        }
		
		FrameworkPlugin.LOGGER.info("====  TransactionContainer: "
				+ transacton.getClass().getName() + " option:" + type);
		return transacton;
	}

}
