/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.security.policy.rolebased;

import org.pentaho.platform.api.mt.ITenant;

import java.util.List;

/**
 * Associates (binds) logical roles with runtime roles.
 * 
 * @author mlowery
 */
public interface IRoleAuthorizationPolicyRoleBindingDao {

  /**
   * Gets a struct-like object that contains everything known by this DAO. This is a batch operation provided for
   * UIs.
   * 
   * @param locale
   *          locale, possibly {@code null}
   * @return role binding struct
   */
  RoleBindingStruct getRoleBindingStruct( final String locale );

  /**
   * Gets a struct-like object that contains everything known by this DAO for a given tenant. This is a batch
   * operation provided for UIs.
   * 
   * @param tenant
   * @param locale
   * @return role binding struct
   */
  RoleBindingStruct getRoleBindingStruct( final ITenant tenant, final String locale );

  /**
   * Sets the bindings for the given runtime role. All other bindings for this runtime role are removed.
   * 
   * @param runtimeRoleName
   *          runtime role name
   * @param logicalRoleNames
   *          list of logical role names
   */
  void setRoleBindings( final String runtimeRoleName, final List<String> logicalRolesNames );

  /**
   * Sets the bindings for the given runtime role in a particular tenant. All other bindings for this runtime role
   * are removed.
   * 
   * @param tenant
   * @param runtimeRoleName
   * @param logicalRolesNames
   */
  void setRoleBindings( final ITenant tenant, final String runtimeRoleName, final List<String> logicalRolesNames );

  /**
   * Gets the logical roles bound to the given runtime roles. Note that the size of the incoming list might not
   * match the size of the returned list. This is a convenience method. The same result could be obtained from
   * {@link #getRoleBindingStruct()}.
   * 
   * @param runtimeRoleNames
   *          list of runtime role names
   * @return list of logical role names, never {@code null}
   */
  List<String> getBoundLogicalRoleNames( final List<String> runtimeRoleNames );

  /**
   * Gets the logical roles bound to the given runtime roles in a particular tenant. Note that the size of the
   * incoming list might not match the size of the returned list. This is a convenience method. The same result
   * could be obtained from {@link #getRoleBindingStruct()}.
   * 
   * @param tenant
   * @param runtimeRoleNames
   *          list of runtime role names
   * @return list of logical role names, never {@code null}
   */
  List<String> getBoundLogicalRoleNames( final ITenant tenant, final List<String> runtimeRoleNames );
}
