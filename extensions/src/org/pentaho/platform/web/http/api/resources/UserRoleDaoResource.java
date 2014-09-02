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

package org.pentaho.platform.web.http.api.resources;

import com.sun.jersey.api.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * UserRoleDao manage pentaho security user and roles in the platform.
 */
@Path ( "/userroledao/" )
public class UserRoleDaoResource extends AbstractJaxRSResource {

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = null;
  private ITenantManager tenantManager = null;
  private ArrayList<String> systemRoles;
  private String adminRole;

  private static final Log logger =
      LogFactory.getLog( UserRoleDaoResource.class );


  public UserRoleDaoResource() {
    this( PentahoSystem.get( IRoleAuthorizationPolicyRoleBindingDao.class ), PentahoSystem.get( ITenantManager.class ),
        PentahoSystem.get( ArrayList.class, "singleTenantSystemAuthorities", PentahoSessionHolder.getSession() ),
        PentahoSystem.get( String.class, "singleTenantAdminAuthorityName", PentahoSessionHolder.getSession() ) );
  }

  public UserRoleDaoResource( final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
                              final ITenantManager tenantMgr, final ArrayList<String> systemRoles, final String adminRole ) {

    if ( roleBindingDao == null ) {
      throw new IllegalArgumentException();
    }

    this.roleBindingDao = roleBindingDao;
    this.tenantManager = tenantMgr;
    this.systemRoles = systemRoles;
    this.adminRole = adminRole;

  }

  /**
   * Returns the list of users in the platform's repository
   *
   * @return list of users in the platform
   * @throws Exception
   */
  @GET
  @Path ( "/users" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  public UserListWrapper getUsers() throws Exception {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        return new UserListWrapper( roleDao.getUsers() );
      } catch ( Throwable t ) {
        throw new WebApplicationException( t );
      }
    } else {
      throw new WebApplicationException( new Throwable() );
    }
  }

  /**
   * Returns the list of roles in the platform's repository
   *
   * @return list of roles in the platform
   * @throws Exception
   */
  @GET
  @Path ( "/roles" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  public RoleListWrapper getRoles() throws Exception {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        return new RoleListWrapper( roleDao.getRoles() );
      } catch ( Throwable t ) {
        throw new WebApplicationException( t );
      }
    } else {
      throw new WebApplicationException( new Throwable() );
    }
  }

  /**
   * Retrieves a selected user's roles
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param userName   (user name)
   * @return list of roles fir the selected user
   * @throws Exception
   */
  @GET
  @Path ( "/userRoles" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  public RoleListWrapper getUserRoles( @QueryParam ( "tenant" ) String tenantPath,
                                       @QueryParam ( "userName" ) String userName ) throws Exception {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        return new RoleListWrapper( roleDao.getUserRoles( getTenant( tenantPath ), userName ) );
      } catch ( Throwable t ) {
        throw new WebApplicationException( t );
      }
    } else {
      throw new WebApplicationException( new Throwable() );
    }
  }

  /**
   * Retrieves list of users for the selected role
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param roleName   (role name)
   * @return list of users for the selected role
   * @throws Exception
   */
  @GET
  @Path ( "/roleMembers" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  public UserListWrapper getRoleMembers( @QueryParam ( "tenant" ) String tenantPath,
                                         @QueryParam ( "roleName" ) String roleName ) throws Exception {
    IUserRoleDao roleDao =
        PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
    return new UserListWrapper( roleDao.getRoleMembers( getTenant( tenantPath ), roleName ) );
  }

  /**
   * Associates selected role(s) to a user
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param userName   (username)
   * @param roleNames  (tab (\t) separated list of role names)
   * @return
   */
  @PUT
  @Path ( "/assignRoleToUser" )
  @Consumes ( { WILDCARD } )
  public Response assignRoleToUser( @QueryParam ( "tenant" ) String tenantPath,
                                    @QueryParam ( "userName" ) String userName, @QueryParam ( "roleNames" ) String roleNames ) {
    if ( canAdminister() ) {
      IUserRoleDao roleDao =
          PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
      StringTokenizer tokenizer = new StringTokenizer( roleNames, "\t" );
      Set<String> assignedRoles = new HashSet<String>();
      for ( IPentahoRole pentahoRole : roleDao.getUserRoles( getTenant( tenantPath ), userName ) ) {
        assignedRoles.add( pentahoRole.getName() );
      }
      while ( tokenizer.hasMoreTokens() ) {
        assignedRoles.add( tokenizer.nextToken() );
      }
      try {
        roleDao.setUserRoles( getTenant( tenantPath ), userName, assignedRoles.toArray( new String[0] ) );
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
      return Response.ok().build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Remove selected roles(s) from a selected user
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param userName   (username)
   * @param roleNames  (tab (\t) separated list of role names)
   * @return
   */
  @PUT
  @Path ( "/removeRoleFromUser" )
  @Consumes ( { WILDCARD } )
  public Response removeRoleFromUser( @QueryParam ( "tenant" ) String tenantPath,
                                      @QueryParam ( "userName" ) String userName, @QueryParam ( "roleNames" ) String roleNames ) {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        StringTokenizer tokenizer = new StringTokenizer( roleNames, "\t" );
        Set<String> assignedRoles = new HashSet<String>();
        for ( IPentahoRole pentahoRole : roleDao.getUserRoles( getTenant( tenantPath ), userName ) ) {
          assignedRoles.add( pentahoRole.getName() );
        }
        while ( tokenizer.hasMoreTokens() ) {
          assignedRoles.remove( tokenizer.nextToken() );
        }
        roleDao.setUserRoles( getTenant( tenantPath ), userName, assignedRoles.toArray( new String[0] ) );
        return Response.ok().build();
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Associate all roles to the selected user
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param userName   (username)
   * @return
   */
  @PUT
  @Path ( "/assignAllRolesToUser" )
  @Consumes ( { WILDCARD } )
  public Response assignAllRolesToUser( @QueryParam ( "tenant" ) String tenantPath,
                                        @QueryParam ( "userName" ) String userName ) {
    IUserRoleDao roleDao =
        PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
    Set<String> assignedRoles = new HashSet<String>();
    for ( IPentahoRole pentahoRole : roleDao.getRoles( getTenant( tenantPath ) ) ) {
      assignedRoles.add( pentahoRole.getName() );
    }
    roleDao.setUserRoles( getTenant( tenantPath ), userName, assignedRoles.toArray( new String[0] ) );
    return Response.ok().build();
  }

  /**
   * Remove all roles from the selected user
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param userName   (username)
   * @return
   */
  @PUT
  @Path ( "/removeAllRolesFromUser" )
  @Consumes ( { WILDCARD } )
  public Response removeAllRolesFromUser( @QueryParam ( "tenant" ) String tenantPath,
                                          @QueryParam ( "userName" ) String userName ) {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        roleDao.setUserRoles( getTenant( tenantPath ), userName, new String[0] );
        return Response.ok().build();
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Associate list of users to the selected role
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param userNames  (list of tab (\t) separated user names
   * @param roleName   (role name)
   * @return
   */
  @PUT
  @Path ( "/assignUserToRole" )
  @Consumes ( { WILDCARD } )
  public Response assignUserToRole( @QueryParam ( "tenant" ) String tenantPath,
                                    @QueryParam ( "userNames" ) String userNames, @QueryParam ( "roleName" ) String roleName ) {
    if ( canAdminister() ) {
      IUserRoleDao roleDao =
          PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
      StringTokenizer tokenizer = new StringTokenizer( userNames, "\t" );
      Set<String> assignedUserNames = new HashSet<String>();
      for ( IPentahoUser pentahoUser : roleDao.getRoleMembers( getTenant( tenantPath ), roleName ) ) {
        assignedUserNames.add( pentahoUser.getUsername() );
      }
      while ( tokenizer.hasMoreTokens() ) {
        assignedUserNames.add( tokenizer.nextToken() );
      }
      try {
        roleDao.setRoleMembers( getTenant( tenantPath ), roleName, assignedUserNames.toArray( new String[0] ) );
        return Response.ok().build();
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Remove user(s) from a particular role
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param userNames  (list of tab (\t) separated user names
   * @param roleName   (role name)
   * @return
   */
  @PUT
  @Path ( "/removeUserFromRole" )
  @Consumes ( { WILDCARD } )
  public Response removeUserFromRole( @QueryParam ( "tenant" ) String tenantPath,
                                      @QueryParam ( "userNames" ) String userNames, @QueryParam ( "roleName" ) String roleName ) {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        StringTokenizer tokenizer = new StringTokenizer( userNames, "\t" );
        Set<String> assignedUserNames = new HashSet<String>();
        for ( IPentahoUser pentahoUser : roleDao.getRoleMembers( getTenant( tenantPath ), roleName ) ) {
          assignedUserNames.add( pentahoUser.getUsername() );
        }
        while ( tokenizer.hasMoreTokens() ) {
          assignedUserNames.remove( tokenizer.nextToken() );
        }
        roleDao.setRoleMembers( getTenant( tenantPath ), roleName, assignedUserNames.toArray( new String[0] ) );
        return Response.ok().build();
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Associates all user to a particular role
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param roleName   (role name)
   * @return
   */
  @PUT
  @Path ( "/assignAllUsersToRole" )
  @Consumes ( { WILDCARD } )
  public Response assignAllUsersToRole( @QueryParam ( "tenant" ) String tenantPath,
                                        @QueryParam ( "roleName" ) String roleName ) {
    IUserRoleDao roleDao =
        PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
    Set<String> assignedUserNames = new HashSet<String>();
    for ( IPentahoUser pentahoUser : roleDao.getUsers( getTenant( tenantPath ) ) ) {
      assignedUserNames.add( pentahoUser.getUsername() );
    }
    roleDao.setRoleMembers( getTenant( tenantPath ), roleName, assignedUserNames.toArray( new String[0] ) );
    return Response.ok().build();
  }

  /**
   * Removes all users from a particular role
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param roleName   (role name)
   * @return
   */
  @PUT
  @Path ( "/removeAllUsersFromRole" )
  @Consumes ( { WILDCARD } )
  public Response removeAllUsersFromRole( @QueryParam ( "tenant" ) String tenantPath,
                                          @QueryParam ( "roleName" ) String roleName ) {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        roleDao.setRoleMembers( getTenant( tenantPath ), roleName, new String[0] );
        return Response.ok().build();
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Create a new user with provided information.
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param user       (user information <code> User </code>)
   * @return
   */
  @PUT
  @Path ( "/createUser" )
  @Consumes ( { WILDCARD } )
  public Response createUser( @QueryParam ( "tenant" ) String tenantPath, User user ) {
    IUserRoleDao roleDao =
        PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
    String userName = user.getUserName();
    String password = user.getPassword();
    try {
      userName = URLDecoder.decode( userName.replace( "+", "%2B" ), "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
      userName = user.getUserName();
      logger.warn( e.getMessage(), e );
    }
    try {
      password = URLDecoder.decode( password.replace( "+", "%2B" ), "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
      password = user.getPassword();
      logger.warn( e.getMessage(), e );
    }
    roleDao.createUser( getTenant( tenantPath ), userName, password, "", new String[0] );
    return Response.ok().build();
  }

  /**
   * Create a new role with the provided information
   *
   * @param tenantPath (tenant path where the user exist, null of empty string assumes default tenant)
   * @param roleName   (name of the new role)
   * @return
   */
  @PUT
  @Path ( "/createRole" )
  @Consumes ( { WILDCARD } )
  public Response createRole( @QueryParam ( "tenant" ) String tenantPath, @QueryParam ( "roleName" ) String roleName ) {
    IUserRoleDao roleDao =
        PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
    roleDao.createRole( getTenant( tenantPath ), roleName, "", new String[0] );
    return Response.ok().build();
  }

  /**
   * Delete role(s) from the platform
   *
   * @param roleNames (list of tab (\t) separated role names)
   * @return
   */
  @PUT
  @Path ( "/deleteRoles" )
  @Consumes ( { WILDCARD } )
  public Response deleteRole( @QueryParam ( "roleNames" ) String roleNames ) {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        StringTokenizer tokenizer = new StringTokenizer( roleNames, "\t" );
        while ( tokenizer.hasMoreTokens() ) {
          IPentahoRole role = roleDao.getRole( null, tokenizer.nextToken() );
          if ( role != null ) {
            roleDao.deleteRole( role );
          }
        }
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
      return Response.ok().build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Delete user(s) from the platform
   *
   * @param userNames (list of tab (\t) separated user names)
   * @return
   */
  @PUT
  @Path ( "/deleteUsers" )
  @Consumes ( { WILDCARD } )
  public Response deleteUser( @QueryParam ( "userNames" ) String userNames ) {
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
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
      return Response.ok().build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Update the password of a selected user
   *
   * @param user (user information <code> User </code>)
   * @return
   */
  @PUT
  @Path ( "/updatePassword" )
  @Consumes ( { WILDCARD } )
  public Response updatePassword( User user ) {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao =
            PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
        String userName = user.getUserName();
        String password = user.getPassword();
        try {
          userName = URLDecoder.decode( userName.replace( "+", "%2B" ), "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
          userName = user.getUserName();
          logger.warn( e.getMessage(), e );
        }
        try {
          password = URLDecoder.decode( password.replace( "+", "%2B" ), "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
          password = user.getPassword();
          logger.warn( e.getMessage(), e );
        }
        IPentahoUser puser = roleDao.getUser( null, userName );
        if ( puser != null ) {
          roleDao.setPassword( null, userName, password );
        }
        return Response.ok().build();
      } catch ( Throwable t ) {
        throw new WebApplicationException( t );
      }
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Retrieve the list of logical roles in the platform
   *
   * @param locale (locale)
   * @return
   */
  @GET
  @Path ( "/logicalRoleMap" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  public SystemRolesMap getRoleBindingStruct( @QueryParam ( "locale" ) String locale ) {
    if ( canAdminister() ) {
      try {
        RoleBindingStruct roleBindingStruct = roleBindingDao.getRoleBindingStruct( locale );
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
      } catch ( Throwable t ) {
        throw new WebApplicationException( t );
      }
    } else {
      throw new WebApplicationException( new Throwable() );
    }
  }

  /**
   * Associate a particular runtime role to list of logical role in the repository
   *
   * @param roleAssignments (logical to runtime role assignments)
   * @return
   */
  @PUT
  @Consumes ( { APPLICATION_XML, APPLICATION_JSON } )
  @Path ( "/roleAssignments" )
  public Response setLogicalRoles( LogicalRoleAssignments roleAssignments ) {
    for ( LogicalRoleAssignment roleAssignment : roleAssignments.getAssignments() ) {
      roleBindingDao.setRoleBindings( roleAssignment.getRoleName(), roleAssignment.getLogicalRoles() );
    }
    return Response.ok().build();
  }

  private ITenant getTenant( String tenantId ) throws NotFoundException {
    ITenant tenant = null;
    if ( tenantId != null ) {
      tenant = tenantManager.getTenant( tenantId );
      if ( tenant == null ) {
        throw new NotFoundException( "Tenant not found." );
      }
    } else {
      IPentahoSession session = PentahoSessionHolder.getSession();
      String tenantPath = (String) session.getAttribute( IPentahoSession.TENANT_ID_KEY );
      if ( tenantPath != null ) {
        tenant = new Tenant( tenantPath, true );
      }
    }
    return tenant;
  }

  private HashSet<String> tokenToString( String tokenString ) {
    StringTokenizer tokenizer = new StringTokenizer( tokenString, "\t" );
    HashSet<String> result = new HashSet<String>();
    while ( tokenizer.hasMoreTokens() ) {
      result.add( tokenizer.nextToken() );
    }
    return result;
  }

  private Response processErrorResponse( String errMessage ) {
    return Response.ok( errMessage ).build();
  }

  private boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
        && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }

}
