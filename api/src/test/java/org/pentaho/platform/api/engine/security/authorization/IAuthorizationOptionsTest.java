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

package org.pentaho.platform.api.engine.security.authorization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IAuthorizationOptionsTest {

  // region getDefault() Tests

  @Test
  void testGetDefaultReturnsNonNullInstance() {
    IAuthorizationOptions options = IAuthorizationOptions.getDefault();
    assertNotNull( options );
  }

  @Test
  void testGetDefaultReturnsSettledDecisionReportingMode() {
    IAuthorizationOptions options = IAuthorizationOptions.getDefault();
    assertNotNull( options );
    assertEquals( AuthorizationDecisionReportingMode.SETTLED, options.getDecisionReportingMode() );
  }

  @Test
  void testGetDefaultConsistentBehavior() {
    IAuthorizationOptions options = IAuthorizationOptions.getDefault();

    // Test that multiple calls to getDecisionReportingMode return the same value
    AuthorizationDecisionReportingMode mode1 = options.getDecisionReportingMode();
    AuthorizationDecisionReportingMode mode2 = options.getDecisionReportingMode();

    assertSame( mode1, mode2 );
    assertEquals( AuthorizationDecisionReportingMode.SETTLED, mode1 );
  }

  @Test
  void testGetDefaultReturnsNullAuthorizationRuleOverrider() {
    IAuthorizationOptions options = IAuthorizationOptions.getDefault();
    assertNull( options.getAuthorizationRuleOverrider() );
  }

  @Test
  void testGetDefaultAuthorizationRuleOverriderConsistentBehavior() {
    IAuthorizationOptions options = IAuthorizationOptions.getDefault();

    // Test that multiple calls to getAuthorizationRuleOverrider return the same value (null)
    IAuthorizationRuleOverrider overrider1 = options.getAuthorizationRuleOverrider();
    IAuthorizationRuleOverrider overrider2 = options.getAuthorizationRuleOverrider();

    assertSame( overrider1, overrider2 );
    assertNull( overrider1 );
  }

  // endregion

  // region equals() Tests

  @SuppressWarnings( { "EqualsWithItself", "SimplifiableAssertion" } )
  @Test
  void testEqualsWithSameInstance() {
    var options = IAuthorizationOptions.getDefault();
    assertTrue( options.equals( options ) );
  }

  @SuppressWarnings( { "SimplifiableAssertion", "ConstantValue" } )
  @Test
  void testEqualsWithDefaultInstances() {
    var options1 = IAuthorizationOptions.getDefault();
    var options2 = IAuthorizationOptions.getDefault();

    assertTrue( options1.equals( options2 ) );
    assertTrue( options2.equals( options1 ) );
  }

  @SuppressWarnings( { "ConstantValue", "SimplifiableAssertion" } )
  @Test
  void testEqualsWithNull() {
    IAuthorizationOptions options = IAuthorizationOptions.getDefault();
    assertFalse( options.equals( null ) );
  }

  @SuppressWarnings( { "SimplifiableAssertion", "EqualsBetweenInconvertibleTypes" } )
  @Test
  void testEqualsWithDifferentClass() {
    IAuthorizationOptions options = IAuthorizationOptions.getDefault();
    assertFalse( options.equals( "not an options object" ) );
    assertFalse( options.equals( 123 ) );
  }
  // endregion

  // region hashCode() Tests
  @Test
  void testHashCodeConsistency() {
    IAuthorizationOptions options = IAuthorizationOptions.getDefault();
    int hash1 = options.hashCode();
    int hash2 = options.hashCode();

    assertEquals( hash1, hash2 );
  }

  @Test
  void testHashCodeWithDefaultInstances() {
    IAuthorizationOptions options1 = IAuthorizationOptions.getDefault();
    IAuthorizationOptions options2 = IAuthorizationOptions.getDefault();

    assertEquals( options1.hashCode(), options2.hashCode() );
  }
  // endregion

  // region toString() Tests
  @Test
  void testToStringContainsMode() {
    var options = IAuthorizationOptions.getDefault();

    var result = options.toString();

    assertNotNull( result );
    assertTrue( result.contains( AuthorizationDecisionReportingMode.SETTLED.toString() ) );
  }

  @Test
  void testToStringContainsAuthorizationRuleOverrider() {
    var options = IAuthorizationOptions.getDefault();

    var result = options.toString();

    assertNotNull( result );
    assertTrue( result.contains( "authorizationRuleOverrider" ) );
  }
  // endregion
}
