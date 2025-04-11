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


package org.pentaho.platform.api.engine;

/**
 * Base Hitachi Vantara Access Control entry. Subclassed <tt>BasicAclEntry</tt> from Spring Security. Provides known access
 * controls.
 *
 * @author mbatchel
 * */
@Deprecated
public interface IPentahoAclEntry extends IPentahoBasicAclEntry {
  /**
   * No access (0)
   */
  public static final int PERM_NOTHING = 0;

  /**
   * Execute access (1)
   */
  public static final int PERM_EXECUTE = 0x01; // Used to turn on/off one bit in a bitmask.

  /**
   * Subscribe access (2)
   */
  public static final int PERM_SUBSCRIBE = 0x02; // Used to turn on/off one bit in a bitmask.

  /**
   * Create access (4)
   */
  public static final int PERM_CREATE = 0x04; // Used to turn on/off one bit in a bitmask.

  /**
   * Update access (8)
   */
  public static final int PERM_UPDATE = 0x08; // Used to turn on/off one bit in a bitmask.

  /**
   * Delete (16)
   */
  public static final int PERM_DELETE = 0x10; // Used to turn on/off one bit in a bitmask.

  /**
   * Manage perms (32)
   */
  public static final int PERM_UPDATE_PERMS = 0x20; // Used to turn on/off one bit in a bitmask.

  /**
   * Administration access (60)
   */
  public static final int PERM_ADMINISTRATION = IPentahoAclEntry.PERM_CREATE | IPentahoAclEntry.PERM_UPDATE
    | IPentahoAclEntry.PERM_DELETE | IPentahoAclEntry.PERM_UPDATE_PERMS;

  /**
   * Execute and subscribe (3)
   */
  public static final int PERM_EXECUTE_SUBSCRIBE = IPentahoAclEntry.PERM_EXECUTE | IPentahoAclEntry.PERM_SUBSCRIBE;

  /**
   * @deprecated Do not use this constant; instead use FULL_CONTROL for truly inclusive all access. Old ADMIN_ALL
   *             (ie, WRITE) combination (31)
   */
  @Deprecated
  public static final int PERM_ADMIN_ALL = IPentahoAclEntry.PERM_CREATE | IPentahoAclEntry.PERM_UPDATE
    | IPentahoAclEntry.PERM_DELETE | IPentahoAclEntry.PERM_EXECUTE | IPentahoAclEntry.PERM_SUBSCRIBE;

  /**
   * All possible permissions (all ones; 0xffffffff; a negative number)
   */
  public static final int PERM_FULL_CONTROL = 0xffffffff;

  /**
   * Subscribe and administration (62)
   */
  public static final int PERM_SUBSCRIBE_ADMINISTRATION = IPentahoAclEntry.PERM_SUBSCRIBE
    | IPentahoAclEntry.PERM_ADMINISTRATION;

  /**
   * Execute and administration (61)
   */
  public static final int PERM_EXECUTE_ADMINISTRATION = IPentahoAclEntry.PERM_EXECUTE
    | IPentahoAclEntry.PERM_ADMINISTRATION;

  public static final String PERMISSIONS_LIST_SOLUTIONS = "solutions"; //$NON-NLS-1$

  public static final String PERMISSIONS_LIST_ALL = "all"; //$NON-NLS-1$

  public static final String PERMISSION_PREFIX = "PERM_"; //$NON-NLS-1$

}
