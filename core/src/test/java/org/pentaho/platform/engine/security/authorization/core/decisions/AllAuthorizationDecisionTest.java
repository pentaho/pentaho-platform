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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AllAuthorizationDecisionTest {

  private IAuthorizationRequest request;
  private IAuthorizationDecision grantedDecision;
  private IAuthorizationDecision deniedDecision;

  @Before
  public void setUp() {
    var user = new AuthorizationUser( "test-user", Set.of( new AuthorizationRole( "Administrator" ) ) );

    var action = mock( IAuthorizationAction.class );
    when( action.getName() ).thenReturn( "read" );

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
    var allWithMixed = new AllAuthorizationDecision( request, Set.of( grantedDecision, deniedDecision ) );
    assertFalse( allWithMixed.isGranted() );

    // ---

    var grantedDecision2 = mock( IAuthorizationDecision.class );
    when( grantedDecision2.isGranted() ).thenReturn( true );

    var allWithGranted = new AllAuthorizationDecision( request, Set.of( grantedDecision, grantedDecision2 ) );
    assertTrue( allWithGranted.isGranted() );

    // ---

    var deniedDecision2 = mock( IAuthorizationDecision.class );
    when( deniedDecision2.isGranted() ).thenReturn( false );

    var allDeniedDecision = new AllAuthorizationDecision( request, Set.of( deniedDecision, deniedDecision2 ) );
    assertFalse( allDeniedDecision.isGranted() );

    // ---

    var allEmptyDecision = new AllAuthorizationDecision( request, Collections.emptySet() );
    assertTrue( allEmptyDecision.isGranted() );
  }

  @Test
  public void testConstructorWithExplicitGranted() {
    var allDecision = new AllAuthorizationDecision( request, true, Set.of( deniedDecision ) );

    assertTrue( allDecision.isGranted() );
    assertEquals( request, allDecision.getRequest() );
    assertEquals( 1, allDecision.getDecisions().size() );
    assertTrue( allDecision.getDecisions().contains( deniedDecision ) );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullRequestThrows() {
    //noinspection DataFlowIssue
    new AllAuthorizationDecision( null, Set.of( grantedDecision ) );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullDecisionsThrows() {
    //noinspection DataFlowIssue
    new AllAuthorizationDecision( request, null );
  }

  @Test
  public void testToStringFormat() {
    var allGranted = new AllAuthorizationDecision( request, Set.of( grantedDecision ) );
    var allDenied = new AllAuthorizationDecision( request, Set.of( deniedDecision ) );

    var grantedString = allGranted.toString();
    var deniedString = allDenied.toString();

    assertTrue( grantedString.contains( "All[" ) );
    assertTrue( grantedString.contains( "Granted" ) );
    assertTrue( grantedString.contains( "GrantedDecision" ) );

    assertTrue( deniedString.contains( "All[" ) );
    assertTrue( deniedString.contains( "Denied" ) );
    assertTrue( deniedString.contains( "DeniedDecision" ) );
  }
}
