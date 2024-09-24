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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.test.platform.utils.TestResourceLocation;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/20/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class AppConfigPropertiesTest {

  @Mock PentahoObjectsConfig pentahoObjectsConfig;
  @Mock IConsoleConfig consoleConfig;
  @Mock HibernateSettingsXml hibernateSettings;

  AppConfigProperties appConfigProperties;
  AppConfigProperties spyAppConfigProperties;

  @Before
  public void setUp() throws Exception {
    appConfigProperties = AppConfigProperties.getInstance();
    spyAppConfigProperties = spy( appConfigProperties );
  }

  @Test
  public void testGetInstance() throws Exception {
    assertNotNull( AppConfigProperties.getInstance() );
    assertTrue( AppConfigProperties.getInstance() instanceof AppConfigProperties );
  }

  @Test
  public void testRefreshConfig() throws Exception {
    doReturn( pentahoObjectsConfig ).when( spyAppConfigProperties ).getPentahoObjectsConfig();
    when( pentahoObjectsConfig.getPasswordService() ).thenReturn( "" );

    spyAppConfigProperties.refreshConfig();
  }

  @Test( expected = AppConfigException.class )
  public void testRefreshConfig_configError() throws Exception {
    doReturn( pentahoObjectsConfig ).when( spyAppConfigProperties ).getPentahoObjectsConfig();
    when( pentahoObjectsConfig.getPasswordService() ).thenReturn( "com.pentaho.DoesNotExist" );

    spyAppConfigProperties.refreshConfig();
  }

  @Test
  public void testIsValidConfiguration() throws Exception {
    assertFalse( appConfigProperties.isValidConfiguration() );
  }

  @Test
  public void testIsValidConfiguration_solutionIsValid_notWar() throws Exception {
    doReturn( "test-res" ).when( spyAppConfigProperties ).getSolutionPath();
    doReturn( TestResourceLocation.TEST_RESOURCES + "/badFile.txt" ).when( spyAppConfigProperties ).getWarPath();

    assertFalse( spyAppConfigProperties.isValidConfiguration() );
  }

  @Test
  public void testIsValidConfiguration_solutionIsNoValid_warIs() throws Exception {
    doReturn( TestResourceLocation.TEST_RESOURCES + "/notAfolder.txt" ).when( spyAppConfigProperties ).getSolutionPath();
    doReturn( "test-res" ).when( spyAppConfigProperties ).getWarPath();

    assertFalse( spyAppConfigProperties.isValidConfiguration() );
  }

  @Test
  public void testGetPasswordEncoder() throws Exception {
    SpringSecurityHibernateConfig springSecurityConfig = mock( SpringSecurityHibernateConfig.class );
    doReturn( springSecurityConfig ).when( spyAppConfigProperties ).getSpringSecurityHibernateConfig();
    PasswordEncoder pwdEncoder = mock( PasswordEncoder.class );
    when( springSecurityConfig.getPasswordEncoder() ).thenReturn( pwdEncoder );

    PasswordEncoder passwordEncoder = spyAppConfigProperties.getPasswordEncoder();
    verify( springSecurityConfig ).getPasswordEncoder();
    assertEquals( pwdEncoder, passwordEncoder );
  }

  @Test
  public void testGetPlatformUsername() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getPlatformUserName() ).thenReturn( "admin" );
    assertEquals( "admin", spyAppConfigProperties.getPlatformUsername() );
  }

  @Test
  public void testGetPlatformUsername_defaultUsername() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getPlatformUserName() ).thenReturn( null );
    assertEquals( AppConfigProperties.DEFAULT_PLATFORM_USERNAME, spyAppConfigProperties.getPlatformUsername() );
  }

  @Test
  public void testGetBiServerContextPath() throws Exception {
    doReturn( "http://localhost:8080/pentaho-di/" ).when( spyAppConfigProperties ).getBiServerBaseUrl();

    String biServerContextPath = spyAppConfigProperties.getBiServerContextPath();
    assertEquals( "/pentaho-di", biServerContextPath );
  }

  @Test
  public void testGetBiServerContextPath_default() throws Exception {
    doReturn( "http://localhost:8080/" ).when( spyAppConfigProperties ).getBiServerBaseUrl();

    String biServerContextPath = spyAppConfigProperties.getBiServerContextPath();
    assertEquals( AppConfigProperties.DEFAULT_BISERVER_CONTEXT_PATH, biServerContextPath );
  }

  @Test
  public void testGetBiServerBaseUrl() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getBaseUrl() ).thenReturn( "http://www.myapp.com/pentaho/" );
    assertEquals( "http://www.myapp.com/pentaho/", spyAppConfigProperties.getBiServerBaseUrl() );
  }

  @Test
  public void testGetBiServerBaseUrl_fromInvalidWebXmlLocation() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getBaseUrl() ).thenReturn( null );
    when( spyAppConfigProperties.getWarPath() ).thenReturn( "test-res" );
    String baseUrl = spyAppConfigProperties.getBiServerBaseUrl();
    assertEquals( AppConfigProperties.DEFAULT_BISERVER_BASE_URL, baseUrl );
  }

  @Test
  public void testGetBiServerStatusCheckPeriod() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getServerStatusCheckPeriod() ).thenReturn( 25L );
    assertEquals( "25", spyAppConfigProperties.getBiServerStatusCheckPeriod() );
  }

  @Test
  public void testGetBiServerStatusCheckPeriod_Default() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getServerStatusCheckPeriod() ).thenReturn( null );
    assertEquals( AppConfigProperties.DEFAULT_BISERVER_STATUS_CHECK_PERIOD,
      spyAppConfigProperties.getBiServerStatusCheckPeriod() );
  }

  @Test
  public void testGetDefaultRolesString() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getDefaultRoles() ).thenReturn( "Authenticated,Contributor" );
    assertEquals( "Authenticated,Contributor", spyAppConfigProperties.getDefaultRolesString() );
    verify( consoleConfig ).getDefaultRoles();
  }

  @Test
  public void testGetDefaultRoles() throws Exception {
    doReturn( "Authenticated,Contributor" ).when( spyAppConfigProperties ).getDefaultRolesString();
    List<String> defaultRoles = spyAppConfigProperties.getDefaultRoles();
    assertEquals( 2, defaultRoles.size() );
    assertEquals( "Authenticated", defaultRoles.get( 0 ) );
    assertEquals( "Contributor", defaultRoles.get( 1 ) );
  }

  @Test
  public void testGetDefaultRoles_nullRolesString() throws Exception {
    doReturn( null ).when( spyAppConfigProperties ).getDefaultRolesString();
    List<String> defaultRoles = spyAppConfigProperties.getDefaultRoles();
    assertEquals( 0, defaultRoles.size() );
  }

  @Test
  public void testGetHomepageUrl() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getHomePageUrl() ).thenReturn( "http://www.pentaho.com" );
    assertEquals( "http://www.pentaho.com", spyAppConfigProperties.getHomepageUrl() );
  }

  @Test
  public void testGetHomepageUrl_Default() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getHomePageUrl() ).thenReturn( "" );
    assertEquals( AppConfigProperties.DEFAULT_HOMEPAGE_URL, spyAppConfigProperties.getHomepageUrl() );
  }

  @Test
  public void testGetHomepageTimeout() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getHomePageTimeout() ).thenReturn( 25 );
    assertEquals( "25", spyAppConfigProperties.getHomepageTimeout() );
  }

  @Test
  public void testGetHomepageTimeout_default() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getHomePageTimeout() ).thenReturn( null );
    assertEquals( AppConfigProperties.DEFAULT_HOMEPAGE_TIMEOUT, spyAppConfigProperties.getHomepageTimeout() );
  }

  @Test
  public void testGetHibernateConfigPath() throws Exception {
    doReturn( hibernateSettings ).when( spyAppConfigProperties ).getHibernateSettingsXml();
    when( hibernateSettings.getHibernateConfigFile() ).thenReturn( "/home/test/config.xml" );
    assertEquals( "/home/test/config.xml", spyAppConfigProperties.getHibernateConfigPath() );
  }

  @Test
  public void testGetHibernateConfigPath_default() throws Exception {
    doReturn( hibernateSettings ).when( spyAppConfigProperties ).getHibernateSettingsXml();
    when( hibernateSettings.getHibernateConfigFile() ).thenReturn( null );
    assertEquals( AppConfigProperties.DEFAULT_HIBERNATE_CONFIG_PATH, spyAppConfigProperties.getHibernateConfigPath() );
  }

  @Test
  public void testIsHibernateManaged() throws Exception {
    doReturn( hibernateSettings ).when( spyAppConfigProperties ).getHibernateSettingsXml();
    when( hibernateSettings.getHibernateManaged() ).thenReturn( true );
    assertEquals( true, spyAppConfigProperties.isHibernateManaged() );
    verify( hibernateSettings ).getHibernateManaged();
  }

  @Test
  public void testGetSolutionPath() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getSolutionPath() ).thenReturn( "/home/users/solutionPath" );
    assertEquals( "/home/users/solutionPath", spyAppConfigProperties.getSolutionPath() );
  }

  @Test
  public void testGetSolutionPath_default() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getSolutionPath() ).thenReturn( null );
    when( consoleConfig.getDefaultBiServerDir() ).thenReturn( null );
    assertEquals( "./../pentaho-solutions", spyAppConfigProperties.getSolutionPath() );
  }

  @Test
  public void testGetWarPath() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getWebAppPath() ).thenReturn( "/home/users/webAppPath" );
    assertEquals( "/home/users/webAppPath", spyAppConfigProperties.getWarPath() );
  }

  @Test
  public void testGetWarPath_default() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getWebAppPath() ).thenReturn( null );
    when( consoleConfig.getDefaultBiServerDir() ).thenReturn( null );
    assertEquals( "./../tomcat/webapps/pentaho", spyAppConfigProperties.getWarPath() );
  }

  @Test
  public void testGetPasswordServiceClass() throws Exception {
    doReturn( pentahoObjectsConfig ).when( spyAppConfigProperties ).getPentahoObjectsConfig();
    when( pentahoObjectsConfig.getPasswordService() ).thenReturn( "org.pentaho.test.PasswordService" );
    assertEquals( "org.pentaho.test.PasswordService", spyAppConfigProperties.getPasswordServiceClass() );
  }

  @Test
  public void testGetPasswordServiceClass_default() throws Exception {
    doReturn( pentahoObjectsConfig ).when( spyAppConfigProperties ).getPentahoObjectsConfig();
    when( pentahoObjectsConfig.getPasswordService() ).thenReturn( null );
    assertEquals( AppConfigProperties.DEFAULT_VALUE_PASSWORD_SERVICE_CLASS,
      spyAppConfigProperties.getPasswordServiceClass() );
  }

  @Test
  public void testGetJdbcDriverPath() throws Exception {
    assertEquals( AppConfigProperties.JDBC_DRIVER_PATH, appConfigProperties.getJdbcDriverPath() );
  }

  @Test
  public void testGetHelpUrl() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getHelpUrl() ).thenReturn( "http://help.pentaho.com" );
    assertEquals( "http://help.pentaho.com", spyAppConfigProperties.getHelpUrl() );
  }

  @Test
  public void testGetHelpUrl_default() throws Exception {
    doReturn( consoleConfig ).when( spyAppConfigProperties ).getConsoleConfig();
    when( consoleConfig.getHelpUrl() ).thenReturn( null );
    assertEquals( AppConfigProperties.DEFAULT_HELP_URL, spyAppConfigProperties.getHelpUrl() );
  }

  @Test
  public void testGetConsoleConfig() throws Exception {
    IConsoleConfig consoleConfig = appConfigProperties.getConsoleConfig();
    assertNotNull( consoleConfig );
  }

  @Test
  public void testGetHibernateSettingsXml() throws Exception {
    HibernateSettingsXml hibernateSettingsXml = appConfigProperties.getHibernateSettingsXml();
    assertNotNull( hibernateSettingsXml );
  }

  @Test
  public void testGetPentahoObjectsConfig() throws Exception {
    PentahoObjectsConfig pentahoObjectsConfig = appConfigProperties.getPentahoObjectsConfig();
    assertNotNull( pentahoObjectsConfig );
  }

  @Test
  public void testGetSpringSecurityHibernateConfig() throws Exception {
    SpringSecurityHibernateConfig springSecurityHibernateConfig =
      appConfigProperties.getSpringSecurityHibernateConfig();
    assertNotNull( springSecurityHibernateConfig );
  }
}
