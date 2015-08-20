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
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
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
import org.pentaho.platform.web.http.api.resources.services.UserRoleDaoService;

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
  private final UserRoleDaoService userRoleDaoService;
  private ArrayList<String> systemRoles;
  private String adminRole;

  private static final Log logger =
      LogFactory.getLog( UserRoleDaoResource.class );


  public UserRoleDaoResource() {
    this( PentahoSystem.get( IRoleAuthorizationPolicyRoleBindingDao.class ), PentahoSystem.get( ITenantManager.class ),
        PentahoSystem.get( ArrayList.class, "singleTenantSystemAuthorities", PentahoSessionHolder.getSession() ),
        PentahoSystem.get( String.class, "singleTenantAdminAuthorityName", PentahoSessionHolder.getSession() ), new UserRoleDaoService() );
  }

  public UserRoleDaoResource( final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
                              final ITenantManager tenantMgr, final ArrayList<String> systemRoles, final String adminRole ) {
    this( roleBindingDao, tenantMgr, systemRoles, adminRole, new UserRoleDaoService() );
  }

  public UserRoleDaoResource( final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
                              final ITenantManager tenantMgr, final ArrayList<String> systemRoles, final String adminRole, UserRoleDaoService  service ) {
    if ( roleBindingDao == null ) {
      throw new IllegalArgumentException();
    }

    this.roleBindingDao = roleBindingDao;
    this.tenantManager = tenantMgr;
    this.systemRoles = systemRoles;
    this.adminRole = adminRole;
    this.userRoleDaoService = service;
  }

  /**
   * Returns the list of users in the platform's repository.
   *
   * <p><b>Example Request:</b><br />
   * GET pentaho/api/userroledao/users
   * </p>
   *
   * @return list of users in the platform.
   *
   * <p><b>Example Response:</b>
   * <pre function="syntax.xml">
   *  <userList>
   *    <users>suzy</users>
   *    <users>pat</users>
   *    <users>tiffany</users>
   *    <users>admin</users>
   *  </userList>
   * </pre>
   */
  @GET
  @Path ( "/users" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( {
      @ResponseCode ( code = 200, condition = "Successfully returned the list of users" ),
      @ResponseCode ( code = 500, condition = "An error occurred in the platform while trying to access the list of users." )
  } )
  public UserListWrapper getUsers() throws WebApplicationException {
    try {
      return userRoleDaoService.getUsers();
    } catch ( Exception e ) {
      logger.warn( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Gets the roles for the given user.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/userroledao/userRoles?userName=suzy
   * </p>
   *
   * @param user The username to get the roles for
   *
   * @return A list containing the roles for the given user.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;roleList&gt;&lt;roles&gt;Power User&lt;/roles&gt;&lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path ( "/userRoles" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully retrieved the list of Role objects." ),
    @ResponseCode ( code = 500, condition = "Invalid user parameter." )
  } )
  public RoleListWrapper getRolesForUser( @QueryParam( "userName" ) String user ) throws Exception {
    try {
      return userRoleDaoService.getRolesForUser( user );
    } catch ( UncategorizedUserRoleDaoException t ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Returns the list of roles in the platform's repository
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/userroledao/roles
   * </p>
   *
   * @return List of roles in the platform.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *   &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;roleList&gt;&lt;roles&gt;Administrator&lt;/roles&gt;&lt;roles&gt;Power User&lt;/roles&gt;&lt;roles&gt;Report Author&lt;/roles&gt;&lt;roles&gt;Business Analyst&lt;/roles&gt;&lt;/roleList&gt;&gt;
   * </pre>
   */
  @GET
  @Path( "/roles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully retrieved the list of Roles." ),
    @ResponseCode ( code = 500, condition = "The system was not able to return the list of roles." )
  } )
  public RoleListWrapper getRoles() throws Exception {
    try {
      return userRoleDaoService.getRoles();
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Retrieves list of users for the selected role. The role must be a valid role in the system. Must have administrative privledges to access this.
   *
   * <p><b>Example Request:</b><br />
   *   GET pentaho/api/userroledao/roleMembers?roleName=Power%20User
   * </p>
   *
   * @param roleName The role name to get the list of users associated with it.
   *
   * @return list of users for the selected role
   *
   *<p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *   &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;userList&gt;&lt;users&gt;suzy&lt;/users&gt;&lt;users&gt;admin&lt;/users&gt;&lt;/userList&gt;
   * </pre>
   */
  @GET
  @Path ( "/roleMembers" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully retrieved the list of Users." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method" ),
    @ResponseCode ( code = 500, condition = "The system was not able to return the list of users." )
  } )
  public UserListWrapper getRoleMembers( @QueryParam ( "roleName" ) String roleName ) throws Exception {
    try {
      return userRoleDaoService.getRoleMembers( roleName );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }
  }

  /**
   * Appends existing roles to an existing user passed to the system through query parameters.
   * This endpoint is only available to users with administrative privileges
   * <p/>
   * <p><b>Example Request:</b><br /> PUT  pentaho/api/userroledao/assignRoleToUser?userName=admin&roleNames=power%20user%09cto%09
   * </p>
   *
   * @param userName  The username that the list of roles will be appended to
   * @param roleNames Rolenames must be associated to existing roles in a tab (\t) separated list
   * @return Response object containing the status code of the operation
   */
  @PUT
  @Path( "/assignRoleToUser" )
  @Consumes( { WILDCARD } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully appened the roles to the user." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method" ),
    @ResponseCode ( code = 500, condition = "Internal server error prevented the system from properly retrieving either the user or roles." )
  } )
  public Response assignRolesToUser( @QueryParam( "userName" ) String userName,
                                     @QueryParam( "roleNames" ) String roleNames ) {
    try {
      userRoleDaoService.assignRolesToUser( userName, roleNames );
      return Response.ok().build();
    } catch ( org.pentaho.platform.api.engine.security.userroledao.NotFoundException e ) {
      throw new WebApplicationException( Response.Status.NOT_FOUND );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }
  }

  /**
   * Removes selected roles from an existing user passed to the system through query parameters.
   * This endpoint is only available to users with administrative privileges
   *
   * <p><b>Example Request:</b><br />
   *  PUT  pentaho/api/userroledao/removeRoleFromUser?userName=admin&roleNames=Business%20User%09Power%20User%09
   * </p>
   *
   * @param userName   The username that the list of roles will be removed from
   * @param roleNames  Rolenames must be associated to existing roles in a tab (\t) separated list
   *
   * @return Response object containing the status code of the operation
   */
  @PUT
  @Path( "/removeRoleFromUser" )
  @Consumes( { WILDCARD } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully removed the roles from the user." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method" ),
    @ResponseCode ( code = 500, condition = "Internal server error prevented the system from properly retrieving either the user or roles." )
  } )
  public Response removeRolesFromUser( @QueryParam( "userName" ) String userName,
                                       @QueryParam( "roleNames" ) String roleNames ) {
    try {
      userRoleDaoService.removeRolesFromUser( userName, roleNames );
      return Response.ok().build();
    } catch ( org.pentaho.platform.api.engine.security.userroledao.NotFoundException e ) {
      throw new WebApplicationException( Response.Status.NOT_FOUND );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
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
  @Facet ( name = "Unsupported" )
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
  @Facet ( name = "Unsupported" )
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
  @Facet ( name = "Unsupported" )
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
        roleDao.setRoleMembers( getTenant( tenantPath ), roleName, assignedUserNames.toArray( new String[ 0 ] ) );
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
  @Facet ( name = "Unsupported" )
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
  @Facet ( name = "Unsupported" )
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
  @Facet ( name = "Unsupported" )
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
  @Facet ( name = "Unsupported" )
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
  @Facet ( name = "Unsupported" )
  public Response createRole( @QueryParam ( "tenant" ) String tenantPath, @QueryParam ( "roleName" ) String roleName ) {
    IUserRoleDao roleDao =
        PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
    roleDao.createRole( getTenant( tenantPath ), roleName, "", new String[ 0 ] );
    return Response.ok().build();
  }

  /**
   * Delete role(s) from the platform. Must have administrative privledges.
   *
   * <p><b>Example Request:</b><br />
   *  PUT  pentaho/api/userroledao/deleteRoles?roleNames=role1%09
   * </p>
   *
   * @param roleNames List of tab (\t) separated role names, must be valid roles.
   * @return Response containing the result of the operation.
   */
  @PUT
  @Path( "/deleteRoles" )
  @Consumes( { WILDCARD } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully deleted the list of roles." ),
    @ResponseCode( code = 403, condition = "Only users with administrative privileges can access this method" ),
    @ResponseCode( code = 500, condition = "The system was unable to delete the roles passed in." )
  } )
  public Response deleteRoles( @QueryParam( "roleNames" ) String roleNames ) {
    try {
      userRoleDaoService.deleteRoles( roleNames );
      return Response.ok().build();
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Delete user(s) from the platform using a query parameter that takes a list of tab separated user names.
   *
   *<p><b>Example Request:</b><br />
   *  DELETE  pentaho/api/userroledao/deleteUsers?userNames=user1%09user2%09
   * </p>
   *
   * @param userNames (list of tab (\t) separated user names)
   *
   * @return Response object containing the status code of the operation
   */
  @PUT
  @Path ( "/deleteUsers" )
  @Consumes ( { WILDCARD } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully deleted the list of users." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method" ),
    @ResponseCode ( code = 500, condition = "Internal server error prevented the system from properly retrieving either the user or roles." )
  } )
  public Response deleteUsers( @QueryParam( "userNames" ) String userNames ) {
    try {
      userRoleDaoService.deleteUsers( userNames );
      return Response.ok().build();
    } catch ( org.pentaho.platform.api.engine.security.userroledao.NotFoundException e ) {
      throw new WebApplicationException( Response.Status.NOT_FOUND );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
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
  @Facet ( name = "Unsupported" )
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
  @Facet ( name = "Unsupported" )
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
  @Facet ( name = "Unsupported" )
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
