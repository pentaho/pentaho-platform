/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.repository2.unified.jcr.sejcr;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.apache.jackrabbit.core.SessionImpl;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JCR session factory that caches sessions by credential identity and requesting thread ID.
 * Cache capacity and entry lifetime are configured with the repository system configuration. Eviction retires the
 * associated {@link CachedJcrSession} immediately; an idle session is then logged out on a background cleanup
 * worker, while a session still in use is logged out by its last user when that user releases it. Zero cache size
 * or TTL selects direct-login sessions managed for the duration of use.
 */
class GuavaCachePoolPentahoJcrSessionFactory extends NoCachePentahoJcrSessionFactory
  implements PentahoJcrSessionFactory {

  static final String USAGE_COUNT = "usage_count"; // diagnostic count of session usages
  private static final String REPOSITORY_CONFIG_ID = "repository";
  /** Maximum number of cache lookup/lease-acquisition races before using a direct-login fallback. */
  private static final int MAX_CACHE_ACQUISITION_ATTEMPTS = 3;

  /** Maximum number of eviction cleanups (repository logouts) that may run concurrently. */
  static final int MAX_CONCURRENT_EVICTION_CLEANUPS = 4;
  /** Idle time after which an eviction worker thread exits, so an idle system holds no cleanup threads. */
  private static final long EVICTION_WORKER_KEEP_ALIVE_SECONDS = 60;
  /** Number of queued eviction logouts at which a warning is logged that cleanup is falling behind. */
  static final int EVICTION_QUEUE_WARNING_THRESHOLD = 100;
  /** Minimum time between two backlog warnings from one factory, so a long stall does not flood the log. */
  private static final long EVICTION_QUEUE_WARNING_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos( 1 );

  /**
   * Shared daemon executor for the repository logouts of evicted idle sessions. Logout may perform repository I/O, so
   * running it here prevents an unrelated cache-acquisition thread from blocking on that work.
   * <p>
   * Concurrency is bounded at {@link #MAX_CONCURRENT_EVICTION_CLEANUPS} worker threads, so slow or stuck logouts
   * cannot exhaust native threads. Logouts beyond that wait in an unbounded queue: a retired session must always be
   * logged out eventually, so a logout is never rejected, dropped or run on the thread that triggered the eviction.
   * <p>
   * Every queued entry is a session that has already been retired (no new user can acquire it) and is idle. Each
   * queued logout corresponds to one session that was previously logged in through the cache, so the queue can grow
   * only as fast as new sessions are logged in. A backlog of {@link #EVICTION_QUEUE_WARNING_THRESHOLD} logouts is
   * reported as a warning.
   */
  static final ThreadPoolExecutor EVICTION_EXECUTOR = createEvictionExecutor();

  /** Owning Spring factory used to detect sessions already bound under its transaction resource key. */
  private CredentialsStrategySessionFactory credentialsStrategySessionFactory;
  private int cacheDuration = 300;
  private int cacheSize = 100;

  private Logger logger = LoggerFactory.getLogger( getClass() );
  /**
   * Reports whether a REQUIRES_NEW transaction is being created; {@code null} disables the cache bypass. Set while the
   * Spring context wires the factory, before it serves requests.
   */
  private PentahoTransactionManager transactionManager;
  /** Workspace passed to repository login, kept for diagnostics; {@code null} means the repository default. */
  private final String workspaceName;
  /** Earliest {@link System#nanoTime()} at which this factory may log another eviction backlog warning. */
  private final AtomicLong nextEvictionBacklogWarningNanos = new AtomicLong( System.nanoTime() );
  /** Cache of per-thread session lifecycle holders. */
  private final LoadingCache<CacheKey, CachedJcrSession> sessionCache;


  /**
   * Creates a cache-backed session factory using the repository's configured cache size and time-to-live.
   *
   * @param repository repository used to create JCR sessions
   * @param workspace workspace passed to repository login
   */
  public GuavaCachePoolPentahoJcrSessionFactory( Repository repository, String workspace ) {
    this( repository, workspace, null );
  }

  /**
   * Creates a cache-backed factory and optionally bypasses the cache while a transaction is being created.
   * Cache configuration is read before the cache is constructed; a size or TTL of zero selects managed direct
   * sessions instead of retaining entries.
   *
   * @param repository repository used to create JCR sessions
   * @param workspace workspace passed to repository login
   * @param transactionManager optional manager used to detect transaction-creation context; may be {@code null}
   */
  public GuavaCachePoolPentahoJcrSessionFactory( Repository repository, String workspace,
                                                 PentahoTransactionManager transactionManager ) {
    super( repository, workspace );
    this.workspaceName = workspace;
    this.transactionManager = transactionManager;

    ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
    if ( systemConfig != null && systemConfig.getConfiguration( REPOSITORY_CONFIG_ID ) != null ) {
      try {
        this.cacheDuration =
          Integer.parseInt( systemConfig.getConfiguration( REPOSITORY_CONFIG_ID ).getProperties().getProperty(
            "cache-ttl", "300" ) );


        this.cacheSize =
          Integer.parseInt( systemConfig.getConfiguration( REPOSITORY_CONFIG_ID ).getProperties().getProperty(
            "cache-size", "100" ) );
      } catch ( IOException e ) {
        logger.info( "Could not find repository.cache-duration" );
      }
    }
    // Build only after reading configuration; a field initializer would always use the defaults.
    this.sessionCache = createSessionCache();
  }

  /**
   * Creates the bounded daemon pool used for eviction cleanup; idle workers time out.
   *
   * @return the configured executor
   */
  private static ThreadPoolExecutor createEvictionExecutor() {
    ThreadPoolExecutor executor = new ThreadPoolExecutor( MAX_CONCURRENT_EVICTION_CLEANUPS,
      MAX_CONCURRENT_EVICTION_CLEANUPS, EVICTION_WORKER_KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
      new LinkedBlockingQueue<>(), runnable -> {
        Thread thread = new Thread( runnable, "jcr-cached-session-eviction" );
        thread.setDaemon( true );
        return thread;
      } );
    executor.allowCoreThreadTimeOut( true );
    return executor;
  }

  /**
   * Builds the cache keyed by credentials and calling thread ID, keeping concurrent callers on separate entries.
   * The removal listener runs on the thread that caused the removal and only retires the holder, which is a short
   * in-memory state change: from then on no caller can acquire the evicted session. Only the repository logout of an
   * idle session is handed to {@link #EVICTION_EXECUTOR}; a session still in use is logged out by its last release.
   * The loader wraps each newly logged-in session with its lifecycle holder.
   *
   * @return a configured loading cache for session holders
   * @see PentahoJcrTemplate#execute(org.springframework.extensions.jcr.JcrCallback, boolean)
   */
  private LoadingCache<CacheKey, CachedJcrSession> createSessionCache() {
    return CacheBuilder.newBuilder()
      .expireAfterAccess( cacheDuration, TimeUnit.SECONDS )
      .maximumSize( cacheSize )
      .removalListener( (RemovalListener<CacheKey, CachedJcrSession>) objectObjectRemovalNotification -> {
        CachedJcrSession cachedSession = objectObjectRemovalNotification.getValue();
        if ( cachedSession != null && cachedSession.markRetired() ) {
          logger.debug( "Logging out evicted idle cached session {}", cachedSession.getSession() );
          scheduleEvictionLogout( cachedSession );
        }
      } ).recordStats()
      .build( new CacheLoader<CacheKey, CachedJcrSession>() {
        @Override public CachedJcrSession load( CacheKey credKey ) throws Exception {
          Session session = GuavaCachePoolPentahoJcrSessionFactory.super.getSession( credKey.creds );
          return trackSession( session );
        }
      } );
  }

  /**
   * Queues the logout of an evicted idle session on {@link #EVICTION_EXECUTOR}, then warns if the backlog has reached
   * {@link #EVICTION_QUEUE_WARNING_THRESHOLD}.
   *
   * @param cachedSession retired holder whose logout was claimed by {@link CachedJcrSession#markRetired()}
   */
  private void scheduleEvictionLogout( CachedJcrSession cachedSession ) {
    EVICTION_EXECUTOR.execute( cachedSession::performClaimedLogout );
    warnIfEvictionCleanupIsFallingBehind();
  }

  /**
   * Logs a warning, at most once per {@link #EVICTION_QUEUE_WARNING_INTERVAL_NANOS} per factory, when the number of
   * queued eviction logouts has reached {@link #EVICTION_QUEUE_WARNING_THRESHOLD}. A growing backlog usually means
   * repository logouts are slow or stalled; the queued sessions stay open until their logout runs.
   */
  private void warnIfEvictionCleanupIsFallingBehind() {
    int queued = EVICTION_EXECUTOR.getQueue().size();
    if ( queued < EVICTION_QUEUE_WARNING_THRESHOLD ) {
      return;
    }
    long now = System.nanoTime();
    long next = nextEvictionBacklogWarningNanos.get();
    if ( now - next >= 0
      && nextEvictionBacklogWarningNanos.compareAndSet( next, now + EVICTION_QUEUE_WARNING_INTERVAL_NANOS ) ) {
      logger.warn( "JCR session eviction cleanup is falling behind: {} session logouts are waiting for {} cleanup "
        + "workers. Repository logouts may be slow or stalled.", queued, MAX_CONCURRENT_EVICTION_CLEANUPS );
    }
  }

  /**
   * Associates a newly created Jackrabbit session with a lifecycle holder and diagnostic usage counter.
   * Non-{@link SessionImpl} implementations are still returned in a holder, but cannot expose these attributes.
   *
   * @param session newly created repository session
   * @return the holder used to coordinate retirement and active use
   */
  private CachedJcrSession trackSession( Session session ) {
    CachedJcrSession cachedSession = new CachedJcrSession( session );
    if ( session instanceof SessionImpl ) {
      ( (SessionImpl) session ).setAttribute( CachedJcrSession.SESSION_ATTRIBUTE, cachedSession );
      ( (SessionImpl) session ).setAttribute( USAGE_COUNT, new AtomicInteger( 0 ) );
    } else {
      logger.warn( "Expected a Jackrabbit SessionImpl. Will not be tracking cached session lifecycle." );
    }
    return cachedSession;
  }

  /**
   * Obtains a direct-login session and, where session attributes are supported, gives it the same lease lifecycle as
   * a cached session while marking it retired so that it closes after its final callback or Spring binding.
   *
   * @param creds credentials supplied to the repository
   * @return the direct-login session
   * @throws RepositoryException if repository login fails
   */
  private Session getUncachedSession( Credentials creds ) throws RepositoryException {
    Session session = super.getSession( creds );
    if ( session instanceof SessionImpl ) {
      CachedJcrSession cachedSession = trackSession( session );
      cachedSession.acquire();
      incrementFactoryUsageCount( session, creds );
      // No cache owns this fallback: close it after the callback and any Spring binding have released it.
      cachedSession.retire();
    }
    return session;
  }

  /**
   * Returns a session for the supplied credentials, using the per-thread cache when eligible and a managed direct
   * login when caching is disabled or cache acquisition cannot complete. A {@code REQUIRES_NEW} transaction being
   * created gets a direct login that never enters the cache, so it cannot share a session with the transaction it
   * suspends.
   *
   * @param creds credentials for the repository login
   * @return a session for the caller; cached sessions carry a factory lease until the template accepts or rolls it back
   * @throws RepositoryException if the direct or fallback repository login fails
   */
  @Override public Session getSession( Credentials creds ) throws RepositoryException {
    if ( isCreatingIndependentTransaction() ) {
      logger.debug( "An independent transaction is being created, obtaining session directly, not cached: {}",
        creds );
      return super.getSession( creds );
    }
    // The cache key is built from SimpleCredentials; other credential types (for example GuestCredentials) are
    // not cacheable, so they take the managed direct login instead of failing in the cache and logging an error.
    if ( cacheSize == 0 || cacheDuration == 0 || ( creds != null && !( creds instanceof SimpleCredentials ) ) ) {
      return getUncachedSession( creds );
    }

    logger.debug( "Thread is not transacted, checking cache for session: {}", creds );
    try {
      Session session = getCachedSession( creds );
      if ( session != null ) {
        return session;
      }
    } catch ( ExecutionException e ) {
      if ( e.getCause() instanceof RepositoryException ) {
        return retryLoginAfterCacheLoadFailure( creds, (RepositoryException) e.getCause() );
      }
      logger.error( "Error obtaining session from cache. Creating one directly instead: {}", creds, e );
    } catch ( Exception e ) {
      logger.error( "Error obtaining session from cache. Creating one directly instead: {}", creds, e );
    }
    return getUncachedSession( creds );
  }

  /**
   * Handles a repository login that failed inside the cache loader. Guava wraps the loader's exception, so without
   * this the failure looks like a cache problem although the loader only calls {@code repository.login}. The failure
   * is logged as a login failure with the user, workspace and thread, then the existing direct-login retry runs, and
   * its outcome is logged too: a successful retry means the first failure was transient; a failed retry means the
   * repository denies this user or workspace, which a session cache cannot fix. The password and credential
   * attributes (such as the pre-authentication token) are never logged.
   *
   * @param creds credentials of the failed login
   * @param loadFailure the repository exception thrown by the cache loader
   * @return the session from the direct retry
   * @throws RepositoryException the retry's own failure, propagated unchanged to the caller, which logs it
   */
  private Session retryLoginAfterCacheLoadFailure( Credentials creds, RepositoryException loadFailure )
      throws RepositoryException {
    String user = describeUser( creds );
    String workspace = workspaceName == null ? "<default>" : workspaceName;
    String thread = Thread.currentThread().getName();
    logger.error( "Repository login failed while loading a cached JCR session: user={} workspace={} thread={}."
      + " Retrying with a direct login.", user, workspace, thread, loadFailure );
    boolean recovered = false;
    try {
      Session session = getUncachedSession( creds );
      recovered = true;
      logger.warn( "Direct login retry succeeded after the cached login failed: user={} workspace={} thread={}."
        + " The first failure was transient.", user, workspace, thread );
      return session;
    } finally {
      if ( !recovered ) {
        // The retry's exception propagates unchanged to the caller, which logs its stack trace.
        logger.error( "Direct login retry also failed: user={} workspace={} thread={}. The repository denies this"
          + " login; it is not a session cache problem.", user, workspace, thread );
      }
    }
  }

  /**
   * Identifies the login's user for diagnostics without exposing secrets.
   *
   * @param creds credentials of a login attempt; may be {@code null}
   * @return the user ID for {@link SimpleCredentials}, otherwise the credentials type
   */
  private static String describeUser( Credentials creds ) {
    if ( creds instanceof SimpleCredentials ) {
      return ( (SimpleCredentials) creds ).getUserID();
    }
    return creds == null ? "<none>" : creds.getClass().getSimpleName();
  }

  /**
   * Reports whether the configured transaction manager is currently beginning a {@code REQUIRES_NEW} transaction.
   *
   * @return {@code true} when a manager is configured and an independent transaction is being created
   */
  private boolean isCreatingIndependentTransaction() {
    return transactionManager != null && transactionManager.isCreatingIndependentTransaction();
  }

  /**
   * Attempts a bounded number of cache acquisitions, retrying when eviction wins the race with lease acquisition.
   *
   * @param creds credentials used to derive the cache key
   * @return an acquired live session, or {@code null} when all attempts lose to eviction
   * @throws ExecutionException if loading a session into the cache fails
   * @throws RepositoryException if validating or refreshing the cached session fails
   */
  private Session getCachedSession( Credentials creds ) throws ExecutionException, RepositoryException {
    CacheKey key = new CacheKey( creds );
    for ( int attempt = 0; attempt < MAX_CACHE_ACQUISITION_ATTEMPTS; attempt++ ) {
      CachedJcrSession cachedSession = sessionCache.get( key );
      Session session = acquireCachedSession( key, cachedSession, creds );
      if ( session != null ) {
        return session;
      }
    }
    logger.debug( "Cache eviction prevented acquisition; using an uncached session for {}", creds );
    return null;
  }

  /**
   * Validates and retains a holder returned by the cache. Invalid, already-bound, or refresh-failing entries are
   * removed, and any factory lease that is not handed to the caller is rolled back in {@code finally}.
   *
   * @param key cache key associated with the holder
   * @param cachedSession holder returned by the cache
   * @param creds credentials used for diagnostics
   * @return the live session when acquisition succeeds; {@code null} when the holder cannot be used
   * @throws RepositoryException if refreshing the session fails; the invalid entry is removed first
   */
  private Session acquireCachedSession( CacheKey key, CachedJcrSession cachedSession, Credentials creds )
      throws RepositoryException {
    if ( !cachedSession.acquire() ) {
      sessionCache.asMap().remove( key, cachedSession );
      return null;
    }

    boolean keepLease = false;
    try {
      Session session = cachedSession.getSession();
      if ( !session.isLive() ) {
        logger.debug( "Cached session is no longer alive. disposing: {}", creds );
        sessionCache.asMap().remove( key, cachedSession );
        return null;
      }

      if ( isBoundToTransaction( session ) ) {
        logger.debug( "Session is bound to a transaction. Ignoring it and creating a new session: {}", creds );
        sessionCache.asMap().remove( key, cachedSession );
        return null;
      }

      session.refresh( false );
      incrementFactoryUsageCount( session, creds );
      keepLease = true;
      return session;
    } catch ( Exception e ) {
      sessionCache.asMap().remove( key, cachedSession );
      throw e;
    } finally {
      if ( !keepLease ) {
        cachedSession.releaseUnhandedFactoryLease();
      }
    }
  }

  /**
   * Reports whether the session is the one bound to the current Spring transaction. Spring binds the
   * logout-suppressing proxy returned by {@link CredentialsStrategySessionFactory#getSession()}, so the bound session
   * is unwrapped before it is compared with the cached session.
   *
   * @param session cached session to look for
   * @return {@code true} when the current thread's bound holder wraps this session
   */
  private boolean isBoundToTransaction( Session session ) {
    if ( credentialsStrategySessionFactory == null ) {
      return false;
    }
    Object resource = TransactionSynchronizationManager.getResource( credentialsStrategySessionFactory );
    if ( !( resource instanceof SessionHolder ) ) {
      return false;
    }
    Session bound = ( (SessionHolder) resource ).getSession();
    return bound == session || CredentialsStrategySessionFactory.unwrapSessionProxy( bound ) == session;
  }

  /**
   * Increments the legacy diagnostic counter when a session is handed off by the factory.
   * Lifecycle safety is enforced by {@link CachedJcrSession}; this counter remains useful for diagnostics.
   *
   * @param session session carrying the counter attribute
   * @param creds credentials used to identify the user in debug logging
   */
  private void incrementFactoryUsageCount( Session session, Credentials creds ) {
    Object usageCount = session.getAttribute( USAGE_COUNT );
    if ( usageCount instanceof AtomicInteger ) {
      int newCount = ( (AtomicInteger) usageCount ).incrementAndGet();
      if ( logger.isDebugEnabled() ) {
        logger.debug( "[JCR-FACTORY-RETRIEVE] Thread={} SessionId={} RefCount={} User={}",
          Thread.currentThread().getName(), System.identityHashCode( session ), newCount,
          creds instanceof SimpleCredentials ? ( (SimpleCredentials) creds ).getUserID() : null );
      }
    }
  }

  /**
   * Invalidates all cache entries and asks Guava to dispatch any pending removal notifications.
   * Retired sessions still in use remain open until their outstanding lifecycle leases are released.
   */
  void invalidateCachedSessions() {
    sessionCache.invalidateAll();
    sessionCache.cleanUp();
  }

  /**
   * Wires the owning {@link SessionFactory} used as the key for
   * {@code SessionFactoryUtils.isSessionThreadBound(Session, SessionFactory)}. Without this reference, the guard
   * cannot detect a session bound to this factory and may return it again from the cache.
   *
   * @param credentialsStrategySessionFactory owning Spring session factory used for resource lookup
   */
  void setCredentialsStrategySessionFactory( CredentialsStrategySessionFactory credentialsStrategySessionFactory ) {
    this.credentialsStrategySessionFactory = credentialsStrategySessionFactory;
  }

  /**
   * Registers the manager that reports when a {@code REQUIRES_NEW} transaction is being created. While one is,
   * sessions are logged in directly and never taken from, or shared through, the per-thread cache: a transaction
   * owns its session, and a {@code REQUIRES_NEW} transaction must not reuse the session of the transaction it
   * suspended.
   *
   * @param transactionManager manager reporting transaction creation; {@code null} disables the bypass
   */
  void setTransactionManager( PentahoTransactionManager transactionManager ) {
    this.transactionManager = transactionManager;
  }

  /**
   * Immutable cache key that partitions a credentials identity by the requesting thread ID.
   * Credentials equality intentionally uses the user ID; the thread component keeps concurrent callers separated.
   */
  private static final class CacheKey {
    /** Credentials used to derive the user identity portion of this key. */
    private final SimpleCredentials creds;
    /** Requesting thread identity; prevents sessions from being shared across threads. */
    private final long threadId;

    /** Captures the credentials identity and current thread for one cache lookup. */
    private CacheKey( Credentials creds ) {
      this.creds = (SimpleCredentials) creds;
      this.threadId = Thread.currentThread().getId();
    }

    /**
     * Compares cache keys by user identity and requesting thread, deliberately ignoring password and credential
     * attributes so one user's session can be reused for that thread.
     *
     * @param o object to compare
     * @return {@code true} when both keys identify the same user and thread
     */
    @Override
    public boolean equals( Object o ) {
      if ( this == o ) {
        return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
        return false;
      }

      CacheKey cacheKey = (CacheKey) o;

      return Objects.equals( creds != null ? creds.getUserID() : null,
        cacheKey.creds != null ? cacheKey.creds.getUserID() : null )
        && threadId == cacheKey.threadId;
    }

    /** Returns a hash consistent with {@link #equals(Object)}. */
    @Override
    public int hashCode() {
      int result = creds != null ? creds.getUserID().hashCode() : 0;
      result = 31 * result + Long.hashCode( threadId );
      return result;
    }
  }

}
