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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IOpposedAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createMockRule;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class OpposedAuthorizationRuleTest {

  private IAuthorizationRequest request;
  private IAuthorizationContext context;

  private IAuthorizationRule<IResourceAuthorizationRequest> mockOpposedToRule;
  private IAuthorizationDecision mockOpposedToDecision;

  private OpposedAuthorizationRule<IResourceAuthorizationRequest> opposedRule;

  @Before
  public void setUp() {
    var user = createTestUser();
    var action = createTestAction( "read" );

    request = new AuthorizationRequest( user, action );
    context = mock( IAuthorizationContext.class );

    mockOpposedToRule = createMockRule( IResourceAuthorizationRequest.class );

    mockOpposedToDecision = mock( IAuthorizationDecision.class );
    when( mockOpposedToDecision.getRequest() ).thenReturn( request );

    opposedRule = new OpposedAuthorizationRule<>( mockOpposedToRule );
  }

  @Test
  public void testGetRequestTypeIsThatOfTheOpposedToRule() {
    assertEquals( IResourceAuthorizationRequest.class, opposedRule.getRequestType() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRuleThrows() {
    //noinspection DataFlowIssue
    new OpposedAuthorizationRule<>( null );
  }

  @Test
  public void testAuthorizeAbstainsWhenRuleAbstains() {
    when( context.authorizeRule( request, mockOpposedToRule ) ).thenReturn( Optional.empty() );

    var result = opposedRule.authorize( request, context );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAuthorizeOpposesGrantedDecision() {
    when( mockOpposedToDecision.isGranted() ).thenReturn( true );
    when( context.authorizeRule( request, mockOpposedToRule ) ).thenReturn( Optional.of( mockOpposedToDecision ) );

    var result = opposedRule.authorize( request, context );

    assertTrue( result.isPresent() );

    // Should be opposed (false) to granted (true)
    var opposedDecision = (IOpposedAuthorizationDecision) result.get();
    assertFalse( opposedDecision.isGranted() );
  }

  @Test
  public void testAuthorizeOpposesDeniedDecision() {
    when( mockOpposedToDecision.isGranted() ).thenReturn( false );
    when( context.authorizeRule( request, mockOpposedToRule ) ).thenReturn( Optional.of( mockOpposedToDecision ) );

    var result = opposedRule.authorize( request, context );

    assertTrue( result.isPresent() );

    // Should be opposed (true) to denied (false)
    var opposedDecision = (IOpposedAuthorizationDecision) result.get();
    assertTrue( opposedDecision.isGranted() );
  }
}
