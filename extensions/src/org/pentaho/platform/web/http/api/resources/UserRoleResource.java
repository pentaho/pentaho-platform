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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
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

@org.codehaus.enunciate.modules.jersey.SpringManagedLifecycle
@Path("/userrole/")
public class UserRoleResource extends AbstractJaxRSResource {

	private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = null;
	private ITenantManager tenantManager = null;
	private ArrayList<String> systemRoles;
	private String adminRole;
	private ArrayList<String> extraRoles;

	public UserRoleResource() {
	  this(PentahoSystem.get(IRoleAuthorizationPolicyRoleBindingDao.class),
	  PentahoSystem.get(ITenantManager.class),
	  PentahoSystem.get(ArrayList.class, "singleTenantSystemAuthorities", PentahoSessionHolder.getSession()),
	  PentahoSystem.get(String.class, "singleTenantAdminAuthorityName", PentahoSessionHolder.getSession()),
	  PentahoSystem.get(ArrayList.class, "extraSystemAuthorities", PentahoSessionHolder.getSession()));
	}
  public UserRoleResource(final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao
      , final ITenantManager tenantMgr, final ArrayList<String> systemRoles, final String adminRole,
      final ArrayList<String> extraRoles) {
    if (roleBindingDao == null) {
      throw new IllegalArgumentException();
    }
    this.roleBindingDao = roleBindingDao;
    this.tenantManager = tenantMgr;
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
	@Path("/logicalRoleMap")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
	public SystemRolesMap getRoleBindingStruct(@QueryParam("locale") String locale) {
		RoleBindingStruct roleBindingStruct = roleBindingDao.getRoleBindingStruct(locale);
		SystemRolesMap systemRolesMap = new SystemRolesMap();
		for (Map.Entry<String, String> localalizeNameEntry : roleBindingStruct.logicalRoleNameMap.entrySet()) {
			systemRolesMap.getLocalizedRoleNames().add(new LocalizedLogicalRoleName(localalizeNameEntry.getKey(), localalizeNameEntry.getValue()));
		}
		for (Map.Entry<String, List<String>> logicalRoleAssignments : roleBindingStruct.bindingMap.entrySet()) {
			systemRolesMap.getAssignments().add(new LogicalRoleAssignment(logicalRoleAssignments.getKey(), logicalRoleAssignments.getValue()));
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

	@GET
	@Path("/getRolesForUser")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
	public Response getRolesForUser(@QueryParam("user") String user) throws Exception {
		try {
			return Response.ok(SystemResourceUtil.getRolesForUser(user).asXML()).type(MediaType.APPLICATION_XML).build();
		} catch (Throwable t) {
			throw new WebApplicationException(t);
		}
	}

	@GET
	@Path("/getUsersInRole")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })
	public Response getUsersInRole(@QueryParam("role") String role) throws Exception {
		try {
			return Response.ok(SystemResourceUtil.getUsersInRole(role).asXML()).type(MediaType.APPLICATION_XML).build();
		} catch (Throwable t) {
			throw new WebApplicationException(t);
		}
	}

	@PUT
	@Path("/assignRoleToUser")
	@Consumes({ WILDCARD })
	public Response assignRoleToUser(@QueryParam("tenant") String tenantPath, @QueryParam("userName") String userName, @QueryParam("roleNames") String roleNames) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
		StringTokenizer tokenizer = new StringTokenizer(roleNames, "|");
		Set<String>assignedRoles = new HashSet<String>();
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
	public Response removeRoleFromUser(@QueryParam("tenant") String tenantPath, @QueryParam("userName") String userName, @QueryParam("roleNames") String roleNames) {
    if (tokenToString(roleNames).contains(adminRole)) {
      String errMessage = checkAdminRole(null, userName + "|");
      if (errMessage != null) {
        return processErrorResponse(errMessage);
      }
    }
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    StringTokenizer tokenizer = new StringTokenizer(roleNames, "|");
    Set<String>assignedRoles = new HashSet<String>();
    for (IPentahoRole pentahoRole : roleDao.getUserRoles(getTenant(tenantPath), userName)) {
      assignedRoles.add(pentahoRole.getName());
    }
    while (tokenizer.hasMoreTokens()) {
      assignedRoles.remove(tokenizer.nextToken());
    }
    roleDao.setUserRoles(getTenant(tenantPath), userName, assignedRoles.toArray(new String[0]));
    return Response.ok().build();
	}

	@PUT
	@Path("/assignAllRolesToUser")
	@Consumes({ WILDCARD })
	public Response assignAllRolesToUser(@QueryParam("tenant") String tenantPath, @QueryParam("userName") String userName) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    Set<String>assignedRoles = new HashSet<String>();
    for (IPentahoRole pentahoRole : roleDao.getRoles(getTenant(tenantPath))) {
      assignedRoles.add(pentahoRole.getName());
    }
    roleDao.setUserRoles(getTenant(tenantPath), userName, assignedRoles.toArray(new String[0]));
    return Response.ok().build();
	}

	@PUT
	@Path("/removeAllRolesFromUser")
	@Consumes({ WILDCARD })
	public Response removeAllRolesFromUser(@QueryParam("tenant") String tenantPath, @QueryParam("userName") String userName) {
    String errMessage = checkAdminRole(null, userName + "|");
    if (errMessage != null) {
      return processErrorResponse(errMessage);
    }
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    roleDao.setUserRoles(getTenant(tenantPath), userName, new String[0]);
    return Response.ok().build();
	}

	@PUT
	@Path("/assignUserToRole")
	@Consumes({ WILDCARD })
	public Response assignUserToRole(@QueryParam("tenant") String tenantPath, @QueryParam("userNames") String userNames, @QueryParam("roleName") String roleName) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    StringTokenizer tokenizer = new StringTokenizer(userNames, "|");
    Set<String>assignedUserNames = new HashSet<String>();
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
	public Response removeUserFromRole(@QueryParam("tenant") String tenantPath, @QueryParam("userNames") String userNames, @QueryParam("roleName") String roleName) {
	  if (roleName.equals(adminRole)) {
      String errMessage = checkAdminRole(null, userNames);
      if (errMessage != null) {
        return processErrorResponse(errMessage);
      }
	  }
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    StringTokenizer tokenizer = new StringTokenizer(userNames, "|");
    Set<String>assignedUserNames = new HashSet<String>();
    for (IPentahoUser pentahoUser : roleDao.getRoleMembers(getTenant(tenantPath), roleName)) {
      assignedUserNames.add(pentahoUser.getUsername());
    }
    while (tokenizer.hasMoreTokens()) {
      assignedUserNames.remove(tokenizer.nextToken());
    }
    roleDao.setRoleMembers(getTenant(tenantPath), roleName, assignedUserNames.toArray(new String[0]));
    return Response.ok().build();
	}

	@PUT
	@Path("/assignAllUsersToRole")
	@Consumes({ WILDCARD })
	public Response assignAllUsersToRole(@QueryParam("tenant") String tenantPath, @QueryParam("roleName") String roleName) {
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    Set<String>assignedUserNames = new HashSet<String>();
    for (IPentahoUser pentahoUser : roleDao.getUsers(getTenant(tenantPath))) {
      assignedUserNames.add(pentahoUser.getUsername());
    }
    roleDao.setRoleMembers(getTenant(tenantPath), roleName, assignedUserNames.toArray(new String[0]));
    return Response.ok().build();
	}

	@PUT
	@Path("/removeAllUsersFromRole")
	@Consumes({ WILDCARD })
	public Response removeAllUsersFromRole(@QueryParam("tenant") String tenantPath, @QueryParam("roleName") String roleName) {
    if (adminRole.equals(roleName)) {
      return processErrorResponse("Illegal to remove all users from the " + adminRole +" role");
    }
    IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
    roleDao.setRoleMembers(getTenant(tenantPath), roleName, new String[0]);
    return Response.ok().build();
	}

	@PUT
	@Path("/createUser")
	@Consumes({ WILDCARD })
	public Response createUser(@QueryParam("tenant") String tenantPath, @QueryParam("userName") String userName, @QueryParam("password") String password) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
		roleDao.createUser(getTenant(tenantPath), userName, password, "", new String[0]);
		return Response.ok().build();
	}

	@PUT
	@Path("/createRole")
	@Consumes({ WILDCARD })
	public Response createRole(@QueryParam("tenant") String tenantPath, @QueryParam("roleName") String roleName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
		roleDao.createRole(getTenant(tenantPath), roleName, "", new String[0]);
		return Response.ok().build();
	}

	@PUT
	@Path("/deleteRoles")
	@Consumes({ WILDCARD })
	public Response deleteRole(@QueryParam("roles") String roles) {
	  String errMessage = checkAdminRole(roles, null);
    if (errMessage != null) {
      return processErrorResponse(errMessage);
    }
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
		StringTokenizer tokenizer = new StringTokenizer(roles, "|");
		while (tokenizer.hasMoreTokens()) {
			IPentahoRole role = roleDao.getRole(null,tokenizer.nextToken());
			if (role != null) {
				roleDao.deleteRole(role);
			}
		}
		return Response.ok().build();
	}

	@PUT
	@Path("/deleteUsers")
	@Consumes({ WILDCARD })
	public Response deleteUser(@QueryParam("users") String users) {
	  String errMessage = checkAdminRole(null, users);
    if (errMessage != null) {
      return processErrorResponse(errMessage);
    }
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
		StringTokenizer tokenizer = new StringTokenizer(users, "|");
		while (tokenizer.hasMoreTokens()) {
			IPentahoUser user = roleDao.getUser(null,tokenizer.nextToken());
			if (user != null) {
				roleDao.deleteUser(user);
			}
		}
		return Response.ok().build();
	}

	@PUT
	@Path("/updatePassword")
	@Consumes({ WILDCARD })
	public Response updatePassword(@QueryParam("userName") String userName, @QueryParam("newPassword") String newPassword) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession());
		IPentahoUser user = roleDao.getUser(null,userName);
		if (user != null) {
			roleDao.setPassword(null,userName, newPassword);
		}
		return Response.ok().build();
	}
	
	private ITenant getTenant(String tenantId) throws NotFoundException {
	  ITenant tenant = null;
	   if(tenantId != null) {
	      tenant = tenantManager.getTenant(tenantId);
	      if (tenant == null) {
	        throw new NotFoundException("Tenant not found.");
	      }
	   } else {
	      IPentahoSession session = PentahoSessionHolder.getSession();
	      String tenantPath = (String) session.getAttribute(IPentahoSession.TENANT_ID_KEY);
	      if(tenantPath != null) {
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
	
	/**
	 * Checks to see if the removal of the received roles and users would
	 * cause the system to have no login associated with the Admin role.
	 * This check is to be made before any changes take place
	 * @param deleteRoles  Roles to be deleted separated with | char
	 * @param deleteUsers  Users to be deleted separated with | char
	 * @return Error message if invalid or null if ok
	 */
	private String checkAdminRole(String deleteRoles, String deleteUsers) {
    //Make sure system roles are not being deleted
	  if (deleteRoles != null && deleteRoles.length() > 0) {
	    StringTokenizer tokenizer = new StringTokenizer(deleteRoles, "|");
	    while (tokenizer.hasMoreTokens()) {
	      String token = tokenizer.nextToken();
	      if (systemRoles.contains(token)) {
          return ("Illegal to remove the " + token +" role");
        }
      }
	  }
	  //Make sure the last user with Admin role is not being deleted
	  ITenant tenant = getTenant(null);
	  IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
	  HashSet<String> usersInAdminRole = new HashSet<String>(userRoleListService.getUsersInRole(tenant, adminRole));
	  if (deleteUsers != null && deleteUsers.length() > 0) {
      StringTokenizer tokenizer = new StringTokenizer(deleteUsers, "|");
      while (tokenizer.hasMoreTokens()) {
        usersInAdminRole.remove(tokenizer.nextToken());
        if (usersInAdminRole.size() == 0) {
          return "Illegal to remove the last user with " + adminRole +" role";
        }
      }
	  }
	  return null;
	}
	
	private Response processErrorResponse(String errMessage) {
	    return Response.ok(errMessage).build();
	}
}
