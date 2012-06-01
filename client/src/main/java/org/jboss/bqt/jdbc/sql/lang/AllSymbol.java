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

package org.jboss.bqt.jdbc.sql.lang;

import java.util.*;

/**
 * <p>This is a subclass of Symbol representing *, which contains all of
 * the elements from all groups in the query.  The name of this symbol is always "*",
 * when resolved it contains a set of Element Symbols referred to by the AllSymbol</p>
 */
public class AllSymbol extends MultipleElementSymbol {

    private static final String ALL_SYMBOL_NAME = "*"; //$NON-NLS-1$

    /**
     * Constructor used for cloning 
     * @param name
     * @param canonicalName
     * @since 4.3
     */
    protected AllSymbol(String name, String canonicalName) {
        super(name, canonicalName);
    }
    
    /**
     * Default constructor
     */
    public AllSymbol(){
        this(ALL_SYMBOL_NAME, ALL_SYMBOL_NAME);
    }

//    public void acceptVisitor(LanguageVisitor visitor) {
//        visitor.visit(this);
//    }

	/**
	 * Return a deep copy of this object
	 * @return Deep copy of this object
	 */
	public Object clone() {
		AllSymbol copy = new AllSymbol(ALL_SYMBOL_NAME, ALL_SYMBOL_NAME);

		List elements = getElementSymbols();
		if(elements != null && elements.size() > 0) {
			ArrayList copyElements = new ArrayList(elements.size());
			Iterator iter = elements.iterator();
			while(iter.hasNext()) {
				ElementSymbol element = (ElementSymbol) iter.next();
				copyElements.add( element.clone() );
			}
			copy.setElementSymbols(copyElements);				
		}	

		return copy;
	}
	
}
