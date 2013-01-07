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
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.bqt.core.CorePlugin;

/**
 * This is a common place to put String utility methods.
 */
public final class StringUtil {

	public static interface Constants {
		char LINE_FEED_CHAR = '\n';
		char NEW_LINE_CHAR = LINE_FEED_CHAR;
		char SPACE_CHAR = ' ';

		String EMPTY_STRING = ""; //$NON-NLS-1$

		String[] EMPTY_STRING_ARRAY = new String[0];

		// all patterns below copied from Eclipse's PatternConstructor class.
		final Pattern PATTERN_BACK_SLASH = Pattern.compile("\\\\"); //$NON-NLS-1$
		final Pattern PATTERN_QUESTION = Pattern.compile("\\?"); //$NON-NLS-1$
		final Pattern PATTERN_STAR = Pattern.compile("\\*"); //$NON-NLS-1$
		final Pattern PARAMETER_COUNT_PATTERN = Pattern.compile("\\{(\\d+)\\}");
	}

	/**
	 * Return a stringified version of the array.
	 * 
	 * @param array
	 *            the array
	 * @param delim
	 *            the delimiter to use between array components
	 * @return the string form of the array
	 */
	private static String toString(final Object[] array, final String delim) {
		if (array == null) {
			return ""; //$NON-NLS-1$
		}
		if (array.length == 0) {
			return "[]"; //$NON-NLS-1$
		}
		final StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int i = 0; i < array.length; ++i) {
			if (i != 0) {
				sb.append(delim);
			}
			sb.append(array[i]);
		}
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Return a stringified version of the array, using a ',' as a delimiter
	 * 
	 * @param array
	 *            the array
	 * @return the string form of the array
	 * @see #toString(Object[], String)
	 */
	static String toString(final Object[] array) {
		return toString(array, ","); //$NON-NLS-1$
	}

	/**
	 * Split a string into pieces based on delimiters. Similar to the perl
	 * function of the same name. The delimiters are not included in the
	 * returned strings.
	 * 
	 * @param str
	 *            Full string
	 * @param splitter
	 *            Characters to split on
	 * @return List of String pieces from full string
	 */
	public static List split(String str, String splitter) {
		StringTokenizer tokens = new StringTokenizer(str, splitter);
		ArrayList l = new ArrayList(tokens.countTokens());
		while (tokens.hasMoreTokens()) {
			l.add(tokens.nextToken());
		}
		return l;
	}

	public static String removeChars(final String value, final char[] chars) {
		final StringBuffer result = new StringBuffer();
		if (value != null && chars != null && chars.length > 0) {
			final String removeChars = String.valueOf(chars);
			for (int i = 0; i < value.length(); i++) {
				final String character = value.substring(i, i + 1);
				if (removeChars.indexOf(character) == -1) {
					result.append(character);
				}
			}
		} else {
			result.append(value);
		}
		return result.toString();
	}	

	/**
	 * Return the last token in the string.
	 * 
	 * @param str
	 *            String to be tokenized
	 * @param delimiter
	 *            Characters which are delimit tokens
	 * @return the last token contained in the tokenized string
	 */
	public static String getLastToken(String str, String delimiter) {
		if (str == null) {
			return Constants.EMPTY_STRING;
		}
		int beginIndex = 0;
		if (str.lastIndexOf(delimiter) > 0) {
			beginIndex = str.lastIndexOf(delimiter) + 1;
		}
		return str.substring(beginIndex, str.length());
	}

	/**
	 * Return the first token in the string.
	 * 
	 * @param str
	 *            String to be tokenized
	 * @param delimiter
	 *            Characters which are delimit tokens
	 * @return the first token contained in the tokenized string
	 */
	public static String getFirstToken(String str, String delimiter) {
		if (str == null) {
			return Constants.EMPTY_STRING;
		}
		int endIndex = str.indexOf(delimiter);
		if (endIndex < 0) {
			endIndex = str.length();
		}
		return str.substring(0, endIndex);
	}
	
    /**
     * Create a string by substituting the parameters into all key occurrences in the supplied format. The pattern consists of
     * zero or more keys of the form <code>{n}</code>, where <code>n</code> is an integer starting at 1. Therefore, the first
     * parameter replaces all occurrences of "{1}", the second parameter replaces all occurrences of "{2}", etc.
     * <p>
     * If any parameter is null, the corresponding key is replaced with the string "null". Therefore, consider using an empty
     * string when keys are to be removed altogether.
     * </p>
     * <p>
     * If there are no parameters, this method does nothing and returns the supplied pattern as is.
     * </p>
     * 
     * @param pattern the pattern
     * @param parameters the parameters used to replace keys
     * @return the string with all keys replaced (or removed)
     */
    public static String createString( String pattern,
                                       Object... parameters ) {
        ArgCheck.isNotNull(pattern, "pattern");
        if (parameters == null) parameters = Constants.EMPTY_STRING_ARRAY;
        Matcher matcher = Constants.PARAMETER_COUNT_PATTERN.matcher(pattern);
        StringBuffer text = new StringBuffer();
        int requiredParameterCount = 0;
        int parmlength = parameters.length;
        if (parameters.length == 1 && parameters[0] == null) {
        	parmlength = 0;
        }
        boolean err = false;
        while (matcher.find()) {
            int ndx = Integer.valueOf(matcher.group(1));
            if (requiredParameterCount <= ndx) {
                requiredParameterCount = ndx + 1;
            }
            if (ndx >= parameters.length) {
                err = true;
                matcher.appendReplacement(text, matcher.group());
            } else {
                Object parameter = parameters[ndx];

                // Automatically pretty-print arrays
                if (parameter != null && parameter.getClass().isArray()) {
                    parameter = Arrays.asList((Object[])parameter);
                }

                matcher.appendReplacement(text, Matcher.quoteReplacement(parameter == null ? "null" : parameter.toString()));
            }
        }
        if (err || requiredParameterCount < parmlength) {
            throw new IllegalArgumentException(
					CorePlugin.Util
					.getString("StringUtil.requiredToSuppliedParameterMismatch",parameters.length,
                                                                                                   parameters.length == 1 ? "" : "s",
                                                                                                   requiredParameterCount,
                                                                                                   requiredParameterCount == 1 ? "" : "s",
                                                                                                   pattern,
                                                                                                   text.toString()));
        }
        matcher.appendTail(text);

        return text.toString();
    }	

	public static String getStackTrace(final Throwable t) {
		final ByteArrayOutputStream bas = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(bas);
		t.printStackTrace(pw);
		pw.close();
		return bas.toString();
	}

	/**
	 * Returns whether the specified text represents a boolean value, i.e.,
	 * whether it equals "true" or "false" (case-insensitive).
	 * @param text 
	 * @return boolean
	 * 
	 * @since 4.0
	 */
	public static boolean isBoolean(final String text) {
		return (Boolean.TRUE.toString().equalsIgnoreCase(text) || Boolean.FALSE
				.toString().equalsIgnoreCase(text));
	}

	/**
	 * <p>
	 * Returns whether the specified text is either empty or null.
	 * </p>
	 * 
	 * @param text
	 *            The text to check; may be null;
	 * @return True if the specified text is either empty or null.
	 * @since 4.0
	 */
	public static boolean isEmpty(final String text) {
		return (text == null || text.length() == 0);
	}

	// ============================================================================================================================
	// Constructors

	/**
	 * <p>
	 * Prevents instantiation.
	 * </p>
	 * 
	 * @since 4.0
	 */
	private StringUtil() {
	}

	public static String toUpperCase(String str) {
		String newStr = convertBasicLatinToUpper(str);
		if (newStr == null) {
			return str.toUpperCase();
		}
		return newStr;
	}

	private static String convertBasicLatinToUpper(String str) {
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (isBasicLatinLowerCase(chars[i])) {
				chars[i] = (char) ('A' + (chars[i] - 'a'));
			} else if (!isBasicLatinChar(chars[i])) {
				return null;
			}
		}
		return new String(chars);
	}

	private static boolean isBasicLatinLowerCase(char c) {
		return c >= 'a' && c <= 'z';
	}

	private static boolean isBasicLatinChar(char c) {
		return c <= '\u007F';
	}

	/**
	 * Convert the given value to specified type.
	 * @param <T> 
	 * @param value
	 * @param type
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	static <T> T valueOf(String value, Class type) {

		if (type == String.class) {
			return (T) value;
		} else if (type == Boolean.class || type == Boolean.TYPE) {
			return (T) Boolean.valueOf(value);
		} else if (type == Integer.class || type == Integer.TYPE) {
			return (T) Integer.decode(value);
		} else if (type == Float.class || type == Float.TYPE) {
			return (T) Float.valueOf(value);
		} else if (type == Double.class || type == Double.TYPE) {
			return (T) Double.valueOf(value);
		} else if (type == Long.class || type == Long.TYPE) {
			return (T) Long.decode(value);
		} else if (type == Short.class || type == Short.TYPE) {
			return (T) Short.decode(value);
		} else if (type.isAssignableFrom(List.class)) {
			return (T) new ArrayList<String>(Arrays.asList(value.split(","))); //$NON-NLS-1$
		} else if (type.isArray()) {
			String[] values = value.split(","); //$NON-NLS-1$
			Object array = Array.newInstance(type.getComponentType(),
					values.length);
			for (int i = 0; i < values.length; i++) {
				Array.set(array, i, valueOf(values[i], type.getComponentType()));
			}
			return (T) array;
		} else if (type == Void.class) {
			return null;
		} else if (type.isEnum()) {
			return (T) Enum.valueOf(type, value);
		} else if (type == URL.class) {
			try {
				return (T) new URL(value);
			} catch (MalformedURLException e) {
				// fall through and end up in error
			}
		} else if (type.isAssignableFrom(Map.class)) {
			List<String> l = Arrays.asList(value.split(",")); //$NON-NLS-1$
			Map m = new HashMap<String, String>();
			for (String key : l) {
				int index = key.indexOf('=');
				if (index != -1) {
					m.put(key.substring(0, index), key.substring(index + 1));
				}
			}
			return (T) m;
		}

		throw new IllegalArgumentException(
				"Conversion from String to " + type.getName() + " is not supported"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
