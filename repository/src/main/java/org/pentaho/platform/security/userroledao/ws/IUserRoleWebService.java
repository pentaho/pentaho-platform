/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.security.userroledao.ws;

import jakarta.jws.WebService;
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

  public default void logout() { }
}
