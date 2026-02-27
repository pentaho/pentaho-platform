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

package org.pentaho.platform.engine.security.authorization.core.decisions;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class OpposedAuthorizationDecisionTest {

  private IAuthorizationRequest request;
  private IAuthorizationDecision grantedDecision;
  private IAuthorizationDecision deniedDecision;

  @Before
  public void setUp() {
    var user = createTestUser();

    var action = createTestAction( "read" );
    request = new AuthorizationRequest( user, action );

    grantedDecision = mock( IAuthorizationDecision.class );
    when( grantedDecision.isGranted() ).thenReturn( true );
    when( grantedDecision.getRequest() ).thenReturn( request );
    when( grantedDecision.toString() ).thenReturn( "GrantedDecision" );
    when( grantedDecision.getShortJustification() ).thenReturn( "Has role admin" );

    deniedDecision = mock( IAuthorizationDecision.class );
    when( deniedDecision.isGranted() ).thenReturn( false );
    when( deniedDecision.getRequest() ).thenReturn( request );
    when( deniedDecision.toString() ).thenReturn( "DeniedDecision" );
    when( deniedDecision.getShortJustification() ).thenReturn( "_Not_ Has role admin" );
  }

  @Test
  public void testConstructorAndGetters() {
    var opposedToGranted = new OpposedAuthorizationDecision( grantedDecision );

    assertEquals( request, opposedToGranted.getRequest() );
    // Negated from granted
    assertFalse( opposedToGranted.isGranted() );
    assertEquals( grantedDecision, opposedToGranted.getOpposedToDecision() );

    var opposedToDenied = new OpposedAuthorizationDecision( deniedDecision );

    assertEquals( request, opposedToDenied.getRequest() );
    // Negated from denied
    assertTrue( opposedToDenied.isGranted() );
    assertEquals( deniedDecision, opposedToDenied.getOpposedToDecision() );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullOpposedToDecisionThrows() {
    //noinspection DataFlowIssue
    new OpposedAuthorizationDecision( null );
  }

  // region getShortJustification() Tests

  @Test
  public void testGetShortJustificationReturnsBaseJustification() {
    // Opposed to GRANTED = DENIED, but justification is the same as base
    // Example: "Opposed to Has-role-admin" when user HAS role:
    //   Base: GRANTED, "Has role admin"
    //   Opposed: DENIED, "Has role admin" (denied BECAUSE they have it)
    var opposedToGranted = new OpposedAuthorizationDecision( grantedDecision );
    assertEquals( "Has role admin", opposedToGranted.getShortJustification() );

    // Opposed to DENIED = GRANTED, but justification is the same as base
    // Example: "Opposed to Has-role-admin" when user LACKS role:
    //   Base: DENIED, "_Not_ Has role admin"
    //   Opposed: GRANTED, "_Not_ Has role admin" (granted BECAUSE they don't have it)
    var opposedToDenied = new OpposedAuthorizationDecision( deniedDecision );
    assertEquals( "_Not_ Has role admin", opposedToDenied.getShortJustification() );
  }

  @Test
  public void testGetShortJustificationWithEmptyBaseJustification() {
    when( grantedDecision.getShortJustification() ).thenReturn( "" );

    var opposedDecision = new OpposedAuthorizationDecision( grantedDecision );

    assertEquals( "", opposedDecision.getShortJustification() );
  }

  @Test
  public void testDoubleOpposedDecisionJustification() {
    when( grantedDecision.getShortJustification() ).thenReturn( "Has role X" );

    var opposedOnce = new OpposedAuthorizationDecision( grantedDecision );
    var opposedTwice = new OpposedAuthorizationDecision( opposedOnce );

    // Double opposed returns to original status AND same justifications
    assertTrue( opposedTwice.isGranted() );
    assertEquals( "Has role X", opposedTwice.getShortJustification() );
  }

  // endregion

  @Test
  public void testToStringFormat() {
    var opposedToGranted = new OpposedAuthorizationDecision( grantedDecision );
    var opposedToDenied = new OpposedAuthorizationDecision( deniedDecision );

    var opposedToGrantedString = opposedToGranted.toString();
    var opposedToDeniedString = opposedToDenied.toString();

    assertTrue( opposedToGrantedString.contains( "Opposed[" ) );
    assertTrue( opposedToGrantedString.contains( "Denied" ) ); // Opposed to granted = denied
    assertTrue( opposedToGrantedString.contains( "to:" ) );
    assertTrue( opposedToGrantedString.contains( "GrantedDecision" ) );

    assertTrue( opposedToDeniedString.contains( "Opposed[" ) );
    assertTrue( opposedToDeniedString.contains( "Granted" ) ); // Opposed to denied = granted
    assertTrue( opposedToDeniedString.contains( "to:" ) );
    assertTrue( opposedToDeniedString.contains( "DeniedDecision" ) );
  }
}
