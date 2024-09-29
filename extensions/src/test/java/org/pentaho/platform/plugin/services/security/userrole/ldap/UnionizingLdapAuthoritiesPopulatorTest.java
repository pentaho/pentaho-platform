/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/30/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class UnionizingLdapAuthoritiesPopulatorTest {

  @Mock LdapAuthoritiesPopulator authPop1;
  @Mock LdapAuthoritiesPopulator authPop2;
  @Mock DirContextOperations userData;

  @Test
  public void testGetGrantedAuthorities() {
    UnionizingLdapAuthoritiesPopulator populator = new UnionizingLdapAuthoritiesPopulator();

    Set<LdapAuthoritiesPopulator> pops = new HashSet<>();
    pops.add( authPop1 );
    pops.add( authPop2 );

    String username = "admin";
    GrantedAuthority g1 = new SimpleGrantedAuthority( "power user" );
    GrantedAuthority g2 = new SimpleGrantedAuthority( "administrator" );
    Collection auths1 = Arrays.asList( new GrantedAuthority[]{ g1 } );
    Collection auths2 = Arrays.asList( new GrantedAuthority[]{ g2 } );

    when( authPop1.getGrantedAuthorities( userData, username ) ).thenReturn( auths1 );
    when( authPop2.getGrantedAuthorities( userData, username ) ).thenReturn( auths2 );

    populator.setPopulators( pops );

    Collection<? extends GrantedAuthority> authorities = populator.getGrantedAuthorities( userData, username );
    assertEquals( 2, authorities.size() );
    assertTrue( authorities.contains( g1 ) );
    assertTrue( authorities.contains( g2 ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testAfterPropertiesSet() throws Exception {
    UnionizingLdapAuthoritiesPopulator populator = new UnionizingLdapAuthoritiesPopulator();
    populator.afterPropertiesSet();
  }
}
