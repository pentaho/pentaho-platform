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
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The {@code PentahoSystemAuthorizationActionService} provides access to authorization actions registered in the
 * Pentaho System. It listens to plugin changes, and updates its internal action list accordingly.
 */
public class PentahoSystemAuthorizationActionService implements IAuthorizationActionService {

  private static class PentahoSystemAuthorizationActionSupplier
    implements Supplier<Stream<IPentahoObjectReference<IAuthorizationAction>>> {

    @Override
    public Stream<IPentahoObjectReference<IAuthorizationAction>> get() {
      return PentahoSystem.getObjectReferences( IAuthorizationAction.class, null ).stream();
    }
  }

  @NonNull
  private MemoryAuthorizationActionService memoryService;

  public PentahoSystemAuthorizationActionService( @NonNull IPluginManager pluginManager ) {
    this( pluginManager, new PentahoSystemAuthorizationActionSupplier() );
  }

  public PentahoSystemAuthorizationActionService(
    @NonNull IPluginManager pluginManager,
    @NonNull Supplier<Stream<IPentahoObjectReference<IAuthorizationAction>>> authorizationActionsSupplier ) {

    Assert.notNull( authorizationActionsSupplier, "Argument 'authorizationActionsSupplier' is required" );

    this.memoryService = new MemoryAuthorizationActionService( authorizationActionsSupplier );

    // Update the in-memory actions whenever plugins are changed.
    pluginManager.addPluginManagerListener( () ->
      this.memoryService = new MemoryAuthorizationActionService( authorizationActionsSupplier ) );
  }


  @NonNull
  @Override
  public Stream<IAuthorizationAction> getActions() {
    return memoryService.getActions();
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getActions( @Nullable String actionNamespace ) {
    return memoryService.getActions( actionNamespace );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationAction> getAction( @NonNull String actionName ) {
    return memoryService.getAction( actionName );
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getSystemActions() {
    return memoryService.getSystemActions();
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getPluginActions() {
    return memoryService.getPluginActions();
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getPluginActions( @NonNull String pluginId ) {
    return memoryService.getPluginActions( pluginId );
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getSelfActions() {
    return memoryService.getSelfActions();
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getResourceActions() {
    return memoryService.getResourceActions();
  }

  @NonNull
  @Override
  public Stream<IAuthorizationAction> getResourceActions( @NonNull String resourceType ) {
    return memoryService.getResourceActions( resourceType );
  }
}
