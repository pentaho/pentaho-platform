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


package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemEntryPoint;
import org.pentaho.platform.api.engine.IPentahoSystemExitPoint;
import org.pentaho.platform.api.engine.IPentahoSystemListener;

/**
 * This class is responsible for making sure the thread local that hangs on to the current session gets cleared
 * when a system exit point happens.
 * 
 * @deprecated this is now handled by HttpSessionPentahoSessionIntegrationFilter
 */
@Deprecated
public class SessionCleanupListener implements IPentahoSystemListener, IPentahoSystemExitPoint,
    IPentahoSystemEntryPoint {

  public void shutdown() {
  }

  public boolean startup( IPentahoSession session ) {
    IApplicationContext applicationContext = PentahoSystem.getApplicationContext();
    applicationContext.addEntryPointHandler( this );
    applicationContext.addExitPointHandler( this );
    return true;
  }

  public void systemExitPoint() {
    PentahoSessionHolder.removeSession();
  }

  public void systemEntryPoint() {
  }

}
