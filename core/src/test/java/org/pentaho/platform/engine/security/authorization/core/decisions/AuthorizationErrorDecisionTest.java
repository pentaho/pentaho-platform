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
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class AuthorizationErrorDecisionTest {

  private IAuthorizationRequest request;
  private Exception cause;

  @Before
  public void setUp() {
    var user = createTestUser();
    var action = createTestAction( "read" );

    request = new AuthorizationRequest( user, action );
    cause = new RuntimeException( "Test error message" );
  }

  @Test
  public void testConstructorAndGetters() {
    var decision = new AuthorizationErrorDecision( request, cause );

    assertEquals( request, decision.getRequest() );
    assertFalse( decision.isGranted() );
    assertEquals( cause, decision.getCause() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRequestThrows() {
    //noinspection DataFlowIssue
    new AuthorizationErrorDecision( null, cause );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullCauseThrows() {
    //noinspection DataFlowIssue
    new AuthorizationErrorDecision( request, null );
  }

  @Test
  public void testIsGrantedAlwaysFalse() {
    var decision = new AuthorizationErrorDecision( request, cause );
    assertFalse( decision.isGranted() );
  }

  @Test
  public void testGetShortJustification() {
    var decision = new AuthorizationErrorDecision( request, cause );

    var justification = decision.getShortJustification();
    assertNotNull( justification );
    assertFalse( justification.isEmpty() );
  }

  @Test
  public void testToStringFormat() {
    var decision = new AuthorizationErrorDecision( request, cause );

    var result = decision.toString();

    assertTrue( result.contains( "AuthorizationErrorDecision" ) );
    assertTrue( result.contains( "Denied" ) );
    assertTrue( result.contains( "Test error message" ) );
  }
}
