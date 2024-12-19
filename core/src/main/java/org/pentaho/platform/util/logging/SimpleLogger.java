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
import org.pentaho.platform.util.messages.Messages;

public class SimpleLogger implements ILogger {

  private Object id;

  public SimpleLogger( final Object caller ) {
    id =
        ( caller == null )
            ? Messages.getInstance().getString( "Logger.DEBUG_LOG_UNKNOWN" ) : caller.getClass().getName(); //$NON-NLS-1$
  }

  public SimpleLogger( final String callerName ) {
    id = callerName;
  }

  public String getLogLevelName( final int localLogLevel ) {
    return Logger.getLogLevelName( localLogLevel );
  }

  public int getLogLevel( final String localLogLevel ) {
    return Logger.getLogLevel( localLogLevel );
  }

  public int getLoggingLevel() {
    return Logger.getLogLevel();
  }

  public int getLogLevel() {
    return Logger.getLogLevel();
  }

  public void setLoggingLevel( final int newLogLevel ) {
    Logger.setLogLevel( newLogLevel );
  }

  public void trace( final String message ) {
    Logger.trace( id, message );
  }

  public void debug( final String message ) {
    Logger.debug( id, message );
  }

  public void info( final String message ) {
    Logger.info( id, message );
  }

  public void warn( final String message ) {
    Logger.warn( id, message );
  }

  public void error( final String message ) {
    Logger.error( id, message );
  }

  public void fatal( final String message ) {
    Logger.fatal( id, message );
  }

  public void trace( final String message, final Throwable error ) {
    Logger.trace( id, message, error );
  }

  public void debug( final String message, final Throwable error ) {
    Logger.debug( id, message, error );
  }

  public void info( final String message, final Throwable error ) {
    Logger.info( id, message, error );
  }

  public void warn( final String message, final Throwable error ) {
    Logger.warn( id, message, error );
  }

  public void error( final String message, final Throwable error ) {
    Logger.error( id, message, error );
  }

  public void fatal( final String message, final Throwable error ) {
    Logger.fatal( id, message, error );
  }

}
