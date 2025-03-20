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
import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Map;
import java.util.Objects;

/**
 * Factory for creating a {@link ClientRegistration} object from a map of properties.
 * <p>
 * This class is used to create a {@link ClientRegistration} object from a map of properties specified in
 * applicationContext-spring-security-oauth.properties.
 * <p>
 * The properties are specified in the map defined in applicationContext-spring-security-oauth.xml and are used to
 * configure the {@link ClientRegistration} object.
 */
public class PentahoClientRegistrationFactory implements FactoryBean<ClientRegistration> {

  private Map<String, String> propertiesMap;
  private Map<String, Object> configurationMetadata;

  public PentahoClientRegistrationFactory( Map<String, String> propertiesMap ) {
    this.propertiesMap = propertiesMap;
  }

  public PentahoClientRegistrationFactory( Map<String, String> propertiesMap,
                                           Map<String, Object> configurationMetadata ) {
    this.propertiesMap = propertiesMap;
    this.configurationMetadata = configurationMetadata;
  }

  @Override
  public ClientRegistration getObject() {
    ClientRegistration.Builder clientRegistrationBuilder =
      ClientRegistration.withRegistrationId( propertiesMap.get( "registrationId" ) );

    if ( propertiesMap.containsKey( "clientId" ) ) {
      clientRegistrationBuilder.clientId( propertiesMap.get( "clientId" ) );
    }
    if ( propertiesMap.containsKey( "clientSecret" ) ) {
      clientRegistrationBuilder.clientSecret( Encr.decryptPassword( propertiesMap.get( "clientSecret" ) ) );
    }
    if ( propertiesMap.containsKey( "authorizationUri" ) ) {
      clientRegistrationBuilder.authorizationUri( propertiesMap.get( "authorizationUri" ) );
    }
    if ( propertiesMap.containsKey( "jwkSetUri" ) ) {
      clientRegistrationBuilder.jwkSetUri( propertiesMap.get( "jwkSetUri" ) );
    }
    if ( propertiesMap.containsKey( "tokenUri" ) ) {
      clientRegistrationBuilder.tokenUri( propertiesMap.get( "tokenUri" ) );
    }
    if ( propertiesMap.containsKey( "userInfoUri" ) ) {
      clientRegistrationBuilder.userInfoUri( propertiesMap.get( "userInfoUri" ) );
    }
    if ( propertiesMap.containsKey( "clientName" ) ) {
      clientRegistrationBuilder.clientName( propertiesMap.get( "clientName" ) );
    }
    if ( propertiesMap.containsKey( "scope" ) ) {
      clientRegistrationBuilder.scope(
        Objects.toString( propertiesMap.get( "scope" ), StringUtils.EMPTY ).split( "," ) );
    }
    if ( propertiesMap.containsKey( "redirectUri" ) ) {
      clientRegistrationBuilder.redirectUri( propertiesMap.get( "redirectUri" ) );
    }
    if ( propertiesMap.containsKey( "authorizationGrantType" ) ) {
      clientRegistrationBuilder.authorizationGrantType(
        new AuthorizationGrantType( propertiesMap.get( "authorizationGrantType" ) ) );
    }
    if ( propertiesMap.containsKey( "clientAuthenticationMethod" ) ) {
      clientRegistrationBuilder.clientAuthenticationMethod(
        new ClientAuthenticationMethod( propertiesMap.get( "clientAuthenticationMethod" ) ) );
    }
    if ( propertiesMap.containsKey( "userInfoAuthenticationMethod" ) ) {
      clientRegistrationBuilder.userInfoAuthenticationMethod(
        new AuthenticationMethod( propertiesMap.get( "userInfoAuthenticationMethod" ) ) );
    }
    if ( MapUtils.isNotEmpty( this.configurationMetadata ) ) {
      clientRegistrationBuilder.providerConfigurationMetadata( this.configurationMetadata );
    }
    if ( propertiesMap.containsKey( "userNameAttributeName" ) ) {
      clientRegistrationBuilder.userNameAttributeName( propertiesMap.get( "userNameAttributeName" ) );
    }
    return clientRegistrationBuilder.build();
  }

  @Override
  public Class<?> getObjectType() {
    return ClientRegistration.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}