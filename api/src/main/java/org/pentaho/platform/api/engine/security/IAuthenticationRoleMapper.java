/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.engine.security;

/**
 * Maps LDAP roles to pentaho security roles using the map defined in the spring configuration file
 * (applicationContext-spring-security-ldap.xml).
 */
public interface IAuthenticationRoleMapper {

  /**
   * Takes a string name of third party role and returns the mapped Pentaho security role.
   * 
   * @param ldapRole Third party role.
   * @return Returns the Pentaho security role.
   */
  public String toPentahoRole( String thirdPartyRole );

  /**
   * Takes a Pentaho security role and returns the mapped third party role.
   * 
   * @param pentahoRole Pentaho security role.
   * @return Returns the third party role.
   */
  public String fromPentahoRole( String pentahoRole );

}
