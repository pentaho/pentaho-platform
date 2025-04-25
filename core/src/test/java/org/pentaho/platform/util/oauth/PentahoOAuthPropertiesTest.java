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

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PentahoOAuthPropertiesTest {

  @Mock
  private ISystemConfig systemConfig;

  @Mock
  private Logger logger;

  @Test
  public void getValue_shouldReturnPropertyValueWhenKeyExists()
    throws IOException, NoSuchFieldException, IllegalAccessException {
    String key = "testKey";
    String value = "testValue";

    IConfiguration config = mock( IConfiguration.class );
    Properties properties = new Properties();
    properties.setProperty( key, value );

    when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
    when( config.getProperties() ).thenReturn( properties );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var systemConfigField = PentahoOAuthProperties.class.getDeclaredField( "systemConfig" );
    systemConfigField.setAccessible( true );
    systemConfigField.set( pentahoOAuthProperties, systemConfig );

    String result = pentahoOAuthProperties.getValue( key );

    assertEquals( value, result );
  }

  @Test
  public void getValue_shouldReturnNullWhenKeyDoesNotExist()
    throws IOException, NoSuchFieldException, IllegalAccessException {
    String key = "nonExistentKey";

    IConfiguration config = mock( IConfiguration.class );
    Properties properties = new Properties();

    when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
    when( config.getProperties() ).thenReturn( properties );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var systemConfigField = PentahoOAuthProperties.class.getDeclaredField( "systemConfig" );
    systemConfigField.setAccessible( true );
    systemConfigField.set( pentahoOAuthProperties, systemConfig );

    String result = pentahoOAuthProperties.getValue( key );

    assertNull( result );
  }

  @Test
  public void getValue_shouldReturnNullWhenPropertiesAreNotLoaded()
    throws IOException, NoSuchFieldException, IllegalAccessException {
    IConfiguration config = mock( IConfiguration.class );

    when( systemConfig.getConfiguration( "oauth" ) ).thenReturn( config );
    when( config.getProperties() ).thenThrow( new IOException() );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var systemConfigField = PentahoOAuthProperties.class.getDeclaredField( "systemConfig" );
    systemConfigField.setAccessible( true );
    systemConfigField.set( pentahoOAuthProperties, systemConfig );

    String result = pentahoOAuthProperties.getValue( "anyKey" );

    assertNull( result );
  }

  @Test
  public void getValue_shouldReturnNullWhenPropertiesAreNull()
    throws NoSuchFieldException, IllegalAccessException {

    Properties properties = new Properties();
    properties.setProperty( "anyKey", "anyValue" );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();

    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getValue( "anyKey" );

    assertNotNull( result );
  }

  @Test
  public void getClientIdReturnsCorrectValueWhenKeyExists() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String clientId = "testClientId";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".client-id", clientId );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getClientId( registrationId );

    assertEquals( clientId, result );
  }

  @Test
  public void getClientIdReturnsNullWhenKeyDoesNotExist() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getClientId( registrationId );

    assertNull( result );
  }

  @Test
  public void getTokenUriReturnsCorrectValueWhenKeyExists() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String tokenUri = "http://test-token-uri";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".token-uri", tokenUri );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getTokenUri( registrationId );

    assertEquals( tokenUri, result );
  }

  @Test
  public void getTokenUriReturnsNullWhenKeyDoesNotExist() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getTokenUri( registrationId );

    assertNull( result );
  }

  @Test
  public void getAccountEnabledReturnsCorrectValueWhenKeyExists() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String accountEnabled = "true";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".account-enabled", accountEnabled );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getAccountEnabled( registrationId );

    assertEquals( accountEnabled, result );
  }

  @Test
  public void getAccountEnabledReturnsNullWhenKeyDoesNotExist() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getAccountEnabled( registrationId );

    assertNull( result );
  }

  @Test
  public void getClientSecretReturnsCorrectValueWhenKeyExists() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String clientSecret = "testClientSecret";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".client-secret", clientSecret );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getClientSecret( registrationId );

    assertEquals( clientSecret, result );
  }

  @Test
  public void getClientSecretReturnsNullWhenKeyDoesNotExist() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getClientSecret( registrationId );

    assertNull( result );
  }

  @Test
  public void getClientCredentialsGrantTypeReturnsCorrectValueWhenKeyExists()
    throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String grantType = "client_credentials";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".client-credentials-grant-type", grantType );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getClientCredentialsGrantType( registrationId );

    assertEquals( grantType, result );
  }

  @Test
  public void getClientCredentialsGrantTypeReturnsNullWhenKeyDoesNotExist()
    throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getClientCredentialsGrantType( registrationId );

    assertNull( result );
  }

  @Test
  public void getClientCredentialsScopeReturnsCorrectValueWhenKeyExists()
    throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String scope = "read write";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".client-credentials-scope", scope );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getClientCredentialsScope( registrationId );

    assertEquals( scope, result );
  }

  @Test
  public void getClientCredentialsScopeReturnsNullWhenKeyDoesNotExist()
    throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getClientCredentialsScope( registrationId );

    assertNull( result );
  }

  @Test
  public void getRedirectUriReturnsCorrectValueWhenKeyExists() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String redirectUri = "http://redirect-uri";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".redirect-uri", redirectUri );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getRedirectUri( registrationId );

    assertEquals( redirectUri, result );
  }

  @Test
  public void getRedirectUriReturnsNullWhenKeyDoesNotExist() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getRedirectUri( registrationId );

    assertNull( result );
  }

  @Test
  public void getScopeReturnsCorrectValueWhenKeyExists() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String scope = "openid profile";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".scope", scope );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getScope( registrationId );

    assertEquals( scope, result );
  }

  @Test
  public void getScopeReturnsNullWhenKeyDoesNotExist() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getScope( registrationId );

    assertNull( result );
  }

  @Test
  public void getAppRolesReturnsCorrectValueWhenKeyExists() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String appRoles = "role1,role2";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".app-roles", appRoles );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getAppRoles( registrationId );

    assertEquals( appRoles, result );
  }

  @Test
  public void getAppRolesReturnsNullWhenKeyDoesNotExist() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getAppRoles( registrationId );

    assertNull( result );
  }

  @Test
  public void getAppRoleAssignmentReturnsCorrectValueWhenKeyExists()
    throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";
    String appRoleAssignment = "assignment1,assignment2";

    Properties properties = new Properties();
    properties.setProperty( registrationId + ".app-role-assignment", appRoleAssignment );

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getAppRoleAssignment( registrationId );

    assertEquals( appRoleAssignment, result );
  }

  @Test
  public void getAppRoleAssignmentReturnsNullWhenKeyDoesNotExist() throws NoSuchFieldException, IllegalAccessException {
    String registrationId = "testRegistration";

    Properties properties = new Properties();

    PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();
    var propertiesField = PentahoOAuthProperties.class.getDeclaredField( "properties" );
    propertiesField.setAccessible( true );
    propertiesField.set( pentahoOAuthProperties, properties );

    String result = pentahoOAuthProperties.getAppRoleAssignment( registrationId );

    assertNull( result );
  }

}
