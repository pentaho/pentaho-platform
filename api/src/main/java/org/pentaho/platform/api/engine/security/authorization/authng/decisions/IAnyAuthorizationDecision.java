package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

/**
 * The {@code IAnyAuthorizationDecision} interface represents a decision for an authorization request determined from
 * the <i>disjunction</i> of the contained decisions for the same authorization request.
 * <p>
 * The composite decision is granted if at least one of the contained decisions is granted. Otherwise, if there are no
 * contained decisions, or if all contained decisions are denied, the composite decision is denied.
 */
public interface IAnyAuthorizationDecision extends ICompositeAuthorizationDecision {
}
