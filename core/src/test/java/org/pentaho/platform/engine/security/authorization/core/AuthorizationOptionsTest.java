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

package org.pentaho.platform.engine.security.authorization.core;

import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRuleOverrider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AuthorizationOptionsTest {

  @Test
  public void testDefaultConstructor() {
    var options = new AuthorizationOptions();
    assertEquals( AuthorizationDecisionReportingMode.SETTLED, options.getDecisionReportingMode() );
    assertNull( options.getAuthorizationRuleOverrider() );
  }

  @Test
  public void testConstructorWithDecisionReportingMode() {
    var options = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL );
    assertEquals( AuthorizationDecisionReportingMode.FULL, options.getDecisionReportingMode() );
  }

  @Test
  public void testConstructorWithAllArguments() {
    var overrider = mock( IAuthorizationRuleOverrider.class );
    var options = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL, overrider );

    assertEquals( AuthorizationDecisionReportingMode.FULL, options.getDecisionReportingMode() );
    assertEquals( overrider, options.getAuthorizationRuleOverrider() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullDecisionReportingModeThrows() {
    //noinspection DataFlowIssue
    new AuthorizationOptions( null );
  }

  @Test
  public void testConstructorWithNullAuthorizationRuleOverrider() {
    var options = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL, null );
    assertNull( options.getAuthorizationRuleOverrider() );
  }

  @Test
  public void testEqualsAndHashCode() {
    var overrider1 = mock( IAuthorizationRuleOverrider.class );
    var overrider2 = mock( IAuthorizationRuleOverrider.class );

    var options1 = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL, overrider1 );
    var options2 = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL, overrider1 );
    var options3 = new AuthorizationOptions( AuthorizationDecisionReportingMode.SETTLED, overrider2 );

    var notOptions = new Object();
    assertNotEquals( options1, notOptions );

    assertEquals( options1, options2 );
    assertNotEquals( options1, options3 );
    assertEquals( options1.hashCode(), options2.hashCode() );
    assertNotEquals( options1.hashCode(), options3.hashCode() );
  }

  @Test
  public void testToStringFormat() {
    var overrider = mock( IAuthorizationRuleOverrider.class );
    var options = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL, overrider );
    String toString = options.toString();

    assertTrue( toString.contains( "FULL" ) );
    assertTrue( toString.contains( overrider.toString() ) );
  }
}
