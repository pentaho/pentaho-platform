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

import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Map role to pentaho security role
 */
public class DefaultJdbcRoleMapper implements IAuthenticationRoleMapper {

  Map<String, String> roleMap;

  /**
   *
   */
  public DefaultJdbcRoleMapper() {
  }

  /**
   * 
   * @param roleMap
   */
  public DefaultJdbcRoleMapper( Map<String, String> roleMap ) {
    this.roleMap = new HashMap<String, String>();
    for ( Entry<String, String> roleEntry : roleMap.entrySet() ) {
      this.roleMap.put( roleEntry.getKey(), roleEntry.getValue() );
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
