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

package org.jboss.bqt.core.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.naming.ConfigurationException;

import org.jboss.bqt.core.exception.FrameworkRuntimeException;

/**
 * Utility class that provides some useful things for users of the
 * com.metamatrix.api.exception package. Also provides package-level
 * functionality that is shared among classes in this package.
 */
public class ExceptionUtil {

	/**
	 * Prevent instantiation
	 */
	private ExceptionUtil() {
		super();
	}

	/**
	 * Convenience method that takes any Throwable and returns an appropriate
	 * Iterator over any nested exceptions in that Throwable. 
	 * 
	 * @param e
	 *            any Throwable
	 * @return an appropriate Iterator over any nested children Throwables;
	 */
	public static Iterator getChildrenIterator(Throwable e) {
		return new NestedExceptionIterator(e);
	}

	public static void printNestedStackTrace(Throwable exception,
			PrintStream output) {
		if (exception != null) {
			exception.printStackTrace(output);

			Iterator children = getChildrenIterator(exception);
			while (children.hasNext()) {
				exception = (Throwable) children.next();
				output.print(FrameworkRuntimeException.CAUSED_BY_STRING);
				exception.printStackTrace(output);
			}
		}
	}

	public static String getLinkedMessagesVerbose(Throwable exception) {
		return getLinkedMessagesVerbose(exception, 0);
	}

	public static String getLinkedMessagesVerbose(Throwable exception, int level) {
		if (exception != null) {
			StringBuffer buf = new StringBuffer();
			String lastMessage = appendMessage("", buf, null, exception); //$NON-NLS-1$
			Iterator children = getChildrenIterator(exception);
			while (children.hasNext()) {
				level++;
				exception = (Throwable) children.next();
				lastMessage = appendMessage("->", buf, lastMessage, exception); //$NON-NLS-1$
			}
			return buf.toString();
		}
		return ""; //$NON-NLS-1$
	}
	
	public static String getExceptionMessage(Throwable exception) {
		return getInnerMostdMessage(exception);
//		return getLinkedMessages(exception, 0);
	}

	public static String getLinkedMessages(Throwable exception) {
		return getLinkedMessages(exception, 0);
	}
	
	public static String getStackTrace(final Throwable t) {
		final ByteArrayOutputStream bas = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(bas);
		t.printStackTrace(pw);
		pw.close();
		return bas.toString();
	}

	/**
	 * Get the chain of messages, starting with the specified exception. The
	 * level number in the chain is prepended.
	 * <P>
	 * 
	 * This method calls the <CODE>getMessage</CODE> method for the exception
	 * passed in, and appends on the result returned from a recursive call to
	 * this method for the child of the passed in exception. The passed in
	 * exception is updated on each recursive call to be the child of the
	 * original exception, and the level is incremented on each call.
	 * 
	 * @param exception
	 *            The exception to print the chained message list of
	 * @param level
	 *            (zero-based) The depth of the exception parameter in the chain
	 *            of exceptions.
	 * @return A string of chained messages, each prepended by its level in the
	 *         chain, and each followed by a newline (blank string if the
	 *         exception passed in is null)
	 * 
	 * @see Throwable#getMessage()
	 */
	public static String getLinkedMessages(Throwable exception, int level) {
		if (exception != null) {
			StringBuffer buf = new StringBuffer();
			buf.append(exception.getMessage());
			Iterator children = getChildrenIterator(exception);
			while (children.hasNext()) {
				level++;
				exception = (Throwable) children.next();
				buf.append(exception.getMessage());
			}
			return buf.toString();
		}
		return ""; //$NON-NLS-1$
	}

	private static final String appendMessage(String prefix,
			StringBuffer buffer, String lastMessage, Throwable exception) {
		String message = exception.getMessage();
		buffer.append(prefix);
		buffer.append(getClassName(exception));
		if (message != null && !message.equals(lastMessage)) {
			buffer.append('-');
			buffer.append(message);
		}
		return message;
	}
	
	private static String getInnerMostdMessage(Throwable exception) {
		
		if (exception != null) {
			Iterator children = getChildrenIterator(exception);
			while (children.hasNext()) {
				exception = (Throwable) children.next();
			}
			return exception.getMessage();
		}
		return ""; //$NON-NLS-1$
	}

	private static final String getClassName(Throwable exception) {
		String className = exception.getClass().getName();
		int index = className.lastIndexOf('.');
		if (index >= 0 && index < className.length() - 1) {
			return className.substring(index + 1);
		}
		return className;
	}

	/**
	 * <p>
	 * An Iterator over any nested children <code>Throwable</code>s.
	 * </p>
	 * 
	 */
	public static class NestedExceptionIterator implements Iterator {

		Throwable exception;
		Throwable child;

		public NestedExceptionIterator(Throwable e) {
			exception = e;
		}

		/**
		 * Returns <tt>true</tt> if the iteration has more elements. (In other
		 * words, returns <tt>true</tt> if <tt>next</tt> would return an element
		 * rather than throwing an exception.)
		 * 
		 * @return <tt>true</tt> if the iterator has more elements.
		 */
		public boolean hasNext() {
			check();
			return (child != null);
		}

		/**
		 * Returns the next element in the iteration.
		 * 
		 * @return the next element in the iteration.
		 * @exception NoSuchElementException
		 *                iteration has no more elements.
		 */
		public Object next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			exception = child;
			child = null;
			return exception;
		}

		private void check() {
			if (child == null) {
				if (exception instanceof ConfigurationException) {
					ConfigurationException e = (ConfigurationException) exception;
					child = e.getRootCause();
				} else if (exception instanceof SQLException) {
					SQLException e = (SQLException) exception;
					child = e.getNextException();
				}
				if (child == null) {
					child = exception.getCause();
				}
				if (child == exception) {
					child = null;
				}
			}
		}

		/**
		 * 
		 * Removes from the underlying collection the last element returned by
		 * the iterator (optional operation). This method can be called only
		 * once per call to <tt>next</tt>. The behavior of an iterator is
		 * unspecified if the underlying collection is modified while the
		 * iteration is in progress in any way other than by calling this
		 * method.
		 * 
		 * @exception UnsupportedOperationException
		 *                if the <tt>remove</tt> operation is not supported by
		 *                this Iterator.
		 * 
		 * @exception IllegalStateException
		 *                if the <tt>next</tt> method has not yet been called,
		 *                or the <tt>remove</tt> method has already been called
		 *                after the last call to the <tt>next</tt> method.
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * The use of REMOVE_PREFIX is a hack to strip down the message so that it matches
	 * what is currently in use.  Teiid added  "Remote   org.teiid.api.exception.....:" 
	 * before the actual exception.
	 */
	static final String REMOVE_PREFIX = "Remote org.teiid.api.exception";
	static final int PREFIX_LENGTH = REMOVE_PREFIX.length();

   
    public static String getExceptionMessage(String msg) {
    	if (msg == null) return "";
    	
    	int loc = msg.indexOf(REMOVE_PREFIX);
    	
    	if (loc > -1) {
    		int idx = msg.indexOf(":", loc);
    		msg = msg.substring(idx + 1);
    		
    	}
    		
//		if (msg.indexOf(REMOVE_PREFIX)) {
//			int idx = msg.indexOf(":");
//			msg = msg.substring(idx +1);
//		} 
		
		return msg.trim();

    }
}
