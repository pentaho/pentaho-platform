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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationPrincipal;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.pentaho.platform.api.engine.security.authorization.resources.IAuthorizationResource;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.resources.GenericAuthorizationResource;
import org.pentaho.platform.engine.security.authorization.core.resources.ResourceAuthorizationRequest;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * The {@code AuthorizationRequestBuilder} class is a builder for creating authorization requests.
 * It allows specifying the action, principal (user, role, etc.), and resource for the authorization request.
 * <p>
 * When the principal is not specified, the request is built with the current principal,
 * obtained from a configured current principal supplier.
 */
public class AuthorizationRequestBuilder {
  @NonNull
  private final IAuthorizationActionService authorizationActionService;

  @NonNull
  private final Supplier<IAuthorizationPrincipal> currentPrincipalSupplier;

  /**
   * Constructs an {@code AuthorizationRequestBuilder} with the specified authorization action service and current user
   * supplier.
   *
   * @param authorizationActionService The service to resolve actions by name.
   * @param currentPrincipalSupplier   A supplier that provides the current principal.
   * @throws IllegalArgumentException If either the <code>authorizationActionService</code> or
   *                                  <code>currentPrincipalSupplier</code> parameters are <code>null</code>.
   */
  public AuthorizationRequestBuilder(
    @NonNull IAuthorizationActionService authorizationActionService,
    @NonNull Supplier<IAuthorizationPrincipal> currentPrincipalSupplier
  ) {
    Assert.notNull( authorizationActionService, "Argument 'authorizationActionService' is required" );
    Assert.notNull( currentPrincipalSupplier, "Argument 'currentPrincipalSupplier' is required" );

    this.authorizationActionService = authorizationActionService;
    this.currentPrincipalSupplier = currentPrincipalSupplier;
  }

  /**
   * Gets the current principal, from the configured current principal supplier.
   *
   * @return The current principal.
   */
  @NonNull
  protected IAuthorizationPrincipal getCurrentPrincipal() {
    return Objects.requireNonNull( currentPrincipalSupplier.get() );
  }

  /**
   * Resolves an action by its name using the configured {@link IAuthorizationActionService}.
   * <p>
   * If the given action name is not registered in the action service, a placeholder action instance is created and
   * returned. This allows the authorization service to gracefully deny the request for actions that are not registered,
   * without throwing an exception when building the request.
   *
   * @param actionName The name of the action to resolve.
   * @return The resolved action, or a dummy action if the action is not registered.
   * @throws IllegalArgumentException If the <code>actionName</code> parameter is <code>null</code> or empty.
   */
  @NonNull
  protected IAuthorizationAction resolveAction( @NonNull String actionName ) {
    return authorizationActionService.getAction( actionName )
      // Create and return a dummy/placeholder action.
      // No way to know if the action existed, and if it was self or resource, and for which resource types.
      .orElseGet( () -> new UndefinedAuthorizationAction( actionName ) );
  }

  /**
   * Starts building an authorization request with the specified action.
   * @param action The action to be authorized.
   * @return A builder for the authorization request.
   * @throws IllegalArgumentException If the <code>action</code> parameter is <code>null</code>.
   * @see #action(String)
   */
  @NonNull
  public WithActionBuilder action( @NonNull IAuthorizationAction action ) {
    return new WithActionBuilder( action );
  }

  /**
   * Starts building an authorization request with the specified action name.
   * <p>
   * If the action is not registered, a placeholder action instance is created and returned.
   * This allows the authorization service to gracefully handle requests for actions that are not registered,
   * without throwing an exception when building the request.
   *
   * @param actionName The name of the action to be authorized.
   * @return A builder for the authorization request.
   * @throws IllegalArgumentException If the <code>actionName</code> parameter is <code>null</code> or empty.
   */
  @NonNull
  public WithActionBuilder action( @NonNull String actionName ) {
    return action( resolveAction( actionName ) );
  }

  // region Step Builder classes

  /**
   * The {@code WithActionBuilder} class is a builder step for creating authorization requests with a specific action.
   * It allows specifying the principal and resource for the authorization request.
   */
  public class WithActionBuilder {
    @NonNull
    private final IAuthorizationAction action;

    @Nullable
    private IAuthorizationPrincipal principal;

    protected WithActionBuilder( @NonNull IAuthorizationAction action ) {
      Assert.notNull( action, "Argument 'action' is required" );

      this.action = action;
    }

    /**
     * Configures the authorization request with a specific user.
     *
     * @param user The user for whom the authorization is being evaluated; <code>null</code> to use the current user.
     * @return A builder for the authorization request.
     */
    @NonNull
    public WithActionBuilder user( @Nullable IAuthorizationUser user ) {
      this.principal = user;
      return this;
    }

    /**
     * Configures the authorization request with a specific role.
     *
     * @param role The role for which the authorization is being evaluated.
     * @return A builder for the authorization request.
     */
    @NonNull
    public WithActionBuilder role( @Nullable IAuthorizationRole role ) {
      this.principal = role;
      return this;
    }

    /**
     * Configures the authorization request with a specific role name.
     * A new {@link AuthorizationRole} instance is created with the given role name.
     *
     * @param roleName The name of the role for which the authorization is being evaluated.
     * @return A builder for the authorization request.
     * @throws IllegalArgumentException If the <code>roleName</code> parameter is <code>null</code> or empty.
     */
    @NonNull
    public WithActionBuilder role( @NonNull String roleName ) {
      this.principal = new AuthorizationRole( roleName );
      return this;
    }

    /**
     * Configures the authorization request with a specific principal.
     *
     * @param principal The principal (user, role, etc.) for whom the authorization is being evaluated;
     *                  <code>null</code> to use the current principal.
     * @return A builder for the authorization request.
     */
    @NonNull
    public WithActionBuilder principal( @Nullable IAuthorizationPrincipal principal ) {
      this.principal = principal;
      return this;
    }

    /**
     * Gets the principal for the authorization request.
     * If no principal is defined, it returns the current principal.
     *
     * @return The principal for the authorization request.
     */
    @NonNull
    protected IAuthorizationPrincipal getPrincipal() {
      return principal != null ? principal : getCurrentPrincipal();
    }

    /**
     * Configures the authorization request with an {@link IAuthorizationResource arbitrary resource}.
     *
     * @param resource The resource to be authorized.
     * @return A builder for the resource authorization request.
     */
    @NonNull
    public WithResourceBuilder resource( @NonNull IAuthorizationResource resource ) {
      return new WithResourceBuilder( action, getPrincipal(), resource );
    }

    /**
     * Configures the authorization request with a {@link GenericAuthorizationResource generic resource} with the
     * specified type and id.
     * @param type The type of the resource.
     * @param id The id of the resource.
     * @return A builder for the resource authorization request.
     * @throws IllegalArgumentException If the <code>type</code> or <code>id</code> parameters are null or empty.
     */
    @NonNull
    public WithResourceBuilder resource( @NonNull String type, @NonNull String id ) {
      return resource( new GenericAuthorizationResource( type, id ) );
    }

    /**
     * Builds an authorization request with the configured action and principal.
     *
     * @return The authorization request.
     */
    @NonNull
    public IAuthorizationRequest build() {
      return new AuthorizationRequest( getPrincipal(), action );
    }
  }

  /**
   * The {@code WithResourceBuilder} class is a builder step for creating resource authorization requests.
   */
  public static class WithResourceBuilder {
    @NonNull
    private final IAuthorizationAction action;

    @NonNull
    private final IAuthorizationPrincipal principal;

    @NonNull
    private final IAuthorizationResource resource;

    protected WithResourceBuilder( @NonNull IAuthorizationAction action,
                                   @NonNull IAuthorizationPrincipal principal,
                                   @NonNull IAuthorizationResource resource ) {
      this.principal = principal;
      this.action = action;
      this.resource = resource;
    }

    /**
     * Builds a resource authorization request with the previously configured principal, action, and resource.
     *
     * @return The resource authorization request.
     */
    @NonNull
    public IResourceAuthorizationRequest build() {
      return new ResourceAuthorizationRequest( principal, action, resource );
    }
  }
  // endregion

  /**
   * Placeholder authorization action for actions that are not registered in the action service.
   */
  private static class UndefinedAuthorizationAction extends AbstractAuthorizationAction {
    @NonNull
    private final String actionName;

    public UndefinedAuthorizationAction( @NonNull String actionName ) {
      this.actionName = actionName;
    }

    @NonNull
    @Override
    public String getName() {
      return actionName;
    }
  }
}
