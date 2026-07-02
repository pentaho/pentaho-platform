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


package org.pentaho.platform.config;

public interface IConsoleConfig {
  public String getSolutionPath();

  public void setSolutionPath( String settingsDirectoryPath );

  public String getWebAppPath();

  public void setWebAppPath( String webAppPath );

  public String getBackupDirectory();

  public void setBackupDirectory( String backupDirectory );

  public String getPlatformUserName();

  public void setPlatformUserName( String username );

  public Long getServerStatusCheckPeriod();

  public void setServerStatusCheckPeriod( Long serverStatusCheckPeriod );

  public Integer getHomePageTimeout();

  public void setHomePageTimeout( Integer serverStatusCheckPeriod );

  public String getHomePageUrl();

  public void setHomePageUrl( String url );

  public String getTempDirectory();

  public void setTempDirectory( String tempDir );

  public String getHelpUrl();

  public void setHelpUrl( String url );

  public String getJdbcDriversClassPath();

  public void setJdbcDriversClassPath( String classpath );

  public String getDefaultRoles();

  public void setDefaultRoles( String defaultRoles );

  public String getBaseUrl();

  public void setBaseUrl( String url );

  public String getDefaultBiServerDir();

  public void setDefaultBiServerDir( String dir );

  public String getXmlEncoding();

  public void setXmlEncoding( String encoding );

}
