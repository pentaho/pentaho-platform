package org.pentaho.platform.engine.security.userrole.ws;

import java.util.List;
import javax.jws.WebService;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Implementation of {@link IUserRoleListWebService} that delegates to an {@link IUserRoleListService} instance.
 * 
 * @author rmansoor
 */
@WebService(endpointInterface = "org.pentaho.platform.engine.security.userrole.ws.IUserRoleListWebService", serviceName = "userRoleListService", portName = "userRoleListServicePort", targetNamespace = "http://www.pentaho.org/ws/1.0")

public class DefaultUserRoleListWebService implements IUserRoleListWebService{

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private IUserRoleListService userRoleListService;

  // ~ Constructors ====================================================================================================

  /**
   * No-arg constructor for when in Pentaho BI Server.
   */
  public DefaultUserRoleListWebService() {
    super();
    userRoleListService = PentahoSystem.get(IUserRoleListService.class);
    if (userRoleListService == null) {
      throw new IllegalStateException("no IUserRoleListService implementation");
    }
  }

  public DefaultUserRoleListWebService(final IUserRoleListService userRoleListService) {
    super();
    this.userRoleListService = userRoleListService;
  }

  // ~ Methods =========================================================================================================

  

  public List<String> getAllRoles() {
    return userRoleListService.getAllRoles();
  }

  public List<String> getAllUsers() {
    return userRoleListService.getAllUsers();
  }

  public List<String> getUsersInRole(String role) {
    return userRoleListService.getUsersInRole(role);
  }

  public List<String> getRolesForUser(String username) {
    return userRoleListService.getRolesForUser(username);
  }

  public UserRoleInfo getUserRoleInfo() {
    UserRoleInfo userRoleInfo = new UserRoleInfo();
    userRoleInfo.setRoles(getAllRoles());
    userRoleInfo.setUsers(getAllUsers());
    return userRoleInfo;
  }

}
