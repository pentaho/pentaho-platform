package org.pentaho.platform.engine.security.userrole.ws;

/**
 * JAX-WS-safe version of {@code IUserRoleListService}.
 * 
 * 
 * @author rmansoor
 */
import java.util.List;

import javax.jws.WebService;

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

}
