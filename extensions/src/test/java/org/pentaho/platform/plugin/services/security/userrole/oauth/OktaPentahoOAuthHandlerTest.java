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
import org.springframework.http.HttpStatus;
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
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class OktaPentahoOAuthHandlerTest {

  @Mock
  IUserRoleDao userRoleDao;

  @Mock
  RestTemplate restTemplate;

  @Mock
  IAuthenticationRoleMapper oauthRoleMapper;

  @Mock
  PentahoOAuthProperties pentahoOAuthProperties;

  @Test
  public void getClientCredentialsTokenReturnsExistingTokenWhenNotRenewed() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    handler.registrationToClientCredentialsToken.put( "testRegistration", "existingToken" );

    String token = handler.getClientCredentialsToken( "testRegistration", false );

    assertEquals( "existingToken", token );
  }

  @Test
  public void getClientCredentialsTokenFetchesNewTokenWhenRenewed() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getClientId( "testRegistration" ) ).thenReturn( "clientId" );
    when( pentahoOAuthProperties.getClientSecret( "testRegistration" ) ).thenReturn( "clientSecret" );
    when( pentahoOAuthProperties.getTokenUri( "testRegistration" ) ).thenReturn( "http://token-uri" );
    Map<String, String> responseBody = Map.of( "access_token", "newToken" );
    when( restTemplate.postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    String token = handler.getClientCredentialsToken( "testRegistration", true );

    assertEquals( "newToken", token );
  }

  @Test
  public void getClientCredentialsTokenNullResponse() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
    when( pentahoOAuthProperties.getClientId( "testRegistration" ) ).thenReturn( "clientId" );
    when( pentahoOAuthProperties.getClientSecret( "testRegistration" ) ).thenReturn( "clientSecret" );
    when( pentahoOAuthProperties.getTokenUri( "testRegistration" ) ).thenReturn( "http://token-uri" );
    when( restTemplate.postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ), eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( null, HttpStatus.OK ) );

    String token = handler.getClientCredentialsToken( "testRegistration", true );

    assertNull( token );
  }

  @Test
  public void isUserAccountEnabledReturnsTrueWhenAccountIsEnabled() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "testRegistration" );

    Map<String, Object> responseBody = Map.of( "accountEnabled", true );
    when( restTemplate.exchange( anyString(), eq( HttpMethod.GET ), any( HttpEntity.class ), eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );

    boolean result = handler.isUserAccountEnabled( "testRegistration", "token", "userId", false );

    assertTrue( result );
  }

  @Test
  public void isUserAccountEnabledReturnsFalseWhenAccountIsDisabled() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    boolean result = handler.isUserAccountEnabled( "testRegistration", "token", "userId", false );

    assertFalse( result );
  }

  @Test
  public void isUserAccountEnabledReturnsNull() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "testRegistration" );

    when( restTemplate.exchange( anyString(), eq( HttpMethod.GET ), any( HttpEntity.class ), eq( Map.class ) ) )
      .thenReturn( new ResponseEntity<>( null, HttpStatus.OK ) );

    boolean result = handler.isUserAccountEnabled( "testRegistration", "token", "userId", false );

    assertFalse( result );
  }

  @Test
  public void isUserAccountEnabledReturnsThrowsException() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "testRegistration" );

    when( restTemplate.exchange( anyString(), eq( HttpMethod.GET ), any( HttpEntity.class ), eq( Map.class ) ) )
      .thenThrow( new RuntimeException( "Error during account status retrieval" ) );

    boolean result = handler.isUserAccountEnabled( "testRegistration", "token", "userId", false );

    assertFalse( result );
  }

  @Test
  public void isUserAccountEnabledReturnsThrowsExceptionRetry() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    when( pentahoOAuthProperties.getAccountEnabled( "testRegistration" ) ).thenReturn( "testRegistration" );

    when( restTemplate.exchange( anyString(), eq( HttpMethod.GET ), any( HttpEntity.class ), eq( Map.class ) ) )
      .thenThrow( new RuntimeException( "Error during account status retrieval" ) );

    assertThrows( IllegalArgumentException.class,
      () -> handler.isUserAccountEnabled( "testRegistration", "token", "userId", true ) );

  }

  @Test
  public void getAppRoleAssignmentsForUserReturnsMappedRoles() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    when( pentahoOAuthProperties.getAppRoleAssignment( "testRegistration" ) ).thenReturn( "testRegistration" );

    List<Map<String, Object>> responseBody = List.of(
      Map.of( "label", "role1" ),
      Map.of( "label", "role2" )
    );
    when( restTemplate.exchange( anyString(), eq( HttpMethod.GET ), any( HttpEntity.class ), eq( List.class ) ) )
      .thenReturn( new ResponseEntity<>( responseBody, HttpStatus.OK ) );
    when( oauthRoleMapper.toPentahoRole( "role1" ) ).thenReturn( "mappedRole1" );
    when( oauthRoleMapper.toPentahoRole( "role2" ) ).thenReturn( "mappedRole2" );

    List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", false );

    assertEquals( List.of( "mappedRole1", "mappedRole2" ), roles );
  }

  @Test
  public void getAppRoleAssignmentsForUserReturnsEmptyListWhenNoRoles() {
    OktaPentahoOAuthHandler handler =
      new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

    List<String> roles = handler.getAppRoleAssignmentsForUser( "testRegistration", "token", "userId", false );

    assertTrue( roles.isEmpty() );
  }

  @Test
  public void performSyncForUserUpdatesRoles() {
    try ( MockedStatic<Encr> mockedEncr = mockStatic( Encr.class ) ) {
      OktaPentahoOAuthHandler handler =
        new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );
      ITenant tenant = mock( ITenant.class );

      PentahoOAuthUser user = mock( PentahoOAuthUser.class );
      when( user.getUsername() ).thenReturn( "username" );
      when( user.isEnabled() ).thenReturn( true );
      when( user.getRegistrationId() ).thenReturn( "testRegistration" );
      when( user.getUserId() ).thenReturn( "userId" );
      when( user.getTenant() ).thenReturn( tenant );
      when( user.isEnabled() ).thenReturn( true );

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

      when( pentahoOAuthProperties.getAppRoleAssignment( "testRegistration" ) ).thenReturn(
        "http://app-role-assignments-uri" );

      mockedEncr.when( () -> Encr.decryptPassword( "clientSecret" ) ).thenReturn( "clientSecret" );
      handler.performSyncForUser( user );

      verify( restTemplate, times( 2 ) ).postForEntity( eq( "http://token-uri" ), any( HttpEntity.class ),
        eq( Map.class ) );
    }
  }

  @Test
  public void performSyncForUserUpdatesRolesUserDisabled() {
    try ( MockedStatic<Encr> mockedEncr = mockStatic( Encr.class ) ) {
      OktaPentahoOAuthHandler handler =
        new OktaPentahoOAuthHandler( userRoleDao, restTemplate, oauthRoleMapper, pentahoOAuthProperties );

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

}
