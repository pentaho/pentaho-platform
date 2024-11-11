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
