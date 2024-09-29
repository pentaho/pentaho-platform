/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.apache.jackrabbit.api.XASession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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