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


package org.pentaho.platform.plugin.action.kettle;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pentaho.di.core.logging.KettleLogLayout;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.KettleLoggingEventListener;
import org.pentaho.platform.api.util.LogUtil;

public class Log4jForwardingKettleLoggingEventListener implements KettleLoggingEventListener {

  public static final String STRING_PENTAHO_DI_LOGGER_NAME = "org.pentaho.di";

  private Logger pentahoLogger;

  private KettleLogLayout layout;

  /**
   * Create a new forwarder from Kettle to Log4j
   */
  public Log4jForwardingKettleLoggingEventListener() {
    pentahoLogger = LogManager.getLogger( STRING_PENTAHO_DI_LOGGER_NAME );

    // ensure all messages get logged in this logger since we filtered it above
    // we do not set the level in the rootLogger so the rootLogger can decide by itself (e.g. in the platform)
    //
    LogUtil.setLevel( pentahoLogger, Level.ALL );

    // The layout
    //
    layout = new KettleLogLayout( true ); // add time
  }

  @Override
  public void eventAdded( KettleLoggingEvent event ) {

    if ( event.getLevel() == org.pentaho.di.core.logging.LogLevel.NOTHING ) {
      return;
    }

    String line = layout.format( event );

    switch ( event.getLevel() ) {
      case ERROR:
        pentahoLogger.log( Level.ERROR, line );
        break;
      case DEBUG:
      case ROWLEVEL:
        pentahoLogger.log( Level.DEBUG, line );
        break;
      default:
        pentahoLogger.log( Level.INFO, line );
        break;
    }
  }
}
