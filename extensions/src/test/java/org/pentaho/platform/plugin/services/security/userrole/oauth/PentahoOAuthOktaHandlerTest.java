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
public class PentahoOAuthOktaHandlerTest {

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
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    handler.registrationToClientCredentialsToken.put( "testRegistration", "existingToken" );

    String token = handler.getClientCredentialsToken( "testRegistration", false );

    assertEquals( "existingToken", token );
  }

  @Test
  public void testGetClientCredentialsNewToken() {
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getValue( "testRegistration.client-id" ) ).thenReturn( "clientId" );
    when( pentahoOAuthProperties.getValue( "testRegistration.client-secret" ) ).thenReturn( "clientSecret" );
    when( pentahoOAuthProperties.getValue( "testRegistration.token-uri" ) ).thenReturn( "http://token-uri" );
    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) ) ).thenReturn(
      new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    String token = handler.getClientCredentialsToken( "testRegistration", true );

    assertEquals( "newToken", token );
  }

  @Test
  public void testGetClientCredentialsNullResponse() {
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getValue( "testRegistration.client-id" ) ).thenReturn( "clientId" );
    when( pentahoOAuthProperties.getValue( "testRegistration.client-secret" ) ).thenReturn( "clientSecret" );
    when( pentahoOAuthProperties.getValue( "testRegistration.token-uri" ) ).thenReturn( "http://token-uri" );
    when( restTemplate.postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) ) ).thenReturn(
      new ResponseEntity<>( null, HttpStatus.OK ) );

    String token = handler.getClientCredentialsToken( "testRegistration", true );

    assertNull( token );
  }

  @Test
  public void testIsUserAccountEnabled() {
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    Map<String, Object> responseBody = Map.of( "accountEnabled", true );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      when( mockInstance.getResponseEntity( "testRegistration.account-enabled", "token", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );

      boolean result = handler.isUserAccountEnabled( "testRegistration", "token", "userId", true );

      assertTrue( result );
    }
  }

  @Test
  public void testIsUserAccountEnabledForExceptions() {
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      when(
        mockInstance.getResponseEntity( "testRegistration.account-enabled", "newToken", "userId", true, Map.class ) )
        .thenReturn( new ResponseEntity<>( null, HttpStatus.OK ) );

      boolean result = handler.isUserAccountEnabled( "testRegistration", "newToken", "userId", true );

      assertFalse( result );
    }
  }

  @Test
  public void testGetAppRoleAssignmentsForUser() {
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    List<Map<String, Object>> responseBody = List.of(
      Map.of( "label", "role1" ),
      Map.of( "label", "role2" )
    );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );
      when(
        mockInstance.getResponseEntity( "testRegistration.app-role-assignments", "token", "userId", true, List.class ) )
        .thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );
      when( oauthRoleMapper.toPentahoRole( "role1" ) ).thenReturn( "mappedRole1" );
      when( oauthRoleMapper.toPentahoRole( "role2" ) ).thenReturn( "mappedRole2" );

      List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", true );

      assertEquals( List.of( "mappedRole1", "mappedRole2" ), roles );
    }
  }

  @Test
  public void testGetAppRoleAssignmentsForUserEmptyRole() {
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    List<Map<String, Object>> responseBody = List.of(
      Map.of( "label", "role1" ),
      Map.of( "label", "" )
    );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );
      when(
        mockInstance.getResponseEntity( "testRegistration.app-role-assignments", "token", "userId", true, List.class ) )
        .thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );
      when( oauthRoleMapper.toPentahoRole( "role1" ) ).thenReturn( "mappedRole1" );

      List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", true );

      assertEquals( List.of( "mappedRole1" ), roles );
    }
  }

  @Test
  public void testGetAppRoleAssignmentsForUserException() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    Map<String, String> responseBody = Map.of( "access_token", "token" );
    when( restTemplate.postForEntity( any( String.class ), any( HttpEntity.class ), eq( Map.class ) ) ).thenReturn(
      new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", true );

    assertTrue( CollectionUtils.isEmpty( roles ) );
  }

  @Test
  public void testPerformSyncForUser() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    PentahoOAuthUser user = mock( PentahoOAuthUser.class );
    when( user.getRegistrationId() ).thenReturn( "testRegistration" );
    when( user.getUserId() ).thenReturn( "userId" );
    when( user.getTenant() ).thenReturn( mock( ITenant.class ) );
    when( user.isEnabled() ).thenReturn( true );

    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( any( String.class ), any( HttpEntity.class ), eq( Map.class ) ) ).thenReturn(
      new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      // Create a mock of the Singleton class
      PentahoOAuthUtility mockInstance = mock( PentahoOAuthUtility.class );

      // Define behavior for the static method getInstance()
      pentahoOAuthUtility.when( PentahoOAuthUtility::getInstance ).thenReturn( mockInstance );

      handler.performSyncForUser( user );

      verify( userRoleDao, times( 1 ) ).changeUserStatus( user );
      verify( userRoleDao, times( 0 ) ).setUserRoles( user.getTenant(), "username",
        new String[] { "mappedRole1", "mappedRole2" } );
    }
  }

  @Test
  public void testPerformSyncForUserEnabled() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    PentahoOAuthUser user = mock( PentahoOAuthUser.class );
    when( user.getRegistrationId() ).thenReturn( "testRegistration" );
    when( user.getUserId() ).thenReturn( "userId" );
    when( user.getTenant() ).thenReturn( mock( ITenant.class ) );
    when( user.isEnabled() ).thenReturn( true );

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

      handler.performSyncForUser( user );

      verify( userRoleDao, times( 0 ) ).changeUserStatus( user );
      verify( userRoleDao, times( 0 ) ).setUserRoles( user.getTenant(), "username",
        new String[] { "mappedRole1", "mappedRole2" } );
    }
  }

  @Test
  public void testPerformSyncForUserDisabled() {
    when( pentahoOAuthProperties.getValue( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
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
      verify( userRoleDao, times( 0 ) ).setUserRoles( user.getTenant(), "username",
        new String[] { "mappedRole1", "mappedRole2" } );
    }
  }

  @Test
  public void testSetUserRoles() {
    PentahoOAuthOktaHandler handler =
      new PentahoOAuthOktaHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    ITenant tenant = mock( ITenant.class );
    String userName = "testUser";
    String[] roles = { "role1", "role2" };

    handler.setUserRoles( tenant, userName, roles );

    verify( userRoleDao ).setUserRoles( tenant, userName, roles );
  }

}
