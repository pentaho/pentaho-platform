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

package org.pentaho.platform.engine.security.authorization.core.rules;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.decisions.DerivedActionAuthorizationDecision;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class DerivedActionAuthorizationRuleTest {

  private IAuthorizationRequest request;
  private IAuthorizationContext context;
  private IAuthorizationUser user;
  private IAuthorizationRequest baseRequest;
  private IAuthorizationAction baseAction;
  private IAuthorizationAction derivedAction;

  private IAuthorizationDecision baseDecision;

  private DerivedActionAuthorizationRule singleDerivedRule;

  @Before
  public void setUp() {
    user = createTestUser();

    baseAction = createTestAction( "base-action" );
    derivedAction = createTestAction( "derived-action" );

    request = new AuthorizationRequest( user, derivedAction );
    baseRequest = new AuthorizationRequest( user, baseAction );

    baseDecision = mock( IAuthorizationDecision.class );
    when( baseDecision.getRequest() ).thenReturn( baseRequest );

    singleDerivedRule = new DerivedActionAuthorizationRule( baseAction, derivedAction );

    context = mock( IAuthorizationContext.class );
  }

  // region null argument tests
  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullBaseActionThrows() {
    //noinspection DataFlowIssue
    new DerivedActionAuthorizationRule( null, derivedAction );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullDerivedActionThrows() {
    //noinspection DataFlowIssue
    new DerivedActionAuthorizationRule( baseAction, (IAuthorizationAction) null );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullDerivedActionsSetThrows() {
    //noinspection DataFlowIssue
    new DerivedActionAuthorizationRule( baseAction, (Set<IAuthorizationAction>) null );
  }

  @Test( expected = NullPointerException.class )
  public void testAuthorizeWithNullRequestThrows() {
    //noinspection DataFlowIssue
    singleDerivedRule.authorize( null, context );
  }

  @Test( expected = NullPointerException.class )
  public void testAuthorizeWithNullContextThrows() {
    //noinspection DataFlowIssue
    singleDerivedRule.authorize( request, null );
  }
  // endregion

  @Test
  public void testGetRequestType() {
    assertEquals( IAuthorizationRequest.class, singleDerivedRule.getRequestType() );
  }

  @Test
  public void testAuthorizeAbstainsForNonDerivedAction() {
    var otherAction = mock( IAuthorizationAction.class );
    when( otherAction.getName() ).thenReturn( "other-action" );

    var otherRequest = new AuthorizationRequest( createTestUser(), otherAction );

    var result = singleDerivedRule.authorize( otherRequest, context );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAuthorizeGrantsWhenBaseActionIsGranted() {
    when( baseDecision.isGranted() ).thenReturn( true );
    when( context.authorize( baseRequest ) ).thenReturn( baseDecision );

    var result = singleDerivedRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get() instanceof DerivedActionAuthorizationDecision );

    var derivedDecision = (DerivedActionAuthorizationDecision) result.get();
    assertTrue( derivedDecision.isGranted() );
    assertEquals( baseDecision, derivedDecision.getDerivedFromDecision() );
    assertEquals( baseAction, derivedDecision.getDerivedFromAction() );
  }

  @Test
  public void testAuthorizeDeniesWhenBaseActionIsDenied() {
    when( baseDecision.isGranted() ).thenReturn( false );
    when( context.authorize( baseRequest ) ).thenReturn( baseDecision );

    var result = singleDerivedRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get() instanceof DerivedActionAuthorizationDecision );

    var derivedDecision = (DerivedActionAuthorizationDecision) result.get();
    assertFalse( derivedDecision.isGranted() );
    assertEquals( baseDecision, derivedDecision.getDerivedFromDecision() );
    assertEquals( baseAction, derivedDecision.getDerivedFromAction() );
  }

  @Test
  public void testMultipleDerivedActionsSupported() {
    var derivedAction2 = mock( IAuthorizationAction.class );
    when( derivedAction2.getName() ).thenReturn( "derived-action-2" );

    var multipleDerivedRule = new DerivedActionAuthorizationRule( baseAction, Set.of( derivedAction, derivedAction2 ) );

    // ---

    var request2 = new AuthorizationRequest( createTestUser(), derivedAction2 );

    when( baseDecision.isGranted() ).thenReturn( true );
    when( context.authorize( any( IAuthorizationRequest.class ) ) ).thenReturn( baseDecision );

    // Test first derived action
    var result1 = multipleDerivedRule.authorize( request, context );
    assertTrue( result1.isPresent() );
    assertTrue( result1.get().isGranted() );

    // Test second derived action
    var result2 = multipleDerivedRule.authorize( request2, context );
    assertTrue( result2.isPresent() );
    assertTrue( result2.get().isGranted() );
  }

  @Test
  public void testAuthorizeSupportsDifferentDerivedActionInstances() {
    when( baseDecision.isGranted() ).thenReturn( true );
    when( context.authorize( baseRequest ) ).thenReturn( baseDecision );

    // A different instance of the derived action, `derivedAction`, with same name.
    var derivedAction2 = createTestAction( "derived-action" );

    var request2 = new AuthorizationRequest( user, derivedAction2 );

    var result = singleDerivedRule.authorize( request2, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get() instanceof DerivedActionAuthorizationDecision );

    var derivedDecision = (DerivedActionAuthorizationDecision) result.get();
    assertTrue( derivedDecision.isGranted() );
    assertSame( baseDecision, derivedDecision.getDerivedFromDecision() );
    assertSame( baseAction, derivedDecision.getDerivedFromAction() );
  }

  @Test
  public void testToStringFormat() {
    var result = singleDerivedRule.toString();

    assertTrue( result.contains( "DerivedActionAuthorizationRule" ) );
    assertTrue( result.contains( "base=base-action" ) );
    assertTrue( result.contains( "derived=derived-action" ) );

    // Test with multiple derived actions
    var derivedAction2 = createTestAction( "derived-action-2" );

    var multipleDerivedRule = new DerivedActionAuthorizationRule( baseAction, Set.of( derivedAction, derivedAction2 ) );
    var multipleResult = multipleDerivedRule.toString();

    assertTrue( multipleResult.contains( "DerivedActionAuthorizationRule" ) );
    assertTrue( multipleResult.contains( "base=base-action" ) );
    assertTrue( multipleResult.contains( "derived-action" ) );
    assertTrue( multipleResult.contains( "derived-action-2" ) );
  }
}
