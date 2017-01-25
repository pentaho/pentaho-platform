/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services;

import org.pentaho.platform.api.engine.ActionSequenceException;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.List;

public abstract class PentahoMessenger extends PentahoBase {

  /**
   * 
   */
  private static final long serialVersionUID = 1617348282619161790L;
  private List messages;
  DateFormat dateFormat = LocaleHelper.getFullDateFormat( true, true );

  public List getMessages() {
    return messages;
  }

  public void setMessages( final List messages ) {
    this.messages = messages;
  }

  @Override
  public void trace( final String message ) {
    if ( loggingLevel <= ILogger.TRACE ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.trace( message );
    }
  }

  @Override
  public void debug( final String message ) {
    if ( loggingLevel <= ILogger.DEBUG ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.debug( message );
    }
  }

  @Override
  public void info( final String message ) {
    if ( loggingLevel <= ILogger.INFO ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_INFO", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.info( message );
    }
  }

  @Override
  public void warn( final String message ) {
    if ( loggingLevel <= ILogger.WARN ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_WARNING", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.warn( message );
    }
  }

  @SuppressWarnings( "unchecked" )
  public void error( ActionSequenceException exception ) {
    if ( loggingLevel <= ILogger.ERROR ) {
      if ( messages != null ) {
        messages.add( exception );
      }
      CharArrayWriter charWriter = new CharArrayWriter();
      PrintWriter printWriter = new PrintWriter( charWriter );
      printWriter.println( getLogId()
          + Messages.getInstance().getString( "MessageFormatter.ACTION_SEQUENCE_EXECUTION_FAILED" ) ); //$NON-NLS-1$
      printWriter.println( Messages.getInstance().getString(
          "MessageFormatter.LOG_EXCEPTION_TIME", dateFormat.format( exception.getDate() ) ) ); //$NON-NLS-1$
      printWriter
          .println( Messages
              .getInstance()
              .getString(
                  "MessageFormatter.LOG_EXCEPTION_SESSION_ID", ( exception.getSessionId() == null ? "" : exception.getSessionId() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
      printWriter
          .println( Messages
              .getInstance()
              .getString(
                  "MessageFormatter.LOG_EXCEPTION_INSTANCE_ID", ( exception.getInstanceId() == null ? "" : exception.getInstanceId() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
      printWriter
          .println( Messages
              .getInstance()
              .getString(
                  "MessageFormatter.LOG_EXCEPTION_ACTION_SEQUENCE", ( exception.getActionSequenceName() == null ? "" : exception.getActionSequenceName() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$

      printWriter.println( Messages.getInstance().getString(
          "MessageFormatter.LOG_EXCEPTION_ACTION_SEQUENCE_EXECUTION_STACK" ) ); //$NON-NLS-1$
      exception.printActionExecutionStack( printWriter );

      printWriter
          .println( Messages
              .getInstance()
              .getString(
                  "MessageFormatter.LOG_EXCEPTION_ACTION_CLASS", ( exception.getActionClass() == null ? Messages.getInstance().getString( "MessageFormatter.EXCEPTION_FIELD_NOT_APPLICABLE" ) : exception.getActionClass() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
      printWriter
          .println( Messages
              .getInstance()
              .getString(
                  "MessageFormatter.LOG_EXCEPTION_ACTION_DESC", ( exception.getStepDescription() == null ? "" : exception.getStepDescription() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
      printWriter
          .println( Messages
              .getInstance()
              .getString(
                  "MessageFormatter.LOG_EXCEPTION_LOOP_INDEX", ( exception.getLoopIndex() == null ? Messages.getInstance().getString( "MessageFormatter.EXCEPTION_FIELD_NOT_APPLICABLE" ) : exception.getLoopIndex().toString() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$

      // STEP_NUM isn't working yet
      //      printWriter.println(Messages.getInstance().getString("MessageFormatter.LOG_EXCEPTION_STEP_NUM", (exception.getStepNumber() == null ? Messages.getInstance().getString("MessageFormatter.EXCEPTION_FIELD_NOT_APPLICABLE") : exception.getStepNumber().toString()))); //$NON-NLS-1$ //$NON-NLS-2$

      printWriter.print( Messages.getInstance().getString( "MessageFormatter.EXCEPTION_STACK_TRACE" ) ); //$NON-NLS-1$
      exception.printStackTrace( printWriter );
      getLogger().error( charWriter.toString() );
    }
  }

  @Override
  public void error( final String message ) {
    if ( loggingLevel <= ILogger.ERROR ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.error( message );
    }
  }

  @Override
  public void fatal( final String message ) {
    if ( loggingLevel <= ILogger.FATAL ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.fatal( message );
    }
  }

  @Override
  public void trace( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.TRACE ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.trace( message, error );
    }
  }

  @Override
  public void debug( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.DEBUG ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.debug( message, error );
    }
  }

  @Override
  public void info( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.INFO ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_INFO", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.info( message, error );
    }
  }

  @Override
  public void warn( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.WARN ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString( "Message.USER_WARNING", message, getClass().getName() ) ); //$NON-NLS-1$
      }
      super.warn( message, error );
    }
  }

  @Override
  public void error( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.ERROR ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString(
            "Message.USER_ERROR_EX", message, getClass().getName(), error.toString() ) ); //$NON-NLS-1$
      }
      super.error( message, error );
    }
  }

  @Override
  public void fatal( final String message, final Throwable error ) {
    if ( loggingLevel <= ILogger.FATAL ) {
      if ( messages != null ) {
        messages.add( Messages.getInstance().getString(
            "Message.USER_ERROR_EX", message, getClass().getName(), error.toString() ) ); //$NON-NLS-1$ 
      }
      super.fatal( message, error );
    }
  }

  public static String getUserString( final String type ) {
    return Messages.getInstance().getString( "Message.USER_" + type ); //$NON-NLS-1$
  }

}
