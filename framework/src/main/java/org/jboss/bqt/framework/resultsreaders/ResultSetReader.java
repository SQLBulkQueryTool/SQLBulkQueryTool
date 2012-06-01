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

package org.jboss.bqt.framework.resultsreaders;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;


/** 
 * This object wraps/extends a SQL ResultSet object as Reader object. Once the
 * ResultSet can read as reader then it can be persisted, printed or compared 
 * easily without lot of code hassle of walking it every time.
 * 
 * <p>PS: remember this is a Reader not InputStream, so all the fields read
 * going to be converted to strings before they returned.

 */
public class ResultSetReader extends StringLineReader {
    ResultSet source = null;
    
    // Number of columns in the result set
    int columnCount = 0;

    // delimiter between the fields while reading each row 
    String delimiter = "    "; //$NON-NLS-1$
 
    boolean firstTime = true;
    int[] columnTypes = null;
    
    private int rowCount;
    
    public ResultSetReader(ResultSet in) {
        this.source = in;        
    }
    
    public ResultSetReader(ResultSet in, String delimiter) {
        this.source = in;        
        this.delimiter = delimiter;
    }
    
    /** 
     * @see java.io.Reader#close()
     * @since 4.3
     */
    public void close() throws IOException {
        try {
            source.close();
            super.close();
        } catch (SQLException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Get the next line of results from the ResultSet. The first line will be the 
     * metadata of the resultset and then followed by the result rows. Each row will be 
     * returned as one line.  
     * @return next result line from result set.
     * @throws IOException 
     */
    protected String nextLine() throws IOException{        
        try {
            if (firstTime) {
                firstTime = false;
                ResultSetMetaData metadata = source.getMetaData();
                columnCount = metadata.getColumnCount();
                columnTypes  = new int[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    columnTypes[i] = metadata.getColumnType(i+1);
                }
                return resultSetMetaDataToString(metadata, delimiter);
            }
            
            // if you get here then we are ready to read the results.
            if (source.next()) {
            	rowCount++;
                StringBuffer sb = new StringBuffer();
                // Walk through column values in this row
                for (int col = 1; col <= columnCount; col++) {
                    Object anObj = source.getObject(col);
                    if (columnTypes[col-1] == Types.CLOB) {
                        sb.append(anObj != null ? anObj : "null"); //$NON-NLS-1$
                    }
                    else if (columnTypes[col-1] == Types.BLOB) {
                        sb.append(anObj != null ? "BLOB" : "null"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    else {
                        sb.append(anObj != null ? anObj : "null"); //$NON-NLS-1$
                    }
                    if (col != columnCount) {
                        sb.append(delimiter); 
                    }                    
                }
                sb.append("\n"); //$NON-NLS-1$
                return sb.toString();
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage());
        }        
        return null;
    }
    
    public int getRowCount() {
		return rowCount;
	}
    
    /**
     * Get the first line from the result set. This is the resultset metadata line where
     * we gather the column names and their types. 
     * @param metadata 
     * @param delimiter 
     * @return String
     * @throws SQLException
     */
    public static String resultSetMetaDataToString(ResultSetMetaData metadata, String delimiter) throws SQLException{
        StringBuffer sb = new StringBuffer();
        int columnCount = metadata.getColumnCount();
        for (int col = 1; col <= columnCount; col++) {
            sb.append(metadata.getColumnName(col))
                .append("[")          //$NON-NLS-1$
                .append(metadata.getColumnTypeName(col))
                .append("]");       //$NON-NLS-1$
            if (col != columnCount) {
                sb.append(delimiter);
            }
        }
        sb.append("\n"); //$NON-NLS-1$
        return sb.toString();        
    }
    
 
}
