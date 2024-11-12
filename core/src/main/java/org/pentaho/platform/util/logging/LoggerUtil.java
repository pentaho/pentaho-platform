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


package org.pentaho.platform.util.logging;

import org.pentaho.platform.api.engine.ILogger;

public class LoggerUtil {

  public static String getLogLevelName( final int localLogLevel ) {
    switch ( localLogLevel ) {
      case ILogger.TRACE:
        return "TRACE"; //$NON-NLS-1$
      case ILogger.DEBUG:
        return "DEBUG"; //$NON-NLS-1$
      case ILogger.INFO:
        return "INFO"; //$NON-NLS-1$
      case ILogger.WARN:
        return "WARN"; //$NON-NLS-1$
      case ILogger.ERROR:
        return "ERROR"; //$NON-NLS-1$
      case ILogger.FATAL:
        return "FATAL"; //$NON-NLS-1$
      default:
        return "UNKNOWN"; //$NON-NLS-1$
    }
  }

  public static int getLogLevel( final String localLogLevel ) {
    if ( "TRACE".equalsIgnoreCase( localLogLevel ) ) { //$NON-NLS-1$
      return ILogger.TRACE;
    }
    if ( "DEBUG".equalsIgnoreCase( localLogLevel ) ) { //$NON-NLS-1$
      return ILogger.DEBUG;
    }
    if ( "INFO".equalsIgnoreCase( localLogLevel ) ) { //$NON-NLS-1$
      return ILogger.INFO;
    }
    if ( "WARN".equalsIgnoreCase( localLogLevel ) ) { //$NON-NLS-1$
      return ILogger.WARN;
    }
    if ( "ERROR".equalsIgnoreCase( localLogLevel ) ) { //$NON-NLS-1$
      return ILogger.ERROR;
    }
    if ( "FATAL".equalsIgnoreCase( localLogLevel ) ) { //$NON-NLS-1$
      return ILogger.FATAL;
    }
    return ILogger.ERROR;
  }

}
