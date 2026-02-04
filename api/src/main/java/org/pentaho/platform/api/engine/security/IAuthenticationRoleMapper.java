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


package org.pentaho.platform.api.engine.security;

/**
 * Maps LDAP roles to pentaho security roles using the map defined in the spring configuration file
 * (applicationContext-spring-security-ldap.xml).
 */
public interface IAuthenticationRoleMapper {

  /**
   * Takes a string name of third party role and returns the mapped Hitachi Vantara security role.
   * 
   * @param ldapRole Third party role.
   * @return Returns the Pentaho security role.
   */
  public String toPentahoRole( String thirdPartyRole );

  /**
   * Takes a Hitachi Vantara security role and returns the mapped third party role.
   * 
   * @param pentahoRole Pentaho security role.
   * @return Returns the third party role.
   */
  public String fromPentahoRole( String pentahoRole );

}
