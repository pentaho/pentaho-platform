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


package org.pentaho.platform.security.policy.rolebased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

/**
 * An authorization policy based on roles.
 * 
 * @author mlowery
 */
public class RoleAuthorizationPolicy implements IAuthorizationPolicy {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  // ~ Constructors
  // ====================================================================================================

  public RoleAuthorizationPolicy( final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao ) {
    super();
    Assert.notNull( roleBindingDao, "The role binding DAO must not be null. Ensure a valid instance is provided." );
    this.roleBindingDao = roleBindingDao;
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public List<String> getAllowedActions( String actionNamespace ) {
    List<String> assignedRolesInNamespace = new ArrayList<String>();
    if ( actionNamespace == null ) {
      assignedRolesInNamespace.addAll( roleBindingDao.getBoundLogicalRoleNames( getRuntimeRoleNames() ) );
    } else {
      if ( !actionNamespace.endsWith( "." ) ) {
        actionNamespace += ".";
      }
      for ( String assignedRole : roleBindingDao.getBoundLogicalRoleNames( getRuntimeRoleNames() ) ) {
        if ( assignedRole.startsWith( actionNamespace ) ) {
          assignedRolesInNamespace.add( assignedRole );
        }
      }
    }
    return assignedRolesInNamespace;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAllowed( String actionName ) {
    return roleBindingDao.getBoundLogicalRoleNames( getRuntimeRoleNames() ).contains( actionName );
  }

  protected List<String> getRuntimeRoleNames() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Assert.state( authentication != null, "Authentication object must not be null. Ensure the security context is properly initialized." );
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    List<String> runtimeRoles = new ArrayList<String>( authorities.size() );
    for ( GrantedAuthority authority : authorities ) {
      runtimeRoles.add( authority.getAuthority() );
    }
    return runtimeRoles;
  }

}
