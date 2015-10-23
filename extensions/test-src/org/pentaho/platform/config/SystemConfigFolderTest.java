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
    assertEquals( path + "/systemListeners.xml", systemConfigFolder.getSystemListenersConfigFile().getPath() );
    assertEquals( path + "/pentaho.xml", systemConfigFolder.getPentahoXmlFile().getPath() );
    assertEquals( path + "/adminPlugins.xml", systemConfigFolder.getAdminPluginsFile().getPath() );
    assertEquals( path + "/pentahoObjects.spring.xml", systemConfigFolder.getPentahoObjectsConfigFile().getPath() );
    assertEquals( path + "/sessionStartupActions.xml", systemConfigFolder.getSessionStartupActionsFile().getPath() );
    assertEquals( path + "/applicationContext-spring-security.xml", systemConfigFolder.getSpringSecurityXmlFile().getPath() );
    assertEquals( path + "/applicationContext-pentaho-security-ldap.xml", systemConfigFolder.getPentahoSecurityXmlFile().getPath() );
    assertEquals( path + "/applicationContext-spring-security-ldap.xml", systemConfigFolder.getSpringSecurityLdapXmlFile().getPath() );
    assertEquals( path + "/applicationContext-common-authorization.xml", systemConfigFolder.getCommonAuthorizationXmlFile().getPath() );
  }

  @Test
  public void testConstructor() throws Exception {
    tmp = File.createTempFile( "SystemConfigFolderTest", ".properties" );
    tmp.deleteOnExit();
    systemConfigFolder = new SystemConfigFolder( tmp.getParentFile() );
    assertEquals( tmp.getParentFile(), systemConfigFolder.getFolder() );
  }
}
