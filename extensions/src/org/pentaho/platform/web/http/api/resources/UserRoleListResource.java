package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

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
    allRoles.addAll(extraRoles);
    return new RoleListWrapper(allRoles);
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

}
