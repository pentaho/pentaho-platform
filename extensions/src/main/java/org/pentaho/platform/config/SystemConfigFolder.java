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

import java.io.File;

public class SystemConfigFolder {
  File systemFolder = null;

  public SystemConfigFolder( File folder ) {
    systemFolder = folder;
  }

  public SystemConfigFolder( String path ) {
    systemFolder = new File( path );
  }

  public File getFolder() {
    return systemFolder;
  }

  public File getSystemListenersConfigFile() {
    return new File( getFolder().getAbsoluteFile() + File.separator + "systemListeners.xml" ); //$NON-NLS-1$
  }

  public File getPentahoXmlFile() {
    return new File( getFolder().getAbsoluteFile() + File.separator + "pentaho.xml" ); //$NON-NLS-1$
  }

  public File getAdminPluginsFile() {
    return new File( getFolder().getAbsoluteFile() + File.separator + "adminPlugins.xml" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public File getPentahoObjectsConfigFile() {
    return new File( getFolder().getAbsoluteFile() + File.separator + "pentahoObjects.spring.xml" ); //$NON-NLS-1$
  }

  public File getSessionStartupActionsFile() {
    return new File( getFolder().getAbsoluteFile() + File.separator + "sessionStartupActions.xml" ); //$NON-NLS-1$
  }

  public File getSpringSecurityXmlFile() {
    return new File( getFolder().getAbsoluteFile() + File.separator + "applicationContext-spring-security.xml" ); //$NON-NLS-1$
  }

  public File getPentahoSecurityXmlFile() {
    return new File( getFolder().getAbsoluteFile() + File.separator + "applicationContext-pentaho-security-ldap.xml" ); //$NON-NLS-1$
  }

  public File getSpringSecurityLdapXmlFile() {
    return new File( getFolder().getAbsoluteFile() + File.separator + "applicationContext-spring-security-ldap.xml" ); //$NON-NLS-1$
  }

  public File getCommonAuthorizationXmlFile() {
    return new File( getFolder().getAbsoluteFile() + File.separator + "applicationContext-common-authorization.xml" ); //$NON-NLS-1$
  }
}
