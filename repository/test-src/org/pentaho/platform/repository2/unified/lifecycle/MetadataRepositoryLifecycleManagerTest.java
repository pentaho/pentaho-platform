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
package org.pentaho.platform.repository2.unified.lifecycle;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository.pmd.PentahoMetadataRepositoryLifecycleManager;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.fs.FileSystemFileAclDao;
import org.pentaho.platform.repository2.unified.fs.FileSystemRepositoryFileDao;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;

/**
 * Class Description
 * User: dkincade
 */
public class MetadataRepositoryLifecycleManagerTest extends TestCase {
  public static final String REPOSITORY_ADMIN_USERNAME = "pentahoRepoAdmin";
  public static final String TENANT_AUTHENTICATED_AUTHORITY_NAME_PATTERN = "{0}_Admin";
  public static final String SINGLE_TENANT_AUTHENTICATED_AUTHORITY_NAME = "Authenticated";
  private static final String TEST_TENANT_ID = "Pentaho";

  private PentahoMetadataRepositoryLifecycleManager lifecycleManager;
  private File tempDir;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Create a temp directory on the filesystem to act as a FS repository
    tempDir = File.createTempFile("MetadataRepositoryLifecycleManagerTest-", null);
    assertTrue(tempDir.delete());
    assertTrue(tempDir.mkdir());

    final IRepositoryFileDao contentDao = new FileSystemRepositoryFileDao(tempDir);
    final IRepositoryFileAclDao repositoryFileAclDao = new FileSystemFileAclDao();
    final TransactionTemplate txnTemplate = new MockTransactionTemplate();
    lifecycleManager = new PentahoMetadataRepositoryLifecycleManager(contentDao, repositoryFileAclDao, txnTemplate,
        REPOSITORY_ADMIN_USERNAME, TENANT_AUTHENTICATED_AUTHORITY_NAME_PATTERN,
        SINGLE_TENANT_AUTHENTICATED_AUTHORITY_NAME);
    lifecycleManager.startup();
  }

  @Override
  protected void tearDown() throws Exception {
    lifecycleManager = null;
    super.tearDown();
  }

  @Test
  public void testDoNewTenant() throws Exception {
    // Save the current session
    final IPentahoSession currentSession = PentahoSessionHolder.getSession();

    try {
      lifecycleManager.newTenant(TEST_TENANT_ID);
      fail("The /etc folder is not setup and this should cause a failure");
    } catch (Exception success) {
      assertEquals(currentSession, PentahoSessionHolder.getSession());
    }

    // Create the folder and it should work
    final String tenantEtcFolderPath = ServerRepositoryPaths.getTenantEtcFolderPath(TEST_TENANT_ID);
    createFolder(tenantEtcFolderPath);
    lifecycleManager.newTenant(TEST_TENANT_ID);
    assertTrue(new File(tempDir, tenantEtcFolderPath + "/metadata").exists());
    assertEquals(currentSession, PentahoSessionHolder.getSession());

    // Nothing should change if we run it again
    lifecycleManager.newTenant(TEST_TENANT_ID);
    assertTrue(new File(tempDir, tenantEtcFolderPath + "/metadata").exists());
    assertEquals(currentSession, PentahoSessionHolder.getSession());
  }

  @Test
  public void testDoNewUser() throws Exception {
    // Nothing to test
    lifecycleManager.doNewUser(null, null);
  }

  @Test
  public void testDoShutdown() throws Exception {
    // Nothing to test
    lifecycleManager.doShutdown();
  }

  @Test
  public void testDoStartup() throws Exception {
    // Nothing to test
    lifecycleManager.doStartup();
  }

  private void createFolder(final String folderName) {
    final String[] folders = StringUtils.split(folderName, '/');
    File currentDir = tempDir;
    for (final String newFolder : folders) {
      currentDir = new File(currentDir, newFolder);
      assertTrue(currentDir.mkdir());
    }
  }

  private class MockTransactionTemplate extends TransactionTemplate {
    @Override
    public Object execute(final TransactionCallback action) throws TransactionException {
      return action.doInTransaction(new MockTransactionStatus());
    }
  }

  /**
   *
   */
  private class MockTransactionStatus implements TransactionStatus {
    public boolean isNewTransaction() {
      return false;
    }

    public boolean hasSavepoint() {
      return false;
    }

    public void setRollbackOnly() {
    }

    public boolean isRollbackOnly() {
      return false;
    }

    public boolean isCompleted() {
      return false;
    }

    public Object createSavepoint() throws TransactionException {
      return null;
    }

    public void rollbackToSavepoint(final Object o) throws TransactionException {
    }

    public void releaseSavepoint(final Object o) throws TransactionException {
    }
  }
}
