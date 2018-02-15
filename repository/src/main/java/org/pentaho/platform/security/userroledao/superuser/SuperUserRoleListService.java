/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.security.userroledao.superuser;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.mt.ITenant;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IUserRoleListService} that delegates to an {@link IUserRoleDao}.
 * 
 * @author mlowery
 */
public class SuperUserRoleListService implements IUserRoleListService {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private List<String> systemRoles;
  private List<String> roles;
  private List<String> users;

  // ~ Constructors
  // ====================================================================================================
  public SuperUserRoleListService() {
    super();
  }

  public SuperUserRoleListService( String role, String user, List<String> systemRoles ) {
    super();
    this.systemRoles = systemRoles;
    this.users = new ArrayList<String>();
    this.users.add( user );
    this.roles = new ArrayList<String>();
    this.roles.add( role );
  }

  // ~ Methods
  // =========================================================================================================

  @Override
  public List<String> getAllRoles() {
    return roles;
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    return getAllRoles();
  }

  @Override
  public List<String> getAllUsers() {
    return users;
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    return getAllUsers();
  }

  @Override
  public List<String> getRolesForUser( ITenant tenant, String username ) throws UsernameNotFoundException,
    DataAccessException {
    return roles;
  }

  public List<String> getUsersInRole( ITenant tenant, IPentahoRole role, String roleName ) {
    return users;
  }

  @Override
  public List<String> getUsersInRole( ITenant tenant, String roleName ) {
    return users;
  }

  @Override
  public List<String> getSystemRoles() {
    return systemRoles;
  }

}
