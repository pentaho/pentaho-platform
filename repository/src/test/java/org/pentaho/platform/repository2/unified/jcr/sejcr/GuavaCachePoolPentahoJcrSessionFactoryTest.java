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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for cache configuration, lease handoff, and fallback behavior at the real Guava/factory boundary.
 * Repository sessions are controlled mocks, allowing failures and eviction races to be deterministic without
 * requiring repository storage; real-repository lifecycle behavior is covered by the integration tests.
 */
public class GuavaCachePoolPentahoJcrSessionFactoryTest {

  /** Verifies guest credentials take the managed direct-login path and are released after template use. */
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

  /** Verifies configured capacity is applied to the cache constructed by the factory. */
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

  /** Verifies a zero cache size selects one managed direct login rather than repeated cache retries. */
  @Test
  public void zeroCacheSizeUsesBoundedManagedFallback() throws Exception {
    verifyDisabledCache( "0", "300" );
  }

  /** Verifies a zero cache TTL selects the managed direct-login path rather than immediate-expiry churn. */
  @Test
  public void zeroCacheTtlUsesBoundedManagedFallback() throws Exception {
    verifyDisabledCache( "100", "0" );
  }

  /**
   * Exercises the disabled-cache path for the supplied size/TTL pair and checks callback-scoped cleanup.
   *
   * @param size configured maximum cache size
   * @param ttl configured entry lifetime in seconds
   * @throws Exception if the mocked repository or template execution fails
   */
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

  /**
   * Creates a factory with an isolated Pentaho system configuration for the requested cache settings.
   * The object factory is deregistered immediately after construction to avoid leaking global test state.
   *
   * @param repository mocked repository used by the factory
   * @param size configured cache size
   * @param ttl configured cache lifetime in seconds
   * @return a factory initialized with those settings
   * @throws Exception if configuration setup or factory construction fails
   */
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

  /** Verifies repeated eviction is bounded and acquisition eventually uses a direct-login fallback. */
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

  /** Verifies a fallback session is released when its callback fails and the callback exception remains primary. */
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

  /** Verifies listener-registration failure rolls back the factory lease before rethrowing the failure. */
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
   * Verifies an already transaction-bound session is not handed out again by the cache.
   * The test primes the cache, binds that session under the owning {@link SessionFactory} key, and reacquires the
   * same credentials; the factory must reject the bound entry and obtain a distinct session.
   */
  /** Verifies an already transaction-bound session is not handed out again by the cache. */
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

  /** Verifies refresh failure discards the cached entry and its managed fallback closes after callback completion. */
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
   * {@code createSessionCache()}'s Guava {@code RemovalListener} must run eviction-triggered
   * {@code retire()}/{@code session.logout()} asynchronously, off the acquiring thread. Otherwise a thread
   * acquiring its OWN, unrelated user's session would be forced to pay, inline, for ANOTHER idle user's slow
   * {@code logout()} I/O as a side effect of crossing {@code maximumSize} - head-of-line blocking / tail-latency
   * injection under eviction churn.
   */
  /**
   * Verifies natural maximum-size eviction dispatches slow logout away from an unrelated acquisition thread.
   * Latches establish that cleanup starts and finishes, while elapsed time and thread identity guard against
   * reintroducing synchronous head-of-line blocking.
   */
  @Test
  public void unrelatedAcquisitionIsNotBlockedByAnotherUsersSlowEviction() throws Exception {
    long slowLogoutMillis = 300;
    Repository repository = mock( Repository.class );
    AtomicBoolean idleLive = new AtomicBoolean( true );
    AtomicReference<Thread> logoutThread = new AtomicReference<>();
    CountDownLatch logoutStarted = new CountDownLatch( 1 );
    CountDownLatch logoutFinished = new CountDownLatch( 1 );
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
      logoutStarted.countDown();
      Thread.sleep( slowLogoutMillis );
      idleLive.set( false );
      logoutFinished.countDown();
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

      assertTrue( "The eviction listener must at least start the idle session's logout",
        logoutStarted.await( 2, TimeUnit.SECONDS ) );
      assertTrue( "An unrelated session acquisition must return well before another user's slow synchronous "
          + "logout completes (took " + elapsedMillis + "ms, expected well under " + slowLogoutMillis + "ms)",
        elapsedMillis < slowLogoutMillis );

      assertTrue( "The idle session's slow logout must eventually complete on a background thread",
        logoutFinished.await( 2, TimeUnit.SECONDS ) );
      assertFalse( "Loading an unrelated user's session must still evict and log out the idle entry",
        idleLive.get() );
      assertNotEquals( "The idle session's logout must run on a dedicated eviction thread, not the unrelated "
          + "caller's own thread",
        callingThread, logoutThread.get() );
    } finally {
      factory.invalidateCachedSessions();
      if ( !logoutFinished.await( 2, TimeUnit.SECONDS ) && idleLive.get() ) {
        idleSession.logout();
      }
      if ( busySession.isLive() ) {
        busySession.logout();
      }
    }
  }

  /**
   * Creates a live Jackrabbit session mock with observable attributes and lifecycle behavior.
   *
   * @return a session whose liveness, attributes, refresh, and logout are controlled by the fixture
   * @throws Exception if Mockito cannot configure the session fixture
   */
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

  /**
   * Decorates the real loading cache so each lookup is invalidated before the factory can acquire its holder.
   * This deterministically exercises the cache-get/lease-acquire race while preserving the actual loader and
   * removal-listener behavior.
   *
   * @param factory factory whose cache is decorated for the test
   * @throws Exception if the cache cannot be accessed or decorated
   */
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