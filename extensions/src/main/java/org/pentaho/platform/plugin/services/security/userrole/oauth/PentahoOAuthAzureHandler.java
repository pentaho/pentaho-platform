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
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.security.userroledao.PentahoOAuthUser;
import org.pentaho.platform.util.oauth.PentahoOAuthProperties;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class PentahoOAuthAzureHandler implements IPentahoOAuthHandler {

  private static final String VALUE = "value";

  IUserRoleDao userRoleDao;

  RestTemplate restTemplate;

  IAuthenticationRoleMapper oauthRoleMapper;

  PentahoOAuthProperties pentahoOAuthProperties;

  Map<String, String> registrationToClientCredentialsToken = new HashMap<>();

  Map<String, Map<String, String>> registrationToAppRoles = new HashMap<>();


  public PentahoOAuthAzureHandler( IUserRoleDao userRoleDao,
                                   RestTemplate restTemplate,
                                   IAuthenticationRoleMapper oauthRoleMapper,
                                   PentahoOAuthProperties pentahoOAuthProperties ) {
    this.userRoleDao = userRoleDao;
    this.restTemplate = restTemplate;
    this.oauthRoleMapper = oauthRoleMapper;
    this.pentahoOAuthProperties = pentahoOAuthProperties;
  }

  public String getClientCredentialsToken( String registrationId, boolean renewToken ) {
    String clientCredentialsToken = registrationToClientCredentialsToken.get( registrationId );

    if ( renewToken || StringUtils.isBlank( clientCredentialsToken ) ) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );

      MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
      map.add( "client_id", pentahoOAuthProperties.getValue( registrationId + ".client-id" ) );
      map.add( "client_secret", pentahoOAuthProperties.getValue( registrationId + ".client-secret" ) );
      map.add( "grant_type", "client_credentials" );
      map.add( "redirect_uri", pentahoOAuthProperties.getValue( registrationId + ".redirect-uri" ) );
      map.add( "scope", "https://graph.microsoft.com/.default" );

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>( map, headers );

      String url = pentahoOAuthProperties.getValue( registrationId + ".token-uri" );
      var responseEntity = restTemplate.postForEntity( url, request, Map.class );
      var responseEntityBody = responseEntity.getBody();

      if ( Objects.nonNull( responseEntityBody ) ) {
        clientCredentialsToken = (String) responseEntityBody.get( "access_token" );
        registrationToClientCredentialsToken.put( registrationId, clientCredentialsToken );
      }

    }

    return clientCredentialsToken;
  }

  public Map<String, String> getAppRolesInIdp( String registrationId, String clientCredentialsToken, boolean retry ) {
    var appRoles = registrationToAppRoles.get( registrationId );
    if ( MapUtils.isEmpty( appRoles ) ) {
      try {
        var responseEntity = PentahoOAuthUtility.getInstance().getResponseEntity( registrationId + ".app-roles",
          clientCredentialsToken,
          "",
          retry,
          Map.class );
        var responseEntityBody = responseEntity.getBody();

        if ( Objects.isNull( responseEntityBody ) ) {
          return appRoles;
        }

        // Extract the 'value' array from the response map
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) responseEntityBody.get( VALUE );

        // Map each item in the 'value' array to an Item object containing only id and desc
        appRoles = valueList.stream()
          .collect( Collectors.toMap( item -> (String) item.get( "id" ), item -> (String) item.get( VALUE ) ) );
        registrationToAppRoles.put( registrationId, appRoles );
      } catch ( Exception e ) {
        if ( retry ) {
          return getAppRolesInIdp( registrationId, getClientCredentialsToken( registrationId, true ), false );
        }
      }
    }

    return appRoles;
  }

  @Override
  public boolean isUserAccountEnabled( String registrationId, String clientCredentialsToken, String userId,
                                       boolean retry ) {
    try {
      var responseEntity = PentahoOAuthUtility.getInstance().getResponseEntity( registrationId + ".account-enabled",
        clientCredentialsToken,
        userId,
        retry,
        Map.class );
      var responseEntityBody = responseEntity.getBody();

      if ( Objects.isNull( responseEntityBody ) ) {
        return false;
      }

      return (Boolean) responseEntityBody.get( "accountEnabled" );
    } catch ( Exception e ) {
      if ( retry ) {
        return isUserAccountEnabled( registrationId, getClientCredentialsToken( registrationId, true ), userId, false );
      }
    }

    return false;
  }

  @Override
  public List<String> getAppRoleAssignmentsForUser( String registrationId,
                                                    String clientCredentialsToken,
                                                    String userId,
                                                    boolean retry ) {

    List<String> roles = new ArrayList<>();

    try {
      var responseEntity =
        PentahoOAuthUtility.getInstance().getResponseEntity( registrationId + ".app-role-assignments",
          clientCredentialsToken,
          userId,
          retry,
          Map.class );
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
    }
    return roles;
  }

  @Override
  public void setUserRoles( ITenant tenant, String userName, String[] roles ) {
    userRoleDao.setUserRolesNoValidation( tenant, userName, roles );
  }

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
