/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.repository2.unified;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;

import java.lang.reflect.Constructor;

/**
 * Class of static methods that return commonly needed absolute paths like "tenant root folder path."
 * 
 * @author mlowery
 */
public class ServerRepositoryPaths {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( ServerRepositoryPaths.class );

  public static final String DEFAULT = "DEFAULT"; //$NON-NLS-1$

  public static final String SYSTEM_PROPERTY = "pentaho.repository.server.pathsStrategy"; //$NON-NLS-1$

  private static String strategyName = System.getProperty( SYSTEM_PROPERTY );

  private static IServerRepositoryPathsStrategy strategy;

  static {
    initialize();
  }

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  private ServerRepositoryPaths() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public static String getPentahoRootFolderPath() {
    return strategy.getPentahoRootFolderPath();
  }

  public static String getTenantHomeFolderPath( final ITenant tenant ) {
    return strategy.getTenantHomeFolderPath( tenant );
  }

  public static String getTenantPublicFolderPath( final ITenant tenant ) {
    return strategy.getTenantPublicFolderPath( tenant );
  }

  public static String getTenantRootFolderPath( final ITenant tenant ) {
    return strategy.getTenantRootFolderPath( tenant );
  }

  public static String getUserHomeFolderPath( ITenant tenant, String username ) {
    return strategy.getUserHomeFolderPath( tenant, username );
  }

  /**
   * Returns the tenant id given an absolute path.
   * 
   * @param absPath
   *          absolute path which will be parsed to determine the tenant id
   * @return tenant id within the path
   */
  public static String getTenantId( final String absPath ) {
    return strategy.getTenantId( absPath );
  }

  public static String getTenantEtcFolderPath( final ITenant tenant ) {
    return strategy.getTenantEtcFolderPath( tenant );
  }

  public static String getTenantHomeFolderPath() {
    return getTenantHomeFolderPath( JcrTenantUtils.getTenant() );
  }

  public static String getTenantPublicFolderPath() {
    return getTenantPublicFolderPath( JcrTenantUtils.getTenant() );
  }

  public static String getTenantRootFolderPath() {
    return getTenantRootFolderPath( JcrTenantUtils.getTenant() );
  }

  public static String getTenantEtcFolderPath() {
    return getTenantEtcFolderPath( JcrTenantUtils.getTenant() );
  }

  public static String getTenantHomeFolderName() {
    return strategy.getTenantHomeFolderName();
  }

  public static String getTenantPublicFolderName() {
    return strategy.getTenantPublicFolderName();
  }

  public static String getPentahoRootFolderName() {
    return strategy.getPentahoRootFolderName();
  }

  public static String getTenantEtcFolderName() {
    return strategy.getTenantEtcFolderName();
  }

  private static void initialize() {
    if ( ( strategyName == null ) || "".equals( strategyName ) ) { //$NON-NLS-1$
      strategyName = DEFAULT;
    }

    if ( strategyName.equals( DEFAULT ) ) {
      strategy = new DefaultServerRepositoryPathsStrategy();
    } else {
      // Try to load a custom strategy
      try {
        Class<?> clazz = Class.forName( strategyName );
        Constructor<?> customStrategy = clazz.getConstructor( new Class[] {} );
        strategy = (IServerRepositoryPathsStrategy) customStrategy.newInstance( new Object[] {} );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }

    logger.debug( "ServerRepositoryPaths initialized: strategy=" + strategyName ); //$NON-NLS-1$
  }

  public static void setStrategyName( final String strategyName ) {
    ServerRepositoryPaths.strategyName = strategyName;
    initialize();
  }

  /**
   * Interface that allows pathing to be configurable.
   */
  public static interface IServerRepositoryPathsStrategy {
    String getPentahoRootFolderPath();

    String getTenantHomeFolderPath( final ITenant tenant );

    String getTenantPublicFolderPath( final ITenant tenant );

    String getTenantRootFolderPath( final ITenant tenant );

    String getUserHomeFolderPath( ITenant tenant, final String username );

    String getTenantEtcFolderPath( final ITenant tenant );

    String getTenantHomeFolderName();

    String getTenantPublicFolderName();

    String getPentahoRootFolderName();

    String getTenantEtcFolderName();

    String getTenantId( final String absPath );
  }

}
