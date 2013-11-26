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

package org.pentaho.platform.web.servlet;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

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

}
