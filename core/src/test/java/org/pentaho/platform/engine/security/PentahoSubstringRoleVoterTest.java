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


package org.pentaho.platform.engine.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDecisionVoter;

import java.util.ArrayList;
import java.util.List;

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
    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(2);
    authorities.add( new SimpleGrantedAuthority( ROLE_UNREACHABLE ) );
    authorities.add( new SimpleGrantedAuthority( ROLE2 ) );

    List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>(3);
    configAttributes.add( new SecurityConfig( PREFIX + ROLE1 ) );
    configAttributes.add( new SecurityConfig( INVALID_PREFIX + ROLE2 ) );
    configAttributes.add( new SecurityConfig( PREFIX + ROLE3 ) );

    assertTrue( "the method \"vote\" must return ACCESS_DENIED", voteGenericTest( authorities, configAttributes,
        AccessDecisionVoter.ACCESS_DENIED ) );
  }

  @Test
  public void voteGrantedTest() {
    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(3);
    authorities.add( new SimpleGrantedAuthority( ROLE1 ) );
    authorities.add( new SimpleGrantedAuthority( ROLE2 ) );
    authorities.add( new SimpleGrantedAuthority( ROLE3 ) );

    List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>(3);
    configAttributes.add( new SecurityConfig( PREFIX + ROLE1 ) );
    configAttributes.add( new SecurityConfig( PREFIX + ROLE2 ) );
    configAttributes.add( new SecurityConfig( PREFIX + ROLE3 ) );

    assertTrue( "the method \"vote\" must return ACCESS_GRANTED", voteGenericTest( authorities, configAttributes,
        AccessDecisionVoter.ACCESS_GRANTED ) );
  }

  @Test
  public void voteAbstainTest() {
    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(2);
    authorities.add( new SimpleGrantedAuthority( ROLE1 ) );
    authorities.add( new SimpleGrantedAuthority( ROLE2 ) );

    List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>(3);
    configAttributes.add( new SecurityConfig( INVALID_PREFIX + ROLE1 ) );
    configAttributes.add( new SecurityConfig( INVALID_PREFIX + ROLE2 ) );
    configAttributes.add( new SecurityConfig( INVALID_PREFIX + ROLE3 ) );

    assertTrue( "the method \"vote\" must return ACCESS_ABSTAIN", voteGenericTest( authorities, configAttributes,
        AccessDecisionVoter.ACCESS_ABSTAIN ) );
  }

  public boolean voteGenericTest( List<GrantedAuthority> authorities, List<ConfigAttribute> configAttributes, int expectedResult ) {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken( StringUtils.EMPTY, StringUtils.EMPTY, authorities );

    return expectedResult == pentahoSubstringRoleVoter.vote( authentication, new Object(), configAttributes );
  }

}
