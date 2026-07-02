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
