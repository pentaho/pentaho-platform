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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.services.UserRoleListService;
import org.pentaho.platform.web.http.api.resources.services.UserRoleListService.UnauthorizedException;

/**
 * UserRoleList resource manage platform's implementation <code> IUserRoleListService </code>
 */
@Path ( "/userrolelist/" )
public class UserRoleListResource extends AbstractJaxRSResource {

  private ArrayList<String> systemRoles;
  protected String adminRole;
  private String anonymousRole;
  private ArrayList<String> extraRoles;

  protected static UserRoleListService userRoleListService;

  public UserRoleListResource() {
    this( PentahoSystem.get( ArrayList.class, "singleTenantSystemAuthorities", PentahoSessionHolder.getSession() ),
        PentahoSystem.get( String.class, "singleTenantAdminAuthorityName", PentahoSessionHolder.getSession() ),
        PentahoSystem.get( String.class, "singleTenantAnonymousAuthorityName", PentahoSessionHolder.getSession() ),
        PentahoSystem.get( ArrayList.class, "extraSystemAuthorities", PentahoSessionHolder.getSession() ) );
  }

  public UserRoleListResource( final ArrayList<String> systemRoles, final String adminRole,
                               final ArrayList<String> extraRoles ) {
    this( systemRoles, adminRole, PentahoSystem.get( String.class, "singleTenantAnonymousAuthorityName",
        PentahoSessionHolder.getSession() ), extraRoles );
  }

  public UserRoleListResource( final ArrayList<String> systemRoles, final String adminRole, final String anonymousRole,
                               final ArrayList<String> extraRoles ) {
    this.systemRoles = systemRoles;
    this.adminRole = adminRole;
    this.anonymousRole = anonymousRole;
    this.extraRoles = extraRoles;

    userRoleListService = new UserRoleListService();
    userRoleListService.setExtraRoles( extraRoles );
    userRoleListService.setSystemRoles( systemRoles );
  }

  /**
   * Returns the list of permission users in the platform
   * <p/>
   * <p><b>Example Request:</b></p>
   * <pre function="syntax.xml">
   * GET api/userrolelist/permission-users
   * </pre>
   *
   * @return A list of User objects for users with permissions in the platform
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;userList&gt;
   * &lt;users&gt;user1&lt;/users&gt;
   * &lt;users&gt;user2&lt;/users&gt;
   * &lt;users&gt;user3&lt;/users&gt;
   * &lt;/userList&gt;
   * </pre>
   * @throws Exception
   */
  @GET
  @Path ( "/permission-users" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the list of User objects." ),
      @ResponseCode ( code = 500, condition = "Unable to retrieve User objects." )
  } )
  public UserListWrapper getPermissionUsers() throws Exception {
    return getUsers();
  }

  /**
   * Return a list of the permission roles in the platform.
   * <p/>
   * <p><b>Example Request:</b></p>
   * <pre function="syntax.xml">
   * GET api/userrolelist/permission-roles
   * </pre>
   *
   * @return A list of permission roles in the platform.
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;roleList&gt;
   * &lt;roles&gt;Anonymous&gt;/roles&gt;
   * &lt;roles&gt;Business Analyst&gt;/roles&gt;
   * &lt;roles&gt;Authenticated&gt;/roles&gt;
   * &lt;roles&gt;Report Author&gt;/roles&gt;
   * &lt;roles&gt;Power User&gt;/roles&gt;
   * &lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path ( "/permission-roles" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the list of permission roles." )
  } )
  public RoleListWrapper getPermissionRoles() throws Exception {
    return userRoleListService.getPermissionRoles( adminRole );
  }

  /**
   * Returns the list of users in the platform
   * <p/>
   * <p><b>Example Request:</b></p>
   * <pre function="syntax.xml">
   * GET api/userrolelist/users
   * </pre>
   *
   * @return A list of User objects for users in the platform
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;userList&gt;
   * &lt;users&gt;user1&lt;/users&gt;
   * &lt;users&gt;user2&lt;/users&gt;
   * &lt;users&gt;user3&lt;/users&gt;
   * &lt;/userList&gt;
   * </pre>
   * @throws Exception
   */
  @GET
  @Path ( "/users" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the list of User objects." ),
      @ResponseCode ( code = 500, condition = "Unable to retrieve User objects." )
  } )
  public UserListWrapper getUsers() throws Exception {
    return userRoleListService.getUsers();
  }

  /**
   * Get a list of the standard roles in the platform
   * <p/>
   * <p><b>Example Request:</b></p>
   * <pre function="syntax.xml">
   * GET api/userrolelist/roles
   * </pre>
   *
   * @return A list of Role objects that contains the standard roles in the platform
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;roleList&gt;
   * &lt;roles&gt;Anonymous&gt;/roles&gt;
   * &lt;roles&gt;Business Analyst&gt;/roles&gt;
   * &lt;roles&gt;Authenticated&gt;/roles&gt;
   * &lt;roles&gt;Report Author&gt;/roles&gt;
   * &lt;roles&gt;Power User&gt;/roles&gt;
   * &lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path ( "/roles" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the list of Role objects." ),
  } )
  public RoleListWrapper getRoles() {
    return userRoleListService.getRoles();
  }

  /**
   * Get a list of the all roles in the platform including Anonymous and Administrator
   * <p/>
   * <p><b>Example Request:</b></p>
   * <pre function="syntax.xml">
   * GET api/userrolelist/roles
   * </pre>
   *
   * @return A list of Role objects that contains all the roles in the platform
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;roleList&gt;
   * &lt;roles&gt;Anonymous&gt;/roles&gt;
   * &lt;roles&gt;Anonymous&gt;/roles&gt;
   * &lt;roles&gt;Business Analyst&gt;/roles&gt;
   * &lt;roles&gt;Authenticated&gt;/roles&gt;
   * &lt;roles&gt;Report Author&gt;/roles&gt;
   * &lt;roles&gt;Power User&gt;/roles&gt;
   * &lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path ( "/allRoles" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the list of Role objects." ),
  } )
  public RoleListWrapper getAllRoles() throws Exception {
    return userRoleListService.getAllRoles();
  }

  /**
   * Returns Roles identified as "system roles" from the repository.
   * <p/>
   * <p><b>Example Request:</b><br>
   * GET api/userrolelist/systemRoles
   * </p>
   *
   * @return A list of system roles
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;roleList&gt;
   * &lt;roles&gt;Anonymous&gt;/roles&gt;
   * &lt;roles&gt;Authenticated&gt;/roles&gt;
   * &lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path ( "/systemRoles" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully returns system role list." )
  } )
  public RoleListWrapper getSystemRoles() {
    return userRoleListService.getSystemRoles();
  }

  /**
   * Returns roles identified as "extra roles" from the repository
   * <p/>
   * <p><b>Example Request:</b></p>
   * <pre function="syntax.xml">
   * GET api/userrolelist/extraRoles
   * </pre>
   *
   * @return Roles identified as "extra roles" are returned as a list from the repository as shown in the example.
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;roleList&gt;
   * &lt;roles>Authenticated&lt;/roles&gt;
   * &lt;roles>Anonymous&lt;/roles&gt;
   * &lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path ( "/extraRoles" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully returns extra role list." )
  } )
  public RoleListWrapper getExtraRoles() {
    return userRoleListService.getExtraRolesList();
  }

  /**
   * Gets the roles for the given user
   * <p/>
   * <p><b>Example Request:</b></p>
   * <pre function="syntax.xml">
   * GET api/userrolelist/getRolesForUser?user=user
   * </pre>
   *
   * @param user The username to get the roles for
   *             <pre function="syntax.xml">
   *             user
   *             </pre>
   * @return A list of Role objects containing the roles for the given user
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;roles&gt;
   * &lt;role&gt;Administrator&lt;/role&gt;
   * &lt;role&gt;Report Author&lt;/role&gt;
   * &lt;/roles&gt;
   * </pre>
   * @throws Exception
   */
  @GET
  @Path ( "/getRolesForUser" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the list of Role objects." ),
      @ResponseCode ( code = 500, condition = "Invalid user parameter." )
  } )
  public Response getRolesForUser( @QueryParam ( "user" ) String user ) throws Exception {
    try {
      String roles = userRoleListService.doGetRolesForUser( user );
      return buildOkResponse( roles, MediaType.APPLICATION_XML );
    } catch ( UnauthorizedException t ) {
      return buildStatusResponse( UNAUTHORIZED );
    } catch ( Throwable t ) {
      throw new WebApplicationException( t );
    }
  }

  /**
   * Gets the list of users that have the role specified by the role parameter. The list of valid roles can be obtained by calling the /roles endpoint.
   * <p/>
   * <p><b>Example Request:</b></p>
   * <pre function="syntax.xml">
   * GET api/userrolelist/getUsersInRole?role=Authenticated
   * </pre>
   *
   * @param role The role to get the users for
   *             <pre function="syntax.xml">
   *             Authenticated
   *             </pre>
   * @return A list of User objects that have the roles specified in the role parameter
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;users&gt;
   * &lt;user&gt;user1&lt;/user&gt;
   * &lt;user&gt;user2&lt;/user&gt;
   * &lt;/users&gt;
   * </pre>
   * @throws Exception
   */
  @GET
  @Path ( "/getUsersInRole" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the list of User objects." ),
      @ResponseCode ( code = 500, condition = "Missing the role parameter." )
  } )
  public Response getUsersInRole( @QueryParam ( "role" ) String role ) throws Exception {
    try {
      String roles = userRoleListService.doGetUsersInRole( role );
      return buildOkResponse( roles, MediaType.APPLICATION_XML );
    } catch ( UnauthorizedException t ) {
      return buildStatusResponse( UNAUTHORIZED );
    } catch ( Throwable t ) {
      throw new WebApplicationException( t );
    }
  }

  protected Response buildOkResponse( Object entity, String type ) {
    return Response.ok( entity ).type( type ).build();
  }

  protected Response buildStatusResponse( Response.Status status ) {
    return Response.status( status ).build();
  }
}
