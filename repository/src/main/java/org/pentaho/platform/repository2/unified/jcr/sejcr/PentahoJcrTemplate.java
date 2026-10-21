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
 * JCR template that applies Pentaho exception translation and coordinates session usage with cache eviction.
 * Cached-session leases are released after callback use and, for Spring-bound sessions, only after transaction
 * completion; this prevents eviction from logging out a session still owned by an active callback or transaction.
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
    * Executes a JCR callback while retaining the session's cache lifecycle lease for the full use interval.
    * Factory handoff protection is consumed separately from callback use; Spring-bound sessions receive a
    * transaction synchronization that retains them through {@code afterCompletion}. Cleanup preserves callback or
    * acquisition failures and applies Pentaho-specific access-denied translation.
    *
    * @param action callback to execute with the acquired JCR session
    * @param exposeNativeSession whether to expose the underlying session rather than a logout-suppressing proxy
    * @return the callback result
    * @throws DataAccessException if session acquisition or callback execution fails
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

  /**
   * Releases callback accounting and factory protection, then releases the lifecycle lease or direct session.
   * A missing session means acquisition failed and there is nothing to clean up. Cleanup failures are contained by
   * the delegated release methods so they cannot replace a callback failure.
   *
   * @param session session returned by the factory, or {@code null} if acquisition failed
   * @param cachedSession lifecycle holder, or {@code null} for an untracked session
   * @param cachedSessionLeaseAcquired whether this execution owns a holder lease
   * @param sessionUsageStarted whether callback usage accounting was successfully started
   * @param releaseFactoryProtection whether the factory's diagnostic protection count must be decremented
   */
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

  /** Starts callback usage accounting for the acquired session. */
  private void useSession( Session session ) {
    int newCount = getUsageCount( session ).incrementAndGet();
    if ( LOG.isDebugEnabled() ) {
      LOG.debug( "[JCR-TEMPLATE-USE] Thread={} SessionId={} RefCount={} (template acquisition)",
        Thread.currentThread().getName(), System.identityHashCode( session ), newCount );
    }
  }

  /**
   * Adds one lifecycle lease for the active Spring binding unless that holder already has a synchronization in the
   * current synchronization scope. The synchronization releases the lease after transaction completion.
   *
   * @param session session whose Spring binding is being checked
   * @param cachedSession holder to retain for the binding lifetime
   * @throws IllegalStateException if the holder can no longer be retained
   */
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

  /** Releases one transaction-scoped lease when Spring finishes the synchronization lifecycle. */
  private static final class CachedSessionSynchronization implements TransactionSynchronization {
    private final CachedJcrSession cachedSession;

    /** Creates a synchronization that owns one lease on the supplied cached session. */
    private CachedSessionSynchronization( CachedJcrSession cachedSession ) {
      this.cachedSession = cachedSession;
    }

    /** Releases the transaction's retained lease after commit or rollback. */
    @Override
    public void afterCompletion( int status ) {
      cachedSession.release();
    }
  }

  /** Decrements diagnostic callback usage; logs failures and continues lifecycle cleanup. */
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
          LOG.error( "[JCR-REFCOUNT-ERROR] Usage count went negative; factory protection decrement imbalance:"
              + " SessionId={} RefCount={}", System.identityHashCode( session ), count,
            new IllegalStateException( "usage count underflow" ) );
        }
      }
    } catch ( Exception e ) {
      // Do not throw from the finally block (that would mask the original exception), but log at WARN
      // with the full exception: failing to release factory protection can leave a cached session
      // permanently non-evictable, and the stack trace helps diagnose intermittent refcount issues.
      LOG.warn( "Could not decrement factory protection for sessionId=" + System.identityHashCode( session ), e );
    }
  }

  /** Releases a direct-login session unless Spring still owns it, without masking callback outcomes. */
  private void releaseUncachedSession( Session session ) {
    try {
      SessionFactoryUtils.releaseSession( session, getSessionFactory() );
    } catch ( Exception e ) {
      // Do not let cleanup failures mask the result or exception from the JCR callback.
      LOG.warn( "Could not release uncached JCR session {}", System.identityHashCode( session ), e );
    }
  }

  /**
   * Returns the diagnostic usage counter attached to a session.
   * Cached sessions use the counter for factory/template diagnostics while {@link CachedJcrSession} owns deferred
   * logout; sessions without the attribute receive a temporary counter so template execution remains safe.
   *
   * @param session session whose counter is requested
   * @return its attached counter, or a new zero-valued counter when the attribute is absent
   * @throws NullPointerException if {@code session} is {@code null}
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

  /**
   * Applies Pentaho's access-denied translation to callback runtime failures, including generic security failures.
   *
   * @param ex runtime failure raised by callback execution
   * @return an access-denied exception for security failures, otherwise the superclass translation
   */
  private RuntimeException pentahoConvertJcrAccessException( final RuntimeException ex ) {
    if ( ex instanceof SecurityException ) {
      return new org.springframework.security.access.AccessDeniedException( Messages.getInstance().getString(
        "PentahoJcrTemplate.ERROR_0001_ACCESS_DENIED" ), ex ); //$NON-NLS-1$
    } else {
      return super.convertJcrAccessException( ex );
    }
  }

  /**
   * Applies Pentaho's access-denied translation to JCR access-denied failures.
   *
   * @param ex repository failure raised during JCR access
   * @return an access-denied exception for JCR access denial, otherwise the superclass translation
   */
  private RuntimeException pentahoConvertJcrAccessException( final RepositoryException ex ) {
    if ( ex instanceof AccessDeniedException ) {
      return new org.springframework.security.access.AccessDeniedException( Messages.getInstance().getString(
        "PentahoJcrTemplate.ERROR_0001_ACCESS_DENIED" ), ex ); //$NON-NLS-1$
    } else {
      return super.convertJcrAccessException( ex );
    }
  }

}
