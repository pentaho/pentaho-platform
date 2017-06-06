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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;


import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.DefaultRoleComparator;
import org.pentaho.platform.engine.security.DefaultUsernameComparator;
import org.pentaho.platform.web.http.api.resources.services.UserRoleListService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * The UserRoleListResource service lists roles, permissions, and users. Provides a list of users per role and roles per
 * user.
 */
@Path( "/userrolelist/" )
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
    userRoleListService.setRoleComparator( new DefaultRoleComparator() );
    userRoleListService.setUserComparator( new DefaultUsernameComparator() );
  }

  /**
   * Returns the list of permission users in the platform.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/userrolelist/permission-users
   * </p>
   *
   * @return A list of User objects for users with permissions in the platform.
   *
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;userList&gt;&lt;users&gt;pat&lt;/users&gt;&lt;users&gt;admin&lt;/users&gt;&lt;users&gt;suzy&lt;/users&gt;&lt;users&gt;tiffany&lt;/users&gt;&lt;users&gt;enco*de:te^s_t$&lt;/users&gt;&lt;/userList&gt;
   * </pre>
   */
  @GET
  @Path( "/permission-users" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully retrieved the list of User objects." ),
    @ResponseCode( code = 500, condition = "Unable to retrieve User objects." ) } )
  public UserListWrapper getPermissionUsers() throws Exception {
    return getUsers();
  }

  /**
   * Return a list of the permission roles in the platform.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/userrolelist/permission-roles
   * </p>
   *
   * @return A list of permission roles in the platform.
   *
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;roleList&gt;&lt;roles&gt;Anonymous&lt;/roles&gt;&lt;roles&gt;Business Analyst&lt;/roles&gt;&lt;roles&gt;Authenticated&lt;/roles&gt;&lt;roles&gt;Report Author&lt;/roles&gt;&lt;roles&gt;Power User&lt;/roles&gt;&lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path( "/permission-roles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully retrieved the list of permission roles." ) } )
  public RoleListWrapper getPermissionRoles() throws Exception {
    return userRoleListService.getPermissionRoles( adminRole );
  }

  /**
   * Returns the list of users in the platform.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/userrolelist/users
   * </p>
   *
   * @return A list of User objects for users in the platform.
   *
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;userList&gt;&lt;users&gt;pat&lt;/users&gt;&lt;users&gt;admin&lt;/users&gt;&lt;users&gt;suzy&lt;/users&gt;&lt;users&gt;tiffany&lt;/users&gt;&lt;users&gt;enco*de:te^s_t$&lt;/users&gt;&lt;/userList&gt;
   * </pre>
   */
  @GET
  @Path( "/users" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully retrieved the list of User objects." ),
    @ResponseCode( code = 500, condition = "Unable to retrieve User objects." ) } )
  public UserListWrapper getUsers() throws Exception {
    return userRoleListService.getUsers();
  }

  /**
   * Get a list of the standard roles in the platform.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/userrolelist/roles
   * </p>
   *
   * @return A list of Role objects that contains the standard roles in the platform.
   *
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;roleList&gt;&lt;roles&gt;Anonymous&lt;/roles&gt;&lt;roles&gt;Business Analyst&lt;/roles&gt;&lt;roles&gt;Authenticated&lt;/roles&gt;&lt;roles&gt;Report Author&lt;/roles&gt;&lt;roles&gt;Power User&lt;/roles&gt;&lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path( "/roles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully retrieved the list of Role objects." ), } )
  public RoleListWrapper getRoles( @QueryParam( "addExtraRoles" ) String addExtraRoles ) {
    boolean includeExtraRoles = true;

    if ( addExtraRoles != null && addExtraRoles.length() > 0 ) {
      includeExtraRoles = Boolean.parseBoolean( addExtraRoles );
    }
    return userRoleListService.getRoles( includeExtraRoles );
  }

  /**
   * Get a list of the all roles in the platform including Anonymous and Administrator.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/userrolelist/allRoles
   * </p>
   *
   * @return A list of Role objects that contains all the roles in the platform.
   *
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;roleList&gt;&lt;roles&gt;Anonymous&lt;/roles&gt;&lt;roles&gt;Business Analyst&lt;/roles&gt;&lt;roles&gt;Authenticated&lt;/roles&gt;&lt;roles&gt;Report Author&lt;/roles&gt;&lt;roles&gt;Power User&lt;/roles&gt;&lt;roles&gt;Authenticated&lt;/roles&gt;&lt;roles&gt;Anonymous&lt;/roles&gt;&lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path( "/allRoles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully retrieved the list of Role objects." ), } )
  public RoleListWrapper getAllRoles() throws Exception {
    return userRoleListService.getAllRoles();
  }

  /**
   * Returns Roles identified as "system roles" from the repository.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/userrolelist/systemRoles
   * </p>
   *
   * @return A list of system roles.
   *
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;roleList&gt;&lt;roles&gt;Authenticated&lt;/roles&gt;&lt;roles&gt;Administrator&lt;/roles&gt;&lt;roles&gt;Anonymous&lt;/roles&gt;&lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path( "/systemRoles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully returns system role list." ) } )
  public RoleListWrapper getSystemRoles() {
    return userRoleListService.getSystemRoles();
  }

  /**
   * Returns roles identified as "extra roles" from the repository.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/userrolelist/extraRoles
   * </p>
   *
   * @return Roles identified as "extra roles" are returned as a list from the repository as shown in the example.
   *
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;&lt;roleList&gt;&lt;roles&gt;Authenticated&lt;/roles&gt;&lt;roles&gt;Anonymous&lt;/roles&gt;&lt;/roleList&gt;
   * </pre>
   */
  @GET
  @Path( "/extraRoles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully returns extra role list." ) } )
  public RoleListWrapper getExtraRoles() {
    return userRoleListService.getExtraRolesList();
  }

  /**
   * Gets the roles for the given user.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/userrolelist/getRolesForUser?user=admin
   * </p>
   *
   * @param user
   *          The username to get the roles for
   *
   * @return A list of Role objects containing the roles for the given user.
   *
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;roles&gt;&lt;role&gt;Administrator&lt;/role&gt;&lt;role&gt;Authenticated&lt;/role&gt;&lt;/roles&gt;
   * </pre>
   */
  @GET
  @Path( "/getRolesForUser" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully retrieved the list of Role objects." ),
    @ResponseCode( code = 500, condition = "Invalid user parameter." ) } )
  public RolesWrapper getRolesForUser( @QueryParam( "user" ) String user ) throws Exception {
    return new RolesWrapper( userRoleListService.doGetRolesForUser( user ) );
  }

  /**
   * Gets the list of users that have the role specified by the role parameter. The list of valid roles can be obtained
   * by calling the /roles endpoint.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/userrolelist/getUsersInRole?role=Authenticated
   * </p>
   *
   * @param role
   *          The role to get the users for
   *
   * @return A list of User objects that have the roles specified in the role parameter.
   *
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;users&gt;&lt;user&gt;pat&lt;/user&gt;&lt;user&gt;admin&lt;/user&gt;&lt;user&gt;suzy&lt;/user&gt;&lt;user&gt;tiffany&lt;/user&gt;&lt;user&gt;enco*de:te^s_t$&lt;/user&gt;&lt;/users&gt;
   * </pre>
   */
  @GET
  @Path( "/getUsersInRole" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully retrieved the list of User objects." ),
    @ResponseCode( code = 500, condition = "Missing the role parameter." ) } )
  public UsersWrapper getUsersInRole( @QueryParam( "role" ) String role ) throws Exception {
    return new UsersWrapper( userRoleListService.doGetUsersInRole( role ) );
  }

}
