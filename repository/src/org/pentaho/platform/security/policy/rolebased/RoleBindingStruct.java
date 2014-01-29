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

package org.pentaho.platform.security.policy.rolebased;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple way to group two related pieces of info together. The caller receives this all at once.
 * 
 * @author mlowery
 */
public class RoleBindingStruct {

  /**
   * Keys are logical role names and values are localized logical role names.
   */
  public Map<String, String> logicalRoleNameMap;

  /**
   * Keys are runtime role names and values are lists of logical role names;
   */
  public Map<String, List<String>> bindingMap;

  public Set<String> immutableRoles;

  public RoleBindingStruct( Map<String, String> logicalRoleNameMap, Map<String, List<String>> bindingMap, Set<String> immutableRoles ) {
    super();
    this.logicalRoleNameMap = logicalRoleNameMap;
    this.bindingMap = bindingMap;
    this.immutableRoles = immutableRoles;
  }

}
