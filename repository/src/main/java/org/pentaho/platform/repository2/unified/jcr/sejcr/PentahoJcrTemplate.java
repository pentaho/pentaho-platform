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

import org.pentaho.platform.repository2.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactoryUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT;

/**
 * Copy of superclass' execute with better exception conversions.
 *
 * @author mlowery
 */
public class PentahoJcrTemplate extends JcrTemplate {
  private static final Logger LOG = LoggerFactory.getLogger( PentahoJcrTemplate.class );

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public PentahoJcrTemplate() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * Copy of superclass' execute with pentahoConvertJcrAccessException substitutions.
   */
  @Override
  public Object execute( JcrCallback action, boolean exposeNativeSession ) throws DataAccessException {

    Session session = null;
    CachedJcrSession cachedSession = null;
    boolean releaseFactoryProtection = true;
    boolean cachedSessionLeaseAcquired = false;
    boolean sessionUsageStarted = false;
    try {
      session = getSession();
      Object attribute = session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE );
      if ( attribute instanceof CachedJcrSession ) {
        cachedSession = (CachedJcrSession) attribute;
        if ( !cachedSession.consumeFactoryLease() ) {
          releaseFactoryProtection = false;
          if ( !cachedSession.acquireForBoundUse() ) {
            throw new IllegalStateException( "Cannot use a cached JCR session after its final lease was released" );
          }
        }
        cachedSessionLeaseAcquired = true;
        retainSpringBinding( session, cachedSession );
      }
      useSession( session );
      sessionUsageStarted = true;

      Session sessionToExpose = ( exposeNativeSession ? session : createSessionProxy( session ) );
      return action.doInJcr( sessionToExpose );
    } catch ( RepositoryException ex ) {
      throw pentahoConvertJcrAccessException( ex );
      // IOException are not converted here
    } catch ( IOException ex ) {
      // use method to decouple the static call
      throw convertJcrAccessException( ex );
    } catch ( RuntimeException ex ) {
      // Callback code threw application exception...
      throw pentahoConvertJcrAccessException( ex );
    } finally {
      releaseAfterExecution( session, cachedSession, cachedSessionLeaseAcquired,
        sessionUsageStarted, releaseFactoryProtection );
    }
  }

  private void releaseAfterExecution( Session session, CachedJcrSession cachedSession,
                                      boolean cachedSessionLeaseAcquired, boolean sessionUsageStarted,
                                      boolean releaseFactoryProtection ) {
    // Acquisition can fail before a session exists. Cleanup must not mask that original exception.
    if ( session == null ) {
      return;
    }
    try {
      if ( sessionUsageStarted ) {
        releaseSession( session );
      }
      if ( releaseFactoryProtection ) {
        decrementFactoryProtection( session );
      }
    } finally {
      if ( cachedSessionLeaseAcquired ) {
        // Retain the holder locally: callback cleanup can make session attributes unavailable.
        cachedSession.release();
      } else if ( cachedSession == null ) {
        releaseUncachedSession( session );
      }
    }
  }

  private void useSession( Session session ) {
    int newCount = getUsageCount( session ).incrementAndGet();
    if ( LOG.isDebugEnabled() ) {
      LOG.debug( "[JCR-TEMPLATE-USE] Thread={} SessionId={} RefCount={} (template acquisition)",
        Thread.currentThread().getName(), System.identityHashCode( session ), newCount );
    }
  }

  private void retainSpringBinding( Session session, CachedJcrSession cachedSession ) {
    if ( !TransactionSynchronizationManager.isSynchronizationActive()
        || !SessionFactoryUtils.isSessionThreadBound( session, getSessionFactory() ) ) {
      return;
    }
    for ( TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations() ) {
      if ( synchronization instanceof CachedSessionSynchronization
          && ( (CachedSessionSynchronization) synchronization ).cachedSession == cachedSession ) {
        return;
      }
    }
    if ( !cachedSession.acquireForBoundUse() ) {
      throw new IllegalStateException( "Cannot protect a Spring-bound JCR session after its final lease was released" );
    }
    try {
      // Both SessionFactoryUtils and LocalTransactionManager can bind sessions. Preserve either owner's lifetime,
      // including suspended synchronizations, without a process-wide registry or one listener per nested callback.
      TransactionSynchronizationManager.registerSynchronization( new CachedSessionSynchronization( cachedSession ) );
    } catch ( RuntimeException e ) {
      cachedSession.release();
      throw e;
    }
  }

  private static final class CachedSessionSynchronization implements TransactionSynchronization {
    private final CachedJcrSession cachedSession;

    private CachedSessionSynchronization( CachedJcrSession cachedSession ) {
      this.cachedSession = cachedSession;
    }

    @Override
    public void afterCompletion( int status ) {
      cachedSession.release();
    }
  }

  private void releaseSession( Session session ) {
    try {
      int newCount = getUsageCount( session ).decrementAndGet();
      if ( LOG.isDebugEnabled() ) {
        LOG.debug( "[JCR-TEMPLATE-RELEASE] Thread={} SessionId={} RefCount={} (template release)",
          Thread.currentThread().getName(), System.identityHashCode( session ), newCount );
      }
    } catch ( Exception e ) {
      // Cleanup must continue so the cached-session lease is released even if the diagnostic counter is unavailable.
      LOG.warn( "Could not release usage count for sessionId=" + System.identityHashCode( session ), e );
    }
  }

  /**
   * Decrement the factory's protection increment that was applied during session retrieval.
   * The factory increments the usage count to protect the session from eviction while it's
   * being transferred; this method releases that protection once the session is returned to cache.
   */
  private void decrementFactoryProtection( Session session ) {
    try {
      Object usageCount = session.getAttribute( USAGE_COUNT );
      if ( usageCount instanceof AtomicInteger ) {
        int count = ( (AtomicInteger) usageCount ).decrementAndGet();
        if ( LOG.isDebugEnabled() ) {
          LOG.debug( "[JCR-FACTORY-RELEASE] Thread={} SessionId={} RefCount={} (factory protection release{})",
            Thread.currentThread().getName(), System.identityHashCode( session ), count,
            count == 0 ? " - SESSION SAFE FOR EVICTION" : "" );
        }
        if ( count < 0 ) {
          LOG.warn( "[JCR-REFCOUNT-ERROR] Usage count went negative; factory protection decrement imbalance:"
            + " SessionId=" + System.identityHashCode( session )
            + " RefCount=" + count
            + " Session=" + session );
        }
      }
    } catch ( Exception e ) {
      // Do not throw from the finally block (that would mask the original exception), but log at WARN
      // with the full exception: failing to release factory protection can leave a cached session
      // permanently non-evictable, and the stack trace helps diagnose intermittent refcount issues.
      LOG.warn( "Could not decrement factory protection for sessionId=" + System.identityHashCode( session ), e );
    }
  }

  private void releaseUncachedSession( Session session ) {
    try {
      SessionFactoryUtils.releaseSession( session, getSessionFactory() );
    } catch ( Exception e ) {
      // Do not let cleanup failures mask the result or exception from the JCR callback.
      LOG.warn( "Could not release uncached JCR session {}", System.identityHashCode( session ), e );
    }
  }

  /**
  * Cached Sessions retrieved from {@link GuavaCachePoolPentahoJcrSessionFactory}
  * have a lifecycle lease that prevents eviction from logging out an active session. The usage counter is retained for
  * diagnostics; the lifecycle holder controls deferred logout.
   */
  private AtomicInteger getUsageCount( Session session ) {
    Objects.requireNonNull( session );
    Object usageCount = session.getAttribute( USAGE_COUNT );
    if ( usageCount instanceof AtomicInteger ) {
      return (AtomicInteger) usageCount;
    } else {
      LOG.debug( "No usage count associated with uncached session {}", session );
      return new AtomicInteger( 0 );
    }
  }

  @Override
  protected Session getSession() {
    return SessionFactoryUtils.getSession( getSessionFactory(), this.isAllowCreate() );
  }

  private RuntimeException pentahoConvertJcrAccessException( final RuntimeException ex ) {
    if ( ex instanceof SecurityException ) {
      return new org.springframework.security.access.AccessDeniedException( Messages.getInstance().getString(
        "PentahoJcrTemplate.ERROR_0001_ACCESS_DENIED" ), ex ); //$NON-NLS-1$
    } else {
      return super.convertJcrAccessException( ex );
    }
  }

  private RuntimeException pentahoConvertJcrAccessException( final RepositoryException ex ) {
    if ( ex instanceof AccessDeniedException ) {
      return new org.springframework.security.access.AccessDeniedException( Messages.getInstance().getString(
        "PentahoJcrTemplate.ERROR_0001_ACCESS_DENIED" ), ex ); //$NON-NLS-1$
    } else {
      return super.convertJcrAccessException( ex );
    }
  }

}
