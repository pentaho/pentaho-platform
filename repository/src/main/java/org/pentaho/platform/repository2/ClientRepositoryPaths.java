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


package org.pentaho.platform.repository2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IClientRepositoryPathsStrategy;

import java.lang.reflect.Constructor;

public class ClientRepositoryPaths {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( ClientRepositoryPaths.class );

  public static final String DEFAULT = "DEFAULT"; //$NON-NLS-1$

  public static final String SYSTEM_PROPERTY = "pentaho.repository.client.pathsStrategy"; //$NON-NLS-1$

  private static String strategyName = System.getProperty( SYSTEM_PROPERTY );

  private static IClientRepositoryPathsStrategy strategy;

  static {
    initialize();
  }

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  private ClientRepositoryPaths() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public static String getPublicFolderName() {
    return strategy.getPublicFolderName();
  }

  public static String getHomeFolderName() {
    return strategy.getHomeFolderName();
  }

  public static String getUserHomeFolderName( final String username ) {
    return strategy.getUserHomeFolderName( username );
  }

  public static String getPublicFolderPath() {
    return strategy.getPublicFolderPath();
  }

  public static String getHomeFolderPath() {
    return strategy.getHomeFolderPath();
  }

  public static String getUserHomeFolderPath( final String username ) {
    return strategy.getUserHomeFolderPath( username );
  }

  public static String getRootFolderPath() {
    return strategy.getRootFolderPath();
  }

  public static String getEtcFolderPath() {
    return strategy.getEtcFolderPath();
  }

  public static String getEtcFolderName() {
    return strategy.getEtcFolderName();
  }

  private static void initialize() {
    if ( ( strategyName == null ) || "".equals( strategyName ) ) { //$NON-NLS-1$
      strategyName = DEFAULT;
    }

    if ( strategyName.equals( DEFAULT ) ) {
      strategy = new DefaultClientRepositoryPathsStrategy();
    } else {
      // Try to load a custom strategy
      try {
        Class<?> clazz = Class.forName( strategyName );
        Constructor<?> customStrategy = clazz.getConstructor( new Class[] {} );
        strategy = (IClientRepositoryPathsStrategy) customStrategy.newInstance( new Object[] {} );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }

    logger.debug( "ClientRepositoryPaths initialized: strategy=" + strategyName ); //$NON-NLS-1$
  }

  public static void setStrategyName( final String strategyName ) {
    ClientRepositoryPaths.strategyName = strategyName;
    initialize();
  }
}
