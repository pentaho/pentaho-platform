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
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.web.http.api.resources.services.FeatureValidationService;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.Properties;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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

    }
    Assert.assertNotNull( resp );
  }

  @Test
  public void testIsEnabledDefault() throws IOException {
    Properties properties = new Properties();
    properties.setProperty( "provider", "jackrabbit" );

    IConfiguration configuration = mock( IConfiguration.class );
    ISystemConfig systemConfig = mock( ISystemConfig.class );
    when( systemConfig.getConfiguration( "security" ) ).thenReturn( configuration );
    when( configuration.getProperties() ).thenReturn( properties );

    systemResource.setFeatureValidationService( new FeatureValidationService( systemConfig ) );
    Response response = systemResource.isEnabled( "test" );
    boolean isEnabled = response.getEntity() instanceof Boolean ? (Boolean) response.getEntity() : false;
    Assert.assertFalse( isEnabled );
  }

  @Test
  public void testIsEnabledProcess() throws IOException {
    Properties properties = new Properties();
    properties.setProperty( "provider", "jackrabbit" );

    IConfiguration configuration = mock( IConfiguration.class );
    ISystemConfig systemConfig = mock( ISystemConfig.class );
    when( systemConfig.getConfiguration( "security" ) ).thenReturn( configuration );
    when( configuration.getProperties() ).thenReturn( properties );

    systemResource.setFeatureValidationService( new FeatureValidationService( systemConfig ) );

    Response response = systemResource.isEnabled( "process" );
    boolean isEnabled = response.getEntity() instanceof Boolean ? (Boolean) response.getEntity() : false;
    Assert.assertTrue( isEnabled );

    response = systemResource.isEnabled( "activate" );
    isEnabled = response.getEntity() instanceof Boolean ? (Boolean) response.getEntity() : false;
    Assert.assertTrue( isEnabled );

    response = systemResource.isEnabled( "change_password" );
    isEnabled = response.getEntity() instanceof Boolean ? (Boolean) response.getEntity() : false;
    Assert.assertTrue( isEnabled );

    properties.setProperty( "provider", "super" );
    response = systemResource.isEnabled( "change_password" );
    isEnabled = response.getEntity() instanceof Boolean ? (Boolean) response.getEntity() : false;
    Assert.assertTrue( isEnabled );
  }

  @Test
  public void testIsEnabledThrowsException() throws IOException {
    IConfiguration configuration = mock( IConfiguration.class );
    ISystemConfig systemConfig = mock( ISystemConfig.class );
    when( systemConfig.getConfiguration( "security" ) ).thenReturn( configuration );
    doThrow( new IOException( "Just For Testing" ) ).when( configuration ).getProperties();

    systemResource.setFeatureValidationService( new FeatureValidationService( systemConfig ) );
    Response response = systemResource.isEnabled( "change_password" );
    boolean isEnabled = response.getEntity() instanceof Boolean ? (Boolean) response.getEntity() : false;
    Assert.assertFalse( isEnabled );
  }

}
