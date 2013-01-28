package org.pentaho.platform.security.userrole.ws;

import java.util.List;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;

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

  @Override
  public List<String> getAllRoles() {
    return userRoleListWebService.getAllRoles();
  }
  
  @Override
  public List<String> getAllUsers() {
    return userRoleListWebService.getAllUsers();
  }

  public UserRoleInfo getUserRoleInfo() {
    return userRoleListWebService.getUserRoleInfo();
  }

  @Override
  public List<String> getAllRoles(ITenant tenant) {
    return userRoleListWebService.getAllRolesForTenant((Tenant)tenant);
  }

  @Override
  public List<String> getAllUsers(ITenant tenant) {
    return userRoleListWebService.getAllUsersForTenant((Tenant)tenant);
  }

  @Override
  public List<String> getUsersInRole(ITenant tenant, String role) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getRolesForUser(ITenant tenant, String username) {
    throw new UnsupportedOperationException();
  }
}
