/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.platform.repository2.unified.jcr.sejcr;

import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import org.apache.jackrabbit.core.SessionImpl;
import org.junit.Test;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jcr.GuestCredentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Failure injection at the real Guava/factory boundary; repository failures are deliberately mocked. */
public class GuavaCachePoolPentahoJcrSessionFactoryTest {

  @Test
  public void guestCredentialsUseManagedFallbackEvenWithDebugLogging() throws Exception {
    Repository repository = mock( Repository.class );
    GuestCredentials credentials = new GuestCredentials();
    Session fallback = trackedSession();
    when( repository.login( credentials, "workspace" ) ).thenReturn( fallback );
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    org.slf4j.Logger logger = mock( org.slf4j.Logger.class );
    when( logger.isDebugEnabled() ).thenReturn( true );
    ReflectionTestUtils.setField( factory, "logger", logger );
    SessionFactory templateFactory = mock( SessionFactory.class );
    when( templateFactory.getSession() ).thenAnswer( invocation -> factory.getSession( credentials ) );
    PentahoJcrTemplate template = new PentahoJcrTemplate();
    template.setSessionFactory( templateFactory );
    template.setAllowCreate( true );

    try {
      template.execute( session -> {
        assertSame( fallback, session );
        assertTrue( session.isLive() );
        return null;
      }, true );
      assertFalse( "A non-cacheable credentials type must still release its direct session", fallback.isLive() );
    } finally {
      if ( fallback.isLive() ) {
        fallback.logout();
      }
    }
  }

  @Test
  public void configuredCacheSizeIsAppliedBeforeBuildingCache() throws Exception {
    Repository repository = mock( Repository.class );
    List<Session> sessions = new ArrayList<>();
    when( repository.login( any( SimpleCredentials.class ), eq( "workspace" ) ) ).thenAnswer( invocation -> {
      Session session = trackedSession();
      sessions.add( session );
      return session;
    } );
    GuavaCachePoolPentahoJcrSessionFactory factory = configuredFactory( repository, "2", "300" );
    try {
      for ( int user = 0; user < 3; user++ ) {
        SimpleCredentials credentials = new SimpleCredentials( "user-" + user, new char[ 0 ] );
        SessionFactory templateFactory = mock( SessionFactory.class );
        when( templateFactory.getSession() ).thenAnswer( invocation -> factory.getSession( credentials ) );
        PentahoJcrTemplate template = new PentahoJcrTemplate();
        template.setSessionFactory( templateFactory );
        template.setAllowCreate( true );
        template.execute( session -> null, true );
      }
      assertEquals( "The configured capacity must evict an idle entry on the third distinct user",
        1, sessions.stream().filter( session -> !session.isLive() ).count() );
    } finally {
      factory.invalidateCachedSessions();
    }
  }

  @Test
  public void zeroCacheSizeUsesBoundedManagedFallback() throws Exception {
    verifyDisabledCache( "0", "300" );
  }

  @Test
  public void zeroCacheTtlUsesBoundedManagedFallback() throws Exception {
    verifyDisabledCache( "100", "0" );
  }

  private void verifyDisabledCache( String size, String ttl ) throws Exception {
    Repository repository = mock( Repository.class );
    List<Session> sessions = new ArrayList<>();
    when( repository.login( any( SimpleCredentials.class ), eq( "workspace" ) ) ).thenAnswer( invocation -> {
      Session session = trackedSession();
      sessions.add( session );
      return session;
    } );
    GuavaCachePoolPentahoJcrSessionFactory factory = configuredFactory( repository, size, ttl );
    SessionFactory templateFactory = mock( SessionFactory.class );
    when( templateFactory.getSession() )
      .thenAnswer( invocation -> factory.getSession( new SimpleCredentials( "user", new char[ 0 ] ) ) );
    PentahoJcrTemplate template = new PentahoJcrTemplate();
    template.setSessionFactory( templateFactory );
    template.setAllowCreate( true );
    try {
      template.execute( session -> {
        assertTrue( session.isLive() );
        return null;
      }, true );
      assertEquals( "A disabled cache should log in directly, not create and immediately evict retry sessions", 1, sessions.size() );
      assertTrue( "A disabled cache must not retain any session after the callback",
        sessions.stream().noneMatch( Session::isLive ) );
    } finally {
      factory.invalidateCachedSessions();
      sessions.forEach( Session::logout );
    }
  }

  private GuavaCachePoolPentahoJcrSessionFactory configuredFactory( Repository repository, String size, String ttl )
      throws Exception {
    ISystemConfig config = mock( ISystemConfig.class );
    IConfiguration repositoryConfig = mock( IConfiguration.class );
    Properties properties = new Properties();
    properties.setProperty( "cache-size", size );
    properties.setProperty( "cache-ttl", ttl );
    when( config.getConfiguration( "repository" ) ).thenReturn( repositoryConfig );
    when( repositoryConfig.getProperties() ).thenReturn( properties );
    StandaloneObjectFactory objects = new StandaloneObjectFactory( "owned-session-cache-configuration" );
    objects.defineInstance( ISystemConfig.class.getSimpleName(), config );
    PentahoSystem.registerObjectFactory( objects );
    try {
      assertSame( "The factory must read the test-owned configuration", config, PentahoSystem.get( ISystemConfig.class ) );
      return new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    } finally {
      PentahoSystem.deregisterObjectFactory( objects );
    }
  }

  @Test
  public void repeatedEvictionBeforeAcquisitionHasBoundedLoginAttempts() throws Exception {
    Repository repository = mock( Repository.class );
    List<Session> createdSessions = new ArrayList<>();
    when( repository.login( any( SimpleCredentials.class ), eq( "workspace" ) ) ).thenAnswer( invocation -> {
      if ( createdSessions.size() == 10 ) {
        throw new RepositoryException( "Abort an otherwise unbounded cache retry" );
      }
      Session session = trackedSession();
      createdSessions.add( session );
      return session;
    } );
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    evictBeforeEveryAcquisition( factory );

    try {
      Session session = factory.getSession( new SimpleCredentials( "user", new char[ 0 ] ) );
      assertTrue( "Persistent eviction must fall back instead of repeatedly logging in", createdSessions.size() <= 4 );
      assertTrue( "The fallback must be usable", session.isLive() );
    } finally {
      createdSessions.forEach( Session::logout );
    }
  }

  @Test
  public void uncachedFallbackIsClosedWhenCallbackFails() throws Exception {
    Repository repository = mock( Repository.class );
    Session failed = trackedSession();
    Session fallback = trackedSession();
    when( repository.login( any( SimpleCredentials.class ), eq( "workspace" ) ) ).thenReturn( failed, fallback );
    doThrow( new RepositoryException( "refresh failed" ) ).when( failed ).refresh( false );
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    SessionFactory templateFactory = mock( SessionFactory.class );
    when( templateFactory.getSession() )
      .thenAnswer( invocation -> factory.getSession( new SimpleCredentials( "user", new char[ 0 ] ) ) );
    PentahoJcrTemplate template = new PentahoJcrTemplate();
    template.setSessionFactory( templateFactory );
    template.setAllowCreate( true );
    IOException callbackFailure = new IOException( "callback failed" );

    try {
      try {
        template.execute( session -> {
          assertSame( fallback, session );
          throw callbackFailure;
        }, true );
        org.junit.Assert.fail( "The callback failure must propagate" );
      } catch ( org.springframework.dao.DataAccessException expected ) {
        assertSame( "Cleanup must not mask the callback's failure", callbackFailure, expected.getCause() );
      }
      assertFalse( "An uncached fallback must be closed even when its callback fails", fallback.isLive() );
    } finally {
      factory.invalidateCachedSessions();
      failed.logout();
      fallback.logout();
    }
  }

  @Test
  public void failedListenerHandoffReleasesFactoryLease() throws Exception {
    Repository repository = mock( Repository.class );
    Session cachedSession = trackedSession();
    SimpleCredentials credentials = new SimpleCredentials( "user", new char[ 0 ] );
    when( repository.login( credentials, "workspace" ) ).thenReturn( cachedSession );
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    RepositoryException listenerFailure = new RepositoryException( "listener registration failed" );
    CredentialsStrategySessionFactory credentialsFactory =
      new CredentialsStrategySessionFactory( repository, new ConstantCredentialsStrategy( credentials ) ) {
        @Override
        protected Session addListeners( Session session ) throws RepositoryException {
          throw listenerFailure;
        }
      };
    credentialsFactory.setSessionFactory( factory );

    try {
      try {
        credentialsFactory.getSession();
        org.junit.Assert.fail( "The listener failure must propagate" );
      } catch ( RepositoryException expected ) {
        assertSame( listenerFailure, expected );
      }
      factory.invalidateCachedSessions();
      assertFalse( "A factory lease must be rolled back when the proxy never reaches a template", cachedSession.isLive() );
    } finally {
      cachedSession.logout();
    }
  }

  @Test
  public void failedRefreshDisposesCachedSessionAndFallbackAfterCallback() throws Exception {
    Repository repository = mock( Repository.class );
    Session cachedSession = trackedSession();
    Session fallback = trackedSession();
    when( repository.login( any( SimpleCredentials.class ), eq( "workspace" ) ) )
      .thenReturn( cachedSession, fallback );
    org.mockito.Mockito.doThrow( new RepositoryException( "refresh failed" ) ).when( cachedSession ).refresh( false );
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    SessionFactory templateFactory = mock( SessionFactory.class );
    when( templateFactory.getSession() )
      .thenAnswer( invocation -> factory.getSession( new SimpleCredentials( "user", new char[ 0 ] ) ) );
    PentahoJcrTemplate template = new PentahoJcrTemplate();
    template.setSessionFactory( templateFactory );
    template.setAllowCreate( true );

    try {
      template.execute( session -> {
        assertSame( fallback, session );
        assertTrue( "The fallback must stay live during the callback", session.isLive() );
        return null;
      }, true );
      assertFalse( "A failed cached session must be discarded rather than failing on every cache hit",
        cachedSession.isLive() );
      assertFalse( "An uncached fallback must not be orphaned after its callback", fallback.isLive() );
    } finally {
      factory.invalidateCachedSessions();
      cachedSession.logout();
      fallback.logout();
    }
  }

  private Session trackedSession() throws Exception {
    SessionImpl session = mock( SessionImpl.class );
    AtomicBoolean live = new AtomicBoolean( true );
    Map<String, Object> attributes = new ConcurrentHashMap<>();
    when( session.isLive() ).thenAnswer( invocation -> live.get() );
    when( session.getAttribute( any( String.class ) ) ).thenAnswer( invocation -> attributes.get( invocation.getArgument( 0 ) ) );
    doAnswer( invocation -> {
      attributes.put( invocation.getArgument( 0 ), invocation.getArgument( 1 ) );
      return null;
    } ).when( session ).setAttribute( any( String.class ), any() );
    doAnswer( invocation -> {
      live.set( false );
      return null;
    } ).when( session ).logout();
    doAnswer( invocation -> {
      if ( !live.get() ) {
        throw new RepositoryException( "Session is closed" );
      }
      return null;
    } ).when( session ).refresh( false );
    return session;
  }

  @SuppressWarnings( "unchecked" )
  private void evictBeforeEveryAcquisition( GuavaCachePoolPentahoJcrSessionFactory factory ) throws Exception {
    // Deterministically model another thread evicting the entry after Guava returns it but before acquire().
    // Decorating the real cache keeps loader and removal-listener behavior intact, unlike stubbing cache.get().
    LoadingCache<Object, Object> cache =
      (LoadingCache<Object, Object>) ReflectionTestUtils.getField( factory, "sessionCache" );
    ReflectionTestUtils.setField( factory, "sessionCache",
      new ForwardingLoadingCache.SimpleForwardingLoadingCache<Object, Object>( cache ) {
        @Override
        public Object get( Object key ) throws ExecutionException {
          Object session = super.get( key );
          invalidate( key );
          cleanUp();
          return session;
        }
      } );
  }
}