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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.userroledao.jackrabbit.security.DefaultPentahoPasswordEncoder;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.LocalizedLogicalRoleName;
import org.pentaho.platform.web.http.api.resources.LogicalRoleAssignment;
import org.pentaho.platform.web.http.api.resources.LogicalRoleAssignments;
import org.pentaho.platform.web.http.api.resources.RoleListWrapper;
import org.pentaho.platform.web.http.api.resources.SystemRolesMap;
import org.pentaho.platform.web.http.api.resources.User;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;

public class UserRoleDaoService {
  private IUserRoleDao roleDao;
  private IAuthorizationPolicy policy;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

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
      getRoleDao().setUserRoles( tenant, userName, assignedRoles.toArray( new String[assignedRoles.size()] ) );
    } else {
      throw new SecurityException();
    }
  }

  public void createRole( String roleName ) throws Exception {
    if ( canAdminister() ) {
      if ( strNotEmpty( roleName ) ) {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        roleDao.createRole( null, roleName, "", new String[0] );
      } else {
        throw new ValidationFailedException();
      }
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

  private boolean containsReservedChars( String username ) {
    StringBuffer reservedChars = new FileService().doGetReservedChars();
    return StringUtils.containsAny( username, reservedChars );
  }

  private boolean strNotEmpty( String str ) {
    return str != null && str.length() > 0;
  }

  private boolean userValid( User user ) {
    String name = user.getUserName();
    String pass = user.getPassword();

    boolean nameValid = strNotEmpty( name ) && !containsReservedChars( name );
    boolean passValid = strNotEmpty( pass );
    return nameValid && passValid;
  }

  private String decode( String toDecode ) {
    try {
      return URLDecoder.decode( toDecode.replace( "+", "%2B" ), "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
      return toDecode;
    }
  }

  public void createUser( User user ) throws Exception {
    if ( canAdminister() ) {
      if ( userValid( user ) ) {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        roleDao.createUser( null, decode( user.getUserName() ), decode( user.getPassword() ), "", new String[0] );
      } else {
        throw new ValidationFailedException();
      }
    } else {
      throw new SecurityException();
    }
  }

  private boolean inputValid( String userName, String newPass, String oldPass ) {
    boolean userNameValid = ( userName != null && userName.length() > 0 );
    boolean newPassValid = ( newPass != null && newPass.length() > 0 );
    boolean oldPassValid = ( oldPass != null && oldPass.length() > 0 );
    return userNameValid && newPassValid && oldPassValid;
  }

  private boolean credentialValid( IPentahoUser pentahoUser, String oldPass ) {
    if ( pentahoUser != null ) {
      DefaultPentahoPasswordEncoder encoder = new DefaultPentahoPasswordEncoder();
      return encoder.isPasswordValid( pentahoUser.getPassword(), oldPass, null );
    }
    return false;
  }

  public void changeUserPassword( final String userName, final String newPass, String oldPass ) throws Exception {
    if ( inputValid( userName, newPass, oldPass ) ) {
      final IUserRoleDao roleDao =
          PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
      IPentahoUser pentahoUser = roleDao.getUser( null, userName );

      if ( credentialValid( pentahoUser, oldPass ) ) {
        SecurityHelper.getInstance().runAsSystem( new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            roleDao.setPassword( null, userName, newPass );
            return null;
          }
        } );
      } else {
        throw new SecurityException();
      }
    } else {
      throw new ValidationFailedException();
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

  public SystemRolesMap getRoleBindingStruct( String locale ) throws SecurityException {
    if ( canAdminister() ) {
      RoleBindingStruct roleBindingStruct = getRoleBindingDao().getRoleBindingStruct( locale );
      SystemRolesMap systemRolesMap = new SystemRolesMap();
      for ( Map.Entry<String, String> localalizeNameEntry : roleBindingStruct.logicalRoleNameMap.entrySet() ) {
        systemRolesMap.getLocalizedRoleNames().add(
          new LocalizedLogicalRoleName( localalizeNameEntry.getKey(), localalizeNameEntry.getValue() ) );
      }
      for ( Map.Entry<String, List<String>> logicalRoleAssignments : roleBindingStruct.bindingMap.entrySet() ) {
        systemRolesMap.getAssignments().add(
          new LogicalRoleAssignment( logicalRoleAssignments.getKey(), logicalRoleAssignments.getValue()
            , roleBindingStruct.immutableRoles.contains( logicalRoleAssignments.getKey() ) )
        );
      }
      return systemRolesMap;
    } else {
      throw new SecurityException();
    }
  }

  public void setLogicalRoles( LogicalRoleAssignments roleAssignments ) throws SecurityException {
    if ( canAdminister() ) {
      for ( LogicalRoleAssignment roleAssignment : roleAssignments.getAssignments() ) {
        getRoleBindingDao().setRoleBindings( roleAssignment.getRoleName(), roleAssignment.getLogicalRoles() );
      }
    } else {
      throw new SecurityException();
    }
  }
  
  public void updatePassword( User user ) throws SecurityException {
    if ( canAdminister() ) {
      String userName = decode( user.getUserName() );
      String password = decode( user.getPassword() );
      IUserRoleDao roleDao =
          PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
      IPentahoUser puser = roleDao.getUser( null, userName );
      if ( puser != null ) {
        roleDao.setPassword( null, userName, password );
      }
    } else {
      throw new SecurityException();
    }
  }

  private boolean canAdminister() {
    return getPolicy().isAllowed( RepositoryReadAction.NAME ) && getPolicy().isAllowed( RepositoryCreateAction.NAME )
      && ( getPolicy().isAllowed( AdministerSecurityAction.NAME ) );
  }

  private IRoleAuthorizationPolicyRoleBindingDao getRoleBindingDao() {
    if ( roleBindingDao == null ) {
      roleBindingDao = PentahoSystem.get( IRoleAuthorizationPolicyRoleBindingDao.class );
    }

    return roleBindingDao;
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
  
  public static class ValidationFailedException extends Exception {
  }
  
}
