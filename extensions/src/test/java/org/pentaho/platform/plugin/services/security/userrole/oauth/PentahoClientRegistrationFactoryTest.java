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
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PentahoClientRegistrationFactoryTest {

  @Mock
  private ClientRegistration.Builder clientRegistrationBuilder;  // Mock the builder

  @Mock
  private ClientRegistration mockClientRegistration;  // Mock the ClientRegistration

  @Test
  public void testGetObject() {
    try ( MockedStatic<Encr> mockedEncr = mockStatic( Encr.class ) ) {
      mockedEncr.when( () -> Encr.decryptPassword( "@B%" ) ).thenReturn( "B" );
      Map<String, String> propertiesMap = new HashMap<>();
      propertiesMap.put( "registrationId", "azure" );
      propertiesMap.put( "clientId", "A" );
      propertiesMap.put( "clientSecret", "@B%" );
      propertiesMap.put( "authorizationUri", "C" );
      propertiesMap.put( "jwkSetUri", "D" );
      propertiesMap.put( "tokenUri", "E" );
      propertiesMap.put( "userInfoUri", "F" );
      propertiesMap.put( "clientName", "G" );
      propertiesMap.put( "redirectUri", "H" );
      propertiesMap.put( "authorizationGrantType", "authorization_code" );
      propertiesMap.put( "clientAuthenticationMethod", "I" );
      propertiesMap.put( "userInfoAuthenticationMethod", "J" );
      propertiesMap.put( "userNameAttributeName", "K" );
      propertiesMap.put( "scope", "L" );

      PentahoClientRegistrationFactory pentahoClientRegistrationFactory =
        new PentahoClientRegistrationFactory( propertiesMap );

      ClientRegistration clientRegistration = pentahoClientRegistrationFactory.getObject();
      assertNotNull( clientRegistration );
      assertEquals( ClientRegistration.class, pentahoClientRegistrationFactory.getObjectType() );
      assertTrue( pentahoClientRegistrationFactory.isSingleton() );
      assertEquals( "azure", clientRegistration.getRegistrationId() );
      assertEquals( "A", clientRegistration.getClientId() );
      assertEquals( "B", clientRegistration.getClientSecret() );
      assertEquals( "C", clientRegistration.getProviderDetails().getAuthorizationUri() );
      assertEquals( "D", clientRegistration.getProviderDetails().getJwkSetUri() );
      assertEquals( "E", clientRegistration.getProviderDetails().getTokenUri() );
      assertEquals( "F", clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri() );
      assertEquals( "G", clientRegistration.getClientName() );
      assertEquals( "H", clientRegistration.getRedirectUri() );
      assertEquals( "authorization_code", clientRegistration.getAuthorizationGrantType().getValue() );
      assertEquals( "I", clientRegistration.getClientAuthenticationMethod().getValue() );
      assertEquals( "J",
        clientRegistration.getProviderDetails().getUserInfoEndpoint().getAuthenticationMethod().getValue() );
      assertEquals( "K", clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName() );
      assertTrue( clientRegistration.getScopes().contains( "L" ) );
    }
  }

  @Test
  public void testGetObjectWithConfigMetadata() {
    try ( MockedStatic<Encr> mockedEncr = mockStatic( Encr.class ) ) {
      mockedEncr.when( () -> Encr.decryptPassword( "@B%" ) ).thenReturn( "B" );
      Map<String, String> propertiesMap = new HashMap<>();
      propertiesMap.put( "registrationId", "azure" );
      propertiesMap.put( "clientId", "A" );
      propertiesMap.put( "clientSecret", "@B%" );
      propertiesMap.put( "authorizationUri", "C" );
      propertiesMap.put( "jwkSetUri", "D" );
      propertiesMap.put( "tokenUri", "E" );
      propertiesMap.put( "userInfoUri", "F" );
      propertiesMap.put( "clientName", "G" );
      propertiesMap.put( "redirectUri", "H" );
      propertiesMap.put( "authorizationGrantType", "authorization_code" );
      propertiesMap.put( "clientAuthenticationMethod", "I" );
      propertiesMap.put( "userInfoAuthenticationMethod", "J" );
      propertiesMap.put( "userNameAttributeName", "K" );
      propertiesMap.put( "scope", "L" );

      Map<String, Object> configurationMetadata = new HashMap<>();
      configurationMetadata.put( "configurationMetadata", "yes" );

      PentahoClientRegistrationFactory pentahoClientRegistrationFactory =
        new PentahoClientRegistrationFactory( propertiesMap, configurationMetadata );

      ClientRegistration clientRegistration = pentahoClientRegistrationFactory.getObject();
      assertNotNull( clientRegistration );
      assertEquals( ClientRegistration.class, pentahoClientRegistrationFactory.getObjectType() );
      assertTrue( pentahoClientRegistrationFactory.isSingleton() );
      assertEquals( "azure", clientRegistration.getRegistrationId() );
      assertEquals( "A", clientRegistration.getClientId() );
      assertEquals( "B", clientRegistration.getClientSecret() );
      assertEquals( "C", clientRegistration.getProviderDetails().getAuthorizationUri() );
      assertEquals( "D", clientRegistration.getProviderDetails().getJwkSetUri() );
      assertEquals( "E", clientRegistration.getProviderDetails().getTokenUri() );
      assertEquals( "F", clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri() );
      assertEquals( "G", clientRegistration.getClientName() );
      assertEquals( "H", clientRegistration.getRedirectUri() );
      assertEquals( "authorization_code", clientRegistration.getAuthorizationGrantType().getValue() );
      assertEquals( "I", clientRegistration.getClientAuthenticationMethod().getValue() );
      assertEquals( "J",
        clientRegistration.getProviderDetails().getUserInfoEndpoint().getAuthenticationMethod().getValue() );
      assertEquals( "K", clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName() );
      assertTrue( clientRegistration.getScopes().contains( "L" ) );
      assertEquals( "yes",
        clientRegistration.getProviderDetails().getConfigurationMetadata().get( "configurationMetadata" ) );
    }
  }

  @Test
  public void testGetObjectWithEmptyProperties() {
    Map<String, String> propertiesMap = new HashMap<>();
    Map<String, Object> configurationMetadata = new HashMap<>();

    PentahoClientRegistrationFactory pentahoClientRegistrationFactory =
      new PentahoClientRegistrationFactory( propertiesMap, configurationMetadata );

    try ( MockedStatic<ClientRegistration> mockedClientRegistrationStatic = mockStatic( ClientRegistration.class ) ) {
      mockedClientRegistrationStatic.when( () -> ClientRegistration.withRegistrationId( null ) )
        .thenReturn( clientRegistrationBuilder );

      when( clientRegistrationBuilder.build() ).thenReturn( mockClientRegistration );

      ClientRegistration clientRegistration = pentahoClientRegistrationFactory.getObject();
      assertNotNull( clientRegistration );
    }
  }
}
