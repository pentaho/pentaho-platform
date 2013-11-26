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

import java.lang.reflect.Constructor;

/**
 * Stores the IPentahoSession session object for the current thread so that a web service bean can get to it
 * without requiring it to be passed to its methods.
 * 
 * <p>
 * Configure using system property {@code pentaho.sessionHolder.strategy} or {@link #setStrategyName(String)}.
 * Valid values are: {@code MODE_INHERITABLETHREADLOCAL} and {@code MODE_GLOBAL}.
 * </p>
 * 
 * <p>
 * Partially inspired by {@code org.springframework.security.context.SecurityContextHolder}.
 * </p>
 * 
 * @author jamesdixon
 * @author mlowery (modifications to support global)
 */
public class PentahoSessionHolder {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( PentahoSessionHolder.class );

  public static final String MODE_INHERITABLETHREADLOCAL = "MODE_INHERITABLETHREADLOCAL"; //$NON-NLS-1$

  public static final String MODE_GLOBAL = "MODE_GLOBAL"; //$NON-NLS-1$

  public static final String SYSTEM_PROPERTY = "pentaho.sessionHolder.strategy"; //$NON-NLS-1$

  private static String strategyName = System.getProperty( SYSTEM_PROPERTY );

  private static IPentahoSessionHolderStrategy strategy;

  static {
    initialize();
  }

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public PentahoSessionHolder() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * Sets an IPentahoSession for the current thread
   * 
   * @param session
   */
  public static void setSession( IPentahoSession session ) {
    strategy.setSession( session );
  }

  /**
   * Returns the IPentahoSession for the current thread
   * 
   * @return thread session
   */
  public static IPentahoSession getSession() {
    return strategy.getSession();
  }

  /**
   * Removes the IPentahoSession for the current thread. It is important that the framework calls this to prevent
   * session bleed- through between requests as threads are re-used by the server.
   */
  public static void removeSession() {
    strategy.removeSession();
  }

  private static void initialize() {
    if ( ( strategyName == null ) || "".equals( strategyName ) ) { //$NON-NLS-1$
      strategyName = MODE_INHERITABLETHREADLOCAL;
    }

    if ( strategyName.equals( MODE_INHERITABLETHREADLOCAL ) ) {
      strategy = new InheritableThreadLocalPentahoSessionHolderStrategy();
    } else if ( strategyName.equals( MODE_GLOBAL ) ) {
      strategy = new GlobalPentahoSessionHolderStrategy();
    } else {
      // Try to load a custom strategy
      try {
        Class clazz = Class.forName( strategyName );
        Constructor customStrategy = clazz.getConstructor( new Class[] {} );
        strategy = (IPentahoSessionHolderStrategy) customStrategy.newInstance( new Object[] {} );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }

    logger.debug( "PentahoSessionHolder initialized: strategy=" + strategyName );

  }

  public static void setStrategyName( final String strategyName ) {
    PentahoSessionHolder.strategyName = strategyName;
    initialize();
  }
}
