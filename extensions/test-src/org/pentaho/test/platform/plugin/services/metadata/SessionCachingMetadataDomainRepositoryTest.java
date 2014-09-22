/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.metadata;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;
import org.pentaho.platform.plugin.services.metadata.SessionCachingMetadataDomainRepository;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.SimpleObjectFactory;

import java.io.File;
import java.util.Set;

import static org.pentaho.test.platform.plugin.services.metadata.MockSessionAwareMetadataDomainRepository.TEST_LOCALE;

public class SessionCachingMetadataDomainRepositoryTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/metadata-solution"; //$NON-NLS-1$
  private static final String ALT_SOLUTION_PATH = "test-src/metadata-solution"; //$NON-NLS-1$
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml"; //$NON-NLS-1$

  private static final String CACHE_NAME = "metadata-domain-repository"; //$NON-NLS-1$

  @Override
  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  private Domain getTestDomain( String id ) {
    Domain d = new Domain();
    d.setId( id );
    return d;
  }

  public void tearDown() {
    // Clean the cache
    ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
    cacheManager.clearRegionCache( CACHE_NAME );

    super.tearDown();
  }

  public void testCreate_no_delegate() throws Exception {
    try {
      new SessionCachingMetadataDomainRepository( null );
      fail( "Should not be able to create a Session Caching Repository without a base implementation (delegate)" ); //$NON-NLS-1$
    } catch ( NullPointerException ex ) {
      // expected
    }
  }

  public void testCannotCreateCache() throws Exception {
    SimpleObjectFactory factory = new SimpleObjectFactory();
    factory.defineObject( "ICacheManager", MockDisabledCacheManager.class.getName() ); //$NON-NLS-1$
    // Swap in an object factory with a cache manager that doesn't allow creating new caches

    Set<IPentahoObjectFactory> facts = ( (AggregateObjectFactory) PentahoSystem.getObjectFactory() ).getFactories();
    PentahoSystem.clearObjectFactory();
    PentahoSystem.registerObjectFactory( factory );
    try {
      try {
        new SessionCachingMetadataDomainRepository( new MockSessionAwareMetadataDomainRepository() );
        fail( "Should not be able to create a Session Caching Repository without an enabled cache" ); //$NON-NLS-1$
      } catch ( IllegalStateException ex ) {
        assertTrue( ex.getMessage().contains( "cannot be initialized" ) ); //$NON-NLS-1$
        // expected
      }
    } finally {
      // Replace the original object factory so the rest of the tests work
      PentahoSystem.clearObjectFactory();
      for ( IPentahoObjectFactory fact : facts ) {
        PentahoSystem.registerObjectFactory( fact );
      }
    }
  }

  public void testGetDomain() throws Exception {
    final String SESSION_ID = "1234-5678-90"; //$NON-NLS-1$
    final String ID = "1"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();
    mock.storeDomain( getTestDomain( ID ), false );

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", SESSION_ID ) ); //$NON-NLS-1$

    assertEquals( 0, mock.getInvocationCount( "getDomain" ) ); //$NON-NLS-1$
    Domain d = repo.getDomain( ID );
    assertEquals( ID, d.getId() );
    // Make sure the domain we got back has our session embedded in it (tests mock repository)
    assertEquals( SESSION_ID, d.getDescription( TEST_LOCALE ) );
    assertEquals( 1, mock.getInvocationCount( "getDomain" ) ); //$NON-NLS-1$
    // Cache should contain a domain for this session
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    d = repo.getDomain( ID );
    // Make sure cache was hit and delegate was not called
    assertEquals( 1, mock.getInvocationCount( "getDomain" ) ); //$NON-NLS-1$

    // Cache should contain a domain for this session
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );
  }

  public void testGetDomain_null_session() throws Exception {
    final String SESSION_ID = null;
    final String ID = "1"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();
    mock.storeDomain( getTestDomain( ID ), false );

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", SESSION_ID ) ); //$NON-NLS-1$

    Domain domain = repo.getDomain( ID );
    // Description will equal the id when no description is provided (null session)
    assertEquals( ID, domain.getDescription( TEST_LOCALE ) );
  }

  public void testGetDomain_differentSessions() throws Exception {
    final String SESSION_ID1 = "1234"; //$NON-NLS-1$
    final String SESSION_ID2 = "5678"; //$NON-NLS-1$
    final String ID = "1"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();
    mock.storeDomain( getTestDomain( ID ), false );

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", SESSION_ID1 ) ); //$NON-NLS-1$

    assertEquals( 0, mock.getInvocationCount( "getDomain" ) ); //$NON-NLS-1$
    Domain d = repo.getDomain( ID );
    assertEquals( ID, d.getId() );
    assertEquals( SESSION_ID1, d.getDescription( TEST_LOCALE ) );
    assertEquals( 1, mock.getInvocationCount( "getDomain" ) ); //$NON-NLS-1$
    // Cache should contain a domain for this session
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    // Get the same domain from a different session
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", SESSION_ID2 ) ); //$NON-NLS-1$

    d = repo.getDomain( ID );
    // Make sure we got a new, session-specific, domain
    assertEquals( SESSION_ID2, d.getDescription( TEST_LOCALE ) );
    // Make sure cache was missed and delegate was called
    assertEquals( 2, mock.getInvocationCount( "getDomain" ) ); //$NON-NLS-1$
    // We should now have two objects in the cache (1 domain per session)
    assertEquals( 2, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    // Domains in current session
    assertEquals( 1, repo.getDomainIds().size() );

    // Switch back to original session
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    // Check for domains available for this session
    assertEquals( 1, repo.getDomainIds().size() );
  }

  /**
   * Getting the domain ids should always hit the delegate and not cache any objects
   */
  public void testGetDomainIds() throws Exception {
    final String ID = "1"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "1234-5678-90" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    Set<String> ids = repo.getDomainIds();
    assertEquals( 0, ids.size() );
    assertEquals( 1, mock.getInvocationCount( "getDomainIds" ) ); //$NON-NLS-1$

    repo.storeDomain( getTestDomain( ID ), false );
    assertEquals( 0, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    ids = repo.getDomainIds();
    assertEquals( 1, ids.size() );
    assertEquals( 2, mock.getInvocationCount( "getDomainIds" ) ); //$NON-NLS-1$
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    ids = repo.getDomainIds();
    assertEquals( 2, mock.getInvocationCount( "getDomainIds" ) ); //$NON-NLS-1$
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );
  }

  /**
   * Make sure all domain ids are returned in all sessions
   */
  public void testGetDomainIds_differentSessions() throws Exception {
    final String ID = "1"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    Set<String> ids = repo.getDomainIds();
    assertEquals( 0, ids.size() );
    assertEquals( 1, mock.getInvocationCount( "getDomainIds" ) ); //$NON-NLS-1$

    repo.storeDomain( getTestDomain( ID ), false );

    ids = repo.getDomainIds();
    assertEquals( 1, ids.size() );
    assertEquals( 2, mock.getInvocationCount( "getDomainIds" ) ); //$NON-NLS-1$

    // Switch the session
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "2" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    // Make sure the domain id is returned
    ids = repo.getDomainIds();
    assertEquals( 1, ids.size() );
    assertEquals( 3, mock.getInvocationCount( "getDomainIds" ) ); //$NON-NLS-1$
  }

  public void testReloadDomains() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();
    mock.setPersistedDomains( getTestDomain( "1" ) ); //$NON-NLS-1$

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "1234-5678-90" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    // Cache should be empty
    assertEquals( 0, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    assertEquals( 0, repo.getDomainIds().size() );

    repo.reloadDomains();
    assertEquals( 1, mock.getInvocationCount( "reloadDomains" ) ); //$NON-NLS-1$
    assertEquals( 1, repo.getDomainIds().size() );

    // Cache should only have the domain ids
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );
  }

  public void testFlushDomains() throws Exception {
    final String ID = "1"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    repo.storeDomain( getTestDomain( ID ), false );

    repo.getDomain( ID );
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    repo.getDomain( ID );
    assertEquals( 2, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    assertEquals( 1, repo.getDomainIds().size() );

    repo.flushDomains();
    assertEquals( 1, mock.getInvocationCount( "flushDomains" ) ); //$NON-NLS-1$
    assertEquals( 0, repo.getDomainIds().size() );
    assertEquals( 0, mock.getDomainIds().size() );
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );
  }

  /**
   * Make sure removing a domain removes all cached instances of the domain
   */
  public void testRemoveDomain() throws Exception {
    final String ID1 = "1"; //$NON-NLS-1$
    final String ID2 = "2"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();
    mock.storeDomain( getTestDomain( ID1 ), false );
    mock.storeDomain( getTestDomain( ID2 ), false );

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    Domain domainFromSession1 = repo.getDomain( ID1 );
    assertNotNull( domainFromSession1 );
    assertEquals( 1, mock.getInvocationCount( "getDomain" ) ); //$NON-NLS-1$

    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "2" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    Domain domainFromSession2 = repo.getDomain( ID1 );
    assertNotNull( domainFromSession2 );
    assertEquals( 2, mock.getInvocationCount( "getDomain" ) ); //$NON-NLS-1$

    assertEquals( 2, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    repo.removeDomain( ID1 );
    assertEquals( 0, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    // Calling getDomain() now should increment the call count to the delegate
    repo.getDomain( ID2 );
    assertEquals( 3, mock.getInvocationCount( "getDomain" ) ); //$NON-NLS-1$

    // There should now only be one in the cache
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );
  }

  public void testStoreDomain() throws Exception {
    final String ID = "1"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    repo.storeDomain( getTestDomain( ID ), false );

    // No cache values when storing a domain
    assertEquals( 0, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    // Cache one domain
    repo.getDomain( ID );

    // Storing a domain under a different session should wipe out all cached domains with the same id
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      repo.storeDomain( getTestDomain( ID ), false );
      fail( "Should have thrown a " + DomainAlreadyExistsException.class.getSimpleName() ); //$NON-NLS-1$
    } catch ( DomainAlreadyExistsException ex ) {
      // expected
    }
    repo.storeDomain( getTestDomain( ID ), true );
    // Storing a domain under a different session should wipe out all cached domains with the same id
    assertEquals( 0, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    assertEquals( 1, repo.getDomainIds().size() );
    repo.getDomain( ID );
    assertEquals( 2, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    // Storing a domain should only wipe out the cached domains with the same id
    repo.storeDomain( getTestDomain( "2" ), false ); //$NON-NLS-1$
    assertEquals( 2, repo.getDomainIds().size() );
    assertEquals( 2, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );
  }

  public void testRemoveModel() throws Exception {
    final String ID1 = "1"; //$NON-NLS-1$
    final String ID2 = "2"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    Domain domain = getTestDomain( ID1 );
    LogicalModel model = new LogicalModel();
    model.setId( "test" ); //$NON-NLS-1$
    domain.addLogicalModel( model );

    repo.storeDomain( domain, false );
    repo.storeDomain( getTestDomain( ID2 ), false );

    repo.getDomain( ID1 );
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );
    repo.getDomain( ID2 );
    assertEquals( 2, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    repo.getDomain( ID1 );
    assertEquals( 3, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    repo.removeModel( ID1, "test" ); //$NON-NLS-1$
    // Removing a model should remove all domains with that same id, leaving others intact
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );
    assertEquals( 1, mock.getInvocationCount( "removeModel" ) ); //$NON-NLS-1$
  }

  public void testGenerateRowLevelSecurityConstraint() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();
    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );

    // Only thing to do here is make sure the return value is the same; cache shouldn't do anything here
    assertEquals( mock.generateRowLevelSecurityConstraint( null ), repo.generateRowLevelSecurityConstraint( null ) );
    assertEquals( 2, mock.getInvocationCount( "generateRowLevelSecurityConstraint" ) ); //$NON-NLS-1$
  }

  public void testHasAccess() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();
    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );

    // Only thing to do here is make sure the return value is the same; cache shouldn't do anything here
    assertEquals( mock.hasAccess( 0, null ), repo.hasAccess( 0, null ) );
    assertEquals( 2, mock.getInvocationCount( "hasAccess" ) ); //$NON-NLS-1$
  }

  public void testOnLogout() throws Exception {
    final String ID1 = "1"; //$NON-NLS-1$
    final String ID2 = "2"; //$NON-NLS-1$
    MockSessionAwareMetadataDomainRepository mock = new MockSessionAwareMetadataDomainRepository();

    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    repo.storeDomain( getTestDomain( ID1 ), false );
    repo.storeDomain( getTestDomain( ID2 ), false );

    PentahoSessionHolder.setSession( new StandaloneSession( "Standalone Session", "1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    repo.getDomain( ID1 );

    IPentahoSession session2 = new StandaloneSession( "Standalone Session", "2" ); //$NON-NLS-1$ //$NON-NLS-2$
    PentahoSessionHolder.setSession( session2 );
    repo.getDomain( ID2 );

    assertEquals( 2, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );

    // Logging out session 2 should only remove cached domains from session 2
    repo.onLogout( session2 );
    assertEquals( 1, PentahoSystem.getCacheManager( null ).getAllKeysFromRegionCache( CACHE_NAME ).size() );
  }
}
