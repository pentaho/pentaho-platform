/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.engine.security.authorization.core.caching;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.util.concurrent.UncheckedExecutionException;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.ILogoutListener;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISessionContainer;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCache;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCacheKey;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

public class MemoryAuthorizationDecisionCache implements
  IAuthorizationDecisionCache,
  ILogoutListener,
  Closeable {

  // region Helper classes

  /**
   * Holds a session cache and manages the set of Pentaho sessions associated with it.
   * <p>
   * The sessions associated with this session cache data are expected to share the same session key, as defined by
   * {@link MemoryAuthorizationDecisionCache#getSessionKey(IPentahoSession)}.
   */
  private class SessionCacheData {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Stores the set of sessions associated with this session cache data.
     * <p>
     * A weak set is used, so that sessions can be garbage collected if there are no other strong references to them.
     * This is only a fallback mechanism, for badly behaved used of sessions, such as direct uses of
     * {@link org.pentaho.platform.api.engine.ISecurityHelper#becomeUser(String)} without a corresponding destruction of
     * the created session. Sessions are expected to be explicitly disassociated from this session cache data, either
     * via a logout listener (for {@code PentahoHttpSession} sessions), or via explicit destruction
     * (for {@link StandaloneSession} sessions).
     * <p>
     * Hiding and cleaning up stale (GC'd) sessions is handled by the {@link WeakHashMap} implementation.
     */
    private final Set<IPentahoSession> sessions = Collections.newSetFromMap( new WeakHashMap<>() );

    @NonNull
    private final Cache<IAuthorizationDecisionCacheKey, IAuthorizationDecision> cache;

    public SessionCacheData( @NonNull Cache<IAuthorizationDecisionCacheKey, IAuthorizationDecision> cache,
                             @NonNull IPentahoSession session ) {
      this.cache = cache;

      addSessionCore( session );
    }

    @NonNull
    public Cache<IAuthorizationDecisionCacheKey, IAuthorizationDecision> getCache(
      @NonNull IPentahoSession forSession ) {
      lock.readLock().lock();
      try {
        if ( sessions.contains( forSession ) ) {
          return cache;
        }
      } finally {
        lock.readLock().unlock();
      }

      lock.writeLock().lock();
      try {
        addSessionCore( forSession );
      } finally {
        lock.writeLock().unlock();
      }

      return cache;
    }

    /**
     * Associates a session with this session cache data.
     * <p>
     * If the session is already associated, does nothing.
     * <p>
     * For {@link StandaloneSession} sessions, adds a {@link ISessionContainer session container} to the session, so
     * that, when the session is destroyed, the session is disassociated from this session cache data.
     * <p>
     * Standalone sessions must be explicitly destroyed, by, calling its destroy() method.
     * To listen to a session's destroy "event", an {@link ISessionContainer} valued attribute is set on the session.
     * Upon destroy(), any associated container's {@link ISessionContainer#setSession(IPentahoSession)} method is called
     * with a null value, to disassociate the session from the container.
     * <p>
     * This contrasts with {@code PentahoHttpSession} sessions, which are "destroyed" via a {@link ILogoutListener
     * logout listener}.
     *
     * @param session The session to associate.
     */
    private void addSessionCore( @NonNull IPentahoSession session ) {
      if ( sessions.add( session )
        && ( session instanceof StandaloneSession standaloneSession ) ) {

        session.setAttribute(
          StandaloneSessionContainer.class.getName(),
          new StandaloneSessionContainer( standaloneSession ) );
      }
    }

    /**
     * Disassociates a session from this session cache data.
     *
     * @param session The session to disassociate.
     * @return {@code true} if the session was disassociated and there are no more associated sessions;
     * {@code false} otherwise.
     */
    public boolean removeSession( @NonNull IPentahoSession session ) {
      lock.writeLock().lock();
      try {
        return sessions.remove( session ) && sessions.isEmpty();
      } finally {
        lock.writeLock().unlock();
      }
    }

    public void invalidate( @NonNull IAuthorizationDecisionCacheKey key ) {
      cache.invalidate( key );
    }

    public void invalidateAll( @NonNull Predicate<IAuthorizationDecisionCacheKey> predicate ) {
      // There doesn't seem to be a "safer" way to iterate the cache keys using the Guava Cache API.
      // The asMap() and its keySet() are both views, backed by the real objects, so they "suffer" from concurrent
      // modification issues. The `asMap()` documentation says:
      // > Iterators from the returned map are at least <i>weakly consistent</i>: they are safe for
      // > concurrent use, but if the cache is modified (including by eviction) after the iterator is
      // > created, it is undefined which of the changes (if any) will be reflected in that iterator.
      // So, it seems that only _changes_ to the cache would possibly not be visible, not the original values at the
      // time
      // when the iterator was created. This is consistent with the general goals described for the main class's
      // invalidate* methods.
      var invalidateRequests = cache
        .asMap()
        .keySet()
        .stream()
        .filter( predicate )
        .toList();

      cache.invalidateAll( invalidateRequests );
    }

    /**
     * Disposes this session cache data, by clearing all associated sessions and disposing the shared cache.
     */
    public void dispose() {
      lock.writeLock().lock();
      try {
        sessions.clear();
        cache.invalidateAll();
        cache.cleanUp();
      } finally {
        lock.writeLock().unlock();
      }
    }
  }

  private class StandaloneSessionContainer implements ISessionContainer {
    @NonNull
    private final StandaloneSession session;

    public StandaloneSessionContainer( @NonNull StandaloneSession session ) {
      this.session = session;
    }

    @Override
    public void setSession( IPentahoSession cleanupSession ) {
      invalidateSession( session );
    }
  }

  private static class AuthorizationDecisionCacheKey implements IAuthorizationDecisionCacheKey {
    @NonNull
    private final IAuthorizationRequest request;
    @NonNull
    private final IAuthorizationOptions options;

    public AuthorizationDecisionCacheKey( @NonNull IAuthorizationRequest request,
                                          @NonNull IAuthorizationOptions options ) {
      this.request = request;
      this.options = options;
    }

    @NonNull
    @Override
    public IAuthorizationRequest getRequest() {
      return request;
    }

    @NonNull
    @Override
    public IAuthorizationOptions getOptions() {
      return options;
    }

    @Override
    public boolean equals( Object o ) {
      if ( this == o ) {
        return true;
      }

      if ( !( o instanceof IAuthorizationDecisionCacheKey that ) ) {
        return false;
      }

      return request.equals( that.getRequest() )
        && options.equals( that.getOptions() );
    }

    @Override
    public int hashCode() {
      int result = request.hashCode();
      result = 31 * result + options.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return String.format( "AuthorizationDecisionCacheKey[request=%s, options=%s]", request, options );
    }
  }
  // endregion Helper classes

  private final long expireAfterWrite;
  private final long maximumSize;
  private final boolean recordStats;

  private final ReentrantReadWriteLock sessionsLock = new ReentrantReadWriteLock();

  // Thread-guarded by sessionsLock.
  @NonNull
  private final Map<String, SessionCacheData> cacheBySessionKey;

  // Accumulates stats of removed session caches.
  // Thread-guarded by sessionsLock.
  private CacheStats pastCacheStats = new CacheStats( 0, 0, 0, 0, 0, 0 );

  public MemoryAuthorizationDecisionCache(
    long expireAfterWrite,
    long maximumSize,
    boolean recordStats ) {

    this.expireAfterWrite = expireAfterWrite;
    this.maximumSize = maximumSize;
    this.recordStats = recordStats;

    this.cacheBySessionKey = new HashMap<>();

    registerLogoutListener();
  }

  // region Global and Per-session cache management
  @NonNull
  protected Cache<IAuthorizationDecisionCacheKey, IAuthorizationDecision> createCache() {
    final var cacheBuilder = CacheBuilder.newBuilder()
      .expireAfterWrite( expireAfterWrite, TimeUnit.SECONDS )
      .maximumSize( maximumSize );

    if ( recordStats ) {
      cacheBuilder.recordStats();
    }

    return cacheBuilder.build();
  }

  @NonNull
  protected String getSessionKey( IPentahoSession session ) {
    // Using session name so that different sessions of same user (e.g. one StandaloneSession and one
    // PentahoHttpSession) use the same cache. It is common for the current PentahoHttpSession to create several
    // StandaloneSessions, typically via runAsUser, or runAsSystem. Using session ID would associate different caches to
    // the same user.
    String sessionKey = session.getName();
    if ( sessionKey == null ) {
      throw new IllegalStateException( "Pentaho session without name" );
    }

    return sessionKey;
  }

  @NonNull
  protected Optional<Cache<IAuthorizationDecisionCacheKey, IAuthorizationDecision>> getCacheOptional() {
    var session = getSession();
    var sessionKey = getSessionKey( session );

    sessionsLock.readLock().lock();
    try {
      return Optional
        .ofNullable( cacheBySessionKey.get( sessionKey ) )
        .map( cacheData -> cacheData.getCache( session ) );
    } finally {
      sessionsLock.readLock().unlock();
    }
  }

  @NonNull
  protected Cache<IAuthorizationDecisionCacheKey, IAuthorizationDecision> getCache() {
    var session = getSession();
    var sessionKey = getSessionKey( session );

    SessionCacheData cacheData;

    // Happy path: a cache already exists for this session (key).
    sessionsLock.readLock().lock();
    try {
      cacheData = cacheBySessionKey.get( sessionKey );
      if ( cacheData != null ) {
        return cacheData.getCache( session );
      }
    } finally {
      sessionsLock.readLock().unlock();
    }

    // Slow path: create a cache for this session (key), if it's not there yet.
    sessionsLock.writeLock().lock();
    try {
      // Recheck, after acquiring write lock.
      cacheData = cacheBySessionKey.get( sessionKey );
      if ( cacheData != null ) {
        return cacheData.getCache( session );
      }

      var cache = createCache();
      cacheData = new SessionCacheData( cache, session );
      cacheBySessionKey.put( sessionKey, cacheData );
      return cache;
    } finally {
      sessionsLock.writeLock().unlock();
    }
  }

  protected void invalidateSession( @NonNull IPentahoSession session ) {
    var sessionKey = getSessionKey( session );

    // Assume it likely has a corresponding cache, and enter write lock directly.
    sessionsLock.writeLock().lock();
    try {
      // Recheck, after acquiring write lock.
      var cacheData = cacheBySessionKey.get( sessionKey );
      if ( cacheData != null && cacheData.removeSession( session ) ) {
        // Last session, so dispose and remove session cache data from the map.

        // Store stats of removed cache.
        if ( recordStats ) {
          pastCacheStats = pastCacheStats.plus( cacheData.cache.stats() );
        }

        cacheData.dispose();
        cacheBySessionKey.remove( sessionKey );
      }
    } finally {
      sessionsLock.writeLock().unlock();
    }
  }
  // endregion Global and Per-session cache management

  // region Pentaho Integration
  // Associate cache entries to the current session.
  // Facilitates invalidation of all entries for a given session on logout,
  // which is important for, for example, testing environments, to avoid cross test contamination.
  @VisibleForTesting
  protected void registerLogoutListener() {
    PentahoSystem.addLogoutListener( this );
  }

  @VisibleForTesting
  protected void unregisterLogoutListener() {
    PentahoSystem.remove( this );
  }

  @VisibleForTesting
  @NonNull
  protected IPentahoSession getSession() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    Assert.notNull( session, "No current Pentaho session" );
    return session;
  }

  // Called for PentahoHttpSession. Not for others: StandaloneSession.
  @Override
  public void onLogout( IPentahoSession session ) {
    invalidateSession( Objects.requireNonNull( session ) );
  }

  @Override
  public void close() throws IOException {
    unregisterLogoutListener();
    invalidateAll();
  }
  // endregion Pentaho Integration

  // region Main get, put methods
  @NonNull
  @Override
  public Optional<IAuthorizationDecision> get( @NonNull IAuthorizationRequest request,
                                               @NonNull IAuthorizationOptions options ) {

    var key = createAuthorizationKey( request, options );

    return getCacheOptional()
      .map( cache -> cache.getIfPresent( key ) );
  }

  @NonNull
  @Override
  public IAuthorizationDecision get(
    @NonNull IAuthorizationRequest request,
    @NonNull IAuthorizationOptions options,
    @NonNull Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader ) {

    var key = createAuthorizationKey( request, options );
    try {
      return Objects.requireNonNull( getCache().get( key, () -> loader.apply( key ) ) );
    } catch ( ExecutionException e ) {
      throw new UncheckedExecutionException( e );
    }
  }

  @Override
  public void put( @NonNull IAuthorizationRequest request,
                   @NonNull IAuthorizationOptions options,
                   @NonNull IAuthorizationDecision decision ) {
    var key = createAuthorizationKey( request, options );
    getCache().put( key, decision );
  }

  @NonNull
  protected IAuthorizationDecisionCacheKey createAuthorizationKey( @NonNull IAuthorizationRequest request,
                                                                   @NonNull IAuthorizationOptions options ) {
    return new AuthorizationDecisionCacheKey( request, options );
  }
  // endregion Main get, put methods

  // region Authorization Request Invalidation

  // NOTE: Regarding all the invalidate* methods below:
  //
  // It is possible that, after the session map is copied and the end of the call, a new session cache is created and
  // added to the original session map.
  // This is acceptable, as the new cache will be empty, so there is nothing to invalidate.
  //
  // It is also possible that a session cache is removed from the original map, remaining present in the copied map.
  // This is also acceptable, as the session cache will simply be invalidated again.
  //
  // Finally, it is also possible that authorization decision(s) are added to the captured session caches, during the
  // per-session invalidation process. For example:
  // 1. invalidate session A
  // 2. new auth added to session C cache (not yet invalidated)
  // 3. invalidate session B
  // 4. invalidate session C
  // 5. new auth added to session A cache (already invalidated)
  //
  // In case 2, above, the new auth will be invalidated in step 4, even though it was likely added to the cache after
  // the invalidation process started. If a write lock were used, the entry would end up in the cache, which could be
  // worse.
  // In case 5, above, the new auth will not be invalidated, but this is acceptable, as the invalidation process
  // started before the new auth was added to the cache.
  //
  // All this appears to be a good compromise between consistency, performance and implementation complexity, especially
  // given the main use cases for these methods, of being able to invalidate authorization decisions when domain objects
  // change, such as when a user's roles change, or a role's permissions change.
  // One point in favor is that these changes are expected to be relatively infrequent, compared to authorization
  // checks.
  //
  // If stronger consistency is required in the future, a more complex approach would be needed, such as read-locking
  // the _entire_ cache (including contained per-session caches) when doing these operations.

  @Override
  public void invalidate( @NonNull IAuthorizationRequest request, @NonNull IAuthorizationOptions options ) {
    var key = createAuthorizationKey( request, options );
    copyCacheBySessionKey()
      .forEach( ( sessionKey, cacheData ) -> cacheData.invalidate( key ) );
  }

  @Override
  public void invalidateAll( @NonNull Predicate<IAuthorizationDecisionCacheKey> predicate ) {
    copyCacheBySessionKey()
      .forEach( ( sessionKey, cacheData ) -> cacheData.invalidateAll( predicate ) );
  }

  @Override
  public void invalidateAll() {
    sessionsLock.writeLock().lock();
    try {
      cacheBySessionKey.forEach( ( sessionId, cacheData ) -> cacheData.dispose() );
      cacheBySessionKey.clear();
    } finally {
      sessionsLock.writeLock().unlock();
    }
  }
  // endregion Authorization Request Invalidation

  /**
   * Creates a copy of the sessions cache.
   * <p>
   * The main use case is to avoid concurrent modification of the cache while iterating it.
   *
   * @return A copy of the sessions cache.
   */
  @NonNull
  private Map<String, SessionCacheData> copyCacheBySessionKey() {
    sessionsLock.readLock().lock();
    try {
      return new HashMap<>( cacheBySessionKey );
    } finally {
      sessionsLock.readLock().unlock();
    }
  }

  /**
   * Builds a consolidated {@link CacheStats} reflecting all session caches.
   * <p>
   * This builds collects stats of each session in a way that is safe for concurrent access, however, the stats may be
   * inconsistent with respect to each other, as they may be collected at different times without locking each session's
   * internal workings. This appears to be a good compromise given the rough requirement of being able to log stats for
   * monitoring and configuration fine-tuning purposes.
   *
   * @return The consolidated stats.
   */
  protected CacheStats getStats() {
    Map<String, SessionCacheData> sessionCacheMapCopy;
    CacheStats pastCacheStatsCopy;

    sessionsLock.readLock().lock();
    try {
      sessionCacheMapCopy = new HashMap<>( cacheBySessionKey );
      pastCacheStatsCopy = pastCacheStats;
    } finally {
      sessionsLock.readLock().unlock();
    }

    return sessionCacheMapCopy
      .values()
      .stream()
      .map( cacheData -> cacheData.cache.stats() )
      .reduce( pastCacheStatsCopy, CacheStats::plus );
  }

  @Override
  public String toString() {
    return String.format( "MemoryAuthorizationDecisionCache[stats=%s]", getStats() );
  }
}
