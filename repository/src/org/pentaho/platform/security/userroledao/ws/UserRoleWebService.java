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

package org.pentaho.platform.security.userroledao.ws;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.security.userroledao.messages.Messages;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class implements a concrete form of IUserRoleDao, wrapping the underlying IUserRoleDao implementation.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
@WebService( endpointInterface = "org.pentaho.platform.security.userroledao.ws.IUserRoleWebService",
    serviceName = "userRoleService", portName = "userRoleServicePort",
    targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class UserRoleWebService implements IUserRoleWebService {

  public UserRoleWebService() {
  }

  protected boolean isAdmin() {
    return SecurityHelper.getInstance().isPentahoAdministrator( PentahoSessionHolder.getSession() );
  }

  protected IUserRoleDao getDao() throws UserRoleException {
    if ( !isAdmin() ) {
      throw new UserRoleException( Messages.getInstance().getErrorString( "UserRoleWebService.ERROR_0001_NOT_ADMIN" ) ); //$NON-NLS-1$
    }
    IUserRoleDao dao = PentahoSystem.get( IUserRoleDao.class, "userRoleDaoTxn", PentahoSessionHolder.getSession() ); //$NON-NLS-1$
    if ( dao == null ) {
      throw new UserRoleException( Messages.getInstance().getErrorString(
          "UserRoleWebService.ERROR_0002_IUSERROLEDAO_NOT_AVAILABLE" ) ); //$NON-NLS-1$
    }
    return dao;
  }

  @Override
  public UserRoleSecurityInfo getUserRoleSecurityInfo() throws UserRoleException {
    UserRoleSecurityInfo userRoleSecurityInfo = new UserRoleSecurityInfo();
    IUserRoleDao dao = getDao();
    List<IPentahoUser> users = dao.getUsers();
    if ( users != null ) {
      for ( IPentahoUser user : users ) {
        userRoleSecurityInfo.getUsers().add( ProxyPentahoUserRoleHelper.toProxyUser( user ) );
        List<IPentahoRole> roles = dao.getUserRoles( user.getTenant(), user.getUsername() );
        if ( roles != null ) {
          for ( IPentahoRole role : roles ) {
            userRoleSecurityInfo.getAssignments().add( new UserToRoleAssignment( user.getUsername(), role.getName() ) );
          }
        }
      }
    }
    userRoleSecurityInfo.getRoles().addAll( Arrays.asList( getRoles() ) );
    return userRoleSecurityInfo;
  }

  // ~ User/Role Methods
  // ===============================================================================================

  @Override
  public boolean createUser( ProxyPentahoUser proxyUser ) throws UserRoleException {
    getDao().createUser( proxyUser.getTenant(), proxyUser.getName(), proxyUser.getPassword(),
        proxyUser.getDescription(), null );
    return true;
  }

  @Override
  public boolean deleteUsers( ProxyPentahoUser[] users ) throws UserRoleException {
    IPentahoUser[] persistedUsers = new IPentahoUser[users.length];
    for ( int i = 0; i < users.length; i++ ) {
      persistedUsers[i] = getDao().getUser( users[i].getTenant(), users[i].getName() );
      if ( persistedUsers[i] == null ) {
        throw new UserRoleException( Messages.getInstance().getErrorString(
            "UserRoleWebService.ERROR_0003_USER_DELETION_FAILED_NO_USER", users[i].getName() ) ); //$NON-NLS-1$
      }
    }
    for ( int i = 0; i < persistedUsers.length; i++ ) {
      getDao().deleteUser( persistedUsers[i] );
    }
    return true;
  }

  @Override
  public ProxyPentahoUser getUser( String pUserName ) throws UserRoleException {
    ProxyPentahoUser proxyPentahoUser = null;
    IPentahoUser user = getDao().getUser( null, pUserName );
    if ( user != null ) {
      proxyPentahoUser = ProxyPentahoUserRoleHelper.toProxyUser( user );
    }
    return proxyPentahoUser;
  }

  @Override
  public ProxyPentahoUser[] getUsers() throws UserRoleException {
    List<IPentahoUser> users = getDao().getUsers();
    if ( users != null ) {
      ProxyPentahoUser[] proxyUsers = new ProxyPentahoUser[users.size()];
      int i = 0;

      for ( IPentahoUser user : users ) {
        proxyUsers[i++] = ProxyPentahoUserRoleHelper.toProxyUser( user );
      }
      return proxyUsers;
    }
    return null;
  }

  @Override
  public ProxyPentahoUser[] getUsersForRole( ProxyPentahoRole proxyRole ) throws UserRoleException {
    ArrayList<ProxyPentahoUser> users = new ArrayList<ProxyPentahoUser>();
    IPentahoRole role = getDao().getRole( proxyRole.getTenant(), proxyRole.getName() );
    if ( role != null ) {
      for ( IPentahoUser user : getDao().getRoleMembers( proxyRole.getTenant(), proxyRole.getName() ) ) {
        users.add( ProxyPentahoUserRoleHelper.toProxyUser( user ) );
      }
    } else {
      throw new UserRoleException( Messages.getInstance().getErrorString(
          "UserRoleWebService.ERROR_0005_FAILED_TO_FIND_ROLE", proxyRole.getName() ) ); //$NON-NLS-1$
    }
    return users.toArray( new ProxyPentahoUser[0] );
  }

  @Override
  public boolean updateUser( ProxyPentahoUser proxyUser ) throws UserRoleException {
    IPentahoUser user = getDao().getUser( proxyUser.getTenant(), proxyUser.getName() );
    if ( user == null ) {
      throw new UserRoleException( Messages.getInstance().getErrorString(
          "UserRoleWebService.ERROR_0004_FAILED_TO_FIND_USER", proxyUser.getName() ) ); //$NON-NLS-1$
    }
    if ( !StringUtils.isBlank( proxyUser.getPassword() ) ) {
      getDao().setPassword( proxyUser.getTenant(), proxyUser.getName(), proxyUser.getPassword() );
    }
    getDao().setUserDescription( proxyUser.getTenant(), proxyUser.getName(), proxyUser.getDescription() );
    return true;
  }

  @Override
  public void setRoles( ProxyPentahoUser proxyUser, ProxyPentahoRole[] assignedRoles ) throws UserRoleException {
    IPentahoUser user = getDao().getUser( proxyUser.getTenant(), proxyUser.getName() );
    if ( user == null ) {
      throw new UserRoleException( Messages.getInstance().getErrorString(
          "UserRoleWebService.ERROR_0004_FAILED_TO_FIND_USER", proxyUser.getName() ) ); //$NON-NLS-1$
    }
    ArrayList<String> roles = new ArrayList<String>();
    for ( ProxyPentahoRole assignedRole : assignedRoles ) {
      roles.add( assignedRole.getName() );
    }
    getDao().setUserRoles( proxyUser.getTenant(), proxyUser.getName(), roles.toArray( new String[0] ) );
  }

  @Override
  public void setUsers( ProxyPentahoRole proxyRole, ProxyPentahoUser[] assignedUsers ) throws UserRoleException {
    IPentahoRole role = getDao().getRole( proxyRole.getTenant(), proxyRole.getName() );
    if ( role == null ) {
      throw new UserRoleException( Messages.getInstance().getErrorString(
          "UserRoleWebService.ERROR_0005_FAILED_TO_FIND_ROLE", proxyRole.getName() ) ); //$NON-NLS-1$
    }
    ArrayList<String> userNames = new ArrayList<String>();
    for ( ProxyPentahoUser proxyUser : assignedUsers ) {
      userNames.add( proxyUser.getName() );
    }
    getDao().setRoleMembers( proxyRole.getTenant(), proxyRole.getName(), userNames.toArray( new String[0] ) );
  }

  @Override
  public void updateRole( String roleName, String description, List<String> usernames ) throws UserRoleException {
    IPentahoRole role = getDao().getRole( null, roleName );
    if ( role == null ) {
      throw new UserRoleException( Messages.getInstance().getErrorString(
          "UserRoleWebService.ERROR_0006_ROLE_UPDATE_FAILED", roleName ) ); //$NON-NLS-1$
    }
    Set<String> users = new HashSet<String>();
    for ( String username : usernames ) {
      IPentahoUser user = getDao().getUser( null, username );
      if ( user == null ) {
        throw new UserRoleException( Messages.getInstance().getErrorString(
            "UserRoleWebService.ERROR_0006_ROLE_UPDATE_FAILED", roleName ) ); //$NON-NLS-1$
      }
      users.add( user.getUsername() );
    }
    getDao().setRoleDescription( null, roleName, description );
    getDao().setRoleMembers( null, roleName, users.toArray( new String[0] ) );
  }

  @Override
  public boolean createRole( ProxyPentahoRole proxyRole ) throws UserRoleException {
    getDao().createRole( proxyRole.getTenant(), proxyRole.getName(), proxyRole.getDescription(), new String[0] );
    return false;
  }

  @Override
  public boolean deleteRoles( ProxyPentahoRole[] roles ) throws UserRoleException {
    IPentahoRole[] persistedRoles;
    persistedRoles = new IPentahoRole[roles.length];
    for ( int i = 0; i < roles.length; i++ ) {
      persistedRoles[i] = getDao().getRole( roles[i].getTenant(), roles[i].getName() );
      if ( persistedRoles[i] == null ) {
        throw new UserRoleException( Messages.getInstance().getErrorString(
            "UserRoleWebService.ERROR_0007_ROLE_DELETION_FAILED_NO_ROLE", roles[i].getName() ) ); //$NON-NLS-1$
      }
    }
    for ( int i = 0; i < persistedRoles.length; i++ ) {
      getDao().deleteRole( persistedRoles[i] );
    }
    return true;
  }

  @Override
  public ProxyPentahoRole[] getRolesForUser( ProxyPentahoUser proxyUser ) throws UserRoleException {
    List<ProxyPentahoRole> proxyRoles = new ArrayList<ProxyPentahoRole>();
    IPentahoUser user = getDao().getUser( proxyUser.getTenant(), proxyUser.getName() );
    if ( user != null ) {
      for ( IPentahoRole role : getDao().getUserRoles( proxyUser.getTenant(), proxyUser.getName() ) ) {
        proxyRoles.add( ProxyPentahoUserRoleHelper.toProxyRole( role ) );
      }
    } else {
      throw new UserRoleException( Messages.getInstance().getErrorString(
          "UserRoleWebService.ERROR_0004_FAILED_TO_FIND_USER", proxyUser.getName() ) ); //$NON-NLS-1$
    }
    return proxyRoles.toArray( new ProxyPentahoRole[0] );
  }

  @Override
  public ProxyPentahoRole[] getRoles() throws UserRoleException {
    List<ProxyPentahoRole> proxyRoles = new ArrayList<ProxyPentahoRole>();
    List<IPentahoRole> roles = getDao().getRoles();
    if ( roles != null ) {
      for ( IPentahoRole role : roles ) {
        proxyRoles.add( ProxyPentahoUserRoleHelper.toProxyRole( role ) );
      }
    }
    return proxyRoles.toArray( new ProxyPentahoRole[0] );
  }

  @Override
  public boolean updateRoleObject( ProxyPentahoRole proxyPentahoRole ) throws UserRoleException {

    IPentahoRole role = getDao().getRole( proxyPentahoRole.getTenant(), proxyPentahoRole.getName() );
    if ( role == null ) {
      throw new UserRoleException( Messages.getInstance().getErrorString(
          "UserRoleWebService.ERROR_0006_ROLE_UPDATE_FAILED", proxyPentahoRole.getName() ) ); //$NON-NLS-1$
    }
    getDao().setRoleDescription( proxyPentahoRole.getTenant(), proxyPentahoRole.getName(),
        proxyPentahoRole.getDescription() );
    return true;
  }

}
