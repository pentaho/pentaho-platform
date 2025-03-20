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

import java.util.Map;

public class DefaultOAuthRoleMapper implements IAuthenticationRoleMapper {

  private Map<String, String> roleMap;

  public DefaultOAuthRoleMapper( Map<String, String> roleMap ) {
    this.roleMap = roleMap;
  }

  /**
   * This method is used to map a third-party role to a Pentaho role. If the third-party role is not found in the
   * roleMap, it returns the third-party role as is.
   *
   * The map is configured in applicationContext-spring-security-oauth.xml
   *
   * @param thirdPartyRole The third-party role to be mapped.
   * @return The mapped Pentaho role or the original third-party role if not found in the map.
   */
  @Override
  public String toPentahoRole( String thirdPartyRole ) {
    return roleMap.getOrDefault( thirdPartyRole, thirdPartyRole );
  }

  /**
   * This method is used to map a Pentaho role to a third-party role. If the Pentaho role is not found in the
   * roleMap, it returns the Pentaho role as is.
   *
   * @param pentahoRole The Pentaho role to be mapped.
   * @return The mapped third-party role or the original Pentaho role if not found in the map.
   */
  @Override
  public String fromPentahoRole( String pentahoRole ) {
    for ( Map.Entry<String, String> roleEntry : roleMap.entrySet() ) {
      if ( roleEntry.getValue().equals( pentahoRole ) ) {
        return roleEntry.getKey();
      }
    }
    return pentahoRole;
  }

}
