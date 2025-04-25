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

package org.pentaho.platform.util.oauth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PentahoOAuthUtilityTest {

  @Mock
  RestTemplate restTemplate;

  @Mock
  IConfiguration config;

  @Mock
  ISystemConfig systemConfig;

  @Before
  public void resetClass() throws NoSuchFieldException, IllegalAccessException, IOException {
    var systemConfigField = PentahoOAuthUtility.class.getDeclaredField( "systemConfig" );
    systemConfigField.setAccessible( true );
    systemConfig = mock( ISystemConfig.class );
    when( systemConfig.getConfiguration( any() ) ).thenReturn( config );
    systemConfigField.set( null, systemConfig );

    var isProviderOAuthField = PentahoOAuthUtility.class.getDeclaredField( "isProviderOAuth" );
    isProviderOAuthField.setAccessible( true );
    isProviderOAuthField.set( null, false );

    restTemplate = mock( RestTemplate.class );
    config = mock( IConfiguration.class );
    when( config.getProperties() ).thenReturn( new Properties() );
    when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
    when( systemConfig.getConfiguration( "security" ) ).thenReturn( config );
  }

  @Test
  public void testIsOAuthEnabledException() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( eq( ISystemConfig.class ) ) ).thenReturn( systemConfig );

      // Mock the behavior of systemConfig and its dependencies
      when( systemConfig.getConfiguration( "security" ) ).thenReturn( config );
      when( config.getProperties() ).thenThrow( new IOException( "Error" ) );

      // Assert the expected outcome
      assertFalse( PentahoOAuthUtility.isOAuthEnabled() );
    }
  }

  @Test
  public void isOAuthEnabledWithDualAuth_configThrowIOException() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      when( systemConfig.getConfiguration( "security" ) ).thenReturn( config );
      when( config.getProperties() ).thenThrow( new IOException() );

      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      assertFalse( PentahoOAuthUtility.isOAuthEnabledWithDualAuth() );
    }
  }

  @Test
  public void shouldPerformLiveUpdate_configThrowsException() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
      when( config.getProperties() ).thenThrow( new IOException() );

      assertFalse( PentahoOAuthUtility.shouldPerformLiveUpdate() );
    }
  }


  @Test
  public void getUserNameAttribute_configThrowsException() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
      when( config.getProperties() ).thenThrow( new IOException() );

      assertNull( PentahoOAuthUtility.getUserNameAttribute( "testRegistrationId" ) );
    }
  }

  @Test
  public void testIsOAuthEnabledOAuthProvider() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      Properties properties = new Properties();
      properties.setProperty( "provider", "oauth" );

      when( config.getProperties() ).thenReturn( properties );
      when( systemConfig.getConfiguration( "security" ) ).thenReturn( config );

      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      assertTrue( PentahoOAuthUtility.isOAuthEnabled() );
    }
  }

  @Test
  public void testIsDualAuthEnabledOAuthProvider() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      Properties dualAuthProperties = new Properties();
      dualAuthProperties.setProperty( "provider", "dualauth" );

      dualAuthProperties.setProperty( "provider1", "oauth" );
      dualAuthProperties.setProperty( "provider2", "other" );
      when( config.getProperties() ).thenReturn( dualAuthProperties );

      when( systemConfig.getConfiguration( "security" ) ).thenReturn( config );

      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      assertTrue( PentahoOAuthUtility.isOAuthEnabled() );
    }
  }

  @Test
  public void testIsOAuthEnabledOtherProvider() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( eq( ISystemConfig.class ) ) ).thenReturn( systemConfig );

      // Mock the behavior of systemConfig and its dependencies
      when( systemConfig.getConfiguration( "security" ) ).thenReturn( config );
      Properties properties = new Properties();
      when( config.getProperties() ).thenReturn( properties );

      properties.setProperty( "provider", "other" );

      // Assert the expected outcome
      assertFalse( PentahoOAuthUtility.isOAuthEnabled() );
    }
  }

  @Test
  public void isOAuthEnabledWithDualAuth_shouldReturnTrueWhenDualAuthAndOAuthProvidersExist() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      when( systemConfig.getConfiguration( "security" ) ).thenReturn( config );
      Properties dualAuthProperties = new Properties();
      dualAuthProperties.setProperty( "provider", "dualauth" );
      dualAuthProperties.setProperty( "provider1", "oauth" );
      dualAuthProperties.setProperty( "provider2", "other" );
      when( config.getProperties() ).thenReturn( dualAuthProperties );
      when( systemConfig.getConfiguration( "dualauth" ) ).thenReturn( config );

      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      assertTrue( PentahoOAuthUtility.isOAuthEnabledWithDualAuth() );
    }
  }

  @Test
  public void isOAuthEnabledWithDualAuth_shouldReturnFalseWhenNoDualAuthProvider() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      when( systemConfig.getConfiguration( "security" ) ).thenReturn( config );
      Properties securityProperties = new Properties();
      securityProperties.setProperty( "provider", "other" );
      when( config.getProperties() ).thenReturn( securityProperties );

      assertFalse( PentahoOAuthUtility.isOAuthEnabledWithDualAuth() );
    }
  }

  @Test
  public void shouldPerformLiveUpdate_shouldReturnTrueWhenPropertyIsTrue() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
      Properties properties = new Properties();
      properties.setProperty( "shouldPerformLiveUpdate", "true" );
      when( config.getProperties() ).thenReturn( properties );

      assertTrue( PentahoOAuthUtility.shouldPerformLiveUpdate() );
    }
  }

  @Test
  public void shouldPerformLiveUpdate_shouldReturnFalseWhenPropertyIsMissing() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
      Properties properties = new Properties();
      when( config.getProperties() ).thenReturn( properties );

      assertFalse( PentahoOAuthUtility.shouldPerformLiveUpdate() );
    }
  }

  @Test
  public void getUserNameAttribute_shouldReturnAttributeWhenExists() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
      Properties properties = new Properties();
      properties.setProperty( "testRegistrationId.user-name-attribute", "username" );
      when( config.getProperties() ).thenReturn( properties );

      assertEquals( "username", PentahoOAuthUtility.getUserNameAttribute( "testRegistrationId" ) );
    }
  }

  @Test
  public void getUserNameAttribute_shouldReturnNullWhenAttributeDoesNotExist() throws IOException {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
      Properties properties = new Properties();
      when( config.getProperties() ).thenReturn( properties );

      assertNull( PentahoOAuthUtility.getUserNameAttribute( "testRegistrationId" ) );
    }
  }

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Test
  public void isUserNamePasswordAuthentication_shouldReturnTrueWhenRequestURIIsLoginEndpoint() {
    SecurityContextHolder.setContext( securityContext );

    boolean result = PentahoOAuthUtility.isUserNamePasswordAuthentication( httpServletRequest );

    assertFalse( result );
    SecurityContextHolder.clearContext();
  }

  @Test
  public void isUserNamePasswordAuthentication_shouldReturnTrueWhenAuthenticationIsUsernamePasswordToken() {
    SecurityContextHolder.setContext( securityContext );
    when( securityContext.getAuthentication() ).thenReturn( mock( UsernamePasswordAuthenticationToken.class ) );

    boolean result = PentahoOAuthUtility.isUserNamePasswordAuthentication( httpServletRequest );

    assertTrue( result );
    SecurityContextHolder.clearContext();
  }

  @Test
  public void isUserNamePasswordAuthentication_shouldReturnFalseWhenRequestURIIsNotLoginEndpointAndAuthenticationIsNotUsernamePasswordToken() {
    SecurityContextHolder.setContext( securityContext );
    when( securityContext.getAuthentication() ).thenReturn( authentication );

    boolean result = PentahoOAuthUtility.isUserNamePasswordAuthentication( httpServletRequest );

    assertFalse( result );
    SecurityContextHolder.clearContext();
  }

  @Test
  public void isUserNamePasswordAuthentication_shouldReturnFalseWhenAuthenticationIsNull() {
    SecurityContextHolder.setContext( securityContext );
    when( securityContext.getAuthentication() ).thenReturn( null );

    boolean result = PentahoOAuthUtility.isUserNamePasswordAuthentication( httpServletRequest );

    assertFalse( result );
    SecurityContextHolder.clearContext();
  }

  @Test
  public void isUserNamePasswordAuthentication_shouldReturnFalseWhenSecurityContextIsNull() {
    SecurityContextHolder.setContext( securityContext );
    SecurityContextHolder.clearContext();

    boolean result = PentahoOAuthUtility.isUserNamePasswordAuthentication( httpServletRequest );

    assertFalse( result );
    SecurityContextHolder.clearContext();
  }

}
