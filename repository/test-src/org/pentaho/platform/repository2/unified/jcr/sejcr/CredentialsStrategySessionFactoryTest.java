package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.apache.jackrabbit.api.XASession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Repository;
import javax.jcr.Session;
import java.lang.reflect.Proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.*;

/**
 * Created by nbaker on 7/21/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class CredentialsStrategySessionFactoryTest {

  private Session sessionProxy;

  @Mock
  private Session underlyingSession;

  @Test
  public void createSessionProxy() throws Exception {
    assertThat( sessionProxy, allOf( instanceOf( XASession.class ), instanceOf( Session.class ) ) );
  }

  @Before
  public void setup() {
    CredentialsStrategySessionFactory factory =
        new CredentialsStrategySessionFactory( mock( Repository.class ), mock( CredentialsStrategy.class ) );
    sessionProxy = factory.createSessionProxy( underlyingSession );
  }

  @Test
  public void testSessionProxy() throws Exception {
    CredentialsStrategySessionFactory.LogoutSuppressingInvocationHandler concreteProxy =
        (CredentialsStrategySessionFactory.LogoutSuppressingInvocationHandler)
            Proxy.getInvocationHandler( sessionProxy );
    TestLogoutHandler logoutHandler = new TestLogoutHandler();
    logoutHandler.setLogout( false );
    concreteProxy.setLogoutDelegate( logoutHandler );

    // This should be swallowed
    sessionProxy.logout();
    verify( underlyingSession, times( 0 ) ).logout();

    logoutHandler.setLogout( true );
    // This should hit the real session
    sessionProxy.logout();
    verify( underlyingSession, times( 1 ) ).logout();

  }

  private class TestLogoutHandler
      implements CredentialsStrategySessionFactory.LogoutSuppressingInvocationHandler.LogoutDelegate {


    private boolean logout;

    @Override public boolean shouldLogout() {
      return logout;
    }

    public void setLogout( boolean logout ) {

      this.logout = logout;
    }
  }

}