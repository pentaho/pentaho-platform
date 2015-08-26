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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.services;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.RoleListWrapper;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class UserRoleDaoService {
  private IUserRoleDao roleDao;
  private IAuthorizationPolicy policy;

  public UserListWrapper getUsers() throws Exception {
    return new UserListWrapper( getRoleDao().getUsers() );
  }

  public RoleListWrapper getRolesForUser( String user ) throws UncategorizedUserRoleDaoException {
    ITenant tenant = TenantUtils.getCurrentTenant();
    return new RoleListWrapper( getRoleDao().getUserRoles( tenant, user ) );
  }

  public void assignRolesToUser( String userName, String roleNames )
    throws NotFoundException, UncategorizedUserRoleDaoException, SecurityException {
    if ( canAdminister() ) {
      StringTokenizer tokenizer = new StringTokenizer( roleNames, "\t" );
      Set<String> assignedRoles = new HashSet<String>();
      ITenant tenant = TenantUtils.getCurrentTenant();

      //Build the set of roles the user already contians
      for ( IPentahoRole pentahoRole : getRoleDao().getUserRoles( tenant, userName ) ) {
        assignedRoles.add( pentahoRole.getName() );
      }
      //Append the parameter of roles
      while ( tokenizer.hasMoreTokens() ) {
        assignedRoles.add( tokenizer.nextToken() );
      }

      getRoleDao().setUserRoles( tenant, userName, assignedRoles.toArray( new String[ assignedRoles.size() ] ) );
    } else {
      throw new SecurityException();
    }
  }

  public void removeRolesFromUser( String userName, String roleNames )
    throws NotFoundException, UncategorizedUserRoleDaoException, SecurityException {
    if ( canAdminister() ) {
      StringTokenizer tokenizer = new StringTokenizer( roleNames, "\t" );
      Set<String> assignedRoles = new HashSet<String>();
      ITenant tenant = TenantUtils.getCurrentTenant();

      for ( IPentahoRole pentahoRole : getRoleDao().getUserRoles( tenant, userName ) ) {
        assignedRoles.add( pentahoRole.getName() );
      }
      while ( tokenizer.hasMoreTokens() ) {
        assignedRoles.remove( tokenizer.nextToken() );
      }
      getRoleDao().setUserRoles( tenant, userName, assignedRoles.toArray( new String[ assignedRoles.size() ] ) );
    } else {
      throw new SecurityException();
    }
  }

  public RoleListWrapper getRoles() throws UncategorizedUserRoleDaoException {
    return new RoleListWrapper( getRoleDao().getRoles() );
  }

  public UserListWrapper getRoleMembers( String roleName ) throws UncategorizedUserRoleDaoException, SecurityException {
    if ( canAdminister() ) {
      return new UserListWrapper( getRoleDao().getRoleMembers( TenantUtils.getCurrentTenant(), roleName ) );
    } else {
      throw new SecurityException();
    }
  }

  public void deleteUsers( String userNames )
    throws NotFoundException, UncategorizedUserRoleDaoException, SecurityException {
    if ( canAdminister() ) {
      StringTokenizer tokenizer = new StringTokenizer( userNames, "\t" );
      while ( tokenizer.hasMoreTokens() ) {
        IPentahoUser user = getRoleDao().getUser( null, tokenizer.nextToken() );
        if ( user != null ) {
          getRoleDao().deleteUser( user );
        }
      }
    } else {
      throw new SecurityException();
    }
  }

  public void deleteRoles( String roleNames ) throws SecurityException, UncategorizedUserRoleDaoException {
    if ( canAdminister() ) {
      StringTokenizer tokenizer = new StringTokenizer( roleNames, "\t" );
      while ( tokenizer.hasMoreTokens() ) {
        IPentahoRole role = getRoleDao().getRole( null, tokenizer.nextToken() );
        if ( role != null ) {
          getRoleDao().deleteRole( role );
        }
      }
    } else {
      throw new SecurityException();
    }
  }

  private boolean canAdminister() {
    return getPolicy().isAllowed( RepositoryReadAction.NAME ) && getPolicy().isAllowed( RepositoryCreateAction.NAME )
      && ( getPolicy().isAllowed( AdministerSecurityAction.NAME ) );
  }

  private IAuthorizationPolicy getPolicy() {
    if ( policy == null ) {
      policy = PentahoSystem.get( IAuthorizationPolicy.class );
    }

    return policy;
  }

  private IUserRoleDao getRoleDao() {
    if ( roleDao == null ) {
      roleDao = PentahoSystem.get( IUserRoleDao.class );
    }

    return roleDao;
  }
}
