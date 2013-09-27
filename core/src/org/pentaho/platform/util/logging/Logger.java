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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.util.messages.Messages;

public class Logger {

  private static final ThreadLocal<List<Throwable>> threadExceptions = new ThreadLocal<List<Throwable>>();

  private static IVersionHelper versionHelper = null;

  private static int logLevel = ILogger.ERROR;

  private static final String MISC_LOG = "misc-"; //$NON-NLS-1$

  private static Log logger;

  private static final String logName = "org.pentaho.platform.util.logging.Logger"; //$NON-NLS-1$

  static {
    Logger.logger = LogFactory.getLog( Logger.logName );
  }

  public static void addException( final Throwable t ) {

    List<Throwable> list = Logger.threadExceptions.get();
    if ( list == null ) {
      list = new ArrayList<Throwable>();
      Logger.threadExceptions.set( list );
    }
    list.add( t );
  }

  public static List<Throwable> getExceptions() {
    return Logger.threadExceptions.get();
  }

  public static synchronized void setVersionHelper( final IVersionHelper helper ) {
    Logger.versionHelper = helper;
  }

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
        return Messages.getInstance().getString( "Logger.CODE_LOG_UNKNOWN" ); //$NON-NLS-1$
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

  public static int getLogLevel() {
    return Logger.logLevel;
  }

  public static void setLogLevel( final int newLogLevel ) {
    Logger.logLevel = newLogLevel;
  }

  public static void trace( final Object caller, final String message ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.TRACE ) {
      Logger.logger.trace( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void debug( final Object caller, final String message ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.DEBUG ) {
      Logger.logger.debug( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void info( final Object caller, final String message ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.INFO ) {
      Logger.logger.info( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void warn( final Object caller, final String message ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.WARN ) {
      Logger.logger.warn( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void error( final Object caller, final String message ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.ERROR ) {
      Logger.logger.error( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void fatal( final Object caller, final String message ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.FATAL ) {
      Logger.logger.fatal( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void trace( final Object caller, final String message, final Throwable error ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.TRACE ) {
      Logger.logger.trace( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
    }
    Logger.addException( error );
  }

  public static void debug( final Object caller, final String message, final Throwable error ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.DEBUG ) {
      Logger.logger.debug( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
    }
    Logger.addException( error );
  }

  public static void info( final Object caller, final String message, final Throwable error ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.INFO ) {
      Logger.logger.info( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
    }
    Logger.addException( error );
  }

  public static void warn( final Object caller, final String message, final Throwable error ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.WARN ) {
      Logger.logger.warn( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
    }
    Logger.addException( error );
  }

  public static void error( final Object caller, final String message, final Throwable error ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.ERROR ) {
      Logger.logger.error( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
    }
    Logger.addException( error );
  }

  public static void fatal( final Object caller, final String message, final Throwable error ) {
    String id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.FATAL ) {
      Logger.logger.fatal( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
    }
    Logger.addException( error );
  }

  public static void debug( final String caller, final String message ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.DEBUG ) {
      Logger.logger.debug( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void info( final String caller, final String message ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.INFO ) {
      Logger.logger.info( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void warn( final String caller, final String message ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.WARN ) {
      Logger.logger.warn( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void error( final String caller, final String message ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.ERROR ) {
      Logger.logger.error( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void fatal( final String caller, final String message ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.FATAL ) {
      Logger.logger.fatal( Logger.MISC_LOG + id + ": " + message ); //$NON-NLS-1$
    }
  }

  public static void debug( final String caller, final String message, final Throwable error ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.DEBUG ) {
      Logger.logger.debug( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
      Logger.addException( error );
    }
  }

  public static void info( final String caller, final String message, final Throwable error ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.INFO ) {
      Logger.logger.info( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
      Logger.addException( error );
    }
  }

  public static void warn( final String caller, final String message, final Throwable error ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.WARN ) {
      Logger.logger.warn( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
      Logger.addException( error );
    }
  }

  public static void error( final String caller, final String message, final Throwable error ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.ERROR ) {
      if ( Logger.versionHelper != null ) {
        Logger.logger.error( "Error: Pentaho " + Logger.versionHelper.getVersionInformation( Logger.class ) ); //$NON-NLS-1$
      } else {
        Logger.logger.error( "Error: Pentaho" ); //$NON-NLS-1$
      }
      Logger.logger.error( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
      Logger.logger.error( "Error end:" ); //$NON-NLS-1$ 
      Logger.addException( error );
    }
  }

  public static void fatal( final String caller, final String message, final Throwable error ) {
    String id = ( caller == null ) ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller; //$NON-NLS-1$
    if ( Logger.logLevel <= ILogger.FATAL ) {
      if ( Logger.versionHelper != null ) {
        Logger.logger.error( "Error: Pentaho " + Logger.versionHelper.getVersionInformation( Logger.class ) ); //$NON-NLS-1$
      } else {
        Logger.logger.error( "Error: Pentaho" ); //$NON-NLS-1$
      }
      Logger.logger.fatal( Logger.MISC_LOG + id + ": " + message, error ); //$NON-NLS-1$
      Logger.logger.error( "Error end:" ); //$NON-NLS-1$ 
      Logger.addException( error );
    }
  }

}
