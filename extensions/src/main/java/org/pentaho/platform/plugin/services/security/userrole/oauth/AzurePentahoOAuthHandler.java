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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.security.userroledao.PentahoOAuthUser;
import org.pentaho.platform.util.oauth.PentahoOAuthProperties;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AzurePentahoOAuthHandler is responsible for handling Azure specific OAuth operations.
 * It retrieves client credentials tokens, app roles, and user account status from Azure.
 * <p>
 * These information is used to sync user roles in jackrabbit and to change the user status as active or inactive
 */
public class AzurePentahoOAuthHandler implements PentahoOAuthHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger( AzurePentahoOAuthHandler.class );

  private static final String ID = "id";

  private static final String VALUE = "value";

  IUserRoleDao userRoleDao;

  RestTemplate restTemplate;

  IAuthenticationRoleMapper oauthRoleMapper;

  PentahoOAuthProperties pentahoOAuthProperties;

  Map<String, String> registrationToClientCredentialsToken = new HashMap<>();

  Map<String, Map<String, String>> registrationToAppRoles = new HashMap<>();


  public AzurePentahoOAuthHandler( IUserRoleDao userRoleDao,
                                   RestTemplate restTemplate,
                                   IAuthenticationRoleMapper oauthRoleMapper,
                                   PentahoOAuthProperties pentahoOAuthProperties ) {
    this.userRoleDao = userRoleDao;
    this.restTemplate = restTemplate;
    this.oauthRoleMapper = oauthRoleMapper;
    this.pentahoOAuthProperties = pentahoOAuthProperties;
  }

  /**
   * Retrieves the client credentials token for the given registration ID.
   * If renewToken is true or the token is not present, a new token is requested.
   * <p>
   * A map is used to store the tokens for each registration ID (for handling multiple IDPs)
   *
   * @param registrationId the registration ID
   * @param renewToken     whether to renew the token
   * @return the client credentials token
   */
  public String getClientCredentialsToken( String registrationId, boolean renewToken ) {
    String clientCredentialsToken = registrationToClientCredentialsToken.get( registrationId );

    if ( renewToken || StringUtils.isBlank( clientCredentialsToken ) ) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );

      MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
      map.add( PentahoOAuthUtility.CLIENT_ID, pentahoOAuthProperties.getClientId( registrationId ) );
      map.add( PentahoOAuthUtility.CLIENT_SECRET,
        Encr.decryptPassword( pentahoOAuthProperties.getClientSecret( registrationId ) ) );
      map.add( PentahoOAuthUtility.GRANT_TYPE, pentahoOAuthProperties.getClientCredentialsGrantType( registrationId ) );
      map.add( PentahoOAuthUtility.REDIRECT_URI, pentahoOAuthProperties.getRedirectUri( registrationId ) );
      map.add( PentahoOAuthUtility.SCOPE, pentahoOAuthProperties.getClientCredentialsScope( registrationId ) );

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>( map, headers );

      String url = pentahoOAuthProperties.getTokenUri( registrationId );
      var responseEntity = restTemplate.postForEntity( url, request, Map.class );
      var responseEntityBody = responseEntity.getBody();

      if ( Objects.nonNull( responseEntityBody ) ) {
        clientCredentialsToken = (String) responseEntityBody.get( PentahoOAuthUtility.ACCESS_TOKEN );
        registrationToClientCredentialsToken.put( registrationId, clientCredentialsToken );
      }

    }

    return clientCredentialsToken;
  }

  /**
   * Retrieves the app roles from the IDP for the given registration ID.
   * If the app roles are not present, they are requested and stored in a map.
   * <p>
   * A map is used to store the app roles for each registration ID (for handling multiple IDPs)
   * <p>
   * The basis of this request is based on client credentials token
   *
   * @param registrationId         the registration ID
   * @param clientCredentialsToken the client credentials token
   * @param retry                  whether to retry if an error occurs
   * @return a map of app roles
   */
  public Map<String, String> getAppRolesInIdp( String registrationId, String clientCredentialsToken, boolean retry ) {
    var appRoles = registrationToAppRoles.get( registrationId );
    if ( MapUtils.isEmpty( appRoles ) ) {
      try {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth( clientCredentialsToken );
        HttpEntity<String> request = new HttpEntity<>( headers );

        var responseEntity =
          restTemplate.exchange( pentahoOAuthProperties.getAppRoles( registrationId ), HttpMethod.GET, request,
            Map.class );
        var responseEntityBody = responseEntity.getBody();

        if ( Objects.isNull( responseEntityBody ) ) {
          return appRoles;
        }

        // Extract the 'value' array from the response map
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) responseEntityBody.get( VALUE );

        // Map each item in the 'value' array to an Item object containing only id and desc
        appRoles = valueList.stream()
          .collect( Collectors.toMap( item -> (String) item.get( ID ), item -> (String) item.get( VALUE ) ) );
        registrationToAppRoles.put( registrationId, appRoles );
      } catch ( Exception e ) {
        if ( retry ) {
          return getAppRolesInIdp( registrationId, getClientCredentialsToken( registrationId, true ), false );
        }
        LOGGER.error( "Exception Occurred in getAppRolesInIdp ", e );
      }
    }

    return appRoles;
  }

  /**
   * Checks if the user account is enabled in the IDP for the given registration ID.
   * If the account status is not present, it is requested.
   * <p>
   * The basis of this request is based on client credentials token
   *
   * @param registrationId         the registration ID
   * @param clientCredentialsToken the client credentials token
   * @param userId                 the user ID
   * @param retry                  whether to retry if an error occurs
   * @return true if the user account is enabled, false otherwise
   */
  @Override
  public boolean isUserAccountEnabled( String registrationId, String clientCredentialsToken, String userId,
                                       boolean retry ) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth( clientCredentialsToken );
      HttpEntity<String> request = new HttpEntity<>( headers );

      var url = pentahoOAuthProperties.getAccountEnabled( registrationId );
      url = url.replace( "{userId}", userId );

      var responseEntity =
        restTemplate.exchange( url, HttpMethod.GET, request, Map.class );
      var responseEntityBody = responseEntity.getBody();

      if ( Objects.isNull( responseEntityBody ) ) {
        return false;
      }

      return (Boolean) responseEntityBody.get( "accountEnabled" );
    } catch ( Exception e ) {
      if ( retry ) {
        return isUserAccountEnabled( registrationId, getClientCredentialsToken( registrationId, true ), userId, false );
      }
      LOGGER.error( "Exception Occurred in isUserAccountEnabled ", e );
    }

    return false;
  }

  /**
   * Retrieves the app role assignments for the user from the IDP for the given registration ID.
   * If the app role assignments are not present, they are requested.
   * <p>
   * The basis of this request is based on client credentials token
   *
   * @param registrationId         the registration ID
   * @param clientCredentialsToken the client credentials token
   * @param userId                 the user ID
   * @param retry                  whether to retry if an error occurs
   * @return a list of app role assignments
   */
  @Override
  public List<String> getAppRoleAssignmentsForUser( String registrationId,
                                                    String clientCredentialsToken,
                                                    String userId,
                                                    boolean retry ) {

    List<String> roles = new ArrayList<>();

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth( clientCredentialsToken );
      HttpEntity<String> request = new HttpEntity<>( headers );

      var url = pentahoOAuthProperties.getAppRoleAssignment( registrationId );
      url = url.replace( "{userId}", userId );

      var responseEntity = restTemplate.exchange( url, HttpMethod.GET, request, Map.class );

      var responseEntityBody = responseEntity.getBody();

      if ( Objects.isNull( responseEntityBody ) ) {
        return Collections.emptyList();
      }

      // Extract the 'value' array from the response map
      var valueList = (List<Map<String, Object>>) responseEntityBody.get( VALUE );

      // Map each item in the 'value' array to an Item object containing only id and desc
      roles.addAll( valueList.stream().map( item -> (String) item.get( "appRoleId" ) ).collect( Collectors.toList() ) );
    } catch ( Exception e ) {
      if ( retry ) {
        return getAppRoleAssignmentsForUser( registrationId, getClientCredentialsToken( registrationId, true ), userId,
          false );
      }
      LOGGER.error( "Exception Occurred in getAppRoleAssignmentsForUser ", e );
    }
    return roles;
  }

  /**
   * Sets the user roles in the jackrabbit system.
   * <p>
   * This method is used to set the roles for a user in the system.
   *
   * @param tenant   the tenant
   * @param userName the username
   * @param roles    the roles to set
   */
  @Override
  public void setUserRoles( ITenant tenant, String userName, String[] roles ) {
    userRoleDao.setUserRolesNoValidation( tenant, userName, roles );
  }

  /**
   * Performs the sync for the user.
   * <p>
   * This method is used to perform the sync for the user.
   *
   * @param pentahoUser the pentaho user
   */
  @Override
  public void performSyncForUser( PentahoOAuthUser pentahoUser ) {
    String registrationId = pentahoUser.getRegistrationId();

    String clientCredentialsToken = getClientCredentialsToken( registrationId, false );

    boolean isUserAccountEnabled =
      isUserAccountEnabled( registrationId, clientCredentialsToken, pentahoUser.getUserId(), true );

    if ( pentahoUser.isEnabled() != isUserAccountEnabled ) {
      userRoleDao.changeUserStatus( pentahoUser );
    }

    if ( !isUserAccountEnabled ) {
      return;
    }

    Map<String, String> pentahoOAuthAzureAppRoles = getAppRolesInIdp( registrationId, clientCredentialsToken, true );
    List<String> oauthRoleIds =
      getAppRoleAssignmentsForUser( registrationId, clientCredentialsToken, pentahoUser.getUserId(), true );

    if ( MapUtils.isEmpty( pentahoOAuthAzureAppRoles ) || oauthRoleIds.isEmpty() ) {
      return;
    }

    String[] pentahoRoles = oauthRoleIds.stream()
      .map( pentahoOAuthAzureAppRoles::get )
      .filter( Objects::nonNull )
      .map( oauthRoleMapper::toPentahoRole )
      .toArray( String[]::new );

    setUserRoles( pentahoUser.getTenant(), pentahoUser.getUsername(), pentahoRoles );
  }

}
