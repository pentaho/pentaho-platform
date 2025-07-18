package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationCycleException;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;

/**
 * The {@code IAuthorizationContext} interface represents a single authorization process.
 * <p>
 * An authorization process, and the corresponding context, are initiated by calling one of the authorization engine's
 * authorization methods, such as {@link IAuthorizationEngine#authorize(AuthorizationRequest, AuthorizationOptions)}.
 * <p>
 * The authorization context holds the specific options, and the owning authorization engine.
 * Notably, it offers authorization rules the ability to safely and conveniently perform sub-authorizations that affect
 * their own decision, using the same options and in a way that protects against cycles in the process. The context
 * tracks authorization requests to detect and prevent cycles in the evaluation process.
 * <p>
 * The engine/context separation aids with the engine being thread-safe, as each authorization's context is independent,
 * and each authorization is executed in a single thread.
 */
public interface IAuthorizationContext {
  /**
   * Gets the options of the authorization context.
   *
   * @return The authorization options.
   */
  @NonNull
  AuthorizationOptions getOptions();

  /**
   * Gets the authorization engine that owns this context.
   *
   * @return The owning authorization engine.
   */
  @NonNull
  IAuthorizationEngine getEngine();

  /**
   * Authorizes a given authorization request.
   * <p>
   * This method performs a <i>sub-authorization</i> on this context. It uses the same options specified in the
   * top-level authorization request.
   *
   * @param request The authorization request.
   * @return The evaluation decision, never {@code null}.
   * @throws AuthorizationCycleException  If an evaluation cycle is detected for the specified request, or that of
   *                                      a contained sub-authorization.
   * @throws AuthorizationException       If an error occurs during the authorization process.
   */
  @NonNull
  IAuthorizationDecision authorize( @NonNull AuthorizationRequest request )
    throws AuthorizationException;
}
