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

package org.pentaho.platform.engine.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;

/**
 * Map ldap role to pentaho security role
 */
public class DefaultLdapRoleMapper implements IAuthenticationRoleMapper, Serializable {

  Map<String,String> roleMap;

  /**
   *
   */
  public DefaultLdapRoleMapper() {
  }

  /**
   *
   * @param roleMap
   */
  public DefaultLdapRoleMapper(Map<String, String> roleMap) {
    this.roleMap = new HashMap<String,String>();
    for(Entry<String, String> roleEntry:roleMap.entrySet()) {
      this.roleMap.put(ldapParseString(roleEntry.getKey(), "cn"), roleEntry.getValue());
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
   * Parse role name from fq ldap designation
   * @param ldapString
   * @return
   */
  private String ldapParseString(String ldapString, String key){
    String[] tokens = ldapString.split(",");
    for(String token : tokens){
      if(token.contains(key)){
        return token.split("=")[1];
      }
    }
    return "";
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
