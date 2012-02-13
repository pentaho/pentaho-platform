/*
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
 */
package org.pentaho.platform.repository2.unified.spring;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.test.platform.MethodTrackingData;
import org.pentaho.test.platform.engine.security.MockSecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;

import junit.framework.TestCase;

/**
 * Test cases for the {@link BackingRepositoryLifecycleManagerAuthenticationSuccessListener}
 */
public class BackingRepositoryLifecycleManagerAuthenticationSuccessListenerTest extends TestCase {

  private static final String CURRENT_USER = "test-currentUser";
  private static final String SYSTEM_USER = "system";
  private static final String USER_PARAMETER = "user";

  public void testOnApplicationEvent() throws Exception {
    final BackingRepositoryLifecycleManagerAuthenticationSuccessListener listener = new
        BackingRepositoryLifecycleManagerAuthenticationSuccessListener();

    // Test the getters and setters
    final int order = listener.getOrder() + 1;
    listener.setOrder(order);
    assertEquals(order, listener.getOrder());

    assertEquals(SecurityHelper.getInstance(), listener.getSecurityHelper());
    final MockSecurityHelper mockSecurityHelper = new MockSecurityHelper();
    listener.setSecurityHelper(mockSecurityHelper);
    assertEquals(mockSecurityHelper, listener.getSecurityHelper());

    final MockBackingRepositoryLifecycleManager mockLifecycleManager = new MockBackingRepositoryLifecycleManager(mockSecurityHelper);
    mockLifecycleManager.setThrowException(false);
    listener.setLifecycleManager(mockLifecycleManager);
    assertEquals(mockLifecycleManager, listener.getLifecycleManager());

    // Test that the "newTenant() method is executed as the system currentUser and the
    listener.onApplicationEvent(new MockAbstractAuthenticationEvent(new MockAuthentication(CURRENT_USER)));
    final List<MethodTrackingData> methodTrackerHistory1 = mockLifecycleManager.getMethodTrackerHistory();
    assertEquals(2, methodTrackerHistory1.size());
    assertEquals("newTenant", methodTrackerHistory1.get(0).getMethodName());
    assertEquals(1, methodTrackerHistory1.get(0).getParameters().size());
    assertEquals(SYSTEM_USER, methodTrackerHistory1.get(0).getParameters().get(USER_PARAMETER));
    assertEquals("newUser", methodTrackerHistory1.get(1).getMethodName());
    assertEquals(1, methodTrackerHistory1.get(1).getParameters().size());
    assertEquals(CURRENT_USER, methodTrackerHistory1.get(1).getParameters().get(USER_PARAMETER));

    // Make sure both methods get called when exceptions are thrown
    mockLifecycleManager.resetCallHistory();
    mockLifecycleManager.setThrowException(true);
    listener.onApplicationEvent(new MockAbstractAuthenticationEvent(new MockAuthentication(CURRENT_USER)));

  }

  /**
   * Mock Lifecycle manager - class used in the above testing
   */
  private class MockBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {
    public static final String UNIT_TEST_EXCEPTION_MESSAGE = "Unit Test Exception";
    private ArrayList<MethodTrackingData> methodTrackerHistory = new ArrayList<MethodTrackingData>();
    private boolean throwException = false;
    private MockSecurityHelper securityHelper;

    private MockBackingRepositoryLifecycleManager(final MockSecurityHelper securityHelper) {
      assert (null != securityHelper);
      this.securityHelper = securityHelper;
    }

    public void startup() {
      methodTrackerHistory.add(new MethodTrackingData("startup")
          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser()));
      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
    }

    public void shutdown() {
      methodTrackerHistory.add(new MethodTrackingData("shutdown")
          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser()));
      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
    }

    public void newTenant(final String tenantId) {
      methodTrackerHistory.add(new MethodTrackingData("newTenant")
          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser())
          .addParameter("tenantId", tenantId));
      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
    }

    public void newTenant() {
      methodTrackerHistory.add(new MethodTrackingData("newTenant")
          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser()));
      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
    }

    public void newUser(final String tenantId, final String username) {
      methodTrackerHistory.add(new MethodTrackingData("newUser")
          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser())
          .addParameter("tenantId", tenantId)
          .addParameter("username", username));
      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
    }

    public void newUser() {
      methodTrackerHistory.add(new MethodTrackingData("newUser")
          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser()));
      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
    }

    public void resetCallHistory() {
      this.methodTrackerHistory.clear();
    }

    public boolean isThrowException() {
      return throwException;
    }

    public void setThrowException(final boolean throwException) {
      this.throwException = throwException;
    }

    public ArrayList<MethodTrackingData> getMethodTrackerHistory() {
      return (ArrayList<MethodTrackingData>) methodTrackerHistory.clone();
    }
  }

  /**
   * Mock class used for testing
   */
  private class MockAuthentication implements Authentication {
    private String currentUser;

    public MockAuthentication(final String currentUser) {
      this.currentUser = currentUser;
    }

    public GrantedAuthority[] getAuthorities() {
      return null;
    }

    public Object getCredentials() {
      return null;
    }

    public Object getDetails() {
      return null;
    }

    public Object getPrincipal() {
      return null;
    }

    public boolean isAuthenticated() {
      return true;
    }

    public void setAuthenticated(final boolean b) throws IllegalArgumentException {
    }

    public String getName() {
      return currentUser;
    }
  }

  /**
   * Mock class used for testing
   */
  private class MockAbstractAuthenticationEvent extends AuthenticationSuccessEvent {
    public MockAbstractAuthenticationEvent(final MockAuthentication mockAuthentication) {
      super(mockAuthentication);
    }
  }
}
