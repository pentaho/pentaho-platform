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

import java.util.List;
import java.util.Map;

/**
 * Associates (binds) logical roles with actions.
 * 
 * @author mlowery
 */
public interface IRoleAuthorizationPolicyActionBindingDao {

  /**
   * Returns a map with action names as keys and lists of logical role names as values.
   * 
   * @param actionNamespace action namespace (aka service name)
   * @return action binding map
   */
  Map<String, List<String>> getActionBindings(final String actionNamespace);

  /**
   * Returns list of logical role names associated with the given action name.
   * 
   * @param actionName namespaced action name
   * @return list of logical role names, never {@code null}
   */
  List<String> getBoundLogicalRoleNames(final String actionName);

}