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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PentahoOAuthOktaHandler implements IPentahoOAuthHandler {

  IUserRoleDao userRoleDao;

  RestTemplate restTemplate;

  IAuthenticationRoleMapper oauthRoleMapper;

  PentahoOAuthProperties pentahoOAuthProperties;

  Map<String, String> registrationToClientCredentialsToken = new HashMap<>();

  public PentahoOAuthOktaHandler( IUserRoleDao userRoleDao,
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
      headers.setBasicAuth( pentahoOAuthProperties.getValue( registrationId + ".client-id" ),
              pentahoOAuthProperties.getValue( registrationId + ".client-secret" ) );

      MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
      map.add( "grant_type", "client_credentials" );
      map.add( "scope", "api.read" );

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

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

  @Override
  public boolean isUserAccountEnabled( String registrationId,
                                       String clientCredentialsToken,
                                       String userId,
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
      var responseEntity = PentahoOAuthUtility.getInstance().getResponseEntity( registrationId + ".app-role-assignments", clientCredentialsToken, userId, retry, List.class );

      // Parse the JSON string into a JsonNode array
      List<Map<String, Object>> dataList = responseEntity.getBody();

      // Step 3: Loop through the roles and extract the "label" (role name)
      for ( Map<String, Object> item : dataList ) {
        String roleLabel = (String) item.get( "label" ); // Extracting the "label" field which represents the role name
        if ( StringUtils.isNotBlank( roleLabel ) ) {
          roles.add( oauthRoleMapper.toPentahoRole( roleLabel ) );
        }
      }

    } catch ( Exception e ) {
      if ( retry ) {
        return this.getAppRoleAssignmentsForUser( registrationId, this.getClientCredentialsToken( registrationId, true ), userId, false );
      }
    }

    // Map each item in the 'value' array to an Item object containing only id and desc
    return roles;
  }

  @Override
  public void setUserRoles(ITenant tenant, String userName, String[] roles ) {
    userRoleDao.setUserRoles( tenant, userName, roles );
  }

  @Override
  public void performSyncForUser( PentahoOAuthUser pentahoUser ) {
    String registrationId = pentahoUser.getRegistrationId();

    String clientCredentialsToken = this.getClientCredentialsToken( registrationId, false );

    boolean isUserAccountEnabled = this.isUserAccountEnabled( registrationId, clientCredentialsToken, pentahoUser.getUserId(), true );

    if ( pentahoUser.isEnabled() != isUserAccountEnabled ) {
      userRoleDao.changeUserStatus( pentahoUser );
    }

    if ( !isUserAccountEnabled ) {
      return;
    }

    List<String> oauthRoleIds = this.getAppRoleAssignmentsForUser( registrationId, clientCredentialsToken, pentahoUser.getUserId(), true );

    String[] pentahoRoles = oauthRoleIds.stream()
            .filter( Objects::nonNull )
            .map( oauthRoleMapper::toPentahoRole )
            .toArray( String[]::new );

    userRoleDao.setUserRoles( pentahoUser.getTenant(), pentahoUser.getUsername(), pentahoRoles );
  }

}
