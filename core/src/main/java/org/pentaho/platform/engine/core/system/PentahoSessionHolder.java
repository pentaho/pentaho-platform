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

  /**
   * Inheritable local strategy.
   */
  public static final String MODE_INHERITABLETHREADLOCAL = "MODE_INHERITABLETHREADLOCAL"; //$NON-NLS-1$

  /**
   * Global strategy.
   */
  public static final String MODE_GLOBAL = "MODE_GLOBAL"; //$NON-NLS-1$

  /**
   * Key for finding session holder strategy.
   */
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

  /**
   * Default constructor.
   */
  public PentahoSessionHolder() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * Sets an IPentahoSession for the current thread.
   * 
   * @param session Session to be set.
   */
  public static void setSession( IPentahoSession session ) {
    strategy.setSession( session );
  }

  /**
   * Returns the IPentahoSession for the current thread.
   * 
   * @return Returns the thread session.
   */
  public static IPentahoSession getSession() {
    return strategy.getSession();
  }

  /**
   * Removes the IPentahoSession for the current thread. It is important that the framework calls this to prevent
   * session "bleed-through" between requests as threads are reused by the server.
   */
  public static void removeSession() {
    strategy.removeSession();
  }

  /**
   * Initializes the session holder with the previously set strategy name. 
   */
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

  /**
   * Sets the behavior of the session.
   * Valid values are: {@code MODE_INHERITABLETHREADLOCAL} and {@code MODE_GLOBAL}.
   * The changes to the strategy are applied immediately.
   * 
   * @param strategyName Name of the strategy to be used.
   */
  public static void setStrategyName( final String strategyName ) {
    PentahoSessionHolder.strategyName = strategyName;
    initialize();
  }
}
