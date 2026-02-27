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
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.engine.core.system.objfac.spring.Const;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The {@code MemoryAuthorizationActionService} provides access to authorization actions stored in memory.
 * The actions are received as Pentaho System object references, at construction.
 */
public class MemoryAuthorizationActionService implements IAuthorizationActionService {

  @NonNull
  private final List<IAuthorizationAction> actions;

  @NonNull
  private final Map<String, IAuthorizationAction> actionsByName;

  @NonNull
  private final List<IAuthorizationAction> systemActions;

  @NonNull
  private final List<IAuthorizationAction> pluginActions;

  @NonNull
  private final Map<String, List<IAuthorizationAction>> actionsByPlugin;

  public MemoryAuthorizationActionService(
    @NonNull Supplier<Stream<IPentahoObjectReference<IAuthorizationAction>>> authorizationActionsSupplier ) {

    Assert.notNull( authorizationActionsSupplier, "Argument 'authorizationActionsSupplier' is required" );

    actions = new ArrayList<>();
    actionsByName = new HashMap<>();
    systemActions = new ArrayList<>();
    pluginActions = new ArrayList<>();
    actionsByPlugin = new HashMap<>();

    authorizationActionsSupplier
      .get()
      .filter( distinctByKey( IPentahoObjectReference::getObject ) )
      .filter( ref -> ref.getObject() != null && StringUtils.isNotEmpty( ref.getObject().getName() ) )
      .forEach( ref -> {
        var action = ref.getObject();

        actions.add( action );
        actionsByName.put( action.getName(), action );

        String pluginId = getPluginId( ref );
        if ( StringUtils.isNotEmpty( pluginId ) ) {
          pluginActions.add( action );
          actionsByPlugin
            .computeIfAbsent( pluginId, k -> new ArrayList<>() )
            .add( action );
        } else {
          systemActions.add( action );
        }
      } );
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

  @Nullable
  private static String getPluginId( @NonNull IPentahoObjectReference<IAuthorizationAction> actionReference ) {
    return (String) actionReference.getAttributes().get( Const.PUBLISHER_PLUGIN_ID_ATTRIBUTE );
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getActions() {
    return actions.stream();
  }

  @NonNull
  @Override
  public Optional<IAuthorizationAction> getAction( @NonNull String actionName ) {
    Assert.hasLength( actionName, "Argument 'actionName' is required" );

    return Optional.ofNullable( actionsByName.get( actionName ) );
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getSystemActions() {
    return systemActions.stream();
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getPluginActions() {
    return pluginActions.stream();
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getPluginActions( @NonNull String pluginId ) {
    Assert.hasLength( pluginId, "Argument 'pluginId' is required" );

    return actionsByPlugin.getOrDefault( pluginId, List.of() ).stream();
  }
}
