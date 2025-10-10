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

package org.pentaho.platform.api.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IAuthorizationAction;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The {@code IAuthorizationActionService} service provides access to authorization actions registered in the platform.
 * <p>
 * The {@link #getActions()} operation returns a stream of all available authorization actions. This stream can be
 * efficiently further filtered for specific types of actions, such as self-actions or resource-actions.
 * <p>
 * Operations such as {@link #getSelfActions()} and {@link #getResourceActions()} provide convenient access to
 * self-actions and resource-actions, respectively.
 * <p>
 * Operations such as {@link #getSystemActions()} and {@link #getPluginActions()} provide access to system-defined and
 * plugin-defined actions, respectively. However, note that it is implementation-dependent how it is determined whether
 * an action is system-defined or plugin-defined.
 */
public interface IAuthorizationActionService {
  /**
   * Gets a stream of all authorization actions available in the platform.
   *
   * @return A stream of {@link IAuthorizationAction}.
   */
  @NonNull
  Stream<IAuthorizationAction> getActions();

  // TODO: tests
  /**
   * Gets a stream of authorization actions whose namespace is contained or equal to a given action namespace.
   *
   * @param actionNamespace The namespace of the actions to filter by. If {@code null} or empty, all actions are
   *                        returned.
   * @return A stream of {@link IAuthorizationAction} that match the specified namespace.
   */
  @NonNull
  default Stream<IAuthorizationAction> getActions( @Nullable String actionNamespace ) {
    if ( StringUtils.isEmpty( actionNamespace ) ) {
      return getActions();
    }

    String actionNamespacePrefix = actionNamespace.endsWith( "." ) ? actionNamespace : ( actionNamespace + "." );

    return getActions()
      .filter( action -> action.getName().startsWith( actionNamespacePrefix ) );
  }

  /**
   * Gets an authorization action given its name.
   * <p>
   * The default implementation of this method finds the action in the stream returned by {@link #getActions()}.
   * Implementations may override this method to provide a more efficient lookup.
   *
   * @param actionName The name of the authorization action.
   * @return An optional of {@link IAuthorizationAction}.
   * @throws IllegalArgumentException if the action name is {@code null} or empty.
   */
  @NonNull
  default Optional<IAuthorizationAction> getAction( @NonNull String actionName ) {
    if ( StringUtils.isEmpty( actionName ) ) {
      throw new IllegalArgumentException( "Argument `actionName` cannot be null or empty." );
    }

    return getActions()
      .filter( action -> action.getName().equals( actionName ) )
      .findFirst();
  }

  // region System and Plugin Actions
  /**
   * Gets a stream of all system-defined authorization actions.
   * <p>
   * It is implementation-dependent how it is determined whether an action is system-defined or plugin-defined.
   *
   * @return A stream of {@link IAuthorizationAction} that are defined by the system.
   */
  @NonNull
  Stream<IAuthorizationAction> getSystemActions();

  /**
   * Gets a stream of all plugin-defined authorization actions.
   * <p>
   * It is implementation-dependent how it is determined whether an action is system-defined or plugin-defined.
   *
   * @return A stream of {@link IAuthorizationAction} that are defined by plugins.
   */
  @NonNull
  Stream<IAuthorizationAction> getPluginActions();

  /**
   * Gets a stream of authorization actions defined by a specific plugin.
   * <p>
   * It is implementation-dependent how it is determined whether an action is system-defined or plugin-defined.
   *
   * @param pluginId The ID of the plugin. If the plugin is not defined, an empty stream is returned.
   * @return A stream of {@link IAuthorizationAction} that are defined by the specified plugin.
   * @throws IllegalArgumentException if the pluginId is {@code null} or empty.
   */
  @NonNull
  Stream<IAuthorizationAction> getPluginActions( @NonNull String pluginId );
  // endregion

  // region Self and Resource Actions
  /**
   * Gets a stream of authorization actions that are self-actions, as determined by
   * {@link IAuthorizationAction#isSelfAction()}.
   * <p>
   * This method is syntactic sugar for {@code getActions().filter(IAuthorizationAction::isSelfAction)}.
   *
   * @return A stream of {@link IAuthorizationAction} that are self-actions.
   */
  @NonNull
  default Stream<IAuthorizationAction> getSelfActions() {
    return getActions().filter( IAuthorizationAction::isSelfAction );
  }

  /**
   * Gets a stream of authorization actions that are resource-actions, as determined by
   * {@link IAuthorizationAction#performsOnResourceType(String)}.
   * <p>
   * This method is syntactic sugar for {@code getActions().filter(IAuthorizationAction::isResourceAction)}.
   *
   * @return A stream of {@link IAuthorizationAction} that are resource-actions.
   */
  @NonNull
  default Stream<IAuthorizationAction> getResourceActions() {
    return getActions().filter( IAuthorizationAction::isResourceAction );
  }

  /**
   * Gets a stream of authorization actions that can be performed on resources of a specific type.
   * <p>
   * This method is syntactic sugar for {@code getActions().filter(action -> action.performsOnResourceType(resourceType))}.
   *
   * @param resourceType The resource type.
   * @return A stream of {@link IAuthorizationAction} that can be performed on resources of the specified type.
   * @throws IllegalArgumentException if the resource type is {@code null} or empty.
   */
  @NonNull
  default Stream<IAuthorizationAction> getResourceActions( @NonNull String resourceType ) {
    if ( StringUtils.isEmpty( resourceType ) ) {
      throw new IllegalArgumentException( "Argument `resourceType` cannot be null or empty." );
    }

    return getActions().filter( action -> action.performsOnResourceType( resourceType ) );
  }
  // endregion
}
