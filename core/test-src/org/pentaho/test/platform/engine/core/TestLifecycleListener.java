package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;

public class TestLifecycleListener implements IPentahoSystemListener {

  public static boolean startupCalled = false;
  public static boolean shutdownCalled = false;
  
  public void shutdown() {
    shutdownCalled = true;
  }

  public boolean startup(IPentahoSession arg0) {
    startupCalled = true;
    return true;
  }

}
