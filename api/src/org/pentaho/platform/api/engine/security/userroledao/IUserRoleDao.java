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

package org.pentaho.platform.api.engine.security.userroledao;

import org.pentaho.platform.api.mt.ITenant;

import java.util.List;

/**
 * Contract for data access objects that read and write users and roles.
 * 
 * @author mlowery
 */
public interface IUserRoleDao {

  /**
   * Creates user under a specified tenant. If the tenant is null then it will create the user under a default
   * tenant.
   * 
   * @param tenant
   * @param username
   * @param password
   * @param description
   * @param roles
   * @return pentaho user
   * @throws AlreadyExistsException
   * @throws UncategorizedUserRoleDaoException
   */
  IPentahoUser createUser( ITenant tenant, String username, String password, String description, String[] roles )
    throws AlreadyExistsException, UncategorizedUserRoleDaoException;

  /**
   * Update the password of an existing user under a specified tenant. If the tenant is null then it will try to
   * update the password of an existing user under a default tenant
   * 
   * @param tenant
   * @param userName
   * @param password
   * @throws NotFoundException
   * @throws UncategorizedUserRoleDaoException
   */
  void setPassword( ITenant tenant, String userName, String password ) throws NotFoundException,
    UncategorizedUserRoleDaoException;

  /**
   * Update the user description of an existing user under a speficied tenant. If the tenant is null then it will
   * try to update the user description of an existing user under a default tenant
   * 
   * @param tenant
   * @param userName
   * @param description
   * @throws NotFoundException
   * @throws UncategorizedUserRoleDaoException
   */
  void setUserDescription( ITenant tenant, String userName, String description ) throws NotFoundException,
    UncategorizedUserRoleDaoException;

  /**
   * Delete the user from the repository
   * 
   * @param user
   * @throws NotFoundException
   * @throws UncategorizedUserRoleDaoException
   */
  void deleteUser( IPentahoUser user ) throws NotFoundException, UncategorizedUserRoleDaoException;

  /**
   * Retrieve the user from a specified tenant of the repository. If the tenant is null then it will try to
   * retrieve the user from a default tenant of the repository.
   * 
   * @param tenant
   * @param name
   * @return pentaho user
   * @throws UncategorizedUserRoleDaoException
   */
  IPentahoUser getUser( ITenant tenant, String name ) throws UncategorizedUserRoleDaoException;

  /**
   * Retrieve all the users from the default tenant of a repository.
   * 
   * @return list of pentaho user
   * @throws UncategorizedUserRoleDaoException
   */
  List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException;

  /**
   * Retrieve all the users from the specified tenant of a repository. If the tenant is null then it will try to
   * retrieve all the users from the default tenant.
   * 
   * @param tenant
   * @return pentaho user list
   * @throws UncategorizedUserRoleDaoException
   */
  List<IPentahoUser> getUsers( ITenant tenant ) throws UncategorizedUserRoleDaoException;

  /**
   * Retrieve all the users from the specified tenant of a repository . If the tenant is null then it will try to
   * retrieve all the users from the default tenant. If includeSubtenants is true, then it will retrieve all the
   * users from the current specified tenant and its subtenants.
   * 
   * @param tenant
   * @param includeSubtenants
   * @return pentaho user list
   * @throws UncategorizedUserRoleDaoException
   */
  List<IPentahoUser> getUsers( ITenant tenant, boolean includeSubtenants ) throws UncategorizedUserRoleDaoException;

  /**
   * Create a role under a specified tenant. If the tenant is null then this role will be created under a default
   * tenant
   * 
   * @param tenant
   * @param roleName
   * @param description
   * @param memberUserNames
   * @return pentaho role
   * @throws AlreadyExistsException
   * @throws UncategorizedUserRoleDaoException
   */
  IPentahoRole createRole( ITenant tenant, String roleName, String description, String[] memberUserNames )
    throws AlreadyExistsException, UncategorizedUserRoleDaoException;

  /**
   * Update the role description of a given role under a specific tenant. If the tenant is null, then it will
   * search for the role in the default tenant and update the description there
   * 
   * @param tenant
   * @param roleName
   * @param description
   * @throws NotFoundException
   * @throws UncategorizedUserRoleDaoException
   */
  void setRoleDescription( ITenant tenant, String roleName, String description ) throws NotFoundException,
    UncategorizedUserRoleDaoException;

  /**
   * Delete the role from the repository
   * 
   * @param role
   * @throws NotFoundException
   * @throws UncategorizedUserRoleDaoException
   */
  void deleteRole( IPentahoRole role ) throws NotFoundException, UncategorizedUserRoleDaoException;

  /**
   * Retrieves the role with a given name from the specified tenant. If the tenant is null, then it will retrieve
   * the role from the default tenant
   * 
   * @param tenant
   * @param name
   * @return pentaho role
   * @throws UncategorizedUserRoleDaoException
   */
  IPentahoRole getRole( ITenant tenant, String name ) throws UncategorizedUserRoleDaoException;

  /**
   * Retrieve all the role from the default tenant
   * 
   * @return pentaho role list
   * @throws UncategorizedUserRoleDaoException
   */
  List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException;

  /**
   * Retrieve all the role from the specified tenant. If the tenant is null, then it will retrieve all the roles
   * from the default tenant
   * 
   * @param tenant
   * @return pentaho role list
   * @throws UncategorizedUserRoleDaoException
   */
  List<IPentahoRole> getRoles( ITenant tenant ) throws UncategorizedUserRoleDaoException;

  /**
   * Retrieve all the roles from the specified tenant. If the includeSubtenants is "true" then it will get all the
   * role from the subtenants as well
   * 
   * @param tenant
   * @param includeSubtenants
   * @return pentaho role list
   * @throws UncategorizedUserRoleDaoException
   */
  List<IPentahoRole> getRoles( ITenant tenant, boolean includeSubtenants ) throws UncategorizedUserRoleDaoException;

  /**
   * Assign list of user names to a particular role in a specified tenant. If the tenant is null, then it will
   * search for this role in a default tenant
   * 
   * @param tenant
   * @param roleName
   * @param memberUserNames
   * @throws NotFoundException
   * @throws UncategorizedUserRoleDaoException
   */
  void setRoleMembers( ITenant tenant, String roleName, String[] memberUserNames ) throws NotFoundException,
    UncategorizedUserRoleDaoException;

  /**
   * Assign a list of roles to a particular user in a specified tenant. If the tenant is null, then it will search
   * for this user in a default tenant
   * 
   * @param tenant
   * @param userName
   * @param roles
   * @throws NotFoundException
   * @throws UncategorizedUserRoleDaoException
   */
  void setUserRoles( ITenant tenant, String userName, String[] roles ) throws NotFoundException,
    UncategorizedUserRoleDaoException;

  /**
   * Retrieves the list of users associated to a particular role in a given tenant. If the tenant is null, then it
   * will get role members in a default tenant
   * 
   * @param tenant
   * @param roleName
   * @return pentaho user list
   * @throws UncategorizedUserRoleDaoException
   */
  List<IPentahoUser> getRoleMembers( ITenant tenant, String roleName ) throws UncategorizedUserRoleDaoException;

  /**
   * Retrieves the list of roles associated to a particular user in a given tenant. If the tenant is null, then it
   * will get user roles in a default tenant
   * 
   * @param tenant
   * @param userName
   * @return pentaho role list
   * @throws UncategorizedUserRoleDaoException
   */
  List<IPentahoRole> getUserRoles( ITenant tenant, String userName ) throws UncategorizedUserRoleDaoException;
}
