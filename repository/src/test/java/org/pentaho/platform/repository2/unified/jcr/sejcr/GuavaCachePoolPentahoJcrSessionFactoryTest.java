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
import javax.jcr.LoginException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
      verify( logger, never() ).error( anyString(), any( Object.class ), any( Object.class ) );
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
    CountDownLatch firstSessionClosed = new CountDownLatch( 1 );
    List<Session> sessions = new ArrayList<>();
    when( repository.login( any( SimpleCredentials.class ), eq( "workspace" ) ) ).thenAnswer( invocation -> {
      Session session = trackedSession( sessions.isEmpty() ? firstSessionClosed : null );
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
      assertTrue( "The evicted least-recently-used session should eventually be logged out",
        firstSessionClosed.await( 5, TimeUnit.SECONDS ) );
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
    return factoryWithSystemConfig( repository, config );
  }

  /**
   * Creates a factory that reads the supplied system configuration, then removes that configuration from the global
   * Pentaho system so no state leaks into other tests.
   *
   * @param repository mocked repository used by the factory
   * @param config system configuration visible to the factory while it is constructed
   * @return the constructed factory
   */
  private GuavaCachePoolPentahoJcrSessionFactory factoryWithSystemConfig( Repository repository,
                                                                         ISystemConfig config ) {
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

  /** Verifies a missing repository configuration leaves the documented default cache size and lifetime in place. */
  @Test
  public void missingRepositoryConfigurationKeepsDefaultCacheSettings() {
    ISystemConfig config = mock( ISystemConfig.class );
    when( config.getConfiguration( "repository" ) ).thenReturn( null );

    GuavaCachePoolPentahoJcrSessionFactory factory = factoryWithSystemConfig( mock( Repository.class ), config );

    assertEquals( 100, ReflectionTestUtils.getField( factory, "cacheSize" ) );
    assertEquals( 300, ReflectionTestUtils.getField( factory, "cacheDuration" ) );
  }

  /** Verifies unreadable repository properties are tolerated and leave the default cache settings in place. */
  @Test
  public void unreadableRepositoryPropertiesKeepDefaultCacheSettings() throws Exception {
    ISystemConfig config = mock( ISystemConfig.class );
    IConfiguration repositoryConfig = mock( IConfiguration.class );
    when( config.getConfiguration( "repository" ) ).thenReturn( repositoryConfig );
    when( repositoryConfig.getProperties() ).thenThrow( new IOException( "repository properties are unreadable" ) );

    GuavaCachePoolPentahoJcrSessionFactory factory = factoryWithSystemConfig( mock( Repository.class ), config );

    assertEquals( 100, ReflectionTestUtils.getField( factory, "cacheSize" ) );
    assertEquals( 300, ReflectionTestUtils.getField( factory, "cacheDuration" ) );
  }

  /**
   * Verifies a session logged out outside the cache is discarded and replaced rather than handed to the next caller.
   */
  @Test
  public void externallyClosedCachedSessionIsReplacedOnNextAcquisition() throws Exception {
    Repository repository = mock( Repository.class );
    Session stale = trackedSession();
    Session replacement = trackedSession();
    SimpleCredentials credentials = new SimpleCredentials( "user", new char[ 0 ] );
    when( repository.login( credentials, "workspace" ) ).thenReturn( stale, replacement );
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    PentahoJcrTemplate template = templateFor( factory, credentials );

    try {
      template.execute( session -> {
        assertSame( stale, session );
        return null;
      }, true );
      stale.logout();

      template.execute( session -> {
        assertSame( "A closed session must be replaced by a new login", replacement, session );
        assertTrue( session.isLive() );
        return null;
      }, true );
    } finally {
      factory.invalidateCachedSessions();
      replacement.logout();
    }
  }

  /**
   * Verifies that while an independent transaction is being created the factory logs in directly instead of using
   * the cache, so a {@code REQUIRES_NEW} transaction cannot be handed the cached session that its suspended outer
   * transaction owns.
   */
  @Test
  public void sessionsAreLoggedInDirectlyWhileAnIndependentTransactionIsBeingCreated() throws Exception {
    Repository repository = mock( Repository.class );
    Session cached = trackedSession();
    Session transactional = trackedSession();
    SimpleCredentials credentials = new SimpleCredentials( "user", new char[ 0 ] );
    when( repository.login( credentials, "workspace" ) ).thenReturn( cached, transactional );
    AtomicBoolean creatingTransaction = new AtomicBoolean( false );
    PentahoTransactionManager transactionManager = new PentahoTransactionManager() {
      @Override public boolean isCreatingIndependentTransaction() {
        return creatingTransaction.get();
      }
    };
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    factory.setTransactionManager( transactionManager );

    try {
      assertSame( "Outside transaction creation the session comes from the cache", cached,
        factory.getSession( credentials ) );

      creatingTransaction.set( true );
      Session duringCreation = factory.getSession( credentials );
      assertSame( "A transaction gets its own login, not the cached session", transactional, duringCreation );
      assertNull( "A transaction's session is owned by Spring, not by the cache lifecycle",
        duringCreation.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) );

      creatingTransaction.set( false );
      assertSame( "The cached session is untouched and remains available", cached,
        factory.getSession( credentials ) );
    } finally {
      factory.invalidateCachedSessions();
      cached.logout();
      transactional.logout();
    }
  }

  /** Verifies debug logging of a cached hand-off reports the user name of {@link SimpleCredentials}. */
  @Test
  public void debugLoggingOfCachedHandoffIdentifiesTheUser() throws Exception {
    Repository repository = mock( Repository.class );
    Session cached = trackedSession();
    SimpleCredentials credentials = new SimpleCredentials( "debug-user", new char[ 0 ] );
    when( repository.login( credentials, "workspace" ) ).thenReturn( cached );
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    org.slf4j.Logger logger = mock( org.slf4j.Logger.class );
    when( logger.isDebugEnabled() ).thenReturn( true );
    ReflectionTestUtils.setField( factory, "logger", logger );

    try {
      templateFor( factory, credentials ).execute( session -> null, true );

      verify( logger ).debug( eq( "[JCR-FACTORY-RETRIEVE] Thread={} SessionId={} RefCount={} User={}" ),
        any(), any(), any(), eq( "debug-user" ) );
    } finally {
      factory.invalidateCachedSessions();
      cached.logout();
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

  /**
   * Verifies a repository login that is denied both by the cache loader and by the direct retry is logged as a login
   * failure with the user, workspace and thread, not as a cache error, and that the denial reaches the caller.
   * Neither the password nor the pre-authentication token may appear in the log.
   */
  @Test
  public void persistentLoginDenialIsLoggedWithContextForBothAttempts() throws Exception {
    Repository repository = mock( Repository.class );
    SimpleCredentials credentials = new SimpleCredentials( "denied-user", "secret-password".toCharArray() );
    credentials.setAttribute( "pre_authentication_token", "secret-token" );
    LoginException denial = new LoginException( "Workspace access denied" );
    when( repository.login( credentials, "workspace" ) ).thenThrow( denial );
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    org.slf4j.Logger logger = mock( org.slf4j.Logger.class );
    ReflectionTestUtils.setField( factory, "logger", logger );

    try {
      try {
        factory.getSession( credentials );
        org.junit.Assert.fail( "A persistent login denial must reach the caller" );
      } catch ( LoginException expected ) {
        assertSame( denial, expected );
      }
      verify( repository, times( 2 ) ).login( credentials, "workspace" );
      List<String> errors = loggedFormats( logger, "error" );
      assertTrue( "The cache-loader denial must be logged as a login failure", errors.stream()
        .anyMatch( message -> message.startsWith( "Repository login failed while loading a cached JCR session" ) ) );
      assertTrue( "The failed retry must be logged", errors.stream()
        .anyMatch( message -> message.startsWith( "Direct login retry also failed" ) ) );
      assertFalse( "A login denial must not be reported as a generic cache error", errors.stream()
        .anyMatch( message -> message.startsWith( "Error obtaining session from cache" ) ) );
      List<String> arguments = loggedArguments( logger );
      assertTrue( "The user must be logged", arguments.contains( "denied-user" ) );
      assertTrue( "The workspace must be logged", arguments.contains( "workspace" ) );
      assertTrue( "The thread must be logged", arguments.contains( Thread.currentThread().getName() ) );
      assertTrue( "Secrets must never be logged", arguments.stream()
        .noneMatch( argument -> argument.contains( "secret-password" ) || argument.contains( "secret-token" ) ) );
    } finally {
      factory.invalidateCachedSessions();
    }
  }

  /**
   * Verifies a cache-loader login failure whose direct retry succeeds is reported as transient, so the logs show
   * whether the retry helps.
   */
  @Test
  public void transientLoginDenialIsLoggedAsRecoveredByTheDirectRetry() throws Exception {
    Repository repository = mock( Repository.class );
    SimpleCredentials credentials = new SimpleCredentials( "flaky-user", new char[ 0 ] );
    Session retried = trackedSession();
    when( repository.login( credentials, "workspace" ) )
      .thenThrow( new LoginException( "Workspace access denied" ) )
      .thenReturn( retried );
    GuavaCachePoolPentahoJcrSessionFactory factory =
      new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
    org.slf4j.Logger logger = mock( org.slf4j.Logger.class );
    ReflectionTestUtils.setField( factory, "logger", logger );

    try {
      assertSame( "The direct retry's session is returned", retried, factory.getSession( credentials ) );
      assertTrue( "A recovered login failure must be reported as transient", loggedFormats( logger, "warn" ).stream()
        .anyMatch( message -> message.startsWith( "Direct login retry succeeded" ) ) );
    } finally {
      factory.invalidateCachedSessions();
      retried.logout();
    }
  }

  /** Returns the message formats passed to the given logger method, in call order. */
  private static List<String> loggedFormats( org.slf4j.Logger logger, String level ) {
    List<String> formats = new ArrayList<>();
    mockingDetails( logger ).getInvocations().stream()
      .filter( invocation -> invocation.getMethod().getName().equals( level ) )
      .forEach( invocation -> formats.add( String.valueOf( invocation.getArguments()[ 0 ] ) ) );
    return formats;
  }

  /** Returns every argument passed to any logger method, rendered as strings. */
  private static List<String> loggedArguments( org.slf4j.Logger logger ) {
    List<String> arguments = new ArrayList<>();
    mockingDetails( logger ).getInvocations().forEach( invocation -> {
      for ( Object argument : invocation.getArguments() ) {
        arguments.add( String.valueOf( argument ) );
      }
    } );
    return arguments;
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
    CountDownLatch cachedSessionClosed = new CountDownLatch( 1 );
    Session cachedSession = trackedSession( cachedSessionClosed );
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
      assertTrue( "The invalidated session should close after the failed handoff releases its lease",
        cachedSessionClosed.await( 5, TimeUnit.SECONDS ) );
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

    // Simulate the cached session legitimately being bound to an active Spring-managed transaction elsewhere.
    // Spring binds the logout-suppressing proxy handed out by the credentials factory, not the cached session itself,
    // under the exact key (credentialsFactory) the template/transaction infrastructure binds against.
    org.springframework.transaction.support.TransactionSynchronizationManager.bindResource(
      credentialsFactory, new org.springframework.extensions.jcr.SessionHolder( credentialsFactory.createSessionProxy( primed ) ) );
    try {
      Session reacquired = factory.getSession( credentials );
      assertNotSame( "A session already bound to an active transaction must never be handed out again from the "
          + "cache while that binding is active",
        boundSession, reacquired );
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
    CountDownLatch cachedSessionClosed = new CountDownLatch( 1 );
    Session cachedSession = trackedSession( cachedSessionClosed );
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
      assertTrue( "A failed cached session should be logged out asynchronously after its lease is released",
        cachedSessionClosed.await( 5, TimeUnit.SECONDS ) );
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
   * Verifies natural maximum-size eviction dispatches a slow logout away from an unrelated acquisition thread.
   * {@code createSessionCache()}'s removal listener must hand the idle session's {@code session.logout()} to the
   * eviction executor; otherwise a thread acquiring its own session would pay, inline, for another idle user's slow
   * logout I/O as a side effect of crossing {@code maximumSize}. The idle session's logout is held behind a latch
   * while an unrelated acquisition runs on another thread, so the assertion does not depend on scheduler timing:
   * a synchronous listener would leave that acquisition blocked and the future below would time out.
   */
  @Test
  public void unrelatedAcquisitionIsNotBlockedByAnotherUsersSlowEviction() throws Exception {
    Repository repository = mock( Repository.class );
    CountDownLatch releaseLogout = new CountDownLatch( 1 );
    BlockingLogoutSession idle = new BlockingLogoutSession( releaseLogout, new AtomicInteger(), new AtomicInteger() );
    Session busySession = trackedSession();
    SimpleCredentials idleUser = new SimpleCredentials( "idle-user", new char[ 0 ] );
    SimpleCredentials busyUser = new SimpleCredentials( "busy-user", new char[ 0 ] );
    when( repository.login( idleUser, "workspace" ) ).thenReturn( idle.session );
    when( repository.login( busyUser, "workspace" ) ).thenReturn( busySession );

    // maximumSize=1 forces Guava to evict the idle entry as soon as a second, unrelated user's entry is loaded.
    GuavaCachePoolPentahoJcrSessionFactory factory = configuredFactory( repository, "1", "300" );
    ExecutorService acquirer = Executors.newSingleThreadExecutor();
    try {
      templateFor( factory, idleUser ).execute( session -> null, true );
      assertTrue( "The idle user's cached session must still be live before the unrelated acquisition",
        idle.live.get() );

      PentahoJcrTemplate busyTemplate = templateFor( factory, busyUser );
      AtomicReference<Thread> acquisitionThread = new AtomicReference<>();
      Future<?> acquisition = acquirer.submit( () -> {
        acquisitionThread.set( Thread.currentThread() );
        busyTemplate.execute( session -> {
          assertSame( "An unrelated user must still receive its own session despite the eviction side effect",
            busySession, session );
          return null;
        }, true );
      } );

      assertTrue( "The eviction listener must at least start the idle session's logout",
        idle.logoutStarted.await( 5, TimeUnit.SECONDS ) );
      acquisition.get( 5, TimeUnit.SECONDS );
      assertEquals( "The unrelated acquisition must finish while the evicted session's logout is still blocked",
        1, idle.logoutFinished.getCount() );
      assertNotSame( "The idle session's logout must run on an eviction thread, not the unrelated caller's thread",
        acquisitionThread.get(), idle.logoutThread.get() );

      releaseLogout.countDown();
      assertTrue( "The idle session's slow logout must eventually complete on a background thread",
        idle.logoutFinished.await( 5, TimeUnit.SECONDS ) );
      assertFalse( "Loading an unrelated user's session must still evict and log out the idle entry",
        idle.live.get() );
    } finally {
      releaseLogout.countDown();
      acquirer.shutdownNow();
      factory.invalidateCachedSessions();
      if ( busySession.isLive() ) {
        busySession.logout();
      }
    }
  }

  /**
   * Verifies eviction cleanups never run on more worker threads than the configured bound, that excess cleanups wait
   * in the queue instead of being rejected or run on the evicting thread, and that every one still completes.
   * All logouts are held behind a latch, so the pool state can be asserted deterministically.
   */
  @Test
  public void concurrentEvictionCleanupsAreBoundedQueuedAndAllCompleted() throws Exception {
    int bound = GuavaCachePoolPentahoJcrSessionFactory.MAX_CONCURRENT_EVICTION_CLEANUPS;
    int evicted = bound + 3;
    CountDownLatch releaseLogouts = new CountDownLatch( 1 );
    AtomicInteger runningLogouts = new AtomicInteger();
    AtomicInteger maxRunningLogouts = new AtomicInteger();
    List<BlockingLogoutSession> sessions = new ArrayList<>();
    // One more user than evictions: the final session remains cached until the factory is invalidated.
    for ( int user = 0; user <= evicted; user++ ) {
      sessions.add( new BlockingLogoutSession( releaseLogouts, runningLogouts, maxRunningLogouts ) );
    }
    AtomicInteger logins = new AtomicInteger();
    Repository repository = mock( Repository.class );
    when( repository.login( any( SimpleCredentials.class ), eq( "workspace" ) ) )
      .thenAnswer( invocation -> sessions.get( logins.getAndIncrement() ).session );
    GuavaCachePoolPentahoJcrSessionFactory factory = configuredFactory( repository, "1", "300" );

    try {
      for ( int user = 0; user <= evicted; user++ ) {
        templateFor( factory, new SimpleCredentials( "user-" + user, new char[ 0 ] ) ).execute( s -> null, true );
      }
      for ( int running = 0; running < bound; running++ ) {
        assertTrue( "The first evictions must occupy every worker",
          sessions.get( running ).logoutStarted.await( 5, TimeUnit.SECONDS ) );
      }

      assertTrue( "The worker pool must never exceed its bound",
        GuavaCachePoolPentahoJcrSessionFactory.EVICTION_EXECUTOR.getPoolSize() <= bound );
      assertTrue( "Cleanups beyond the bound must wait in the queue",
        GuavaCachePoolPentahoJcrSessionFactory.EVICTION_EXECUTOR.getQueue().size() >= evicted - bound );
      assertEquals( "A cleanup beyond the bound must not start while every worker is busy",
        1, sessions.get( bound ).logoutStarted.getCount() );

      releaseLogouts.countDown();
      for ( int index = 0; index < evicted; index++ ) {
        assertTrue( "Every queued cleanup must eventually run",
          sessions.get( index ).logoutFinished.await( 5, TimeUnit.SECONDS ) );
      }
      assertTrue( "No more than the bounded number of logouts may run at once",
        maxRunningLogouts.get() <= bound );
    } finally {
      releaseLogouts.countDown();
      factory.invalidateCachedSessions();
    }
  }

  /**
   * Verifies a session evicted while in use is retired by the eviction itself, so it closes when its last user
   * releases it instead of waiting for a cleanup worker. Every worker is first held by a stalled logout, so a
   * retirement that was only queued would leave the session open after its callback.
   */
  @Test
  public void evictedSessionInUseClosesOnItsLastReleaseWhileCleanupWorkersAreStalled() throws Exception {
    int bound = GuavaCachePoolPentahoJcrSessionFactory.MAX_CONCURRENT_EVICTION_CLEANUPS;
    CountDownLatch releaseLogouts = new CountDownLatch( 1 );
    AtomicInteger runningLogouts = new AtomicInteger();
    AtomicInteger maxRunningLogouts = new AtomicInteger();
    List<BlockingLogoutSession> stalled = new ArrayList<>();
    // One more than the worker bound: evicting the first `bound` occupies every worker, the last one queues.
    for ( int user = 0; user <= bound; user++ ) {
      stalled.add( new BlockingLogoutSession( releaseLogouts, runningLogouts, maxRunningLogouts ) );
    }
    Session busy = trackedSession();
    Session evictor = trackedSession();
    List<Session> logins = new ArrayList<>();
    stalled.forEach( blocking -> logins.add( blocking.session ) );
    logins.add( busy );
    logins.add( evictor );
    AtomicInteger nextLogin = new AtomicInteger();
    Repository repository = mock( Repository.class );
    when( repository.login( any( SimpleCredentials.class ), eq( "workspace" ) ) )
      .thenAnswer( invocation -> logins.get( nextLogin.getAndIncrement() ) );
    GuavaCachePoolPentahoJcrSessionFactory factory = configuredFactory( repository, "1", "300" );

    try {
      for ( int user = 0; user <= bound; user++ ) {
        templateFor( factory, new SimpleCredentials( "stalled-" + user, new char[ 0 ] ) ).execute( s -> null, true );
      }
      for ( int worker = 0; worker < bound; worker++ ) {
        assertTrue( "Every cleanup worker must be held by a stalled logout",
          stalled.get( worker ).logoutStarted.await( 5, TimeUnit.SECONDS ) );
      }
      PentahoJcrTemplate evictorTemplate = templateFor( factory, new SimpleCredentials( "evictor", new char[ 0 ] ) );

      templateFor( factory, new SimpleCredentials( "busy", new char[ 0 ] ) ).execute( session -> {
        assertSame( busy, session );
        // With a cache size of 1, loading another user's session evicts this one while it is still in use.
        evictorTemplate.execute( s -> null, true );
        assertTrue( "Eviction must not close a session that is still in use", session.isLive() );
        return null;
      }, true );

      assertFalse( "A session evicted while in use must close on its last release, not wait for a cleanup worker",
        busy.isLive() );
    } finally {
      releaseLogouts.countDown();
      factory.invalidateCachedSessions();
    }
  }

  /**
   * Verifies a backlog of eviction logouts is reported once it reaches the warning threshold, and that the warning is
   * rate limited rather than repeated for every further eviction. Every worker is held by a stalled logout, so each
   * later eviction adds exactly one queued logout.
   */
  @Test
  public void evictionCleanupBacklogIsWarnedOnceWhenItReachesTheThreshold() throws Exception {
    awaitIdleEvictionExecutor();
    int bound = GuavaCachePoolPentahoJcrSessionFactory.MAX_CONCURRENT_EVICTION_CLEANUPS;
    int threshold = GuavaCachePoolPentahoJcrSessionFactory.EVICTION_QUEUE_WARNING_THRESHOLD;
    CountDownLatch releaseLogouts = new CountDownLatch( 1 );
    AtomicInteger runningLogouts = new AtomicInteger();
    AtomicInteger maxRunningLogouts = new AtomicInteger();
    List<Session> logins = new ArrayList<>();
    List<BlockingLogoutSession> stalled = new ArrayList<>();
    for ( int worker = 0; worker < bound; worker++ ) {
      BlockingLogoutSession blocking = new BlockingLogoutSession( releaseLogouts, runningLogouts, maxRunningLogouts );
      stalled.add( blocking );
      logins.add( blocking.session );
    }
    // threshold + 2 queued logouts, plus one session that stays cached.
    for ( int queued = 0; queued <= threshold + 2; queued++ ) {
      logins.add( trackedSession() );
    }
    AtomicInteger nextLogin = new AtomicInteger();
    Repository repository = mock( Repository.class );
    when( repository.login( any( SimpleCredentials.class ), eq( "workspace" ) ) )
      .thenAnswer( invocation -> logins.get( nextLogin.getAndIncrement() ) );
    GuavaCachePoolPentahoJcrSessionFactory factory = configuredFactory( repository, "1", "300" );
    org.slf4j.Logger logger = mock( org.slf4j.Logger.class );
    ReflectionTestUtils.setField( factory, "logger", logger );
    String warning = "JCR session eviction cleanup is falling behind";

    try {
      int user = 0;
      // Evicting the first `bound` sessions occupies every worker with a stalled logout.
      for ( ; user <= bound; user++ ) {
        templateFor( factory, new SimpleCredentials( "user-" + user, new char[ 0 ] ) ).execute( s -> null, true );
      }
      for ( BlockingLogoutSession blocking : stalled ) {
        assertTrue( blocking.logoutStarted.await( 5, TimeUnit.SECONDS ) );
      }
      // Each later eviction queues exactly one logout; stop one short of the threshold.
      for ( ; user < bound + threshold; user++ ) {
        templateFor( factory, new SimpleCredentials( "user-" + user, new char[ 0 ] ) ).execute( s -> null, true );
      }
      verify( logger, never() ).warn( startsWith( warning ), any( Object.class ), any( Object.class ) );

      templateFor( factory, new SimpleCredentials( "user-" + user++, new char[ 0 ] ) ).execute( s -> null, true );
      verify( logger, times( 1 ) ).warn( startsWith( warning ), any( Object.class ), any( Object.class ) );

      templateFor( factory, new SimpleCredentials( "user-" + user, new char[ 0 ] ) ).execute( s -> null, true );
      verify( logger, times( 1 ) ).warn( startsWith( warning ), any( Object.class ), any( Object.class ) );
    } finally {
      releaseLogouts.countDown();
      factory.invalidateCachedSessions();
    }
  }

  /**
   * Waits until earlier tests' eviction cleanups have drained from the shared executor, without polling. Each round
   * occupies every worker with a marker task; while all markers run, no other cleanup can run or be dequeued, so an
   * empty queue at that moment means everything earlier has finished. A marker can start on a newly created worker
   * ahead of queued work, so a non-empty queue releases the markers and tries again.
   */
  private void awaitIdleEvictionExecutor() throws InterruptedException {
    int workers = GuavaCachePoolPentahoJcrSessionFactory.MAX_CONCURRENT_EVICTION_CLEANUPS;
    for ( int round = 0; round < 5; round++ ) {
      CountDownLatch allWorkersReached = new CountDownLatch( workers );
      CountDownLatch release = new CountDownLatch( 1 );
      for ( int worker = 0; worker < workers; worker++ ) {
        GuavaCachePoolPentahoJcrSessionFactory.EVICTION_EXECUTOR.execute( () -> {
          allWorkersReached.countDown();
          try {
            release.await( 5, TimeUnit.SECONDS );
          } catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
          }
        } );
      }
      boolean drained;
      try {
        assertTrue( "Eviction workers did not become available", allWorkersReached.await( 5, TimeUnit.SECONDS ) );
        drained = GuavaCachePoolPentahoJcrSessionFactory.EVICTION_EXECUTOR.getQueue().isEmpty();
      } finally {
        release.countDown();
      }
      if ( drained ) {
        return;
      }
    }
    org.junit.Assert.fail( "Earlier eviction cleanups did not drain" );
  }

  /**
   * Creates a template whose sessions come from the supplied cache factory for one set of credentials.
   *
   * @param factory cache factory under test
   * @param credentials credentials used for every session acquisition
   * @return a template that creates sessions on demand
   * @throws RepositoryException never; declared by the mocked {@link SessionFactory#getSession()}
   */
  private PentahoJcrTemplate templateFor( GuavaCachePoolPentahoJcrSessionFactory factory,
                                          SimpleCredentials credentials ) throws RepositoryException {
    SessionFactory templateFactory = mock( SessionFactory.class );
    when( templateFactory.getSession() ).thenAnswer( invocation -> factory.getSession( credentials ) );
    PentahoJcrTemplate template = new PentahoJcrTemplate();
    template.setSessionFactory( templateFactory );
    template.setAllowCreate( true );
    return template;
  }

  /**
   * A Jackrabbit session mock whose {@code logout()} blocks until released, simulating slow repository I/O. It
   * records which thread ran the logout and how many logouts were running at once across all such sessions.
   */
  private static final class BlockingLogoutSession {
    /** The mock session. */
    private final SessionImpl session = mock( SessionImpl.class );
    /** Whether the session is still open. */
    private final AtomicBoolean live = new AtomicBoolean( true );
    /** Counted down when this session's logout begins. */
    private final CountDownLatch logoutStarted = new CountDownLatch( 1 );
    /** Counted down when this session's logout completes. */
    private final CountDownLatch logoutFinished = new CountDownLatch( 1 );
    /** The thread that ran this session's logout. */
    private final AtomicReference<Thread> logoutThread = new AtomicReference<>();

    /**
     * Creates the session.
     *
     * @param release latch that every logout waits on before completing
     * @param running counter of logouts currently in progress across sessions
     * @param maxRunning highest value {@code running} has reached
     */
    private BlockingLogoutSession( CountDownLatch release, AtomicInteger running, AtomicInteger maxRunning ) {
      Map<String, Object> attributes = new ConcurrentHashMap<>();
      when( session.isLive() ).thenAnswer( invocation -> live.get() );
      when( session.getAttribute( any( String.class ) ) )
        .thenAnswer( invocation -> attributes.get( invocation.getArgument( 0 ) ) );
      doAnswer( invocation -> {
        attributes.put( invocation.getArgument( 0 ), invocation.getArgument( 1 ) );
        return null;
      } ).when( session ).setAttribute( any( String.class ), any() );
      doAnswer( invocation -> {
        logoutThread.set( Thread.currentThread() );
        maxRunning.accumulateAndGet( running.incrementAndGet(), Math::max );
        logoutStarted.countDown();
        try {
          if ( !release.await( 10, TimeUnit.SECONDS ) ) {
            throw new AssertionError( "The test did not release the deliberately blocked logout" );
          }
        } finally {
          running.decrementAndGet();
        }
        live.set( false );
        logoutFinished.countDown();
        return null;
      } ).when( session ).logout();
    }
  }

  /**
   * Creates a live Jackrabbit session mock with observable attributes and lifecycle behavior.
   *
   * @return a session whose liveness, attributes, refresh, and logout are controlled by the fixture
   * @throws Exception if Mockito cannot configure the session fixture
   */
  private Session trackedSession() throws Exception {
    return trackedSession( null );
  }

  /** Creates a tracked session and optionally signals when its logout completes. */
  private Session trackedSession( CountDownLatch logoutCompleted ) throws Exception {
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
      if ( logoutCompleted != null ) {
        logoutCompleted.countDown();
      }
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
   * removal-listener behavior. The removal listener retires the holder on the invalidating thread, so the holder is
   * already retired when it is returned.
   *
   * @param factory factory whose cache is decorated for the test
   */
  @SuppressWarnings( "unchecked" )
  private void evictBeforeEveryAcquisition( GuavaCachePoolPentahoJcrSessionFactory factory ) {
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