package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;

@Path("/userroledao/")
public class UserRoleDaoResource extends AbstractJaxRSResource {

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = null;
  private ITenantManager tenantManager = null;
  private ArrayList<String> systemRoles;
  private String adminRole;  


  public UserRoleDaoResource() {
    this(PentahoSystem.get(IRoleAuthorizationPolicyRoleBindingDao.class), PentahoSystem.get(ITenantManager.class),
        PentahoSystem.get(ArrayList.class, "singleTenantSystemAuthorities", PentahoSessionHolder.getSession()),
        PentahoSystem.get(String.class, "singleTenantAdminAuthorityName", PentahoSessionHolder.getSession()));
  }

  public UserRoleDaoResource(final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
      final ITenantManager tenantMgr, final ArrayList<String> systemRoles, final String adminRole) {
   
    if (roleBindingDao == null) {
      throw new IllegalArgumentException();
    }
    
    this.roleBindingDao = roleBindingDao;
    this.tenantManager = tenantMgr;
    this.systemRoles = systemRoles;
    this.adminRole = adminRole;

  }  
  
  @GET
  @Path("/users")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public UserListWrapper getUsers() throws Exception {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    return new UserListWrapper(roleDao.getUsers());
  }

  @GET
  @Path("/roles")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RoleListWrapper getRoles() throws Exception {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    return new RoleListWrapper(roleDao.getRoles());
  }

  @GET
  @Path("/userRoles")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public RoleListWrapper getUserRoles(@QueryParam("tenant") String tenantPath
      , @QueryParam("userName") String userName) throws Exception {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    return new RoleListWrapper(roleDao.getUserRoles(getTenant(tenantPath),userName));
  }

  @GET
  @Path("/roleMembers")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public UserListWrapper getRoleMembers(@QueryParam("tenant") String tenantPath
      , @QueryParam("roleName") String roleName) throws Exception {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    return new UserListWrapper(roleDao.getRoleMembers(getTenant(tenantPath),roleName));
  }

  
  
  @PUT
  @Path("/assignRoleToUser")
  @Consumes({ WILDCARD })
  public Response assignRoleToUser(@QueryParam("tenant") String tenantPath
      , @QueryParam("userName") String userName
      , @QueryParam("roleNames") String roleNames) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    StringTokenizer tokenizer = new StringTokenizer(roleNames, "|");
    Set<String> assignedRoles = new HashSet<String>();
    for (IPentahoRole pentahoRole : roleDao.getUserRoles(getTenant(tenantPath), userName)) {
      assignedRoles.add(pentahoRole.getName());
    }
    while (tokenizer.hasMoreTokens()) {
      assignedRoles.add(tokenizer.nextToken());
    }
    roleDao.setUserRoles(getTenant(tenantPath), userName, assignedRoles.toArray(new String[0]));
    return Response.ok().build();
  }

  @PUT
  @Path("/removeRoleFromUser")
  @Consumes({ WILDCARD })
  public Response removeRoleFromUser(@QueryParam("tenant") String tenantPath
      , @QueryParam("userName") String userName
      , @QueryParam("roleNames") String roleNames) {
    try {
      IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
      StringTokenizer tokenizer = new StringTokenizer(roleNames, "|");
      Set<String> assignedRoles = new HashSet<String>();
      for (IPentahoRole pentahoRole : roleDao.getUserRoles(getTenant(tenantPath), userName)) {
        assignedRoles.add(pentahoRole.getName());
      }
      while (tokenizer.hasMoreTokens()) {
        assignedRoles.remove(tokenizer.nextToken());
      }
      roleDao.setUserRoles(getTenant(tenantPath), userName, assignedRoles.toArray(new String[0]));
      return Response.ok().build();
    } catch(Throwable th) {
      return processErrorResponse(th.getLocalizedMessage());
    }
  }

  @PUT
  @Path("/assignAllRolesToUser")
  @Consumes({ WILDCARD })
  public Response assignAllRolesToUser(@QueryParam("tenant") String tenantPath
      , @QueryParam("userName") String userName) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    Set<String> assignedRoles = new HashSet<String>();
    for (IPentahoRole pentahoRole : roleDao.getRoles(getTenant(tenantPath))) {
      assignedRoles.add(pentahoRole.getName());
    }
    roleDao.setUserRoles(getTenant(tenantPath), userName, assignedRoles.toArray(new String[0]));
    return Response.ok().build();
  }

  @PUT
  @Path("/removeAllRolesFromUser")
  @Consumes({ WILDCARD })
  public Response removeAllRolesFromUser(@QueryParam("tenant") String tenantPath
      , @QueryParam("userName") String userName) {
    try {
      IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
      roleDao.setUserRoles(getTenant(tenantPath), userName, new String[0]);
      return Response.ok().build();
    } catch(Throwable th) {
      return processErrorResponse(th.getLocalizedMessage());
    }
  }

  @PUT
  @Path("/assignUserToRole")
  @Consumes({ WILDCARD })
  public Response assignUserToRole(@QueryParam("tenant") String tenantPath
      , @QueryParam("userNames") String userNames
      , @QueryParam("roleName") String roleName) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    StringTokenizer tokenizer = new StringTokenizer(userNames, "|");
    Set<String> assignedUserNames = new HashSet<String>();
    for (IPentahoUser pentahoUser : roleDao.getRoleMembers(getTenant(tenantPath), roleName)) {
      assignedUserNames.add(pentahoUser.getUsername());
    }
    while (tokenizer.hasMoreTokens()) {
      assignedUserNames.add(tokenizer.nextToken());
    }
    roleDao.setRoleMembers(getTenant(tenantPath), roleName, assignedUserNames.toArray(new String[0]));
    return Response.ok().build();
  }

  @PUT
  @Path("/removeUserFromRole")
  @Consumes({ WILDCARD })
  public Response removeUserFromRole(@QueryParam("tenant") String tenantPath
      , @QueryParam("userNames") String userNames
      , @QueryParam("roleName") String roleName) {
    try {
      IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
      StringTokenizer tokenizer = new StringTokenizer(userNames, "|");
      Set<String> assignedUserNames = new HashSet<String>();
      for (IPentahoUser pentahoUser : roleDao.getRoleMembers(getTenant(tenantPath), roleName)) {
        assignedUserNames.add(pentahoUser.getUsername());
      }
      while (tokenizer.hasMoreTokens()) {
        assignedUserNames.remove(tokenizer.nextToken());
      }
      roleDao.setRoleMembers(getTenant(tenantPath), roleName, assignedUserNames.toArray(new String[0]));
      return Response.ok().build();
    } catch(Throwable th) {
      return processErrorResponse(th.getLocalizedMessage());
    }
  }

  @PUT
  @Path("/assignAllUsersToRole")
  @Consumes({ WILDCARD })
  public Response assignAllUsersToRole(@QueryParam("tenant") String tenantPath
      , @QueryParam("roleName") String roleName) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    Set<String> assignedUserNames = new HashSet<String>();
    for (IPentahoUser pentahoUser : roleDao.getUsers(getTenant(tenantPath))) {
      assignedUserNames.add(pentahoUser.getUsername());
    }
    roleDao.setRoleMembers(getTenant(tenantPath), roleName, assignedUserNames.toArray(new String[0]));
    return Response.ok().build();
  }

  @PUT
  @Path("/removeAllUsersFromRole")
  @Consumes({ WILDCARD })
  public Response removeAllUsersFromRole(@QueryParam("tenant") String tenantPath
      , @QueryParam("roleName") String roleName) {
    try {
      IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
      roleDao.setRoleMembers(getTenant(tenantPath), roleName, new String[0]);
      return Response.ok().build();
    } catch(Throwable th) {
      return processErrorResponse(th.getLocalizedMessage());
    }
  }

  @PUT
  @Path("/createUser")
  @Consumes({ WILDCARD })
  public Response createUser(@QueryParam("tenant") String tenantPath, User user) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    roleDao.createUser(getTenant(tenantPath), user.getUserName(), user.getPassword(), "", new String[0]);
    return Response.ok().build();
  }

  @PUT
  @Path("/createRole")
  @Consumes({ WILDCARD })
  public Response createRole(@QueryParam("tenant") String tenantPath
      , @QueryParam("roleName") String roleName) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    roleDao.createRole(getTenant(tenantPath), roleName, "", new String[0]);
    return Response.ok().build();
  }

  @PUT
  @Path("/deleteRoles")
  @Consumes({ WILDCARD })
  public Response deleteRole(@QueryParam("roleNames") String roleNames) {
    try {
      IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
      StringTokenizer tokenizer = new StringTokenizer(roleNames, "|");
      while (tokenizer.hasMoreTokens()) {
        IPentahoRole role = roleDao.getRole(null, tokenizer.nextToken());
        if (role != null) {
          roleDao.deleteRole(role);
        }
      }
    } catch(Throwable th) {
      return processErrorResponse(th.getLocalizedMessage());
    }
    return Response.ok().build();
  }

  @PUT
  @Path("/deleteUsers")
  @Consumes({ WILDCARD })
  public Response deleteUser(@QueryParam("userNames") String userNames) {
    try {
      IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
      StringTokenizer tokenizer = new StringTokenizer(userNames, "|");
      while (tokenizer.hasMoreTokens()) {
        IPentahoUser user = roleDao.getUser(null, tokenizer.nextToken());
        if (user != null) {
          roleDao.deleteUser(user);
        }
      }
    } catch(Throwable th) {
      return processErrorResponse(th.getLocalizedMessage());
    }
    return Response.ok().build();
  }

  @PUT
  @Path("/updatePassword")
  @Consumes({ WILDCARD })
  public Response updatePassword(User user) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    IPentahoUser puser = roleDao.getUser(null, user.getUserName());
    if (puser != null) {
      roleDao.setPassword(null, user.getUserName(), user.getPassword());
    }
    return Response.ok().build();
  }

  @GET
  @Path("/logicalRoleMap")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
  public SystemRolesMap getRoleBindingStruct(@QueryParam("locale") String locale) {
    RoleBindingStruct roleBindingStruct = roleBindingDao.getRoleBindingStruct(locale);
    SystemRolesMap systemRolesMap = new SystemRolesMap();
    for (Map.Entry<String, String> localalizeNameEntry : roleBindingStruct.logicalRoleNameMap.entrySet()) {
      systemRolesMap.getLocalizedRoleNames().add(
          new LocalizedLogicalRoleName(localalizeNameEntry.getKey(), localalizeNameEntry.getValue()));
    }
    for (Map.Entry<String, List<String>> logicalRoleAssignments : roleBindingStruct.bindingMap.entrySet()) {
      systemRolesMap.getAssignments().add(
          new LogicalRoleAssignment(logicalRoleAssignments.getKey(), logicalRoleAssignments.getValue()));
    }
    return systemRolesMap;
  }

  @PUT
  @Consumes({ APPLICATION_XML, APPLICATION_JSON })
  @Path("/roleAssignments")
  public Response setLogicalRoles(LogicalRoleAssignments roleAssignments) {
    for (LogicalRoleAssignment roleAssignment : roleAssignments.getAssignments()) {
      roleBindingDao.setRoleBindings(roleAssignment.getRoleName(), roleAssignment.getLogicalRoles());
    }
    return Response.ok().build();
  }

  
  

  
  private ITenant getTenant(String tenantId) throws NotFoundException {
    ITenant tenant = null;
    if (tenantId != null) {
      tenant = tenantManager.getTenant(tenantId);
      if (tenant == null) {
        throw new NotFoundException("Tenant not found.");
      }
    } else {
      IPentahoSession session = PentahoSessionHolder.getSession();
      String tenantPath = (String) session.getAttribute(IPentahoSession.TENANT_ID_KEY);
      if (tenantPath != null) {
        tenant = new Tenant(tenantPath, true);
      }
    }
    return tenant;
  }

  private HashSet<String> tokenToString(String tokenString) {
    StringTokenizer tokenizer = new StringTokenizer(tokenString, "|");
    HashSet<String> result = new HashSet<String>();
    while (tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken());
    }
    return result;
  }


  private Response processErrorResponse(String errMessage) {
    return Response.ok(errMessage).build();
  }

}
