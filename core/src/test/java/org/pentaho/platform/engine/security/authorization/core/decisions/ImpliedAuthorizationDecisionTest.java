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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImpliedAuthorizationDecisionTest {

  private IAuthorizationRequest request1;
  private IAuthorizationRequest request2;
  private IAuthorizationDecision grantedDecision;
  private IAuthorizationDecision deniedDecision;

  @Before
  public void setUp() {
    var user = new AuthorizationUser( "test-user", Set.of( new AuthorizationRole( "Administrator" ) ) );

    var action1 = mock( IAuthorizationAction.class );
    when( action1.getName() ).thenReturn( "read" );
    request1 = new AuthorizationRequest( user, action1 );

    var action2 = mock( IAuthorizationAction.class );
    when( action2.getName() ).thenReturn( "write" );
    request2 = new AuthorizationRequest( user, action2 );

    grantedDecision = mock( IAuthorizationDecision.class );
    when( grantedDecision.isGranted() ).thenReturn( true );
    when( grantedDecision.getRequest() ).thenReturn( request1 );
    when( grantedDecision.toString() ).thenReturn( "GrantedDecision" );

    deniedDecision = mock( IAuthorizationDecision.class );
    when( deniedDecision.isGranted() ).thenReturn( false );
    when( deniedDecision.getRequest() ).thenReturn( request1 );
    when( deniedDecision.toString() ).thenReturn( "DeniedDecision" );
  }

  @Test
  public void testConstructorAndGetters() {
    var impliedGranted = new ImpliedAuthorizationDecision( request2, grantedDecision );
    assertEquals( request2, impliedGranted.getRequest() );
    assertTrue( impliedGranted.isGranted() );
    assertEquals( grantedDecision, impliedGranted.getImpliedFromDecision() );

    var impliedDenied = new ImpliedAuthorizationDecision( request2, deniedDecision );
    assertEquals( request2, impliedDenied.getRequest() );
    assertFalse( impliedDenied.isGranted() );
    assertEquals( deniedDecision, impliedDenied.getImpliedFromDecision() );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullRequestThrows() {
    //noinspection DataFlowIssue
    new ImpliedAuthorizationDecision( null, grantedDecision );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullImpliedFromDecisionThrows() {
    //noinspection DataFlowIssue
    new ImpliedAuthorizationDecision( request2, null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithSameRequestThrows() {
    new ImpliedAuthorizationDecision( request1, grantedDecision );
  }

  @Test
  public void testGetShortJustification() {
    var impliedDecision = new ImpliedAuthorizationDecision( request2, grantedDecision );
    var justification = impliedDecision.getShortJustification();
    assertNotNull( justification );
    assertFalse( justification.isEmpty() );
  }

  @Test
  public void testToStringFormat() {
    var impliedGranted = new ImpliedAuthorizationDecision( request2, grantedDecision );
    var impliedDenied = new ImpliedAuthorizationDecision( request2, deniedDecision );

    var grantedString = impliedGranted.toString();
    var deniedString = impliedDenied.toString();

    assertTrue( grantedString.contains( "Implied[" ) );
    assertTrue( grantedString.contains( "Granted" ) );
    assertTrue( grantedString.contains( "from:" ) );
    assertTrue( grantedString.contains( "GrantedDecision" ) );

    assertTrue( deniedString.contains( "Implied[" ) );
    assertTrue( deniedString.contains( "Denied" ) );
    assertTrue( deniedString.contains( "from:" ) );
    assertTrue( deniedString.contains( "DeniedDecision" ) );
  }
}
