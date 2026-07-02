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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Arrays;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
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

    ChainedUserDetailsService userDetailsService = new ChainedUserDetailsService( Arrays.asList( mock1, mock2 )  );
    final UserDetails joe = userDetailsService.loadUserByUsername( "joe" );
    assertSame( joeDetails, joe );
    final UserDetails admin = userDetailsService.loadUserByUsername( "admin" );
    assertSame( adminDetails, admin );
  }
}
