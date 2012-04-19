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


package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
  
  // ~ Tenant Creation =================================================================
  /**
   * @param tenantName
   * @return
   */
  Serializable createSystemTenant(final String tenantName);
  
  /**
   * @param parentPath
   * @param tenantName
   * @return a "tenantPath"
   */
  Serializable createTenant(final Serializable parentTenantFolderId, final String tenantName);
  
  /**
   * @param parentPath
   * @param tenantNames
   * @return a List of strings each holding a "tenantPath"
   */
  List<Serializable> createTenants(final Serializable parentTenantFolderId, final List<String> tenantNames);
  
  /**
   * @param tenantPath
   * @param username
   * @return
   */
  Serializable createUserHomeFolder(final String tenantPath, final String username);
  
  // ~ List Tenants ====================================================================
  /**
   * Gets children of the "parentPath" tenant.  Returns only level one children.  Not descendants
   * 
   * @param parentPath
   * @return a list of "tenentPath"
   */
  List<Serializable> getChildTenants(final String parentTenantPath);
  
  /**
   * Gets children tenants of the "parentFolderId" tenant.  Returns only level one children.  Not descendants
   * 
   * @param parentFolderId - Serializable that represents the folder id of the parent tenant
   * @return List of children that are subTenants of the parent tenant.
   */
  List<Serializable> getChildTenants(final Serializable parentTenantFolderId);
  
  // ~ Modify Tenant ===================================================================
  /**
   * Updates tenant with the items in tenant info.  Each item must be a "well-know" attribute
   * 
   * @param tenantPath
   * @param tenantInfo
   * @return success
   */
  void updateTentant(final String tenantPath, final Map<String, Serializable> tenantInfo);
  
  // ~ Remove Tenants ==================================================================
  /**
   * Deletes the tenant based on the tenantId
   * @param tenantId
   */
  void deleteTenant(final Serializable tenantFolderId);
  
  /**
   * Deletes the tenant
   * 
   * @param tenantPath
   * @return success
   */
  void deleteTenant(final String tenantPath);
  
  /**
   * Deletes a list of tenants
   * 
   * @param tenantPaths
   * @return success
   */
  void deleteTenants(final List<String> tenantPaths);
  
  // ~ Disable Tenants ================================================================
  /**
   * Disables the tenant based on the tenantId
   * @param tenantId
   */
  void disableTenant(final Serializable tenantFolderId);
  
  /**
   * Disables the tenant.  No user login can be performed.
   * 
   * @param tenantPath
   * @return success
   */
  void disableTenant(final String tenantPath);
  
  /**
   * Disable the tenants in tenantPaths
   * 
   * @param tenantPaths
   * @return success
   */
  void disableTenants(final List<String> tenantPaths);
  
  // ~ Query Tenants ===================================================================
  /**
   * @param tenantRootfileId
   * @return boolean that is true if the tenantRootfileId is a tenant root directory
   */
  boolean isTenantRoot(final Serializable tenantFolderId);
  
  /**
   * @param tenantPath
   * @return boolean that is true if the tenantPath is a tenant root directory
   */
  boolean isTenantRoot(final String tenantPath);
  
  /**
   * @param tenantRootfileId
   * @return boolean that is true if the tenantRootfileId is an enabled tenant root directory
   */
  boolean isTenantEnabled(final Serializable tenantFolderId);
  
  /**
   * @param tenantPath
   * @return boolean that is true if the tenantPath is an enabled tenant root directory
   */
  boolean isTenantEnabled(final String tenantPath);
  
  /**
   * 
   * @param tenantPath
   * @param descendantTenantPath
   * @return boolean that is true if the parentTenantPath is the same as 
   * descendantTenantPath or the descendantTenantPath is the descendant of the
   * parent
   */
  boolean isSubTenant(final String parentTenantPath, final String descendantTenantPath);
}
