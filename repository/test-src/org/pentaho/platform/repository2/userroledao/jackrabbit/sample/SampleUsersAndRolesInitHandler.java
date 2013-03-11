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
        userRoleDao.createRole(getSampleTenant(), "Admin", "Super User", null);
        userRoleDao.createRole(getSampleTenant(), "ceo", "Chief Executive Officer", null);
        userRoleDao.createRole(getSampleTenant(), "cto", "Chief Technology Officer", null);
        userRoleDao.createRole(getSampleTenant(), "dev", "Developer", null);
        userRoleDao.createRole(getSampleTenant(), "devmgr", "Development Manager", null);
        userRoleDao.createRole(getSampleTenant(), "is", "Information Services", null);

        userRoleDao.createUser(getSampleTenant(), "admin", "admin", null, new String[]{"Admin"});
        userRoleDao.createUser(getSampleTenant(), "admin", "password", null, new String[]{"Admin"});
        userRoleDao.createUser(getSampleTenant(), "pat", "password", null, new String[]{"dev"});
        userRoleDao.createUser(getSampleTenant(), "suzy", "password", null, new String[]{"cto", "is"});
        userRoleDao.createUser(getSampleTenant(), "tiffany", "password", null, new String[]{"dev", "devmgr"});
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
