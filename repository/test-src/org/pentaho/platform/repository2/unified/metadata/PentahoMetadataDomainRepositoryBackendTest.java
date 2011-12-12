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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ_ACL;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE_ACL;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository.SpringSecurityCurrentUserProvider;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

/**
 * User: dkincade
 */
@SuppressWarnings("nls")
public class PentahoMetadataDomainRepositoryBackendTest {
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
  
  private MockUnifiedRepository repository;
  
  /**
   * Class under test.
   */
  private PentahoMetadataDomainRepositoryBackend backend;

  @BeforeClass
  public static void setUpClass() {
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("joe", null, new GrantedAuthority[0]));
  }
  
  @AfterClass
  public static void tearDownClass() {
    SecurityContextHolder.clearContext();
  }
  
  @Before
  public void setUp() {
    repository = new MockUnifiedRepository(new SpringSecurityCurrentUserProvider());
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(MockUnifiedRepository.root().getName(), null, new GrantedAuthority[0]));
    try {
    repository.createFolder(repository.getFile(RepositoryFile.SEPARATOR).getId(), new RepositoryFile.Builder(ClientRepositoryPaths.getEtcFolderName()).folder(true).build(), 
        new RepositoryFileAcl.Builder(MockUnifiedRepository.root()).ace(MockUnifiedRepository.everyone(), 
            READ, READ_ACL, WRITE, WRITE_ACL).build(), null);
    } finally {
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("joe", null, new GrantedAuthority[0]));
    }
    backend = new PentahoMetadataDomainRepositoryBackend(repository);
  }
  
  private void writeProperties(final Properties properties) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    properties.store(out, null);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(MockUnifiedRepository.root().getName(), null, new GrantedAuthority[0]));
    try {
    RepositoryFile f = repository.getFile(METADATA_INFO.getMetadataFolderPath());
    // get or create
    if (f == null) {
      f = repository.createFolder(repository.getFile(METADATA_INFO.getMetadataParentPath()).getId(), new RepositoryFile.Builder(METADATA_INFO.getMetadataFolderName()).folder(true).build(), null);
    }
    
    repository.createFile(f.getId(), new RepositoryFile.Builder(METADATA_INFO.getMetadataMappingFileName()).build(), 
        new SimpleRepositoryFileData(in, "ISO8859_1", "text/plain"), null);
    } finally {
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("joe", null, new GrantedAuthority[0]));
    }
  }
  
  private Properties readProperties() throws Exception {
    RepositoryFile f = repository.getFile(METADATA_INFO.getMetadataMappingFilePath());
    SimpleRepositoryFileData data = repository.getDataForRead(f.getId(), SimpleRepositoryFileData.class);
    Properties p = new Properties();
    p.load(data.getStream());
    return p;
  }
  
  @Test
  public void testLoadDomainMappingFromRepositoryEmpty() throws Exception {
    // If there is no entries, it should work fine
    writeProperties(new Properties());
    
    final Map<String, RepositoryFile> emptyMappings = backend.loadDomainMappingFromRepository();
    assertNotNull(emptyMappings);
    assertEquals(0, emptyMappings.size());
  }
  
  @Test
  public void testLoadDomainMappingFromRepositoryCorrupt() throws Exception {
    corrupt();
    
    // This should throw an exception
    try {
      backend.loadDomainMappingFromRepository();
      fail("Exception should be thrown if the system has become corrupt");
    } catch (Exception success) {
    }
  }
  
  @Test
  public void testLoadDomainMappingFromRepositorySuccess() throws Exception {
    // Setup a fake mapping properties file in the repository
    writeProperties(SAMPLE_PROPERTIES_FILE);
    
    // Test the success condition
    final Map<String, RepositoryFile> mappings = backend.loadDomainMappingFromRepository();
    assertNotNull(mappings);
    assertEquals(SAMPLE_PROPERTIES_FILE.size(), mappings.size());
    assertTrue(mappings.containsKey(COMPLEX_NAME));
  }

  @Test
  public void testMetadataFolderDoesNotExist() throws Exception {
    // With the parent directory defined, this should now create the file and succeed
      final RepositoryFile mappingPropertiesFile = backend.getMappingPropertiesFile();
      assertNotNull(mappingPropertiesFile);
      assertNotNull(repository.getFile(METADATA_INFO.getMetadataFolderPath()));
      assertNotNull(repository.getFile(METADATA_INFO.getMetadataMappingFilePath()));
    }

    @Test
    public void testMetadataFolderExists() throws Exception {
    // Make sure this works if the metadata path exists but does not contain a mapping file
    repository.createFolder(repository.getFile(METADATA_INFO.getMetadataParentPath()).getId(), new RepositoryFile.Builder(METADATA_INFO.getMetadataFolderName()).folder(true).build(), null);

      final RepositoryFile mappingPropertiesFile = backend.getMappingPropertiesFile();
      assertNotNull(mappingPropertiesFile);
      assertNotNull(repository.getFile(METADATA_INFO.getMetadataFolderPath()));
      assertNotNull(repository.getFile(METADATA_INFO.getMetadataMappingFilePath()));
    
  }

  @Test
  public void testLoadDomain() throws Exception {

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
    writeProperties(SAMPLE_PROPERTIES_FILE);
    writeComplex();
    
    // Success test
    final Domain complexDomain = backend.loadDomain(COMPLEX_NAME, new RepositoryFile.Builder(COMPLEX_PATH,
        COMPLEX_PATH).path(COMPLEX_PATH).build());
    assertNotNull(complexDomain);
    assertEquals(COMPLEX_NAME, complexDomain.getId());
  }

  @Test
  public void testRemoveDomainFromRepositoryBadInput() throws Exception {
    // Bad input should yield exception
    try {
      backend.removeDomainFromRepository(null);
      fail();
    } catch (Exception success) {
    }
  }
  
  @Test
  public void testRemoveDomainFromRepositoryCorrupt() throws Exception {
    corrupt();
    
    // Corrupt repo should throw an exception
    try {
      backend.removeDomainFromRepository(COMPLEX_NAME);
      fail();
    } catch (Exception success) {
    }
  }
  
  private void writeComplex() {
    RepositoryFile p = repository.createFolder(repository.getFile(METADATA_INFO.getMetadataFolderPath()).getId(), new RepositoryFile.Builder(COMPLEX_NAME).folder(true).build(), null);
    repository.createFile(p.getId(), new RepositoryFile.Builder(METADATA_INFO.getMetadataFilename()).build(), new SimpleRepositoryFileData(new ByteArrayInputStream(new byte[0]), "UTF-8", "text/xml"), null);
  }
  
  @Test
  public void testRemoveDomainFromRepositoryDoesNotExist2() throws Exception {
    // Add the sample mappings and the complex domain information
    writeProperties(SAMPLE_PROPERTIES_FILE);
    writeComplex();
    
    // This should not delete anything
    backend.removeDomainFromRepository(COMPLEX_NAME + " does not exist");
    assertNotNull(repository.getFile(COMPLEX_PATH));
    assertEquals(SAMPLE_PROPERTIES_FILE, readProperties());
  }
  
  @Test
  public void testRemoveDomainFromRepositorySuccess() throws Exception {
    // Add the sample mappings and the complex domain information
    writeProperties(SAMPLE_PROPERTIES_FILE);
    writeComplex();

    // This one should work
    backend.removeDomainFromRepository(COMPLEX_NAME);
    assertNull(repository.getFile(COMPLEX_PATH));
    final Properties actual2 = readProperties();
    assertEquals(SAMPLE_PROPERTIES_FILE.size() - 1, actual2.size());

  }
  
  @Test
  public void testRemoveDoaminFromRepositoryClear() throws Exception {
    // Add the sample mappings and the complex domain information
    writeProperties(SAMPLE_PROPERTIES_FILE);
    writeComplex();
    
    // Delete all the domain IDs
    while(backend.getMetadataMappingFile().size() > 0) {
      final String domainId = backend.getMetadataMappingFile().stringPropertyNames().iterator().next();
      backend.removeDomainFromRepository(domainId);
    }
    assertNotNull(repository.getFile(METADATA_INFO.getMetadataMappingFilePath()));
  }

  
  @Test
  public void testAddDomainToRepositoryBadInput() throws Exception {
    backend.setXmiParser(new MockXmiParser());

    // Bad input should yield exception
    try {
      backend.addDomainToRepository(null);
      fail();
    } catch (Exception success) {
    }
  }
  
  @Test
  public void testAddDomainToRepositoryBad2() throws Exception {
    backend.setXmiParser(new MockXmiParser());

    try {
      backend.addDomainToRepository(new MockDomain(null));
      fail();
    } catch (Exception success) {
    }
  }
  
  @Test
  public void testAddDomainToRepositoryCorrupt() throws Exception {
    corrupt();
    
    backend.setXmiParser(new MockXmiParser());
    
    // Corrupt repo should throw an exception
    try {
      backend.addDomainToRepository(new MockDomain("test"));
      fail();
    } catch (Exception success) {
    }
  }
  
  @Test
  public void testAddDomainToRepository() throws Exception {
    backend.setXmiParser(new MockXmiParser());
    
    final String domainId = "Test Domain";
    final String expectedPath = METADATA_INFO.getMetadataFolderPath() + "/"+domainId+"/"+METADATA_INFO.getMetadataFilename();


    // Success test
    writeProperties(SAMPLE_PROPERTIES_FILE);

    final MockDomain mockDomain = new MockDomain(domainId);
    backend.addDomainToRepository(mockDomain);
    assertNotNull(repository.getFile(METADATA_INFO.getMetadataMappingFilePath()));
    assertNotNull(repository.getFile(expectedPath));

    // Ensure the correct information is being stored in the mappings file
    Properties properties = readProperties();
    final String loc = properties.getProperty(domainId);
    assertNotNull(loc);
    assertEquals(expectedPath, loc);

    final Map<String, RepositoryFile> repositoryFileMap1 = backend.loadDomainMappingFromRepository();
    assertNotNull(repositoryFileMap1);
    assertNotNull(repositoryFileMap1.containsKey(domainId));
    assertEquals(expectedPath, repositoryFileMap1.get(domainId).getPath());
    final Domain domain1 = backend.loadDomain(domainId, repositoryFileMap1.get(domainId));

    // Overwrite test
    assertNotNull(domain1);
    backend.setXmiParser(new AlteredMockXmiParser());
    backend.addDomainToRepository(mockDomain);
    final Map<String, RepositoryFile> repositoryFileMap2 = backend.loadDomainMappingFromRepository();
    final Domain domain2 = backend.loadDomain(domainId, repositoryFileMap2.get(domainId));
    assertEquals(domain1.getId(), domain2.getId());
    assertNotSame(domain1, domain2);
  }

  private class AlteredMockXmiParser extends MockXmiParser {
    @Override
    public String generateXmi(final Domain domain) {
      return super.generateXmi(domain)+super.generateXmi(domain);
    }
  }
  
  private void corrupt() {
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(MockUnifiedRepository.root().getName(), null, new GrantedAuthority[0]));
    try {
      repository.deleteFile(repository.getFile(ClientRepositoryPaths.getEtcFolderPath()).getId(), null);
    } finally {
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("joe", null, new GrantedAuthority[0]));
    }
    
  }
}
