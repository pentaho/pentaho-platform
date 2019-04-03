package org.pentaho.platform.scheduler2.ws.test;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;

import java.util.Collections;
import java.util.List;

public class ScheduleAuthorizationPolicy implements IAuthorizationPolicy {
  public boolean isAllowed( String actionName ) {
    return true;
  }

  public List<String> getAllowedActions( String actionNamespace ) {
    return Collections.<String>emptyList();
  }
}
