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

package org.jboss.bqt.core.exception;

import java.sql.SQLException;
import java.util.Iterator;

import org.jboss.bqt.core.util.ExceptionUtil;

/**
 * Exception which occurs if an error occurs within the server that is not
 * business-related. For instance, if a service or bean is not available or
 * communication fails.
 */
public class FrameworkException extends Exception {

	private static final long serialVersionUID = -3033427629587497938L;
	protected String code;
	private transient String originalType;

	public FrameworkException() {
	}

	public FrameworkException(String message) {
		super(message);
	}

	public FrameworkException(String errorCode, String message) {
		super(message);
		this.code = errorCode;
	}

	public FrameworkException(Throwable e) {
		this(e, e != null ? e.getMessage() : null);
	}

	public FrameworkException(Throwable e, String message) {
		super(message, e);
		setCode(e);
	}

	public FrameworkException(Throwable e, String errorCode, String message) {
		super(message, e);
		this.code = errorCode;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getOriginalType() {
		return originalType;
	}

	public void setOriginalType(String originalType) {
		this.originalType = originalType;
	}

	private void setCode(Throwable e) {
		if (e instanceof FrameworkException) {
			this.code = (((FrameworkException) e).getCode());
		} else if (e instanceof FrameworkRuntimeException) {
			this.code = ((FrameworkRuntimeException) e).getCode();
		} else if (e instanceof SQLException) {
			this.code = Integer.toString(((SQLException) e).getErrorCode());
		}
	}

	public String getMessage() {
		if (code == null || code.length() == 0) {
			return super.getMessage();
		}
		return "Error Code:" + code + " Message:" + super.getMessage(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the error message, formatted for output.
	 * <P>
	 * 
	 * The default formatting provided by this method is to prepend the error
	 * message with the level and the name of the class, and to append the error
	 * code on the end if a non-zero code is defined.
	 * <P>
	 * 
	 * This method provides a hook for subclasses to override the default
	 * formatting of any one exception.
	 * 
	 * @param throwable
	 *            The exception to print
	 * @param level
	 *            The depth of the exception in the chain of exceptions
	 * @return A formatted string for the exception
	 */
	static String getFormattedMessage(final Throwable throwable, final int level) {
		String code;
		if (throwable instanceof FrameworkException) {
			code = ((FrameworkException) throwable).getCode();
		} else if (throwable instanceof FrameworkRuntimeException) {
			code = ((FrameworkRuntimeException) throwable).getCode();
		} else {
			code = null;
		}
		return ((level != 0) ? ("\n" + level + " ") : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "[" + throwable.getClass().getSimpleName() + "]" //$NON-NLS-1$ //$NON-NLS-2$
				+ ((code != null) ? (' ' + code + ": ") : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (throwable.getMessage() == null ? "" : throwable.getMessage()); //$NON-NLS-1$
	}

	/**
	 * Get the full error message, including any message(s) from child
	 * exceptions. Messages of any exceptions chained to this exception are
	 * prepended with their "level" in the chain.
	 * 
	 * @return The full error message
	 * 
	 * @see #getFormattedMessage
	 */
	public String getFullMessage() {
		int level = 0;
		StringBuffer buf = new StringBuffer();
		buf.append(getFormattedMessage(this, level));
		Iterator children = ExceptionUtil.getChildrenIterator(this);
		while (children.hasNext()) {
			level++;
			Throwable exception = (Throwable) children.next();
			buf.append(getFormattedMessage(exception, level));
		}
		return buf.toString();
	}

	/**
	 * Get the exception which is linked to this exception.
	 * 
	 * @return The linked exception
	 * @see #getCause()
	 * @deprecated
	 */
	public Throwable getChild() {
		return super.getCause();
	}

	/**
	 * Returns a string representation of this class.
	 * 
	 * @return String representation of instance
	 */
	public String toString() {
		return getFullMessage();
	}
}
