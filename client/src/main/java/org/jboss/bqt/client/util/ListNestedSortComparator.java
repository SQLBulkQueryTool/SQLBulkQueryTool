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

package org.jboss.bqt.client.util;

import java.util.List;

/**
 * This class can be used for comparing lists of elements, when the fields to be
 * sorted on and the comparison mechanism are dynamically specified.
 * <p>
 * 
 * Typically, the lists are records in a collection that is to be sorted.
 * <p>
 * 
 * <h3>Example</h3>
 * 
 * <pre>
 *    Records...
 *      { "a1", "b1", "c1" } 
 *      { "a1", "b1", "c2" }
 *      { "a1", "b2", "c1" }
 *      { "a1", "b2", "c2" }
 *      { "a2", "b1", "c1" } 
 *      { "a2", "b1", "c2" } 
 *      { "a2", "b2", "c1" } 
 *      { "a2", "b2", "c2" }
 * 
 *    Records sorted in ascending order on columns 0, 2...
 *      { "a1", "b1", "c1" } 
 *      { "a1", "b2", "c1" }
 *      { "a1", "b2", "c2" }
 *      { "a1", "b1", "c2" }
 *      { "a2", "b1", "c1" } 
 *      { "a2", "b2", "c1" } 
 *      { "a2", "b1", "c2" } 
 *      { "a2", "b2", "c2" }
 * </pre>
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class ListNestedSortComparator implements java.util.Comparator,
		java.io.Serializable {

	/**
	 * Specifies which fields to sort on.
	 */
	private final int[] sortParameters;

	/**
	 * Indicates whether comparison should be based on ascending or descending
	 * order.
	 */
	private boolean ascendingOrder = false;

	/**
	 * List of booleans indicating the order in which each column should be
	 * sorted
	 */
	private List orderTypes = null;

	private boolean isDistinct = true;
	private int distinctIndex;

	/**
	 * Constructs an instance of this class given the indicies of the parameters
	 * to sort on, and whether the sort should be in ascending or descending
	 * order.
	 * @param sortParameters 
	 */
	public ListNestedSortComparator(final int[] sortParameters) {
		this(sortParameters, false);
	}

	/**
	 * Constructs an instance of this class given the indicies of the parameters
	 * to sort on, and whether the sort should be in ascending or descending
	 * order.
	 * @param sortParameters 
	 * @param ascending 
	 */
	public ListNestedSortComparator(final int[] sortParameters, final boolean ascending) {
		this.sortParameters = sortParameters;
		this.ascendingOrder = ascending;
	}

	/**
	 * Constructs an instance of this class given the indicies of the parameters
	 * to sort on, and orderList used to determine the order in which each
	 * column is sorted.
	 * @param sortParameters 
	 * @param orderTypes 
	 */
	public ListNestedSortComparator(final int[] sortParameters, final List orderTypes) {
		this.sortParameters = sortParameters;
		this.orderTypes = orderTypes;
	}

	public boolean isDistinct() {
		return isDistinct;
	}

	public void setDistinctIndex(final int distinctIndex) {
		this.distinctIndex = distinctIndex;
	}

	/**
	 * Compares its two arguments for order. Returns a negative integer, zero,
	 * or a positive integer as the first argument is less than, equal to, or
	 * greater than the second.
	 * <p>
	 * 
	 * The <code>compare</code> method returns
	 * <p>
	 * <ul>
	 * <li>-1 if object1 less than object 2</li>
	 * <li>0 if object1 equal to object 2</li>
	 * <li>+1 if object1 greater than object 2</li>
	 * </ul>
	 * 
	 * @param o1
	 *            The first object being compared
	 * @param o2
	 *            The second object being compared
	 * @return int
	 */
	public int compare(final Object o1, final Object o2) {
		final List list1 = (List) o1;
		final List list2 = (List) o2;

		int compare = 0;
		for (int k = 0; k < sortParameters.length; k++) {
			final Object param1 = list1.get(sortParameters[k]);
			final Object param2 = list2.get(sortParameters[k]);

			if (param1 == null) {
				if (param2 == null) {
					// Both are null
					compare = 0;
				} else {
					// param1 = null, so is less than a non-null
					compare = -1;
				}
			} else if (param2 == null) {
				// param1 != null, param2 == null
				compare = 1;
			} else if (param1 instanceof Comparable) {
				compare = ((Comparable) param1).compareTo(param2);
			} else {
				compare = 0;
			}

			if (compare != 0) {
				final boolean asc = orderTypes != null ? ((Boolean) orderTypes.get(k))
						.booleanValue() : this.ascendingOrder;
				return asc ? compare : -compare;
			} else if (k == distinctIndex) {
				isDistinct = false;
			}
		}
		return 0;
	}

} // END CLASS

