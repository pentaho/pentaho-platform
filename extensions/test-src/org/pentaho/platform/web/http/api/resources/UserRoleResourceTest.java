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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;
import org.pentaho.platform.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;

import edu.emory.mathcs.backport.java.util.Arrays;

public class UserRoleResourceTest {
	private static final String[][] testUserData = new String[][] {
			new String[] { "abby", "a" }, new String[] { "blank" },
			new String[] { "cathy", "c", "dev" },
			new String[] { "danielle", "d", "dev" },
			new String[] { "elle", "e", "dev", "ceo" },
			new String[] { "francine", "f" }, new String[] { "gina", "g" },
			new String[] { "helen", "h" }, };

	private static final String[] testRoleData = new String[] { "a", "b", "c",
			"d", "e", "f", "g", "h", "dev", "ceo" };

	@Test
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

	private class MockRoleAuthorizationPolicyRoleBindingDao implements
			IRoleAuthorizationPolicyRoleBindingDao {
		private RoleBindingStruct roleBindingStruct;
		/**
		 * Keys are logical role names and values are localized logical role names.
		 * For now making the keys and values match.
		 */
		private Map<String, String> logicalRoleNameMap = new HashMap<String, String>();
		/**
		 * Keys are runtime role names and values are lists of logical role names;
		 */
		private Map<String, List<String>> bindingMap = new HashMap<String, List<String>>();

		public MockRoleAuthorizationPolicyRoleBindingDao() {
			final UserRoleResourceTest.MockUserRoleListService murl = new UserRoleResourceTest.MockUserRoleListService();
			List<String> logicalRoles = murl.getAllRoles();
			for (String role : logicalRoles) {
				logicalRoleNameMap.put(role, role);
				ArrayList<String> rolelist = new ArrayList<String>();
				rolelist.add(role);
				bindingMap.put(role, rolelist);
				logicalRoleNameMap.put(role, role);
			}
			roleBindingStruct = new RoleBindingStruct(logicalRoleNameMap, bindingMap);
		}

		@Override
		public RoleBindingStruct getRoleBindingStruct(final String locale) {
			return roleBindingStruct;
		}

		@Override
		public void setRoleBindings(final String runtimeRoleName,
				final List<String> logicalRolesNames) {
		}

		@Override
		public List<String> getBoundLogicalRoleNames(
				final List<String> runtimeRoleNames) {
			// not completed
			return null;
		}
	}

	private static class MockUserRoleListService implements IUserRoleListService {

		private static int userCount = 0;
		private static final String[][] userData = testUserData;
		private static final List<String> userRoles = Arrays.asList(testRoleData);

		static {
			setUserCount(0);
		};

		public static void setUserCount(final int userCount) {
			MockUserRoleListService.userCount = Math.max(userData.length,
					Math.min(0, userCount));
		}

		public static int getUserCount() {
			return userCount;
		}

		public static int getRoleCount() {
			return userRoles.size();
		}

		@Override
		public List<String> getAllRoles() {
			return new ArrayList<String>(userRoles);
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
			final List<String> result = new ArrayList<String>();
			for (int i = 0; i < userCount; ++i) {
				for (int j = 1; j < userData[i].length; ++j) {
					if (userData[i][j].equals(role)) {
						result.add(userData[i][j]);
					}
				}
			}
			return result;
		}

		@Override
		public List<String> getRolesForUser(final String username) {
			final List<String> result = new ArrayList<String>();
			for (int i = 0; i < userCount; ++i) {
				if (userData[i][0].equals(username)) {
					for (int j = 1; j < userData[i].length; ++j) {
						result.add(userData[i][j]);
					}
				}
			}
			return result;
		}
	}

	private static class MockUserRoleDao implements IUserRoleDao {

		private static List<IPentahoUser> users = new ArrayList<IPentahoUser>();
		private static List<IPentahoRole> roles = new ArrayList<IPentahoRole>();
		static {
			for (int i = 0; i < testUserData.length; ++i) {
				users.add(new PentahoUser(testUserData[i][0]));
			}
			for (int i = 0; i < testRoleData.length; ++i) {
				roles.add(new PentahoRole(testRoleData[i]));
			}
		}

		public void createRole(IPentahoRole newRole) throws AlreadyExistsException,
				UncategorizedUserRoleDaoException {
			roles.add(newRole);
		}

		public void createUser(IPentahoUser newUser) throws AlreadyExistsException,
				UncategorizedUserRoleDaoException {
			users.add(newUser);
		}

		public void deleteRole(IPentahoRole role) throws NotFoundException,
				UncategorizedUserRoleDaoException {
			roles.remove(role);
		}

		public void deleteUser(IPentahoUser user) throws NotFoundException,
				UncategorizedUserRoleDaoException {
			users.remove(user);
		}

		public IPentahoRole getRole(String name)
				throws UncategorizedUserRoleDaoException {
			for (IPentahoRole role : roles) {
				if (role.getName().equals(name)) {
					return role;
				}
			}
			return null;
		}

		public List<IPentahoRole> getRoles()
				throws UncategorizedUserRoleDaoException {
			return roles;
		}

		public IPentahoUser getUser(String name)
				throws UncategorizedUserRoleDaoException {
			for (IPentahoUser user : users) {
				if (user.getUsername().equals(name)) {
					return user;
				}
			}
			return null;
		}

		public List<IPentahoUser> getUsers()
				throws UncategorizedUserRoleDaoException {
			return users;
		}

		public void updateRole(IPentahoRole role) throws NotFoundException,
				UncategorizedUserRoleDaoException {
			// TODO Auto-generated method stub

		}

		public void updateUser(IPentahoUser user) throws NotFoundException,
				UncategorizedUserRoleDaoException {
			// TODO Auto-generated method stub

		}

	}

	private class MockPentahoObjectFactory implements IPentahoObjectFactory {
		@Override
		public <T> T get(final Class<T> interfaceClass,
				final IPentahoSession session) throws ObjectFactoryException {
			return get(interfaceClass, "", session);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(final Class<T> interfaceClass, final String key,
				final IPentahoSession session) throws ObjectFactoryException {
			System.out.println(interfaceClass);
			if (interfaceClass.equals(IRoleAuthorizationPolicyRoleBindingDao.class)) {
				return (T) new MockRoleAuthorizationPolicyRoleBindingDao();
			} else {
				if (interfaceClass.equals(IUserRoleListService.class)) {
					return (T) new MockUserRoleListService();
				} else {
					if (interfaceClass.equals(IUserRoleDao.class)) {
						return (T) new MockUserRoleDao();
					}
				}
			}
			return null;
		}

		@Override
		public boolean objectDefined(final String key) {
			return StringUtils.equals(key, "IRoleAuthorizationPolicyRoleBindingDao")
					|| StringUtils.equals(key, "IUserRoleListService")
					|| StringUtils.equals(key, "IUserRoleDao")
					|| StringUtils.equals(key, "txnUserRoleDao");
		}

		@Override
		public Class<?> getImplementingClass(final String key) {
			// This method was never called
			// return StringUtils.equals(key,
			// "IRoleAuthorizationPolicyRoleBindingDao") ?
			// MockRoleAuthorizationPolicyRoleBindingDao.class
			// : null;
			return null;
		}

		@Override
		public void init(final String configFile, final Object context) {
		}
	}

	private UserRoleResource userRoleResouceFactory() {
		PentahoSystem.setObjectFactory(new MockPentahoObjectFactory());
		return new UserRoleResource();
	}

	@Test
	public void testGetUsers() throws Exception {
		UserRoleResource userRoleResource = userRoleResouceFactory();
			UserListWrapper ulw = userRoleResource.getUsers();
			assertNotNull("Cannot be null", ulw);
			assertSame("User Count", MockUserRoleListService.getUserCount(), ulw.getUsers().size());
	}

	@Test
	public void testGetRoles() throws Exception {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		RoleListWrapper irw = userRoleResource.getRoles();
		assertNotNull("method output should not be null", irw);
		assertEquals("Role Count ", MockUserRoleListService.getRoleCount(), irw
				.getRoles().size());
	}

	@Test
	public void testGetRoleBindingStruct() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		SystemRolesMap srm = userRoleResource.getRoleBindingStruct("DummyLocation");
		assertNotNull("method output should not be null", srm);
		assertEquals("Localized Role Count",
				MockUserRoleListService.getRoleCount(), srm.getLocalizedRoleNames()
						.size());
		// A logical Role is setup for each UserRole in the mock data
		assertEquals("Logical Role Count", MockUserRoleListService.getRoleCount(),
				srm.getLogicalRoleAssignments().size());
	}

	@Test
	public void testSetLogicalRoles() {
		UserRoleResource userRoleResource = userRoleResouceFactory();

		// Build a roleAssignments object to add
		ArrayList<String> roleList = new ArrayList<String>();
		roleList.add("abc");
		roleList.add("def");

		LogicalRoleAssignment lra = new LogicalRoleAssignment();
		lra.setRoleName("newRoleName");
		lra.setLogicalRoles(roleList);

		ArrayList<LogicalRoleAssignment> logicalRoleAssignments = new ArrayList<LogicalRoleAssignment>();
		logicalRoleAssignments.add(lra);

		LogicalRoleAssignments roleAssignments = new LogicalRoleAssignments();
		roleAssignments.setLogicalRoleAssignments(logicalRoleAssignments);
		assertOkResponse(userRoleResource.setLogicalRoles(roleAssignments));
	}

	@Test
	public void testGetRolesForUser() throws Exception {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.getRolesForUser("elle"));
		// TODO: Possible parse of response object
	}

	@Test
	public void testGetUsersInRole() throws Exception {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.getUsersInRole("dev"));

	}

	@Test
	public void testAssignRoleToUser() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.assignRoleToUser("elle", "b"));
	}

	@Test
	public void testRemoveRoleFromUser() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.assignRoleToUser("elle", "dev"));
	}

	@Test
	public void testAssignAllRolesToUser() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.assignAllRolesToUser("elle"));
	}

	@Test
	public void testRemoveAllRolesFromUser() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.removeAllRolesFromUser("elle"));
	}

	@Test
	public void testAssignUserToRole() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.assignUserToRole("elle", "h"));
	}

	@Test
	public void testRemoveUserFromRole() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.removeUserFromRole("elle", "ceo"));
	}

	@Test
	public void testAssignAllUsersToRole() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.assignAllUsersToRole("b"));
	}

	@Test
	public void testRemoveAllUsersFromRole() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.removeAllUsersFromRole("b"));
	}

	@Test
	public void testCreateUser() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.createUser("dummy", "dumpas"));
	}

	@Test
	public void testCreateRole() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.createRole("newRole"));
	}

	@Test
	public void testDeleteRole() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.deleteRole("c"));
	}

	@Test
	public void testDeleteUser() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.deleteUser("Helen"));
	}

	@Test
	public void testUpdatePassword() {
		UserRoleResource userRoleResource = userRoleResouceFactory();
		assertOkResponse(userRoleResource.updatePassword("G", "dumpass"));
	}

	private static void assertOkResponse(Response resp) {
		assertNotNull(resp);
		assertEquals("Response Output", resp.getStatus(), 200);
	}

}
