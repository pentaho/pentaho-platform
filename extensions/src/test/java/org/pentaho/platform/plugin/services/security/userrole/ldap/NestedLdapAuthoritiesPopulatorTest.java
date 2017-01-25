/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ldap.core.ContextSource;
import org.springframework.security.core.GrantedAuthority;

import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 10/30/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class NestedLdapAuthoritiesPopulatorTest {

  NestedLdapAuthoritiesPopulator authoritiesPopulator;
  String groupSearchBase = "search";

  @Mock ContextSource contextSource;
  @Mock DirContext dirContext;

  @Before
  public void setUp() throws Exception {
    authoritiesPopulator = new NestedLdapAuthoritiesPopulator( contextSource, groupSearchBase );
  }

  @Test
  public void testGetGroupMembershipRoles() throws Exception {
    NestedLdapAuthoritiesPopulator authPop = spy( authoritiesPopulator );
    when( contextSource.getReadOnlyContext() ).thenReturn( dirContext );
    NamingEnumeration<SearchResult> namingEnumeration = mock( NamingEnumeration.class );
    when( dirContext.search( anyString(), anyString(), any( SearchControls.class ) ) ).thenReturn( namingEnumeration );

    Set roles = authoritiesPopulator.getGroupMembershipRoles( "uid=suzy,ou=users,dc=pentaho,dc=org", "suzy" );
    assertNotNull( roles );
    assertEquals( 0, roles.size() );
  }

  @Test
  public void testGetParentRoles() throws Exception {
    Map<String, String> rolesMap = new HashMap<>();
    rolesMap.put( "child", "parentA" );
    rolesMap.put( "grandchild", "child" );

    authoritiesPopulator.setExtraRolesMapping( rolesMap );
    assertEquals( rolesMap, authoritiesPopulator.getExtraRolesMapping() );
    Set<String> children = new HashSet<>();
    children.add( "child" );

    Set parentRoles = authoritiesPopulator.getParentRoles( children );

    assertNotNull( parentRoles );
    assertEquals( 1, parentRoles.size() );
    assertTrue( parentRoles.contains( "parentA" ) );
  }

  @Test
  public void testToGrantedAuthorities() throws Exception {
    Set<String> roles = new HashSet<>();
    roles.add( "authenticated" );
    roles.add( "power user" );

    Set authorities = authoritiesPopulator.toGrantedAuthorities( roles );
    assertNotNull( authorities );
    assertEquals( 2, authorities.size() );
    for ( Object authority : authorities ) {
      assertTrue( authority instanceof GrantedAuthority );
      GrantedAuthority ga = (GrantedAuthority) authority;
      assertTrue( "authenticated".equals( ga.getAuthority() ) || "power user".equals( ga.getAuthority() ) );
    }
  }

  @Test( expected = IllegalArgumentException.class )
  public void testAfterPropertiesSet() throws Exception {
    authoritiesPopulator.afterPropertiesSet();
  }
}
