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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.bqt.core.CorePlugin;

/**
 * This is a common place to put String helper methods.
 */
public final class StringHelper {

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

	
	@SuppressWarnings("null")
	public static boolean isEqual(String arg1, String arg2) {
		if (arg1 == null && arg2 == null) return true;
		if (arg1 == null && arg2 != null) return false;
		if (arg2 == null && arg1 != null) return false;
				
		return arg1.equals(arg2);

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
					.getString("StringHelper.requiredToSuppliedParameterMismatch",parameters.length,
                                                                                                   parameters.length == 1 ? "" : "s",
                                                                                                   requiredParameterCount,
                                                                                                   requiredParameterCount == 1 ? "" : "s",
                                                                                                   pattern,
                                                                                                   text.toString()));
        }
        matcher.appendTail(text);

        return text.toString();
    }	

}
