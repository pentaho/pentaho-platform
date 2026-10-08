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
import org.springframework.extensions.jcr.SessionFactoryUtils;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JCR Session Factory which caches Sessions by Credentials per Thread. The size of the cache and TTL of the entries can
 * be configured with repository.spring.properties
 * <p>
 * Created by nbaker on 6/9/14.
 */
class GuavaCachePoolPentahoJcrSessionFactory extends NoCachePentahoJcrSessionFactory
  implements PentahoJcrSessionFactory {

  static final String USAGE_COUNT = "usage_count"; // diagnostic count of session usages
  private static final String REPOSITORY_CONFIG_ID = "repository";
  private static final int MAX_CACHE_ACQUISITION_ATTEMPTS = 3;

  private CredentialsStrategySessionFactory credentialsStrategySessionFactory;
  private int cacheDuration = 300;
  private int cacheSize = 100;

  private Logger logger = LoggerFactory.getLogger( getClass() );
  private PentahoTransactionManager transactionManager;
  private final LoadingCache<CacheKey, CachedJcrSession> sessionCache;


  public GuavaCachePoolPentahoJcrSessionFactory( Repository repository, String workspace ) {
    this( repository, workspace, null );
  }

  public GuavaCachePoolPentahoJcrSessionFactory( Repository repository, String workspace,
                                                 PentahoTransactionManager transactionManager ) {
    super( repository, workspace );
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
    * Session cache by credentials, partitioned by thread. Two threads obtaining sessions for the same credentials cannot
    * use the same Session.
    * <p>
    * Cached sessions use a lifecycle holder to coordinate active users with eviction. See
   * {@link PentahoJcrTemplate#execute(org.springframework.extensions.jcr.JcrCallback,
   * boolean)}
   */
  private LoadingCache<CacheKey, CachedJcrSession> createSessionCache() {
    return CacheBuilder.newBuilder()
      .expireAfterAccess( cacheDuration, TimeUnit.SECONDS )
      .maximumSize( cacheSize )
      .removalListener( (RemovalListener<CacheKey, CachedJcrSession>) objectObjectRemovalNotification -> {
        CachedJcrSession cachedSession = objectObjectRemovalNotification.getValue();
        if ( cachedSession != null ) {
          logger.debug( "Retiring cached session after eviction {}", cachedSession.getSession() );
          cachedSession.retire();
        }
      } ).recordStats()
      .build( new CacheLoader<CacheKey, CachedJcrSession>() {
        @Override public CachedJcrSession load( CacheKey credKey ) throws Exception {
          Session session = GuavaCachePoolPentahoJcrSessionFactory.super.getSession( credKey.creds );
          return trackSession( session );
        }
      } );
  }

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

  @Override public Session getSession( Credentials creds ) throws RepositoryException {
    if ( isCreatingTransaction() ) {
      logger.debug( "Thread is transacted, obtaining session directly, not cached: {}", creds );
      return super.getSession( creds );
    }
    if ( cacheSize == 0 || cacheDuration == 0 ) {
      return getUncachedSession( creds );
    }

    logger.debug( "Thread is not transacted, checking cache for session: {}", creds );
    try {
      Session session = getCachedSession( creds );
      if ( session != null ) {
        return session;
      }
    } catch ( Exception e ) {
      logger.error( "Error obtaining session from cache. Creating one directly instead: {}", creds, e );
    }
    return getUncachedSession( creds );
  }

  private boolean isCreatingTransaction() {
    return transactionManager != null && transactionManager.isCreatingTransaction();
  }

  private Session getCachedSession( Credentials creds ) throws Exception {
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

  private Session acquireCachedSession( CacheKey key, CachedJcrSession cachedSession, Credentials creds )
      throws Exception {
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

      if ( SessionFactoryUtils.isSessionThreadBound( session, credentialsStrategySessionFactory ) ) {
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

  void invalidateCachedSessions() {
    sessionCache.invalidateAll();
    sessionCache.cleanUp();
  }

  /**
   * Wires the owning {@link SessionFactory} used as the key for
   * {@code SessionFactoryUtils.isSessionThreadBound(Session, SessionFactory)}, so a cached session that is
   * legitimately bound to an active transaction elsewhere is never handed out again from the cache.
   */
  void setCredentialsStrategySessionFactory( CredentialsStrategySessionFactory credentialsStrategySessionFactory ) {
    this.credentialsStrategySessionFactory = credentialsStrategySessionFactory;
  }

  /**
   * Used by the sessionCache as a key for Jcr Sessions.
   */
  private static final class CacheKey {
    private final SimpleCredentials creds;
    private final long threadId;

    private CacheKey( Credentials creds ) {
      this.creds = (SimpleCredentials) creds;
      this.threadId = Thread.currentThread().getId();
    }

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

    @Override
    public int hashCode() {
      int result = creds != null ? creds.getUserID().hashCode() : 0;
      result = 31 * result + Long.hashCode( threadId );
      return result;
    }
  }

}
