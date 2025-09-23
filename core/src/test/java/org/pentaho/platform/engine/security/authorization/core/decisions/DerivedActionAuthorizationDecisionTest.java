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
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class DerivedActionAuthorizationDecisionTest {

  private IAuthorizationRequest request1;
  private IAuthorizationRequest request2;
  private IAuthorizationAction derivedFromAction;
  private IAuthorizationDecision grantedDecision;
  private IAuthorizationDecision deniedDecision;

  @Before
  public void setUp() {
    var user = createTestUser();

    derivedFromAction = createTestAction( "read" );

    request1 = new AuthorizationRequest( user, derivedFromAction );

    var action2 = createTestAction( "write" );
    request2 = new AuthorizationRequest( user, action2 );

    grantedDecision = mock( IAuthorizationDecision.class );
    when( grantedDecision.isGranted() ).thenReturn( true );
    when( grantedDecision.getRequest() ).thenReturn( request1 );
    when( grantedDecision.toString() ).thenReturn( "GrantedDecision" );

    deniedDecision = mock( IAuthorizationDecision.class );
    when( deniedDecision.isGranted() ).thenReturn( false );
    when( deniedDecision.getRequest() ).thenReturn( request1 );
    when( deniedDecision.toString() ).thenReturn( "DeniedDecision" );
  }

  @Test
  public void testConstructorAndGetters() {
    var derivedGranted = new DerivedActionAuthorizationDecision( request2, grantedDecision );

    assertEquals( request2, derivedGranted.getRequest() );
    assertTrue( derivedGranted.isGranted() );
    assertEquals( grantedDecision, derivedGranted.getDerivedFromDecision() );
    assertEquals( derivedFromAction, derivedGranted.getDerivedFromAction() );

    var derivedDenied = new DerivedActionAuthorizationDecision( request2, deniedDecision );

    assertEquals( request2, derivedDenied.getRequest() );
    assertFalse( derivedDenied.isGranted() );
    assertEquals( deniedDecision, derivedDenied.getDerivedFromDecision() );
    assertEquals( derivedFromAction, derivedDenied.getDerivedFromAction() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRequestThrows() {
    //noinspection DataFlowIssue
    new DerivedActionAuthorizationDecision( null, grantedDecision );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullDerivedFromDecisionThrows() {
    //noinspection DataFlowIssue
    new DerivedActionAuthorizationDecision( request2, null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithSameRequestThrows() {
    new DerivedActionAuthorizationDecision( request1, grantedDecision );
  }

  @Test
  public void testGetDerivedFromAction() {
    var derivedDecision = new DerivedActionAuthorizationDecision( request2, grantedDecision );

    assertSame( derivedFromAction, derivedDecision.getDerivedFromAction() );
  }

  @Test
  public void testGetShortJustification() {
    var derivedDecision = new DerivedActionAuthorizationDecision( request2, grantedDecision );

    var justification = derivedDecision.getShortJustification();

    assertNotNull( justification );
    assertFalse( justification.isEmpty() );
  }

  @Test
  public void testToStringFormat() {
    var derivedGranted = new DerivedActionAuthorizationDecision( request2, grantedDecision );
    var derivedDenied = new DerivedActionAuthorizationDecision( request2, deniedDecision );

    var grantedString = derivedGranted.toString();
    var deniedString = derivedDenied.toString();

    assertTrue( grantedString.contains( "DerivedActionAuthorizationDecision" ) );
    assertTrue( grantedString.contains( "Granted" ) );
    assertTrue( grantedString.contains( "from: read" ) );

    assertTrue( deniedString.contains( "DerivedActionAuthorizationDecision" ) );
    assertTrue( deniedString.contains( "Denied" ) );
    assertTrue( deniedString.contains( "from: read" ) );
  }
}
