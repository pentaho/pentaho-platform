/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.config;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/22/15.
 */
public class SystemConfigFolderTest {

  File tmp;
  String path;

  SystemConfigFolder systemConfigFolder;

  @Before
  public void setUp() throws Exception {
    tmp = File.createTempFile( "SystemConfigFolderTest", null );
    path = tmp.getParent();
    tmp.deleteOnExit();
    systemConfigFolder = new SystemConfigFolder( path );
  }

  @Test
  public void testGetters() throws Exception {
    assertPathsAreEqual( "systemListeners.xml", systemConfigFolder.getSystemListenersConfigFile() );
    assertPathsAreEqual( "pentaho.xml", systemConfigFolder.getPentahoXmlFile() );
    assertPathsAreEqual( "adminPlugins.xml", systemConfigFolder.getAdminPluginsFile() );
    assertPathsAreEqual( "pentahoObjects.spring.xml", systemConfigFolder.getPentahoObjectsConfigFile() );
    assertPathsAreEqual( "sessionStartupActions.xml", systemConfigFolder.getSessionStartupActionsFile() );
    assertPathsAreEqual( "applicationContext-spring-security.xml", systemConfigFolder.getSpringSecurityXmlFile() );
    assertPathsAreEqual( "applicationContext-pentaho-security-ldap.xml", systemConfigFolder.getPentahoSecurityXmlFile() );
    assertPathsAreEqual( "applicationContext-spring-security-ldap.xml", systemConfigFolder.getSpringSecurityLdapXmlFile() );
    assertPathsAreEqual( "applicationContext-common-authorization.xml", systemConfigFolder.getCommonAuthorizationXmlFile() );
  }

  private void assertPathsAreEqual( String expectedFile, File actual ) {
    String expectedPath = path + File.separatorChar + expectedFile;
    assertEquals( actual.getPath(), expectedPath );
  }

  @Test
  public void testConstructor() throws Exception {
    tmp = File.createTempFile( "SystemConfigFolderTest", ".properties" );
    tmp.deleteOnExit();
    systemConfigFolder = new SystemConfigFolder( tmp.getParentFile() );
    assertEquals( tmp.getParentFile(), systemConfigFolder.getFolder() );
  }
}
