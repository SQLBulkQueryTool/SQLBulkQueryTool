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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.bqt.core.CorePlugin;
import org.jboss.bqt.core.exception.FrameworkRuntimeException;

public class ReflectionHelper {

	private Class<?> targetClass;
	private Map methodMap = null; // used for the brute-force method finder

	/**
	 * Construct a ReflectionHelper instance that cache's some information about
	 * the target class. The target class is the Class object upon which the
	 * methods will be found.
	 * 
	 * @param targetClass
	 *            the target class
	 * @throws IllegalArgumentException
	 *             if the target class is null
	 */
	public ReflectionHelper(Class targetClass) {
		if (targetClass == null) {
			throw new IllegalArgumentException(
					CorePlugin.Util
							.getString("ReflectionHelper.errorConstructing")); //$NON-NLS-1$
		}
		this.targetClass = targetClass;
	}

	/**
	 * Find the best method on the target class that matches the signature
	 * specified with the specified name and the list of arguments. This method
	 * first attempts to find the method with the specified arguments; if no
	 * such method is found, a NoSuchMethodException is thrown.
	 * <P>
	 * This method is unable to find methods with signatures that include both
	 * primitive arguments <i>and</i> arguments that are instances of
	 * <code>Number</code> or its subclasses.
	 * 
	 * @param methodName
	 *            the name of the method that is to be invoked.
	 * @param arguments
	 *            the array of Object instances that correspond to the arguments
	 *            passed to the method.
	 * @return the Method object that references the method that satisfies the
	 *         requirements, or null if no satisfactory method could be found.
	 * @throws NoSuchMethodException
	 *             if a matching method is not found.
	 * @throws SecurityException
	 *             if access to the information is denied.
	 */
	public Method findBestMethodOnTarget(String methodName, Object[] arguments)
			throws NoSuchMethodException, SecurityException {
		if (arguments == null) {
			return findBestMethodWithSignature(methodName,
					Collections.EMPTY_LIST);
		}
		int size = arguments.length;
		List argumentClasses = new ArrayList(size);
		for (int i = 0; i != size; ++i) {
			if (arguments[i] != null) {
				Class clazz = arguments[i].getClass();
				argumentClasses.add(clazz);
			} else {
				argumentClasses.add(null);
			}
		}
		return findBestMethodWithSignature(methodName, argumentClasses);
	}

	/**
	 * Find the best method on the target class that matches the signature
	 * specified with the specified name and the list of argument classes. This
	 * method first attempts to find the method with the specified argument
	 * classes; if no such method is found, a NoSuchMethodException is thrown.
	 * 
	 * @param methodName
	 *            the name of the method that is to be invoked.
	 * @param argumentsClasses
	 *            the list of Class instances that correspond to the classes for
	 *            each argument passed to the method.
	 * @return the Method object that references the method that satisfies the
	 *         requirements, or null if no satisfactory method could be found.
	 * @throws NoSuchMethodException
	 *             if a matching method is not found.
	 * @throws SecurityException
	 *             if access to the information is denied.
	 */
	public Method findBestMethodWithSignature(String methodName,
			Object[] argumentsClasses) throws NoSuchMethodException,
			SecurityException {
		List argumentClassesList = Arrays.asList(argumentsClasses);
		return findBestMethodWithSignature(methodName, argumentClassesList);
	}

	/**
	 * Find the best method on the target class that matches the signature
	 * specified with the specified name and the list of argument classes. This
	 * method first attempts to find the method with the specified argument
	 * classes; if no such method is found, a NoSuchMethodException is thrown.
	 * 
	 * @param methodName
	 *            the name of the method that is to be invoked.
	 * @param argumentsClasses
	 *            the list of Class instances that correspond to the classes for
	 *            each argument passed to the method.
	 * @return the Method object that references the method that satisfies the
	 *         requirements, or null if no satisfactory method could be found.
	 * @throws NoSuchMethodException
	 *             if a matching method is not found.
	 * @throws SecurityException
	 *             if access to the information is denied.
	 */
	public Method findBestMethodWithSignature(String methodName,
			List argumentsClasses) throws NoSuchMethodException,
			SecurityException {
		// Attempt to find the method
		Method result = null;
		Class[] classArgs = new Class[argumentsClasses.size()];

		// -------------------------------------------------------------------------------
		// First try to find the method with EXACTLY the argument classes as
		// specified ...
		// -------------------------------------------------------------------------------
		try {
			argumentsClasses.toArray(classArgs);
			result = this.targetClass.getMethod(methodName, classArgs); // this
																		// may
																		// throw
																		// an
																		// exception
																		// if
																		// not
																		// found
			return result;
		} catch (NoSuchMethodException e) {
			// No method found, so continue ...
		}

		// ---------------------------------------------------------------------------------------------
		// Then try to find a method with the argument classes converted to a
		// primitive, if possible ...
		// ---------------------------------------------------------------------------------------------
		List argumentsClassList = convertArgumentClassesToPrimitives(argumentsClasses);
		argumentsClassList.toArray(classArgs);
		try {
			result = this.targetClass.getMethod(methodName, classArgs); // this
																		// may
																		// throw
																		// an
																		// exception
																		// if
																		// not
																		// found
			return result;
		} catch (NoSuchMethodException e) {
			// No method found, so continue ...
		}

		// ---------------------------------------------------------------------------------------------
		// Still haven't found anything. So far, the "getMethod" logic only
		// finds methods that EXACTLY
		// match the argument classes (i.e., not methods declared with
		// superclasses or interfaces of
		// the arguments). There is no canned algorithm in Java to do this, so
		// we have to brute-force it.
		// ---------------------------------------------------------------------------------------------
		if (this.methodMap == null) {
			this.methodMap = new HashMap();
			Method[] methods = this.targetClass.getMethods();
			for (int i = 0; i != methods.length; ++i) {
				Method method = methods[i];
				LinkedList methodsWithSameName = (LinkedList) this.methodMap
						.get(method.getName());
				if (methodsWithSameName == null) {
					methodsWithSameName = new LinkedList();
					this.methodMap.put(method.getName(), methodsWithSameName);
				}
				methodsWithSameName.addFirst(method); // add lower methods first
			}
		}

		LinkedList<Method> methodsWithSameName = (LinkedList) this.methodMap
				.get(methodName);
		if (methodsWithSameName == null) {
			throw new NoSuchMethodException(methodName);
		}
		for (Method method : methodsWithSameName) {
			Class[] args = method.getParameterTypes();
			if (args.length != argumentsClasses.size()) {
				continue;
			}
			boolean allMatch = true; // assume all args match
			for (int i = 0; i < args.length && allMatch == true; ++i) {
				Class primitiveClazz = (Class) argumentsClassList.get(i);
				Class objectClazz = (Class) argumentsClasses.get(i);
				if (objectClazz != null) {
					// Check for possible matches with (converted) primitive
					// types
					// as well as the original Object type
					if (!args[i].equals(primitiveClazz)
							&& !args[i].isAssignableFrom(objectClazz)) {
						allMatch = false; // found one that doesn't match
					}
				} else {
					// a null is assignable for everything except a primitive
					if (args[i].isPrimitive()) {
						allMatch = false; // found one that doesn't match
					}
				}
			}
			if (allMatch) {
				if (result != null) {
					throw new NoSuchMethodException(
							methodName
									+ " Args: " + argumentsClasses + " has multiple possible signatures."); //$NON-NLS-1$ //$NON-NLS-2$
				}
				result = method;
			}
		}

		if (result != null) {
			return result;
		}

		throw new NoSuchMethodException(methodName
				+ " Args: " + argumentsClasses); //$NON-NLS-1$
	}

	/**
	 * Convert any argument classes to primitives.
	 * 
	 * @param arguments
	 *            the list of argument classes.
	 * @return the list of Class instances in which any classes that could be
	 *         represented by primitives (e.g., Boolean) were replaced with the
	 *         primitive classes (e.g., Boolean.TYPE).
	 */
	private static List convertArgumentClassesToPrimitives(List arguments) {
		List result = new ArrayList(arguments.size());
		Iterator iter = arguments.iterator();
		while (iter.hasNext()) {
			Class clazz = (Class) iter.next();
			if (clazz == Boolean.class)
				clazz = Boolean.TYPE;
			else if (clazz == Character.class)
				clazz = Character.TYPE;
			else if (clazz == Byte.class)
				clazz = Byte.TYPE;
			else if (clazz == Short.class)
				clazz = Short.TYPE;
			else if (clazz == Integer.class)
				clazz = Integer.TYPE;
			else if (clazz == Long.class)
				clazz = Long.TYPE;
			else if (clazz == Float.class)
				clazz = Float.TYPE;
			else if (clazz == Double.class)
				clazz = Double.TYPE;
			else if (clazz == Void.class)
				clazz = Void.TYPE;
			result.add(clazz);
		}

		return result;
	}

	/**
	 * Helper method to load a class.
	 * 
	 * @param className
	 *            is the class to instantiate
	 * @param classLoader
	 *            the class loader to use; may be null if the current class
	 *            loader is to be used
	 * @return Class is the instance of the class
	 * @throws ClassNotFoundException
	 */
	private static final Class loadClass(final String className,
			final ClassLoader classLoader) throws ClassNotFoundException {
		Class cls = null;
		if (classLoader == null) {
			cls = Class.forName(className.trim());
		} else {
			cls = Class.forName(className.trim(), true, classLoader);
		}
		return cls;
	}

	/**
	 * Helper method to create an instance of the class using the appropriate
	 * constructor based on the ctorObjs passed.
	 * 
	 * @param className
	 *            is the class to instantiate
	 * @param ctorObjs
	 *            are the objects to pass to the constructor; optional, nullable
	 * @param classLoader
	 *            the class loader to use; may be null if the current class
	 *            loader is to be used
	 * @return Object is the instance of the class
	 * @throws FrameworkRuntimeException
	 *             if an error occurrs instantiating the class
	 */

	public static final Object create(String className, Collection ctorObjs,
			final ClassLoader classLoader) throws FrameworkRuntimeException {
		try {
			int size = (ctorObjs == null ? 0 : ctorObjs.size());
			Class[] names = new Class[size];
			Object[] objArray = new Object[size];
			int i = 0;

			if (size > 0) {
				for (Iterator it = ctorObjs.iterator(); it.hasNext();) {
					Object obj = it.next();
					names[i] = loadClass(obj.getClass().getName(), classLoader);
					objArray[i] = obj;
					i++;
				}
			}
			return create(className, objArray, names, classLoader);
		} catch (FrameworkRuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new FrameworkRuntimeException(e);
		}
	}

	public static final Object create(String className, Object[] ctorObjs,
			Class<?>[] argTypes, final ClassLoader classLoader)
			throws Throwable {
		try {
			final Class<?> cls = loadClass(className, classLoader);

			Constructor ctor = cls.getDeclaredConstructor(argTypes);

			return ctor.newInstance(ctorObjs);

		} catch (FrameworkRuntimeException e) {
			throw e;
		} catch (InvocationTargetException ite) {
			throw ite.getCause();
		} catch (Exception e) {
			throw new FrameworkRuntimeException(e);
		}
	}

}
