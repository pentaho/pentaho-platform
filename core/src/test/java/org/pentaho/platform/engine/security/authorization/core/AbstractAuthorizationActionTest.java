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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;

public class AbstractAuthorizationActionTest {

  // Test implementation of AbstractAuthorizationAction
  private static class TestAuthorizationAction extends AbstractAuthorizationAction {
    private final String name;
    private final String localizedDisplayName;

    public TestAuthorizationAction( String name ) {
      this( name, null );
    }

    public TestAuthorizationAction( String name, String localizedDisplayName ) {
      this.name = name;
      this.localizedDisplayName = localizedDisplayName;
    }

    @NonNull
    @Override
    public String getName() {
      return name;
    }

    @NonNull
    @Override
    public String getLocalizedDisplayName( @Nullable String localeString ) {
      return localizedDisplayName;
    }
  }

  private TestAuthorizationAction action1;
  private TestAuthorizationAction action2;
  private TestAuthorizationAction action3;

  @Before
  public void setUp() {
    action1 = new TestAuthorizationAction( "read" );
    action2 = new TestAuthorizationAction( "read" ); // Same name as action1
    action3 = new TestAuthorizationAction( "write" ); // Different name
  }

  // region equals Tests

  @SuppressWarnings( { "SimplifiableAssertion", "EqualsWithItself" } )
  @Test
  public void testEqualsWithSameInstance() {
    // Use assertTrue for reflexivity test as it's testing the specific behavior
    assertTrue( action1.equals( action1 ) );
  }

  @Test
  public void testEqualsWithSameName() {
    assertEquals( action1, action2 );
    assertEquals( action2, action1 );
  }

  @Test
  public void testEqualsWithDifferentName() {
    assertNotEquals( action1, action3 );
    assertNotEquals( action3, action1 );
  }

  @Test
  public void testEqualsWithNull() {
    assertNotEquals( null, action1 );
  }

  @SuppressWarnings( { "SimplifiableAssertion", "EqualsBetweenInconvertibleTypes" } )
  @Test
  public void testEqualsWithDifferentClass() {
    assertFalse( action1.equals( "read" ) );
    assertFalse( action1.equals( 123 ) );
  }

  @SuppressWarnings( { "SimplifiableAssertion" } )
  @Test
  public void testEqualsWithOtherIAuthorizationAction() {
    // Create a different implementation of IAuthorizationAction
    IAuthorizationAction otherAction = createTestAction( "read" );

    assertTrue( action1.equals( otherAction ) );
  }
  // endregion

  // region hashCode Tests
  @Test
  public void testHashCodeConsistency() {
    int hash1 = action1.hashCode();
    int hash2 = action1.hashCode();
    assertEquals( hash1, hash2 );
  }

  @Test
  public void testHashCodeEqualityContract() {
    // Objects that are equal must have the same hash code
    assertEquals( action1.hashCode(), action2.hashCode() );
  }

  @Test
  public void testHashCodeWithDifferentNames() {
    // Objects with different names should typically have different hash codes
    assertNotEquals( action1.hashCode(), action3.hashCode() );
  }
  // endregion

  // region toString Tests
  @Test
  public void testToStringWithNameOnly() {
    var result = action1.toString();
    assertNotNull( result );
    assertEquals( "read", result );
  }

  @Test
  public void testToStringWithLocalizedDisplayName() {
    var actionWithDisplayName = new TestAuthorizationAction( "read", "Read Data" );
    var result = actionWithDisplayName.toString();

    assertNotNull( result );
    assertTrue( result.contains( "Read Data" ) );
    assertTrue( result.contains( "read" ) );
  }

  @Test
  public void testToStringWithEmptyLocalizedDisplayName() {
    var actionWithEmptyDisplayName = new TestAuthorizationAction( "read", "" );
    var result = actionWithEmptyDisplayName.toString();

    assertNotNull( result );
    assertEquals( "read", result );
  }

  @Test
  public void testToStringWithEqualLocalizedDisplayName() {
    var actionWithEmptyDisplayName = new TestAuthorizationAction( "read", "read" );
    var result = actionWithEmptyDisplayName.toString();

    assertNotNull( result );
    assertEquals( "read", result );
  }

  @Test
  public void testToStringWithNullLocalizedDisplayName() {
    var actionWithNullDisplayName = new TestAuthorizationAction( "read", null );
    var result = actionWithNullDisplayName.toString();

    assertNotNull( result );
    assertEquals( "read", result );
  }
  // endregion
}
