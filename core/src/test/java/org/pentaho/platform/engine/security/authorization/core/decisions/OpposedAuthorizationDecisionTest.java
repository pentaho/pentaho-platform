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
import static org.junit.Assert.assertNotNull;
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
    when( grantedDecision.getShortJustification() ).thenReturn( "Because yes" );

    deniedDecision = mock( IAuthorizationDecision.class );
    when( deniedDecision.isGranted() ).thenReturn( false );
    when( deniedDecision.getRequest() ).thenReturn( request );
    when( deniedDecision.toString() ).thenReturn( "DeniedDecision" );
    when( deniedDecision.getShortJustification() ).thenReturn( "Because not" );
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

  @Test
  public void testGetShortJustification() {
    var opposedDecision = new OpposedAuthorizationDecision( grantedDecision );
    var justification = opposedDecision.getShortJustification();
    assertNotNull( justification );
    assertFalse( justification.isEmpty() );
  }

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
