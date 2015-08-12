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

package org.pentaho.platform.web.http.api.resources.services;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
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
import java.util.StringTokenizer;

public class UserRoleListService {

  protected IUserRoleListService userRoleListService;

  private ArrayList<String> extraRoles;

  private ArrayList<String> systemRoles;

  private Comparator<String> roleComparator;

  private Comparator<String> userComparator;

  public String doGetRolesForUser( String user ) throws Exception {
      return getRolesForUser( user );
  }

  public String doGetUsersInRole( String role ) throws Exception {
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

  public void deleteUsers( String userNames ) throws UnauthorizedException, UncategorizedUserRoleDaoException {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
          PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        StringTokenizer tokenizer = new StringTokenizer( userNames, "\t" );
        while ( tokenizer.hasMoreTokens() ) {
          IPentahoUser user = roleDao.getUser( null, tokenizer.nextToken() );
          if ( user != null ) {
            roleDao.deleteUser( user );
          }
        }
      } catch ( UncategorizedUserRoleDaoException e ) {
        throw new UncategorizedUserRoleDaoException( e.getLocalizedMessage() );
      }
    } else {
      throw new UnauthorizedException();
    }
  }

  private ITenant getTenant() {
    ITenant tenant = null;
    IPentahoSession session = PentahoSessionHolder.getSession();
    String tenantPath = (String) session.getAttribute( IPentahoSession.TENANT_ID_KEY );
    if ( tenantPath != null ) {
      tenant = new Tenant( tenantPath, true );
    }

    return tenant;
  }

  public void assignRoleToUser( String userName, String roleNames )
    throws UnauthorizedException, UncategorizedUserRoleDaoException, NotFoundException {
    if ( canAdminister() ) {
      IUserRoleDao roleDao =
        PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
      ITenant tenant = getTenant();

      StringTokenizer tokenizer = new StringTokenizer( roleNames, "\t" );
      Set<String> assignedRoles = new HashSet<String>();
      //Build a set of roles the userName already has
      for ( IPentahoRole pentahoRole : roleDao.getUserRoles( tenant, userName ) ) {
        assignedRoles.add( pentahoRole.getName() );
      }
      //Add the roles we passed in to the set of roles
      while ( tokenizer.hasMoreTokens() ) {
        assignedRoles.add( tokenizer.nextToken() );
      }
      try {
        //Apply the total list of roles to the user
        roleDao.setUserRoles( tenant, userName, assignedRoles.toArray( new String[assignedRoles.size()] ) );
      } catch ( NotFoundException e ) {
        throw new NotFoundException( e.getLocalizedMessage() );
      } catch( UncategorizedUserRoleDaoException e ) {
        throw new UncategorizedUserRoleDaoException( e.getLocalizedMessage() );
      }
    } else {
      throw new UnauthorizedException();
    }
  }

  public RoleListWrapper getRoles() {
    return new RoleListWrapper( getUserRoleListService().getAllRoles() );
  }

  public RoleListWrapper getAllRoles() {
    List<String> roles = getUserRoleListService().getAllRoles();
    roles.addAll( getExtraRoles() );
    return new RoleListWrapper( roles );
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

  protected String getRolesForUser( String user ) throws Exception {
    return SystemService.getSystemService().getRolesForUser( user ).asXML();
  }

  protected String getUsersInRole( String role ) throws Exception {
    return SystemService.getSystemService().getUsersInRole( role ).asXML();
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

  public static class UnauthorizedException extends Exception {
  }
}
