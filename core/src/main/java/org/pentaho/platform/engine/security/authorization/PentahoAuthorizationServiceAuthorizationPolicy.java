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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationService;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The {@code AuthorizationServiceAuthorizationPolicy} is an implementation of the {@link IAuthorizationPolicy}
 * backed by an instance of {@link IAuthorizationService}.
 */
public class PentahoAuthorizationServiceAuthorizationPolicy implements IAuthorizationPolicy {

  @NonNull
  private final IAuthorizationActionService authorizationActionService;

  @NonNull
  private final IAuthorizationService authorizationService;

  @NonNull
  private final Supplier<Authentication> currentAuthenticationSupplier;

  public PentahoAuthorizationServiceAuthorizationPolicy(
    @NonNull IAuthorizationActionService authorizationActionService,
    @NonNull IAuthorizationService authorizationService ) {
    this(
      authorizationActionService,
      authorizationService,
      PentahoAuthorizationServiceAuthorizationPolicy::getSpringSessionContextAuthentication );
  }

  public PentahoAuthorizationServiceAuthorizationPolicy(
    @NonNull IAuthorizationActionService authorizationActionService,
    @NonNull IAuthorizationService authorizationService,
    @NonNull Supplier<Authentication> currentAuthenticationSupplier ) {

    Assert.notNull( authorizationActionService, "Argument 'authorizationActionService' is required" );
    Assert.notNull( authorizationService, "Argument 'authorizationService' is required" );
    Assert.notNull( currentAuthenticationSupplier, "Argument 'currentAuthenticationSupplier' is required" );

    this.authorizationActionService = authorizationActionService;
    this.authorizationService = authorizationService;
    this.currentAuthenticationSupplier = currentAuthenticationSupplier;
  }

  @NonNull
  private static Authentication getSpringSessionContextAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Assert.notNull( authentication, "No authentication found in the security context" );

    return authentication;
  }

  @NonNull
  protected Authentication getCurrentAuthentication() {
    return currentAuthenticationSupplier.get();
  }

  @Override
  public boolean isAllowed( String actionName ) {
    // If the action is not defined (possibly, no longer), then just deny access.
    Optional<IAuthorizationAction> actionOptional = authorizationActionService.getAction( actionName );

    // Orphan actions are not allowed!
    return actionOptional.isPresent() && isAllowed( actionOptional.get() );
  }

  protected boolean isAllowed( @NonNull IAuthorizationAction action ) {
    IAuthorizationRequest request = new AuthorizationRequest(
      new SpringAuthenticationAuthorizationUser( getCurrentAuthentication() ),
      action );

    return authorizationService.authorize( request ).isGranted();
  }

  @Override
  public List<String> getAllowedActions( String actionNamespace ) {
    return authorizationActionService.getActions( actionNamespace )
      .filter( this::isAllowed )
      .map( IAuthorizationAction::getName )
      .collect( Collectors.toList() );
  }
}
