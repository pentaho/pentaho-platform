/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
