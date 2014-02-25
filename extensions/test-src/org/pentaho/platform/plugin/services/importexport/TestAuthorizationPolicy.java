package org.pentaho.platform.plugin.services.importexport;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;

import java.util.List;

public class TestAuthorizationPolicy implements IAuthorizationPolicy {
  @Override
  public boolean isAllowed( String actionName ) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public List<String> getAllowedActions( String actionNamespace ) {
    // TODO Auto-generated method stub
    return null;
  }
}
