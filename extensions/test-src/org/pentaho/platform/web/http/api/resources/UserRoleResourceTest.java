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
 *
 * Copyright 2012 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;

import junit.framework.TestCase;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class UserRoleResourceTest extends TestCase {

  public void testConstruction() throws Exception {
    // Failures
    try {
      new UserRoleResource(null);
      fail("Passing a null reference is invalid");
    } catch (IllegalArgumentException success) {
    }

    try {
      new UserRoleResource();
      fail("The use of PentahoSystem should throw an exception since it has not been setup correctly");
    } catch (Throwable success) {
    }

    // Successful
    new UserRoleResource(new MockRoleAuthorizationPolicyRoleBindingDao());

    PentahoSystem.setObjectFactory(new MockPentahoObjectFactory());
    new UserRoleResource();
  }

  public void getAllUsers() throws Exception {

  }


  private class MockRoleAuthorizationPolicyRoleBindingDao implements IRoleAuthorizationPolicyRoleBindingDao {
    @Override
    public RoleBindingStruct getRoleBindingStruct(final String locale) {
      return null;
    }

    @Override
    public void setRoleBindings(final String runtimeRoleName, final List<String> logicalRolesNames) {
    }

    @Override
    public List<String> getBoundLogicalRoleNames(final List<String> runtimeRoleNames) {
      return null;
    }
  }

  private static class MockUserRoleListService implements IUserRoleListService {

    private static int userCount = 0;
    private static final String[][] userData = new String[][] {
        new String[] { "abby", "a" },
        new String[] { "blank" },
        new String[] { "cathy", "c", "dev" },
        new String[] { "danielle", "d", "dev" },
        new String[] { "elle", "e", "dev", "ceo" },
        new String[] { "francine", "f" },
        new String[] { "gina", "g" },
        new String[] { "helen", "h" },
    };

    private static final List<String> userRoles = new ArrayList<String>();

    static {
      userRoles.add("a");
      userRoles.add("b");
      userRoles.add("c");
      userRoles.add("d");
      userRoles.add("e");
      userRoles.add("f");
      userRoles.add("g");
      userRoles.add("h");
      userRoles.add("dev");
      userRoles.add("ceo");
    };

    public static void setUserCount(final int userCount) {
      MockUserRoleListService.userCount = Math.max(userData.length, Math.min(0, userCount));
    }

    public static int getUserCount() {
      return userCount;
    }

    public static int getMaxUsers() {
      return userData.length;
    }


    @Override
    public List<String> getAllRoles() {
      return new ArrayList(userRoles);
    }

    @Override
    public List<String> getAllUsers() {
      final List<String> result = new ArrayList<String>(userCount);
      for (int i = 0; i < userCount; ++i) {
        result.add(userData[i][0]);
      }
      return result;
    }

    @Override
    public List<String> getUsersInRole(final String role) {
      return null;
    }

    @Override
    public List<String> getRolesForUser(final String username) {
      return null;
    }
  }

  private class MockPentahoObjectFactory implements IPentahoObjectFactory {
    @Override
    public <T> T get(final Class<T> interfaceClass, final IPentahoSession session) throws ObjectFactoryException {
      if (interfaceClass.equals(IRoleAuthorizationPolicyRoleBindingDao.class)) {
        return (T) new MockRoleAuthorizationPolicyRoleBindingDao();
      }
      return null;
    }

    @Override
    public <T> T get(final Class<T> interfaceClass, final String key, final IPentahoSession session) throws ObjectFactoryException {
      if (interfaceClass.equals(IRoleAuthorizationPolicyRoleBindingDao.class)) {
        return (T) new MockRoleAuthorizationPolicyRoleBindingDao();
      }
      return null;
    }

    @Override
    public boolean objectDefined(final String key) {
      return StringUtils.equals(key, "IRoleAuthorizationPolicyRoleBindingDao");
    }

    @Override
    public Class<?> getImplementingClass(final String key) {
      return StringUtils.equals(key, "IRoleAuthorizationPolicyRoleBindingDao") ? MockRoleAuthorizationPolicyRoleBindingDao.class : null;
    }

    @Override
    public void init(final String configFile, final Object context) {
    }
  }
}
