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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class PentahoCachingUserDetailsServiceTest {

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
    when( cachedUser.getUsername() ).thenReturn( "cachedUser" );
    userCache = mock( UserCache.class );
    service.setUserCache( userCache );
    when( userCache.getUserFromCache( "cachedUser" ) ).thenReturn( cachedUser );
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::shouldPerformLiveUpdate ).thenReturn( false );
      UserDetails result = service.loadUserByUsername( "cachedUser" );
      assertEquals( "cachedUser", result.getUsername() );
    }

  }

}
