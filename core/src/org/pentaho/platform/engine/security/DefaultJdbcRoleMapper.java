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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.engine.security;

import org.pentaho.platform.api.engine.security.IRoleMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Map role to pentaho security role
 */
public class DefaultJdbcRoleMapper implements IRoleMapper {

  Map<String,String> roleMap;

  /**
   *
   */
  public DefaultJdbcRoleMapper() {
  }

  /**
   *
   * @param roleMap
   */
  public DefaultJdbcRoleMapper(Map<String, String> roleMap) {
    this.roleMap = new HashMap<String,String>();
    for(Entry<String, String> roleEntry:roleMap.entrySet()) {
      this.roleMap.put(roleEntry.getKey(), roleEntry.getValue());
    }
  }

  /**
   *
   * @param thirdPartyRole
   * @return
   */
  @Override
  public String toPentahoRole(String thirdPartyRole) {
    if(roleMap.containsKey(thirdPartyRole)) {
      return roleMap.get(thirdPartyRole);
    }
    return thirdPartyRole;
  }

  /**
   *
   * @param pentahoRole
   * @return
   */
  @Override
  public String fromPentahoRole(String pentahoRole) {
    if(roleMap.containsValue(pentahoRole)) {
      for(Entry<String, String> roleEntry:roleMap.entrySet()) {
        if(roleEntry.getValue().equals(pentahoRole)) {
          return roleEntry.getKey();
        }
      }
    }
    return pentahoRole;
  }
}
