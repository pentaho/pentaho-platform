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
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationUser;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractCompositeAuthorizationDecisionTest {

  private IAuthorizationRequest request;
  private IAuthorizationDecision decision1;
  private IAuthorizationDecision decision2;
  private Set<IAuthorizationDecision> decisions;
  private TestCompositeAuthorizationDecision grantedComposite;
  private TestCompositeAuthorizationDecision deniedComposite;

  // Concrete test implementation of AbstractCompositeAuthorizationDecision
  private static class TestCompositeAuthorizationDecision extends AbstractCompositeAuthorizationDecision {
    public TestCompositeAuthorizationDecision( IAuthorizationRequest request,
                                               boolean granted,
                                               Set<IAuthorizationDecision> decisions ) {
      super( request, granted, decisions );
    }
  }

  @Before
  public void setUp() {
    var user = new AuthorizationUser( "test-user", Set.of( new AuthorizationRole( "Administrator" ) ) );

    var action = mock( IAuthorizationAction.class );
    when( action.getName() ).thenReturn( "read" );

    request = new AuthorizationRequest( user, action );

    decision1 = mock( IAuthorizationDecision.class );
    when( decision1.toString() ).thenReturn( "Decision1" );

    decision2 = mock( IAuthorizationDecision.class );
    when( decision2.toString() ).thenReturn( "Decision2" );

    decisions = Set.of( decision1, decision2 );

    grantedComposite = new TestCompositeAuthorizationDecision( request, true, decisions );
    deniedComposite = new TestCompositeAuthorizationDecision( request, false, decisions );
  }

  @Test
  public void testConstructorAndGetters() {
    assertEquals( request, grantedComposite.getRequest() );
    assertTrue( grantedComposite.isGranted() );
    assertEquals( 2, grantedComposite.getDecisions().size() );
    assertTrue( grantedComposite.getDecisions().contains( decision1 ) );
    assertTrue( grantedComposite.getDecisions().contains( decision2 ) );

    assertEquals( request, deniedComposite.getRequest() );
    assertFalse( deniedComposite.isGranted() );
    assertEquals( decisions, deniedComposite.getDecisions() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRequestThrows() {
    new TestCompositeAuthorizationDecision( null, true, decisions );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullDecisionsThrows() {
    new TestCompositeAuthorizationDecision( request, true, null );
  }

  @Test
  public void testGetDecisionsReturnsUnmodifiableSet() {
    var returnedDecisions = grantedComposite.getDecisions();
    try {
      returnedDecisions.add( mock( IAuthorizationDecision.class ) );
      fail( "Decisions set should be unmodifiable" );
    } catch ( UnsupportedOperationException e ) {
      // expected
    }
  }

  @Test
  public void testGetDecisionsLogText() {
    var logText = grantedComposite.getDecisionsLogText();
    assertTrue( logText.contains( "Decision1" ) );
    assertTrue( logText.contains( "Decision2" ) );
    assertTrue( logText.contains( ", " ) );
  }

  @Test
  public void testWithEmptyDecisions() {
    var emptyComposite = new TestCompositeAuthorizationDecision( request, true, Collections.emptySet() );
    assertTrue( emptyComposite.getDecisions().isEmpty() );
    assertEquals( "", emptyComposite.getDecisionsLogText() );
  }
}
