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


package org.pentaho.platform.engine.security;

import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Map ldap role to pentaho security role
 */
public class DefaultLdapRoleMapper implements IAuthenticationRoleMapper, Serializable {

  Map<String,String> roleMap;

  private final static String DEFAULT_ROLE_ATTRIBUTE_VALUE = "cn";
  private final static String ROLE_ATTRIBUTE_PROPERTY = "allAuthoritiesSearch.roleAttribute";
  private final static String LDAP_PROPERTIES_FILENAME = "applicationContext-security-ldap.properties";

  /**
   *
   */
  public DefaultLdapRoleMapper() {
  }

  /**
   * Get the role attribute from PentahoSystem if not provided to constructor
   *
   * @param newRoleMap
   */
  public DefaultLdapRoleMapper( Map<String, String> newRoleMap ) {
    String roleAttribute = getRoleAttributeFromProperties();
    this.roleMap = new HashMap<String,String>();
    for ( Entry<String, String> roleEntry : newRoleMap.entrySet() ) {
      this.roleMap.put( ldapParseString( roleEntry.getKey(), roleAttribute ), roleEntry.getValue() );
    }
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
  public String toPentahoRole(String thirdPartyRole) {
    if(roleMap.containsKey(thirdPartyRole)) {
      return roleMap.get(thirdPartyRole);
    }
    return thirdPartyRole;
  }

  /**
   * Parse role name from fq ldap designation
   *
   * @param ldapString
   * @return
   */
  private String ldapParseString(String ldapString, String key){
    String[] tokens = ldapString.split(",");

    // should always be the first occurrence of the key, e.g.:
    // CN=MuppetAdmins,CN=pentahoDepartments,CN=Pentaho,DC=muppets,DC=com
    // only return if it matches expected key, likely allAuthoritiesSearch.roleAttribute
    // only return first occurrence if key exists multiple times
    if( tokens.length > 0 ){
    for(String token : tokens){
        if(token.split( "=" )[0].toLowerCase().equals(key.toLowerCase())){
        return token.split("=")[1];
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


  /**
   * get role attribute from ldap properties using PentahoSystem
   */
  private String getRoleAttributeFromProperties() {
    Properties ldapProperties = new Properties();

    try {
      File propertiesFile = new File(System.getProperty("PentahoSystemPath") + System.getProperty("line.separator") + LDAP_PROPERTIES_FILENAME);
      InputStream propertiesInputFile = new FileInputStream(propertiesFile);
      ldapProperties.load(propertiesInputFile);

      if (ldapProperties != null) {
        String roleAttribute = (String) ldapProperties.getProperty(ROLE_ATTRIBUTE_PROPERTY);
        if (roleAttribute != null) {
          return roleAttribute;
        }
      }
    } catch (FileNotFoundException e) {
      // just swallow exception and return default
    } catch (IOException e) {

    }

    return DEFAULT_ROLE_ATTRIBUTE_VALUE;
  }
}
