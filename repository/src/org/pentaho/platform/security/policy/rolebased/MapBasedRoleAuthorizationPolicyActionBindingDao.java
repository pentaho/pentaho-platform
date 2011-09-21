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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * An {@link IRoleAuthorizationPolicyActionBindingDao} implementation based on a {@code Map}.
 * 
 * @author mlowery
 */
public class MapBasedRoleAuthorizationPolicyActionBindingDao implements IRoleAuthorizationPolicyActionBindingDao {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  /**
   * Keys are action names. Values are lists of logical role names.
   */
  private Map<String, List<String>> actionNameToLogicalRoleNameMap;

  /**
   * Keys are namespaces. Values are action names.
   */
  private Multimap<String, String> namespaceToActionNameMap = ArrayListMultimap.create();

  // ~ Constructors ====================================================================================================

  public MapBasedRoleAuthorizationPolicyActionBindingDao(final Map<String, List<String>> actionNameToLogicalRoleNameMap) {
    super();
    this.actionNameToLogicalRoleNameMap = actionNameToLogicalRoleNameMap;
    processNamespaces();
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public Map<String, List<String>> getActionBindings(final String actionNamespace) {
    if (actionNamespace == null) {
      return actionNameToLogicalRoleNameMap;
    }
    Map<String, List<String>> actionBindings = new HashMap<String, List<String>>();
    List<String> actionNames = new ArrayList<String>(namespaceToActionNameMap.get(actionNamespace));
    for (String actionName : actionNames) {
      actionBindings.put(actionName, actionNameToLogicalRoleNameMap.get(actionName));
    }
    return actionBindings;
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getBoundLogicalRoleNames(final String actionName) {
    if (actionNameToLogicalRoleNameMap.containsKey(actionName)) {
      return actionNameToLogicalRoleNameMap.get(actionName);
    } else {
      return Collections.emptyList();
    }
  }

  protected void processNamespaces() {
    for (String actionName : actionNameToLogicalRoleNameMap.keySet()) {
      String[] tokens = actionName.split("\\."); //$NON-NLS-1$
      // tokens.length - 1 is to not process the actual action name (the last token)
      for (int i = 0; i < tokens.length-1; i++) {
        StringBuilder buf = new StringBuilder(actionName.length());
        for (int j = 0; j <= i; j++) {
          if (j > 0) {
            buf.append("."); //$NON-NLS-1$
          }
          buf.append(tokens[j]);
        }
        namespaceToActionNameMap.put(buf.toString(), actionName);
      }
    }
  }

}
