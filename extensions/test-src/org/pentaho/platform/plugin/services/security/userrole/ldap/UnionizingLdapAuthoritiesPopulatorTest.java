/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.ldap.LdapAuthoritiesPopulator;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
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
  public void testGetGrantedAuthorities() throws Exception {
    UnionizingLdapAuthoritiesPopulator populator = new UnionizingLdapAuthoritiesPopulator();

    Set<LdapAuthoritiesPopulator> pops = new HashSet<>();
    pops.add( authPop1 );
    pops.add( authPop2 );

    String username = "admin";
    GrantedAuthority g1 = new GrantedAuthorityImpl( "power user" );
    GrantedAuthority g2 = new GrantedAuthorityImpl( "administrator" );
    GrantedAuthority[] auths1 = new GrantedAuthority[]{ g1 };
    GrantedAuthority[] auths2 = new GrantedAuthority[]{ g2 };

    when( authPop1.getGrantedAuthorities( userData, username ) ).thenReturn( auths1 );
    when( authPop2.getGrantedAuthorities( userData, username ) ).thenReturn( auths2 );

    populator.setPopulators( pops );

    GrantedAuthority[] authorities = populator.getGrantedAuthorities( userData, username );
    assertEquals( 2, authorities.length );
    assertTrue( ArrayUtils.contains( authorities, g1 ) );
    assertTrue( ArrayUtils.contains( authorities, g2 ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testAfterPropertiesSet() throws Exception {
    UnionizingLdapAuthoritiesPopulator populator = new UnionizingLdapAuthoritiesPopulator();
    populator.afterPropertiesSet();
  }
}
