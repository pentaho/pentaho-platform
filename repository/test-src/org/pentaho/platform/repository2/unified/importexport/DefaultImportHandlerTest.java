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
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.repository2.unified.importexport.legacy.ZipSolutionRepositoryImportSource;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
  private static final int FINAL_COUNT = 22;

  private IUnifiedRepository repository;

  @Override
  protected void setUp() throws Exception {
    // Define a repository for testing
    // TODO : talk to Mat L about why the mock repo doesn't work here
//    repository = new MockUnifiedRepository(new MockUserProvider());
    repository = new FileSystemBackedUnifiedRepository(createTempDir());
    new RepositoryUtils(repository).getFolder("/etc/metadata", true, true, null);
    new RepositoryUtils(repository).getFolder("/public", true, true, null);
    new RepositoryUtils(repository).getFolder("/home/user", true, true, null);
  }

  private static File createTempDir() throws IOException {
    final File tempDir = File.createTempFile("DefaultImportHandlerTest-", "");
    assertTrue(tempDir.delete());
    assertTrue(tempDir.mkdir());
    return tempDir;
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
      assertEquals("The test should start with exactly 65 files", 65, importSource.getCount());

      final DefaultImportHandler handler = new DefaultImportHandler(repository);
      handler.doImport(importSource.getFiles(), "/home/user", "import comment", true);

      // The import handler should skip any file (or directory) with 'system' or 'admin' in first 2 path locations
      // and files without extensions, mime-types, and converters
      assertEquals("The test should have ended without processing " + FINAL_COUNT + " files", FINAL_COUNT, importSource.getCount());

      assertNull(repository.getFile("/pentaho-solutions/system/metadata/README.txt"));
      final RepositoryFile urlFile = repository.getFile("/home/user/pentaho-solutions/bi-developers/charts/chartsamplesdashboard.url");
      assertNotNull(urlFile);
      assertFalse(urlFile.isHidden()); /// URL is an executable type
      final RepositoryFile xactionFile = repository.getFile("/home/user/pentaho-solutions/bi-developers/reporting/JFree_Quad.xaction");
      assertNotNull(xactionFile);
      assertFalse(xactionFile.isHidden());
      final RepositoryFile folder = repository.getFile("/home/user/pentaho-solutions/bi-developers/datasources/");
      assertNotNull(folder);
      assertTrue(folder.isFolder());
      assertFalse(folder.isHidden());
      final RepositoryFile xmlFile = repository.getFile("/home/user/pentaho-solutions/bi-developers/cdf-samples/10-cdf/index.xml");
      assertNotNull(xmlFile);
      // TODO: the filesystem repository dao doesn't support hidden / not hidden - fix and uncomment the following test
//      assertTrue(xmlFile.isHidden());
      final RepositoryFile prptWithSpaces = repository.getFile("/home/user/pentaho-solutions/steel-wheels/reports/Buyer Product Analysis.prpt");
      assertNotNull(prptWithSpaces);

      // Since we don't have access to the plugins, a .prpt file will not be known as an executable type and therefore hidden
      // TODO: the filesystem repository dao doesn't support hidden / not hidden - fix and uncomment the following test
//      assertTrue(prptWithSpaces.isHidden());

      // Ensure the system and admin folders don't show up
      assertNull(repository.getFile("/home/user/pentaho-solutions/admin"));
      assertNull(repository.getFile("/home/user/pentaho-solutions/system"));

    } finally {
      IOUtils.closeQuietly(zis);
    }

    // Re-import the same file with overwrite FALSE (should not change any files) but all should still be marked
    // as processed)
    zis = null;
    try {
      zis = getZipInputStream("testdata/pentaho-solutions.zip");
      final ZipSolutionRepositoryImportSource importSource = new ZipSolutionRepositoryImportSource(zis, "UTF-8");
      assertEquals("The test should start with exactly 65 files", 65, importSource.getCount());

      final DefaultImportHandler handler = new DefaultImportHandler(repository.unmodifiable());
      handler.doImport(importSource.getFiles(), "/home/user", "import comment", false);

      // The import handler should skip any file (or directory) with 'system' or 'admin' in first 2 path locations
      // and files without extensions, mime-types, and converters
      assertEquals("The test should have ended without processing " + FINAL_COUNT + " files", FINAL_COUNT, importSource.getCount());
    } finally {
      IOUtils.closeQuietly(zis);
    }


    // Re-import with overwrite TRUE
    zis = null;
    try {
      zis = getZipInputStream("testdata/pentaho-solutions.zip");
      final ZipSolutionRepositoryImportSource importSource = new ZipSolutionRepositoryImportSource(zis, "UTF-8");
      assertEquals("The test should start with exactly 65 files", 65, importSource.getCount());

      final DefaultImportHandler handler = new DefaultImportHandler(repository);
      handler.doImport(importSource.getFiles(), "/home/user", "import comment", true);

      // The import handler should skip any file (or directory) with 'system' or 'admin' in first 2 path locations
      // and files without extensions, mime-types, and converters
      assertEquals("The test should have ended without processing " + FINAL_COUNT + " files", FINAL_COUNT, importSource.getCount());
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
   * Simple user provider
   */
  private class MockUserProvider implements MockUnifiedRepository.ICurrentUserProvider {
    @Override
    public String getUser() {
      return MockUnifiedRepository.root().getName();
    }

    @Override
    public List<String> getRoles() {
      return new ArrayList<String>();
    }
  }
}
