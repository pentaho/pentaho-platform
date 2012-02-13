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
package org.pentaho.platform.plugin.services.importexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importexport.legacy.ZipSolutionRepositoryImportSource;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.repository2.unified.EmptyUnifiedRepository;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.pentaho.test.platform.repository2.unified.UnmodifiableRepository;

import junit.framework.TestCase;

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

      // Make sure it created the correct tree structure
      assertTrue(repository.getFile("/home/user/pentaho-solutions").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers/analysis").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers/cdf-samples").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers/cdf-samples/10-cdf").isFolder());
      assertTrue(repository.getFile(
          "/home/user/pentaho-solutions/bi-developers/cdf-samples/30-documentation").isFolder());
      assertTrue(repository.getFile(
          "/home/user/pentaho-solutions/bi-developers/cdf-samples/30-documentation/30-component_reference").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers/cdf-samples/30-documentation/30-component_reference/10-core").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers/cdf-samples/30-documentation/30" +
          "-component_reference/10-core/31-TimePlotComponent").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers/charts").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers/datasources").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers/reporting").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/bi-developers/reporting/steel-wheels-reports")
          .isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/metadata-test").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/steel-wheels").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/steel-wheels/analysis").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/steel-wheels/reports").isFolder());
      assertTrue(repository.getFile("/home/user/pentaho-solutions/test").isFolder());

      // Make sure it didn't also flatten the structure
      assertNull(repository.getFile("/home/user/bi-developers"));
      assertNull(repository.getFile("/home/user/steel-wheels"));
      assertNull(repository.getFile("/home/user/test"));

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

      final DefaultImportHandler handler = new DefaultImportHandler(new UnmodifiableRepository(repository));
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

  public void testGetParentId() throws Exception {
    final SimpleMockRepository repo = new SimpleMockRepository();
    final DefaultImportHandler handler = new DefaultImportHandler(repo);
    {
      final Serializable parentId = handler.getParentId("/public/pentaho-solutions");
      assertNotNull(parentId);
      assertEquals("/public", (String) parentId);
      assertTrue(repo.getCreated());
      repo.reset();
    }
    {
      final Serializable parentId = handler.getParentId("/public/pentaho-solutions");
      assertNotNull(parentId);
      assertEquals("/public", (String) parentId);
      assertFalse(repo.getCreated());
      repo.reset();
    }
    {
      final Serializable parentId = handler.getParentId("/public/pentaho-solutions/");
      assertNotNull(parentId);
      assertEquals("/public/pentaho-solutions", (String) parentId);
      repo.reset();
    }
    {
      final Serializable parentId = handler.getParentId("/public/pentaho-solutions/bi-developers");
      assertNotNull(parentId);
      assertEquals("/public/pentaho-solutions", (String) parentId);
      assertFalse(repo.getCreated());
      repo.reset();
    }
  }

  private ZipInputStream getZipInputStream(final String path) throws FileNotFoundException {
    return new ZipInputStream(new FileInputStream(path));
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

  /**
   * Simple mock IUnifiedRepository implementation that will be used to test directory search and creation. The
   * getFile() method will always return a file with the id() the same as the normalized path. It has 2 boolean flags
   * that indicate if the getFile() method was called and if the getFile() was called on a file for the 1st time.
   */
  private class SimpleMockRepository extends EmptyUnifiedRepository {
    private boolean created = false;
    private final Map<String, RepositoryFile> map = new HashMap<String, RepositoryFile>();

    public void reset() {
      created = false;
    }

    public boolean getCreated() {
      return created;
    }

    public RepositoryFile getFile(String path) {
      path = RepositoryFilenameUtils.normalize(path, true);
      RepositoryFile file = map.get(path);
      if (file == null) {
        created = true;
        final String filename = RepositoryFilenameUtils.getName(path);
        file = new RepositoryFile.Builder(filename).id(path).path(path).build();
        map.put(path, file);
      } else {
        throw new IllegalStateException("This indicates the caching in getParentId() has failed!");
      }
      return file;
    }
  }
}
