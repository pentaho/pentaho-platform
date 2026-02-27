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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Set;

/**
 * The {@code IAuthorizationAction} interface defines the contract for actions that can be authorized via the
 * authorization system.
 * <p>
 * An action is identified by its name, which is a unique string that represents the action within the system.
 * It is recommended that the action name uses a reverse domain name notation (e.g., "com.example.action").
 * <p>
 * Accordingly, equality is based on the action name. The implementations of the {@link Object#equals(Object)} and
 * {@link Object#hashCode()} methods must ensure this behavior.
 * <p>
 * The string representation of this action, as returned by {@link Object#toString()}, should be appropriate for logging
 * and debugging purposes.
 */
public interface IAuthorizationAction {
  /**
   * Gets the name of the action.
   *
   * @return The action name, never {@code null} or empty.
   */
  @NonNull
  String getName();

  /**
   * Gets the localized display name of the action for the default locale.
   * <p>
   * This method is syntactic sugar for {@code getLocalizedDisplayName(null)}.
   *
   * @return The localized name, never {@code null} or empty.
   */
  @NonNull
  default String getLocalizedDisplayName() {
    return getLocalizedDisplayName( null );
  }

  /**
   * Gets the localized display name of the action for a specific locale.
   * <p>
   * If there is no localization available whatsoever, the action name should be returned.
   * This is ensured by the default implementation of this method.
   *
   * @param locale The locale to use for localization.
   *               The default locale is used if the string is {@code null} or empty.
   * @return The localized name, never {@code null} or empty.
   */
  @NonNull
  default String getLocalizedDisplayName( @Nullable String locale ) {
    return getName();
  }

  /**
   * Gets the localized description of the action for the default locale.
   * <p>
   * This method is syntactic sugar for {@code getLocalizedDescription(null)}.
   *
   * @return The localized description.
   */
  @Nullable
  default String getLocalizedDescription() {
    return getLocalizedDescription( null );
  }

  /**
   * Gets the localized description of the action for a specific locale.
   * <p>
   * The default implementation returns an empty string.
   *
   * @param locale The locale to use for localization.
   *               The default locale is used if the string is {@code null} or empty.
   * @return The localized description.
   */
  @Nullable
  default String getLocalizedDescription( @Nullable String locale ) {
    return "";
  }

  /**
   * Get the set of resource types that this action can be performed on / applied to.
   *
   * <h3>Self Actions</h3>
   * <p>
   * An action may be <i>intransitive</i>, meaning it does not require a resource to be performed on.
   * In this case, the action is referred to as a <i>self action</i>.
   * <p>
   * Other terms commonly used to describe this type of action include <i>complete</i>, <i>self-contained</i> and
   * <i>self-performing</i>.
   * <p>
   * Examples are "login", "logout", etc.
   * <p>
   * A <i>self action</i> has an empty set of resource types.
   *
   * <h3>Resource Actions</h3>
   * An action may also be <i>transitive</i>, meaning it requires a resource to be performed on.
   * In this case, the action is referred to as a <i>resource action</i>.
   * <p>
   * Examples are "read a file", "write to a folder", "delete a model", etc.
   * <p>
   * A <i>resource action</i> has a non-empty set of resource types, defining the types of resources it can be
   * performed on.
   *
   * <h3>Resource Types</h3>
   * <p>
   * Resource types are identified by a string and are not limited by the platform.
   * Actions can reference arbitrary resource types, whether these actions are registered by the platform or by plugins.
   *
   * @return A set of resource type names; never {@code null}.
   */
  @NonNull
  default Set<String> getResourceTypes() {
    return Set.of();
  }

  /**
   * Indicates if this action is a <i>self action</i>.
   * <p>
   * This method is syntactic sugar for checking if the action has any resource types defined.
   * <p>
   * For information on the concept of <i>self action</i>, see {@link #getResourceTypes()}.
   *
   * @return {@code true} if the action is a self action; {@code false} otherwise.
   */
  default boolean isSelfAction() {
    return getResourceTypes().isEmpty();
  }

  /**
   * Indicates if this action is a <i>resource action</i>.
   * <p>
   * This method is syntactic sugar for checking if the action has any associated resource types.
   * <p>
   * For information on the concept of <i>resource action</i>, see {@link #getResourceTypes()}.
   *
   * @return {@code true} if the action is a resource action; {@code false} otherwise.
   */
  default boolean isResourceAction() {
    return !getResourceTypes().isEmpty();
  }

  /**
   * Indicates if this action can be performed on / applies to a resource of the specified type.
   * <p>
   * This method is syntactic sugar for checking if a given resource type is part of the action's supported resource
   * types.
   *
   * @param resourceType The resource type.
   * @return {@code true} if the action can be performed on the specified resource type; {@code false} otherwise.
   * @throws IllegalArgumentException if the resource type is {@code null} or empty.
   * @see #getResourceTypes()
   */
  default boolean performsOnResourceType( @NonNull String resourceType ) {
    return getResourceTypes().contains( resourceType );
  }
}
