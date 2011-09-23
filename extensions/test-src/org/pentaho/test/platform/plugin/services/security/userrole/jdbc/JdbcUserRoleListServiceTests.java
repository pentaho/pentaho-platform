/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.plugin.services.security.userrole.jdbc;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.pentaho.platform.plugin.services.security.userrole.jdbc.JdbcUserRoleListService;
import org.springframework.security.PopulatedDatabase;
import org.springframework.security.userdetails.jdbc.JdbcDaoImpl;

public class JdbcUserRoleListServiceTests {

  @Test
	public void testGetAllUsernames() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao
				.setAllUsernamesQuery("SELECT DISTINCT(USERNAME) FROM USERS ORDER BY USERNAME"); //$NON-NLS-1$
		dao.afterPropertiesSet();
		List<String> allUsers = dao.getAllUsers();
		assertTrue("User List should not be empty", allUsers.size() > 0); //$NON-NLS-1$
		for (String username : allUsers) {
			System.out.println("User: " + username); //$NON-NLS-1$
		}
	}

  @Test
	public void testGetAllAuthorities() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao
				.setAllAuthoritiesQuery("SELECT DISTINCT(AUTHORITY) AS AUTHORITY FROM AUTHORITIES ORDER BY 1"); //$NON-NLS-1$
		dao.afterPropertiesSet();
		List<String> auths = dao.getAllRoles();
		assertTrue("Authorities list should not be empty", auths.size() > 0); //$NON-NLS-1$
		for (String auth : auths) {
			System.out.println("Authority: " + auth); //$NON-NLS-1$
		}
	}

  @Test
	public void testGetAllAuthoritiesWithRolePrefix() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao
				.setAllAuthoritiesQuery("SELECT DISTINCT(AUTHORITY) AS AUTHORITY FROM AUTHORITIES ORDER BY 1"); //$NON-NLS-1$
		dao.setRolePrefix("ARBITRARY_PREFIX_"); //$NON-NLS-1$
		dao.afterPropertiesSet();
		List<String> auths = dao.getAllRoles();
		assertTrue("Authorities list should not be empty", auths.size() > 0); //$NON-NLS-1$
		for (String role : auths) {
			System.out
					.println("Authority with prefix: " + role); //$NON-NLS-1$
		}
	}

  @Test
	public void testGetAllUsernamesInRole() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao
				.setAllUsernamesInRoleQuery("SELECT DISTINCT(USERNAME) AS USERNAME FROM AUTHORITIES WHERE AUTHORITY = ? ORDER BY 1"); //$NON-NLS-1$
		dao.afterPropertiesSet();
		List<String> allUsers = dao.getUsersInRole("ROLE_TELLER"); //$NON-NLS-1$
		assertTrue("User List should not be empty", allUsers.size() > 0); //$NON-NLS-1$
		for (String username : allUsers) {
			System.out.println("ROLE_TELLER User: " + username); //$NON-NLS-1$
		}
	}

  @Test
	public void testGetRolesForUser() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao.setUserDetailsService(makePopulatedJdbcDao()); 
		dao.afterPropertiesSet();
		List<String> roles = dao.getRolesForUser("rod"); //$NON-NLS-1$
		assertTrue(roles.contains("ROLE_TELLER")); //$NON-NLS-1$
		assertTrue(roles.contains("ROLE_SUPERVISOR")); //$NON-NLS-1$

	}

	protected JdbcUserRoleListService makePopulatedJdbcUserRoleListService()
			throws Exception {
		JdbcUserRoleListService dao = new JdbcUserRoleListService(makePopulatedJdbcDao());
		dao.setDataSource(PopulatedDatabase.getDataSource());
		return dao;
	}

	private JdbcDaoImpl makePopulatedJdbcDao() throws Exception {
		JdbcDaoImpl dao = new JdbcDaoImpl();
		dao.setDataSource(PopulatedDatabase.getDataSource());
		dao.afterPropertiesSet();
		return dao;
	}

}
