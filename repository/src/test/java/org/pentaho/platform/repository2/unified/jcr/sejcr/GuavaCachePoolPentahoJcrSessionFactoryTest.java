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
import java.util.concurrent.atomic.AtomicReference;

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

  /**
   * {@code acquireCachedSession()}'s {@code isSessionThreadBound(session, credentialsStrategySessionFactory)}
   * guard exists to refuse handing out a cached session that is legitimately bound to an active transaction
   * elsewhere, forcing a fresh session instead. {@code SessionFactoryUtils.isSessionThreadBound(Session, null)}
   * always returns {@code false} without even inspecting the thread-local binding, so if
   * {@code credentialsStrategySessionFactory} is never wired to the real owning {@link SessionFactory}, the guard
   * can never fire regardless of whether the session really is thread-bound.
   */
  @Test
  public void threadBoundCachedSessionIsNotReusedFromTheCache() throws Exception {
    Repository repository = mock( Repository.class );
    Session boundSession = trackedSession();
    Session freshSession = trackedSession();
    SimpleCredentials credentials = new SimpleCredentials( "user", new char[ 0 ] );
    when( repository.login( credentials, "workspace" ) ).thenReturn( boundSession, freshSession );

    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    CredentialsStrategySessionFactory credentialsFactory =
      new CredentialsStrategySessionFactory( repository, new ConstantCredentialsStrategy( credentials ) );
    credentialsFactory.setSessionFactory( factory );

    Session primed = factory.getSession( credentials );
    assertSame( "Sanity check: the cache must hand out the session it just created", boundSession, primed );

    // Simulate the cached session legitimately being bound to an active Spring-managed transaction elsewhere,
    // under the exact key (credentialsFactory) the template/transaction infrastructure binds against.
    org.springframework.transaction.support.TransactionSynchronizationManager.bindResource(
      credentialsFactory, new org.springframework.extensions.jcr.SessionHolder( primed ) );
    try {
      Session reacquired = factory.getSession( credentials );
      assertFalse( "A session already bound to an active transaction must never be handed out again from the "
          + "cache while that binding is active",
        boundSession == reacquired );
    } finally {
      org.springframework.transaction.support.TransactionSynchronizationManager.unbindResource( credentialsFactory );
      factory.invalidateCachedSessions();
      boundSession.logout();
      freshSession.logout();
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

  /**
   * Characterizes a production resilience hazard rather than a functional bug: {@code createSessionCache()}
   * registers its {@code RemovalListener} on the plain {@link com.google.common.cache.CacheBuilder}, without
   * {@code RemovalListeners.asynchronous(...)}. Guava therefore runs eviction notifications synchronously, inline,
   * on whichever caller's thread happens to cross {@code maximumSize} while loading an unrelated key.
   *
   * <p>In production this means: an incoming request for user B's session can be forced to pay, inline, for
   * logging out an idle, completely unrelated user A's session, with no bound on how long that takes (real
   * {@code Session#logout()} is repository I/O). Under load/eviction churn this turns an O(1) cache hit for one
   * user into unpredictable tail latency for another. This test proves the hazard is real today; it does not
   * assert any desired/fixed behavior, since no production code was changed while writing it.</p>
   */
  @Test
  public void unrelatedAcquisitionSynchronouslyAbsorbsAnotherUsersSlowEviction() throws Exception {
    long slowLogoutMillis = 300;
    Repository repository = mock( Repository.class );
    AtomicBoolean idleLive = new AtomicBoolean( true );
    AtomicReference<Thread> logoutThread = new AtomicReference<>();
    Map<String, Object> idleAttributes = new ConcurrentHashMap<>();
    SessionImpl idleSession = mock( SessionImpl.class );
    when( idleSession.isLive() ).thenAnswer( invocation -> idleLive.get() );
    when( idleSession.getAttribute( any( String.class ) ) )
      .thenAnswer( invocation -> idleAttributes.get( invocation.getArgument( 0 ) ) );
    doAnswer( invocation -> {
      idleAttributes.put( invocation.getArgument( 0 ), invocation.getArgument( 1 ) );
      return null;
    } ).when( idleSession ).setAttribute( any( String.class ), any() );
    doAnswer( invocation -> null ).when( idleSession ).refresh( false );
    doAnswer( invocation -> {
      // Simulate slow repository-side logout I/O, and record which thread was forced to pay for it.
      logoutThread.set( Thread.currentThread() );
      Thread.sleep( slowLogoutMillis );
      idleLive.set( false );
      return null;
    } ).when( idleSession ).logout();

    Session busySession = trackedSession();
    SimpleCredentials idleUser = new SimpleCredentials( "idle-user", new char[ 0 ] );
    SimpleCredentials busyUser = new SimpleCredentials( "busy-user", new char[ 0 ] );
    when( repository.login( eq( idleUser ), eq( "workspace" ) ) ).thenReturn( idleSession );
    when( repository.login( eq( busyUser ), eq( "workspace" ) ) ).thenReturn( busySession );

    // maximumSize=1 forces Guava to evict the idle entry as soon as a second, unrelated user's entry is loaded.
    GuavaCachePoolPentahoJcrSessionFactory factory = configuredFactory( repository, "1", "300" );
    try {
      SessionFactory idleFactory = mock( SessionFactory.class );
      when( idleFactory.getSession() ).thenAnswer( invocation -> factory.getSession( idleUser ) );
      PentahoJcrTemplate idleTemplate = new PentahoJcrTemplate();
      idleTemplate.setSessionFactory( idleFactory );
      idleTemplate.setAllowCreate( true );
      idleTemplate.execute( session -> null, true );
      assertTrue( "The idle user's cached session must still be live before the unrelated acquisition",
        idleLive.get() );

      SessionFactory busyFactory = mock( SessionFactory.class );
      when( busyFactory.getSession() ).thenAnswer( invocation -> factory.getSession( busyUser ) );
      PentahoJcrTemplate busyTemplate = new PentahoJcrTemplate();
      busyTemplate.setSessionFactory( busyFactory );
      busyTemplate.setAllowCreate( true );

      Thread callingThread = Thread.currentThread();
      long before = System.nanoTime();
      busyTemplate.execute( session -> {
        assertSame( "An unrelated user must still receive its own session despite the eviction side effect",
          busySession, session );
        return null;
      }, true );
      long elapsedMillis = ( System.nanoTime() - before ) / 1_000_000;

      assertFalse( "Loading an unrelated user's session must evict and log out the idle entry", idleLive.get() );
      assertEquals( "The idle session's slow logout ran inline on the unrelated caller's own thread, not a "
          + "dedicated background/eviction thread",
        callingThread, logoutThread.get() );
      assertTrue( "An unrelated session acquisition absorbed another user's slow synchronous logout and should "
          + "take at least as long as that logout (took " + elapsedMillis + "ms, expected at least "
          + slowLogoutMillis + "ms) because Guava's removal listener runs synchronously on the calling thread "
          + "by default",
        elapsedMillis >= slowLogoutMillis );
    } finally {
      factory.invalidateCachedSessions();
      if ( idleLive.get() ) {
        idleSession.logout();
      }
      if ( busySession.isLive() ) {
        busySession.logout();
      }
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