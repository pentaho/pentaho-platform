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
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoRequestContextHolderStrategy;
import org.pentaho.platform.engine.core.messages.Messages;

/**
 * An {@code InheritableThreadLocal}-based implementation of {@link IPentahoRequestContextHolderStrategy}.
 * 
 * @author rmansoor
 */
public class InheritableThreadLocalPentahoRequestContextHolderStrategy implements IPentahoRequestContextHolderStrategy {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog(
    InheritableThreadLocalPentahoRequestContextHolderStrategy.class );

  // ~ Instance fields
  // =================================================================================================

  private static final ThreadLocal<IPentahoRequestContext> perThreadRequestContext =
      new InheritableThreadLocal<IPentahoRequestContext>();

  // ~ Constructors
  // ====================================================================================================

  public InheritableThreadLocalPentahoRequestContextHolderStrategy() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * Sets an IPentahoRequestContext for the current thread
   * 
   * @param requestContext
   */
  public void setRequestContext( IPentahoRequestContext requestContext ) {
    perThreadRequestContext.set( requestContext );
  }

  /**
   * Returns the IPentahoRequestContext for the current thread
   * 
   * @return thread requestContext
   */
  public IPentahoRequestContext getRequestContext() {
    IPentahoRequestContext requestContext = perThreadRequestContext.get();
    if ( requestContext == null ) {
      logger.debug( Messages.getInstance().getString(
          "PentahoRequestContextHolder.WARN_THREAD_REQUEST_CONTEXT_NULL", Thread.currentThread().getName() ) ); //$NON-NLS-1$
    }
    return requestContext;
  }

  /**
   * Removes the IPentahoRequestContext for the current thread. It is important that the framework calls this to
   * prevent request context bleed- through between requests as threads are re-used by the server.
   */
  public void removeRequestContext() {
    IPentahoRequestContext requestContext = perThreadRequestContext.get();

    if ( requestContext != null ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( Messages.getInstance().getString( "PentahoRequestContextHolder.DEBUG_REMOVING_REQUEST_CONTEXT", //$NON-NLS-1$
            Thread.currentThread().getName(), String.valueOf( Thread.currentThread().getId() ) ) );
      }
      if ( logger.isTraceEnabled() ) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        logger.trace( Messages.getInstance().getString( "PentahoRequestContextHolder.DEBUG_THREAD_STACK_TRACE" ) ); //$NON-NLS-1$
        for ( int i = 0; i < elements.length; i++ ) {
          logger.trace( elements[i] );
        }
      }
      perThreadRequestContext.remove();
    }
  }

}
