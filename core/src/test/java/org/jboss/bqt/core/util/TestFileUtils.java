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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jboss.bqt.core.exception.FrameworkException;

import junit.framework.TestCase;


/**
 * @since 4.0
 */
public final class TestFileUtils extends TestCase {
    
    private static final String FILE_NAME = UnitTestUtil.getTestDataPath() + File.separator + "fakeScript.txt"; //$NON-NLS-1$
    private static final String TEMP_FILE_PREFFIX = "mmtmp"; //$NON-NLS-1$
    

    private final static String TEMP_DIR_NAME = "tempdir"; //$NON-NLS-1$
    File tempDir;
    private final static String TEMP_FILE_NAME = "tempfile.txt"; //$NON-NLS-1$
    private final static String TEMP_FILE_NAME2 = "tempfile2.txt"; //$NON-NLS-1$
    
    // =========================================================================
    //                        F R A M E W O R K
    // =========================================================================
    /**
     * Constructor for TestJDBCRepositoryWriter.
     * @param name
     */
    public TestFileUtils(String name) {
        super(name);
    }

    // =========================================================================
    //                 S E T   U P   A N D   T E A R   D O W N
    // =========================================================================

    public void setUp() throws Exception {
        super.setUp();
        
        //create a temp directory
        tempDir = new File(TEMP_DIR_NAME);
        tempDir.mkdir();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        
        try {
            tempDir.delete();
        } catch (Exception e) {
        }
        
        try {
            new File(TEMP_FILE_NAME).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            new File(TEMP_FILE_NAME2).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testWrite() throws Exception{
        File tmp = null;
        try {
            tmp = File.createTempFile(TEMP_FILE_PREFFIX, null);
            final FileInputStream is = new FileInputStream(FILE_NAME);
            FileUtils.write(is, tmp);
            
            if(tmp == null || tmp.length() == 0){
                fail("Content not written to new file"); //$NON-NLS-1$
            }
        }finally {
            if(tmp != null) {
                tmp.delete();   
            }
        }
    }
    
    public void testWriteEmptyFile() throws Exception{
        File tmp = null;
        File emptyFile = null;
        try {
            tmp = File.createTempFile(TEMP_FILE_PREFFIX, null);
            emptyFile = File.createTempFile("EMPTY", null); //$NON-NLS-1$
            final FileInputStream is = new FileInputStream(emptyFile);
            FileUtils.write(is, tmp);            
            if(tmp == null || tmp.length() > 0){
                fail("content available; must be empty"); //$NON-NLS-1$
            }
        }finally {
            if(tmp != null) {
                tmp.delete();   
            }            
            if (emptyFile != null) {
                emptyFile.delete();
            }
        }
    }    
    
    /**
     * Tests FileUtils.testDirectoryPermissions()
     * @throws Exception 
     * @since 4.3
     */
    public void testTestDirectoryPermissions() throws Exception {
        
        
        //positive case
        FileUtils.testDirectoryPermissions(TEMP_DIR_NAME);
        
        //negative case: dir doesn't exist
        try {
            FileUtils.testDirectoryPermissions("fakeDir"); //$NON-NLS-1$
            fail("Expected a BQTException"); //$NON-NLS-1$
        } catch (FrameworkException e) {
        }
    }

    
    
    /**
     * Tests FileUtils.copy()
     * @throws Exception 
     * @since 4.3
     */
    public void testCopy() throws Exception {
        String contents1 = ObjectConverterUtil.convertFileToString(new File(FILE_NAME));
        
        //positive case
        FileUtils.copy(FILE_NAME, TEMP_FILE_NAME, false);
        String contents2 = ObjectConverterUtil.convertFileToString(new File(TEMP_FILE_NAME));
        assertEquals("Expected file contents to be the same", contents1, contents2);  //$NON-NLS-1$
        assertTrue("Expected original file to still exist", new File(FILE_NAME).exists());  //$NON-NLS-1$

        //negative case: should fail because file already exists
        try {
            FileUtils.copy(FILE_NAME, TEMP_FILE_NAME, false);
            fail("Expected BQTException"); //$NON-NLS-1$
        } catch (IOException e) {
        }    
        
        
        //positive case: should succeed because we've specified to overwrite
        FileUtils.copy(FILE_NAME, TEMP_FILE_NAME, true);
        contents2 = ObjectConverterUtil.convertFileToString(new File(TEMP_FILE_NAME));
        assertEquals("Expected file contents to be the same", contents1, contents2);  //$NON-NLS-1$
        assertTrue("Expected original file to still exist", new File(FILE_NAME).exists());  //$NON-NLS-1$
        
    }
    
    
    /**
     * Tests FileUtils.rename()
     * @throws Exception 
     * @since 4.3
     */
    public void testRename() throws Exception {
        String contents1 = ObjectConverterUtil.convertFileToString(new File(FILE_NAME));
        

        //positive case
        FileUtils.copy(FILE_NAME, TEMP_FILE_NAME, true);
        FileUtils.rename(TEMP_FILE_NAME, TEMP_FILE_NAME2, false);
        String contents2 = ObjectConverterUtil.convertFileToString(new File(TEMP_FILE_NAME2));
        assertEquals("Expected file contents to be the same", contents1, contents2);  //$NON-NLS-1$
        assertFalse("Expected original file to not exist", new File(TEMP_FILE_NAME).exists());  //$NON-NLS-1$

        
        //negative case: should fail because file already exists
        FileUtils.copy(FILE_NAME, TEMP_FILE_NAME, true);
        FileUtils.copy(FILE_NAME, TEMP_FILE_NAME2, true);
        try {
            FileUtils.rename(FILE_NAME, TEMP_FILE_NAME2, false);
            fail("Expected BQTException"); //$NON-NLS-1$
        } catch (IOException e) {
        }
        
        
        //positive case: should succeed because we've specified to overwrite
        FileUtils.copy(FILE_NAME, TEMP_FILE_NAME, true);
        FileUtils.copy(FILE_NAME, TEMP_FILE_NAME2, true);
        FileUtils.rename(TEMP_FILE_NAME, TEMP_FILE_NAME2, true);
        contents2 = ObjectConverterUtil.convertFileToString(new File(TEMP_FILE_NAME2));
        assertEquals("Expected file contents to be the same", contents1, contents2);  //$NON-NLS-1$
        assertFalse("Expected original file to not exist", new File(TEMP_FILE_NAME).exists());  //$NON-NLS-1$

    }
    
    
    
    /**
     * Tests FileUtils.remove()
     * @throws Exception 
     * @since 4.3
     */
    public void testRemove() throws Exception {
        FileUtils.copy(FILE_NAME, TEMP_FILE_NAME, true);
        
        //positive case
        FileUtils.remove(TEMP_FILE_NAME);
        assertFalse("Expected File to not exist", new File(TEMP_FILE_NAME).exists());  //$NON-NLS-1$

        
        //call again - this should not throw an exception
        FileUtils.remove(TEMP_FILE_NAME);
    }
    
    /**
     * Tests FileUtils.copyRecursively()
     * Should fail because a file is given for source dir
     * @throws Exception 
     * @since 4.3
     */
    public void testCopyRecursivelyNull() throws Exception {
        File fileSource = new File(TEMP_FILE_NAME);
        fileSource.delete();
        fileSource.createNewFile();
        try {
            FileUtils.copyDirectoriesRecursively(fileSource, fileSource);
            fail("File arg should have been illegal."); //$NON-NLS-1$
        } catch (final Exception err) {
            // source was a file instead of dir - exception
//            err.printStackTrace();
        } finally {
            fileSource.delete();
        }
    }
    
}
