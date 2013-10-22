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

package org.pentaho.platform.repository2;

import org.pentaho.platform.api.repository.IClientRepositoryPathsStrategy;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

public class DefaultClientRepositoryPathsStrategy implements IClientRepositoryPathsStrategy {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final String FOLDER_HOME = "home"; //$NON-NLS-1$

  private static final String FOLDER_PUBLIC = "public"; //$NON-NLS-1$

  private static final String FOLDER_ETC = "etc"; //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public DefaultClientRepositoryPathsStrategy() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public String getUserHomeFolderPath( final String username ) {
    return getHomeFolderPath() + RepositoryFile.SEPARATOR + getUserHomeFolderName( username );
  }

  public String getHomeFolderPath() {
    return RepositoryFile.SEPARATOR + FOLDER_HOME;
  }

  public String getPublicFolderPath() {
    return RepositoryFile.SEPARATOR + FOLDER_PUBLIC;
  }

  public String getHomeFolderName() {
    return FOLDER_HOME;
  }

  public String getPublicFolderName() {
    return FOLDER_PUBLIC;
  }

  public String getUserHomeFolderName( final String username ) {
    return username;
  }

  public String getRootFolderPath() {
    return RepositoryFile.SEPARATOR;
  }

  public String getEtcFolderPath() {
    return RepositoryFile.SEPARATOR + FOLDER_ETC;
  }

  public String getEtcFolderName() {
    return FOLDER_ETC;
  }

}
