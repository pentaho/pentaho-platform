/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.metadata;

import org.junit.Test;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.test.platform.plugin.services.metadata.MockSessionAwareMetadataDomainRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionCachingMetadataDomainRepositoryTest {

  @Test
  public void testStoreAnnotationsXml() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    SessionCachingMetadataDomainRepository repo = spy( new SessionCachingMetadataDomainRepository( mock ) );
    PentahoMetadataDomainRepository delegate = mock( PentahoMetadataDomainRepository.class );

    String domainId = "myDomain";
    String annotationsXml = "<annotations/>";

    repo.storeAnnotationsXml( domainId, annotationsXml );
    verify( delegate, times( 0 ) ).storeAnnotationsXml( domainId, annotationsXml );

    repo = spy( new SessionCachingMetadataDomainRepository( delegate ) ); // use a valid delegate
    repo.storeAnnotationsXml( domainId, annotationsXml );
    verify( delegate, times( 1 ) ).storeAnnotationsXml( domainId, annotationsXml );
  }

  @Test
  public void testLoadAnnotationsXml() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    SessionCachingMetadataDomainRepository repo = spy( new SessionCachingMetadataDomainRepository( mock ) );
    PentahoMetadataDomainRepository delegate = mock( PentahoMetadataDomainRepository.class );

    String domainId = "myDomain";

    repo.loadAnnotationsXml( domainId );
    verify( delegate, times( 0 ) ).loadAnnotationsXml( domainId );

    repo = spy( new SessionCachingMetadataDomainRepository( delegate ) ); // use a valid delegate
    repo.loadAnnotationsXml( domainId );
    verify( delegate, times( 1 ) ).loadAnnotationsXml( domainId );
  }

  @Test
  public void shouldNotUseDomainIdsCacheIfDisabled() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    PentahoSessionHolder.setSession( new StandaloneSession( "session", "1" ) );
    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    Domain domain = new Domain();
    domain.setId( "id" );
    mock.setPersistedDomains( domain );

    ICacheManager manager = mock( ICacheManager.class );
    repo.cacheManager = manager;
    repo.domainIdsCacheEnabled = false;

    Set<String> domainIds = repo.getDomainIds();

    assertEquals( 1, domainIds.size() );
    assertTrue( domainIds.contains( "id" ) );
    verify( mock, times( 1 ) ).getDomainIds();
    verify( mock, times( 1 ) ).reloadDomains();
    verify( manager, times( 0 ) ).getFromRegionCache( "metadata-domain-repository", "domain-id-cache-for-session:1" );
    verify( manager, times( 0 ) ).addCacheRegion( "domain-id-cache-for-session:1" );
  }

  @Test
  public void shouldUseDomainIdsCacheIfEnabled() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    PentahoSessionHolder.setSession( new StandaloneSession( "session", "1" ) );
    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    Domain domain = new Domain();
    domain.setId( "id" );
    mock.setPersistedDomains( domain );

    ICacheManager manager = mock( ICacheManager.class );
    Set<String> ids = new HashSet<>( Arrays.asList( "domainId1", "domainId2" ) );
    when( manager.getFromRegionCache( "metadata-domain-repository", "domain-id-cache-for-session:1" ) ).thenReturn( ids );

    repo.cacheManager = manager;
    repo.domainIdsCacheEnabled = true;

    Set<String> domainIds = repo.getDomainIds();
    assertEquals( ids, domainIds );
    verify( mock, times( 0 ) ).reloadDomains();
    verify( manager, times( 1 ) ).getFromRegionCache( "metadata-domain-repository", "domain-id-cache-for-session:1" );
  }
}
