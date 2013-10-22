/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.userroledao.jackrabbit.sample;

import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;

public class SampleUsersAndRolesInitHandler {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private IUserRoleDao userRoleDao;

  // ~ Constructors
  // ====================================================================================================

  public SampleUsersAndRolesInitHandler() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public void handleInit() {

    try {
      boolean hasUsers = hasUsers();

      if ( !hasUsers ) {
        userRoleDao.createRole( getSampleTenant(), "Administrator", "", null );
        userRoleDao.createRole( getSampleTenant(), "Power User", "", null );
        userRoleDao.createRole( getSampleTenant(), "Business Analyst", "", null );
        userRoleDao.createRole( getSampleTenant(), "Report Author", "", null );

        userRoleDao.createUser( getSampleTenant(), "admin", "password", null, new String[] { "Administrator" } );
        userRoleDao.createUser( getSampleTenant(), "pat", "password", null, new String[] { "Business Analyst" } );
        userRoleDao.createUser( getSampleTenant(), "suzy", "password", null, new String[] { "Power User" } );
        userRoleDao.createUser( getSampleTenant(), "tiffany", "password", null, new String[] { "Report Author" } );
      }
    } catch ( UncategorizedUserRoleDaoException e ) {
      //ignored
    }
  }

  protected boolean hasUsers() {
    return userRoleDao.getUsers( getSampleTenant() ).size() > 0;
  }

  public void setUserRoleDao( final IUserRoleDao userRoleDao ) {
    this.userRoleDao = userRoleDao;
  }

  protected ITenant getSampleTenant() {
    return new Tenant( "/penahot/steel-wheels", true );
  }

}
