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
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationUser;
import org.pentaho.platform.engine.security.authorization.core.resources.GenericAuthorizationResource;
import org.pentaho.platform.engine.security.authorization.core.resources.ResourceAuthorizationRequest;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;

public class SupportedActionResourceTypeAuthorizationDecisionTest {

  private IResourceAuthorizationRequest request;

  @Before
  public void setUp() {
    var user = new AuthorizationUser( "test-user", Set.of( new AuthorizationRole( "Administrator" ) ) );

    var action = createTestAction( "read" );

    var resource = new GenericAuthorizationResource( "file", "report123" );

    request = new ResourceAuthorizationRequest( user, action, resource );
  }

  @Test
  public void testConstructorAndGetters() {
    var grantedDecision = new SupportedActionResourceTypeAuthorizationDecision( request, true );
    assertEquals( request, grantedDecision.getRequest() );
    assertTrue( grantedDecision.isGranted() );

    var deniedDecision = new SupportedActionResourceTypeAuthorizationDecision( request, false );
    assertEquals( request, deniedDecision.getRequest() );
    assertFalse( deniedDecision.isGranted() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRequestThrows() {
    //noinspection DataFlowIssue
    new SupportedActionResourceTypeAuthorizationDecision( null, true );
  }

  @Test
  public void testToStringFormat() {
    var grantedDecision = new SupportedActionResourceTypeAuthorizationDecision( request, true );
    var deniedDecision = new SupportedActionResourceTypeAuthorizationDecision( request, false );

    var grantedString = grantedDecision.toString();
    var deniedString = deniedDecision.toString();

    assertTrue( grantedString.contains( "SupportedActionResourceTypeAuthorizationDecision" ) );
    assertTrue( grantedString.contains( "Granted" ) );
    assertTrue( grantedString.contains( "action='read-display-name (read)'" ) );
    assertTrue( grantedString.contains( "resourceType='file'" ) );

    assertTrue( deniedString.contains( "SupportedActionResourceTypeAuthorizationDecision" ) );
    assertTrue( deniedString.contains( "Denied" ) );
    assertTrue( deniedString.contains( "action='read-display-name (read)'" ) );
    assertTrue( deniedString.contains( "resourceType='file'" ) );
  }
}
