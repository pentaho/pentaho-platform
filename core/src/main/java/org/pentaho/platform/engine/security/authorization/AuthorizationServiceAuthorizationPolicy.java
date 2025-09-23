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
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationPrincipal;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationService;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The {@code AuthorizationServiceAuthorizationPolicy} is an implementation of the {@link IAuthorizationPolicy} backed
 * by an instance of {@link IAuthorizationService}.
 *
 * @deprecated since 11.0, for removal in a future release.
 */
@SuppressWarnings( { "removal", "DeprecatedIsStillUsed" } )
@Deprecated( since = "11.0", forRemoval = true )
public class AuthorizationServiceAuthorizationPolicy implements IAuthorizationPolicy {

  @NonNull
  private final IAuthorizationActionService authorizationActionService;

  @NonNull
  private final IAuthorizationService authorizationService;

  @NonNull
  private final Supplier<IAuthorizationPrincipal> currentPrincipalSupplier;

  public AuthorizationServiceAuthorizationPolicy(
    @NonNull IAuthorizationActionService authorizationActionService,
    @NonNull IAuthorizationService authorizationService,
    @NonNull Supplier<IAuthorizationPrincipal> currentPrincipalSupplier ) {

    Assert.notNull( authorizationActionService, "Argument 'authorizationActionService' is required" );
    Assert.notNull( authorizationService, "Argument 'authorizationService' is required" );
    Assert.notNull( currentPrincipalSupplier, "Argument 'currentPrincipalSupplier' is required" );

    this.authorizationActionService = authorizationActionService;
    this.authorizationService = authorizationService;
    this.currentPrincipalSupplier = currentPrincipalSupplier;
  }

  @NonNull
  private IAuthorizationPrincipal getCurrentPrincipal() {
    return Objects.requireNonNull( currentPrincipalSupplier.get() );
  }

  @Override
  public boolean isAllowed( String actionName ) {
    // If the action is not defined (possibly, no longer), then just deny access.
    Optional<IAuthorizationAction> actionOptional = authorizationActionService.getAction( actionName );

    // Orphan actions are not allowed!
    return actionOptional.isPresent() && isAllowed( actionOptional.get() );
  }

  private boolean isAllowed( @NonNull IAuthorizationAction action ) {
    return authorizationService
      .authorize( new AuthorizationRequest( getCurrentPrincipal(), action ) )
      .isGranted();
  }

  @Override
  public List<String> getAllowedActions( String actionNamespace ) {
    return authorizationActionService.getActions( actionNamespace )
      .filter( this::isAllowed )
      .map( IAuthorizationAction::getName )
      .collect( Collectors.toList() );
  }
}
