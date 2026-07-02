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


package org.pentaho.platform.engine.core.system;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.util.logging.Logger;

import java.io.Serializable;
import java.text.MessageFormat;

public abstract class PentahoBase implements ILogger, Serializable {

  private static final long serialVersionUID = 1376440889763516196L;

  protected int loggingLevel = PentahoSystem.loggingLevel;

  public static final String LOGID_MASK1 = "{0}:{1}:{2}: "; //$NON-NLS-1$

  public static final String LOGID_MASK2 = "{0}:{1}:{2}:{3} "; //$NON-NLS-1$

  public static final String LOGID_SEPARATOR = ":"; //$NON-NLS-1$

  public String EMPTYLOGID = "::: "; //$NON-NLS-1$

  private String logId = EMPTYLOGID;

  public abstract Log getLogger();

  public String getLogId() {
    return logId;
  }

  public void setLogId( final String lId ) {
    logId = lId;
  }

  public void genLogIdFromSession( final IPentahoSession sess ) {
    genLogIdFromInfo( sess.getId(), sess.getProcessId(), sess.getActionName() );
  }

  public void genLogIdFromInfo( final String sessId, final String procId, final String actName ) {
    Object[] args = { sessId, procId, actName };
    setLogId( MessageFormat.format( PentahoBase.LOGID_MASK1, noNulls( args ) ) );
  }

  public void genLogIdFromInfo( final String sessId, final String procId, final String actName, final String instId ) {
    Object[] args = { sessId, procId, actName, instId };
    setLogId( MessageFormat.format( PentahoBase.LOGID_MASK2, noNulls( args ) ) );
  }

  private Object[] noNulls( final Object[] inStr ) {
    for ( int i = 0; i < inStr.length; ++i ) {
      if ( inStr[i] == null ) {
        inStr[i] = ""; //$NON-NLS-1$
      }
    }
    return ( inStr );
  }

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
      getLogger().trace( getLogId() + message );
    }
  }

  public void debug( final String message ) {
    if ( loggingLevel <= ILogger.DEBUG ) {
      getLogger().debug( getLogId() + message );
    }
  }

  public void info( final String message ) {
    if ( loggingLevel <= ILogger.INFO ) {
      getLogger().info( getLogId() + message );
    }
  }

  public void warn( final String message ) {
    if ( loggingLevel <= ILogger.WARN ) {
      getLogger().warn( getLogId() + message );
    }
  }

  public void error( final String message ) {
    if ( loggingLevel <= ILogger.ERROR ) {
      getLogger().error( getLogId() + message );
    }
  }

  public void fatal( final String message ) {
    if ( loggingLevel <= ILogger.FATAL ) {
      getLogger().fatal( getLogId() + message );
    }
  }

  public void trace( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.TRACE ) {
      getLogger().trace( getLogId() + message, error );
      Logger.addException( error );
    }
  }

  public void debug( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.DEBUG ) {
      getLogger().debug( getLogId() + message, error );
      Logger.addException( error );
    }
  }

  public void info( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.INFO ) {
      getLogger().info( getLogId() + message, error );
      Logger.addException( error );
    }
  }

  public void warn( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.WARN ) {
      getLogger().warn( getLogId() + message, error );
      Logger.addException( error );
    }
  }

  public void error( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.ERROR ) {
      IVersionHelper helper = PentahoSystem.get( IVersionHelper.class, null );
      if ( helper != null ) {
        getLogger().error( "Error Start: Pentaho " + helper.getVersionInformation( PentahoSystem.class ) ); //$NON-NLS-1$
      } else {
        getLogger().error( "Error Start: Pentaho " ); //$NON-NLS-1$
      }
      getLogger().error( getLogId() + message, error );
      getLogger().error( "Error end:" ); //$NON-NLS-1$ 
      Logger.addException( error );
    }
  }

  public void fatal( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.FATAL ) {
      IVersionHelper helper = PentahoSystem.get( IVersionHelper.class, null );
      if ( helper != null ) {
        getLogger().error( "Error Start: Pentaho " + helper.getVersionInformation( PentahoSystem.class ) ); //$NON-NLS-1$
      } else {
        getLogger().error( "Error Start: Pentaho " ); //$NON-NLS-1$
      }
      getLogger().fatal( getLogId() + message, error );
      getLogger().error( "Error end:" ); //$NON-NLS-1$ 
      Logger.addException( error );
    }
  }

}
