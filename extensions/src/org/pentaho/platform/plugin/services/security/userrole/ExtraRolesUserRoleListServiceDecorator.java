package org.pentaho.platform.plugin.services.security.userrole;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.springframework.util.Assert;

/**
 * Decorates another {@link IUserRoleListService} and returns a merged list consisting of the original roles from 
 * {@link IUserRoleListService#getAllRoles()} plus the extra roles. Roles are added to the end of the list and 
 * only if they don't already exist.
 * 
 * Use with {@code DefaultRoleUserDetailsServiceDecorator}.
 * 
 * @author mlowery
 */
public class ExtraRolesUserRoleListServiceDecorator implements IUserRoleListService {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(ExtraRolesUserRoleListServiceDecorator.class);

  // ~ Instance fields =================================================================================================

  private IUserRoleListService userRoleListService;

  private List<String> extraRoles;

  // ~ Constructors ====================================================================================================

  public ExtraRolesUserRoleListServiceDecorator() {
    super();
  }

  // ~ Methods =========================================================================================================

  @Override
  public List<String> getAllRoles() {
    return filterExtraRoles(getNewRoles());
  }

  protected List<String> getNewRoles() {
    List<String> origRoles = userRoleListService.getAllRoles();
    List<String> newRoles1 = new ArrayList<String>(origRoles);
    for (String extraRole : extraRoles) {
      if (!origRoles.contains(extraRole)) {
        newRoles1.add(extraRole);
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("original roles: %s, new roles: %s", origRoles, newRoles1)); //$NON-NLS-1$
    }
    return newRoles1;
  }
  
  @Override
  public List<String> getAllUsers() {
    return userRoleListService.getAllUsers();
  }

  public void setUserRoleListService(final IUserRoleListService userRoleListService) {
    this.userRoleListService = userRoleListService;
  }

  public void setExtraRoles(final List<String> extraRoles) {
    Assert.notNull(extraRoles);
    this.extraRoles = new ArrayList<String>(extraRoles);
  }
  
  public void setSystemRoles(final Set<String> systemRoles){
    Assert.notNull(systemRoles);
  }

  @Override
  public List<String> getAllRoles(ITenant tenant) {
    return filterExtraRoles(userRoleListService.getAllRoles(tenant));
  }

  @Override
  public List<String> getAllUsers(ITenant tenant) {
    return userRoleListService.getAllUsers();
  }

  @Override
  public List<String> getUsersInRole(ITenant tenant, String role) {
    return userRoleListService.getUsersInRole(tenant, role);
  }

  @Override
  public List<String> getRolesForUser(ITenant tenant, String username) {
    return filterExtraRoles(userRoleListService.getRolesForUser(tenant, username));
  }

  @Override
  public List<String> getSystemRoles() {
    return filterExtraRoles(userRoleListService.getSystemRoles());
  }
  
  private List<String> filterExtraRoles(List<String> roles){
    List<String> auths = new ArrayList<String>(roles.size() - extraRoles.size());
    
    for(String role : roles){
      if(!extraRoles.contains(role)){
        auths.add(role);
      }
    }
    
    return auths;
  }

}
