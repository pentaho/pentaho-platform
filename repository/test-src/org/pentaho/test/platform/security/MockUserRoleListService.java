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

package org.pentaho.test.platform.security;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockUserRoleListService implements IUserRoleListService {

  Map<String, List<String>> roleMap = new HashMap<String, List<String>>();
  Map<String, List<String>> userMap = new HashMap<String, List<String>>();
  Map<String, List<UsersInRole>> usersInRole = new HashMap<String, List<UsersInRole>>();
  Map<String, List<RolesInUser>> rolesInUser = new HashMap<String, List<RolesInUser>>();
  List<String> systemRoles = new ArrayList<String>();

  public MockUserRoleListService() {
    List<String> allAuths = new ArrayList<String>( 7 );
    allAuths.add( "dev" ); //$NON-NLS-1$
    allAuths.add( "Admin" ); //$NON-NLS-1$
    allAuths.add( "devmgr" ); //$NON-NLS-1$
    allAuths.add( "ceo" ); //$NON-NLS-1$
    allAuths.add( "cto" ); //$NON-NLS-1$
    allAuths.add( "Authenticated" ); //$NON-NLS-1$
    allAuths.add( "is" ); //$NON-NLS-1$
    roleMap.put( "default", allAuths );
    List<String> allUsers = new ArrayList<String>( 4 );
    allUsers.add( "pat" ); //$NON-NLS-1$
    allUsers.add( "tiffany" ); //$NON-NLS-1$
    allUsers.add( "admin" ); //$NON-NLS-1$
    allUsers.add( "suzy" ); //$NON-NLS-1$
    userMap.put( "default", allUsers );

    List<UsersInRole> userInRoles = new ArrayList<UsersInRole>( 6 );
    userInRoles.add( new UsersInRole( "dev", Arrays.asList( new String[] { "pat", "tiffany" } ) ) );
    userInRoles.add( new UsersInRole( "Admin", Arrays.asList( new String[] { "admin" } ) ) );
    userInRoles.add( new UsersInRole( "devmgr", Arrays.asList( new String[] { "tiffany" } ) ) );
    userInRoles.add( new UsersInRole( "ceo", Arrays.asList( new String[] { "admin" } ) ) );
    userInRoles.add( new UsersInRole( "cto", Arrays.asList( new String[] { "suzy" } ) ) );
    userInRoles.add( new UsersInRole( "is", Arrays.asList( new String[] { "suzy" } ) ) );
    usersInRole.put( "default", userInRoles );

    List<RolesInUser> roleInUser = new ArrayList<RolesInUser>( 6 );
    roleInUser.add( new RolesInUser( "pat", Arrays.asList( new String[] { "dev", "Authenticated" } ) ) );
    roleInUser.add( new RolesInUser( "tiffany", Arrays.asList( new String[] { "dev", "devmgr", "Authenticated" } ) ) );
    roleInUser.add( new RolesInUser( "admin", Arrays.asList( new String[] { "Admin", "ceo", "Authenticated" } ) ) );
    roleInUser.add( new RolesInUser( "suzy", Arrays.asList( new String[] { "cto", "is", "Authenticated" } ) ) );
    rolesInUser.put( "default", roleInUser );

    systemRoles.add( "Admin" );
    systemRoles.add( "Authenticated" );
  }

  @Override
  public List<String> getAllRoles() {
    return roleMap.get( "default" );
  }

  @Override
  public List<String> getAllUsers() {
    return userMap.get( "default" );
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    return roleMap.get( tenant.getName() );
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    return userMap.get( tenant.getName() );
  }

  private List<String> getUsersInRole( List<UsersInRole> usersInRoleList, String role ) {
    for ( UsersInRole userInRole : usersInRoleList ) {
      if ( userInRole.getRole().equals( role ) ) {
        return userInRole.getUsers();
      }
    }
    return Collections.emptyList();
  }

  @Override
  public List<String> getUsersInRole( ITenant tenant, String role ) {
    return getUsersInRole( usersInRole.get( tenant.getName() ), role );
  }

  private List<String> getRolesForUser( List<RolesInUser> rolesInUserList, String username ) {
    for ( RolesInUser roleInUser : rolesInUserList ) {
      if ( roleInUser.getUser().equals( username ) ) {
        return roleInUser.getRoles();
      }
    }
    return Collections.emptyList();
  }

  @Override
  public List<String> getRolesForUser( ITenant tenant, String username ) {
    return getRolesForUser( rolesInUser.get( tenant.getName() ), username );
  }

  class UsersInRole {
    String role;
    List<String> users;

    public UsersInRole() {
      role = null;
      users = new ArrayList<String>();
    }

    public UsersInRole( String role, List<String> users ) {
      this();
      this.role = role;
      this.users.addAll( users );
    }

    public String getRole() {
      return role;
    }

    public void setRole( String role ) {
      this.role = role;
    }

    public List<String> getUsers() {
      return users;
    }

    public void setUsers( List<String> users ) {
      this.users.clear();
      this.users.addAll( users );
    }

    public void add( String user ) {
      this.users.add( user );
    }
  }

  class RolesInUser {
    String user;
    List<String> roles;

    public RolesInUser() {
      user = null;
      roles = new ArrayList<String>();
    }

    public RolesInUser( String user, List<String> roles ) {
      this();
      this.user = user;
      this.roles.addAll( roles );
    }

    public String getUser() {
      return user;
    }

    public void setUser( String user ) {
      this.user = user;
    }

    public List<String> getRoles() {
      return roles;
    }

    public void setRoles( List<String> roles ) {
      this.roles.clear();
      this.roles.addAll( roles );
    }

    public void add( String role ) {
      this.roles.add( role );
    }
  }

  @Override
  public List<String> getSystemRoles() {
    return systemRoles;
  }
}
