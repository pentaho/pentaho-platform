package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;

import java.util.List;
import java.util.Map;
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

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.springframework.security.providers.encoding.PasswordEncoder;

@Path("/userrole/")
public class UserRoleResource extends AbstractJaxRSResource {

	private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(IRoleAuthorizationPolicyRoleBindingDao.class);

	@GET
	@Path("/users")
	@Produces({ MediaType.APPLICATION_XML })
	public Response getUsers() throws Exception {
		try {
			return Response.ok(SystemResourceUtil.getUsers().asXML()).type(MediaType.APPLICATION_XML).build();
		} catch (Throwable t) {
			throw new WebApplicationException(t);
		}
	}

	@GET
	@Path("/roles")
	@Produces({ MediaType.APPLICATION_JSON })
	public RoleListWrapper getRoles() throws Exception {
		IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
		return new RoleListWrapper(userRoleListService.getAllRoles());
	}

	@GET
	@Path("/logicalRoleMap")
	@Produces({ MediaType.APPLICATION_JSON })
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
	@Consumes({ APPLICATION_XML, APPLICATION_JSON })
	@Path("/roleAssignments")
	public Response setLogicalRoles(LogicalRoleAssignments roleAssignments) {
		for (LogicalRoleAssignment roleAssignment : roleAssignments.getLogicalRoleAssignments()) {
			roleBindingDao.setRoleBindings(roleAssignment.getRoleName(), roleAssignment.getLogicalRoles());
		}
		return Response.ok().build();
	}

	@GET
	@Path("/getRolesForUser")
	@Produces({ MediaType.APPLICATION_XML })
	public Response getRolesForUser(@QueryParam("user") String user) throws Exception {
		try {
			return Response.ok(SystemResourceUtil.getRolesForUser(user).asXML()).type(MediaType.APPLICATION_XML).build();
		} catch (Throwable t) {
			throw new WebApplicationException(t);
		}
	}

	@GET
	@Path("/getUsersInRole")
	@Produces({ MediaType.APPLICATION_XML })
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
	public Response assignRoleToUser(@QueryParam("userName") String userName, @QueryParam("roleName") String roleName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		IPentahoRole role = roleDao.getRole(roleName);
		IPentahoUser user = roleDao.getUser(userName);
		user.addRole(role);
		roleDao.updateUser(user);
		return Response.ok().build();
	}

	@PUT
	@Path("/removeRoleFromUser")
	@Consumes({ WILDCARD })
	public Response removeRoleFromUser(@QueryParam("userName") String userName, @QueryParam("roleName") String roleName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		IPentahoRole role = roleDao.getRole(roleName);
		IPentahoUser user = roleDao.getUser(userName);
		user.removeRole(role);
		roleDao.updateUser(user);
		return Response.ok().build();
	}
	
	@PUT
	@Path("/assignAllRolesToUser")
	@Consumes({ WILDCARD })
	public Response assignAllRolesToUser(@QueryParam("userName") String userName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
		IPentahoUser user = roleDao.getUser(userName);
		List<String> roleNames = userRoleListService.getAllRoles();
		for(String roleName : roleNames) {
			IPentahoRole role = roleDao.getRole(roleName);
			user.addRole(role);
		}
		roleDao.updateUser(user);
		return Response.ok().build();
	}
	
	@PUT
	@Path("/removeAllRolesFromUser")
	@Consumes({ WILDCARD })
	public Response removeAllRolesFromUser(@QueryParam("userName") String userName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
		IPentahoUser user = roleDao.getUser(userName);
		List<String> roleNames = userRoleListService.getAllRoles();
		for(String roleName : roleNames) {
			IPentahoRole role = roleDao.getRole(roleName);
			user.removeRole(role);
		}
		roleDao.updateUser(user);
		return Response.ok().build();
	}

	@PUT
	@Path("/assignUserToRole")
	@Consumes({ WILDCARD })
	public Response assignUserToRole(@QueryParam("userName") String userName, @QueryParam("roleName") String roleName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		IPentahoRole role = roleDao.getRole(roleName);
		IPentahoUser user = roleDao.getUser(userName);
		role.addUser(user);
		roleDao.updateRole(role);
		return Response.ok().build();
	}

	@PUT
	@Path("/removeUserFromRole")
	@Consumes({ WILDCARD })
	public Response removeUserFromRole(@QueryParam("userName") String userName, @QueryParam("roleName") String roleName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		IPentahoRole role = roleDao.getRole(roleName);
		IPentahoUser user = roleDao.getUser(userName);
		role.removeUser(user);
		roleDao.updateRole(role);
		return Response.ok().build();
	}
	
	@PUT
	@Path("/assignAllUsersToRole")
	@Consumes({ WILDCARD })
	public Response assignAllUsersToRole(@QueryParam("roleName") String roleName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
		IPentahoRole role = roleDao.getRole(roleName);
		List<String> userNames = userRoleListService.getAllUsers();
		for(String userName : userNames) {
			IPentahoUser user = roleDao.getUser(userName);
			role.addUser(user);
		}
		roleDao.updateRole(role);
		return Response.ok().build();
	}
	
	@PUT
	@Path("/removeAllUsersFromRole")
	@Consumes({ WILDCARD })
	public Response removeAllUsersFromRole(@QueryParam("roleName") String roleName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
		IPentahoRole role = roleDao.getRole(roleName);
		List<String> userNames = userRoleListService.getAllUsers();
		for(String userName : userNames) {
			IPentahoUser user = roleDao.getUser(userName);
			role.removeUser(user);
		}
		roleDao.updateRole(role);
		return Response.ok().build();
	}
	
	@PUT
	@Path("/createUser")
	@Consumes({ WILDCARD })
	public Response createUser(@QueryParam("userName") String userName, @QueryParam("password") String password) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		PentahoUser user = new PentahoUser(userName, password, "", true);
		roleDao.createUser(user);
		return Response.ok().build();
	}
	
	@PUT
	@Path("/createRole")
	@Consumes({ WILDCARD })
	public Response createRole(@QueryParam("roleName") String roleName) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		PentahoRole role = new PentahoRole(roleName);
		roleDao.createRole(role);
		return Response.ok().build();
	}
	
	@PUT
	@Path("/deleteRoles")
	@Consumes({ WILDCARD })
	public Response deleteRole(@QueryParam("roles") String roles) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		StringTokenizer tokenizer = new StringTokenizer(roles, "|");
		while(tokenizer.hasMoreTokens()) {
			IPentahoRole role = roleDao.getRole(tokenizer.nextToken());
			if(role != null) {
				roleDao.deleteRole(role);
			}
		}
		return Response.ok().build();
	}
	
	@PUT
	@Path("/deleteUsers")
	@Consumes({ WILDCARD })
	public Response deleteUser(@QueryParam("users") String users) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
		StringTokenizer tokenizer = new StringTokenizer(users, "|");
		while(tokenizer.hasMoreTokens()) {
			IPentahoUser user = roleDao.getUser(tokenizer.nextToken());
			if(user != null) {
				roleDao.deleteUser(user);
			}
		}
		return Response.ok().build();
	}
	
	@PUT
	@Path("/updatePassword")
	@Consumes({ WILDCARD })
	public Response updatePassword(@QueryParam("userName") String userName, @QueryParam("newPassword") String newPassword) {
		IUserRoleDao roleDao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession());
        PasswordEncoder encoder = PentahoSystem.get(PasswordEncoder.class, "passwordEncoder", PentahoSessionHolder.getSession());
		IPentahoUser user = roleDao.getUser(userName);
		if(user != null) {
			user.setPassword(encoder.encodePassword(newPassword, null));
			roleDao.updateUser(user);
		}
		return Response.ok().build();
	}
}
