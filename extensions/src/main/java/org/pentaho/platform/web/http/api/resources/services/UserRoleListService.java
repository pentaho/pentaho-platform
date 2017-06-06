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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.services;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.RoleListWrapper;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserRoleListService {

  protected IUserRoleListService userRoleListService;

  private ArrayList<String> extraRoles;

  private ArrayList<String> systemRoles;

  private Comparator<String> roleComparator;

  private Comparator<String> userComparator;

  public List<String> doGetRolesForUser( String user ) throws UnauthorizedException {
    if ( canAdminister() ) {
      return getRolesForUser( user );
    } else {
      throw new UnauthorizedException();
    }
  }

  public List<String> doGetUsersInRole( String role ) throws UnauthorizedException {
    if ( canAdminister() ) {
      return getUsersInRole( role );
    } else {
      throw new UnauthorizedException();
    }
  }

  public UserListWrapper getUsers() {
    IUserRoleListService service = getUserRoleListService();
    List<String> allUsers = service.getAllUsers();
    if ( null != userComparator ) {
      Collections.sort( allUsers, userComparator );
    }
    return new UserListWrapper( allUsers );
  }

  public RoleListWrapper getRoles() {
    return getRoles( true );
  }

  public RoleListWrapper getRoles( boolean includeExtraRoles ) {
    List<String> roles = getUserRoleListService().getAllRoles();
    /* If we need to exclude extra roles from the list of roles, we will remove it here.
    /  One thing to note that if a user has a role which is same as the extra role, that
    /  role will be removed as well. So we do not recommend user having same roles as the
    /  extra roles */
    if ( !includeExtraRoles ) {
      for ( String role : getExtraRoles() ) {
        roles.remove( role );
      }
    }
    return new RoleListWrapper( roles );
  }

  public RoleListWrapper getAllRoles() {
    Set<String> existingRoles = new HashSet<>( getUserRoleListService().getAllRoles() );
    List<String> extraRoles = getExtraRoles();
    if ( extraRoles == null ) {
      extraRoles = new ArrayList<>();
    }
    if ( systemRoles != null ) {
      extraRoles.addAll( systemRoles );
    }

    existingRoles.addAll( extraRoles );

    return new RoleListWrapper( existingRoles );
  }

  public RoleListWrapper getSystemRoles() {

    return new RoleListWrapper( systemRoles );
  }

  public RoleListWrapper getPermissionRoles( String adminRole ) {
    IUserRoleListService userRoleListService = getUserRoleListService();
    List<String> allRoles = userRoleListService.getAllRoles();
    // We will not allow user to update permission for Administrator
    if ( allRoles.contains( adminRole ) ) {
      allRoles.remove( adminRole );
    }

    // Add extra roles to the list of roles
    if ( extraRoles != null ) {
      for ( String extraRole : extraRoles ) {
        if ( !allRoles.contains( extraRole ) ) {
          allRoles.add( extraRole );
        }
      }
    }
    if ( null != roleComparator ) {
      Collections.sort( allRoles, roleComparator );
    }
    return new RoleListWrapper( allRoles );
  }

  public RoleListWrapper getExtraRolesList() {
    return new RoleListWrapper( getExtraRoles() );
  }

  protected boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
      && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }

  public IUserRoleListService getUserRoleListService() {
    if ( userRoleListService == null ) {
      userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    }
    return userRoleListService;
  }

  protected List<String> getRolesForUser( String user ) {
    return SystemService.getSystemService().getRolesForUser( user );
  }

  protected List<String> getUsersInRole( String role )  {
    return SystemService.getSystemService().getUsersInRole( role );
  }

  public void setExtraRoles( ArrayList<String> extraRoles ) {
    this.extraRoles = extraRoles;
  }

  public void setSystemRoles( ArrayList<String> systemRoles ) {
    this.systemRoles = systemRoles;
  }

  public void setRoleComparator( Comparator<String> roleComparator ) {
    this.roleComparator = roleComparator;
  }

  public void setUserComparator( Comparator<String> userComparator ) {
    this.userComparator = userComparator;
  }

  public ArrayList<String> getExtraRoles() {
    return this.extraRoles;
  }

  public class UnauthorizedException extends Exception {
  }
}
