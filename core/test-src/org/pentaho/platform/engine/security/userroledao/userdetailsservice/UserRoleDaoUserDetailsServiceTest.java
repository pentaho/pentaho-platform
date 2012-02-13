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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.engine.security.userroledao.userdetailsservice;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link UserRoleDaoUserDetailsService}.
 * 
 * @author mlowery
 */
@RunWith(JMock.class)
public class UserRoleDaoUserDetailsServiceTest {

  private static final String ROLE_PREFIX = "ROLE_"; //$NON-NLS-1$

  private static final String ROLE = "Admin"; //$NON-NLS-1$

  private static final String PASSWORD = "password"; //$NON-NLS-1$

  private static final String USERNAME = "joe"; //$NON-NLS-1$

  private Mockery context = new JUnit4Mockery();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test(expected = UsernameNotFoundException.class)
  public void testLoadUserByUsernameUsernameNotFound() {
    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    context.checking(new Expectations() {
      {
        one(dao).getUser(with(equal(USERNAME)));
        will(returnValue(null));
      }
    });

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao(dao);
    userDetailsService.loadUserByUsername(USERNAME);
  }

  @Test
  public void testLoadUserByUsername() {
    final IPentahoUser user = new PentahoUser(USERNAME);
    user.setEnabled(true);
    user.setPassword(PASSWORD);
    user.addRole(new PentahoRole(ROLE));

    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    context.checking(new Expectations() {
      {
        one(dao).getUser(with(equal(USERNAME)));
        will(returnValue(user));
      }
    });

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao(dao);
    UserDetails userFromService = userDetailsService.loadUserByUsername(USERNAME);

    assertTrue(userFromService.getUsername().equals(USERNAME));
    assertTrue(userFromService.getPassword().equals(PASSWORD));
    assertTrue(userFromService.isEnabled() == true);
    assertTrue(userFromService.getAuthorities().length == 1);
    assertTrue(userFromService.getAuthorities()[0].getAuthority().equals(ROLE_PREFIX + ROLE));
  }

  @Test(expected = UsernameNotFoundException.class)
  public void testLoadUserByUsernameNoRoles() {
    final IPentahoUser user = new PentahoUser(USERNAME);
    user.setEnabled(true);
    user.setPassword(PASSWORD);

    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    context.checking(new Expectations() {
      {
        one(dao).getUser(with(equal(USERNAME)));
        will(returnValue(user));
      }
    });

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao(dao);
    userDetailsService.loadUserByUsername(USERNAME);
  }

  @Test
  public void testLoadUserByUsernameWithRolePrefix() {
    final IPentahoUser user = new PentahoUser(USERNAME);
    user.setEnabled(true);
    user.setPassword(PASSWORD);
    user.addRole(new PentahoRole(ROLE));

    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    context.checking(new Expectations() {
      {
        one(dao).getUser(with(equal(USERNAME)));
        will(returnValue(user));
      }
    });

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao(dao);
    userDetailsService.setRolePrefix(ROLE_PREFIX);
    UserDetails userFromService = userDetailsService.loadUserByUsername(USERNAME);

    assertTrue(userFromService.getAuthorities()[0].getAuthority().equals(ROLE_PREFIX + ROLE));
  }
}
