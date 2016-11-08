/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
