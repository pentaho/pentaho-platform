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
package org.pentaho.platform.engine.security.userroledao.jackrabbit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.engine.security.userroledao.DefaultTenantedPrincipleNameUtils;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;

/**
 * Unit test for {@link HibernateUserRoleDao}.
 * 
 * @author mlowery
 */
public class JackrabbitUserRoleDaoTest {

  public static final String PASSWORD_1 = "password1"; //$NON-NLS-1$
  public static final String PASSWORD_2 = "password2"; //$NON-NLS-1$
  public static final String PASSWORD_3 = "password3"; //$NON-NLS-1$
  public static final String PASSWORD_4 = "password4"; //$NON-NLS-1$
  public static final String PASSWORD_5 = "password5"; //$NON-NLS-1$
  public static final String PASSWORD_6 = "password6"; //$NON-NLS-1$
  public static final String PASSWORD_7 = "password7"; //$NON-NLS-1$
  public static final String PASSWORD_8 = "password8"; //$NON-NLS-1$

  public static final String USER_1 = "joe"; //$NON-NLS-1$
  public static final String USER_2 = "jim"; //$NON-NLS-1$
  public static final String USER_3 = "sally"; //$NON-NLS-1$
  public static final String USER_4 = "suzy"; //$NON-NLS-1$
  public static final String USER_5 = "nancy"; //$NON-NLS-1$
  public static final String USER_6 = "john"; //$NON-NLS-1$
  public static final String USER_7 = "jane"; //$NON-NLS-1$
  public static final String USER_8 = "jerry"; //$NON-NLS-1$
  public static final String UNKNOWN_USER = "unknownUser"; //$NON-NLS-1$
  

  public static final String TENANT_1 = "Acme";  //$NON-NLS-1$
  public static final String TENANT_2 = "Ace";  //$NON-NLS-1$
  public static final String TENANT_3 = "Superior"; //$NON-NLS-1$
  public static final String TENANT_4 = "All-Pro"; //$NON-NLS-1$
  public static final String TENANT_5 = "National"; //$NON-NLS-1$
  public static final String TENANT_6 = "Progressive"; //$NON-NLS-1$
  public static final String TENANT_7 = "Aliance"; //$NON-NLS-1$
  public static final String TENANT_8 = "A-1"; //$NON-NLS-1$
  public static final String UNKNOWN_TENANT = "unknownTenant"; //$NON-NLS-1$
  
  public static final String ROLE_1 = "SalesMgr"; //$NON-NLS-1$
  public static final String ROLE_2 = "IT"; //$NON-NLS-1$
  public static final String ROLE_3 = "Sales"; //$NON-NLS-1$
  public static final String ROLE_4 = "Developer"; //$NON-NLS-1$
  public static final String ROLE_5 = "CEO"; //$NON-NLS-1$
  public static final String ROLE_6 = "Finance"; //$NON-NLS-1$
  public static final String ROLE_7 = "Marketing"; //$NON-NLS-1$
  public static final String ROLE_8 = "RegionalMgr"; //$NON-NLS-1$
  public static final String UNKNOWN_ROLE = "unknownRole"; //$NON-NLS-1$
  
  public static final String USER_DESCRIPTION_1 = "User Description 1"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_2 = "User Description 2"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_3 = "User Description 3"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_4 = "User Description 4"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_5 = "User Description 5"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_6 = "User Description 6"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_7 = "User Description 7"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_8 = "User Description 8"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_1 = "Role Description 1"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_2 = "Role Description 2"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_3 = "Role Description 3"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_4 = "Role Description 4"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_5 = "Role Description 5"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_6 = "Role Description 6"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_7 = "Role Description 7"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_8 = "Role Description 8"; //$NON-NLS-1$

  NameFactory NF = NameFactoryImpl.getInstance();
  Name P_PRINCIPAL_NAME = NF.create(Name.NS_REP_URI, "principalName"); //$NON-NLS-1$
  
  Repository repository;
  File repositoryDir;
  String pPrincipalName;
  JackrabbitUserRoleDao userRoleDao;

  @Before
  public void setUp() throws Exception {
    repositoryDir = new File(new File(System.getProperty("java.io.tmpdir")), "jackrabbitRepo-" + System.currentTimeMillis()); //$NON-NLS-1$  //$NON-NLS-2$
    repositoryDir.mkdir();
    repository = new TransientRepository(repositoryDir); 
    SessionFactory sessionFactory = new SessionFactory() {      
      public SessionHolder getSessionHolder(Session arg0) {
        return new SessionHolder(arg0);
      }
      
      public Session getSession() throws RepositoryException {
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        return session;
      }
    };
    
    JcrTemplate jcrTemplate =  new JcrTemplate(sessionFactory);
    jcrTemplate.setAllowCreate(true);
    jcrTemplate.setExposeNativeSession(true);
    
    JackrabbitUserRoleDao jackrabbitUserRoleDao = new JackrabbitUserRoleDao(jcrTemplate);
    jackrabbitUserRoleDao.setTenantedUserNameUtils(new DefaultTenantedPrincipleNameUtils());
    jackrabbitUserRoleDao.setTenantedRoleNameUtils(new DefaultTenantedPrincipleNameUtils());
    
    userRoleDao = jackrabbitUserRoleDao;
  }

  @After
  public void tearDown() throws Exception {
    repositoryDir.delete();
  }

  @Test
  public void testCreateUser() throws Exception {
    List<IPentahoUser> users = userRoleDao.getUsers(TENANT_1);
    IPentahoUser pentahoUser = userRoleDao.createUser(TENANT_1, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
    
    pentahoUser = userRoleDao.getUser(TENANT_1, USER_1);
    assertEquals(pentahoUser.getTenant(), TENANT_1);
    assertEquals(pentahoUser.getUsername(), USER_1);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_1);
    assertEquals(pentahoUser.isEnabled(), true);
    
    SessionImpl session = (SessionImpl)repository.login(new SimpleCredentials("admin", "admin".toCharArray())); //$NON-NLS-1$ //$NON-NLS-2$
    traverseNodes(session.getRootNode(), 0);
    
    users = userRoleDao.getUsers(TENANT_1);
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), TENANT_1);
    assertEquals(pentahoUser.getUsername(), USER_1);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_1);
    assertEquals(pentahoUser.isEnabled(), true);
    
    pentahoUser = userRoleDao.createUser(TENANT_2, USER_1, PASSWORD_2, USER_DESCRIPTION_2, null);
    
    pentahoUser = userRoleDao.getUser(TENANT_2, USER_1);
    assertEquals(pentahoUser.getTenant(), TENANT_2);
    assertEquals(pentahoUser.getUsername(), USER_1);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);
    
    users = userRoleDao.getUsers(TENANT_2);
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), TENANT_2);
    assertEquals(pentahoUser.getUsername(), USER_1);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);
    
    pentahoUser = userRoleDao.createUser(USER_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_3, PASSWORD_3, USER_DESCRIPTION_3, null);
    
    pentahoUser = userRoleDao.getUser(USER_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_3);
    assertEquals(pentahoUser.getTenant(), TENANT_3);
    assertEquals(pentahoUser.getUsername(), USER_3);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_3);
    assertEquals(pentahoUser.isEnabled(), true);
    
    users = userRoleDao.getUsers(TENANT_3);
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), TENANT_3);
    assertEquals(pentahoUser.getUsername(), USER_3);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_3);
    assertEquals(pentahoUser.isEnabled(), true);
    
    pentahoUser = userRoleDao.createUser(USER_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_4, PASSWORD_4, USER_DESCRIPTION_4, null);
    
    pentahoUser = userRoleDao.getUser(USER_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_4);
    assertEquals(pentahoUser.getTenant(), TENANT_4);
    assertEquals(pentahoUser.getUsername(), USER_4);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_4);
    assertEquals(pentahoUser.isEnabled(), true);
    
    users = userRoleDao.getUsers(TENANT_4);
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), TENANT_4);
    assertEquals(pentahoUser.getUsername(), USER_4);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_4);
    assertEquals(pentahoUser.isEnabled(), true);
    
    try {
      pentahoUser = userRoleDao.createUser(TENANT_1, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = userRoleDao.createUser(USER_1 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
  }

  @Test
  public void testCreateRole() throws Exception {
    IPentahoRole pentahoRole = userRoleDao.createRole(TENANT_1, ROLE_1, ROLE_DESCRIPTION_1, null);
    
    pentahoRole = userRoleDao.getRole(TENANT_1, ROLE_1);
    assertEquals(pentahoRole.getTenant(), TENANT_1);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_1);
    
    List<IPentahoRole> roles = userRoleDao.getRoles(TENANT_1);
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), TENANT_1);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_1);
    
    pentahoRole = userRoleDao.createRole(TENANT_2, ROLE_1, ROLE_DESCRIPTION_2, null);
    
    pentahoRole = userRoleDao.getRole(TENANT_2, ROLE_1);
    assertEquals(pentahoRole.getTenant(), TENANT_2);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_2);
    
    roles = userRoleDao.getRoles(TENANT_2);
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), TENANT_2);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_2);
    
    userRoleDao.createRole(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_3, ROLE_DESCRIPTION_3, null);
    
    pentahoRole = userRoleDao.getRole(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_3);
    assertEquals(pentahoRole.getTenant(), TENANT_3);
    assertEquals(pentahoRole.getName(), ROLE_3);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_3);
    
    roles = userRoleDao.getRoles(TENANT_3);
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), TENANT_3);
    assertEquals(pentahoRole.getName(), ROLE_3);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_3);
    
    pentahoRole = userRoleDao.createRole(ROLE_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_4, ROLE_DESCRIPTION_4, null);
    
    pentahoRole = userRoleDao.getRole(ROLE_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_4);
    assertEquals(pentahoRole.getTenant(), TENANT_4);
    assertEquals(pentahoRole.getName(), ROLE_4);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_4);
    
    roles = userRoleDao.getRoles(TENANT_4);
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), TENANT_4);
    assertEquals(pentahoRole.getName(), ROLE_4);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_4);

    try {
      pentahoRole = userRoleDao.createRole(TENANT_1, ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = userRoleDao.createRole(ROLE_1 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
  }

  @Test
  public void testUpdateUser() throws Exception {
    IPentahoUser pentahoUser = userRoleDao.createUser(TENANT_5, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null);   
    pentahoUser = userRoleDao.getUser(TENANT_5, USER_5);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_5);
    String originalPassword = pentahoUser.getPassword();
    String encryptedPassword = originalPassword;
    
    String changedDescription1 = USER_DESCRIPTION_5 + "change1";
    userRoleDao.setUserDescription(TENANT_5, USER_5, changedDescription1);
    pentahoUser = userRoleDao.getUser(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5);
    assertEquals(changedDescription1, pentahoUser.getDescription());
    
    String changedDescription2 = USER_DESCRIPTION_5 + "change2";
    userRoleDao.setUserDescription(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5, changedDescription2);
    pentahoUser = userRoleDao.getUser(TENANT_5, USER_5);
    assertEquals(changedDescription2, pentahoUser.getDescription());
    
    userRoleDao.setUserDescription(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5, null);
    pentahoUser = userRoleDao.getUser(TENANT_5, USER_5);
    assertNull(pentahoUser.getDescription());
    
    try {
      userRoleDao.setUserDescription(null, changedDescription2);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      userRoleDao.setUserDescription(USER_5, changedDescription2);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
        
    try {
      userRoleDao.setUserDescription(TENANT_5, UNKNOWN_USER, changedDescription2);
      fail("Exception not thrown");
    } catch (NotFoundException ex) {
      // Expected exception
    }
    
//    String changedPwd1 = PASSWORD_5 + "change1";
//    userRoleDao.setPassword(TENANT_5, USER_5, changedPwd1);
//    pentahoUser = userRoleDao.getUser(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5);
//    assertTrue(!encryptedPassword.equals(pentahoUser.getPassword()));
//    encryptedPassword = pentahoUser.getPassword();
//    
//    String changedPwd2 = PASSWORD_5 + "change2";
//    userRoleDao.setPassword(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5, changedPwd2);
//    pentahoUser = userRoleDao.getUser(TENANT_5, USER_5);
//    assertTrue(!encryptedPassword.equals(pentahoUser.getPassword()));
//
//    userRoleDao.setPassword(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5, PASSWORD_5);
//    pentahoUser = userRoleDao.getUser(TENANT_5, USER_5);
//    assertTrue(originalPassword.equals(pentahoUser.getPassword()));
  }

  @Test
  public void testUpdateRole() throws Exception {
    IPentahoRole pentahoRole = userRoleDao.createRole(TENANT_5, ROLE_5, ROLE_DESCRIPTION_5, null);   
    pentahoRole = userRoleDao.getRole(TENANT_5, ROLE_5);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_5);
    
    String changedDescription1 = ROLE_DESCRIPTION_5 + "change1";
    userRoleDao.setRoleDescription(TENANT_5, ROLE_5, changedDescription1);
    pentahoRole = userRoleDao.getRole(ROLE_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5);
    assertEquals(changedDescription1, pentahoRole.getDescription());
    
    String changedDescription2 = ROLE_DESCRIPTION_5 + "change2";
    userRoleDao.setRoleDescription(ROLE_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5, changedDescription2);
    pentahoRole = userRoleDao.getRole(TENANT_5, ROLE_5);
    assertEquals(changedDescription2, pentahoRole.getDescription());
    
    userRoleDao.setRoleDescription(ROLE_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5, null);
    pentahoRole = userRoleDao.getRole(TENANT_5, ROLE_5);
    assertNull(pentahoRole.getDescription());
    
    try {
      userRoleDao.setRoleDescription(null, changedDescription2);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      userRoleDao.setRoleDescription(ROLE_5, changedDescription2);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    
    try {
      userRoleDao.setRoleDescription(TENANT_5, UNKNOWN_ROLE, changedDescription2);
      fail("Exception not thrown");
    } catch (NotFoundException ex) {
      // Expected exception
    }
  }

  @Test
  public void testDeleteUser() throws Exception {
    IPentahoUser pentahoUser = userRoleDao.createUser(TENANT_6, USER_6, PASSWORD_6, USER_DESCRIPTION_6, null);       
    pentahoUser = userRoleDao.getUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_6);   
    assertNotNull(pentahoUser);
    
    userRoleDao.deleteUser(pentahoUser);
    
    pentahoUser = userRoleDao.getUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_6);   
    assertNull(pentahoUser);
    assertEquals(0, userRoleDao.getUsers(TENANT_6).size());
   
    pentahoUser = userRoleDao.createUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_6, PASSWORD_6, USER_DESCRIPTION_6, null);   
    pentahoUser = userRoleDao.getUser(TENANT_6, USER_6);
    
    assertNotNull(pentahoUser);
    
    userRoleDao.deleteUser(pentahoUser);
    
    assertNull(userRoleDao.getUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_6));
    
    try {
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(null, USER_6, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(TENANT_6, null, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(TENANT_6, UNKNOWN_USER, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }

  }

  @Test
  public void testDeleteRole() throws Exception {
    IPentahoRole pentahoRole = userRoleDao.createRole(TENANT_6, ROLE_6, ROLE_DESCRIPTION_6, null);       
    pentahoRole = userRoleDao.getRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_6);   
    assertNotNull(pentahoRole);
    
    userRoleDao.deleteRole(pentahoRole);
    
    pentahoRole = userRoleDao.getRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_6);   
    assertNull(pentahoRole);
    assertEquals(0, userRoleDao.getRoles(TENANT_6).size());
   
    pentahoRole = userRoleDao.createRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_6, ROLE_DESCRIPTION_6, null);   
    pentahoRole = userRoleDao.getRole(TENANT_6, ROLE_6);
    
    assertNotNull(pentahoRole);
    
    userRoleDao.deleteRole(pentahoRole);
    
    assertNull(userRoleDao.getRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_6));
    
    try {
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(null, ROLE_6, ROLE_DESCRIPTION_6);
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(TENANT_6, null, ROLE_DESCRIPTION_6);
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(TENANT_6, UNKNOWN_ROLE, ROLE_DESCRIPTION_6);
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
  }
  
  @Test
  public void testGetUser() throws Exception {
    assertNull(userRoleDao.getUser(UNKNOWN_TENANT, UNKNOWN_USER));   
    try {
      userRoleDao.getUser(UNKNOWN_USER);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Exception expected
    }
  }

  @Test
  public void testGetUsers() throws Exception {
    userRoleDao.createUser(TENANT_7, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null);       
    userRoleDao.createUser(TENANT_7, USER_8, PASSWORD_8, USER_DESCRIPTION_8, null);
    List<IPentahoUser> users = userRoleDao.getUsers(TENANT_7);
    assertEquals(2, users.size());
    
    for (IPentahoUser user : users) {
      if (user.getUsername().equals(USER_7)) {
        assertEquals(user.getTenant(), TENANT_7);
        assertEquals(user.getDescription(), USER_DESCRIPTION_7);
        assertEquals(user.isEnabled(), true);
      } else if (user.getUsername().equals(USER_8)) {
        assertEquals(user.getTenant(), TENANT_7);
        assertEquals(user.getDescription(), USER_DESCRIPTION_8);
        assertEquals(user.isEnabled(), true);
      } else {
        fail("Invalid user name");
      }
    }
    
    users = userRoleDao.getUsers(UNKNOWN_TENANT);
    assertEquals(0, users.size());
  }

  @Test
  public void testGetRoles() throws Exception {
    userRoleDao.createRole(TENANT_7, ROLE_7, ROLE_DESCRIPTION_7, null);       
    userRoleDao.createRole(TENANT_7, ROLE_8, ROLE_DESCRIPTION_8, null);
    List<IPentahoRole> roles = userRoleDao.getRoles(TENANT_7);
    assertEquals(2, roles.size());
    
    for (IPentahoRole user : roles) {
      if (user.getName().equals(ROLE_7)) {
        assertEquals(user.getTenant(), TENANT_7);
        assertEquals(user.getDescription(), ROLE_DESCRIPTION_7);
      } else if (user.getName().equals(ROLE_8)) {
        assertEquals(user.getTenant(), TENANT_7);
        assertEquals(user.getDescription(), ROLE_DESCRIPTION_8);
      } else {
        fail("Invalid user name");
      }
    }
    
    roles = userRoleDao.getRoles(UNKNOWN_TENANT);
    assertEquals(0, roles.size());
  }

  @Test
  public void testRoleWithMembers() throws Exception {
    userRoleDao.createRole(TENANT_8, ROLE_1, ROLE_DESCRIPTION_1, null);       
    userRoleDao.createRole(TENANT_8, ROLE_2, ROLE_DESCRIPTION_2, null);
    userRoleDao.createRole(TENANT_8, ROLE_3, ROLE_DESCRIPTION_3, null);
    userRoleDao.createUser(TENANT_8, USER_1, PASSWORD_1, USER_DESCRIPTION_1, new String[]{ROLE_1});
    userRoleDao.createUser(TENANT_8, USER_2, PASSWORD_2, USER_DESCRIPTION_2, new String[]{ROLE_1, ROLE_2});
    
    List<IPentahoUser> users = userRoleDao.getRoleMembers(TENANT_8, ROLE_2);
    assertEquals(1, users.size());
    assertEquals(USER_2, users.get(0).getUsername());
    
    ArrayList<String> expectedUserNames = new ArrayList<String>();
    expectedUserNames.add(USER_1);
    expectedUserNames.add(USER_2);
    ArrayList<String> actualUserNames = new ArrayList<String>();
    users = userRoleDao.getRoleMembers(ROLE_1 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_8);
    for (IPentahoUser user : users) {
      actualUserNames.add(user.getUsername());
    }
    assertEquals(2, actualUserNames.size());
    assertTrue(actualUserNames.containsAll(expectedUserNames));
        
    users = userRoleDao.getRoleMembers(TENANT_8, ROLE_3);
    assertEquals(0, users.size());
    
    userRoleDao.createUser(TENANT_8, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null);       
    userRoleDao.createUser(TENANT_8, USER_6, PASSWORD_6, USER_DESCRIPTION_6, null);
    userRoleDao.createUser(TENANT_8, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null);
    userRoleDao.createRole(TENANT_8, ROLE_5, ROLE_DESCRIPTION_6, new String[]{USER_5});
    userRoleDao.createRole(TENANT_8, ROLE_6, ROLE_DESCRIPTION_7, new String[]{USER_5, USER_6});
    
    List<IPentahoRole> roles = userRoleDao.getUserRoles(TENANT_8, USER_6);
    assertEquals(1, roles.size());
    assertEquals(ROLE_6, roles.get(0).getName());
    
    ArrayList<String> expectedRoleNames = new ArrayList<String>();
    expectedRoleNames.add(ROLE_5);
    expectedRoleNames.add(ROLE_6);
    ArrayList<String> actualRoleNames = new ArrayList<String>();
    roles = userRoleDao.getUserRoles(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_8);
    for (IPentahoRole role : roles) {
      actualRoleNames.add(role.getName());
    }
    assertEquals(2, actualRoleNames.size());
    assertTrue(actualRoleNames.containsAll(expectedRoleNames));
        
    roles = userRoleDao.getUserRoles(TENANT_8, USER_7);
    assertEquals(0, roles.size());
    
    userRoleDao.setUserRoles(USER_7 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_8, new String[]{ROLE_5, ROLE_6});
    roles = userRoleDao.getUserRoles(USER_7 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_8);
    actualRoleNames.clear();
    for (IPentahoRole role : roles) {
      actualRoleNames.add(role.getName());
    }
    assertEquals(2, actualRoleNames.size());
    assertTrue(actualRoleNames.containsAll(expectedRoleNames));
    
    userRoleDao.setRoleMembers(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_8, new String[]{USER_1, USER_2});
    users = userRoleDao.getRoleMembers(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_8);
    actualUserNames.clear();
    for (IPentahoUser user : users) {
      actualUserNames.add(user.getUsername());
    }
    assertEquals(2, actualUserNames.size());
    assertTrue(actualUserNames.containsAll(expectedUserNames));
  }

  @Test
  public void testGetRole() throws Exception {
    assertNull(userRoleDao.getRole(UNKNOWN_TENANT, UNKNOWN_ROLE));   
    try {
      userRoleDao.getRole(UNKNOWN_ROLE);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Exception expected
    }
  }

  private static void traverseNodes(Node node, int currentLevel) throws Exception {
    System.out.println(node.getPath());
    NodeIterator children = node.getNodes();
    while (children.hasNext()) {
        traverseNodes(children.nextNode(), currentLevel + 1);
    }
  }  
}
