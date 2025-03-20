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

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class PentahoOAuthUtility {

  private static final Logger LOGGER = LoggerFactory.getLogger( PentahoOAuthUtility.class );

  public static final String OAUTH = "oauth";

  public static final String DUALAUTH = "dualauth";

  public static final String REGISTRATION_ID = "registrationId";

  public static final String USER_ID = "userId";

  RestTemplate restTemplate = new RestTemplate();

  PentahoOAuthProperties pentahoOAuthProperties = new PentahoOAuthProperties();

  private static ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );

  private static boolean isProviderOAuth = false;

  private static boolean isProviderDualAuth = false;

  private static PentahoOAuthUtility instance;

  // Private constructor to prevent instantiation from outside
  private PentahoOAuthUtility() {
  }

  // Public method to provide access to the single instance
  public static PentahoOAuthUtility getInstance() {
    if ( instance == null ) {
      instance = new PentahoOAuthUtility();
    }
    return instance;
  }

  /**
   * Check if oauth is enabled in the system configuration - security.properties
   *
   * @return true if OAuth is enabled, false otherwise
   */
  public static boolean isOAuthEnabled() {

    IConfiguration config = systemConfig.getConfiguration( "security" );

    if ( !isProviderOAuth ) {
      try {
        String provider = config.getProperties().getProperty( "provider" );
        isProviderOAuth = StringUtils.equals( provider, OAUTH );
        isProviderDualAuth = StringUtils.equals( provider, DUALAUTH );
      } catch ( IOException e ) {
        isProviderOAuth = false;
      }
    }

    if ( !isProviderOAuth && isProviderDualAuth ) {
      config = systemConfig.getConfiguration( OAUTH );
      try {
        String provider1 = config.getProperties().getProperty( "provider1" );
        String provider2 = config.getProperties().getProperty( "provider2" );
        isProviderOAuth = StringUtils.equals( provider1, OAUTH ) || StringUtils.equals( provider2, OAUTH );
      } catch ( IOException e ) {
        isProviderOAuth = false;
      }
    }

    return isProviderOAuth;
  }

  /**
   * Check if dualauth is enabled in the system configuration - security.properties
   * And in applicationContext-spring-security-dualauth.properties, check if one of the providers is oauth
   *
   * @return true if OAuth is enabled, false otherwise
   */
  public static boolean isOAuthEnabledWithDualAuth() {

    IConfiguration config = systemConfig.getConfiguration( "security" );

    if ( !isProviderDualAuth ) {
      String provider = null;
      try {
        provider = config.getProperties().getProperty( "provider" );
      } catch ( IOException e ) {
        isProviderDualAuth = false;
      }
      isProviderDualAuth = StringUtils.equals( provider, DUALAUTH );
    }

    if ( isProviderDualAuth ) {
      config = systemConfig.getConfiguration( DUALAUTH );
      String provider1 = null;
      String provider2 = null;
      try {
        provider1 = config.getProperties().getProperty( "provider1" );
        provider2 = config.getProperties().getProperty( "provider2" );
      } catch ( IOException e ) {
        isProviderOAuth = false;
      }
      isProviderOAuth = StringUtils.equals( provider1, OAUTH ) || StringUtils.equals( provider2, OAUTH );
    }

    return isProviderOAuth;
  }

  /**
   * Check if live update is enabled in the system configuration - applicationContext-spring-security-oauth.properties
   *
   * @return true if live update is enabled, false otherwise
   */
  public static boolean shouldPerformLiveUpdate() {
    IConfiguration config = systemConfig.getConfiguration( OAUTH );
    try {
      return Boolean.parseBoolean( config.getProperties().getProperty( "shouldPerformLiveUpdate", "false" ) );
    } catch ( IOException e ) {
      return false;
    }
  }

  /**
   * Check if the authentication is of type UsernamePasswordAuthenticationToken or Basic Authentication when dualauth
   * is configured.
   *
   * @param httpServletRequest the HttpServletRequest object
   * @return true if the authentication is of type UsernamePasswordAuthenticationToken, false otherwise
   */
  public static boolean isUserNamePasswordAuthentication( HttpServletRequest httpServletRequest ) {
    return SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken
      || StringUtils.indexOf( httpServletRequest.getHeader( "Authorization" ), "Basic" ) == 0;
  }

  /**
   * Get the username attribute from the system configuration - applicationContext-spring-security-oauth.properties
   * The reason we need this attribute is to determine which parameter to rely on for the username field like name,
   * email etc., And end user has to ensure the value is unique always
   *
   * @param registrationId the registration ID
   * @return the user name attribute
   */
  public static String getUserNameAttribute( String registrationId ) {
    IConfiguration config = systemConfig.getConfiguration( OAUTH );
    try {
      return config.getProperties().getProperty( registrationId + ".user-name-attribute" );
    } catch ( IOException e ) {
      return null;
    }
  }

  /**
   * This is common function to handle all GET requests to the OAuth provider.
   * The GET request is sent to the URL specified in the properties file - applicationContext-spring-security-oauth
   * .properties
   *
   * @param key
   * @param clientCredentialsToken
   * @param userId
   * @param retry
   * @param responseType
   * @param <T>
   * @return
   */
  public <T> ResponseEntity<T> getResponseEntity( String key,
                                                  String clientCredentialsToken,
                                                  String userId,
                                                  boolean retry,
                                                  Class<T> responseType ) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth( clientCredentialsToken );
    HttpEntity<String> request = new HttpEntity<>( headers );
    String url = pentahoOAuthProperties.getValue( key ).replace( "{userId}", userId );

    try {
      return restTemplate.exchange( url, HttpMethod.GET, request, responseType );
    } catch ( Exception e ) {
      LOGGER.error( "Exception occurred while fetching data from OAuth", e );
      if ( retry ) {
        throw e;
      }
    }
    return null;
  }

}
