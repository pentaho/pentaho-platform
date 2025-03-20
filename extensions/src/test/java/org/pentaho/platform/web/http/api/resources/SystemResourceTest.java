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


package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class SystemResourceTest {
  SystemResource systemResource;
  private static MicroPlatform platform;

  @BeforeClass
  public static void initPlatform() throws Exception {
    platform = new MicroPlatform();
    platform.defineInstance( IUserSettingService.class, mock( IUserSettingService.class ) );
    platform.start();
  }

  @Before
  public void setup() {
    PentahoSessionHolder.setSession( null );
    systemResource = new SystemResource();
  }

  @After
  public void teardown() {
    systemResource = null;
  }

  @AfterClass
  public static void shutdownPlatform() {
    platform.stop();
  }

  @Test
  public void testSetLocaleOverride() {
    Response resp = null;
    try {
      resp = systemResource.setLocaleOverride( "en_US" );
    } catch ( Exception e ) {
      // ignore
    }
    Assert.assertNotNull( resp );
  }

  @Test
  public void returnsOAuthProviderWhenCallerIsGWTAndOAuthEnabled() throws Exception {
    IConfiguration config = mock( IConfiguration.class );
    Properties properties = new Properties();
    properties.setProperty( "provider", "default" );
    when( config.getProperties() ).thenReturn( properties );

    ISystemConfig sysConfig = mock( ISystemConfig.class );
    when( sysConfig.getConfiguration( "security" ) ).thenReturn( config );

    systemResource = new SystemResource( sysConfig );

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    when( mockRequest.getHeader( "caller" ) ).thenReturn( "gwt" );
    systemResource.httpServletRequest = mockRequest;

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( true );

      Response response = systemResource.getAuthenticationProvider();
      Assert.assertNotNull( response );
      Assert.assertTrue( response.getEntity().toString().contains( "oauth" ) );
    }
  }

  @Test
  public void returnsOAuthProviderWhenCallerIsGWTAndOAuthNotEnabled() throws Exception {
    IConfiguration config = mock( IConfiguration.class );
    Properties properties = new Properties();
    properties.setProperty( "provider", "default" );
    when( config.getProperties() ).thenReturn( properties );

    ISystemConfig sysConfig = mock( ISystemConfig.class );
    when( sysConfig.getConfiguration( "security" ) ).thenReturn( config );

    systemResource = new SystemResource( sysConfig );

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    when( mockRequest.getHeader( "caller" ) ).thenReturn( "gwt" );
    systemResource.httpServletRequest = mockRequest;

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( false );

      Response response = systemResource.getAuthenticationProvider();
      Assert.assertNotNull( response );
      Assert.assertFalse( response.getEntity().toString().contains( "oauth" ) );
    }
  }

  @Test
  public void returnsOAuthProviderWhenCallerIsNotGWT() throws Exception {
    IConfiguration config = mock( IConfiguration.class );
    Properties properties = new Properties();
    properties.setProperty( "provider", "default" );
    when( config.getProperties() ).thenReturn( properties );

    ISystemConfig sysConfig = mock( ISystemConfig.class );
    when( sysConfig.getConfiguration( "security" ) ).thenReturn( config );

    systemResource = new SystemResource( sysConfig );

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    when( mockRequest.getHeader( "caller" ) ).thenReturn( "not-gwt" );
    systemResource.httpServletRequest = mockRequest;

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( false );

      Response response = systemResource.getAuthenticationProvider();
      Assert.assertNotNull( response );
      Assert.assertFalse( response.getEntity().toString().contains( "oauth" ) );
    }
  }

}
