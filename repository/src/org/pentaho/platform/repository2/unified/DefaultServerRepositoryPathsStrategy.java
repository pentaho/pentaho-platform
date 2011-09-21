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

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths.IServerRepositoryPathsStrategy;
import org.springframework.util.Assert;


/**
 * Default {@link IServerRepositoryPathsStrategy} implementation. Uses MessageFormat patterns.
 * 
 * @author mlowery
 */
public class DefaultServerRepositoryPathsStrategy implements IServerRepositoryPathsStrategy {

  // ~ Static fields/initializers ======================================================================================

  private static final String FOLDER_ROOT = "pentaho"; //$NON-NLS-1$

  private static final String FOLDER_ETC = "etc"; //$NON-NLS-1$

  private static final String PATH_ROOT = RepositoryFile.SEPARATOR + FOLDER_ROOT;

  // ~ Instance fields =================================================================================================

  private final String PATTERN_TENANT_ROOT_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}"; //$NON-NLS-1$

  private final String PATTERN_TENANT_HOME_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
      + ClientRepositoryPaths.getHomeFolderPath();

  private final String PATTERN_TENANT_PUBLIC_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
      + ClientRepositoryPaths.getPublicFolderPath();

  private final String PATTERN_TENANT_ETC_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
      + RepositoryFile.SEPARATOR + FOLDER_ETC;

//  private final String PATTERN_USER_HOME_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
//      + RepositoryFile.SEPARATOR + FOLDER_HOME + RepositoryFile.SEPARATOR + "{1}"; //$NON-NLS-1$

  private final String REGEX_TENANT_ABS_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "([\\w-]*)" //$NON-NLS-1$
      + RepositoryFile.SEPARATOR + ".*"; //$NON-NLS-1$

  // ~ Constructors ====================================================================================================

  public DefaultServerRepositoryPathsStrategy() {
    super();
  }

  // ~ Methods =========================================================================================================

  public String getPentahoRootFolderPath() {
    return PATH_ROOT;
  }

  public String getTenantHomeFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_HOME_PATH, tenantId);
  }

  public String getTenantPublicFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_PUBLIC_PATH, tenantId);
  }

  public String getTenantRootFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_ROOT_PATH, tenantId);
  }

  public String getUserHomeFolderPath(final String tenantId, final String username) {
    return MessageFormat.format(PATTERN_TENANT_ROOT_PATH, tenantId)
        + ClientRepositoryPaths.getUserHomeFolderPath(username);
  }

  public String getPentahoRootFolderName() {
    return FOLDER_ROOT;
  }

  public String getTenantHomeFolderName() {
    return ClientRepositoryPaths.getHomeFolderName();
  }

  public String getTenantPublicFolderName() {
    return ClientRepositoryPaths.getPublicFolderName();
  }

  public String getTenantEtcFolderName() {
    return FOLDER_ETC;
  }

  public String getTenantEtcFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_ETC_PATH, tenantId);
  }

  public String getTenantId(final String absPath) {
    Pattern pattern = Pattern.compile(REGEX_TENANT_ABS_PATH);
    Matcher matcher = pattern.matcher(absPath);
    Assert.isTrue(matcher.matches());
    Assert.isTrue(matcher.groupCount() == 1);
    return matcher.group(1);
  }

}
