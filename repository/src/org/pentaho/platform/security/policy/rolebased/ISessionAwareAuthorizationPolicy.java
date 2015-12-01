package org.pentaho.platform.security.policy.rolebased;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;

import javax.jcr.Session;

/**
 * This is a extension interface to decouple {@link org.apache.jackrabbit.core.security.authorization.acl
 * .PentahoEntryCollector}
 * @author Andrey Khayrutdinov
 */
public interface ISessionAwareAuthorizationPolicy extends IAuthorizationPolicy {
  /**
   * Returns {@code true} if the the action should be allowed doing all checks within {@code session}.
   *
   * @param actionName name of action (see {@linkplain org.pentaho.platform.api.engine.IAuthorizationAction
   * IAuthorizationAction})
   * @return {@code true} if {@code actionName} is allowed
   */
  boolean isAllowed( Session session, String actionName );
}
