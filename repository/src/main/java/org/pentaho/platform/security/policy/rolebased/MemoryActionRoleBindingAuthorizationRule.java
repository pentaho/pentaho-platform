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

import java.util.Map;
import java.util.Set;

/**
 * An in-memory implementation of {@link AbstractActionRoleBindingAuthorizationRule} that allows for overriding
 * persisted role bindings with a provided map of role to action bindings.
 */
public class MemoryActionRoleBindingAuthorizationRule extends AbstractActionRoleBindingAuthorizationRule {

  @NonNull
  private final Map<String, Set<String>> overrideRoleBindings;

  /**
   * Constructs the rule with an action role bindings map.
   *
   * @param overrideRoleBindings The action role bindings map.
   */
  public MemoryActionRoleBindingAuthorizationRule( @NonNull Map<String, Set<String>> overrideRoleBindings ) {

    Assert.notNull( overrideRoleBindings, "Argument 'overrideRoleBindings' is required" );

    this.overrideRoleBindings = overrideRoleBindings;
  }

  @Override
  protected boolean hasRoleActionBinding( @NonNull IAuthorizationRole role, @NonNull String actionName ) {
    return overrideRoleBindings.getOrDefault( role.getName(), Set.of() ).contains( actionName );
  }
}
