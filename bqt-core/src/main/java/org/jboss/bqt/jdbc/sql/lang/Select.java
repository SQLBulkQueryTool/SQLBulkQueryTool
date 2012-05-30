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

import org.jboss.bqt.core.util.EquivalenceUtil;
import org.jboss.bqt.core.util.HashCodeUtil;


/**
 * This class represents the SELECT clause of a query, which defines what elements
 * or expressions are returned from the query.
 */
public class Select  {

    /** The set of symbols for the data elements to be selected. */
    private List symbols;     // List<SelectSymbols>

    /** Flag for whether duplicate removal should be performed on the results */
    private boolean distinct;

    // =========================================================================
    //                         C O N S T R U C T O R S
    // =========================================================================

    /**
     * Constructs a default instance of this class.
     */
    public Select() {
        symbols = new ArrayList();
    }

    /**
     * Constructs an instance of this class from an ordered set of symbols.
     * @param symbols The ordered list of symbols
     */
    public Select( List symbols ) {
        this.symbols = new ArrayList( symbols );
    }

    // =========================================================================
    //                             M E T H O D S
    // =========================================================================

    /**
     * Returns the number of symbols in select.
     * @return Get count of number of symbols in select
     */
    public int getCount() {
        return symbols.size();
    }
    /**
	 * Checks for a Select * clause
	 * @return True if Select * is used
	 */
    public boolean isStar() {
		return (symbols.size() == 1 && symbols.get(0) instanceof AllSymbol);
    }

    /**
     * Returns an ordered list of the symbols in the select.
     * @param Get list of SelectSymbol in SELECT
     */
    public List getSymbols() {
        return symbols;
    }
    
    /**
     * Sets an ordered list of the symbols in the select.
     * @param symbols list of SelectSymbol in SELECT
     */
    public void setSymbols(List symbols) {
        this.symbols = symbols;
    }    

    /**
     * Returns the select symbol at the specified index.
     * @param index Index to get
     * @return The variable identifier at the index
     */
    public SelectSymbol getSymbol( int index ) {
        return (SelectSymbol) symbols.get(index);
    }

    /**
     * Adds a new symbol to the list of symbols.
     * @param symbol New symbol
     */
    public void addSymbol( SelectSymbol symbol ) {
    	if(symbol != null) {
	        symbols.add(symbol);
        }
    }

    /**
     * Adds a new collection of symbols to the list of symbols.
     * @param symbols Collection of SelectSymbols
     */
    public void addSymbols( Collection symbols) {
    	if(symbols != null) {
	        this.symbols.addAll(symbols);
        }
    }
    
    /**
     * Remove all current symbols
     */
    public void clearSymbols() {
    	symbols.clear();
    }

    /**
     * Checks if a symbol is in the Select.
     * @param symbol Symbol to check for
     * @return True if the Select contains the symbol
     */
    public boolean containsSymbol( SelectSymbol symbol ) {
        return symbols.contains(symbol);
    }

    /**
     * Check is the element symbol is being selected by this
     * select clause.  This includes checking for select start
     * and select group.star for the group of this element symbol.
     * ElementSymbol is assumed to be fully resolved.
     * @param elementSymbol fully resolved ElementSymbol
     * @return whether this select will select the element symbol
     */
//    public boolean isElementBeingSelected(ElementSymbol elementSymbol){
//        boolean isBeingSelected = this.containsSymbol(elementSymbol);
//        if (!isBeingSelected){
//            GroupSymbol g = elementSymbol.getGroupSymbol();
//            String groupDotStarName = g.getName() + ".*"; //$NON-NLS-1$
//            Iterator i = this.getSymbols().iterator();
//            while (i.hasNext()) {
//                Object selectSymbol = i.next();
//                if (selectSymbol instanceof AllSymbol){
//                    isBeingSelected = true;
//                    break;
//                } else if (selectSymbol instanceof AllInGroupSymbol){
//                    AllInGroupSymbol aigSymbol = (AllInGroupSymbol)selectSymbol;
//                    if (aigSymbol.getName().equalsIgnoreCase(groupDotStarName)){
//                        isBeingSelected = true;
//                        break;
//                    }
//                }
//            }
//        }
//        return isBeingSelected;
//    }
    

	/**
	 * Set whether select is distinct.
	 * @param isDistinct True if SELECT is distinct
	 */
	public void setDistinct(boolean isDistinct) {
		this.distinct = isDistinct;
	}

	/**
	 * Checks whether the select is distinct
	 * @return True if select is distinct
	 */
	public boolean isDistinct() {
		return this.distinct;
	}

//    public void acceptVisitor(LanguageVisitor visitor) {
//        visitor.visit(this);
//    }
		
	/**
	 * Get the ordered list of all elements returned by this select.  These elements
	 * may be ElementSymbols or ExpressionSymbols but in all cases each represents a 
	 * single column.
	 * @return Ordered list of SingleElementSymbol
	 */
//	public List getProjectedSymbols() { 
//		ArrayList projectedSymbols = new ArrayList();
//		Iterator iter = symbols.iterator();
//		while(iter.hasNext()) {
//			SelectSymbol symbol = (SelectSymbol) iter.next();
//			if(symbol instanceof SingleElementSymbol) { 
//				projectedSymbols.add(symbol);
//			} else {
//			    List multiSymbols = ((MultipleElementSymbol)symbol).getElementSymbols();
//			    if(multiSymbols != null) { 
//			        projectedSymbols.addAll(multiSymbols);
//			    }
//			}	
//		}		
//		return projectedSymbols;
//	}
	
    // =========================================================================
    //          O V E R R I D D E N     O B J E C T     M E T H O D S
    // =========================================================================

	/**
	 * Return a deep copy of this Select.
	 * @return Deep clone
	 */
	public Object clone() {
	    List thisSymbols = getSymbols();
	    List copySymbols = new ArrayList(thisSymbols.size());
	    Iterator iter = thisSymbols.iterator();
	    while(iter.hasNext()) {
	    	SelectSymbol ss = (SelectSymbol) iter.next();
	    	copySymbols.add(ss.clone());    
	    }
	    	    
		Select copy = new Select(copySymbols);
		copy.setDistinct( isDistinct() );
		return copy;
	}

	/**
	 * Compare two Selects for equality.  Order is important in the select (for
	 * determining the order of the returned columns), so this is a compare
	 * with order, not just a set comparison.
	 * @param obj Other object
	 * @return True if equal
	 */
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}

		if(!(obj instanceof Select)) {
			return false;
		}

		Select other = (Select) obj;
        
        return other.isDistinct() == isDistinct() &&
               EquivalenceUtil.areEqual(getSymbols(), other.getSymbols());
	}

	/**
	 * Get hashcode for Select.  WARNING: The hash code relies on the variables
	 * in the select, so changing the variables will change the hash code, causing
	 * a select to be lost in a hash structure.  Do not hash a Select if you plan
	 * to change it.
	 * @return Hash code
	 */
	public int hashCode() {
		return HashCodeUtil.hashCode(0, getSymbols());
	}

    /**
     * Returns a string representation of an instance of this class.
     * @return String representation of object
     */
    public String toString() {
    	return "Select.toString() not implemented";
 //   	return SQLStringVisitor.getSQLString(this);
    }
        
}  // END CLASS
