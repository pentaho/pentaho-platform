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
package org.pentaho.platform.engine.security.userroledao.hibernate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertRoleAssignmentPersisted;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertRoleAssignmentRemoved;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertRolePersisted;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertRoleRemoved;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertUserPersisted;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertUserRemoved;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.createTestRole;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.createTestUser;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.generateAndExecuteDdl;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.getConnection;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.getSessionFactory;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.DdlType.CREATE;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.DdlType.DROP;

import java.sql.Connection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;
/**
 * Unit test for {@link HibernateUserRoleDao}.
 * 
 * @author mlowery
 */
public class HibernateUserRoleDaoTest {

  private static final String PASSWORD2 = "Passw0rd"; //$NON-NLS-1$

  private static final String SUZY = "suzy"; //$NON-NLS-1$

  private static final String CEO = "ceo"; //$NON-NLS-1$

  private static final String CTO = "cto"; //$NON-NLS-1$

  private static final String PASSWORD = "password"; //$NON-NLS-1$

  private static final String JOE = "joe"; //$NON-NLS-1$

  private static final String ADMIN = "Admin"; //$NON-NLS-1$

  private HibernateUserRoleDao dao;

  private SessionFactory sessionFactory;

  private Connection connection;

  @Before
  public void setUp() throws Exception {
    // create and initialize class under test
    dao = new HibernateUserRoleDao();
    sessionFactory = getSessionFactory();
    dao.setSessionFactory(sessionFactory);

    // setup tables
    generateAndExecuteDdl(CREATE);

    // create connection for use in verification (don't want to use the class we're testing for verification purposes)
    connection = getConnection();
  }

  @After
  public void tearDown() throws Exception {
    // remove tables at the end of test
    generateAndExecuteDdl(DROP);
    connection.close();
    sessionFactory.close();
  }

  @Test
  public void testCreateUser() throws Exception {
    createTestRole(connection, ADMIN, null);

    PentahoUser joeTransient = new PentahoUser(JOE);
    joeTransient.setPassword(PASSWORD);
    joeTransient.setEnabled(true);
    joeTransient.addRole(new PentahoRole(ADMIN));
    dao.createUser(joeTransient);

    assertUserPersisted(connection, JOE, PASSWORD, true);
    assertRoleAssignmentPersisted(connection, JOE, ADMIN);
  }

  @Test
  public void testDeleteUser() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);
    // make sure user and role and role assignment exist (so that we can verify they have been deleted)
    assertUserPersisted(connection, JOE, PASSWORD, true);

    PentahoUser joeTransient = new PentahoUser(JOE);
    dao.deleteUser(joeTransient);

    assertUserRemoved(connection, JOE);
    assertRoleAssignmentRemoved(connection, JOE, ADMIN);
    // make sure role is still there (should not get deleted when user is deleted)
    assertRolePersisted(connection, ADMIN);
  }

  @Test
  public void testGetUser() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);

    IPentahoUser user = dao.getUser(JOE);

    assertNotNull(user);
    assertFalse(user.getRoles().isEmpty());
  }

  @Test
  public void testGetUsers() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestRole(connection, CTO, null);
    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);
    createTestUser(connection, SUZY, PASSWORD, true, null, CTO);

    List<IPentahoUser> users = dao.getUsers();

    assertTrue(users != null && users.size() == 2);
    
    for (IPentahoUser user : users) {
      assertFalse(user.getRoles().isEmpty());
    }

  }

  @Test
  public void testUpdateUser() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestRole(connection, CEO, null);

    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);

    // get user, mod it, and update it
    IPentahoUser joe = dao.getUser(JOE);
    joe.setPassword(PASSWORD2);
    joe.addRole(new PentahoRole(CEO));
    dao.updateUser(joe);

    assertUserPersisted(connection, JOE, PASSWORD2, true);
    assertRoleAssignmentPersisted(connection, JOE, CEO);
  }

  @Test
  public void testUpdateUserWithTransient() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestRole(connection, CEO, null);

    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);

    // create transient user (with same username) and update it
    IPentahoUser joe = new PentahoUser(JOE);
    joe.setPassword(PASSWORD2);
    joe.addRole(new PentahoRole(CEO));
    dao.updateUser(joe);

    assertUserPersisted(connection, JOE, PASSWORD2, true);
    assertRoleAssignmentPersisted(connection, JOE, CEO);
  }

  @Test
  public void testCreateRole() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);

    PentahoRole adminRoleTransient = new PentahoRole(CEO);
    PentahoUser joeTransient = new PentahoUser(JOE);
    adminRoleTransient.addUser(joeTransient);
    dao.createRole(adminRoleTransient);

    assertRolePersisted(connection, CEO);
    assertRoleAssignmentPersisted(connection, JOE, CEO);
  }

  @Test
  public void testDeleteRole() throws Exception {
    createTestRole(connection, ADMIN, null);
    assertRolePersisted(connection, ADMIN);

    dao.deleteRole(new PentahoRole(ADMIN));

    assertRoleRemoved(connection, ADMIN);
  }

  @Test
  public void testDeleteRoleWithMembers() throws Exception {
    createTestRole(connection, ADMIN, null);
    assertRolePersisted(connection, ADMIN);
    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);

    dao.deleteRole(new PentahoRole(ADMIN));

    assertRoleAssignmentRemoved(connection, JOE, ADMIN);
    assertRoleRemoved(connection, ADMIN);
  }

  @Test
  public void testGetRole() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);

    IPentahoRole role = dao.getRole(ADMIN);

    assertNotNull(role);
    assertFalse(role.getUsers().isEmpty());
  }

  @Test
  public void testGetRoles() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestRole(connection, CTO, null);
    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);
    createTestUser(connection, SUZY, PASSWORD, true, null, CTO);

    List<IPentahoRole> roles = dao.getRoles();

    assertTrue(roles != null && roles.size() == 2);
    
    for (IPentahoRole role : roles) {
      assertFalse(role.getUsers().isEmpty());
    }
  }

  @Test
  public void testUpdateRole() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestRole(connection, CTO, null);
    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);
    createTestUser(connection, SUZY, PASSWORD, true, null, CTO);

    assertRoleAssignmentPersisted(connection, JOE, ADMIN);
    
    // get role, add suzy as a member of role, remove joe as a member of role, and update role
    IPentahoRole adminRole = dao.getRole(ADMIN);
    PentahoUser suzyTransient = new PentahoUser(SUZY);
    PentahoUser joeTransient = new PentahoUser(JOE);
    adminRole.addUser(suzyTransient);
    adminRole.removeUser(joeTransient);
    dao.updateRole(adminRole);

    assertRoleAssignmentPersisted(connection, SUZY, ADMIN);
    assertRoleAssignmentRemoved(connection, JOE, ADMIN);
  }

  @Test
  public void testUpdateRoleWithTransient() throws Exception {
    createTestRole(connection, ADMIN, null);
    createTestRole(connection, CTO, null);
    createTestUser(connection, JOE, PASSWORD, true, null, ADMIN);
    createTestUser(connection, SUZY, PASSWORD, true, null, CTO);

    // create role (with same name) and update role
    IPentahoRole adminRole = new PentahoRole(ADMIN);
    PentahoUser suzyTransient = new PentahoUser(SUZY);
    adminRole.addUser(suzyTransient);
    dao.updateRole(adminRole);

    assertRoleAssignmentPersisted(connection, SUZY, ADMIN);
  }

}
