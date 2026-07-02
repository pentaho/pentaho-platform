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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    when( dirContext.search( nullable( String.class ), nullable( String.class ), any( SearchControls.class ) ) ).thenReturn( namingEnumeration );

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
