package org.pentaho.platform.repository2.userroledao.jackrabbit.sample;

import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;

public class SampleUsersAndRolesInitHandler {

  // ~ Static fields/initializers ====================================================================================== 

  // ~ Instance fields =================================================================================================

  private IUserRoleDao userRoleDao;

  // ~ Constructors ====================================================================================================

  public SampleUsersAndRolesInitHandler() {
    super();
  }

  // ~ Methods =========================================================================================================

  public void handleInit() {

    try {
      boolean hasUsers = hasUsers();

      if (!hasUsers) {
        userRoleDao.createRole(getSampleTenant(), "Administrator", "", null);
        userRoleDao.createRole(getSampleTenant(), "Power User", "", null);
        userRoleDao.createRole(getSampleTenant(), "Business Analyst", "", null);
        userRoleDao.createRole(getSampleTenant(), "Report Author", "", null);

        userRoleDao.createUser(getSampleTenant(), "admin", "password", null, new String[]{"Administrator"});
        userRoleDao.createUser(getSampleTenant(), "pat", "password", null, new String[]{"Business Analyst"});
        userRoleDao.createUser(getSampleTenant(), "suzy", "password", null, new String[]{"Power User"});
        userRoleDao.createUser(getSampleTenant(), "tiffany", "password", null, new String[]{"Report Author"});
      }
    } catch (UncategorizedUserRoleDaoException e) {
    }
  }
  
  protected boolean hasUsers() {
    return userRoleDao.getUsers(getSampleTenant()).size() > 0;
  }

  public void setUserRoleDao(final IUserRoleDao userRoleDao) {
    this.userRoleDao = userRoleDao;
  }
  
  protected ITenant getSampleTenant() {
    return new Tenant("/penahot/steel-wheels", true);
  }

}
