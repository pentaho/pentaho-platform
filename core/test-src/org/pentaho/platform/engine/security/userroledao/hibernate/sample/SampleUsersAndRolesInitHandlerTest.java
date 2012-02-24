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
package org.pentaho.platform.engine.security.userroledao.hibernate.sample;

import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertRoleAssignmentPersisted;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertRolePersisted;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertUserPersisted;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertUserRemoved;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.generateAndExecuteDdl;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.getConnection;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.getSessionFactory;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.DdlType.CREATE;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.DdlType.DROP;

import java.sql.Connection;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.security.userroledao.hibernate.HibernateUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil;

/**
 * Unit test for {@link SampleUsersAndRolesInitHandler}.
 * 
 * @author mlowery
 */
public class SampleUsersAndRolesInitHandlerTest {

  private static final String PASSWORD = "cGFzc3dvcmQ="; //$NON-NLS-1$

  private static final String JOE = "joe"; //$NON-NLS-1$

  private static final String SUZY = "suzy"; //$NON-NLS-1$

  private static final String ADMIN = "Admin"; //$NON-NLS-1$

  private HibernateUserRoleDao userRoleDao;

  private SessionFactory sessionFactory;

  private Connection connection;

  @Before
  public void setUp() throws Exception {
    // create and initialize class under test
    userRoleDao = new HibernateUserRoleDao();
    sessionFactory = getSessionFactory();
    userRoleDao.setSessionFactory(sessionFactory);

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
  public void testHandleInit() throws Exception {
    SampleUsersAndRolesInitHandler initHandler = new SampleUsersAndRolesInitHandler();
    initHandler.setSessionFactory(sessionFactory);
    initHandler.setUserRoleDao(userRoleDao);

    initHandler.handleInit();

    assertRolePersisted(connection, ADMIN);
    assertUserPersisted(connection, JOE, PASSWORD, true);
    assertRoleAssignmentPersisted(connection, JOE, ADMIN);
  }

  @Test
  public void testHandleInitUsersAlreadyExist() throws Exception {

    TestUtil.createTestUser(connection, SUZY, PASSWORD, true, null);
    
    SampleUsersAndRolesInitHandler initHandler = new SampleUsersAndRolesInitHandler();
    initHandler.setSessionFactory(sessionFactory);
    initHandler.setUserRoleDao(userRoleDao);

    initHandler.handleInit();

    assertUserRemoved(connection, JOE);
  }

}
