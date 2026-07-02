/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.core;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;

public class TestLifecycleListener implements IPentahoSystemListener {

  public static boolean startupCalled = false;
  public static boolean shutdownCalled = false;

  public void shutdown() {
    shutdownCalled = true;
  }

  public boolean startup( IPentahoSession arg0 ) {
    startupCalled = true;
    return true;
  }

}
