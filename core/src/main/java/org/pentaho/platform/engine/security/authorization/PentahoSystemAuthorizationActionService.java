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


package org.pentaho.platform.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.spring.Const;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The {@code PentahoSystemAuthorizationActionService} provides access to authorization actions registered in the
 * Pentaho System at any given time.
 */
public class PentahoSystemAuthorizationActionService implements IAuthorizationActionService {
  @NonNull
  private final Supplier<Stream<IPentahoObjectReference<IAuthorizationAction>>> authorizationActionsSupplier;

  public PentahoSystemAuthorizationActionService() {
    this( PentahoSystemAuthorizationActionService::getPentahoSystemActionObjectReferences );
  }

  public PentahoSystemAuthorizationActionService(
    @NonNull Supplier<Stream<IPentahoObjectReference<IAuthorizationAction>>> authorizationActionsSupplier ) {
    this.authorizationActionsSupplier = Objects.requireNonNull( authorizationActionsSupplier );
  }

  // region Helper methods

  /**
   * Gets all authorization action object references registered in the Pentaho System.
   * <p>
   * This method is used to retrieve all actions, including duplicates, as registered in the system.
   *
   * @return A stream of all authorization action object references.
   */
  private static Stream<IPentahoObjectReference<IAuthorizationAction>> getPentahoSystemActionObjectReferences() {
    return PentahoSystem.getObjectReferences( IAuthorizationAction.class, null ).stream();
  }

  /**
   * Gets all authorization actions registered in the Pentaho System, while making sure that no duplicate actions
   * are returned.
   * <p>
   * An action is a duplicate action if it has the same {@link IAuthorizationAction#getName() name} as another action.
   * The action registered with the highest {@link IPentahoObjectReference#getRanking() ranking} is considered the
   * original one, and any others duplicates.
   *
   * @return A stream of authorization action object references ensured to have no duplicates.
   */
  protected Stream<IPentahoObjectReference<IAuthorizationAction>> getActionObjectReferences() {
    return Objects.requireNonNull( authorizationActionsSupplier.get() )
      .filter( distinctByKey( PentahoSystemAuthorizationActionService::getActionObjectReferenceKey ) );
  }

  /**
   * Gets the key to use to compare two action object references.
   * <p>
   * Returns the name of the action, which is should be unique for each action.
   * <p>
   * Unfortunately, it is not possible to enforce the implementation of {@link Object#equals(Object)}
   * on an existing interface like {@link IAuthorizationAction}.
   *
   * @param actionReference The action object reference to get the key for.
   */
  private static String getActionObjectReferenceKey(
    @NonNull IPentahoObjectReference<IAuthorizationAction> actionReference ) {
    return actionReference.getObject().getName();
  }

  /**
   * Creates a predicate that filters out duplicate elements based on a key extracted from each element.
   * <p>
   * This method uses a concurrent set to track seen keys, ensuring that only the first occurrence of each key is kept.
   * <p>
   * Any elements with {@code null} keys are filtered out.
   *
   * @param keyExtractor A function to extract the key from each element.
   * @param <T>          The type of the elements in the stream.
   * @return A predicate that returns true for the first occurrence of each key and false for duplicates.
   */
  private static <T> Predicate<T> distinctByKey( Function<? super T, ?> keyExtractor ) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> {
      Object key = keyExtractor.apply( t );
      return key != null && seen.add( key );
    };
  }

  /**
   * Determines is a given action object reference is of a plugin action.
   *
   * @param actionReference The action object reference to check.
   * @return {@code true} if the action reference is a plugin action; {@code false} otherwise.
   */
  protected boolean isPluginActionObjectReference(
    @NonNull IPentahoObjectReference<IAuthorizationAction> actionReference ) {
    return actionReference.getAttributes().containsKey( Const.PUBLISHER_PLUGIN_ID_ATTRIBUTE );
  }

  /**
   * Determines if a given action object reference is of a plugin action with the specified plugin ID.
   *
   * @param actionReference The action object reference to check.
   * @param pluginId        The ID of the plugin to check against.
   * @return {@code true} if the action reference is a plugin action for the specified plugin ID; {@code false}
   * otherwise.
   */
  protected boolean isPluginActionObjectReference(
    @NonNull IPentahoObjectReference<IAuthorizationAction> actionReference,
    @NonNull String pluginId ) {
    return pluginId.equals( actionReference.getAttributes().get( Const.PUBLISHER_PLUGIN_ID_ATTRIBUTE ) );
  }

  /**
   * Determines if a given action object reference is of a system action.
   * <p>
   * A system action is one that is not associated with any plugin, meaning it does not have the
   * {@link Const#PUBLISHER_PLUGIN_ID_ATTRIBUTE} attribute set.
   *
   * @param actionReference The action object reference to check.
   * @return {@code true} if the action reference is a system action; {@code false} otherwise.
   */
  protected boolean isSystemActionObjectReference(
    @NonNull IPentahoObjectReference<IAuthorizationAction> actionReference ) {
    return !isPluginActionObjectReference( actionReference );
  }
  // endregion

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getActions() {
    return getActionObjectReferences()
      .map( IPentahoObjectReference::getObject );
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getSystemActions() {
    return getActionObjectReferences()
      .filter( this::isSystemActionObjectReference )
      .map( IPentahoObjectReference::getObject );
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getPluginActions() {
    return getActionObjectReferences()
      .filter( this::isPluginActionObjectReference )
      .map( IPentahoObjectReference::getObject );
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getPluginActions( @NonNull String pluginId ) {
    if ( StringUtils.isEmpty( pluginId ) ) {
      throw new IllegalArgumentException( "Argument `pluginId` cannot be null or empty." );
    }

    return getActionObjectReferences()
      .filter( actionReference -> isPluginActionObjectReference( actionReference, pluginId ) )
      .map( IPentahoObjectReference::getObject );
  }
}
