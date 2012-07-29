/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Mar 8, 2012 
 * @author wseyler
 */


package org.pentaho.platform.api.mt;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

/**
 * @author wseyler
 * 
 * This interface follows the following argument conventions:
 * - parentPath is a fully qualified TENANT ONLY path that resolves to the parent of the tenant to be operated on
 * - tenentPath is a fully qualified TENANT ONLY path that resolves to the tenant root folder.
 * - tenantId is a internal UUID that uniquely identifies the tenant root folder 
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
  ITenant createTenant(final ITenant parentTenant, final String tenantName, final String tenantAdminRoleName, final String authenticatedRoleName, final String anonymousRoleName);

  
  // ~ List Tenants ====================================================================

  /**
   * Gets children tenants of the "parent" tenant.  Returns only level one children.  Not descendants
   * 
   * @param parentTenant - 
   * @return List of children that are subTenants of the parent tenant.
   */
  List<ITenant> getChildTenants(final ITenant parentTenant);
  
  /**
   * Gets children tenants of the "parent" tenant.  Returns only level one children.  Not descendants. If the includeDisabledTenants
   * is true then it will return disabled tenants as well
   * 
   * @param parentTenant - 
   * @param includeDisabledTenants 
   * @return List of children that are subTenants of the parent tenant.
   */
  List<ITenant> getChildTenants(final ITenant parentTenant, boolean includeDisabledTenants);

  // ~ Modify Tenant ===================================================================
  /**
   * Updates tenant with the items in tenant info.  Each item must be a "well-know" attribute
   * 
   * @param tenantPath
   * @param tenantInfo
   * @return success
   */
  void updateTentant(final String tenantPath, final Map<String, Serializable> tenantInfo);
  

  /**
   * Deletes the tenant
   * 
   * @param tenant
   * @return success
   */
  void deleteTenant(final ITenant tenant);

  /**
   * Deletes a list of tenants
   * 
   * @param tenantPaths
   * @return success
   */
  void deleteTenants(final List<ITenant> tenantPaths);

  
  // ~ Enable/Disable Tenants ================================================================

  /**
   * Enables/disables the tenant with the paths of tenantPath
   * @param tenant
   * @param enable
   */
  void enableTenant(final ITenant tenant, final boolean enable);
  
  /**
   * Enables/disables the tenants with paths in the tenantPaths list
   * @param tenantPaths
   * @param enable
   */
  void enableTenants(final List<ITenant> tenantPaths, final boolean enable);
  
  // ~ Query Tenants ===================================================================

  /**
   * 
   * @param parentTenant
   * @param descendantTenant
   * @return boolean that is true if the parentTenant is the same as 
   * descendantTenant or the descendantTenant is the descendant of the
   * parent
   */
  boolean isSubTenant(final ITenant parentTenant, final ITenant descendantTenant);
  
  RepositoryFile getTenantRootFolder(final ITenant tenant);
  
  ITenant getTenant(String tenantId);
  
  ITenant getTenantByRootFolderPath(String tenantRootFolderPath);
}
