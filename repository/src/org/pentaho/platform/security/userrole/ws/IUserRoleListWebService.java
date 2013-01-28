package org.pentaho.platform.security.userrole.ws;

/**
 * JAX-WS-safe version of {@code IUserRoleListService}.
 * 
 * 
 * @author rmansoor
 */

import java.util.List;

import javax.jws.WebService;

import org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo;
import org.pentaho.platform.core.mt.Tenant;

@WebService
public interface IUserRoleListWebService {
  /**
  * This method returns the entire set of roles in the system
  * 
  * @return List of roles
  * 
  */
  public List<String> getAllRoles();
  /**
  * This method returns the entire set of users in the system
  * 
  * @return List of users
  * 
  */
  public List<String> getAllUsers();
 
  /**
  * This method returns the entire set of users and roles in the system
  * 
  * @return an info object
  * 
  */
  public UserRoleInfo getUserRoleInfo();

  public List<String> getAllRolesForTenant(Tenant tenant);

  public List<String> getAllUsersForTenant(Tenant tenant);

}
