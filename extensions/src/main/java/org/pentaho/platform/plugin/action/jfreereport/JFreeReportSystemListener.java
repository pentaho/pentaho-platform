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


package org.pentaho.platform.plugin.action.jfreereport;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoReportConfiguration;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;

public class JFreeReportSystemListener implements IPentahoSystemListener {
  public JFreeReportSystemListener() {
  }

  public boolean startup( final IPentahoSession session ) {
    try {
      synchronized ( ClassicEngineBoot.class ) {
        ClassicEngineBoot.setUserConfig( new PentahoReportConfiguration() );
        ClassicEngineBoot.getInstance().start();
      }
    } catch ( Exception ex ) {
      Logger.warn( JFreeReportSystemListener.class.getName(), Messages.getInstance().getErrorString(
          "JFreeReportSystemListener.ERROR_0001_JFREEREPORT_INITIALIZATION_FAILED" ), //$NON-NLS-1$
          ex );
    }
    return true;
  }

  public void shutdown() {
    // Nothing required
  }

}
