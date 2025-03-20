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

  @Override
  public String toPentahoRole( String thirdPartyRole ) {
    return roleMap.getOrDefault( thirdPartyRole, thirdPartyRole );
  }

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
