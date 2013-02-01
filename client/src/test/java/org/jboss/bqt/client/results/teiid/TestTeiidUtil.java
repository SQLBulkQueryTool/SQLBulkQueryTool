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

package org.jboss.bqt.client.results.teiid;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.bqt.core.exception.FrameworkException;
import org.jboss.bqt.core.util.UnitTestUtil;
import org.jboss.bqt.core.xml.JdomHelper;
import org.jboss.bqt.framework.ConfigPropertyLoader;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @version 	1.0
 * @author
 */
public class TestTeiidUtil  {
    private static final String TEMP_DIR = UnitTestUtil.getTestScratchPath();  //$NON-NLS-1$
    
    private static final String newLine = String.format("%n");
    

    	//System.getProperty("line.separator");
    //
    private static final String SAVED_EXPECTED_RESULT = 	
    	"<node name=\"AccessNode\">\r\n" +
    	"  <property name=\"Output Columns\">\r\n" + 
    	"    <value>INTKEY (integer)</value>\r\n" + 
    	"  </property>\r\n" +
    	"  <property name=\"Statistics\">\r\n" +
    	"    <value>Node Output Rows: 1</value>\r\n" +
    	"    <value>Node Process Time: 123</value>\r\n" +
    	"    <value>Node Cumulative Process Time: 123</value>\r\n" +
    	"    <value>Node Cumulative Next Batch Process Time: 0</value>\r\n" +
    	"    <value>Node Next Batch Calls: 2</value>\r\n"  +
    	"    <value>Node Blocks: 1</value>\r\n" +
    	"  </property>\r\n" + 
    	"  <property name=\"Cost Estimates\">\r\n" + 
    	"    <value>Estimated Node Cardinality: 1.0</value>\r\n" + 
    	"  </property>\r\n" + 
    	"  <property name=\"Query\">\r\n" + 
    	"    <value>SELECT g_0.INTKEY FROM Source.smalla AS g_0 WHERE g_0.INTKEY = 22</value>\r\n" + 
    	"  </property>\r\n" + 
    	"  <property name=\"Model Name\">\r\n" + 
    	"    <value>Source</value>\r\n" + 
    	"  </property>\r\n" + 
    	"</node>";
    
    private static final String EXPECTED_RESULT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
	SAVED_EXPECTED_RESULT;

    
    private static File resultPlanFile = null;

    
	@Before
    public  void setUp() throws Exception {
        
    	ConfigPropertyLoader.reset();
    }

    
	private static void printDoc(String doc, File outputFile) throws Exception {
		JdomHelper.write(doc, outputFile.getAbsolutePath());
	}

	//  ********* T E S T   S U I T E   M E T H O D S  *********

    /**
     * Tests {@link org.jboss.bqt.core.util.StringHelper}
     * @throws Exception 
     */
    @Test
    @Ignore
    public void xtestPrintResults() throws Exception {
    	
    	File testFile = new File(TEMP_DIR + File.separator + "printResults1.pln");
    	
    	String queryplan = "Query Plan " +
          "AccessNode " +
          "  + Output Columns: " +
          "   0: EVENT_TIME (long) " +
          "   1: VDB (string) " +
          "  + Statistics: " +
          "   0: Node Output Rows: 8 " +
          "  + Model Name:Logging ";
    	
    	TeiidUtil.printResults(queryplan, null, testFile);
    	
    	File errorFile = new File(TEMP_DIR + File.separator + "printResults1.error");
    	
    	TeiidUtil.compareToResults(queryplan,  errorFile, null, testFile, true); 
    }
    
    private static void printResults() throws Exception {
    	if (resultPlanFile != null) return;
    	
    	resultPlanFile = new File(TEMP_DIR + File.separator + "printResults2.pln");
    	
    	
    	printDoc(SAVED_EXPECTED_RESULT, resultPlanFile);
    	
    }
  
    
    @Test
    public void testNoFailure() throws Exception {
    	printResults();
     	
    	File errorFile = new File(TEMP_DIR + File.separator + "printResults3.error");
    	
      	String resultplan = EXPECTED_RESULT;
      	     	
    	List differences = TeiidUtil.compareToResults(resultplan,  errorFile, null, resultPlanFile, false); 
    	System.out.println(differences);
    	assertEquals(0, differences.size());
    }
    
    /**
     * Tests {@link org.jboss.bqt.core.util.StringHelper}
     * @throws Exception 
     */
    @Test
    public void testCompareResultsFailure() throws Exception {
    	printResults();
    	
    	File errorFile = new File(TEMP_DIR + File.separator + "printResults2.error");
    	String resultplan = StringUtils.replace(EXPECTED_RESULT, "Cardinality: 1.0", "Cardinality: 2.0");
     	
    	List differences = TeiidUtil.compareToResults(resultplan,  errorFile, null, resultPlanFile, false); 
    	assertEquals(1, differences.size());
    }
    
    @Test
    public void testTwoLineFailure() throws Exception {
    	printResults();
    	
       	File errorFile = new File(TEMP_DIR + File.separator + "printResults4.error");
           	
    	String resultplan = StringUtils.replace(EXPECTED_RESULT, "Cardinality: 1.0", "Cardinality: 2.0");
    	resultplan = StringUtils.replace(resultplan, "Node Output Rows: 1", "Node Output Rows: 2");
       	
    	List differences = TeiidUtil.compareToResults(resultplan,  errorFile, null, resultPlanFile, false); 
    	assertEquals(2, differences.size());
    }
    
    /**
     * testing that any changes to the TIME related node info that it doesnt trigger a difference
     * @throws Exception
     */
    @Test
    public void testTwoLineFailure2() throws Exception {
    	printResults();
    	
       	File errorFile = new File(TEMP_DIR + File.separator + "printResults5.error");
           	
    	String resultplan = StringUtils.replace(EXPECTED_RESULT, "Cardinality: 1.0", "Cardinality: 2.0");
    	resultplan = StringUtils.replace(resultplan, "Node Output Rows: 1", "Node Output Rows: 2");
    	resultplan = StringUtils.replace(resultplan, "Node Process Time: 123", "Node Process Time: 124");
       	resultplan = StringUtils.replace(resultplan, "Node Cumulative Process Time: 123", "Node Cumulative Process Time: 124");
       	resultplan = StringUtils.replace(resultplan, "Node Cumulative Next Batch Process Time: 0", "Node Cumulative Next Batch Process Time: 10");

       	
    	List differences = TeiidUtil.compareToResults(resultplan,  errorFile, null, resultPlanFile, false); 
    	assertEquals(2, differences.size());
    }    
    	
 
}
