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

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author wseyler
 * 
 *         This interface follows the following argument conventions: - parentPath is a fully qualified TENANT ONLY
 *         path that resolves to the parent of the tenant to be operated on - tenentPath is a fully qualified
 *         TENANT ONLY path that resolves to the tenant root folder. - tenantId is a internal UUID that uniquely
 *         identifies the tenant root folder
 */
public interface ITenantManager {
  // ~ Constants
  public static final String TENANT_ROOT = "isTenantRoot"; //$NON-NLS-1$
  public static final String TENANT_ENABLED = "isTenantEnabled"; //$NON-NLS-1$

  /**
   * @param parentPath
   * @param tenantName
   * @return a "tenantPath"
   */
  ITenant createTenant( final ITenant parentTenant, final String tenantName, final String tenantAdminRoleName,
      final String authenticatedRoleName, final String anonymousRoleName );

  // ~ List Tenants ====================================================================

  /**
   * Gets children tenants of the "parent" tenant. Returns only level one children. Not descendants
   * 
   * @param parentTenant
   *          -
   * @return List of children that are subTenants of the parent tenant.
   */
  List<ITenant> getChildTenants( final ITenant parentTenant );

  /**
   * Gets children tenants of the "parent" tenant. Returns only level one children. Not descendants. If the
   * includeDisabledTenants is true then it will return disabled tenants as well
   * 
   * @param parentTenant
   *          -
   * @param includeDisabledTenants
   * @return List of children that are subTenants of the parent tenant.
   */
  List<ITenant> getChildTenants( final ITenant parentTenant, boolean includeDisabledTenants );

  // ~ Modify Tenant ===================================================================
  /**
   * Updates tenant with the items in tenant info. Each item must be a "well-know" attribute
   * 
   * @param tenantPath
   * @param tenantInfo
   * @return success
   */
  void updateTentant( final String tenantPath, final Map<String, Serializable> tenantInfo );

  /**
   * Deletes the tenant
   * 
   * @param tenant
   * @return success
   */
  void deleteTenant( final ITenant tenant );

  /**
   * Deletes a list of tenants
   * 
   * @param tenantPaths
   * @return success
   */
  void deleteTenants( final List<ITenant> tenantPaths );

  // ~ Enable/Disable Tenants ================================================================

  /**
   * Enables/disables the tenant with the paths of tenantPath
   * 
   * @param tenant
   * @param enable
   */
  void enableTenant( final ITenant tenant, final boolean enable );

  /**
   * Enables/disables the tenants with paths in the tenantPaths list
   * 
   * @param tenantPaths
   * @param enable
   */
  void enableTenants( final List<ITenant> tenantPaths, final boolean enable );

  // ~ Query Tenants ===================================================================

  /**
   * 
   * @param parentTenant
   * @param descendantTenant
   * @return boolean that is true if the parentTenant is the same as descendantTenant or the descendantTenant is
   *         the descendant of the parent
   */
  boolean isSubTenant( final ITenant parentTenant, final ITenant descendantTenant );

  RepositoryFile getTenantRootFolder( final ITenant tenant );

  ITenant getTenant( String tenantId );

  ITenant getTenantByRootFolderPath( String tenantRootFolderPath );

  /**
   * Creates users home folder. If the user is admin, this method will create home/admin folder
   * 
   * @param tenantPath
   * @param username
   * @return home folder
   */
  RepositoryFile createUserHomeFolder( final ITenant tenant, final String username );

  /**
   * Retrieves users home folder.
   * 
   * @param tenantPath
   * @param username
   * @return home folder if it exists
   */
  RepositoryFile getUserHomeFolder( final ITenant tenant, final String username );

}
