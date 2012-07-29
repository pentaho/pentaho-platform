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
 */
package org.pentaho.platform.security.policy.rolebased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * An authorization policy based on roles.
 * 
 * @author mlowery
 */
public class RoleAuthorizationPolicy implements IAuthorizationPolicy {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  // ~ Constructors ====================================================================================================

  public RoleAuthorizationPolicy(final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao) {
    super();
    Assert.notNull(roleBindingDao);
    this.roleBindingDao = roleBindingDao;
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public List<String> getAllowedActions(String actionNamespace) {
    List<String> assignedRolesInNamespace = new ArrayList<String>();
    if (actionNamespace == null) {
      assignedRolesInNamespace.addAll(roleBindingDao.getBoundLogicalRoleNames(getRuntimeRoleNames()));      
    } else {
      if (!actionNamespace.endsWith(".")) {
        actionNamespace += ".";
      }
      for (String assignedRole : roleBindingDao.getBoundLogicalRoleNames(getRuntimeRoleNames())) {
        if (assignedRole.startsWith(actionNamespace)) {
          assignedRolesInNamespace.add(assignedRole);
        }      
      }
    }
    return assignedRolesInNamespace;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAllowed(String actionName) {
    return roleBindingDao.getBoundLogicalRoleNames(getRuntimeRoleNames()).contains(actionName);
  }

  protected List<String> getRuntimeRoleNames() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    Authentication authentication = SecurityHelper.getInstance().getAuthentication();
    GrantedAuthority[] authorities = authentication.getAuthorities();
    List<String> runtimeRoles = new ArrayList<String>();
    for (int i = 0; i < authorities.length; i++) {
      runtimeRoles.add(authorities[i].getAuthority());
    }
    return runtimeRoles;
  }

}
