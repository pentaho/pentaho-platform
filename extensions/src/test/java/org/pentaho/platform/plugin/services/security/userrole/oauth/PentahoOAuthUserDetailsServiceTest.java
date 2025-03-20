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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PentahoOAuthUserDetailsServiceTest {

  @Mock
  IUserRoleListService userRoleListService;

  @Test
  public void testLoadUserByUsername() {
    PentahoOAuthUserDetailsService pentahoOAuthUserDetailsService =
      spy( new PentahoOAuthUserDetailsService( userRoleListService ) );

    Authentication authentication = mock( Authentication.class );

    when( authentication.getName() ).thenReturn( "JOE" );
    when( userRoleListService.getRolesForUser( null, authentication.getName() ) ).thenReturn(
      List.of( "Administrator", "Authenticated" ) );

    try ( MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic( SecurityContextHolder.class );
          MockedStatic<Encr> mockedEncr = mockStatic( Encr.class );
          MockedStatic<PentahoSystem> pentahoSystemMockedStatic = mockStatic( PentahoSystem.class ); ) {
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( String.class, "systemTenantAdminPassword", null ) )
        .thenReturn( "password" );
      mockedEncr.when( () -> Encr.decryptPassword( "password" ) ).thenReturn( "password" );
      SecurityContext securityContext = mock( SecurityContext.class );
      mockedSecurityContextHolder.when( SecurityContextHolder::getContext ).thenReturn( securityContext );
      when( securityContext.getAuthentication() ).thenReturn( authentication );

      UserDetails userDetails = pentahoOAuthUserDetailsService.loadUserByUsername( "JOE" );

      // Assert that the returned UserDetails contains the expected roles
      List<String> expectedRoles = List.of( "Administrator", "Authenticated" );
      List<String> actualRoles = userDetails.getAuthorities().stream()
        .map( GrantedAuthority::getAuthority )
        .collect( Collectors.toList() );
      Assert.assertEquals( expectedRoles, actualRoles );
    }
  }

  @Test
  public void testLoadUserByUsernameForAdmin() {
    PentahoOAuthUserDetailsService pentahoOAuthUserDetailsService =
      spy( new PentahoOAuthUserDetailsService( userRoleListService ) );

    try ( MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic( SecurityContextHolder.class );
          MockedStatic<Encr> mockedEncr = mockStatic( Encr.class );
          MockedStatic<PentahoSystem> pentahoSystemMockedStatic = mockStatic( PentahoSystem.class ); ) {
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( String.class, "systemTenantAdminPassword", null ) )
        .thenReturn( "password" );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.getSystemSetting( "acl-voter/admin-role", "Administrator" ) )
        .thenReturn( "Administrator" );
      mockedEncr.when( () -> Encr.decryptPassword( "password" ) ).thenReturn( "password" );
      SecurityContext securityContext = mock( SecurityContext.class );
      mockedSecurityContextHolder.when( SecurityContextHolder::getContext ).thenReturn( securityContext );
      // Load user details for "admin"
      UserDetails adminUserDetails = pentahoOAuthUserDetailsService.loadUserByUsername( "admin" );
      List<String> expectedAdminRoles = List.of( "Administrator" );
      List<String> actualAdminRoles = adminUserDetails.getAuthorities().stream()
        .map( GrantedAuthority::getAuthority )
        .collect( Collectors.toList() );
      Assert.assertEquals( expectedAdminRoles, actualAdminRoles );

      // Load user details for "admin-/pentaho/tenant0"
      UserDetails tenantAdminUserDetails =
        pentahoOAuthUserDetailsService.loadUserByUsername( "admin-/pentaho/tenant0" );
      List<String> expectedTenantAdminRoles = List.of( "Administrator" );
      List<String> actualTenantAdminRoles = tenantAdminUserDetails.getAuthorities().stream()
        .map( GrantedAuthority::getAuthority )
        .collect( Collectors.toList() );
      Assert.assertEquals( expectedTenantAdminRoles, actualTenantAdminRoles );
    }
  }

}
