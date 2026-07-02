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

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.springframework.util.Assert;

import java.util.List;

/***
 * An implementation of the {@link AbstractActionRoleBindingAuthorizationRule} that retrieves role bindings from a
 * provided {@link IRoleAuthorizationPolicyRoleBindingDao}.
 */
public class ActionRoleBindingAuthorizationRule extends AbstractActionRoleBindingAuthorizationRule {

  @NonNull
  private final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  public ActionRoleBindingAuthorizationRule( @NonNull IRoleAuthorizationPolicyRoleBindingDao roleBindingDao ) {

    Assert.notNull( roleBindingDao, "Argument 'roleBindingDao' is required" );

    this.roleBindingDao = roleBindingDao;
  }

  @Override
  protected boolean hasRoleActionBinding( @NonNull IAuthorizationRole role, @NonNull String actionName ) {
    return roleBindingDao.getBoundLogicalRoleNames( List.of( role.getName() ) ).contains( actionName );
  }
}
