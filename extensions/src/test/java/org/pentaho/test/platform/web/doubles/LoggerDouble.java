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
