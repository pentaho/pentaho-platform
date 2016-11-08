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

import java.lang.reflect.Constructor;

/**
 * Stores the IPentahoRequestContext object for the current thread so that a web service bean can get to it without
 * requiring it to be passed to its methods.
 * 
 * <p>
 * Configure using system property {@code pentaho.requuestContextHolder.strategy} or
 * {@link #setStrategyName(String)}. Valid values are: {@code MODE_INHERITABLETHREADLOCAL} and {@code MODE_GLOBAL}.
 * </p>
 * 
 * <p>
 * Partially inspired by {@code org.springframework.security.context.SecurityContextHolder}.
 * </p>
 * 
 * @author Ramaiz Mansoor
 **/
public class PentahoRequestContextHolder {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( PentahoRequestContextHolder.class );

  public static final String MODE_INHERITABLETHREADLOCAL = "MODE_INHERITABLETHREADLOCAL"; //$NON-NLS-1$

  public static final String MODE_GLOBAL = "MODE_GLOBAL"; //$NON-NLS-1$

  public static final String SYSTEM_PROPERTY = "pentaho.requestContextHolder.strategy"; //$NON-NLS-1$

  private static String strategyName = System.getProperty( SYSTEM_PROPERTY );

  private static IPentahoRequestContextHolderStrategy strategy;

  static {
    initialize();
  }

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public PentahoRequestContextHolder() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * Sets an IPentahoRequestContext for the current thread
   * 
   * @param requestContext
   */
  public static void setRequestContext( IPentahoRequestContext requestContext ) {
    strategy.setRequestContext( requestContext );
  }

  /**
   * Returns the IPentahoRequestContext for the current thread
   * 
   * @return thread requestContext
   */
  public static IPentahoRequestContext getRequestContext() {
    if ( strategy != null && strategy.getRequestContext() != null ) {
      return strategy.getRequestContext();
    } else {
      if ( PentahoSystem.getInitializedOK() ) {
        return new BasePentahoRequestContext( PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() );
      } else {
        try{
          logger.debug( "Something went wrong. Trying to proceed. System is in status " + PentahoSystem.getInitializedStatus() );
          if( PentahoSystem.getApplicationContext() != null && PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() != null ){
            return new BasePentahoRequestContext( PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() );
          }
        }
        catch(Exception ex){
          logger.debug( "Restore attempt failed ", ex );
        }
        return null;
      }
    }
  }

  /**
   * Removes the IPentahoRequestContext for the current thread. It is important that the framework calls this to
   * prevent RequestContext bleed- through between requests as threads are re-used by the server.
   */
  public static void removeRequestContext() {
    strategy.removeRequestContext();
  }

  private static void initialize() {
    if ( ( strategyName == null ) || "".equals( strategyName ) ) { //$NON-NLS-1$
      strategyName = MODE_INHERITABLETHREADLOCAL;
    }

    if ( strategyName.equals( MODE_INHERITABLETHREADLOCAL ) ) {
      strategy = new InheritableThreadLocalPentahoRequestContextHolderStrategy();
    } else if ( strategyName.equals( MODE_GLOBAL ) ) {
      strategy = new GlobalPentahoRequestContextHolderStrategy();
    } else {
      // Try to load a custom strategy
      try {
        Class clazz = Class.forName( strategyName );
        Constructor customStrategy = clazz.getConstructor( new Class[] {} );
        strategy = (IPentahoRequestContextHolderStrategy) customStrategy.newInstance( new Object[] {} );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }

    logger.debug( "PentahoRequestContextHolder initialized: strategy=" + strategyName );

  }

  public static void setStrategyName( final String strategyName ) {
    PentahoRequestContextHolder.strategyName = strategyName;
    initialize();
  }
}
