/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.security.userroledao.ws;

import javax.jws.WebService;
import java.util.List;

/**
 * This webservice interface may be used in the platform to expose the management of users and roles, it is a
 * webservices compatible concrete form of IUserRoleDao.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
@WebService
public interface IUserRoleWebService {

  /**
   * creates a new role.
   * 
   * @param proxyRole
   * @return
   * @throws UserRoleException
   */
  public boolean createRole( ProxyPentahoRole proxyRole ) throws UserRoleException;

  /**
   * creates a new user.
   * 
   * @param proxyUser
   * @return
   * @throws UserRoleException
   */
  public boolean createUser( ProxyPentahoUser proxyUser ) throws UserRoleException;

  /**
   * deletes a set of roles.
   * 
   * @param roles
   * @return
   * @throws UserRoleException
   */
  public boolean deleteRoles( ProxyPentahoRole[] roles ) throws UserRoleException;

  /**
   * deletes a set of users.
   * 
   * @param users
   * @return
   * @throws UserRoleException
   */
  public boolean deleteUsers( ProxyPentahoUser[] users ) throws UserRoleException;

  /**
   * returns the list of roles defined in the system
   * 
   * @return
   * @throws UserRoleException
   */
  public ProxyPentahoRole[] getRoles() throws UserRoleException;

  /**
   * returns a set of roles for a user
   * 
   * @param proxyUser
   * @return
   * @throws UserRoleException
   */
  public ProxyPentahoRole[] getRolesForUser( ProxyPentahoUser proxyUser ) throws UserRoleException;

  /**
   * returns the user object based on the username.
   * 
   * @param pUserName
   * @return
   * @throws UserRoleException
   */
  public ProxyPentahoUser getUser( String pUserName ) throws UserRoleException;

  /**
   * This method returns the entire set of users and roles in the system
   * 
   * @return an info object
   * 
   * @throws UserRoleException
   */
  public UserRoleSecurityInfo getUserRoleSecurityInfo() throws UserRoleException;

  /**
   * returns the list of users.
   * 
   * @return
   * @throws UserRoleException
   */
  public ProxyPentahoUser[] getUsers() throws UserRoleException;

  /**
   * returns the list of users that have a specific role.
   * 
   * @param proxyRole
   * @return
   * @throws UserRoleException
   */
  public ProxyPentahoUser[] getUsersForRole( ProxyPentahoRole proxyRole ) throws UserRoleException;

  /**
   * This updates the named user's attributes
   * 
   * @param proxyUser
   * @return
   * @throws UserRoleException
   */
  public boolean updateUser( ProxyPentahoUser proxyUser ) throws UserRoleException;

  /**
   * sets the roles for a user.
   * 
   * @param proxyUser
   * @param assignedRoles
   * @throws UserRoleException
   */
  public void setRoles( ProxyPentahoUser proxyUser, ProxyPentahoRole[] assignedRoles ) throws UserRoleException;

  /**
   * sets the users for a role.
   * 
   * @param proxyRole
   * @param assignedUsers
   * @throws UserRoleException
   */
  public void setUsers( ProxyPentahoRole proxyRole, ProxyPentahoUser[] assignedUsers ) throws UserRoleException;

  /**
   * This updates the named role's attributes. It is named differently than the IUserRoleDao because of issues with
   * webservice method overloading.
   * 
   * @param proxyPentahoRole
   * @return
   * @throws UserRoleException
   */
  public boolean updateRoleObject( ProxyPentahoRole proxyPentahoRole ) throws UserRoleException;

  /**
   * Updates a role object with a new description and set of usernames.
   * 
   * @param roleName
   * @param description
   * @param usernames
   * @throws UserRoleException
   */
  public void updateRole( String roleName, String description, List<String> usernames ) throws UserRoleException;
}
