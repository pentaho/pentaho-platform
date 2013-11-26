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

package org.pentaho.platform.engine.core.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.messages.Messages;

/**
 * An {@code InheritableThreadLocal}-based implementation of {@link IPentahoSessionHolderStrategy}.
 * 
 * @author mlowery
 */
public class InheritableThreadLocalPentahoSessionHolderStrategy implements IPentahoSessionHolderStrategy {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( InheritableThreadLocalPentahoSessionHolderStrategy.class );

  // ~ Instance fields
  // =================================================================================================

  private static final ThreadLocal<IPentahoSession> perThreadSession = new InheritableThreadLocal<IPentahoSession>();

  // ~ Constructors
  // ====================================================================================================

  public InheritableThreadLocalPentahoSessionHolderStrategy() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * Sets an IPentahoSession for the current thread
   * 
   * @param session
   */
  public void setSession( IPentahoSession session ) {
    perThreadSession.set( session );
  }

  /**
   * Returns the IPentahoSession for the current thread
   * 
   * @return thread session
   */
  public IPentahoSession getSession() {
    IPentahoSession sess = perThreadSession.get();
    if ( sess == null ) {
      // In a perfect world, the platform should never be in a state where session is null, but we are not there
      // yet.
      // Not all places
      // that instance sessions use the PentahoSessionHolder yet, so we will not make a fuss here if session is
      // null.
      // When PentahoSessionHolder
      // is fully integrated with all sessions, then we should probably throw an exception here since in that case
      // a
      // null session means
      // the system is in an illegal state.
      logger.debug( Messages.getInstance().getString(
          "PentahoSessionHolder.WARN_THREAD_SESSION_NULL", Thread.currentThread().getName() ) ); //$NON-NLS-1$
    }
    return sess;
  }

  /**
   * Removes the IPentahoSession for the current thread. It is important that the framework calls this to prevent
   * session bleed- through between requests as threads are re-used by the server.
   */
  public void removeSession() {
    IPentahoSession sess = perThreadSession.get();

    if ( sess != null ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( Messages.getInstance().getString( "PentahoSessionHolder.DEBUG_REMOVING_SESSION", //$NON-NLS-1$
            Thread.currentThread().getName(), String.valueOf( Thread.currentThread().getId() ) ) );
      }
      if ( logger.isTraceEnabled() ) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        logger.trace( Messages.getInstance().getString( "PentahoSessionHolder.DEBUG_THREAD_STACK_TRACE" ) ); //$NON-NLS-1$
        for ( int i = 0; i < elements.length; i++ ) {
          logger.trace( elements[i] );
        }
      }

      // If the session is a custom/stand-alone session, we need to remove references
      // to it from other objects which may be holding on to it. We do this to prevent
      // memory leaks. In the future, this should not be necessary since objects
      // should not need to have setSesssion methods, but instead use PentahoSessionHolder.getSession()
      if ( sess instanceof StandaloneSession ) {
        if ( logger.isDebugEnabled() ) {
          logger.debug( Messages.getInstance().getString( "PentahoSessionHolder.DEBUG_DESTROY_STANDALONE_SESSION", //$NON-NLS-1$
              String.valueOf( sess.getId() ), sess.getName(), String.valueOf( Thread.currentThread().getId() ) ) );
        }
        ( (StandaloneSession) sess ).destroy();
      }

      perThreadSession.remove();
    }
  }

}
