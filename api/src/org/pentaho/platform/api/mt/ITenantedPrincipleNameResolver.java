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

package org.pentaho.platform.api.mt;

/**
 * Utility to go between principal ids (user/role ids) and principal name and tenant. For example let's assume that
 * user "admin" belongs to tenant "acme". This resolver class class is responsible for determining the user's
 * unique id within a multi-tenanted environment where multiple users with the same name may exist across tenants.
 * Conversely this class must be able to convert a unique user id to a user name & tenant.
 * 
 * @author rmansoor
 * 
 */
public interface ITenantedPrincipleNameResolver {

  /**
   * Extract the tenant from the principleId
   * 
   * @param principleId
   * @return tenant
   */
  public ITenant getTenant( String principleId );

  /**
   * Extract the principle name from the principleId
   * 
   * @param principleId
   * @return principle name
   */
  public String getPrincipleName( String principleId );

  /**
   * Construct a principle Id from tenant and principle name
   * 
   * @param tenant
   * @param principalName
   * @return principle id
   */
  public String getPrincipleId( ITenant tenant, String principalName );

  /**
   * Return flag indicating that principleId is a valid tenated string.
   * 
   * @param principleId
   * @return principle id
   */
  public boolean isValid( String principleId );
}
