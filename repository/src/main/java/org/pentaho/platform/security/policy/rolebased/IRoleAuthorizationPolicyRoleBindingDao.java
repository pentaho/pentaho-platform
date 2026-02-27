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


package org.pentaho.platform.security.policy.rolebased;

import org.pentaho.platform.api.mt.ITenant;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
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

  /**
   * This was added to decouple {@link org.apache.jackrabbit.core.security.authorization.acl.PentahoEntryCollector}
   *
   * Gets the logical roles bound to the given runtime roles. Note that the size of the incoming list might not
   * match the size of the returned list. This is a convenience method. The same result could be obtained from
   * {@link #getRoleBindingStruct()}.
   *
   * @param runtimeRoleNames
   *          list of runtime role names
   * @return list of logical role names, never {@code null}
   */
  List<String> getBoundLogicalRoleNames( final Session session, final List<String> runtimeRoleNames ) throws RepositoryException;

  /**
   * This was added to decouple {@link org.apache.jackrabbit.core.security.authorization.acl.PentahoEntryCollector}
   *
   * Gets the logical roles bound to the given runtime roles in a particular tenant. Note that the size of the
   * incoming list might not match the size of the returned list. This is a convenience method. The same result
   * could be obtained from {@link #getRoleBindingStruct()}.
   *
   * @param tenant
   * @param runtimeRoleNames
   *          list of runtime role names
   * @return list of logical role names, never {@code null}
   */
  List<String> getBoundLogicalRoleNames( final Session session, final ITenant tenant, final List<String> runtimeRoleNames ) throws RepositoryException;
}
