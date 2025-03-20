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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.security.userroledao.PentahoOAuthUser;
import org.pentaho.platform.util.oauth.PentahoOAuthProperties;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PentahoOAuthAzureHandlerTest {

  @Mock
  IUserRoleDao userRoleDao;

  @Mock
  RestTemplate restTemplate;

  @Mock
  IAuthenticationRoleMapper oauthRoleMapper;

  @Mock
  PentahoOAuthProperties pentahoOAuthProperties;

  @Test
  public void testGetClientCredentialsToken() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    handler.registrationToClientCredentialsToken.put( "testRegistration", "existingToken" );

    String token = handler.getClientCredentialsToken( "testRegistration", false );

    assertEquals( "existingToken", token );
  }

  @Test
  public void testGetClientCredentialsNewToken() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getValue( "testRegistration.client-id" ) ).thenReturn( "clientId" );
    when( pentahoOAuthProperties.getValue( "testRegistration.client-secret" ) ).thenReturn( "clientSecret" );
    when( pentahoOAuthProperties.getValue( "testRegistration.token-uri" ) ).thenReturn( "http://token-uri" );
    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( eq( "http://token-uri" ), any( org.springframework.http.HttpEntity.class ),
      eq( Map.class ) ) ).thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    String token = handler.getClientCredentialsToken( "testRegistration", true );

    assertEquals( "newToken", token );
  }

  @Test
  public void testGetClientCredentialsNullResponse() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getValue( "testRegistration.client-id" ) ).thenReturn( "clientId" );
    when( pentahoOAuthProperties.getValue( "testRegistration.client-secret" ) ).thenReturn( "clientSecret" );
    when( pentahoOAuthProperties.getValue( "testRegistration.token-uri" ) ).thenReturn( "http://token-uri" );
    when( restTemplate.postForEntity( eq( "http://token-uri" ), any( org.springframework.http.HttpEntity.class ),
      eq( Map.class ) ) ).thenReturn( new ResponseEntity<>( null, HttpStatus.OK ) );

    String token = handler.getClientCredentialsToken( "testRegistration", true );

    assertNull( token );
  }

  @Test
  public void getAppRolesInIdp_shouldReturnAppRolesWhenValidResponse() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    String registrationId = "testRegistration";
    String clientCredentialsToken = "token";
    Map<String, String> expectedRoles = Map.of( "roleId1", "roleName1", "roleId2", "roleName2" );

    Map<String, Object> responseBody = Map.of( "value", List.of(
      Map.of( "id", "roleId1", "value", "roleName1" ),
      Map.of( "id", "roleId2", "value", "roleName2" )
    ) );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      when( mockInstance.getResponseEntity( "testRegistration.app-roles", "token", "", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );

      Map<String, String> roles = handler.getAppRolesInIdp( registrationId, clientCredentialsToken, true );

      assertEquals( expectedRoles, roles );
    }
  }

  @Test
  public void getAppRolesInIdp_shouldReturnAppRolesWhenNullResponse() {
    String registrationId = "testRegistration";
    String clientCredentialsToken = "token";

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      when( mockInstance.getResponseEntity( "testRegistration.app-roles", "token", "", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( null, HttpStatus.OK ) );

      PentahoOAuthAzureHandler handler =
        new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
      Map<String, String> roles = handler.getAppRolesInIdp( registrationId, clientCredentialsToken, true );

      assertTrue( MapUtils.isEmpty( roles ) );
    }
  }

  @Test
  public void getAppRolesInIdp_mockRoles() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    var expectedRoles = Map.of( "testRegistration", Map.of( "roleId1", "roleName1", "roleId2", "roleName2" ) );
    handler.registrationToAppRoles.putAll( expectedRoles );

    Map<String, String> roles = handler.getAppRolesInIdp( "testRegistration", "", true );
    assertEquals( expectedRoles.get( "testRegistration" ), roles );
  }

  @Test
  public void isUserAccountEnabled() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    String registrationId = "testRegistration";
    String clientCredentialsToken = "token";
    String userId = "userId";

    Map<String, Object> responseBody = Map.of( "accountEnabled", true );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      when( mockInstance.getResponseEntity( "testRegistration.account-enabled", "token", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );

      boolean isEnabled = handler.isUserAccountEnabled( registrationId, clientCredentialsToken, userId, true );

      assertTrue( isEnabled );
    }
  }

  @Test
  public void isUserAccountEnabledNullResponse() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    String registrationId = "testRegistration";
    String clientCredentialsToken = "token";
    String userId = "userId";

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      when( mockInstance.getResponseEntity( "testRegistration.account-enabled", "token", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( null, HttpStatus.OK ) );

      boolean isEnabled = handler.isUserAccountEnabled( registrationId, clientCredentialsToken, userId, true );

      assertFalse( isEnabled );
    }
  }

  @Test
  public void testGetAppRoleAssignmentsForUser() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    List<Map<String, Object>> responseBody = List.of(
      Map.of( "appRoleId", "role1" ),
      Map.of( "appRoleId", "role2" )
    );
    Map<String, Object> map = Map.of( "value", responseBody );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );
      when(
        mockInstance.getResponseEntity( "testRegistration.app-role-assignments", "token", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( map, HttpStatus.OK ) );

      List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", true );

      assertEquals( List.of( "role1", "role2" ), roles );
    }
  }

  @Test
  public void testGetAppRoleAssignmentsForUserNullResponse() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );
      when(
        mockInstance.getResponseEntity( "testRegistration.app-role-assignments", "token", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( null, HttpStatus.OK ) );

      List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", true );

      assertTrue( CollectionUtils.isEmpty( roles ) );
    }
  }

  @Test
  public void setUserRoles() {
    ITenant tenant = mock( ITenant.class );
    String userName = "testUser";
    String[] roles = {};

    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    handler.setUserRoles( tenant, userName, roles );

    verify( userRoleDao, times( 1 ) ).setUserRolesNoValidation( tenant, userName, roles );
  }

  @Test
  public void testGetAppRoleAssignmentsForUserEmptyRole() {
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    List<Map<String, Object>> responseBody = List.of(
      Map.of( "appRoleId", "role1" ),
      Map.of( "appRoleId", "" )
    );
    Map<String, Object> map = Map.of( "value", responseBody );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );
      when(
        mockInstance.getResponseEntity( "testRegistration.app-role-assignments", "token", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( map, HttpStatus.OK ) );

      List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", true );

      assertEquals( List.of( "role1", "" ), roles );
    }
  }

  @Test
  public void testGetAppRoleAssignmentsForUserException() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    Map<String, String> responseBody = Map.of( "access_token", "token" );
    when( restTemplate.postForEntity( any( String.class ), any( org.springframework.http.HttpEntity.class ),
      eq( Map.class ) ) ).thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", true );

    assertTrue( CollectionUtils.isEmpty( roles ) );
  }

  @Test
  public void testPerformSyncForUser() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    PentahoOAuthUser user = mock( PentahoOAuthUser.class );
    when( user.getRegistrationId() ).thenReturn( "testRegistration" );
    when( user.getUserId() ).thenReturn( "userId" );
    when( user.getTenant() ).thenReturn( mock( ITenant.class ) );
    when( user.isEnabled() ).thenReturn( true );

    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( any( String.class ), any( org.springframework.http.HttpEntity.class ),
      eq( Map.class ) ) ).thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      handler.performSyncForUser( user );

      verify( userRoleDao, times( 1 ) ).changeUserStatus( user );
      verify( userRoleDao, times( 0 ) ).setUserRolesNoValidation( user.getTenant(), "username",
        new String[] { "mappedRole1", "mappedRole2" } );
    }
  }

  @Test
  public void testPerformSyncForUserEnabled() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    PentahoOAuthUser user = mock( PentahoOAuthUser.class );
    when( user.getRegistrationId() ).thenReturn( "testRegistration" );
    when( user.getUserId() ).thenReturn( "userId" );
    when( user.getTenant() ).thenReturn( mock( ITenant.class ) );
    when( user.isEnabled() ).thenReturn( true );

    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( any( String.class ), any( org.springframework.http.HttpEntity.class ),
      eq( Map.class ) ) ).thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      Map<String, Object> responseBodyForAccountEnabled = Map.of( "accountEnabled", true );
      when(
        mockInstance.getResponseEntity( "testRegistration.account-enabled", "newToken", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( responseBodyForAccountEnabled, HttpStatus.OK ) );

      handler.performSyncForUser( user );

      verify( userRoleDao, times( 0 ) ).changeUserStatus( user );
      verify( userRoleDao, times( 0 ) ).setUserRolesNoValidation( user.getTenant(), "username",
        new String[] { "mappedRole1", "mappedRole2" } );
    }
  }

  @Test
  public void testPerformSyncForUserDisabled() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    PentahoOAuthUser user = mock( PentahoOAuthUser.class );
    when( user.getRegistrationId() ).thenReturn( "testRegistration" );
    when( user.getUserId() ).thenReturn( "userId" );
    when( user.getTenant() ).thenReturn( mock( ITenant.class ) );
    when( user.isEnabled() ).thenReturn( false );

    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( any( String.class ), any( HttpEntity.class ), eq( Map.class ) ) ).thenReturn(
      new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      Map<String, Object> responseBodyForAccountEnabled = Map.of( "accountEnabled", false );
      when(
        mockInstance.getResponseEntity( "testRegistration.account-enabled", "newToken", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( responseBodyForAccountEnabled, HttpStatus.OK ) );

      handler.performSyncForUser( user );

      verify( userRoleDao, times( 0 ) ).changeUserStatus( user );
      verify( userRoleDao, times( 0 ) ).setUserRolesNoValidation( user.getTenant(), "username",
        new String[] { "mappedRole1", "mappedRole2" } );
    }
  }

  @Test
  public void testPerformSyncForUserWithEmptyAppRoles() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    PentahoOAuthUser user = mock( PentahoOAuthUser.class );
    when( user.getRegistrationId() ).thenReturn( "testRegistration" );
    when( user.getUserId() ).thenReturn( "userId" );
    when( user.isEnabled() ).thenReturn( false );

    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( any( String.class ), any( HttpEntity.class ), eq( Map.class ) ) ).thenReturn(
      new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      Map<String, Object> responseBodyForAccountEnabled = Map.of( "accountEnabled", true );
      when(
        mockInstance.getResponseEntity( "testRegistration.account-enabled", "newToken", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( responseBodyForAccountEnabled, HttpStatus.OK ) );

      Map<String, Object> appRolesResponseBody = Map.of( "value", List.of(
        Map.of( "id", "roleId1", "value", "roleName1" ),
        Map.of( "id", "roleId2", "value", "roleName2" )
      ) );

      when( mockInstance.getResponseEntity( "testRegistration.app-roles", "newToken", "", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( appRolesResponseBody, HttpStatus.OK ) );

      when( mockInstance.getResponseEntity( "testRegistration.app-role-assignments", "newToken", "userId", true,
        Map.class ) )
        .thenReturn( new ResponseEntity<>( new HashMap<>(), HttpStatus.OK ) );

      handler.performSyncForUser( user );

      verify( userRoleDao, times( 1 ) ).changeUserStatus( user );
      verify( userRoleDao, times( 0 ) ).setUserRolesNoValidation( any(), any(), eq( new String[ 0 ] ) );
    }
  }

  @Test
  public void testPerformSyncForUserWithAppRoles() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    PentahoOAuthAzureHandler handler =
      new PentahoOAuthAzureHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    PentahoOAuthUser user = mock( PentahoOAuthUser.class );
    when( user.getRegistrationId() ).thenReturn( "testRegistration" );
    when( user.getUserId() ).thenReturn( "userId" );
    when( user.getTenant() ).thenReturn( mock( ITenant.class ) );
    when( user.isEnabled() ).thenReturn( false );

    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( any( String.class ), any( HttpEntity.class ), eq( Map.class ) ) ).thenReturn(
      new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      Map<String, Object> responseBodyForAccountEnabled = Map.of( "accountEnabled", true );
      when(
        mockInstance.getResponseEntity( "testRegistration.account-enabled", "newToken", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( responseBodyForAccountEnabled, HttpStatus.OK ) );

      Map<String, Object> appRolesResponseBody = Map.of( "value", List.of(
        Map.of( "id", "roleId1", "value", "roleName1" ),
        Map.of( "id", "roleId2", "value", "roleName2" )
      ) );

      when( mockInstance.getResponseEntity( "testRegistration.app-roles", "newToken", "", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( appRolesResponseBody, HttpStatus.OK ) );

      List<Map<String, Object>> rolesResponseBody = List.of(
        Map.of( "appRoleId", "role1" ),
        Map.of( "appRoleId", "role2" )
      );
      Map<String, Object> map = Map.of( "value", rolesResponseBody );

      when( mockInstance.getResponseEntity( "testRegistration.app-role-assignments", "newToken", "userId", true,
        Map.class ) )
        .thenReturn( new ResponseEntity<>( map, HttpStatus.OK ) );

      handler.performSyncForUser( user );

      verify( userRoleDao, times( 1 ) ).changeUserStatus( user );
      verify( userRoleDao, times( 1 ) ).setUserRolesNoValidation( any(), any(), eq( new String[ 0 ] ) );
    }
  }
}