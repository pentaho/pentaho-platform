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


package org.pentaho.platform.web.http.api.resources.services;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.RoleListWrapper;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserRoleListService {

  protected IUserRoleListService userRoleListService;

  private List<String> extraRoles;

  private List<String> systemRoles;

  private String adminRole;

  private String anonymousRole;

  private Comparator<String> roleComparator;

  private Comparator<String> userComparator;

  public List<String> doGetRolesForUser( String user ) throws UnauthorizedException {
    if ( !canAdminister() ) {
      throw new UnauthorizedException();
    }

    return sortRoles( getRolesForUser( user ) );
  }

  public List<String> doGetUsersInRole( String role ) throws UnauthorizedException {
    if ( !canAdminister() ) {
      throw new UnauthorizedException();
    }

    return sortUsers( getUsersInRole( role ) );
  }

  public UserListWrapper getUsers() {
    IUserRoleListService service = getUserRoleListService();

    List<String> allUsers = new ArrayList<>( service.getAllUsers() );

    return new UserListWrapper( sortUsers( allUsers ) );
  }

  public RoleListWrapper getRoles() {
    return getRoles( true );
  }

  public RoleListWrapper getRoles( boolean includeExtraRoles ) {
    List<String> roles = new ArrayList<>( getUserRoleListService().getAllRoles() );
    /* If we need to exclude extra roles from the list of roles, we will remove it here.
    /  One thing to note that if a user has a role which is same as the extra role, that
    /  role will be removed as well. So we do not recommend user having same roles as the
    /  extra roles */
    if ( !includeExtraRoles && extraRoles != null ) {
      for ( String role : extraRoles ) {
        roles.remove( role );
      }
    }

    return new RoleListWrapper( sortRoles( roles ) );
  }

  public RoleListWrapper getAllRoles() {
    return getAllRoles( false );
  }

  public RoleListWrapper getAllRoles( boolean excludeAnonymous ) {
    Set<String> existingRoles = new HashSet<>( getUserRoleListService().getAllRoles() );

    if ( extraRoles != null ) {
      existingRoles.addAll( extraRoles );
    }

    if ( systemRoles != null ) {
      existingRoles.addAll( systemRoles );
    }

    if ( excludeAnonymous && getAnonymousRole() != null ) {
      existingRoles.remove( getAnonymousRole() );
    }

    return new RoleListWrapper( sortRoles( existingRoles ) );
  }

  public RoleListWrapper getSystemRoles() {
    return new RoleListWrapper( sortRoles( systemRoles ) );
  }

  /**
   * Gets the existing roles for permission/ACL management purposes.
   *
   * @return A list of roles.
   */
  public RoleListWrapper getPermissionRoles() {
    List<String> allRoles = new ArrayList<>( getUserRoleListService().getAllRoles() );

    // We will not allow user to update permission for Administrator.
    // Using getAdminRole() to support mocking in unit tests.
    String administratorRole = getAdminRole();
    if ( administratorRole != null ) {
      allRoles.remove( administratorRole );
    }

    // Add extra roles to the list of roles.
    if ( extraRoles != null ) {
      for ( String extraRole : extraRoles ) {
        if ( !allRoles.contains( extraRole ) ) {
          allRoles.add( extraRole );
        }
      }
    }

    return new RoleListWrapper( sortRoles( allRoles ) );
  }

  public RoleListWrapper getExtraRolesList() {
    return new RoleListWrapper( sortRoles( getExtraRoles() ) );
  }

  protected boolean canAdminister() {
    return SystemUtils.canAdminister();
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

  protected List<String> getUsersInRole( String role ) {
    return SystemService.getSystemService().getUsersInRole( role );
  }

  public void setExtraRoles( List<String> extraRoles ) {
    this.extraRoles = extraRoles;
  }

  public void setSystemRoles( List<String> systemRoles ) {
    this.systemRoles = systemRoles;
  }

  public void setRoleComparator( Comparator<String> roleComparator ) {
    this.roleComparator = roleComparator;
  }

  public void setUserComparator( Comparator<String> userComparator ) {
    this.userComparator = userComparator;
  }

  public List<String> getExtraRoles() {
    return this.extraRoles;
  }

  public String getAdminRole() {
    return adminRole;
  }

  public void setAdminRole( String adminRole ) {
    this.adminRole = adminRole;
  }

  public String getAnonymousRole() {
    return anonymousRole;
  }

  public void setAnonymousRole( String anonymousRole ) {
    this.anonymousRole = anonymousRole;
  }

  protected List<String> sortRoles( Collection<String> roles ) {
    Stream<String> rolesStream = roles.stream();

    if ( roleComparator != null ) {
      rolesStream = rolesStream.sorted( roleComparator );
    }

    return rolesStream.collect( Collectors.toList() );
  }

  protected List<String> sortUsers( Collection<String> users ) {
    Stream<String> usersStream = users.stream();

    if ( userComparator != null ) {
      usersStream = usersStream.sorted( userComparator );
    }

    return usersStream.collect( Collectors.toList() );
  }

  public static class UnauthorizedException extends Exception {
  }
}
