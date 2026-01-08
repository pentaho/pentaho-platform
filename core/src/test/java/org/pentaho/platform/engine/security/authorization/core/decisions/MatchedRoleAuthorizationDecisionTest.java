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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class MatchedRoleAuthorizationDecisionTest {

  private IAuthorizationRequest request;
  private IAuthorizationRole role;

  @Before
  public void setUp() {
    var user = createTestUser();
    var action = createTestAction( "read" );

    request = new AuthorizationRequest( user, action );

    role = new AuthorizationRole( "Administrator" );
  }

  @Test
  public void testConstructorAndGetters() {
    var grantedDecision = new MatchedRoleAuthorizationDecision( request, true, role );
    assertEquals( request, grantedDecision.getRequest() );
    assertTrue( grantedDecision.isGranted() );
    assertEquals( role, grantedDecision.getRole() );

    var deniedDecision = new MatchedRoleAuthorizationDecision( request, false, role );
    assertEquals( request, deniedDecision.getRequest() );
    assertFalse( deniedDecision.isGranted() );
    assertEquals( role, deniedDecision.getRole() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRequestThrows() {
    //noinspection DataFlowIssue
    new MatchedRoleAuthorizationDecision( null, true, role );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRoleThrows() {
    //noinspection DataFlowIssue
    new MatchedRoleAuthorizationDecision( request, true, null );
  }

  @Test
  public void testGetShortJustification() {
    var decision = new MatchedRoleAuthorizationDecision( request, true, role );
    var justification = decision.getShortJustification();
    assertNotNull( justification );
    assertFalse( justification.isEmpty() );
  }

  @Test
  public void testToStringFormat() {
    var grantedDecision = new MatchedRoleAuthorizationDecision( request, true, role );
    var deniedDecision = new MatchedRoleAuthorizationDecision( request, false, role );

    var grantedString = grantedDecision.toString();
    var deniedString = deniedDecision.toString();

    assertTrue( grantedString.contains( "MatchedRole[" ) );
    assertTrue( grantedString.contains( "Granted" ) );
    assertTrue( grantedString.contains( "Administrator" ) );

    assertTrue( deniedString.contains( "MatchedRole[" ) );
    assertTrue( deniedString.contains( "Denied" ) );
    assertTrue( deniedString.contains( "Administrator" ) );
  }
}
