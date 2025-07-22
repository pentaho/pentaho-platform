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

package org.pentaho.platform.engine.security.authorization.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.pentaho.platform.api.engine.security.authorization.resources.IAuthorizationResource;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.resources.ResourceAuthorizationRequest;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.function.Supplier;

public class AuthorizationRequestBuilder {
  @NonNull
  private final IAuthorizationActionService authorizationActionService;

  @NonNull
  private final Supplier<IAuthorizationUser> currentUserSupplier;

  public AuthorizationRequestBuilder(
    @NonNull IAuthorizationActionService authorizationActionService,
    @NonNull Supplier<IAuthorizationUser> currentUserSupplier
  ) {
    Assert.notNull( authorizationActionService, "Argument 'authorizationActionService' is required" );
    Assert.notNull( currentUserSupplier, "Argument 'currentUserSupplier' is required" );

    this.authorizationActionService = authorizationActionService;
    this.currentUserSupplier = currentUserSupplier;
  }

  @NonNull
  protected IAuthorizationUser getCurrentUser() {
    return Objects.requireNonNull( currentUserSupplier.get() );
  }

  @NonNull
  protected IAuthorizationAction resolveAction( @NonNull String actionName ) {
    return authorizationActionService.getAction( actionName )
      .orElseThrow( () -> new IllegalArgumentException(
        String.format(
          "There is no registered action with name '%s'.",
          actionName ) ) );
  }

  @NonNull
  public WithActionBuilder action( @NonNull IAuthorizationAction action ) {
    return new WithActionBuilder( action );
  }

  @NonNull
  public WithActionBuilder action( @NonNull String actionName ) {
    return action( resolveAction( actionName ) );
  }

  public class WithActionBuilder {
    @NonNull
    private final IAuthorizationAction action;

    @Nullable
    private IAuthorizationUser user;

    protected WithActionBuilder( @NonNull IAuthorizationAction action ) {
      this.action = Objects.requireNonNull( action );
    }

    @NonNull
    public WithActionBuilder user( @Nullable IAuthorizationUser user ) {
      this.user = user;
      return this;
    }

    @NonNull
    protected IAuthorizationUser getUser() {
      return user != null ? user : getCurrentUser();
    }

    @NonNull
    public WithResourceBuilder resource( @NonNull IAuthorizationResource resource ) {
      return new WithResourceBuilder( action, getUser(), resource );
    }

    @NonNull
    public IAuthorizationRequest build() {
      return new AuthorizationRequest( getUser(), action );
    }
  }

  public static class WithResourceBuilder {
    @NonNull
    private final IAuthorizationAction action;

    @NonNull
    private final IAuthorizationUser user;

    @NonNull
    private final IAuthorizationResource resource;

    protected WithResourceBuilder( @NonNull IAuthorizationAction action,
                                   @NonNull IAuthorizationUser user,
                                   @NonNull IAuthorizationResource resource ) {
      this.user = user;
      this.action = action;
      this.resource = resource;
    }

    @NonNull
    public IResourceAuthorizationRequest build() {
      return new ResourceAuthorizationRequest( user, action, resource );
    }
  }
}
