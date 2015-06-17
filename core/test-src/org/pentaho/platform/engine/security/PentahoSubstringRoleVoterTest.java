/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.SecurityConfig;
import org.springframework.security.adapters.PrincipalSpringSecurityUserToken;
import org.springframework.security.vote.AccessDecisionVoter;

public class PentahoSubstringRoleVoterTest {

  private static final String PREFIX = "PREFIX_";
  private static final String INVALID_PREFIX = "BADPREFIX_";
  private static final String TEST_VALUE = "testValue";
  private static final String ROLE1 = "element1";
  private static final String ROLE2 = "element2";
  private static final String ROLE3 = "element3";
  private static final String ROLE_UNREACHABLE = "unreachable-role";

  private PentahoSubstringRoleVoter pentahoSubstringRoleVoter;

  @Before
  public void init() {
    pentahoSubstringRoleVoter = new PentahoSubstringRoleVoter( PREFIX );
  }

  @Test
  public void supportsValidConfigAttributeTest() {
    ConfigAttribute validConfigAttribute = new SecurityConfig( PREFIX + TEST_VALUE );
    assertTrue( "Valid prefix must be supported", pentahoSubstringRoleVoter.supports( validConfigAttribute ) );
  }

  @Test
  public void supportsInvalidConfigAttributeTest() {
    ConfigAttribute invalidConfigAttribute = new SecurityConfig( INVALID_PREFIX + TEST_VALUE );
    assertFalse( "Invalid prefix must not be supported", pentahoSubstringRoleVoter.supports( invalidConfigAttribute ) );
  }

  @Test
  public void supportsNullConfigAttributeTest() {
    ConfigAttribute nullConfigAttribute = mock( ConfigAttribute.class );
    when( nullConfigAttribute.getAttribute() ).thenReturn( null );
    assertFalse( "Null argument must not be supported", pentahoSubstringRoleVoter.supports( nullConfigAttribute ) );
  }

  @Test
  public void supportsClassTest() {
    boolean isSupports = pentahoSubstringRoleVoter.supports( Class.class );
    assertTrue( "this method must always return true", isSupports );
  }

  @Test
  public void voteDeniedTest() {
    GrantedAuthority[] authorities = { new GrantedAuthorityImpl( ROLE_UNREACHABLE ), new GrantedAuthorityImpl( ROLE2 ) };
    String[] strConfArr = { PREFIX + ROLE1, INVALID_PREFIX + ROLE2, PREFIX + ROLE3 };

    assertTrue( "the method \"vote\" must return ACCESS_DENIED", voteGenericTest( authorities, strConfArr,
        AccessDecisionVoter.ACCESS_DENIED ) );
  }

  @Test
  public void voteGrantedTest() {
    GrantedAuthority[] authorities = { new GrantedAuthorityImpl( ROLE1 ), new GrantedAuthorityImpl( ROLE2 ), new GrantedAuthorityImpl( ROLE3 ) };
    String[] strConfArr = { PREFIX + ROLE1, PREFIX + ROLE2, PREFIX + ROLE3 };

    assertTrue( "the method \"vote\" must return ACCESS_GRANTED", voteGenericTest( authorities, strConfArr,
        AccessDecisionVoter.ACCESS_GRANTED ) );
  }

  @Test
  public void voteAbstainTest() {
    GrantedAuthority[] authorities = { new GrantedAuthorityImpl( ROLE1 ), new GrantedAuthorityImpl( ROLE2 ) };
    String[] strConfArr = { INVALID_PREFIX + ROLE1, INVALID_PREFIX + ROLE2, INVALID_PREFIX + ROLE3 };

    assertTrue( "the method \"vote\" must return ACCESS_ABSTAIN", voteGenericTest( authorities, strConfArr,
        AccessDecisionVoter.ACCESS_ABSTAIN ) );
  }

  public boolean voteGenericTest( GrantedAuthority[] authorities, String[] strConfArr, int expectedResult ) {
    Authentication authentication =
        new PrincipalSpringSecurityUserToken( StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, authorities,
            null );
    ConfigAttributeDefinition config = new ConfigAttributeDefinition( strConfArr );

    return expectedResult == pentahoSubstringRoleVoter.vote( authentication, new Object(), config );
  }

}
