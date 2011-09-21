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
 */
package org.pentaho.platform.repository2.unified;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.springframework.util.Assert;

/**
 * Class of static methods that return commonly needed absolute paths like "tenant root folder path."
 * 
 * @author mlowery
 */
public class ServerRepositoryPaths {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(ServerRepositoryPaths.class);

  public static final String DEFAULT = "DEFAULT"; //$NON-NLS-1$

  public static final String SYSTEM_PROPERTY = "pentaho.repository.server.pathsStrategy"; //$NON-NLS-1$

  private static String strategyName = System.getProperty(SYSTEM_PROPERTY);

  private static IServerRepositoryPathsStrategy strategy;

  static {
    initialize();
  }

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  private ServerRepositoryPaths() {
    super();
  }

  // ~ Methods =========================================================================================================

  public static String getPentahoRootFolderPath() {
    return strategy.getPentahoRootFolderPath();
  }

  public static String getTenantHomeFolderPath(final String tenantId) {
    return strategy.getTenantHomeFolderPath(tenantId);
  }

  public static String getTenantPublicFolderPath(final String tenantId) {
    return strategy.getTenantPublicFolderPath(tenantId);
  }

  public static String getTenantRootFolderPath(final String tenantId) {
    return strategy.getTenantRootFolderPath(tenantId);
  }

  public static String getUserHomeFolderPath(final String tenantId, final String username) {
    return strategy.getUserHomeFolderPath(tenantId, username);
  }

  /**
   * Returns the tenant id given an absolute path.
   * 
   * @param absPath absolute path which will be parsed to determine the tenant id
   * @return tenant id within the path
   */
  public static String getTenantId(final String absPath) {
    return strategy.getTenantId(absPath);
  }

  public static String getTenantEtcFolderPath(final String tenantId) {
    return strategy.getTenantEtcFolderPath(tenantId);
  }

  public static String getTenantHomeFolderPath() {
    return getTenantHomeFolderPath(TenantUtils.getTenantId());
  }

  public static String getTenantPublicFolderPath() {
    return getTenantPublicFolderPath(TenantUtils.getTenantId());
  }

  public static String getTenantRootFolderPath() {
    return getTenantRootFolderPath(TenantUtils.getTenantId());
  }

  public static String getUserHomeFolderPath() {
    return getUserHomeFolderPath(TenantUtils.getTenantId(), internalGetUsername());
  }

  public static String getTenantEtcFolderPath() {
    return getTenantEtcFolderPath(TenantUtils.getTenantId());
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
    if ((strategyName == null) || "".equals(strategyName)) { //$NON-NLS-1$
      strategyName = DEFAULT;
    }

    if (strategyName.equals(DEFAULT)) {
      strategy = new DefaultServerRepositoryPathsStrategy();
    } else {
      // Try to load a custom strategy
      try {
        Class<?> clazz = Class.forName(strategyName);
        Constructor<?> customStrategy = clazz.getConstructor(new Class[] {});
        strategy = (IServerRepositoryPathsStrategy) customStrategy.newInstance(new Object[] {});
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    logger.debug("ServerRepositoryPaths initialized: strategy=" + strategyName); //$NON-NLS-1$
  }

  public static void setStrategyName(final String strategyName) {
    ServerRepositoryPaths.strategyName = strategyName;
    initialize();
  }

  /**
   * Interface that allows pathing to be configurable.
   */
  public static interface IServerRepositoryPathsStrategy {
    String getPentahoRootFolderPath();

    String getTenantHomeFolderPath(final String tenantId);

    String getTenantPublicFolderPath(final String tenantId);

    String getTenantRootFolderPath(final String tenantId);

    String getUserHomeFolderPath(final String tenantId, final String username);

    String getTenantEtcFolderPath(final String tenantId);

    String getTenantHomeFolderName();

    String getTenantPublicFolderName();

    String getPentahoRootFolderName();

    String getTenantEtcFolderName();

    String getTenantId(final String absPath);
  }

  /**
   * Returns the username of the current user.
   */
  private static String internalGetUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return pentahoSession.getName();
  }

}
