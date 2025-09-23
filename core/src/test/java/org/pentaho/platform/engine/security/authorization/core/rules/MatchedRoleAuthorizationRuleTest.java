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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.decisions.MatchedRoleAuthorizationDecision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class MatchedRoleAuthorizationRuleTest {

  private IAuthorizationRequest requestWithRole;
  private IAuthorizationRequest requestWithoutRole;
  private IAuthorizationContext context;

  private MatchedRoleAuthorizationRule ruleFromRole;

  @Before
  public void setUp() {
    var principalWithRole = createTestUser( "test-user", "Administrator" );
    var principalWithoutRole = createTestUser( "other-user", "User" );

    var action = createTestAction( "read" );

    requestWithRole = new AuthorizationRequest( principalWithRole, action );
    requestWithoutRole = new AuthorizationRequest( principalWithoutRole, action );
    context = mock( IAuthorizationContext.class );

    ruleFromRole = new MatchedRoleAuthorizationRule( new AuthorizationRole( "Administrator" ) );
  }

  // region NPE / Illegal argument tests
  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRoleNameThrows() {
    //noinspection DataFlowIssue
    new MatchedRoleAuthorizationRule( (String) null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRoleThrows() {
    //noinspection DataFlowIssue
    new MatchedRoleAuthorizationRule( (IAuthorizationRole) null );
  }
  // endregion

  @Test
  public void testGetRequestType() {
    assertEquals( IAuthorizationRequest.class, ruleFromRole.getRequestType() );
  }

  @Test
  public void testAuthorizeGrantsWhenUserHasMatchingRole() {
    var result = ruleFromRole.authorize( requestWithRole, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get() instanceof MatchedRoleAuthorizationDecision );

    var matchedDecision = (MatchedRoleAuthorizationDecision) result.get();
    assertTrue( matchedDecision.isGranted() );
    assertEquals( requestWithRole, matchedDecision.getRequest() );
    assertEquals( "Administrator", matchedDecision.getRole().getName() );
  }

  @Test
  public void testAuthorizeDeniesWhenUserDoesNotHaveMatchingRole() {
    var result = ruleFromRole.authorize( requestWithoutRole, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get() instanceof MatchedRoleAuthorizationDecision );

    var matchedDecision = (MatchedRoleAuthorizationDecision) result.get();
    assertFalse( matchedDecision.isGranted() );
    assertEquals( requestWithoutRole, matchedDecision.getRequest() );
    assertEquals( "Administrator", matchedDecision.getRole().getName() );
  }

  @Test
  public void testAuthorizeWithStringConstructorWhenUserHasMatchingRole() {
    var ruleFromString = new MatchedRoleAuthorizationRule( "Administrator" );

    var result = ruleFromString.authorize( requestWithRole, context );

    assertTrue( result.isPresent() );

    var decision = (MatchedRoleAuthorizationDecision) result.get();

    assertTrue( decision.isGranted() );
    assertEquals( decision.getRole().getName(), decision.getRole().getName() );
  }

  @Test
  public void testAuthorizeWithStringConstructorWhenUserDoesNotHaveMatchingRole() {
    var ruleFromString = new MatchedRoleAuthorizationRule( "Administrator" );

    var result = ruleFromString.authorize( requestWithoutRole, context );

    assertTrue( result.isPresent() );

    var decision = (MatchedRoleAuthorizationDecision) result.get();

    assertFalse( decision.isGranted() );
    assertEquals( decision.getRole().getName(), decision.getRole().getName() );
  }

  @Test
  public void testToStringFormat() {
    var result = ruleFromRole.toString();

    assertTrue( result.contains( "MatchedRoleAuthorizationRule" ) );
    assertTrue( result.contains( "role=Administrator" ) );

    // Test with custom role name
    var customRule = new MatchedRoleAuthorizationRule( new AuthorizationRole( "CustomRole" ) );
    var customResult = customRule.toString();

    assertTrue( customResult.contains( "MatchedRoleAuthorizationRule" ) );
    assertTrue( customResult.contains( "role=CustomRole" ) );
  }
}
