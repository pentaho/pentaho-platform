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
      // Session will be null if getSession() fails.
      if ( session != null ) {
        releaseSession( session );
      }
    }
  }

  private void useSession( Session session ) {
    getUsageCount( session ).incrementAndGet();
  }

  private void releaseSession( Session session ) {
    getUsageCount( session ).decrementAndGet();
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
