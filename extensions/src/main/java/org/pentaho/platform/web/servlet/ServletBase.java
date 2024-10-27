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


package org.pentaho.platform.web.servlet;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.web.WebUtil;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class ServletBase extends HttpServlet implements ILogger {

  public static final boolean debug = PentahoSystem.debug;

  private int loggingLevel = ILogger.ERROR;

  protected IPentahoSession getPentahoSession( final HttpServletRequest request ) {
    return PentahoSessionHolder.getSession();
  }

  public abstract Log getLogger();

  /* ILogger Implementation */

  public String getObjectName() {
    return this.getClass().getName();
  }

  public int getLoggingLevel() {
    return loggingLevel;
  }

  public void setLoggingLevel( final int logLevel ) {
    this.loggingLevel = logLevel;
  }

  public void trace( final String message ) {
    if ( loggingLevel <= ILogger.TRACE ) {
      getLogger().trace( message );
    }
  }

  public void debug( final String message ) {
    if ( loggingLevel <= ILogger.DEBUG ) {
      getLogger().debug( message );
    }
  }

  public void info( final String message ) {
    if ( loggingLevel <= ILogger.INFO ) {
      getLogger().info( message );
    }
  }

  public void warn( final String message ) {
    if ( loggingLevel <= ILogger.WARN ) {
      getLogger().warn( message );
    }
  }

  public void error( final String message ) {
    if ( loggingLevel <= ILogger.ERROR ) {
      getLogger().error( message );
    }
  }

  public void fatal( final String message ) {
    if ( loggingLevel <= ILogger.FATAL ) {
      getLogger().fatal( message );
    }
  }

  public void trace( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.TRACE ) {
      getLogger().trace( message, error );
      Logger.addException( error );
    }
  }

  public void debug( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.DEBUG ) {
      getLogger().debug( message, error );
      Logger.addException( error );
    }
  }

  public void info( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.INFO ) {
      getLogger().info( message, error );
      Logger.addException( error );
    }
  }

  public void warn( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.WARN ) {
      getLogger().warn( message, error );
      Logger.addException( error );
    }
  }

  public void error( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.ERROR ) {
      getLogger().error( message, error );
      Logger.addException( error );
    }
  }

  public void fatal( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.FATAL ) {
      getLogger().fatal( message, error );
      Logger.addException( error );
    }
  }

  public void setCorsHeaders( HttpServletRequest request, HttpServletResponse response ) {
    WebUtil.setCorsResponseHeaders( request, response );
  }
}
