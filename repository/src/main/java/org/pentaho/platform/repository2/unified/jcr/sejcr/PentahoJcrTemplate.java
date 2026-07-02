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

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.security.AccessControlException;
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
    try {
      session = getSession();
      useSession( session );

      Session sessionToExpose = ( exposeNativeSession ? session : createSessionProxy( session ) );
      Object result = action.doInJcr( sessionToExpose );
      // TODO: does flushing (session.refresh) should work here?
      // flushIfNecessary(session, existingTransaction);
      return result;
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
      // Guard against a null session (e.g. getSession() threw): releaseSession() delegates to
      // getUsageCount() which requires a non-null session and would otherwise throw here,
      // masking the original exception from getSession()/doInJcr().
      if ( session != null ) {
        releaseSession( session );
        decrementFactoryProtection( session );
      }
    }
  }

  private void useSession( Session session ) {
    int newCount = getUsageCount( session ).incrementAndGet();
    if ( LOG.isDebugEnabled() ) {
      LOG.debug( "[JCR-TEMPLATE-USE] Thread=" + Thread.currentThread().getName()
        + " SessionId=" + System.identityHashCode( session )
        + " RefCount=" + newCount
        + " (template acquisition)" );
    }
  }

  private void releaseSession( Session session ) {
    int newCount = getUsageCount( session ).decrementAndGet();
    if ( LOG.isDebugEnabled() ) {
      LOG.debug( "[JCR-TEMPLATE-RELEASE] Thread=" + Thread.currentThread().getName()
        + " SessionId=" + System.identityHashCode( session )
        + " RefCount=" + newCount
        + " (template release)" );
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
          LOG.debug( "[JCR-FACTORY-RELEASE] Thread=" + Thread.currentThread().getName()
            + " SessionId=" + System.identityHashCode( session )
            + " RefCount=" + count
            + " (factory protection release" + ( count == 0 ? " - SESSION SAFE FOR EVICTION" : "" ) + ")" );
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

  /**
   * Cached Sessions retrieved from {@link GuavaCachePoolPentahoJcrSessionFactory}
   * will have a "usage_count" attribute indicating whether the session is
   * currently in use.  This allows safe eviction.
   */
  private AtomicInteger getUsageCount( Session session ) {
    Objects.requireNonNull( session );
    Object usageCount = session.getAttribute( USAGE_COUNT );
    if ( usageCount instanceof AtomicInteger ) {
      return (AtomicInteger) usageCount;
    } else {
      LOG.debug( "No usage count associated with session " + session
        + "\nThis can safely happen with uncached sessions. " );
      return new AtomicInteger( 0 );
    }
  }

  @Override
  protected Session getSession() {
    return SessionFactoryUtils.getSession( getSessionFactory(), this.isAllowCreate() );
  }

  private RuntimeException pentahoConvertJcrAccessException( final RuntimeException ex ) {
    if ( ex instanceof AccessControlException ) {
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
