package org.pentaho.platform.plugin.services.security.userrole;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IUserRoleListService;
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

  public List<String> getAllRoles() {
    return getNewRoles();
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
  
  public List<String> getAllUsers() {
    return userRoleListService.getAllUsers();
  }

  public List<String> getRolesForUser(final String username) {
    return userRoleListService.getRolesForUser(username);
  }

  public List<String> getUsersInRole(final String role) {
    return userRoleListService.getUsersInRole(role);
  }

  public void setUserRoleListService(final IUserRoleListService userRoleListService) {
    this.userRoleListService = userRoleListService;
  }

  public void setExtraRoles(final List<String> extraRoles) {
    Assert.notNull(extraRoles);
    this.extraRoles = new ArrayList<String>(extraRoles);
  }

}
