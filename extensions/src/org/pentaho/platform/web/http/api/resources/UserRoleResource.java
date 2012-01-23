package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;

@Path("/userrole/")
public class UserRoleResource extends AbstractJaxRSResource {

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(IRoleAuthorizationPolicyRoleBindingDao.class);
  
  @GET
  @Path("/users")
  @Produces({MediaType.APPLICATION_XML})
  public Response getUsers()  throws Exception {
      try {
          return Response.ok(SystemResourceUtil.getUsers().asXML()).type(MediaType.APPLICATION_XML).build();
      } catch (Throwable t) {
         throw new WebApplicationException(t);
      }

  }
  
  @GET
  @Path("/roles")
  @Produces({MediaType.APPLICATION_JSON})
  public RoleListWrapper getRoles() throws Exception {
    IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
    return new RoleListWrapper(userRoleListService.getAllRoles());
  }

  @GET
  @Path("/logicalRoleMap")
  @Produces({MediaType.APPLICATION_JSON})
  public SystemRolesMap getRoleBindingStruct(@QueryParam("locale") String locale) {
    RoleBindingStruct roleBindingStruct = roleBindingDao.getRoleBindingStruct(locale);
    SystemRolesMap systemRolesMap = new SystemRolesMap();
    for (Map.Entry<String, String> localalizeNameEntry : roleBindingStruct.logicalRoleNameMap.entrySet()) {
      systemRolesMap.getLocalizedRoleNames().add(new LocalizedLogicalRoleName(localalizeNameEntry.getKey(), localalizeNameEntry.getValue()));
    }   
    for (Map.Entry<String, List<String>> logicalRoleAssignments : roleBindingStruct.bindingMap.entrySet()) {
      systemRolesMap.getLogicalRoleAssignments().add(new LogicalRoleAssignment(logicalRoleAssignments.getKey(), logicalRoleAssignments.getValue()));
    }   
    return systemRolesMap;
  }

  @PUT
  @Consumes( { APPLICATION_XML, APPLICATION_JSON })
  @Path("/roleAssignments")
  public Response setLogicalRoles(LogicalRoleAssignments roleAssignments) {
    int x = 1;
    for (LogicalRoleAssignment roleAssignment : roleAssignments.getLogicalRoleAssignments()) {
      roleBindingDao.setRoleBindings(roleAssignment.getRoleName(), roleAssignment.getLogicalRoles());
    }
    return Response.ok().build();
  }
}
