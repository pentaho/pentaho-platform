/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
