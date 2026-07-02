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
