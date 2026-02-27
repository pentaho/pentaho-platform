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
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.decisions.SupportedActionResourceTypeAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.resources.GenericAuthorizationResource;
import org.pentaho.platform.engine.security.authorization.core.resources.ResourceAuthorizationRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createResourceAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class SupportedActionResourceTypeAuthorizationRuleTest {

  private IResourceAuthorizationRequest supportedRequest;
  private IResourceAuthorizationRequest unsupportedRequest;
  private IAuthorizationContext context;

  private SupportedActionResourceTypeAuthorizationRule rule;

  @Before
  public void setUp() {
    var user = createTestUser();

    var supportedAction = createResourceAction( "read", "file" );
    var unsupportedAction = createResourceAction( "write", "database" );

    var resource = new GenericAuthorizationResource( "file", "report123" );

    supportedRequest = new ResourceAuthorizationRequest( user, supportedAction, resource );
    unsupportedRequest = new ResourceAuthorizationRequest( user, unsupportedAction, resource );
    context = mock( IAuthorizationContext.class );

    rule = new SupportedActionResourceTypeAuthorizationRule();
  }

  @Test
  public void testGetRequestType() {
    assertEquals( IResourceAuthorizationRequest.class, rule.getRequestType() );
  }

  @Test
  public void testAuthorizeAbstainsWhenActionSupportsResourceType() {
    var result = rule.authorize( supportedRequest, context );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAuthorizeGrantsWhenActionDoesNotSupportResourceType() {
    var result = rule.authorize( unsupportedRequest, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get() instanceof SupportedActionResourceTypeAuthorizationDecision );

    var decision = (SupportedActionResourceTypeAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertEquals( unsupportedRequest, decision.getRequest() );
  }

  @Test
  public void testToStringFormat() {
    var result = rule.toString();

    assertTrue( result.contains( SupportedActionResourceTypeAuthorizationRule.class.getSimpleName() ) );
  }
}
