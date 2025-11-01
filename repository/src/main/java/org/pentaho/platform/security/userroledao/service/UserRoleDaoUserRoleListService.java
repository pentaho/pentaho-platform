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


package org.pentaho.platform.security.userroledao.service;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An {@link IUserRoleListService} that delegates to an {@link IUserRoleDao}.
 * 
 * @author mlowery
 */
public class UserRoleDaoUserRoleListService implements IUserRoleListService {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private IUserRoleDao userRoleDao;
  private UserDetailsService userDetailsService;
  private List<String> systemRoles;
  private List<String> extraRoles;
  private String adminRole;
  private ITenantedPrincipleNameResolver usernamePrincipalResolver;

  // ~ Constructors
  // ====================================================================================================
  public UserRoleDaoUserRoleListService() {
    super();
  }

  public UserRoleDaoUserRoleListService( IUserRoleDao userRoleDao, UserDetailsService userDetailsService,
      ITenantedPrincipleNameResolver usernamePrincipalResolver, List<String> systemRoles, List<String> extraRoles,
      final String adminRole ) {
    super();
    this.userRoleDao = userRoleDao;
    this.userDetailsService = userDetailsService;
    this.usernamePrincipalResolver = usernamePrincipalResolver;
    this.systemRoles = systemRoles;
    this.extraRoles = extraRoles;
    this.adminRole = adminRole;
  }

  // ~ Methods
  // =========================================================================================================

  private List<String> getAllRoles( List<IPentahoRole> roles ) {
    List<String> auths = new ArrayList<String>( roles.size() );

    for ( IPentahoRole role : roles ) {
      auths.add( role.getName() );
    }
    // We will not allow user to update permission for Administrator
    if ( auths.contains( adminRole ) ) {
      auths.remove( adminRole );
    }
    // Add extra roles to the list of roles if it does not already have it
    for ( String extraRole : extraRoles ) {
      if ( !auths.contains( extraRole ) ) {
        auths.add( extraRole );
      }
    }

    return auths;

  }

  @Override
  public List<String> getAllRoles() {
    return getAllRoles( userRoleDao.getRoles() );
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    return getAllRoles( userRoleDao.getRoles( tenant ) );
  }

  private List<String> getAllUsers( List<IPentahoUser> users ) {
    List<String> usernames = new ArrayList<String>();

    for ( IPentahoUser user : users ) {
      usernames.add( user.getUsername() );
    }

    return usernames;
  }

  @Override
  public List<String> getAllUsers() {
    return getAllUsers( userRoleDao.getUsers() );
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    return getAllUsers( userRoleDao.getUsers( tenant ) );
  }

  @Override
  public List<String> getRolesForUser( ITenant tenant, String username ) throws UsernameNotFoundException,
    DataAccessException {
    String userToSearch = username;
    // Extract Tenant from the user name
    ITenant tenantFromUser = JcrTenantUtils.getTenant( username, true );
    if ( tenantFromUser == null || tenantFromUser.getId() == null ) {
      // No tenant information in the user name so we check the tenant argument
      if ( tenant == null || tenant.getId() == null ) {
        // No tenant provided so we assume default tenant
        tenant = JcrTenantUtils.getDefaultTenant();
      }
      userToSearch = usernamePrincipalResolver.getPrincipleId( tenant, username );
    }
    UserDetails user = userDetailsService.loadUserByUsername( userToSearch );
    List<String> roles = new ArrayList<String>( user.getAuthorities().size() );
    for ( GrantedAuthority role : user.getAuthorities() ) {
      String principalName = role.getAuthority();
      roles.add( principalName );
    }
    return roles;
  }

  public List<String> getUsersInRole( ITenant tenant, IPentahoRole role, String roleName ) {
    if ( role == null ) {
      return Collections.emptyList();
    }
    List<IPentahoUser> users = null;
    List<String> usernames = new ArrayList<String>();
    if ( tenant == null ) {
      users = userRoleDao.getRoleMembers( null, roleName );
    } else {
      users = userRoleDao.getRoleMembers( tenant, roleName );
    }

    for ( IPentahoUser user : users ) {
      usernames.add( user.getUsername() );
    }

    return usernames;
  }

  @Override
  public List<String> getUsersInRole( ITenant tenant, String roleName ) {
    return getUsersInRole( tenant, userRoleDao.getRole( tenant, roleName ), roleName );
  }

  public void setUserRoleDao( IUserRoleDao userRoleDao ) {
    this.userRoleDao = userRoleDao;
  }

  public void setUserDetailsService( UserDetailsService userDetailsService ) {
    this.userDetailsService = userDetailsService;
  }

  @Override
  public List<String> getSystemRoles() {
    return systemRoles;
  }
}
