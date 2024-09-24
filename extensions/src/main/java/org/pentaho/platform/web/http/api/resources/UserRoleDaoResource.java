/*!
 *
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
 *
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gwt.user.server.Base64Utils;
import org.apache.commons.lang.StringUtils;
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
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.services.UserRoleDaoService;
import org.pentaho.platform.web.http.api.resources.services.UserRoleDaoService.ValidationFailedException;

import com.sun.jersey.api.NotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * UserRoleDao manages Pentaho Security user and roles in the BA platform.
 */
@Path ( "/userroledao/" )
public class UserRoleDaoResource extends AbstractJaxRSResource {

  private static final String PUC_VALIDATION_ERROR_MESSAGE = "PUC_VALIDATION_ERROR_MESSAGE";
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
   * Creates a new user with the specified name and password. This request is encapsulated inside a user object that has userName and password values.
   * The user is created without any assigned roles, roles must be assigned separately. This endpoint is only accessible to an administrative user.
   *
   * <p>
   * <b>Example Request:</b><br />
   * PUT pentaho/api/userroledao/createUser
   * <pre function="syntax.xml">
   * <user>
   *   <userName>Luke</userName>
   *   <password>password</password>
   * </user>
   * </pre>
   * </p>
   *
   * @param user
   *          A user is an object the system uses to pass along a userName and password in the format:
   *          <pre function="syntax.xml">
   *          <user>
   *            <userName>Joe</userName>
   *            <password>password</password>
   *          </user>
   *          </pre>
   *
   * @return Response object containing the status code of the operation
   */
  @PUT
  @Path( "/createUser" )
  @Consumes( { MediaType.WILDCARD } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully created new user." ),
    @ResponseCode( code = 400, condition = "Provided data has invalid format." ),
    @ResponseCode( code = 403, condition = "Only users with administrative privileges can access this method." ),
    @ResponseCode( code = 412, condition = "Unable to create user." )
    } )
  public Response createUser( User user ) {
    try {
      user.setPassword( b64DecodePassword( user.getPassword() ) );
      userRoleDaoService.createUser( user );
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    } catch ( UserRoleDaoService.ValidationFailedException e ) {
      Response.ResponseBuilder response = Response.status( Response.Status.BAD_REQUEST );
      response.header( PUC_VALIDATION_ERROR_MESSAGE, e.getMessage() );
      throw new WebApplicationException( response.build() );
    } catch ( Exception e ) {
      // TODO: INTERNAL_SERVER_ERROR(500) returns (FORBIDDEN)403 error instead for unknown reason. To avoid it use
      // PRECONDITION_FAILED
      throw new WebApplicationException( Response.Status.PRECONDITION_FAILED );
    }
    return Response.noContent().build();
  }

  /**
   * Delete user(s) from the platform using a query parameter that takes a list of tab separated user names. This endpoint is only available to users with administrative privledges.
   *
   *<p><b>Example Request:</b><br />
   *  PUT pentaho/api/userroledao/deleteUsers?userNames=user1%09user2%09
   * </p>
   *
   * @param userNames (List of tab (\t) separated user names)
   *
   * @return Response object containing the status code of the operation
   */
  @PUT
  @Path ( "/deleteUsers" )
  @Consumes ( { MediaType.WILDCARD } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully deleted the list of users." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method." ),
    @ResponseCode ( code = 500, condition = "Internal server error prevented the system from properly retrieving either the user or roles." )
    } )
  public Response deleteUsers( @QueryParam( "userNames" ) String userNames ) {
    try {
      userRoleDaoService.deleteUsers( userNames );
      return Response.noContent().build();
    } catch ( org.pentaho.platform.api.engine.security.userroledao.NotFoundException e ) {
      throw new WebApplicationException( Response.Status.NOT_FOUND );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }
  }

  /**
   * Allows a user to change their password. The information is encapsulated in a ChangeUserPassword object that contains these fields: userName, newPassword, oldPassword.
   *
   * <p>
   * <b>Example Request:</b><br />
   * PUT pentaho/api/userroledao/user
   * <pre function="syntax.xml">
   * <ChangePasswordUser>
   *   <userName>Luke</userName>
   *   <newPassword>newPassword</newPassword>
   *   <oldPassword>oldPassword</oldPassword>
   * </ChangePasswordUser>
   * </pre>
   * </p>
   *
   * @param user Encapsulates the fields required for a user to update their password. The object requires the name of the user whose password is being changed, the old password, and the new password.
   *                           A ChangePasswordUser object can be constructed as follows:
   * <<pre function="syntax.xml">>
   * <ChangePasswordUser>
   *   <userName>Luke</userName>
   *   <newPassword>newPassword</newPassword>
   *   <oldPassword>oldPassword</oldPassword>
   * </ChangePasswordUser>
   * </pre>
   *
   * @return Response object containing the status code of the operation
   */
  @PUT
  @Path( "/user" )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully changed password." ),
    @ResponseCode( code = 400, condition = "Provided data has invalid format." ),
    @ResponseCode( code = 403, condition = "Provided user name or password is incorrect." ),
    @ResponseCode( code = 412, condition = "An error occurred in the platform." )
    } )
  public Response changeUserPassword( ChangePasswordUser user ) {
    try {
      user.setNewPassword( b64DecodePassword( user.getNewPassword() ) );
      user.setOldPassword( b64DecodePassword( user.getOldPassword() ) );
      userRoleDaoService.changeUserPassword( user.getUserName(), user.getNewPassword(), user.getOldPassword() );
    } catch ( ValidationFailedException e ) {
      Response.ResponseBuilder response = Response.status( Response.Status.BAD_REQUEST );
      response.header( PUC_VALIDATION_ERROR_MESSAGE, e.getMessage() );
      throw new WebApplicationException( response.build() );
    } catch ( SecurityException e ) {
      Response.ResponseBuilder response = Response.status( Response.Status.FORBIDDEN );
      response.header( PUC_VALIDATION_ERROR_MESSAGE, e.getMessage() );
      throw new WebApplicationException( response.build() );
    } catch ( Exception e ) {
      // TODO: INTERNAL_SERVER_ERROR(500) returns (FORBIDDEN)403 error instead for unknown reason. I used
      // PRECONDITION_FAILED
      throw new WebApplicationException( Response.Status.PRECONDITION_FAILED );
    }
    return Response.noContent().build();
  }

  private String b64DecodePassword( String encodedPassword ) throws UnsupportedEncodingException {
    if ( !StringUtils.isEmpty( encodedPassword ) && encodedPassword.startsWith( "ENC:" ) ) {
      String password = new String( Base64Utils.fromBase64( encodedPassword.substring( 4 ) ), StandardCharsets.UTF_8 );
      return URLDecoder.decode( password, StandardCharsets.UTF_8.name() );
    } else {
      return encodedPassword;
    }
  }

  /**
   * Returns the list of users in the platform's repository.
   *
   * <p><b>Example Request:</b><br />
   * GET pentaho/api/userroledao/users
   * </p>
   *
   * @return List of users in the platform.
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
  @Produces ( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
  @StatusCodes( {
      @ResponseCode ( code = 200, condition = "Successfully returned the list of users." ),
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
   * @param user The username to get the roles for.
   *
   * @return List containing the roles for the given user.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;roleList&gt;&lt;roles&gt;Power User&lt;/roles&gt;&lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path ( "/userRoles" )
  @Produces ( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully retrieved the list of roles." ),
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
   * Appends existing roles to an existing user passed to the system through query parameters.<br />
   * If the user name exists but the role name is not valid, the call will return 200. This means that the call itself was successful and able to find the user, but added no new roles to it.
   * This prevents the call from failing in the instance of a set of other valid roles, with a single invalid role among them.
   * This endpoint is only available to users with administrative privileges.
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
  @Consumes( { MediaType.WILDCARD } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully append the roles to the user." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method." ),
    @ResponseCode ( code = 500, condition = "Internal server error prevented the system from properly retrieving either the user or roles." )
    } )
  public Response assignRolesToUser( @QueryParam( "userName" ) String userName,
                                     @QueryParam( "roleNames" ) String roleNames ) {
    try {
      userRoleDaoService.assignRolesToUser( userName, roleNames );
      if ( userName.equals( getSession().getName() ) ) {
        updateRolesForCurrentSession();
      }
      return Response.noContent().build();
    } catch ( org.pentaho.platform.api.engine.security.userroledao.NotFoundException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }
  }

  /**
   * Removes selected roles from an existing user passed to the system through query parameters.
   * This endpoint is only available to users with administrative privileges.
   *
   * <p><b>Example Request:</b><br />
   *  PUT  pentaho/api/userroledao/removeRoleFromUser?userName=admin&roleNames=Business%20User%09Power%20User%09
   * </p>
   *
   * @param userName   The username that the list of roles will be removed from.
   * @param roleNames  Rolenames must be associated to existing roles in a tab (\t) separated list.
   *
   * @return Response object containing the status code of the operation.
   */
  @PUT
  @Path( "/removeRoleFromUser" )
  @Consumes( { MediaType.WILDCARD } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully removed the roles from the user." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method." ),
    @ResponseCode ( code = 500, condition = "Internal server error prevented the system from properly retrieving either the user or roles." )
    } )
  public Response removeRolesFromUser( @QueryParam( "userName" ) String userName,
                                       @QueryParam( "roleNames" ) String roleNames ) {
    try {
      userRoleDaoService.removeRolesFromUser( userName, roleNames );
      if ( userName.equals( getSession().getName() ) ) {
        updateRolesForCurrentSession();
      }
      return Response.noContent().build();
    } catch ( org.pentaho.platform.api.engine.security.userroledao.NotFoundException e ) {
      throw new WebApplicationException( Response.status( Response.Status.NOT_FOUND ).entity( e.getLocalizedMessage() ).build() );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.getLocalizedMessage() ).build() );
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.status( Response.Status.FORBIDDEN ).entity( e.getLocalizedMessage() ).build() );
    }
  }

  /**
   * Creates a new role that that does not have any permissions assigned to it. Permissions must be assigned after creating the role.
   * This endpoint is only usable by an administrative user.
   *
   * <p>
   * <b>Example Request:</b><br />
   * PUT pentaho/api/userroledao/createRole?roleName=rName
   * </p>
   *
   * @param roleName
   *          Name of the new role to create in the system.
   * @return Response containing the result of the operation.
   */
  @PUT
  @Path( "/createRole" )
  @Consumes( { MediaType.WILDCARD } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully created new role." ),
    @ResponseCode( code = 400, condition = "Provided data has invalid format." ),
    @ResponseCode( code = 403, condition = "Only users with administrative privileges can access this method." ),
    @ResponseCode( code = 412, condition = "Unable to create role objects." )
    } )
  public Response createRole( @QueryParam( "roleName" ) String roleName ) {
    try {
      userRoleDaoService.createRole( roleName );
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    } catch ( UserRoleDaoService.ValidationFailedException e ) {
      throw new WebApplicationException( Response.Status.BAD_REQUEST );
    } catch ( Exception e ) {
      // TODO: INTERNAL_SERVER_ERROR(500) returns (FORBIDDEN)403 error instead for unknown reason. To avoid it use
      // PRECONDITION_FAILED
      throw new WebApplicationException( Response.Status.PRECONDITION_FAILED );
    }
    return Response.noContent().build();
  }

  /**
   * Delete role(s) from the platform. This endpoint is only available to users with administrative privileges.
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
  @Consumes( { MediaType.WILDCARD } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully deleted the list of roles." ),
    @ResponseCode( code = 403, condition = "Only users with administrative privileges can access this method." ),
    @ResponseCode( code = 500, condition = "The system was unable to delete the roles passed in." )
    } )
  public Response deleteRoles( @QueryParam( "roleNames" ) String roleNames ) {
    try {
      userRoleDaoService.deleteRoles( roleNames );
      updateRolesForCurrentSession();
      return Response.noContent().build();
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Returns the list of roles in the platform's repository.
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
  @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully retrieved the list of roles." ),
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
   * Retrieves list of users for the selected role. The role must be a valid role in the system. Must have administrative privileges to access this.
   *
   * <p><b>Example Request:</b><br />
   *   GET pentaho/api/userroledao/roleMembers?roleName=Power%20User
   * </p>
   *
   * @param roleName The role name to get the list of users associated with it.
   *
   * @return List of users for the selected role.
   *
   *<p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *   &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;userList&gt;&lt;users&gt;suzy&lt;/users&gt;&lt;users&gt;admin&lt;/users&gt;&lt;/userList&gt;
   * </pre>
   */
  @GET
  @Path ( "/roleMembers" )
  @Produces ( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully retrieved the list of Users." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method." ),
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
   * Associate a particular role to a list of physical permissions available in the system. Setting the physical permissions to the roles is a way to add and delete permissions from the role.
   * Any permissions the role had before that are not on this list will be deleted. Any permissions on this list that were not previously assigned will now be assigned.
   *
   *<p><b>Example Request:</b><br />
   *  PUT /pentaho/api/userroledao/roleAssignments
   *</p>
   * <pre function="syntax.xml">
   *   &lt;systemRolesMap&gt;
   *   &lt;assignments&gt;
   *   &lt;roleName&gt;Report Author&lt;/roleName&gt;
   *   &lt;logicalRoles&gt;org.pentaho.scheduler.manage&lt;/logicalRoles&gt;
   *   &lt;logicalRoles&gt;org.pentaho.repository.read&lt;/logicalRoles&gt;
   *   &lt;logicalRoles&gt;org.pentaho.security.publish&lt;/logicalRoles&gt;
   *   &lt;logicalRoles&gt;org.pentaho.repository.create&lt;/logicalRoles&gt;
   *   &lt;logicalRoles&gt;org.pentaho.repository.execute&lt;/logicalRoles&gt;
   *   &lt;/assignments&gt;
   *   &lt;/systemRolesMap&gt;
   * </pre>
   *
   * @param roleAssignments Built from the Request payload, an example of the role assignments exists in the example request.
   *
   * @return Response code determining the success of the operation.
   */
  @PUT
  @Consumes ( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
  @Path ( "/roleAssignments" )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully applied the logical role assignment." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method." )
    } )
  public Response setLogicalRoles( LogicalRoleAssignments roleAssignments ) {
    try {
      userRoleDaoService.setLogicalRoles( roleAssignments );
      return Response.noContent().build();
    } catch ( SecurityException e ) {
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }
  }

  /**
   * Retrieves the list of roles in the platform and the mapping for operation permissions, along with a list of operation permissions.
   * The logical role name mapping is determined by the locale. If the locale is empty, the system will use the default locale of "en".
   *
   * <p><b>Example Request:</b><br />
   *  GET  pentaho/api/userroledao/logicalRoleMap?locale=en
   * </p>
   *
   * @param locale The locale paramter is optional and determines the localized role name for a physical permission in the system roles map.
   * @return A role mapping for the current system. Each assignment contains the immutable flag and
   * roles for immutable assignments cannot be edited. This is useful for roles such as administrator, who should never lose the administrative privilege.
   * Logical roles in the assignment are the physical permissions currently mapped to the role. The role name is the name of the role that can be assigned to users.
   * The system roles map also includes a list of all physical permissions in the system along with their localized role name. The localized role name is based on the locale passed into the call, defaulting to "en".
   * These are the physical permissions that can be used to create roles.
   *
   *<p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *  &lt;systemRolesMap&gt;
   *  &lt;assignments&gt;
   *  &lt;immutable&gt;false&lt;/immutable&gt;
   *  &lt;logicalRoles&gt;org.pentaho.scheduler.manage&lt;/logicalRoles&gt;
   *  &lt;logicalRoles&gt;org.pentaho.security.publish&lt;/logicalRoles&gt;
   *  &lt;logicalRoles&gt;org.pentaho.repository.create&lt;/logicalRoles&gt;
   *  &lt;logicalRoles&gt;org.pentaho.repository.execute&lt;/logicalRoles&gt;
   *  &lt;roleName&gt;Power User&lt;/roleName&gt;
   *  &lt;/assignments&gt;
   *  &lt;assignments&gt;
   *  &lt;immutable&gt;true&lt;/immutable&gt;
   *  &lt;logicalRoles&gt;org.pentaho.repository.execute&lt;/logicalRoles&gt;
   *  &lt;logicalRoles&gt;
   *    org.pentaho.platform.dataaccess.datasource.security.manage
   *  &lt;/logicalRoles&gt;
   *  &lt;logicalRoles&gt;org.pentaho.repository.read&lt;/logicalRoles&gt;
   *  &lt;logicalRoles&gt;org.pentaho.repository.create&lt;/logicalRoles&gt;
   *  &lt;logicalRoles&gt;org.pentaho.scheduler.manage&lt;/logicalRoles&gt;
   *  &lt;logicalRoles&gt;org.pentaho.security.administerSecurity&lt;/logicalRoles&gt;
   *  &lt;logicalRoles&gt;org.pentaho.security.publish&lt;/logicalRoles&gt;
   *  &lt;roleName&gt;Administrator&lt;/roleName&gt;
   *  &lt;/assignments&gt;
   *  &lt;localizedRoleNames&gt;
   *  &lt;localizedName&gt;Administer Security&lt;/localizedName&gt;
   *  &lt;roleName&gt;org.pentaho.security.administerSecurity&lt;/roleName&gt;
   *  &lt;/localizedRoleNames&gt;
   *  &lt;localizedRoleNames&gt;
   *  &lt;localizedName&gt;Schedule Content&lt;/localizedName&gt;
   *  &lt;roleName&gt;org.pentaho.scheduler.manage&lt;/roleName&gt;
   *  &lt;/localizedRoleNames&gt;
   *  &lt;localizedRoleNames&gt;
   *  &lt;localizedName&gt;Read Content&lt;/localizedName&gt;
   *  &lt;roleName&gt;org.pentaho.repository.read&lt;/roleName&gt;
   *  &lt;/localizedRoleNames&gt;
   *  &lt;localizedRoleNames&gt;
   *  &lt;localizedName&gt;Publish Content&lt;/localizedName&gt;
   *  &lt;roleName&gt;org.pentaho.security.publish&lt;/roleName&gt;
   *  &lt;/localizedRoleNames&gt;
   *  &lt;localizedRoleNames&gt;
   *  &lt;localizedName&gt;Create Content&lt;/localizedName&gt;
   *  &lt;roleName&gt;org.pentaho.repository.create&lt;/roleName&gt;
   *  &lt;/localizedRoleNames&gt;
   *  &lt;localizedRoleNames&gt;
   *  &lt;localizedName&gt;Execute&lt;/localizedName&gt;
   *  &lt;roleName&gt;org.pentaho.repository.execute&lt;/roleName&gt;
   *  &lt;/localizedRoleNames&gt;
   *  &lt;localizedRoleNames&gt;
   *  &lt;localizedName&gt;Manage Data Sources&lt;/localizedName&gt;
   *  &lt;roleName&gt;
   *  org.pentaho.platform.dataaccess.datasource.security.manage
   *  &lt;/roleName&gt;
   *  &lt;/localizedRoleNames&gt;
   *  &lt;/systemRolesMap&gt;
   * </pre>
   */
  @GET
  @Path ( "/logicalRoleMap" )
  @Produces ( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
  @StatusCodes ( {
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method." )
    } )
  public SystemRolesMap getRoleBindingStruct( @QueryParam ( "locale" ) String locale ) {
    try {
      return userRoleDaoService.getRoleBindingStruct( locale );
    } catch ( Exception e ) {
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
  @Consumes ( { MediaType.WILDCARD } )
  @Facet ( name = "Unsupported" )
  public Response assignAllRolesToUser( @QueryParam ( "tenant" ) String tenantPath,
                                        @QueryParam ( "userName" ) String userName ) {
    IUserRoleDao roleDao = getUserRoleDao();
    Set<String> assignedRoles = new HashSet<String>();
    for ( IPentahoRole pentahoRole : roleDao.getRoles( getTenant( tenantPath ) ) ) {
      assignedRoles.add( pentahoRole.getName() );
    }
    roleDao.setUserRoles( getTenant( tenantPath ), userName, assignedRoles.toArray( new String[ 0 ] ) );

    if ( userName.equals( getSession().getName() ) ) {
      updateRolesForCurrentSession();
    }

    return Response.noContent().build();
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
  @Consumes ( { MediaType.WILDCARD } )
  @Facet ( name = "Unsupported" )
  public Response removeAllRolesFromUser( @QueryParam ( "tenant" ) String tenantPath,
                                          @QueryParam ( "userName" ) String userName ) {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao = getUserRoleDao();
        roleDao.setUserRoles( getTenant( tenantPath ), userName, new String[0] );

        if ( userName.equals( getSession().getName() ) ) {
          updateRolesForCurrentSession();
        }

        return Response.noContent().build();
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
    } else {
      return Response.status( Response.Status.UNAUTHORIZED ).build();
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
  @Consumes ( { MediaType.WILDCARD } )
  @Facet ( name = "Unsupported" )
  public Response assignUserToRole( @QueryParam ( "tenant" ) String tenantPath,
                                    @QueryParam ( "userNames" ) String userNames, @QueryParam ( "roleName" ) String roleName ) {
    if ( canAdminister() ) {
      IUserRoleDao roleDao = getUserRoleDao();
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

        if ( assignedUserNames.contains( getSession().getName() ) ) {
          updateRolesForCurrentSession();
        }

        return Response.noContent().build();
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
    } else {
      return Response.status( Response.Status.UNAUTHORIZED ).build();
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
  @Consumes ( { MediaType.WILDCARD } )
  @Facet ( name = "Unsupported" )
  public Response removeUserFromRole( @QueryParam ( "tenant" ) String tenantPath,
                                      @QueryParam ( "userNames" ) String userNames, @QueryParam ( "roleName" ) String roleName ) {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao = getUserRoleDao();
        StringTokenizer tokenizer = new StringTokenizer( userNames, "\t" );
        Set<String> assignedUserNames = new HashSet<String>();
        for ( IPentahoUser pentahoUser : roleDao.getRoleMembers( getTenant( tenantPath ), roleName ) ) {
          assignedUserNames.add( pentahoUser.getUsername() );
        }
        while ( tokenizer.hasMoreTokens() ) {
          assignedUserNames.remove( tokenizer.nextToken() );
        }
        roleDao.setRoleMembers( getTenant( tenantPath ), roleName, assignedUserNames.toArray( new String[ 0 ] ) );
        if ( assignedUserNames.contains( getSession().getName() ) ) {
          updateRolesForCurrentSession();
        }
        return Response.noContent().build();
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
    } else {
      return Response.status( Response.Status.UNAUTHORIZED ).build();
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
  @Consumes ( { MediaType.WILDCARD } )
  @Facet ( name = "Unsupported" )
  public Response assignAllUsersToRole( @QueryParam ( "tenant" ) String tenantPath,
                                        @QueryParam( "roleName" ) String roleName ) {
    IUserRoleDao roleDao = getUserRoleDao();
    Set<String> assignedUserNames = new HashSet<String>();
    for ( IPentahoUser pentahoUser : roleDao.getUsers( getTenant( tenantPath ) ) ) {
      assignedUserNames.add( pentahoUser.getUsername() );
    }
    roleDao.setRoleMembers( getTenant( tenantPath ), roleName, assignedUserNames.toArray( new String[ 0 ] ) );

    if ( assignedUserNames.contains( getSession().getName() ) ) {
      updateRolesForCurrentSession();
    }

    return Response.noContent().build();
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
  @Consumes ( { MediaType.WILDCARD } )
  @Facet ( name = "Unsupported" )
  public Response removeAllUsersFromRole( @QueryParam ( "tenant" ) String tenantPath,
                                          @QueryParam ( "roleName" ) String roleName ) {
    if ( canAdminister() ) {
      try {
        IUserRoleDao roleDao = getUserRoleDao();
        roleDao.setRoleMembers( getTenant( tenantPath ), roleName, new String[0] );
        updateRolesForCurrentSession();
        return Response.noContent().build();
      } catch ( Throwable th ) {
        return processErrorResponse( th.getLocalizedMessage() );
      }
    } else {
      return Response.status( Response.Status.UNAUTHORIZED ).build();
    }
  }

  /**
   * This is an administrative tool, that allows an administrator the ability to change any users password by
   * passing in the username and the new password.
   *
   * Additionally the current administrator password is needed to authorize the change.
   *
   * The fields are encapsulated in a user object containing a userName, password and administratorPassword.
   *
   * <p>
   * <b>Example Request:</b><br />
   * PUT pentaho/api/userroledao/updatePassword
   * <pre function="syntax.xml">
   * <user>
   *   <userName>Joe</userName>
   *   <password>password</password>
   *   <administratorPassword>administratorPassword</administratorPassword>
   * </user>
   * </pre>
   * </p>
   *
   * @param user
   *          A user is an object the system uses to pass along a userName, password and administratorPassword
   *          in the format:
   *          <pre function="syntax.xml">
   *          <user>
   *            <userName>Joe</userName>
   *            <password>password</password>
   *            <administratorPassword>administratorPassword</administratorPassword>
   *          </user>
   *          </pre>
   *
   * @return response object containing the status code of the operation
   */
  @PUT
  @Path( "/updatePassword" )
  @Consumes( { MediaType.WILDCARD } )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully deleted the list of users." ),
    @ResponseCode ( code = 403, condition = "Only users with administrative privileges can access this method." ),
    @ResponseCode ( code = 500, condition = "Internal server error prevented the system from properly retrieving either the user or roles." )
    } )
  public Response updatePassword( UserChangePasswordDTO user ) {
    try {
      user.setPassword( b64DecodePassword( user.getPassword() ) );
      user.setAdministratorPassword( b64DecodePassword( user.getAdministratorPassword() ) );
      userRoleDaoService.updatePassword( user, user.getAdministratorPassword() );
    } catch ( SecurityException e ) {
      Response.ResponseBuilder response = Response.status( Response.Status.FORBIDDEN );
      response.header( PUC_VALIDATION_ERROR_MESSAGE, e.getMessage() );
      throw new WebApplicationException( response.build() );
    } catch ( ValidationFailedException e ) {
      Response.ResponseBuilder response = Response.status( Response.Status.BAD_REQUEST );
      response.header( PUC_VALIDATION_ERROR_MESSAGE, e.getMessage() );
      throw new WebApplicationException( response.build() );
    } catch ( Exception e ) {
      throw new WebApplicationException( Response.Status.PRECONDITION_FAILED );
    }
    return Response.noContent().build();
  }

  protected ITenant getTenant( String tenantId ) throws NotFoundException {
    ITenant tenant = null;
    if ( tenantId != null ) {
      tenant = tenantManager.getTenant( tenantId );
      if ( tenant == null ) {
        throw new NotFoundException( "Tenant not found." );
      }
    } else {
      IPentahoSession session = getSession();
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

  protected boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
      && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }

  protected void updateRolesForCurrentSession() {
    List<String> userRoles = userRoleDaoService.getRolesForUser( getSession().getName() ).getRoles();
    List<GrantedAuthority> authorities = new ArrayList<>();
    userRoles.forEach( role -> authorities.add( new SimpleGrantedAuthority( role ) ) );

    getSession().setAttribute( IPentahoSession.SESSION_ROLES, authorities );
  }

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  /**
   * For testing
   *
   **/
  protected IUserRoleDao getUserRoleDao() {
    return PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", getSession() );
  }
}
