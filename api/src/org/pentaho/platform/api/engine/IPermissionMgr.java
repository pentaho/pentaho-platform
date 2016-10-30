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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

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
