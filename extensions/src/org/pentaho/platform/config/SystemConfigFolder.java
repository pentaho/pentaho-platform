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
