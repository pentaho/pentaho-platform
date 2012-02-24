/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.engine.security.userroledao.hibernate.sample;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;
import org.pentaho.platform.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.engine.security.userroledao.hibernate.HibernateUserRoleDao.InitHandler;
import org.pentaho.platform.engine.security.userroledao.messages.Messages;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Inserts sample users and roles into tables if those tables are empty.
 * 
 * <p>This handler checks to see if the users table is empty. If it is empty, then it inserts sample users and roles.
 * </p>
 * 
 * TODO mlowery Use DefaultPentahoPasswordEncoder to encode the hard-coded passwords.
 * 
 * @see InitHandler
 * @author mlowery
 */
public class SampleUsersAndRolesInitHandler extends HibernateDaoSupport implements InitHandler {

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
        PentahoRole adminRole = new PentahoRole("Admin", "Super User"); //$NON-NLS-1$ //$NON-NLS-2$
        PentahoRole ceo = new PentahoRole("ceo", "Chief Executive Officer"); //$NON-NLS-1$ //$NON-NLS-2$
        PentahoRole cto = new PentahoRole("cto", "Chief Technology Officer"); //$NON-NLS-1$ //$NON-NLS-2$
        PentahoRole dev = new PentahoRole("dev", "Developer"); //$NON-NLS-1$ //$NON-NLS-2$
        PentahoRole devMgr = new PentahoRole("devmgr", "Development Manager"); //$NON-NLS-1$ //$NON-NLS-2$
        PentahoRole is = new PentahoRole("is", "Information Services"); //$NON-NLS-1$ //$NON-NLS-2$

        userRoleDao.createRole(adminRole);
        userRoleDao.createRole(ceo);
        userRoleDao.createRole(cto);
        userRoleDao.createRole(dev);
        userRoleDao.createRole(devMgr);
        userRoleDao.createRole(is);

        PentahoUser admin = new PentahoUser("admin", "c2VjcmV0", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        admin.addRole(adminRole);
        PentahoUser joe = new PentahoUser("joe", "cGFzc3dvcmQ=", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        joe.addRole(adminRole);
        joe.addRole(ceo);
        PentahoUser pat = new PentahoUser("pat", "cGFzc3dvcmQ=", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        pat.addRole(dev);
        PentahoUser suzy = new PentahoUser("suzy", "cGFzc3dvcmQ=", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        suzy.addRole(cto);
        suzy.addRole(is);
        PentahoUser tiffany = new PentahoUser("tiffany", "cGFzc3dvcmQ=", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        tiffany.addRole(dev);
        tiffany.addRole(devMgr);

        userRoleDao.createUser(admin);
        userRoleDao.createUser(joe);
        userRoleDao.createUser(pat);
        userRoleDao.createUser(suzy);
        userRoleDao.createUser(tiffany);
      }
    } catch (UncategorizedUserRoleDaoException e) {
      // log error and simply return
      logger.error(Messages.getInstance().getString("SampleUsersAndRolesInitHandler.ERROR_0001_COULD_NOT_INSERT_SAMPLES"), e); //$NON-NLS-1$
    }
  }
  
  protected boolean hasUsers() {
    Long count = (Long) getHibernateTemplate().execute(
        new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException {
            Query query = session.createQuery("select count(*) from PentahoUser"); //$NON-NLS-1$
            return query.iterate().next();
          }
        });
    return count.longValue() > 0;
  }

  public void setUserRoleDao(final IUserRoleDao userRoleDao) {
    this.userRoleDao = userRoleDao;
  }

}
