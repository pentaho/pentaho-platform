package org.pentaho.platform.engine.security.authorization.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AbstractAuthorizationUserTest {

  private static class TestUser extends AbstractAuthorizationUser {
    private final String name;

    public TestUser( String name ) {
      this.name = name;
    }

    @NonNull
    @Override
    public String getName() {
      return name;
    }

    @NonNull
    @Override
    public Set<IAuthorizationRole> getRoles() {
      return Set.of();
    }
  }

  @Test
  public void testEqualsAndHashCode() {
    var user1 = new TestUser( "user1" );
    var user2 = new TestUser( "user1" );
    var user3 = new TestUser( "user2" );

    assertEquals( user1, user2 );
    assertNotEquals( user1, user3 );
    assertEquals( user1.hashCode(), user2.hashCode() );
    assertNotEquals( user1.hashCode(), user3.hashCode() );
  }
}
