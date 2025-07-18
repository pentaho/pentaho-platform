package org.pentaho.platform.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationEngine;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The {@code AuthorizationEngineAuthorizationPolicy} is an implementation of the {@link IAuthorizationPolicy}
 * backed by an instance of {@link IAuthorizationEngine}.
 * <p>
 * Any {@link AuthorizationException exceptions} thrown by the authorization engine while authorizing are logged as
 * errors and treated as "not allowed" results.
 */
public class AuthorizationEngineAuthorizationPolicy implements IAuthorizationPolicy {

  private static final Log logger = LogFactory.getLog( AuthorizationEngineAuthorizationPolicy.class );

  @NonNull
  private final IAuthorizationActionService authorizationActionService;

  @NonNull
  private final IAuthorizationEngine authorizationEngine;

  @NonNull
  private final Supplier<Authentication> currentAuthenticationSupplier;

  public AuthorizationEngineAuthorizationPolicy(
    @NonNull IAuthorizationActionService authorizationActionService,
    @NonNull IAuthorizationEngine authorizationEngine ) {
    this(
      authorizationActionService,
      authorizationEngine,
      AuthorizationEngineAuthorizationPolicy::getSpringSessionContextAuthentication );
  }

  public AuthorizationEngineAuthorizationPolicy(
    @NonNull IAuthorizationActionService authorizationActionService,
    @NonNull IAuthorizationEngine authorizationEngine,
    @NonNull Supplier<Authentication> currentAuthenticationSupplier ) {

    Assert.notNull( authorizationActionService, "Argument 'authorizationActionService' is required" );
    Assert.notNull( authorizationEngine, "Argument 'authorizationEngine' is required" );
    Assert.notNull( currentAuthenticationSupplier, "Argument 'currentAuthenticationSupplier' is required" );

    this.authorizationActionService = authorizationActionService;
    this.authorizationEngine = authorizationEngine;
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
    return actionOptional.isPresent() && isAllowed( actionOptional.get() );
  }

  protected boolean isAllowed( @NonNull IAuthorizationAction action ) {
    AuthorizationRequest request = new AuthorizationRequest(
      new SpringAuthenticationAuthorizationUser( getCurrentAuthentication() ),
      action );

    try {
      return authorizationEngine.authorize( request ).isGranted();
    } catch ( AuthorizationException e ) {
      // Lenient approach.
      logger.error( "Authorization engine evaluation failed. Assuming not allowed.", e );
      return false;
    }
  }

  @Override
  public List<String> getAllowedActions( String actionNamespace ) {
    return authorizationActionService.getActions( actionNamespace )
      .filter( this::isAllowed )
      .map( IAuthorizationAction::getName )
      .collect( Collectors.toList() );
  }
}
