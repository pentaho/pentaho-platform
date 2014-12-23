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
 * Utility to go between principal IDs (user/role IDs) and principal name and tenant. To illustrate, if 
 * user "admin" belongs to tenant "acme," this resolver class determines the user's
 * unique ID within a multi-tenanted environment where multiple users with the same name might exist across tenants.
 * Conversely this class must be able to convert a unique user ID to a user name & tenant.
 * 
 * @author rmansoor
 * 
 */
public interface ITenantedPrincipleNameResolver {

  /**
   * Extracts the tenant from the principleId.
   * 
   * @param principleId Principle ID.
   * @return Returns tenant that corresponds to the id.
   */
  public ITenant getTenant( String principleId );

  /**
   * Extracts the principle name from the principleId.
   * 
   * @param principleId Principle ID.
   * @return Returns principle name that matches the principle ID.
   */
  public String getPrincipleName( String principleId );

  /**
   * Constructs a principle ID from tenant and principle name.
   * 
   * @param tenant Tenant to be used for ID construction.
   * @param principalName Name of the principle.
   * @return Returns Unique ID for the specified principle name and tenant across all the tenants. 
   */
  public String getPrincipleId( ITenant tenant, String principalName );

  /**
   * Returns flag indicating that principleId is a valid tenanted string.
   * 
   * @param principleId ID of the principle.
   * @return Teturns True if the ID provided is a valid principle ID.
   */
  public boolean isValid( String principleId );
}
