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

import java.util.Set;

/**
 * Represents a Logical Role name used by some IAuthorizationPolicy implementations. Also known as Action-Based Security
 */
public interface IAuthorizationAction {
  /**
   * Gets the name of the action.
   *
   * @return The action name.
   */
  String getName();

  /**
   * Gets the localized display name of the action for the default locale.
   * <p>
   * This method is syntactic sugar for {@code getLocalizedDisplayName(null)}.
   *
   * @return The localized name.
   */
  default String getLocalizedDisplayName() {
    return getLocalizedDisplayName( null );
  }

  /**
   * Gets the localized display name of the action for a specific locale.
   *
   * @param locale The locale to use for localization.
   *               The default locale is used if the string is {@code null} or empty.
   * @return The localized name.
   */
  String getLocalizedDisplayName( String locale );

  /**
   * Gets the localized description of the action for the default locale.
   * <p>
   * This method is syntactic sugar for {@code getLocalizedDescription(null)}.
   *
   * @return The localized description.
   */
  default String getLocalizedDescription() {
    return getLocalizedDescription( null );
  }

  /**
   * Gets the localized description of the action for a specific locale.
   *
   * @param locale The locale to use for localization.
   *               The default locale is used if the string is {@code null} or empty.
   * @return The localized description.
   */
  String getLocalizedDescription( String locale );

  /**
   * Get the set of object types that this action can be executed on / applied to.
   *
   * <h3>Self Actions</h3>
   * <p>
   * An action may be <i>intransitive</i>, meaning it does not require an object to be executed on.
   * In this case, the action is referred to as a <i>self action</i>.
   * <p>
   * Other terms commonly used to describe this type of action include <i>complete</i>, <i>self-contained</i> and
   * <i>self-executing</i>.
   * <p>
   * Examples are "login", "logout", etc.
   * <p>
   * A <i>self action</i> has an empty set of object types.
   *
   * <h3>Object Actions</h3>
   * An action may also be <i>transitive</i>, meaning it requires an object to be executed on.
   * In this case, the action is referred to as an <i>object action</i>.
   * <p>
   * Examples are "read a file", "write to a folder", "delete a model", etc.
   * <p>
   * An <i>object action</i> has a non-empty set of object types, defining the types of objects it can be executed on.
   *
   * <h3>Object Types</h3>
   * <p>
   * Object types are identified by a string and are not limited by the platform.
   * Actions can reference arbitrary object types, whether these actions are registered by the platform or by plugins.
   *
   * @return A set of object type names; never {@code null}.
   */
  @NonNull
  default Set<String> getObjectTypes() {
    return Set.of();
  };

  /**
   * Indicates if this action is a <i>self action</i>.
   * <p>
   * This method is syntactic sugar for checking if the action has any object types defined.
   * <p>
   * For information on the concept of <i>self action</i>, see {@link #getObjectTypes()}.
   *
   * @return {@code true} if the action is a self action; {@code false} otherwise.
   */
  default boolean isSelfAction() {
    return getObjectTypes().isEmpty();
  }

  /**
   * Indicates if this action is an <i>object action</i>.
   * <p>
   * This method is syntactic sugar for checking if the action has any object types defined.
   * <p>
   * For information on the concept of <i>object action</i>, see {@link #getObjectTypes()}.
   *
   * @return {@code true} if the action is an object action; {@code false} otherwise.
   */
  default boolean isObjectAction() {
    return !getObjectTypes().isEmpty();
  }

  /**
   * Indicates if this action can be executed on / applies to an object of the specified type.
   * <p>
   * This method is syntactic sugar for checking if a given object type is part of the action's supported object types.
   *
   * @param objectType The object type.
   * @return {@code true} if the action can be executed on the specified object type; {@code false} otherwise.
   * @throws IllegalArgumentException if the object type is {@code null} or empty.
   * @see #getObjectTypes()
   */
  default boolean executesOnObjectType( @NonNull String objectType ) {
    return getObjectTypes().contains( objectType );
  }
}
