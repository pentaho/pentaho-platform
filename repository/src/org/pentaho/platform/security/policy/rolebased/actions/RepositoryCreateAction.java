package org.pentaho.platform.security.policy.rolebased.actions;

import org.pentaho.platform.api.engine.IAuthorizationAction;

/**
 * User: nbaker
 * Date: 3/19/13
 */
public class RepositoryCreateAction implements IAuthorizationAction {
  public static final String NAME = "org.pentaho.repository.create";
  @Override
  public String getName() {
    return NAME;
  }
}
