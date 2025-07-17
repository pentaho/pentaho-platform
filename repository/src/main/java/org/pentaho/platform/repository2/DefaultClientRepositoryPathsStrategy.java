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
