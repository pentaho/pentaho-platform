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
 */
package org.pentaho.platform.repository2.unified.lifecycle;

import junit.framework.TestCase;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryLifecycleManagerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.security.MockSecurityHelper;

import java.io.File;
import java.io.IOException;

/**
 * Unit tests for the MetadataRepositoryLifecycleManager class
 */
public class MetadataRepositoryLifecycleManagerTest extends TestCase {
  @Test
  public void testNewTenantSuccess() throws IOException {
    // Create a standard repository with the /etc folder
    final File tempDir = createTempDir();
    System.err.println("tempDir="+tempDir.getAbsolutePath());
    final FileSystemBackedUnifiedRepository fsRepo = new FileSystemBackedUnifiedRepository(tempDir);
    fsRepo.createFolder(fsRepo.getFile(RepositoryFile.SEPARATOR).getId(),
        new RepositoryFile.Builder("etc").folder(true).build(), "");

    // Create a lifecycle manager that uses the fs repository
    final MetadataRepositoryLifecycleManager mgr =
        new MetadataRepositoryLifecycleManager(fsRepo, new MockSecurityHelper());
    mgr.newTenant("test");

    // Verify that the metadata folder and file exists
    final RepositoryFile metadataFile = fsRepo.getFile(mgr.getMetadataMappingFilePath());
    assertNotNull("The metadata mapping file should exist in the repository", metadataFile);
  }

  @Test
  public void testNewTenantError1() throws IOException {
    // Create a blank FS Repository and a we should get an exception that the root folder doens't exist
    final File tempDir = createTempDir();
    System.err.println("tempDir="+tempDir.getAbsolutePath());
    final FileSystemBackedUnifiedRepository fsRepo = new FileSystemBackedUnifiedRepository(tempDir);
    final MetadataRepositoryLifecycleManager mgr =
        new MetadataRepositoryLifecycleManager(fsRepo, new MockSecurityHelper());

    try {
      mgr.newTenant("test");
      fail("An exception should be thrown when the /etc folder doesn't exist");
    } catch (RepositoryLifecycleManagerException e) {
      System.err.println(e.getMessage());
      assertTrue("Error message not generated property - is ["+e.getMessage()+"] - should contain ["+mgr
          .getMetadataParentPath()+"]",
          e.getMessage().contains(mgr.getMetadataParentPath())
      );
    }
  }

  private static File createTempDir() throws IOException {
    final File tempFile = File.createTempFile("UnitTest-", ".repo");
    tempFile.delete();
    tempFile.mkdir();
    tempFile.deleteOnExit();
    return tempFile;
  }
}
