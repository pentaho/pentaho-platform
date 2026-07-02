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

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;

public class GlobalObjectInitializer implements IPentahoSystemListener {

  public boolean startup( final IPentahoSession session ) {
    // return PentahoSystem.initGlobalObjects(session);
    // This run of code has been removed from here and gets called directly
    // in the PentahoSystem at the right place before the other
    // system listeners.
    return true;
  }

  public void shutdown() {

  }

}
