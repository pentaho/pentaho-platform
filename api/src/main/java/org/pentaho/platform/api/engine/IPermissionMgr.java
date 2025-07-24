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

import java.util.Map;

@Deprecated
public interface IPermissionMgr {
  /**
   * TODO mlowery This is really addPermission. Perhaps a method name change?
   */
  public void setPermission( IPermissionRecipient permRecipient, IPermissionMask permission, Object domainInstance );

  /**
   * Returns permission map containing access control entries that are defined directly on this
   * <code>domainInstance</code>.
   *
   * @param domainInstance
   *          the object for which to fetch permissions
   * @return a map of permissions
   */
  public Map<IPermissionRecipient, IPermissionMask> getPermissions( Object domainInstance );

  /**
   * Returns permission map containing access control entries that are defined directly on this
   * <code>domainInstance</code>. If there are no direct entries, then the permission map will be the map of one of
   * <code>domainInstance</code>'s ancestors.
   *
   * @param domainInstance
   *          the object for which to fetch permissions
   * @return a map of permissions
   */
  public Map<IPermissionRecipient, IPermissionMask> getEffectivePermissions( Object domainInstance );

  public void setPermissions( Map<IPermissionRecipient, IPermissionMask> acl, Object domainInstance );
}
