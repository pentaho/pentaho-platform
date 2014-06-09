/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr.sejcr;

import java.io.IOException;
import java.security.AccessControlException;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.repository2.messages.Messages;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactoryUtils;

/**
 * Copy of superclass' execute with better exception conversions.
 * 
 * @author mlowery
 */
public class PentahoJcrTemplate extends JcrTemplate {

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
    Session session = getSession();
    boolean existingTransaction = SessionFactoryUtils.isSessionThreadBound( session, getSessionFactory() );

    try {
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
//      if ( !existingTransaction ) {
//        SessionFactoryUtils.releaseSession( session, getSessionFactory() );
//      }
    }
  }

  @Override
  protected Session getSession() {
    return SessionFactoryUtils.getSession(getSessionFactory(), this.isAllowCreate());
  }

  private RuntimeException pentahoConvertJcrAccessException( final RuntimeException ex ) {
    if ( ex instanceof AccessControlException ) {
      return new org.springframework.security.AccessDeniedException( Messages.getInstance().getString(
          "PentahoJcrTemplate.ERROR_0001_ACCESS_DENIED" ), ex ); //$NON-NLS-1$
    } else {
      return super.convertJcrAccessException( ex );
    }
  }

  private RuntimeException pentahoConvertJcrAccessException( final RepositoryException ex ) {
    if ( ex instanceof AccessDeniedException ) {
      return new org.springframework.security.AccessDeniedException( Messages.getInstance().getString(
          "PentahoJcrTemplate.ERROR_0001_ACCESS_DENIED" ), ex ); //$NON-NLS-1$
    } else {
      return super.convertJcrAccessException( ex );
    }
  }

}
