package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

/**
 * The {@code IAllAuthorizationDecision} interface represents a decision for an authorization request determined from
 * the <i>conjunction</i> of the contained decisions for the same authorization request.
 * <p>
 * The composite decision is granted if all contained decisions are granted. Otherwise, if there are no contained
 * decisions, or if at least one of the contained decisions is denied, the composite decision is denied.
 */
public interface IAllAuthorizationDecision extends ICompositeAuthorizationDecision {
}
