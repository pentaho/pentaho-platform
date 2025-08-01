package org.pentaho.platform.engine.security.authorization.core;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthorizationRequestTest {

  private AuthorizationUser user;

  @Before
  public void setUp() {
    user = new AuthorizationUser( "test-user", Set.of( new AuthorizationRole( "Administrator" ) ) );
  }

  @Test
  public void testConstructorAndGetters() {
    var action = mock( IAuthorizationAction.class );
    when( action.getName() ).thenReturn( "read" );

    var request = new AuthorizationRequest( user, action );

    assertEquals( user, request.getUser() );
    assertEquals( action, request.getAction() );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullUserThrows() {
    var action = mock( IAuthorizationAction.class );
    new AuthorizationRequest( null, action );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullActionThrows() {
    new AuthorizationRequest( user, null );
  }

  @Test
  public void testWithAction() {
    var action1 = mock( IAuthorizationAction.class );
    var action2 = mock( IAuthorizationAction.class );
    when( action1.getName() ).thenReturn( "read" );
    when( action2.getName() ).thenReturn( "write" );

    var request1 = new AuthorizationRequest( user, action1 );
    var request2 = request1.withAction( action2 );

    assertEquals( user, request2.getUser() );
    assertEquals( action2, request2.getAction() );
    assertNotSame( request1, request2 );
  }

  @Test
  public void testEqualsAndHashCode() {
    var action1 = mock( IAuthorizationAction.class );
    when( action1.getName() ).thenReturn( "read" );

    var action2 = mock( IAuthorizationAction.class );
    when( action2.getName() ).thenReturn( "read" );

    var action3 = mock( IAuthorizationAction.class );
    when( action3.getName() ).thenReturn( "write" );

    var request1 = new AuthorizationRequest( user, action1 );
    var request2 = new AuthorizationRequest( user, action2 );
    var request3 = new AuthorizationRequest( user, action3 );

    assertEquals( request1, request2 );
    assertEquals( request1.hashCode(), request2.hashCode() );

    // Different action.
    assertNotEquals( request1, request3 );
    assertNotEquals( request1.hashCode(), request3.hashCode() );

    var notRequest = new Object();
    assertNotEquals( request1, notRequest );
  }

  @Test
  public void testToStringFormat() {
    var action = mock( IAuthorizationAction.class );
    when( action.getName() ).thenReturn( "read" );

    var request = new AuthorizationRequest( user, action );
    var result = request.toString();

    assertTrue( result.contains( "AuthorizationRequest" ) );
    assertTrue( result.contains( "test-user" ) );
    assertTrue( result.contains( "read" ) );
  }

  @Test
  public void testImplementsInterface() {
    var action = mock( IAuthorizationAction.class );

    var request = new AuthorizationRequest( user, action );

    assertTrue( request instanceof IAuthorizationRequest );
  }
}
