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
package org.pentaho.platform.engine.security.userroledao;

import java.util.List;

/**
 * Contract for data access objects that read and write users and roles.
 * 
 * @author mlowery
 */
public interface IUserRoleDao {

  void createUser(IPentahoUser newUser) throws AlreadyExistsException, UncategorizedUserRoleDaoException;

  void deleteUser(IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException;

  IPentahoUser getUser(String name) throws UncategorizedUserRoleDaoException;

  List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException;

  void updateUser(IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException;

  void createRole(IPentahoRole newRole) throws AlreadyExistsException, UncategorizedUserRoleDaoException;

  void deleteRole(IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException;

  IPentahoRole getRole(String name) throws UncategorizedUserRoleDaoException;

  List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException;

  void updateRole(IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException;

}
