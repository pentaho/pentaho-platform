/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *  
 * @author dkincade
 */
package org.pentaho.platform.repository2.unified.importexport.legacy;

import junit.framework.TestCase;
import org.pentaho.platform.repository2.unified.importexport.ImportSource;

import java.io.File;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class FileSolutionRepositoryImportSourceTest extends TestCase {
  public void testInitialize() throws Exception {
    {
      final File tempFile = File.createTempFile("junit", "tmp");
      final FileSolutionRepositoryImportSource importSource
          = new FileSolutionRepositoryImportSource(tempFile, "sample.xaction", "UTF-8");
      importSource.initialize();
      assertEquals(1, importSource.getCount());
      final Iterable<ImportSource.IRepositoryFileBundle> files = importSource.getFiles();
      assertNotNull(files);
      final ImportSource.IRepositoryFileBundle bundle = files.iterator().next();
      assertNotNull(bundle);
      assertEquals("", bundle.getPath());
      assertNotNull(bundle.getFile());
      assertEquals("sample.xaction", bundle.getFile().getName());
      assertFalse(bundle.getFile().isFolder());
    }

    {
      final File sourceFile = new File("./test-src/org/pentaho/platform/repository2/unified/importexport/testdata");
      assertTrue("Make sure your current directory is the repository project", sourceFile.exists());
      FileSolutionRepositoryImportSource importSource
          = new FileSolutionRepositoryImportSource(sourceFile, "UTF-8");
      importSource.initialize();
      assertEquals(11, importSource.getCount());
    }

  }

  public void testGetFiles() throws Exception {

  }

  public void testCreate() throws Exception {
    final File tempFile = File.createTempFile("junit", "tmp");
    try {
      new FileSolutionRepositoryImportSource(null, "UTF-8");
      fail();
    } catch (Exception success) {
    }

    try {
      new FileSolutionRepositoryImportSource(tempFile, null);
      fail();
    } catch (Exception success) {
    }

    try {
      new FileSolutionRepositoryImportSource(tempFile, "");
      fail();
    } catch (Exception success) {
    }

    try {
      new FileSolutionRepositoryImportSource(null, "filename.txt", "UTF-8");
      fail();
    } catch (Exception success) {
    }

    try {
      new FileSolutionRepositoryImportSource(tempFile, null, "UTF-8");
      fail();
    } catch (Exception success) {
    }

    try {
      new FileSolutionRepositoryImportSource(tempFile, "", "UTF-8");
      fail();
    } catch (Exception success) {
    }

    try {
      new FileSolutionRepositoryImportSource(tempFile, "filename.txt", null);
      fail();
    } catch (Exception success) {
    }

    try {
      new FileSolutionRepositoryImportSource(tempFile, "filename.txt", "");
      fail();
    } catch (Exception success) {
    }

    new FileSolutionRepositoryImportSource(tempFile, "UTF-8");
  }
}
