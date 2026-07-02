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
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.decisions.ResourceActionGeneralRequirementAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.resources.GenericAuthorizationResource;
import org.pentaho.platform.engine.security.authorization.core.resources.ResourceAuthorizationRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class ResourceActionGeneralRequirementAuthorizationRuleTest {

  private IResourceAuthorizationRequest resourceRequest;
  private IAuthorizationContext context;
  private IAuthorizationRequest generalRequest;

  private ResourceActionGeneralRequirementAuthorizationRule rule;

  @Before
  public void setUp() {
    context = mock( IAuthorizationContext.class );

    var user = createTestUser();
    var action = createTestAction( "read" );
    var resource = new GenericAuthorizationResource( "file", "report123" );

    resourceRequest = spy( new ResourceAuthorizationRequest( user, action, resource ) );

    // Create the general request that asGeneral() should return.
    generalRequest = new AuthorizationRequest( user, action );

    // Mock asGeneral() to return our controlled general request.
    when( resourceRequest.asGeneral() ).thenReturn( generalRequest );

    rule = new ResourceActionGeneralRequirementAuthorizationRule();
  }

  @Test
  public void testGetRequestType() {
    assertEquals( IResourceAuthorizationRequest.class, rule.getRequestType() );
  }

  @Test
  public void testAuthorizeWithGrantedGeneralDecision() {
    var grantedGeneralDecision = mock( IAuthorizationDecision.class );
    when( grantedGeneralDecision.isGranted() ).thenReturn( true );
    when( grantedGeneralDecision.getRequest() ).thenReturn( generalRequest );

    when( context.authorize( generalRequest ) ).thenReturn( grantedGeneralDecision );

    var result = rule.authorize( resourceRequest, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get() instanceof ResourceActionGeneralRequirementAuthorizationDecision );

    var decision = (ResourceActionGeneralRequirementAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( resourceRequest, decision.getRequest() );
    assertEquals( grantedGeneralDecision, decision.getDerivedFromDecision() );
  }

  @Test
  public void testAuthorizeWithDeniedGeneralDecision() {
    var deniedGeneralDecision = mock( IAuthorizationDecision.class );
    when( deniedGeneralDecision.isGranted() ).thenReturn( false );
    when( deniedGeneralDecision.getRequest() ).thenReturn( generalRequest );

    when( context.authorize( generalRequest ) ).thenReturn( deniedGeneralDecision );

    var result = rule.authorize( resourceRequest, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get() instanceof ResourceActionGeneralRequirementAuthorizationDecision );

    var decision = (ResourceActionGeneralRequirementAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertEquals( resourceRequest, decision.getRequest() );
    assertEquals( deniedGeneralDecision, decision.getDerivedFromDecision() );
  }

  @Test
  public void testToStringFormat() {
    var result = rule.toString();

    assertTrue( result.contains( ResourceActionGeneralRequirementAuthorizationRule.class.getSimpleName() ) );
  }
}
