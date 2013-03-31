package org.pentaho.platform.security.policy.rolebased.actions;

import org.pentaho.platform.api.engine.IAuthorizationAction;


/**
 * User: nbaker
 * Date: 3/30/13
 */
public class PublishAction implements IAuthorizationAction {
  public static final String NAME = "org.pentaho.security.publish";
  @Override
  public String getName() {
    return NAME;
  }
}
