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

  private CredentialsStrategySessionFactory credentialsStrategySessionFactory;
  private int cacheDuration = 300;
  private int cacheSize = 100;

  private Logger logger = LoggerFactory.getLogger( getClass() );
  private PentahoTransactionManager transactionManager;


  public GuavaCachePoolPentahoJcrSessionFactory( Repository repository, String workspace ) {
    this( repository, workspace, null );
  }

  public GuavaCachePoolPentahoJcrSessionFactory( Repository repository, String workspace,
                                                 PentahoTransactionManager transactionManager ) {
    super( repository, workspace );
    this.transactionManager = transactionManager;

    ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
    if ( systemConfig != null && systemConfig.getConfiguration( "repository" ) != null ) {
      try {
        this.cacheDuration =
          Integer.parseInt( systemConfig.getConfiguration( "repository" ).getProperties().getProperty(
            "cache-ttl", "300" ) );


        this.cacheSize =
          Integer.parseInt( systemConfig.getConfiguration( "repository" ).getProperties().getProperty(
            "cache-size", "100" ) );
      } catch ( IOException e ) {
        logger.info( "Could not find repository.cache-duration" );
      }
    }
  }

  /**
    * Session cache by credentials, partitioned by thread. Two threads obtaining sessions for the same credentials cannot
    * use the same Session.
    * <p>
    * Cached sessions use a lifecycle holder to coordinate active users with eviction. See
   * {@link PentahoJcrTemplate#execute(org.springframework.extensions.jcr.JcrCallback,
   * boolean)}
   */
  private LoadingCache<CacheKey, CachedJcrSession> sessionCache =
    CacheBuilder.newBuilder()
      .expireAfterAccess( cacheDuration, TimeUnit.SECONDS )
      .maximumSize( cacheSize )
      .removalListener( (RemovalListener<CacheKey, CachedJcrSession>) objectObjectRemovalNotification -> {
        CachedJcrSession cachedSession = objectObjectRemovalNotification.getValue();
        if ( cachedSession != null ) {
          logger.debug( "Retiring cached session after eviction " + cachedSession.getSession() );
          cachedSession.retire();
        }
      } ).recordStats()
      .build( new CacheLoader<CacheKey, CachedJcrSession>() {
        @Override public CachedJcrSession load( CacheKey credKey ) throws Exception {
          Session session = GuavaCachePoolPentahoJcrSessionFactory.super.getSession( credKey.creds );
          CachedJcrSession cachedSession = new CachedJcrSession( session );
          if ( session instanceof SessionImpl ) {
            ( (SessionImpl) session ).setAttribute( CachedJcrSession.SESSION_ATTRIBUTE, cachedSession );
            ( (SessionImpl) session ).setAttribute( USAGE_COUNT, new AtomicInteger( 0 ) );
          } else {
            logger.warn( "Expected a Jackrabbit SessionImpl.  Will not be tracking cached session lifecycle." );
          }
          return cachedSession;
        }
      } );

  @Override public Session getSession( Credentials creds ) throws RepositoryException {


    // Acquire from cache
    Session session;

    if ( transactionManager == null || !transactionManager.isCreatingTransaction() ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Thread is not transacted, checking cache for session: " + creds );
      }
      try {
        CacheKey key = new CacheKey( creds );
        while ( true ) {
          CachedJcrSession cachedSession = sessionCache.get( key );
          if ( !cachedSession.acquire() ) {
            sessionCache.asMap().remove( key, cachedSession );
            continue;
          }
          boolean keepLease = false;
          try {
            session = cachedSession.getSession();
            if ( !session.isLive() ) {
              if ( logger.isDebugEnabled() ) {
                logger.debug( "Cached session is no longer alive. disposing: " + creds );
              }
              sessionCache.asMap().remove( key, cachedSession );
              continue;
            }

            if ( SessionFactoryUtils.isSessionThreadBound( session, credentialsStrategySessionFactory ) ) {
              if ( logger.isDebugEnabled() ) {
                logger.debug(
                  "Session is bound to a transaction. This should never happen, ignoring this session and creating a "
                    + "new session: " + creds );
              }
              sessionCache.asMap().remove( key, cachedSession );
              continue;
            }

            session.refresh( false );

            // Increment the diagnostic count for this factory retrieval. The matching count and lifecycle lease are
            // released by the template after execution completes.
            Object usageCount = session.getAttribute( USAGE_COUNT );
            if ( usageCount instanceof AtomicInteger ) {
              ( (AtomicInteger) usageCount ).incrementAndGet();
            }
            keepLease = true;
            return session;
          } finally {
            if ( !keepLease ) {
              cachedSession.release();
            }
          }
        }
      } catch ( Exception e ) {
        logger.error( "Error obtaining session from cache. Creating one directly instead: " + creds, e );
        session = super.getSession( creds );
      }
    } else {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Thread is transacted, obtaining session directly, not cached: " + creds );
      }
      session = super.getSession( creds );
    }
    return session;
  }

  /**
   * Used by the sessionCache as a key for Jcr Sessions.
   */
  private class CacheKey {
    SimpleCredentials creds;
    Long threadId;

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

      if ( creds != null ? !creds.getUserID().equals( cacheKey.creds.getUserID() ) : cacheKey.creds != null ) {
        return false;
      }
      if ( threadId != null ? !threadId.equals( cacheKey.threadId ) : cacheKey.threadId != null ) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = creds != null ? creds.getUserID().hashCode() : 0;
      result = 31 * result + ( threadId != null ? threadId.hashCode() : 0 );
      return result;
    }
  }

}
