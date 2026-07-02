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

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class AnyAuthorizationDecisionTest {

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
    when( grantedDecision.toString() ).thenReturn( "GrantedDecision" );

    deniedDecision = mock( IAuthorizationDecision.class );
    when( deniedDecision.isGranted() ).thenReturn( false );
    when( deniedDecision.toString() ).thenReturn( "DeniedDecision" );
  }

  @Test
  public void testConstructorWithCalculatedGranted() {
    var anyWithGranted = new AnyAuthorizationDecision( request, Set.of( grantedDecision, deniedDecision ) );
    assertTrue( anyWithGranted.isGranted() );

    var anyWithAllDenied = new AnyAuthorizationDecision( request, Set.of( deniedDecision ) );
    assertFalse( anyWithAllDenied.isGranted() );

    var anyWithEmptyDecisions = new AnyAuthorizationDecision( request, Collections.emptySet() );
    assertFalse( anyWithEmptyDecisions.isGranted() );
  }

  @Test
  public void testConstructorWithExplicitGranted() {
    var anyDecision = new AnyAuthorizationDecision( request, true, Set.of( deniedDecision ) );
    assertTrue( anyDecision.isGranted() );

    assertEquals( request, anyDecision.getRequest() );
    assertEquals( 1, anyDecision.getDecisions().size() );
    assertTrue( anyDecision.getDecisions().contains( deniedDecision ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRequestThrows() {
    //noinspection DataFlowIssue
    new AnyAuthorizationDecision( null, Set.of( grantedDecision ) );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullDecisionsThrows() {
    //noinspection DataFlowIssue
    new AnyAuthorizationDecision( request, null );
  }

  @Test
  public void testToStringFormat() {
    var anyGranted = new AnyAuthorizationDecision( request, Set.of( grantedDecision ) );
    var anyDenied = new AnyAuthorizationDecision( request, Set.of( deniedDecision ) );

    var grantedString = anyGranted.toString();
    var deniedString = anyDenied.toString();

    assertTrue( grantedString.contains( "Any[" ) );
    assertTrue( grantedString.contains( "Granted" ) );
    assertTrue( grantedString.contains( "GrantedDecision" ) );

    assertTrue( deniedString.contains( "Any[" ) );
    assertTrue( deniedString.contains( "Denied" ) );
    assertTrue( deniedString.contains( "DeniedDecision" ) );
  }
}
