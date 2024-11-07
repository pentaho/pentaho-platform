/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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

  static final String USAGE_COUNT = "usage_count"; // attribute key for tracking session usages

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
   * Sessions from the cache will have a "usage_count" attribute set to track if still in use, to verify they can be
   * safely logged out on eviction. See
   * {@link PentahoJcrTemplate#execute(org.springframework.extensions.jcr.JcrCallback,
   * boolean)}
   */
  private LoadingCache<CacheKey, Session> sessionCache =
    CacheBuilder.newBuilder()
      .expireAfterAccess( cacheDuration, TimeUnit.SECONDS )
      .maximumSize( cacheSize )
      .removalListener( (RemovalListener<CacheKey, Session>) objectObjectRemovalNotification -> {
        Session session = objectObjectRemovalNotification.getValue();
        if ( sessionIsUnused( session ) ) {
          logger.debug( "Logging out cached session after eviction " + session );
          session.logout();
        } else {
          logger.warn( "Session has expired from cache, but still marked as in use.  May be orphaned.  " + session );
        }
      } ).recordStats()
      .build( new CacheLoader<CacheKey, Session>() {
        @Override public Session load( CacheKey credKey ) throws Exception {
          Session session = GuavaCachePoolPentahoJcrSessionFactory.super.getSession( credKey.creds );
          if ( session instanceof SessionImpl ) {
            ( (SessionImpl) session ).setAttribute( USAGE_COUNT, new AtomicInteger( 0 ) );
          } else {
            logger.warn( "Expected a Jackrabbit SessionImpl.  Will not be tracking usage." );
          }
          return session;
        }
      } );

  private boolean sessionIsUnused( Session session ) {
    return session.getAttribute( USAGE_COUNT ) instanceof AtomicInteger
      && ( (AtomicInteger) session.getAttribute( USAGE_COUNT ) ).get() == 0;
  }

  @Override public Session getSession( Credentials creds ) throws RepositoryException {


    // Aquire from cache
    Session session;

    if ( transactionManager == null || !transactionManager.isCreatingTransaction() ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Thread is not transacted, checking cache for session: " + creds );
      }
      try {
        CacheKey key = new CacheKey( creds );
        // find or create
        session = sessionCache.get( key );
        if ( !session.isLive() ) {
          if ( logger.isDebugEnabled() ) {
            logger.debug( "Cached session is not longer alive. disposing: " + creds );
          }
          sessionCache.invalidate( key );
          session = sessionCache.get( key );
        }

        if ( SessionFactoryUtils.isSessionThreadBound( session, credentialsStrategySessionFactory ) ) {
          if ( logger.isDebugEnabled() ) {
            logger.debug(
              "Session is bound to a transaction. This should never happen, ignoring this session and creating a new "
                +
                "session: "
                + creds );
          }
          sessionCache.invalidate( key );
          session = sessionCache.get( key );
        }

        session.refresh( false );

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
