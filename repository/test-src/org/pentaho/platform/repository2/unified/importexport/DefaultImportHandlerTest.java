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
package org.pentaho.platform.repository2.unified.importexport;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.repository2.unified.importexport.legacy.ZipSolutionRepositoryImportSource;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class DefaultImportHandlerTest extends TestCase {
  private static Log log = LogFactory.getLog(DefaultImportHandlerTest.class);

  private IUnifiedRepository repository;
  private File tempDir;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Create a temp directory for use by the FileSystem repository
    tempDir = File.createTempFile("DefaultImportHandlerTest-", "");
    assertTrue(tempDir.delete());
    assertTrue(tempDir.mkdir());
    repository = new FileSystemBackedUnifiedRepository(tempDir);

    // Create the /etc and /etc/public folders
    repository.createFolder(repository.getFile("/").getId(), new RepositoryFile.Builder("etc").folder(true).build(), "initialization");
    final RepositoryFile publicFolder
        = repository.createFolder(repository.getFile("/").getId(), new RepositoryFile.Builder("public").folder(true).build(), "initialization");
    repository.createFolder(publicFolder.getId(), new RepositoryFile.Builder("user").folder(true).build(), "initialization");
  }

  public void testInitialize() throws Exception {
    try {
      new DefaultImportHandler(null);
      fail("Passing a null repository should throw exception");
    } catch (Exception success) {
    }

    final DefaultImportHandler handler = new DefaultImportHandler(repository);
    assertFalse(StringUtils.isEmpty(handler.getName()));

    final Set<String> executableTypes = handler.getExecutableTypes();
    assertNotNull(executableTypes);
    assertTrue(!executableTypes.isEmpty());
    assertTrue(executableTypes.contains("xaction"));
    assertTrue(executableTypes.contains("url"));

    final Map<String, Serializable> parentIdCache = handler.getParentIdCache();
    assertNotNull(parentIdCache);
    assertTrue(parentIdCache.isEmpty()); /// It starts empty

    final Map<String, Converter> converters = handler.getConverters();
    assertNotNull(converters);
    assertTrue(!converters.isEmpty());
  }

  public void testIsSystemPath() throws Exception {
    assertTrue(DefaultImportHandler.isSystemPath("system/jackrabbit/repository.xml"));
    assertTrue(DefaultImportHandler.isSystemPath("/system/jackrabbit/repository.xml"));
    assertTrue(DefaultImportHandler.isSystemPath("pentaho-solutions/system/jackrabbit/repository.xml"));
    assertTrue(DefaultImportHandler.isSystemPath("/pentaho-solutions/system/jackrabbit/repository.xml"));
    assertTrue(DefaultImportHandler.isSystemPath("/pentaho-solutions/admin"));
    assertFalse(DefaultImportHandler.isSystemPath(null));
    assertFalse(DefaultImportHandler.isSystemPath(""));
    assertFalse(DefaultImportHandler.isSystemPath("/solutions/user/system"));
    assertFalse(DefaultImportHandler.isSystemPath("/solutions/user/system/"));
    assertFalse(DefaultImportHandler.isSystemPath("/solutions/user/system/init.xml"));
    assertFalse(DefaultImportHandler.isSystemPath("/solutions/user/admin/init.xml"));
    assertFalse(DefaultImportHandler.isSystemPath("/one/two/three/four"));
  }

  public void testDoImport() throws Exception {
    ZipInputStream zis = null;
    try {
      // Use the test ZIP file and the ZipSolutionRepositoryImportSource
      zis = getZipInputStream("testdata/pentaho-solutions.zip");
      final ZipSolutionRepositoryImportSource importSource = new ZipSolutionRepositoryImportSource(zis, "UTF-8");
      importSource.initialize();
      assertEquals("The test should start with exactly 65 files", 65, importSource.getCount());

      final DefaultImportHandler handler = new DefaultImportHandler(repository);
      handler.doImport(importSource.getFiles(), "/public/user", "import comment", true);

      // The import handler should skip any file (or directory) with 'system' or 'admin' in first 2 path locations
      // and files without extensions, mime-types, and converters
      assertEquals("The test should have ended without processing 20 files", 20, importSource.getCount());

      assertNull(repository.getFile("/pentaho-solutions/system/metadata/README.txt"));
      final RepositoryFile urlFile = repository.getFile("/public/user/pentaho-solutions/bi-developers/charts/chartsamplesdashboard.url");
      assertNotNull(urlFile);
      assertFalse(urlFile.isHidden()); /// URL is an executable type
      final RepositoryFile xactionFile = repository.getFile("/public/user/pentaho-solutions/bi-developers/reporting/JFree_Quad.xaction");
      assertNotNull(xactionFile);
      assertFalse(xactionFile.isHidden());
      final RepositoryFile folder = repository.getFile("/public/user/pentaho-solutions/bi-developers/datasources/");
      assertNotNull(folder);
      assertTrue(folder.isFolder());
      assertFalse(folder.isHidden());
      final RepositoryFile xmlFile = repository.getFile("/public/user/pentaho-solutions/bi-developers/cdf-samples/10-cdf/index.xml");
      assertNotNull(xmlFile);
      // TODO: the filesystem repository dao doesn't support hidden / not hidden - fix and uncomment the following test
//      assertTrue(xmlFile.isHidden());
      final RepositoryFile prptWithSpaces = repository.getFile("/public/user/pentaho-solutions/steel-wheels/reports/Buyer Product Analysis.prpt");
      assertNotNull(prptWithSpaces);

      // Since we don't have access to the plugins, a .prpt file will not be known as an executable type and therefore hidden
      // TODO: the filesystem repository dao doesn't support hidden / not hidden - fix and uncomment the following test
//      assertTrue(prptWithSpaces.isHidden());
    } finally {
      IOUtils.closeQuietly(zis);
    }

    // Re-import the same file with overwrite FALSE (should not change any files) but all should still be marked
    // as processed)
    zis = null;
    try {
      zis = getZipInputStream("testdata/pentaho-solutions.zip");
      final ZipSolutionRepositoryImportSource importSource = new ZipSolutionRepositoryImportSource(zis, "UTF-8");
      importSource.initialize();
      assertEquals("The test should start with exactly 65 files", 65, importSource.getCount());

      final DefaultImportHandler handler = new DefaultImportHandler(new ReadOnlyRepository(tempDir));
      handler.doImport(importSource.getFiles(), "/public/user", "import comment", false);

      // The import handler should skip any file (or directory) with 'system' or 'admin' in first 2 path locations
      // and files without extensions, mime-types, and converters
      assertEquals("The test should have ended without processing 20 files", 20, importSource.getCount());
    } finally {
      IOUtils.closeQuietly(zis);
    }


    // Re-import with overwrite TRUE
    zis = null;
    try {
      zis = getZipInputStream("testdata/pentaho-solutions.zip");
      final ZipSolutionRepositoryImportSource importSource = new ZipSolutionRepositoryImportSource(zis, "UTF-8");
      importSource.initialize();
      assertEquals("The test should start with exactly 65 files", 65, importSource.getCount());

      final DefaultImportHandler handler = new DefaultImportHandler(repository);
      handler.doImport(importSource.getFiles(), "/public/user", "import comment", true);

      // The import handler should skip any file (or directory) with 'system' or 'admin' in first 2 path locations
      // and files without extensions, mime-types, and converters
      assertEquals("The test should have ended without processing 20 files", 20, importSource.getCount());
    } finally {
      IOUtils.closeQuietly(zis);
    }

  }

  private ZipInputStream getZipInputStream(final String path) {
    final InputStream inputStream = this.getClass().getResourceAsStream(path);
    assertNotNull(inputStream);
    return new ZipInputStream(inputStream);
  }

  /**
   * Mock repository that will throw an exception if the repository received a modify operation
   */
  private class ReadOnlyRepository extends FileSystemBackedUnifiedRepository {
    public ReadOnlyRepository(final File tempDir) {
      super(tempDir);
    }

    @Override
    public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData data, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
      return null;
    }

    @Override
    public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
      return null;
    }

    @Override
    public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
      return null;
    }

    @Override
    public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file, final RepositoryFileAcl acl, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
      return null;
    }

    @Override
    public void deleteFile(final Serializable fileId, final boolean permanent, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
    }

    @Override
    public void deleteFile(final Serializable fileId, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
    }

    @Override
    public void deleteFileAtVersion(final Serializable fileId, final Serializable versionId) {
      fail("Attempted a modify operation on a read-only repository");
    }

    @Override
    public void lockFile(final Serializable fileId, final String message) {
      fail("Attempted a modify operation on a read-only repository");
    }

    @Override
    public void moveFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
    }

    @Override
    public void restoreFileAtVersion(final Serializable fileId, final Serializable versionId, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
    }

    @Override
    public void undeleteFile(final Serializable fileId, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
    }

    @Override
    public void unlockFile(final Serializable fileId) {
      fail("Attempted a modify operation on a read-only repository");
    }

    @Override
    public RepositoryFileAcl updateAcl(final RepositoryFileAcl acl) {
      fail("Attempted a modify operation on a read-only repository");
      return null;
    }

    @Override
    public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
      return null;
    }

    @Override
    public void setFileMetadata(final Serializable fileId, final Map<String, Serializable> metadataMap) {
      fail("Attempted a modify operation on a read-only repository");
    }

    @Override
    public void copyFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
      fail("Attempted a modify operation on a read-only repository");
    }
  }
}
