/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole;

import org.junit.Test;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

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
