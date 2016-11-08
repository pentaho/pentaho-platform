/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
