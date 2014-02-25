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

import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Map ldap role to pentaho security role
 */
public class DefaultLdapRoleMapper implements IAuthenticationRoleMapper, Serializable {

  Map<String, String> roleMap;

  private final static String DEFAULT_ROLE_ATTRIBUTE_VALUE = "cn";
  private final static String ROLE_ATTRIBUTE_PROPERTY = "allAuthoritiesSearch.roleAttribute";
  private final static String LDAP_PROPERTIES_FILENAME = "applicationContext-security-ldap.properties";

  private static String roleAttribute;

  static {
    roleAttribute = getRoleAttributeFromProperties();
  }

  /**
   * get role attribute from ldap properties using PentahoSystem
   */
  private static String getRoleAttributeFromProperties() {
    Properties ldapProperties = null;
    ISystemSettings systemSettings = PentahoSystem.getSystemSettings();

    if (systemSettings != null) {
      ldapProperties = systemSettings.getSystemSettingsProperties(LDAP_PROPERTIES_FILENAME);
    }

    if (ldapProperties != null) {
      String roleAttribute = (String) ldapProperties.getProperty(ROLE_ATTRIBUTE_PROPERTY);
      if (roleAttribute != null) {
        return roleAttribute;
      }
    }

    return DEFAULT_ROLE_ATTRIBUTE_VALUE;
  }

  /**
   *
   */
  public DefaultLdapRoleMapper() {
  }

  /**
   * Pass the role attribute from PentahoSystem if not provided to constructor
   *
   * @param roleMap
   */
  public DefaultLdapRoleMapper( Map<String, String> roleMap ) {
    this(roleMap, roleAttribute);
  }

  /**
   * 
   * @param newRoleMap
   */
  public DefaultLdapRoleMapper( Map<String, String> newRoleMap, String roleAttribute ) {
    this.roleMap = new HashMap<String, String>();
    for ( Entry<String, String> roleEntry : newRoleMap.entrySet() ) {
      this.roleMap.put( ldapParseString( roleEntry.getKey(), roleAttribute ), roleEntry.getValue() );
    }
  }

  /**
   * 
   * @param thirdPartyRole
   * @return
   */
  @Override
  public String toPentahoRole( String thirdPartyRole ) {
    if ( roleMap.containsKey( thirdPartyRole ) ) {
      return roleMap.get( thirdPartyRole );
    }
    return thirdPartyRole;
  }

  /**
   * Parse role name from fq ldap designation
   * 
   * @param ldapString
   * @return
   */
  private String ldapParseString( String ldapString, String key ) {
    String[] tokens = ldapString.split( "," );

    // should always be the first occurrence of the key, e.g.:
    // CN=MuppetAdmins,CN=pentahoDepartments,CN=Pentaho,DC=muppets,DC=com
    // only return if it matches expected key, likely allAuthoritiesSearch.roleAttribute
    // only return first occurrence if key exists multiple times
    if( tokens.length > 0 ){
      for(String token : tokens){
        if(token.split( "=" )[0].toLowerCase().equals(key.toLowerCase())){
          return token.split( "=" )[1];
        }
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
  public String fromPentahoRole( String pentahoRole ) {
    if ( roleMap.containsValue( pentahoRole ) ) {
      for ( Entry<String, String> roleEntry : roleMap.entrySet() ) {
        if ( roleEntry.getValue().equals( pentahoRole ) ) {
          return roleEntry.getKey();
        }
      }
    }
    return pentahoRole;
  }
}
