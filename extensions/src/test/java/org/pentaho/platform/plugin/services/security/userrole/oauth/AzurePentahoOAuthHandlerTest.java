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

package org.pentaho.platform.plugin.services.security.userrole.oauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.security.userroledao.PentahoOAuthUser;
import org.pentaho.platform.util.oauth.PentahoOAuthProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class AzurePentahoOAuthHandlerTest {

  @Mock
  IUserRoleDao userRoleDao;

  @Mock
  RestTemplate restTemplate;

  @Mock
  IAuthenticationRoleMapper oauthRoleMapper;

  @Mock
  PentahoOAuthProperties pentahoOAuthProperties;

  @Test
  public void getClientCredentialsTokenReturnsExistingToken() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    handler.registrationToClientCredentialsToken.put( "testRegistration", "existingToken" );

    String token = handler.getClientCredentialsToken( "testRegistration", false );

    assertEquals( "existingToken", token );
  }

  @Test
  public void getClientCredentialsTokenFetchesNewToken() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getClientId( "testRegistration" ) ).thenReturn( "clientId" );
    when( pentahoOAuthProperties.getClientSecret( "testRegistration" ) ).thenReturn( "clientSecret" );
    when( pentahoOAuthProperties.getTokenUri( "testRegistration" ) ).thenReturn( "http://token-uri" );
    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( responseBody, org.springframework.http.HttpStatus.OK ) );

    try ( MockedStatic<Encr> mockedEncr = mockStatic( Encr.class ) ) {
      mockedEncr.when( () -> Encr.decryptPassword( anyString() ) ).thenReturn( anyString() );
      String token = handler.getClientCredentialsToken( "testRegistration", true );

      assertEquals( "newToken", token );
    }
  }

  @Test
  public void getClientCredentialsTokenNullResponse() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getClientId( "testRegistration" ) ).thenReturn( "clientId" );
    when( pentahoOAuthProperties.getClientSecret( "testRegistration" ) ).thenReturn( "clientSecret" );
    when( pentahoOAuthProperties.getTokenUri( "testRegistration" ) ).thenReturn( "http://token-uri" );
    when( restTemplate.postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( null, org.springframework.http.HttpStatus.OK ) );

    try ( MockedStatic<Encr> mockedEncr = mockStatic( Encr.class ) ) {
      mockedEncr.when( () -> Encr.decryptPassword( anyString() ) ).thenReturn( anyString() );
      String token = handler.getClientCredentialsToken( "testRegistration", true );

      assertNull( token );
    }
  }

  @Test
  public void getAppRolesInIdpReturnsCachedRoles() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    Map<String, String> cachedRoles = Map.of( "roleId1", "roleName1" );
    handler.registrationToAppRoles.put( "testRegistration", cachedRoles );

    Map<String, String> roles = handler.getAppRolesInIdp( "testRegistration", "token", false );

    assertEquals( cachedRoles, roles );
  }

  @Test
  public void getAppRolesInIdpFetchesRolesFromIdp() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getAppRoles( "testRegistration" ) ).thenReturn( "http://app-roles-uri" );
    Map<String, Object> responseBody = Map.of( "value", List.of(
      Map.of( "id", "roleId1", "value", "roleName1" ),
      Map.of( "id", "roleId2", "value", "roleName2" )
    ) );
    when( restTemplate.exchange( eq( "http://app-roles-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( responseBody, org.springframework.http.HttpStatus.OK ) );

    Map<String, String> roles = handler.getAppRolesInIdp( "testRegistration", "token", false );

    assertEquals( 2, roles.size() );
    assertEquals( "roleName1", roles.get( "roleId1" ) );
    assertEquals( "roleName2", roles.get( "roleId2" ) );
  }

  @Test
  public void getAppRolesInIdpThrowsException() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getAppRoles( "testRegistration" ) ).thenReturn( "http://app-roles-uri" );
    when( restTemplate.exchange( eq( "http://app-roles-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenThrow( new RuntimeException( "Error during account status retrieval" ) );

    assertNull( handler.getAppRolesInIdp( "testRegistration", "token", false ) );
  }

  @Test
  public void getAppRolesInIdpThrowsExceptionRetry() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getAppRoles( "testRegistration" ) ).thenReturn( "http://app-roles-uri" );
    when( restTemplate.exchange( eq( "http://app-roles-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenThrow( new RuntimeException( "Error during account status retrieval" ) );

    assertThrows( NullPointerException.class, () -> handler.getAppRolesInIdp( "testRegistration", "token", true ) );
  }

  @Test
  public void getAppRolesInIdpNullResponseBody() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getAppRoles( "testRegistration" ) ).thenReturn( "http://app-roles-uri" );
    when( restTemplate.exchange( eq( "http://app-roles-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( null, org.springframework.http.HttpStatus.OK ) );

    Map<String, String> roles = handler.getAppRolesInIdp( "testRegistration", "token", false );

    assertNull( roles );
  }

  @Test
  public void isUserAccountEnabledReturnsTrueWhenEnabled() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "http://account-enabled-uri" );
    Map<String, Object> responseBody = Map.of( "accountEnabled", true );
    when( restTemplate.exchange( eq( "http://account-enabled-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( responseBody, org.springframework.http.HttpStatus.OK ) );

    boolean isEnabled = handler.isUserAccountEnabled( "testRegistration", "token", "userId", false );

    assertTrue( isEnabled );
  }

  @Test
  public void isUserAccountEnabledNullResponse() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "http://account-enabled-uri" );
    when( restTemplate.exchange( eq( "http://account-enabled-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( null, org.springframework.http.HttpStatus.OK ) );

    boolean isEnabled = handler.isUserAccountEnabled( "testRegistration", "token", "userId", false );

    assertFalse( isEnabled );
  }

  @Test
  public void isUserAccountEnabledReturnsFalseWhenDisabled() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "http://account-enabled-uri" );
    Map<String, Object> responseBody = Map.of( "accountEnabled", false );
    when( restTemplate.exchange( eq( "http://account-enabled-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( responseBody, org.springframework.http.HttpStatus.OK ) );

    boolean isEnabled = handler.isUserAccountEnabled( "testRegistration", "token", "userId", false );

    assertFalse( isEnabled );
  }

  @Test
  public void isUserAccountEnabledThrowsExceptionRetry() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "http://account-enabled-uri" );
    when( restTemplate.exchange( eq( "http://account-enabled-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenThrow( new RuntimeException( "Error during account status retrieval" ) );

    assertThrows( NullPointerException.class,
      () -> handler.isUserAccountEnabled( "testRegistration", "token", "userId", true ) );
  }

  @Test
  public void isUserAccountEnabledThrowsException() {
    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "http://account-enabled-uri" );
    when( restTemplate.exchange( eq( "http://account-enabled-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenThrow( new RuntimeException( "Error during account status retrieval" ) );

    boolean isEnabled = handler.isUserAccountEnabled( "testRegistration", "token", "userId", false );

    assertFalse( isEnabled );
  }

  @Test
  public void performSyncForUserUpdatesRoles() {
    try ( MockedStatic<Encr> mockedEncr = mockStatic( Encr.class ) ) {
      AzurePentahoOAuthHandler handler =
        new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
      ITenant tenant = mock( ITenant.class );

      PentahoOAuthUser user = mock( PentahoOAuthUser.class );
      when( user.getUsername() ).thenReturn( "username" );
      when( user.isEnabled() ).thenReturn( true );
      when( user.getRegistrationId() ).thenReturn( "testRegistration" );
      when( user.getUserId() ).thenReturn( "userId" );
      when( user.getTenant() ).thenReturn( tenant );
      when( user.isEnabled() ).thenReturn( true );

      doNothing().when( userRoleDao ).setUserRolesNoValidation( eq( tenant ), eq( "username" ), any() );

      when( pentahoOAuthProperties.getClientId( "testRegistration" ) ).thenReturn( "clientId" );
      when( pentahoOAuthProperties.getClientSecret( "testRegistration" ) ).thenReturn( "clientSecret" );
      when( pentahoOAuthProperties.getTokenUri( "testRegistration" ) ).thenReturn( "http://token-uri" );
      Map<String, String> tokenResponse = Map.of( "access_token", "newToken" );
      when( restTemplate.postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) ) )
        .thenReturn( new ResponseEntity<>( tokenResponse, org.springframework.http.HttpStatus.OK ) );

      when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "http://account-enabled-uri" );
      Map<String, Object> accountEnabledResponse = Map.of( "accountEnabled", true );
      when( restTemplate.exchange( eq( "http://account-enabled-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
        eq( Map.class ) ) )
        .thenReturn( new ResponseEntity<>( accountEnabledResponse, org.springframework.http.HttpStatus.OK ) );

      when( pentahoOAuthProperties.getAppRoles( "testRegistration" ) ).thenReturn( "http://app-roles-uri" );
      Map<String, Object> appRolesResponse = Map.of( "value", List.of(
        Map.of( "id", "roleId1", "value", "roleName1" ),
        Map.of( "id", "roleId2", "value", "roleName2" )
      ) );
      when( restTemplate.exchange( eq( "http://app-roles-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
        eq( Map.class ) ) )
        .thenReturn( new ResponseEntity<>( appRolesResponse, org.springframework.http.HttpStatus.OK ) );

      when( pentahoOAuthProperties.getAppRoleAssignment( "testRegistration" ) ).thenReturn(
        "http://app-role-assignments-uri" );
      Map<String, Object> roleAssignmentsResponse = Map.of( "value", List.of(
        Map.of( "appRoleId", "roleId1" ),
        Map.of( "appRoleId", "roleId2" )
      ) );
      when(
        restTemplate.exchange( eq( "http://app-role-assignments-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
          eq( Map.class ) ) )
        .thenReturn( new ResponseEntity<>( roleAssignmentsResponse, org.springframework.http.HttpStatus.OK ) );

      when( oauthRoleMapper.toPentahoRole( "roleName1" ) ).thenReturn( "mappedRole1" );
      when( oauthRoleMapper.toPentahoRole( "roleName2" ) ).thenReturn( "mappedRole2" );

      mockedEncr.when( () -> Encr.decryptPassword( "clientSecret" ) ).thenReturn( "clientSecret" );
      handler.performSyncForUser( user );

      verify( restTemplate ).postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) );
    }
  }

  @Test
  public void performSyncForUserUpdatesRolesUserDisabled() {
    try ( MockedStatic<Encr> mockedEncr = mockStatic( Encr.class ) ) {
      AzurePentahoOAuthHandler handler =
        new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

      PentahoOAuthUser user = mock( PentahoOAuthUser.class );
      when( user.isEnabled() ).thenReturn( false );
      when( user.getRegistrationId() ).thenReturn( "testRegistration" );
      when( user.getUserId() ).thenReturn( "userId" );
      when( user.isEnabled() ).thenReturn( true );

      when( pentahoOAuthProperties.getClientId( "testRegistration" ) ).thenReturn( "clientId" );
      when( pentahoOAuthProperties.getClientSecret( "testRegistration" ) ).thenReturn( "clientSecret" );
      when( pentahoOAuthProperties.getTokenUri( "testRegistration" ) ).thenReturn( "http://token-uri" );
      Map<String, String> tokenResponse = Map.of( "access_token", "newToken" );
      when( restTemplate.postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) ) )
        .thenReturn( new ResponseEntity<>( tokenResponse, org.springframework.http.HttpStatus.OK ) );

      when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "http://account-enabled-uri" );
      Map<String, Object> accountEnabledResponse = Map.of( "accountEnabled", false );
      when( restTemplate.exchange( eq( "http://account-enabled-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
        eq( Map.class ) ) )
        .thenReturn( new ResponseEntity<>( accountEnabledResponse, org.springframework.http.HttpStatus.OK ) );

      mockedEncr.when( () -> Encr.decryptPassword( "clientSecret" ) ).thenReturn( "clientSecret" );
      handler.performSyncForUser( user );

      verify( restTemplate ).postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) );
    }
  }

  @Test
  public void isUserAccountEnabledHandlesExceptionGracefully() {
    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "http://account-enabled-uri" );
    when( restTemplate.exchange( eq( "http://account-enabled-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenThrow( new RuntimeException( "Error during account status retrieval" ) );

    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    boolean isEnabled = handler.isUserAccountEnabled( "testRegistration", "token", "userId", false );

    assertFalse( isEnabled );
  }

  @Test
  public void getAppRoleAssignmentsForUserNullResponse() {
    when( pentahoOAuthProperties.getAppRoleAssignment( "testRegistration" ) ).thenReturn(
      "http://app-role-assignments-uri" );
    when( restTemplate.exchange( eq( "http://app-role-assignments-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( null, org.springframework.http.HttpStatus.OK ) );

    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    assertTrue( handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", false ).isEmpty() );
  }

  @Test
  public void getAppRoleAssignmentsForUserHandlesExceptionRetry() {
    when( pentahoOAuthProperties.getAppRoleAssignment( "testRegistration" ) ).thenReturn(
      "http://app-role-assignments-uri" );
    when( restTemplate.exchange( eq( "http://app-role-assignments-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenThrow( new RuntimeException( "Error during role assignments retrieval" ) );

    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    assertThrows( NullPointerException.class,
      () -> handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", true ) );
  }

  @Test
  public void getAppRoleAssignmentsForUserHandlesExceptionGracefully() {
    when( pentahoOAuthProperties.getAppRoleAssignment( "testRegistration" ) ).thenReturn(
      "http://app-role-assignments-uri" );
    when( restTemplate.exchange( eq( "http://app-role-assignments-uri" ), eq( HttpMethod.GET ), any( HttpEntity.class ),
      eq( Map.class ) ) )
      .thenThrow( new RuntimeException( "Error during role assignments retrieval" ) );

    AzurePentahoOAuthHandler handler =
      new AzurePentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", false );

    assertTrue( roles.isEmpty() );
  }

}
