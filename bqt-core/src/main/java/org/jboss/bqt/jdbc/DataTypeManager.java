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

package org.jboss.bqt.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * <p>
 * This class manages data type, conversions between data types, and comparators
 * for data types. In the future other data type information may be managed
 * here.
 * </p>
 * 
 * <p>
 * In general, methods are provided to refer to types either by Class, or by
 * Class name. The benefit of the Class name option is that the user does not
 * need to load the Class object, which may not be in the classpath. The
 * advantage of the Class option is speed.
 * </p>
 */
public class DataTypeManager {
	
	public static final class DataTypeAliases {
		public static final String VARCHAR = "varchar"; //$NON-NLS-1$
		public static final String TINYINT = "tinyint"; //$NON-NLS-1$
		public static final String SMALLINT = "smallint"; //$NON-NLS-1$
		public static final String BIGINT = "bigint"; //$NON-NLS-1$
		public static final String REAL = "real"; //$NON-NLS-1$
		public static final String DECIMAL = "decimal"; //$NON-NLS-1$
	}

	public static final class DefaultDataTypes {
		public static final String STRING = "string"; //$NON-NLS-1$
		public static final String BOOLEAN = "boolean"; //$NON-NLS-1$
		public static final String BYTE = "byte"; //$NON-NLS-1$
		public static final String SHORT = "short"; //$NON-NLS-1$
		public static final String CHAR = "char"; //$NON-NLS-1$
		public static final String INTEGER = "integer"; //$NON-NLS-1$
		public static final String LONG = "long"; //$NON-NLS-1$
		public static final String BIG_INTEGER = "biginteger"; //$NON-NLS-1$
		public static final String FLOAT = "float"; //$NON-NLS-1$
		public static final String DOUBLE = "double"; //$NON-NLS-1$
		public static final String BIG_DECIMAL = "bigdecimal"; //$NON-NLS-1$
		public static final String DATE = "date"; //$NON-NLS-1$
		public static final String TIME = "time"; //$NON-NLS-1$
		public static final String TIMESTAMP = "timestamp"; //$NON-NLS-1$
		public static final String OBJECT = "object"; //$NON-NLS-1$
		public static final String NULL = "null"; //$NON-NLS-1$
		public static final String BLOB = "blob"; //$NON-NLS-1$
		public static final String CLOB = "clob"; //$NON-NLS-1$
		public static final String XML = "xml"; //$NON-NLS-1$
	}

	public static final class DefaultDataClasses {
		public static final Class<String> STRING = String.class;
		public static final Class<Boolean> BOOLEAN = Boolean.class;
		public static final Class<Byte> BYTE = Byte.class;
		public static final Class<Short> SHORT = Short.class;
		public static final Class<Character> CHAR = Character.class;
		public static final Class<Integer> INTEGER = Integer.class;
		public static final Class<Long> LONG = Long.class;
		public static final Class<BigInteger> BIG_INTEGER = BigInteger.class;
		public static final Class<Float> FLOAT = Float.class;
		public static final Class<Double> DOUBLE = Double.class;
		public static final Class<BigDecimal> BIG_DECIMAL = BigDecimal.class;
		public static final Class<java.sql.Date> DATE = java.sql.Date.class;
		public static final Class<Time> TIME = Time.class;
		public static final Class<Timestamp> TIMESTAMP = Timestamp.class;
		public static final Class<Object> OBJECT = Object.class;
//		public static final Class<NullType> NULL = NullType.class;
//		public static final Class<BlobType> BLOB = BlobType.class;
//		public static final Class<ClobType> CLOB = ClobType.class;
//		public static final Class<XMLType> XML = XMLType.class;
	}



	/** Base data type names and classes, Type name --> Type class */
	private static Map<String, Class<?>> dataTypeNames = new LinkedHashMap<String, Class<?>>(128);

	/** Base data type names and classes, Type class --> Type name */
	private static Map<Class, String> dataTypeClasses = new LinkedHashMap<Class, String>(128);

	private static Set<String> DATA_TYPE_NAMES;

	private static Set<Class> DATA_TYPE_CLASSES = Collections.unmodifiableSet(dataTypeClasses.keySet());

	// Static initializer - loads basic transforms types
	static {
		// Load default data types - not extensible yet
		loadDataTypes();

	}

	/**
	 * Constructor is private so instance creation is controlled by the class.
	 */
	private DataTypeManager() {
	}

	/**
	 * Add a new data type. For now this consists just of the Class - in the
	 * future a data type will be a more complicated entity. This is
	 * package-level for now as it is just used to add the default data types.
	 * @param typeName 
	 * 
	 * @param dataType
	 * 		New data type defined by Class
	 */
	static void addDataType(String typeName, Class dataType) {
		dataTypeNames.put(typeName, dataType);
		dataTypeClasses.put(dataType, typeName);
	}

	/**
	 * Get a set of all data type names.
	 * 
	 * @return Set of data type names (String)
	 */
	public static Set<String> getAllDataTypeNames() {
		return DATA_TYPE_NAMES;
	}

	public static Set<Class> getAllDataTypeClasses() {
		return DATA_TYPE_CLASSES;
	}


	public static String getDataTypeName(Class typeClass) {
		if (typeClass == null) {
			return DefaultDataTypes.NULL;
		}

		String result = dataTypeClasses.get(typeClass);
		if (result == null) {
			result = DefaultDataTypes.STRING;
		}

		return result;
	}

	/**
	 * Load default data types.
	 */
	static void loadDataTypes() {
		DataTypeManager.addDataType(DefaultDataTypes.BOOLEAN, DefaultDataClasses.BOOLEAN);
		DataTypeManager.addDataType(DefaultDataTypes.BYTE, DefaultDataClasses.BYTE);
		DataTypeManager.addDataType(DefaultDataTypes.SHORT,	DefaultDataClasses.SHORT);
		DataTypeManager.addDataType(DefaultDataTypes.CHAR, DefaultDataClasses.CHAR);
		DataTypeManager.addDataType(DefaultDataTypes.INTEGER, DefaultDataClasses.INTEGER);
		DataTypeManager.addDataType(DefaultDataTypes.LONG, DefaultDataClasses.LONG);
		DataTypeManager.addDataType(DefaultDataTypes.BIG_INTEGER, DefaultDataClasses.BIG_INTEGER);
		DataTypeManager.addDataType(DefaultDataTypes.FLOAT, DefaultDataClasses.FLOAT);
		DataTypeManager.addDataType(DefaultDataTypes.DOUBLE, DefaultDataClasses.DOUBLE);
		DataTypeManager.addDataType(DefaultDataTypes.BIG_DECIMAL, DefaultDataClasses.BIG_DECIMAL);
		DataTypeManager.addDataType(DefaultDataTypes.DATE, DefaultDataClasses.DATE);
		DataTypeManager.addDataType(DefaultDataTypes.TIME, DefaultDataClasses.TIME);
		DataTypeManager.addDataType(DefaultDataTypes.TIMESTAMP, DefaultDataClasses.TIMESTAMP);
		DataTypeManager.addDataType(DefaultDataTypes.STRING, DefaultDataClasses.STRING);
		DataTypeManager.addDataType(DefaultDataTypes.OBJECT, DefaultDataClasses.OBJECT);

	}

}
