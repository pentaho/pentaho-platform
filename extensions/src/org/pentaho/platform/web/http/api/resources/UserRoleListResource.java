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
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.jmeter.annotation.JMeterTest;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.services.UserRoleListService;
import org.pentaho.platform.web.http.api.resources.services.UserRoleListService.UnauthorizedException;

/**
 * UserRoleList resource manage platform's implementation <code> IUserRoleListService </code>
 * 
 * 
 */
@Path( "/userrolelist/" )
public class UserRoleListResource extends AbstractJaxRSResource {

  private ArrayList<String> systemRoles;
  private String adminRole;
  private String anonymousRole;
  private ArrayList<String> extraRoles;
  
  private static UserRoleListService userRoleListService;

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
  }
  
  /**
   * Returns the list of users in the platform
   * @return <code>
   * &lt;userList&gt; 
   * &lt;users&gt;
   * &lsqb;admin, joe, suzy&rsqb;
   * &lt;/users&gt;
   * &lt;/userList&gt; 
   * </code>
   * @throws Exception 
   */
  @GET
  @Path( "/permission-users" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @JMeterTest( url = "/userrolelist/permission-users", requestType = "GET", statusCode = "200" )
  public UserListWrapper getPermissionUsers() throws Exception {
    return getUsers();
  }

  /**
   * Returns the list of roles in the platform.
   * 
   * @return list of roles
   * 
   * @throws Exception
   */
  @GET
  @Path( "/permission-roles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RoleListWrapper getPermissionRoles() throws Exception {
    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    List<String> allRoles = userRoleListService.getAllRoles();
    // We will not allow user to update permission for Administrator
    if ( allRoles.contains( adminRole ) ) {
      allRoles.remove( adminRole );
    }

    // Add extra roles to the list of roles
    for ( String extraRole : extraRoles ) {
      if ( !allRoles.contains( extraRole ) ) {
        allRoles.add( extraRole );
      }
    }

    return new RoleListWrapper( allRoles );
  }
 
  /**
   * Returns the list of users in the platform
   * @return <code>
   * &lt;userList&gt; 
   * &lt;users&gt;
   * &lsqb;admin, joe, suzy&rsqb;
   * &lt;/users&gt;
   * &lt;/userList&gt; 
   * </code>
   * @throws Exception 
   */
  @GET
  @Path( "/users" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @JMeterTest( url = "/userrolelist/users", requestType = "GET", statusCode = "200" )
  public UserListWrapper getUsers() throws Exception {
    return userRoleListService.getUsers();
  }

    /**
     * Save or update a User in the system.
     *
     * Create or update a system user with the values from the supplied XML document. Existing user
     * records will be overwritten if the overwrite flag is true. If a user was overwritten,
     * <code>true</code> will be returned in the Response object.
     *
     * <p>Example Request:</p>
     * <pre function="syntax.xml">
     * GET api/userrolelist/roles
     * </pre>
     *
     * @return Response   Response wraps a boolean with true indicating that the save overwrote a
     *                    previous record
     *
     * <p>Example Response:</p>
     * <pre function="syntax.xml">
     * HTTP/1.1 200 OK
     *
     * &lt;roleList&gt;
     *  &lt;roles&gt;Anonymous&gt;/roles&gt;
     *  &lt;roles&gt;Business Analyst&gt;/roles&gt;
     *  &lt;roles&gt;Authenticated&gt;/roles&gt;
     *  &lt;roles&gt;Report Author&gt;/roles&gt;
     *  &lt;roles&gt;Power User&gt;/roles&gt;
     * &lt;/roleList&gt;
     * </pre>
     */
  @GET
  @Path( "/roles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @JMeterTest( url = "/userrolelist/roles", requestType = "GET",  statusCode = "200")
  public RoleListWrapper getRoles() {
    return userRoleListService.getRoles();
  }

  /**
   * Returns all role in the platform. This include extra roles which are (Anonymous and Authenticated)
   *
   * @return list of roles
   *
   * @throws Exception
   */
  @GET
  @Path( "/allRoles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RoleListWrapper getAllRoles() throws Exception {
    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    List<String> roles = userRoleListService.getAllRoles();
    roles.addAll( extraRoles );
    return new RoleListWrapper( roles );
  }

  /**
   * Returns roles identified as "system roles" from the repository
   * 
   * 
   * @return system roles
   * 
   * @throws Exception
   */
  @GET
  @Path( "/systemRoles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RoleListWrapper getSystemRoles() throws Exception {
    return new RoleListWrapper( systemRoles );
  }

  /**
   * Returns roles identified as "extra roles" from the repository
   * 
   * @return extra roles
   * 
   * @throws Exception
   */
  @GET
  @Path( "/extraRoles" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RoleListWrapper getExtraRoles() throws Exception {
    return new RoleListWrapper( extraRoles );
  }
  
  /**
   * Returns roles for a given user
   *
   * @param user The user to get the roles for
   * <pre function="syntax.xml">
   *  joe
   * </pre>
   * @return <code>
   * &lt;roles&gt;
   * &lt;role&gt;Administrator&lt;/role&gt;                 
   * &lt;role&gt;Report Author&lt;/role&gt;
   * &lt;/roles&gt;
   * </code>
   * @throws Exception 
   */
  @GET
  @Path( "/getRolesForUser" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @JMeterTest( url = "/userrolelist/getRolesForUser", requestType = "GET", statusCode = "200" )
  public Response getRolesForUser( @QueryParam( "user" ) String user ) throws Exception {
    try {
        String roles = userRoleListService.doGetRolesForUser( user );
        return Response.ok( roles ).type( MediaType.APPLICATION_XML )
            .build();
      } catch ( UnauthorizedException t ) {
        return Response.status( UNAUTHORIZED ).build();
      } catch ( Throwable t ) {
        throw new WebApplicationException( t );
      }  
  }

  /**
   * Returns roles for a given user
   *
   * @param role The role to get the users for
   * <pre function="syntax.xml">
   *  Administrator
   * </pre>
   * @return <code>
   * &lt;users&gt;
   * &lt;user&gt;pat&lt;/user&gt;
   * &lt;user&gt;suzy&lt;/user&gt;                   
   * &lt;/users&gt;
   * </code>
   * @throws Exception 
   */
  @GET
  @Path( "/getUsersInRole" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @JMeterTest( url = "/userrolelist/getUsersInRole", requestType = "GET", statusCode = "200" )
  public Response getUsersInRole( @QueryParam( "role" ) String role ) throws Exception {
    try {
      String roles = userRoleListService.doGetUsersInRole( role );
      return Response.ok( roles ).type( MediaType.APPLICATION_XML )
          .build();
    } catch ( UnauthorizedException t ) {
      return Response.status( UNAUTHORIZED ).build();
    } catch ( Throwable t ) {
      throw new WebApplicationException( t );
    }  
  }
}
