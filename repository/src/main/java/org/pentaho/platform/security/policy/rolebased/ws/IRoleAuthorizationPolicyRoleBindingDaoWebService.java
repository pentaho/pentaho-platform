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

package org.pentaho.platform.security.policy.rolebased.ws;

import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;

import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * JAX-WS-safe version of {@link IRoleAuthorizationPolicyRoleBindingDao}. In this case, nothing is different but it
 * keeps JAX-WS annotations out of the core classes.
 * 
 * @author mlowery
 */
@WebService
public interface IRoleAuthorizationPolicyRoleBindingDaoWebService {

  @XmlJavaTypeAdapter( RoleBindingStructAdapter.class )
  RoleBindingStruct getRoleBindingStruct( final String locale );

  /**
   * Gets a struct-like object that contains everything known by this DAO. This is a batch operation provided for
   * UIs.
   * 
   * @param locale
   *          locale, possibly {@code null}
   * @return role binding struct
   */

  @XmlJavaTypeAdapter( RoleBindingStructAdapter.class )
  RoleBindingStruct getRoleBindingStructForTenant( final Tenant tenant, final String locale );

  /**
   * Sets the bindings for the given runtime role. All other bindings for this runtime role are removed.
   * 
   * @param runtimeRoleName
   *          runtime role name
   * @param logicalRoleNames
   *          list of logical role names
   */
  void setRoleBindings( final String runtimeRoleName, final List<String> logicalRolesNames );

  void
  setRoleBindingsForTenant( final Tenant tenant, final String runtimeRoleName, final List<String> logicalRolesNames );

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

  List<String> getBoundLogicalRoleNamesForTenant( final Tenant tenant, final List<String> runtimeRoleNames );

}
