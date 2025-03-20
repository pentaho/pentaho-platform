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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class PentahoCachingUserDetailsServiceTest {

  public static final String CACHED_USER = "cachedUser";

  @Mock
  UserCache userCache;

  @Mock
  UserDetailsService delegate;

  @Mock
  ITenantedPrincipleNameResolver nameResolver;

  @Test
  public void loadUserByUsernameReturnsCachedUser() {
    PentahoCachingUserDetailsService service = new PentahoCachingUserDetailsService( delegate, nameResolver );
    UserDetails cachedUser = mock( UserDetails.class );
    when( cachedUser.getUsername() ).thenReturn( CACHED_USER );
    userCache = mock( UserCache.class );
    service.setUserCache( userCache );
    when( userCache.getUserFromCache( CACHED_USER ) ).thenReturn( cachedUser );
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( false );
      UserDetails result = service.loadUserByUsername( CACHED_USER );
      assertEquals( CACHED_USER, result.getUsername() );
    }
  }

  @Test
  public void loadUserByUsernamePerformLiveUpdate() {
    delegate = mock( UserDetailsService.class );
    UserDetails cachedUser = mock( UserDetails.class );
    when( delegate.loadUserByUsername( CACHED_USER ) ).thenReturn( cachedUser );
    nameResolver = mock( ITenantedPrincipleNameResolver.class );
    PentahoCachingUserDetailsService service = new PentahoCachingUserDetailsService( delegate, nameResolver );
    when( cachedUser.getUsername() ).thenReturn( CACHED_USER );
    userCache = mock( UserCache.class );
    service.setUserCache( userCache );
    when( userCache.getUserFromCache( CACHED_USER ) ).thenReturn( cachedUser );
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( true );
      when( nameResolver.getPrincipleId( any(), eq( CACHED_USER ) ) ).thenReturn( CACHED_USER );
      UserDetails result = service.loadUserByUsername( CACHED_USER );
      assertEquals( CACHED_USER, result.getUsername() );
    }
  }

  @Test
  public void loadUserByUsernameNotCachedUser() {
    delegate = mock( UserDetailsService.class );
    UserDetails cachedUser = mock( UserDetails.class );
    when( delegate.loadUserByUsername( CACHED_USER ) ).thenReturn( cachedUser );
    nameResolver = mock( ITenantedPrincipleNameResolver.class );
    PentahoCachingUserDetailsService service = new PentahoCachingUserDetailsService( delegate, nameResolver );
    when( cachedUser.getUsername() ).thenReturn( CACHED_USER );
    userCache = mock( UserCache.class );
    service.setUserCache( userCache );
    when( userCache.getUserFromCache( CACHED_USER ) ).thenReturn( null );
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( true );
      when( nameResolver.getPrincipleId( any(), eq( CACHED_USER ) ) ).thenReturn( CACHED_USER );
      UserDetails result = service.loadUserByUsername( CACHED_USER );
      assertEquals( CACHED_USER, result.getUsername() );
    }
  }

}
