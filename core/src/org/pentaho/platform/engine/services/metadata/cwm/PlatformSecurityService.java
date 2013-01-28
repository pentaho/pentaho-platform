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
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 *
 * @created Sep 20, 2007 
 * @author wseyler
 */

package org.pentaho.platform.engine.services.metadata.cwm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.pms.schema.security.SecurityACL;
import org.pentaho.pms.schema.security.SecurityService;

/**
 * @author wseyler
 *
 */
public class PlatformSecurityService extends SecurityService {
  List users = null;

  List roles = null;

  List acls = null;

  public PlatformSecurityService() {
    super();
    IUserRoleListService service = PentahoSystem.get(IUserRoleListService.class);
    users = service.getAllUsers();
    roles = service.getAllRoles();
    acls = new ArrayList();
    Map validPermissionsNameMap = PentahoAclEntry.getValidPermissionsNameMap(IPentahoAclEntry.PERMISSIONS_LIST_ALL);
    if (validPermissionsNameMap != null) {
      Set aclsKeySet = validPermissionsNameMap.keySet();
      for (Iterator aclsIterator = aclsKeySet.iterator(); aclsIterator.hasNext();) {
        String aclName = aclsIterator.next().toString();
        int aclMask = null != validPermissionsNameMap.get(aclName) ? Integer.parseInt(validPermissionsNameMap.get(
            aclName).toString()) : 0;
        acls.add(new SecurityACL(aclName, aclMask));
      }
    }
  }

  /**
   * Returns XML for list of users.
   */
  protected void doUsers(final StringBuffer buf) {
    IUserRoleListService service = PentahoSystem.get(IUserRoleListService.class);
    buf.append("<users>"); //$NON-NLS-1$
    if (service != null) {
      List users = service.getAllUsers();
      for (Iterator usersIterator = users.iterator(); usersIterator.hasNext();) {
        String username = usersIterator.next().toString();
        if ((null != username) && (username.length() > 0)) {
          buf.append("<user>" + username + "</user>"); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
    buf.append("</users>"); //$NON-NLS-1$
  }

  /**
   * Returns XML for list of roles.
   */
  protected void doRoles(final StringBuffer buf) {
    IUserRoleListService service = PentahoSystem.get(IUserRoleListService.class);
    buf.append("<roles>"); //$NON-NLS-1$
    if (service != null) {
      List roles = service.getAllRoles();
      for (Iterator rolesIterator = roles.iterator(); rolesIterator.hasNext();) {
        String roleName = rolesIterator.next().toString();
        if ((null != roleName) && (roleName.length() > 0)) {
          buf.append("<role>" + roleName + "</role>"); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
    buf.append("</roles>"); //$NON-NLS-1$
  }

  /**
   * Returns XML for list of ACLs.
   */
  protected void doACLs(final StringBuffer buf) {
    Map validPermissionsNameMap = PentahoAclEntry.getValidPermissionsNameMap(PentahoAclEntry.PERMISSIONS_LIST_ALL);
    buf.append("<acls>"); //$NON-NLS-1$
    if (validPermissionsNameMap != null) {
      Set aclsKeySet = validPermissionsNameMap.keySet();
      for (Iterator aclsIterator = aclsKeySet.iterator(); aclsIterator.hasNext();) {
        String aclName = aclsIterator.next().toString();
        String aclMask = null != validPermissionsNameMap.get(aclName) ? validPermissionsNameMap.get(aclName).toString()
            : null;

        if ((null != aclName) && (aclName.length() > 0) && (null != aclMask) && (aclMask.length() > 0)) {
          buf.append("<acl>"); //$NON-NLS-1$
          buf.append("<name>"); //$NON-NLS-1$
          buf.append(aclName);
          buf.append("</name>"); //$NON-NLS-1$
          buf.append("<mask>"); //$NON-NLS-1$
          buf.append(aclMask);
          buf.append("</mask>"); //$NON-NLS-1$
          buf.append("</acl>"); //$NON-NLS-1$
        }

      }
    }
    buf.append("</acls>"); //$NON-NLS-1$
  }

  public List getAcls() {
    return acls;
  }

  public List getUsers() {
    return users;
  }

  public List getRoles() {
    return roles;
  }
}
