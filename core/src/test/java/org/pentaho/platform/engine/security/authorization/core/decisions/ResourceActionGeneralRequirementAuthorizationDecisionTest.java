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

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceActionGeneralRequirementAuthorizationDecisionTest {

  private IAuthorizationRequest request1;
  private IAuthorizationRequest request2;
  private IAuthorizationDecision grantedDecision;
  private IAuthorizationDecision deniedDecision;

  @Before
  public void setUp() {
    var user = new AuthorizationUser( "test-user", Set.of( new AuthorizationRole( "Administrator" ) ) );

    var action1 = mock( IAuthorizationAction.class );
    when( action1.getName() ).thenReturn( "general-read" );
    request1 = new AuthorizationRequest( user, action1 );

    var action2 = mock( IAuthorizationAction.class );
    when( action2.getName() ).thenReturn( "resource-read" );
    request2 = new AuthorizationRequest( user, action2 );

    grantedDecision = mock( IAuthorizationDecision.class );
    when( grantedDecision.isGranted() ).thenReturn( true );
    when( grantedDecision.getRequest() ).thenReturn( request1 );
    when( grantedDecision.toString() ).thenReturn( "GrantedGeneralDecision" );

    deniedDecision = mock( IAuthorizationDecision.class );
    when( deniedDecision.isGranted() ).thenReturn( false );
    when( deniedDecision.getRequest() ).thenReturn( request1 );
    when( deniedDecision.toString() ).thenReturn( "DeniedGeneralDecision" );
  }

  @Test
  public void testConstructorAndGetters() {
    var grantedRequirementDecision =
      new ResourceActionGeneralRequirementAuthorizationDecision( request2, grantedDecision );
    assertEquals( request2, grantedRequirementDecision.getRequest() );
    assertTrue( grantedRequirementDecision.isGranted() );
    assertSame( grantedDecision, grantedRequirementDecision.getDerivedFromDecision() );

    var deniedRequirementDecision =
      new ResourceActionGeneralRequirementAuthorizationDecision( request2, deniedDecision );
    assertEquals( request2, deniedRequirementDecision.getRequest() );
    assertFalse( deniedRequirementDecision.isGranted() );
    assertSame( deniedDecision, deniedRequirementDecision.getDerivedFromDecision() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRequestThrows() {
    //noinspection DataFlowIssue
    new ResourceActionGeneralRequirementAuthorizationDecision( null, grantedDecision );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullDerivedFromDecisionThrows() {
    //noinspection DataFlowIssue
    new ResourceActionGeneralRequirementAuthorizationDecision( request2, null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithSameRequestThrows() {
    new ResourceActionGeneralRequirementAuthorizationDecision( request1, grantedDecision );
  }

  @Test
  public void testGetShortJustification() {
    var requirementDecision = new ResourceActionGeneralRequirementAuthorizationDecision( request2, grantedDecision );
    var justification = requirementDecision.getShortJustification();
    assertNotNull( justification );
    assertFalse( justification.isEmpty() );
  }

  @Test
  public void testToStringFormat() {
    var grantedRequirementDecision =
      new ResourceActionGeneralRequirementAuthorizationDecision( request2, grantedDecision );
    var deniedRequirementDecision =
      new ResourceActionGeneralRequirementAuthorizationDecision( request2, deniedDecision );

    var grantedString = grantedRequirementDecision.toString();
    var deniedString = deniedRequirementDecision.toString();

    assertTrue( grantedString.contains( "ResourceActionGeneralRequirementAuthorizationDecision" ) );
    assertTrue( grantedString.contains( "Granted" ) );
    assertTrue( grantedString.contains( "from:" ) );
    assertTrue( grantedString.contains( "GrantedGeneralDecision" ) );

    assertTrue( deniedString.contains( "ResourceActionGeneralRequirementAuthorizationDecision" ) );
    assertTrue( deniedString.contains( "Denied" ) );
    assertTrue( deniedString.contains( "from:" ) );
    assertTrue( deniedString.contains( "DeniedGeneralDecision" ) );
  }
}
