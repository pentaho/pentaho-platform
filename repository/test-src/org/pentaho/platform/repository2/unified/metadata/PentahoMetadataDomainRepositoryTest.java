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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ_ACL;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE_ACL;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
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
 * Class Description
 * User: dkincade
 */
@SuppressWarnings("nls")
public class PentahoMetadataDomainRepositoryTest {
  protected static final JcrMetadataInfo METADATA_INFO = new JcrMetadataInfo();
  private static final String DEFAULT_DOMAIN_NAME = "Default";
  private static final String COMPLEX_DOMAIN_NAME = "Sample (!@#$%^&*_-[]{};:'\",.<>?`~)";

  private PentahoMetadataDomainRepository mdr;
  private PentahoMetadataDomainRepositoryBackend backend;
  private MockUnifiedRepository repository;
  private Properties samplePropertiesFile;

  @Before
  public void setUp() throws Exception {
    samplePropertiesFile = new Properties();

    repository = new MockUnifiedRepository(new SpringSecurityCurrentUserProvider());
    
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(MockUnifiedRepository.root().getName(), null, new GrantedAuthority[0]));
    try {
    RepositoryFile etcFolder = repository.createFolder(repository.getFile(RepositoryFile.SEPARATOR).getId(), new RepositoryFile.Builder(ClientRepositoryPaths.getEtcFolderName()).folder(true).build(), 
        new RepositoryFileAcl.Builder(MockUnifiedRepository.root()).ace(MockUnifiedRepository.everyone(), 
            READ, READ_ACL, WRITE, WRITE_ACL).build(), null);
    RepositoryFile metadataFolder = repository.createFolder(etcFolder.getId(), new RepositoryFile.Builder(METADATA_INFO.getMetadataFolderName()).folder(true).build(), null);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    samplePropertiesFile.store(out, null);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    repository.createFile(metadataFolder.getId(), new RepositoryFile.Builder(METADATA_INFO.getMetadataMappingFileName()).build(), 
        new SimpleRepositoryFileData(in, "ISO8859_1", "text/plain"), null);
    } finally {
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("joe", null, new GrantedAuthority[0]));
    }


    backend = new PentahoMetadataDomainRepositoryBackend(repository);
    backend.setXmiParser(new MockXmiParser());
    backend.addDomainToRepository(new MockDomain(COMPLEX_DOMAIN_NAME));
    backend.addDomainToRepository(new MockDomain(DEFAULT_DOMAIN_NAME));

    mdr = new PentahoMetadataDomainRepository(backend);
  }

  @After
  public void tearDown() throws Exception {
    mdr = null;
    backend = null;
    repository = null;
    samplePropertiesFile = null;
  }

  @Test
  public void testGetDomainIds() throws Exception {
    final Set<String> domainIds = mdr.getDomainIds();
    assertNotNull(domainIds);
    assertEquals(2, domainIds.size());
    assertTrue(domainIds.contains("Default"));
    assertTrue(domainIds.contains(COMPLEX_DOMAIN_NAME));
  }
  
  @Test
  public void testGetDomain() throws Exception {
    try {
      mdr.getDomain(null);
      fail("null domain id should throw exception");
    } catch (Exception success) {

    }
    assertNull(mdr.getDomain("domain doesn't exist"));

    // Success 1
    final Domain domain = mdr.getDomain(COMPLEX_DOMAIN_NAME);
    assertNotNull(domain);
    assertEquals(COMPLEX_DOMAIN_NAME, domain.getId());

    // Add a domain and force the repo to refresh to find it
    final String domainIdJustAdded = "Just Added";
    backend.addDomainToRepository(new MockDomain(domainIdJustAdded));
    final Domain domainJustAdded = mdr.getDomain(domainIdJustAdded);
    assertNotNull(domainJustAdded);
    assertEquals(domainIdJustAdded, domainJustAdded.getId());
  }

  @Test
  public void testReloadDomains() throws Exception {
    final int before = mdr.getLoadedDomainCount();
    mdr.reloadDomains();
    final int during = mdr.getLoadedDomainCount();
    mdr.flushDomains();
    final int after = mdr.getLoadedDomainCount();
    assert (0 == before);
    assert (0 < during);
    assert (0 == after);
  }

  @Test
  public void testFlushDomains() throws Exception {
    mdr.reloadDomains();
    final int before = mdr.getLoadedDomainCount();
    assertTrue("The initial check should have one or domains loaded, but it has " + before, 0 < before);
    mdr.flushDomains();
    final int after = mdr.getLoadedDomainCount();
    assertTrue("After flushDomains(), there should be 0 domains loaded, but there are " + after, 0 == after);
  }

  @Test
  public void testStoreDomain() throws Exception {
    // Exceptions
    try {
      mdr.storeDomain(null, false);
      fail("Null domain id should throw exception");
    } catch (Exception success) {
    }
    try {
      mdr.storeDomain(new MockDomain(null), false);
      fail("Domain with null domain id should throw exception");
    } catch (Exception success) {
    }
    try {
      mdr.storeDomain(new MockDomain(COMPLEX_DOMAIN_NAME), false);
      fail("Domain that already exists but overwrite set to false should throw exception");
    } catch (Exception success) {
    }

    // Success -- overwrite a current one
    final Set<String> beforeDomainIds = mdr.getDomainIds();
    assertTrue(0 < beforeDomainIds.size());
    {
      mdr.storeDomain(new MockDomain(COMPLEX_DOMAIN_NAME), true);
      final Set<String> afterDomainIds = mdr.getDomainIds();
      assertEquals(beforeDomainIds.size(), afterDomainIds.size());
      for (final String domainId : beforeDomainIds) {
        assertTrue(afterDomainIds.contains(domainId));
      }
    }

    // Success -- add a new one
    {
      final String newDomainId = "new domain";
      mdr.storeDomain(new MockDomain(newDomainId), false);
      final Set<String> afterDomainIds = mdr.getDomainIds();
      assertEquals(beforeDomainIds.size() + 1, afterDomainIds.size());
      assertFalse(beforeDomainIds.contains(newDomainId));
      assertTrue(afterDomainIds.contains(newDomainId));
      for (final String domainId : beforeDomainIds) {
        assertTrue(afterDomainIds.contains(domainId));
      }
    }
  }

  @Test
  public void testRemoveDomain() throws Exception {
    // Exceptions
    try {
      mdr.removeDomain(null);
      fail("removeDomain should fail with a null domain ID");
    } catch (Exception success) {
    }

    // Removing a domain that doesn't exist should work
    final Set<String> beforeDomainIds = mdr.getDomainIds();
    {
      mdr.removeDomain("doesn't exist");
      final Set<String> afterDomainIds = mdr.getDomainIds();
      assertEquals(beforeDomainIds.size(), afterDomainIds.size());
    }

    // Success
    {
      mdr.removeDomain(COMPLEX_DOMAIN_NAME);
      final Set<String> afterDomainIds = mdr.getDomainIds();
      assertEquals(beforeDomainIds.size() - 1, afterDomainIds.size());
      assertFalse(afterDomainIds.contains(COMPLEX_DOMAIN_NAME));
    }

    // All should work when all domains are gone
    for (final String domainId : beforeDomainIds) {
      mdr.removeDomain(domainId);
    }
    assertEquals(0, mdr.getDomainIds().size());
    mdr.reloadDomains();
    assertEquals(0, mdr.getDomainIds().size());
  }

  @Test
  public void testRemoveModel() throws Exception {

  }
}
