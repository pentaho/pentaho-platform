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

package org.pentaho.test.platform.web.doubles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ILogger;

public class LoggerDouble implements ILogger {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog( LoggerDouble.class );

  // ~ Instance fields =================================================================================================

  protected int loggingLevel = DEBUG;

  // ~ Constructors ====================================================================================================

  public LoggerDouble() {
    super();
  }

  // ~ Methods =========================================================================================================

  public void debug( final String message ) {
    if ( logger.isDebugEnabled() ) {
      logger.debug( message );
    }
  }

  public void debug( final String message, final Throwable error ) {
    if ( logger.isDebugEnabled() ) {
      logger.debug( message, error );
    }
  }

  public void error( final String message ) {
    if ( logger.isErrorEnabled() ) {
      logger.error( message );
    }
  }

  public void error( final String message, final Throwable error ) {
    if ( logger.isErrorEnabled() ) {
      logger.error( message, error );
    }
  }

  public void fatal( final String message ) {
    if ( logger.isFatalEnabled() ) {
      logger.fatal( message );
    }
  }

  public void fatal( final String message, final Throwable error ) {
    if ( logger.isFatalEnabled() ) {
      logger.fatal( message, error );
    }
  }

  public int getLoggingLevel() {
    return loggingLevel;
  }

  public void info( final String message ) {
    if ( logger.isInfoEnabled() ) {
      logger.info( message );
    }
  }

  public void info( final String message, final Throwable error ) {
    if ( logger.isInfoEnabled() ) {
      logger.info( message, error );
    }
  }

  public void setLoggingLevel( final int loggingLevel ) {
    if ( loggingLevel == TRACE || loggingLevel == DEBUG || loggingLevel == INFO || loggingLevel == WARN
        || loggingLevel == ERROR || loggingLevel == FATAL ) {
      this.loggingLevel = loggingLevel;
    } else {
      this.loggingLevel = DEBUG;
    }
  }

  public void trace( final String message ) {
    if ( logger.isTraceEnabled() ) {
      logger.trace( message );
    }
  }

  public void trace( final String message, final Throwable error ) {
    if ( logger.isTraceEnabled() ) {
      logger.trace( message, error );
    }
  }

  public void warn( final String message ) {
    if ( logger.isWarnEnabled() ) {
      logger.warn( message );
    }
  }

  public void warn( final String message, final Throwable error ) {
    if ( logger.isWarnEnabled() ) {
      logger.warn( message, error );
    }

  }

}
