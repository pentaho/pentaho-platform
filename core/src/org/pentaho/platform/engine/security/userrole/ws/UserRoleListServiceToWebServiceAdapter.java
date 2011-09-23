package org.pentaho.platform.engine.security.userrole.ws;

import java.util.List;

import org.pentaho.platform.api.engine.IUserRoleListService;

/**
 * Converts calls to {@link IUserRoleListService} into {@link IUserRoleListWebService}. This is how client code 
 * remains unaware of server code location.
 * 
 * @author rmansoor
 */

public class UserRoleListServiceToWebServiceAdapter implements IUserRoleListService{
  
  private IUserRoleListWebService userRoleListWebService;
  

  public UserRoleListServiceToWebServiceAdapter(IUserRoleListWebService userRoleListWebService) {
    super();
    this.userRoleListWebService = userRoleListWebService;
  }

  public List<String> getAllRoles() {
    return userRoleListWebService.getAllRoles();
  }
  
  public List<String> getAllUsers() {
    return userRoleListWebService.getAllUsers();
  }

  public UserRoleInfo getUserRoleInfo() {
    return userRoleListWebService.getUserRoleInfo();
  }

  public List<String> getUsersInRole(String role) {
    throw new UnsupportedOperationException();
  }

  public List<String> getRolesForUser(String userName) {
    throw new UnsupportedOperationException();
  }
}
