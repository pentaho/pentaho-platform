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


package org.pentaho.platform.plugin.services.security.userrole;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.security.userrole.oauth.PentahoOAuthUserSync;
import org.pentaho.platform.security.userroledao.PentahoOAuthUser;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.util.Arrays;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 5/14/14.
 */
public class ChainedUserDetailsServiceTest {
  @Test
  public void testLoadUserByUsername() throws Exception {

    final UserDetailsService mock1 = mock( UserDetailsService.class );
    UserDetails joeDetails = mock( UserDetails.class );
    UserDetails adminDetails = mock( UserDetails.class );
    when( mock1.loadUserByUsername( "joe" ) ).thenReturn( joeDetails );
    final UserDetailsService mock2 = mock( UserDetailsService.class );
    when( mock1.loadUserByUsername( "admin" ) ).thenReturn( adminDetails );

    ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1, mock2 ) );
    final UserDetails joe = userDetailsService.loadUserByUsername( "joe" );
    assertSame( joeDetails, joe );
    final UserDetails admin = userDetailsService.loadUserByUsername( "admin" );
    assertSame( adminDetails, admin );
  }

  @Test
  public void testLoadUserByUsernameOAuth() {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class );
          MockedStatic<PentahoSystem> mockedPentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {

      final UserDetailsService mock1 = mock( UserDetailsService.class );
      UserDetails joeDetails = mock( UserDetails.class );
      when( mock1.loadUserByUsername( "joe" ) ).thenReturn( joeDetails );

      final UserRoleDaoUserDetailsService mock2 = mock( UserRoleDaoUserDetailsService.class );
      UserDetails adminDetails = mock( UserDetails.class );
      when( mock2.loadUserByUsername( "admin" ) ).thenReturn( adminDetails );

      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabledWithDualAuth ).thenReturn( true );
      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( true );

      PentahoOAuthUserSync pentahoOAuthUserSync = mock( PentahoOAuthUserSync.class );

      mockedPentahoSystem.when( () -> PentahoSystem.get( PentahoOAuthUserSync.class, "pentahoOAuthUserSync",
        PentahoSessionHolder.getSession() ) ).thenReturn( pentahoOAuthUserSync );

      IPentahoUser pentahoOAuthUser = mock( PentahoOAuthUser.class );
      when( mock2.getPentahoOAuthUser( null, "admin" ) ).thenReturn( pentahoOAuthUser );

      ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1, mock2 ) );
      final UserDetails joe = userDetailsService.loadUserByUsername( "joe" );
      assertSame( joeDetails, joe );
      final UserDetails admin = userDetailsService.loadUserByUsername( "admin" );
      assertSame( adminDetails, admin );
    }
  }

  @Test
  public void testLoadUserByUsernameOAuthWithSync() {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class );
          MockedStatic<PentahoSystem> mockedPentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {

      final UserDetailsService mock1 = mock( UserDetailsService.class );
      UserDetails joeDetails = mock( UserDetails.class );
      when( mock1.loadUserByUsername( "joe" ) ).thenReturn( joeDetails );

      final UserRoleDaoUserDetailsService mock2 = mock( UserRoleDaoUserDetailsService.class );
      UserDetails adminDetails = mock( UserDetails.class );
      when( mock2.loadUserByUsername( "admin" ) ).thenReturn( adminDetails );

      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabledWithDualAuth ).thenReturn( true );
      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( true );

      PentahoOAuthUserSync pentahoOAuthUserSync = mock( PentahoOAuthUserSync.class );

      mockedPentahoSystem.when( () -> PentahoSystem.get( PentahoOAuthUserSync.class, "pentahoOAuthUserSync",
        PentahoSessionHolder.getSession() ) ).thenReturn( pentahoOAuthUserSync );

      PentahoOAuthUser pentahoOAuthUser = mock( PentahoOAuthUser.class );
      when( pentahoOAuthUser.getUserId() ).thenReturn( "userId" );
      when( pentahoOAuthUser.getRegistrationId() ).thenReturn( "testRegistration" );
      when( mock2.getPentahoOAuthUser( null, "admin" ) ).thenReturn( pentahoOAuthUser );

      ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1, mock2 ) );
      final UserDetails joe = userDetailsService.loadUserByUsername( "joe" );
      assertSame( joeDetails, joe );
      final UserDetails admin = userDetailsService.loadUserByUsername( "admin" );
      assertSame( adminDetails, admin );
    }
  }

  @Test
  public void testLoadUserByUsernameOAuthWithoutSync() {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class );
          MockedStatic<PentahoSystem> mockedPentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {

      final UserDetailsService mock1 = mock( UserDetailsService.class );
      UserDetails joeDetails = mock( UserDetails.class );
      when( mock1.loadUserByUsername( "joe" ) ).thenReturn( joeDetails );

      final UserRoleDaoUserDetailsService mock2 = mock( UserRoleDaoUserDetailsService.class );
      UserDetails adminDetails = mock( UserDetails.class );
      when( mock2.loadUserByUsername( "admin" ) ).thenReturn( adminDetails );

      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabledWithDualAuth ).thenReturn( true );
      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( true );

      PentahoOAuthUser pentahoOAuthUser = mock( PentahoOAuthUser.class );
      when( pentahoOAuthUser.getUserId() ).thenReturn( "userId" );
      when( pentahoOAuthUser.getRegistrationId() ).thenReturn( "testRegistration" );
      when( mock2.getPentahoOAuthUser( null, "admin" ) ).thenReturn( pentahoOAuthUser );

      ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1, mock2 ) );
      final UserDetails joe = userDetailsService.loadUserByUsername( "joe" );
      assertSame( joeDetails, joe );
      final UserDetails admin = userDetailsService.loadUserByUsername( "admin" );
      assertSame( adminDetails, admin );
    }
  }

  @Test
  public void testLoadUserByUsernameOAuthWithoutLiveUpdate() {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class );
          MockedStatic<PentahoSystem> mockedPentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {

      final UserDetailsService mock1 = mock( UserDetailsService.class );
      UserDetails joeDetails = mock( UserDetails.class );
      when( mock1.loadUserByUsername( "joe" ) ).thenReturn( joeDetails );

      final UserRoleDaoUserDetailsService mock2 = mock( UserRoleDaoUserDetailsService.class );
      UserDetails adminDetails = mock( UserDetails.class );
      when( mock2.loadUserByUsername( "admin" ) ).thenReturn( adminDetails );

      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabledWithDualAuth ).thenReturn( true );

      PentahoOAuthUserSync pentahoOAuthUserSync = mock( PentahoOAuthUserSync.class );

      mockedPentahoSystem.when( () -> PentahoSystem.get( PentahoOAuthUserSync.class, "pentahoOAuthUserSync",
        PentahoSessionHolder.getSession() ) ).thenReturn( pentahoOAuthUserSync );

      PentahoOAuthUser pentahoOAuthUser = mock( PentahoOAuthUser.class );
      when( pentahoOAuthUser.getUserId() ).thenReturn( "userId" );
      when( pentahoOAuthUser.getRegistrationId() ).thenReturn( "testRegistration" );
      when( mock2.getPentahoOAuthUser( null, "admin" ) ).thenReturn( pentahoOAuthUser );

      ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1, mock2 ) );
      final UserDetails joe = userDetailsService.loadUserByUsername( "joe" );
      assertSame( joeDetails, joe );
      final UserDetails admin = userDetailsService.loadUserByUsername( "admin" );
      assertSame( adminDetails, admin );
    }
  }

  @Test
  public void testLoadUserByUsernameOAuthResourceAccessException() {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class );
          MockedStatic<PentahoSystem> mockedPentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {

      final UserDetailsService mock1 = mock( UserDetailsService.class );
      UserDetails joeDetails = mock( UserDetails.class );
      when( mock1.loadUserByUsername( "joe" ) ).thenReturn( joeDetails );

      final UserRoleDaoUserDetailsService mock2 = mock( UserRoleDaoUserDetailsService.class );
      UserDetails adminDetails = mock( UserDetails.class );
      when( mock2.loadUserByUsername( "admin" ) ).thenReturn( adminDetails );

      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabledWithDualAuth ).thenReturn( true );
      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( true );

      mockedPentahoSystem.when( () -> PentahoSystem.get( PentahoOAuthUserSync.class, "pentahoOAuthUserSync",
        PentahoSessionHolder.getSession() ) ).thenThrow(
        new ResourceAccessException( "I/O error on request", new SocketTimeoutException( "Read timed out" ) ) );

      PentahoOAuthUser pentahoOAuthUser = mock( PentahoOAuthUser.class );
      when( pentahoOAuthUser.getUserId() ).thenReturn( "userId" );
      when( pentahoOAuthUser.getRegistrationId() ).thenReturn( "testRegistration" );
      when( mock2.getPentahoOAuthUser( null, "admin" ) ).thenReturn( pentahoOAuthUser );

      ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1, mock2 ) );
      final UserDetails joe = userDetailsService.loadUserByUsername( "joe" );
      assertSame( joeDetails, joe );
      assertThrows( UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername( "admin" ) );
    }
  }

  @Test
  public void testLoadUserByUsernameOAuthWithoutDualAuth() {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class );
          MockedStatic<PentahoSystem> mockedPentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {

      final UserDetailsService mock1 = mock( UserDetailsService.class );
      UserDetails joeDetails = mock( UserDetails.class );
      when( mock1.loadUserByUsername( "joe" ) ).thenReturn( joeDetails );

      final UserRoleDaoUserDetailsService mock2 = mock( UserRoleDaoUserDetailsService.class );
      UserDetails adminDetails = mock( UserDetails.class );
      when( mock2.loadUserByUsername( "admin" ) ).thenReturn( adminDetails );

      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( true );

      PentahoOAuthUserSync pentahoOAuthUserSync = mock( PentahoOAuthUserSync.class );

      mockedPentahoSystem.when( () -> PentahoSystem.get( PentahoOAuthUserSync.class, "pentahoOAuthUserSync",
        PentahoSessionHolder.getSession() ) ).thenReturn( pentahoOAuthUserSync );

      PentahoOAuthUser pentahoOAuthUser = mock( PentahoOAuthUser.class );
      when( pentahoOAuthUser.getUserId() ).thenReturn( "userId" );
      when( pentahoOAuthUser.getRegistrationId() ).thenReturn( "testRegistration" );
      when( mock2.getPentahoOAuthUser( null, "admin" ) ).thenReturn( pentahoOAuthUser );

      ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1, mock2 ) );
      final UserDetails joe = userDetailsService.loadUserByUsername( "joe" );
      assertSame( joeDetails, joe );
      final UserDetails admin = userDetailsService.loadUserByUsername( "admin" );
      assertSame( adminDetails, admin );
    }
  }

  @Test
  public void testLoadUserByUsernameOAuthWithoutRegistrationId() {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class );
          MockedStatic<PentahoSystem> mockedPentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {

      final UserDetailsService mock1 = mock( UserDetailsService.class );
      UserDetails joeDetails = mock( UserDetails.class );
      when( mock1.loadUserByUsername( "joe" ) ).thenReturn( joeDetails );

      final UserRoleDaoUserDetailsService mock2 = mock( UserRoleDaoUserDetailsService.class );
      UserDetails adminDetails = mock( UserDetails.class );
      when( mock2.loadUserByUsername( "admin" ) ).thenReturn( adminDetails );

      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabledWithDualAuth ).thenReturn( true );
      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( true );

      PentahoOAuthUserSync pentahoOAuthUserSync = mock( PentahoOAuthUserSync.class );

      mockedPentahoSystem.when( () -> PentahoSystem.get( PentahoOAuthUserSync.class, "pentahoOAuthUserSync",
        PentahoSessionHolder.getSession() ) ).thenReturn( pentahoOAuthUserSync );

      PentahoOAuthUser pentahoOAuthUser = mock( PentahoOAuthUser.class );
      when( pentahoOAuthUser.getUserId() ).thenReturn( "userId" );
      when( mock2.getPentahoOAuthUser( null, "admin" ) ).thenReturn( pentahoOAuthUser );

      ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1, mock2 ) );
      final UserDetails joe = userDetailsService.loadUserByUsername( "joe" );
      assertSame( joeDetails, joe );
      final UserDetails admin = userDetailsService.loadUserByUsername( "admin" );
      assertSame( adminDetails, admin );
    }
  }

  @Test
  public void testLoadUserByUsernameThrowUserNotFound() {
    final UserDetailsService mock1 = mock( UserDetailsService.class );
    when( mock1.loadUserByUsername( "joe" ) ).thenThrow( new UsernameNotFoundException( "User not found" ) );

    ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1 ) );
    assertThrows( UsernameNotFoundException.class, () -> {
      userDetailsService.loadUserByUsername( "joe" );
    } );
  }

}
