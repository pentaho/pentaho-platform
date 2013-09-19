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

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

@Path("/userrolelist/")
public class UserRoleListResource extends AbstractJaxRSResource  {

  private ArrayList<String> systemRoles;
  private String adminRole;
  private ArrayList<String> extraRoles;

  public UserRoleListResource() {
    this(PentahoSystem.get(ArrayList.class, "singleTenantSystemAuthorities", PentahoSessionHolder.getSession()),
        PentahoSystem.get(String.class, "singleTenantAdminAuthorityName", PentahoSessionHolder.getSession()),
        PentahoSystem.get(ArrayList.class, "extraSystemAuthorities", PentahoSessionHolder.getSession()));
  }

  public UserRoleListResource(final ArrayList<String> systemRoles, final String adminRole,
      final ArrayList<String> extraRoles) {

    this.systemRoles = systemRoles;
    this.adminRole = adminRole;
    this.extraRoles = extraRoles;
  }
    
  @GET
  @Path("/permission-users")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public UserListWrapper getPermissionUsers() throws Exception {
    return getUsers();
  }

  @GET
  @Path("/permission-roles")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RoleListWrapper getPermissionRoles() throws Exception {
    IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
    List<String> allRoles = userRoleListService.getAllRoles();
    // We will not allow user to update permission for Administrator
    if(allRoles.contains(adminRole)) {
      allRoles.remove(adminRole);
    }
    // Add extra roles to the list of roles
    for(String extraRole:extraRoles) {
    	if(!allRoles.contains(extraRole)) {
    		allRoles.add(extraRole);
    	}
    }
    return new RoleListWrapper(allRoles);
  }
  
  
  @GET
  @Path("/users")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public UserListWrapper getUsers() throws Exception {
    IUserRoleListService service = PentahoSystem.get(IUserRoleListService.class);
    return new UserListWrapper(service.getAllUsers());
  }

  @GET
  @Path("/roles")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RoleListWrapper getRoles() throws Exception {
    IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
    return new RoleListWrapper(userRoleListService.getAllRoles());
  }

  @GET
  @Path("/allRoles")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RoleListWrapper getAllRoles() throws Exception {
    IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
    List<String> roles = userRoleListService.getAllRoles();
    roles.addAll(extraRoles);
    return new RoleListWrapper(roles);
  }
  
  @GET
  @Path("/systemRoles")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RoleListWrapper getSystemRoles() throws Exception {
    return new RoleListWrapper(systemRoles);
  }
  
  @GET
  @Path("/extraRoles")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RoleListWrapper getExtraRoles() throws Exception {
    return new RoleListWrapper(extraRoles);
  }

  @GET
  @Path("/getRolesForUser")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public Response getRolesForUser(@QueryParam("user") String user) throws Exception {
    if(canAdminister()) {
      try {
        return Response.ok(SystemResourceUtil.getRolesForUser(user).asXML()).type(MediaType.APPLICATION_XML).build();
      } catch (Throwable t) {
        throw new WebApplicationException(t);
      }
    } else {
      return Response.status(UNAUTHORIZED).build();
    }
  }
  


  @GET
  @Path("/getUsersInRole")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public Response getUsersInRole(@QueryParam("role") String role) throws Exception {
    if(canAdminister()) {
      try {
        return Response.ok(SystemResourceUtil.getUsersInRole(role).asXML()).type(MediaType.APPLICATION_XML).build();
      } catch (Throwable t) {
        throw new WebApplicationException(t);
      }
    } else {
      return Response.status(UNAUTHORIZED).build();
    }
  }
  
  private boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem
        .get(IAuthorizationPolicy.class);
    return policy
        .isAllowed(RepositoryReadAction.NAME) && policy.isAllowed(RepositoryCreateAction.NAME)
        && (policy.isAllowed(AdministerSecurityAction.NAME));
  }
}
