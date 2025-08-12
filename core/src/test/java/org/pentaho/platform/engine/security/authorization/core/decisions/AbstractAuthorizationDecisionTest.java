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
import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class AbstractAuthorizationDecisionTest {

  private IAuthorizationRequest request;
  private TestAuthorizationDecision grantedDecision;
  private TestAuthorizationDecision deniedDecision;

  // Concrete test implementation of AbstractAuthorizationDecision
  private static class TestAuthorizationDecision extends AbstractAuthorizationDecision {
    public TestAuthorizationDecision( IAuthorizationRequest request, boolean granted ) {
      super( request, granted );
    }
  }

  @Before
  public void setUp() {
    var user = createTestUser();
    var action = createTestAction( "read" );

    request = new AuthorizationRequest( user, action );

    grantedDecision = new TestAuthorizationDecision( request, true );
    deniedDecision = new TestAuthorizationDecision( request, false );
  }

  @Test
  public void testConstructorAndGetters() {
    assertEquals( request, grantedDecision.getRequest() );
    assertTrue( grantedDecision.isGranted() );

    assertEquals( request, deniedDecision.getRequest() );
    assertFalse( deniedDecision.isGranted() );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullRequestThrows() {
    new TestAuthorizationDecision( null, true );
  }

  @Test
  public void testGetShortJustificationDefaultsToEmptyString() {
    assertEquals( "", grantedDecision.getShortJustification() );
    assertEquals( "", deniedDecision.getShortJustification() );
  }

  @Test
  public void testGetGrantedLogText() {
    assertEquals( "Granted", grantedDecision.getGrantedLogText() );
    assertEquals( "Denied", deniedDecision.getGrantedLogText() );
  }

  @Test
  public void testToStringFormat() {
    var grantedString = grantedDecision.toString();
    var deniedString = deniedDecision.toString();

    assertTrue( grantedString.contains( "TestAuthorizationDecision" ) );
    assertTrue( grantedString.contains( "Granted" ) );

    assertTrue( deniedString.contains( "TestAuthorizationDecision" ) );
    assertTrue( deniedString.contains( "Denied" ) );
  }
}
