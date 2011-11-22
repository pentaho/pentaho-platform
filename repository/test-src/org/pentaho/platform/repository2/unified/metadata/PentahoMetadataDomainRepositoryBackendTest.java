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
package org.pentaho.platform.repository2.unified.metadata;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.pms.core.exception.PentahoMetadataException;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

import java.util.Map;
import java.util.Properties;

/**
 * User: dkincade
 */
public class PentahoMetadataDomainRepositoryBackendTest extends TestCase {
  protected static final Properties SAMPLE_PROPERTIES_FILE = new Properties();
  protected static final JcrMetadataInfo METADATA_INFO = new JcrMetadataInfo();

  private static final String PREFIX = METADATA_INFO.getMetadataFolderPath() + "/";
  private static final String POSTFIX = "/" + METADATA_INFO.getMetadataFilename();
  private static final String COMPLEX_NAME = "Sample (!@#$%^&*_-[]{};:'\",.<>?`~)";
  private static final String COMPLEX_PATH = PREFIX + COMPLEX_NAME + POSTFIX;

  static {
    SAMPLE_PROPERTIES_FILE.setProperty("Default", PREFIX + "Default" + POSTFIX);
    SAMPLE_PROPERTIES_FILE.setProperty(COMPLEX_NAME, COMPLEX_PATH);
  }

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testLoadDomainMappingFromRepository() throws Exception {
    // Setup a fake mapping properties file in the repository
    final MockUnifiedRepository repository = new MockUnifiedRepository();
    repository.getFileTable().put(METADATA_INFO.getMetadataMappingFilePath(), SAMPLE_PROPERTIES_FILE);

    // Create the class under test
    final PentahoMetadataDomainRepositoryBackend backend = new PentahoMetadataDomainRepositoryBackend(repository);

    // Test the success condition
    final Map<String, RepositoryFile> mappings = backend.loadDomainMappingFromRepository();
    assertNotNull(mappings);
    assertEquals(SAMPLE_PROPERTIES_FILE.size(), mappings.size());
    assertTrue(mappings.containsKey(COMPLEX_NAME));

    // This should throw an exception
    try {
      repository.getFileTable().clear();
      backend.loadDomainMappingFromRepository();
      fail("Exception should be thrown if the system has become corrupt");
    } catch (Exception success) {
    }

    // If there is no entries, it should work find
    repository.getFileTable().put(METADATA_INFO.getMetadataMappingFilePath(), new Properties());
    final Map<String, RepositoryFile> emptyMappings = backend.loadDomainMappingFromRepository();
    assertNotNull(emptyMappings);
    assertEquals(0, emptyMappings.size());
  }

  @Test
  public void testMappingsFileDoesntExist() throws PentahoMetadataException {
    // With the parent directory defined, this should now create the file and succeed
    {
      final MockUnifiedRepository repository = new MockUnifiedRepository();
      final PentahoMetadataDomainRepositoryBackend backend = new PentahoMetadataDomainRepositoryBackend(repository);
      repository.getFileTable().put(METADATA_INFO.getMetadataParentPath(), null);
      final RepositoryFile mappingPropertiesFile = backend.getMappingPropertiesFile();
      assertNotNull(mappingPropertiesFile);
      assertNotNull(repository.getFileTable().containsKey(METADATA_INFO.getMetadataFolderPath()));
      assertNotNull(repository.getFileTable().get(METADATA_INFO.getMetadataMappingFilePath()));
    }

    // Make sure this works if the metadata path exists but does not contain a mapping file
    {
      final MockUnifiedRepository repository = new MockUnifiedRepository();
      final PentahoMetadataDomainRepositoryBackend backend = new PentahoMetadataDomainRepositoryBackend(repository);
      repository.getFileTable().put(METADATA_INFO.getMetadataParentPath(), null);
      repository.getFileTable().put(METADATA_INFO.getMetadataFolderPath(), null);
      final RepositoryFile mappingPropertiesFile = backend.getMappingPropertiesFile();
      assertNotNull(mappingPropertiesFile);
      assertNotNull(repository.getFileTable().containsKey(METADATA_INFO.getMetadataFolderPath()));
      assertNotNull(repository.getFileTable().get(METADATA_INFO.getMetadataMappingFilePath()));
    }
  }

  @Test
  public void testLoadDomain() throws Exception {
    final MockUnifiedRepository repository = new MockUnifiedRepository();

    // Create the class under test
    final PentahoMetadataDomainRepositoryBackend backend = new PentahoMetadataDomainRepositoryBackend(repository);
    backend.setXmiParser(new MockXmiParser());

    // An exception should be thrown if the system is corrupt
    try {
      backend.loadDomain("test", new RepositoryFile.Builder("test", "test").build());
      fail("Exception should be thrown if the system is corrupt");
    } catch (Exception success) {
    }

    // An exception should be thrown if bad parameters provided
    try {
      backend.loadDomain(null, new RepositoryFile.Builder("test", "test").build());
      fail("Exception should be thrown if domain is null");
    } catch (Exception success) {
    }
    try {
      backend.loadDomain("test", null);
      fail("Exception should be thrown if RepositoryFile is null");
    } catch (Exception success) {
    }
    try {
      backend.loadDomain("test", new RepositoryFile.Builder(null, "test").build());
      fail("Exception should be thrown if RepositoryFile ID is null");
    } catch (Exception success) {
    }

    // Add the sample mappings and the complex domain information
    repository.getFileTable().put(METADATA_INFO.getMetadataMappingFilePath(), SAMPLE_PROPERTIES_FILE);
    repository.getFileTable().put(COMPLEX_PATH, null);

    // Success test
    final Domain complexDomain = backend.loadDomain(COMPLEX_NAME, new RepositoryFile.Builder(COMPLEX_PATH,
        COMPLEX_PATH).path(COMPLEX_PATH).build());
    assertNotNull(complexDomain);
    assertEquals(COMPLEX_NAME, complexDomain.getId());
  }

  @Test
  public void testRemoveDomainFromRepository() throws Exception {
    final MockUnifiedRepository repository = new MockUnifiedRepository();

    // Create the class under test
    final PentahoMetadataDomainRepositoryBackend backend = new PentahoMetadataDomainRepositoryBackend(repository);

    // Bad input should yield exception
    try {
      backend.removeDomainFromRepository(null);
      fail();
    } catch (Exception success) {
    }

    // Corrupt repo should throw an exception
    try {
      backend.removeDomainFromRepository(COMPLEX_NAME);
      fail();
    } catch (Exception success) {
    }

    // Success test
    repository.getFileTable().put(METADATA_INFO.getMetadataMappingFilePath(), SAMPLE_PROPERTIES_FILE);
    repository.getFileTable().put(COMPLEX_PATH, null);

    // This should not delete anything
    backend.removeDomainFromRepository(COMPLEX_NAME + " does not exist");
    assertEquals(2, repository.getFileTable().size());
    assertEquals(SAMPLE_PROPERTIES_FILE, repository.getFileTable().get(METADATA_INFO.getMetadataMappingFilePath()));
    final Properties actual1 = (Properties) (repository.getFileTable().get(METADATA_INFO.getMetadataMappingFilePath()));
    assertEquals(SAMPLE_PROPERTIES_FILE.size(), actual1.size());

    // This one should work
    backend.removeDomainFromRepository(COMPLEX_NAME);
    assertEquals(1, repository.getFileTable().size());
    assertNotSame(SAMPLE_PROPERTIES_FILE, repository.getFileTable().get(METADATA_INFO.getMetadataMappingFilePath()));
    final Properties actual2 = (Properties) (repository.getFileTable().get(METADATA_INFO.getMetadataMappingFilePath()));
    assertEquals(SAMPLE_PROPERTIES_FILE.size() - 1, actual2.size());

    // Delete all the domain IDs
    while(backend.getMetadataMappingFile().size() > 0) {
      final String domainId = backend.getMetadataMappingFile().stringPropertyNames().iterator().next();
      backend.removeDomainFromRepository(domainId);
    }
    assertTrue(repository.getFileTable().containsKey(METADATA_INFO.getMetadataMappingFilePath()));
  }

  @Test
  public void testAddDomainToRepository() throws Exception {
    final MockUnifiedRepository repository = new MockUnifiedRepository();

    // Create the class under test
    final PentahoMetadataDomainRepositoryBackend backend = new PentahoMetadataDomainRepositoryBackend(repository);
    backend.setXmiParser(new MockXmiParser());

    // Bad input should yield exception
    try {
      backend.addDomainToRepository(null);
      fail();
    } catch (Exception success) {
    }
    try {
      backend.addDomainToRepository(new MockDomain(null));
      fail();
    } catch (Exception success) {
    }

    // Corrupt repo should throw an exception
    try {
      backend.addDomainToRepository(new MockDomain("test"));
      fail();
    } catch (Exception success) {
    }

    // Success test
    repository.createFolder("/etc");
    repository.createFolder("/etc/metadata");
    repository.getFileTable().put(METADATA_INFO.getMetadataMappingFilePath(), SAMPLE_PROPERTIES_FILE);
    assertEquals(3, repository.getFileTable().size());
    backend.addDomainToRepository(new MockDomain("Test Domain #1"));
    assertEquals(5, repository.getFileTable().size());
  }
}
