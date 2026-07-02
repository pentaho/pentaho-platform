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
