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


package org.pentaho.platform.api.repository;

/**
 * @author Rowell Belen
 */
public interface IClientRepositoryPathsStrategy {

  String getPublicFolderName();

  String getHomeFolderName();

  String getUserHomeFolderName( final String username );

  String getPublicFolderPath();

  String getHomeFolderPath();

  String getUserHomeFolderPath( final String username );

  String getRootFolderPath();

  String getEtcFolderPath();

  String getEtcFolderName();

}
