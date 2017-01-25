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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ActionSequenceException;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.runtime.ParameterManager.ReturnParameter;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class MessageFormatter implements IMessageFormatter {

  public static final String HTML_MIME_TYPE = "text/html"; //$NON-NLS-1$

  public static final String TEXT_MIME_TYPE = "text/plain"; //$NON-NLS-1$

  public static final int MAX_RESULT_THRESHOLD = 100;

  DateFormat dateFormat = LocaleHelper.getFullDateFormat( true, true );

  public void formatErrorMessage( final String mimeType, final String title, final String message,
      final StringBuffer messageBuffer ) {
    ArrayList messages = new ArrayList( 1 );
    messages.add( message );
    formatErrorMessage( mimeType, title, messages, messageBuffer );
  }

  /**
   * If PentahoMessenger.getUserString("ERROR") returns the string: "Error: {0} ({1})" (which is the case for
   * English) Find the substring before the first "{". In this case, that would be: "Error: ". Return the first
   * string in the messages list that contains the string "Error: ". If no string in the list contains "Error: ",
   * return null;
   * 
   * @param messages
   * @return
   */
  public String getFirstError( final List messages ) {
    // returns something like: "Error: {0} ({1})"
    String errorStart = PentahoMessenger.getUserString( "ERROR" ); //$NON-NLS-1$
    if ( ( messages != null ) && errorStart != null ) {
      int pos = errorStart.indexOf( '{' );
      if ( pos != -1 ) {
        errorStart = errorStart.substring( 0, pos );
      }
      Iterator messageIterator = messages.iterator();
      while ( messageIterator.hasNext() ) {
        String message = (String) messageIterator.next();
        if ( message == null ) {
          continue;
        }
        if ( ( message != null ) && ( message.indexOf( errorStart ) == 0 ) ) {
          message = StringEscapeUtils.escapeHtml( message ); // Escape this to prevent CSS (PPP-1595)
          return message;
        }
      }
    }
    return null;
  }

  public void formatErrorMessage( final String mimeType, final String title, final List messages,
      final StringBuffer messageBuffer ) {
    // TODO make this template or XSL based

    // String product = VersionHelper.getVersionInfo().getProductID();
    // String version = VersionHelper.getVersionInfo().getVersionNumber();
    if ( "text/html".equals( mimeType ) ) { //$NON-NLS-1$
      messageBuffer
          .append( "<html><head><title>" ) //$NON-NLS-1$
          .append( Messages.getInstance().getString( "MessageFormatter.ERROR_PAGE_TITLE" ) ) //$NON-NLS-1$
          .append(
              "</title><link rel=\"stylesheet\" type=\"text/css\" href=\"/pentaho-style/active/default.css\"></head>" ) //$NON-NLS-1$
          .append( "<body dir=\"" ).append( LocaleHelper.getTextDirection() ).append( "\"><table cellspacing=\"10\"><tr><td class=\"portlet-section\" colspan=\"3\">" ) //$NON-NLS-1$ //$NON-NLS-2$
          .append( title ).append( "<hr size=\"1\"/></td></tr><tr><td class=\"portlet-font\" valign=\"top\">" ); //$NON-NLS-1$
      Iterator messageIterator = messages.iterator();
      String errorStart = PentahoMessenger.getUserString( "ERROR" ); //$NON-NLS-1$
      int pos = errorStart.indexOf( '{' );
      if ( pos != -1 ) {
        errorStart = errorStart.substring( 0, pos );
      }
      String firstMessage = getFirstError( messages );
      if ( firstMessage != null ) {
        messageBuffer.append( "<span style=\"color:red\">" ).append( firstMessage ).append( "</span><p/>" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      messageIterator = messages.iterator();
      while ( messageIterator.hasNext() ) {
        messageBuffer.append( StringEscapeUtils.escapeHtml( (String) messageIterator.next() ) ).append( "<br/>" ); //$NON-NLS-1$
      }
      messageBuffer.append( "</td></tr></table><p>" ); //$NON-NLS-1$
      if ( PentahoSystem.getObjectFactory().objectDefined( IVersionHelper.class.getSimpleName() ) ) {
        IVersionHelper versionHelper = PentahoSystem.get( IVersionHelper.class, null );
        messageBuffer
            .append( "&nbsp;&nbsp;" + Messages.getInstance().getString( "MessageFormatter.USER_SERVER_VERSION", versionHelper.getVersionInformation( PentahoSystem.class ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      messageBuffer.append( "</body></html>" ); //$NON-NLS-1$
    }
  }

  public void formatFailureMessage( final String mimeType, final IRuntimeContext context,
      final StringBuffer messageBuffer, final List defaultMessages ) {

    // TODO handle error messages from the runtime context

    if ( ( context == null ) && ( defaultMessages == null ) ) {
      // something went badly wrong
      formatErrorMessage(
          mimeType,
          Messages.getInstance().getString( "MessageFormatter.ERROR_0001_REQUEST_FAILED" ), Messages.getInstance().getString( "MessageFormatter.ERROR_0002_COULD_NOT_PROCESS" ), messageBuffer ); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      if ( context == null ) {
        formatErrorMessage( mimeType, "Failed", defaultMessages, messageBuffer ); //$NON-NLS-1$
      } else {
        // By convention, if the solution engine encounters an exception while trying to execute and xaction,
        // the engine will stick the caught exception in the runtime context messages. Here we're looking through
        // the messages to see if an an action sequence exception was caught and stored with the messages.
        // If so we'll format and return an exception message.
        ActionSequenceException theFailureException = null;
        List theMessages = context.getMessages();
        for ( Object msg : theMessages ) {
          if ( msg instanceof ActionSequenceException ) {
            theFailureException = (ActionSequenceException) msg;
            break;
          }
        }
        if ( theFailureException != null ) {
          formatExceptionMessage( mimeType, theFailureException, messageBuffer );
        } else {
          formatErrorMessage( mimeType, "Failed", theMessages, messageBuffer ); //$NON-NLS-1$
        }
      }
    }
  }

  public void formatExceptionMessage( String mimeType, ActionSequenceException exception, StringBuffer messageBuffer ) {
    if ( "text/html".equals( mimeType ) ) { //$NON-NLS-1$
      String templateFile = null;
      String templatePath = "system/ui/templates/viewActionErrorTemplate.html"; //$NON-NLS-1$
      try {
        byte[] bytes =
            IOUtils.toByteArray( ActionSequenceResource.getInputStream( templatePath, LocaleHelper.getLocale() ) );
        templateFile = new String( bytes, LocaleHelper.getSystemEncoding() );
      } catch ( IOException e ) {
        messageBuffer.append( Messages.getInstance().getErrorString(
            "MessageFormatter.RESPONSE_ERROR_HEADING", templatePath ) ); //$NON-NLS-1$
        e.printStackTrace();
      }

      // NOTE: StringUtils.replace is used here instead of String.replaceAll because since the latter uses regex,
      // the
      // replacment
      // text can cause exceptions if '$' or other special characters are present. We cannot guarantee that the
      // replacement
      // text does not have these characters, so a non-regex replacer was used.

      // TODO: there is a bit of extraneous String object creation here. If performance becomes an issue, there are
      // more
      // efficient
      // ways of doing mass replacements of text, such as using StringBuilder.replace

      // %ERROR_HEADING%
      templateFile = StringUtils.replace( templateFile, "%ERROR_HEADING%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_ERROR_HEADING" ) ); //$NON-NLS-1$

      // %EXCEPTION_MSG%
      templateFile = StringUtils.replace( templateFile, "%EXCEPTION_MSG%", StringEscapeUtils.escapeHtml( exception //$NON-NLS-1$
          .getMessage() == null ? "" : exception.getMessage() ) ); //$NON-NLS-1$
      templateFile = StringUtils.replace( templateFile, "%EXCEPTION_MSG_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_MSG_LABEL" ) ); //$NON-NLS-1$

      // %EXCEPTION_TIME%
      templateFile = StringUtils.replace( templateFile, "%EXCEPTION_TIME%", StringEscapeUtils.escapeHtml( dateFormat //$NON-NLS-1$
          .format( exception.getDate() ) ) );
      templateFile = StringUtils.replace( templateFile, "%EXCEPTION_TIME_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_TIME_LABEL" ) ); //$NON-NLS-1$

      // %EXCEPTION_TYPE%
      templateFile = StringUtils.replace( templateFile, "%EXCEPTION_TYPE%", StringEscapeUtils.escapeHtml( exception //$NON-NLS-1$
          .getClass().getSimpleName() ) );
      templateFile = StringUtils.replace( templateFile, "%EXCEPTION_TYPE_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_TYPE_LABEL" ) ); //$NON-NLS-1$

      // %SESSION_ID%
      templateFile = StringUtils.replace( templateFile, "%SESSION_ID%", StringEscapeUtils.escapeHtml( exception //$NON-NLS-1$
          .getSessionId() == null ? "" : exception.getSessionId() ) ); //$NON-NLS-1$
      templateFile = StringUtils.replace( templateFile, "%SESSION_ID_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_SESSION_ID_LABEL" ) ); //$NON-NLS-1$

      // %INSTANCE_ID%
      templateFile = StringUtils.replace( templateFile, "%INSTANCE_ID%", StringEscapeUtils.escapeHtml( exception //$NON-NLS-1$
          .getInstanceId() == null ? "" : exception.getInstanceId() ) ); //$NON-NLS-1$
      templateFile = StringUtils.replace( templateFile, "%INSTANCE_ID_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_INSTANCE_ID_LABEL" ) ); //$NON-NLS-1$

      // %ACTION_SEQUENCE%
      templateFile = StringUtils.replace( templateFile, "%ACTION_SEQUENCE%", StringEscapeUtils.escapeHtml( exception //$NON-NLS-1$
          .getActionSequenceName() == null ? "" : exception.getActionSequenceName() ) ); //$NON-NLS-1$
      templateFile = StringUtils.replace( templateFile, "%ACTION_SEQUENCE_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_ACTION_SEQUENCE_LABEL" ) ); //$NON-NLS-1$

      // %ACTION_SEQUENCE_EXECUTION_STACK%
      CharArrayWriter charWriter = new CharArrayWriter();
      PrintWriter printWriter = new PrintWriter( charWriter );
      exception.printActionExecutionStack( printWriter );
      templateFile =
          StringUtils.replace( templateFile,
              "%ACTION_SEQUENCE_EXECUTION_STACK%", StringEscapeUtils.escapeHtml( charWriter //$NON-NLS-1$
                  .toString() ) );
      templateFile =
          StringUtils.replace( templateFile, "%ACTION_SEQUENCE_EXECUTION_STACK_LABEL%", Messages.getInstance() //$NON-NLS-1$
              .getString( "MessageFormatter.RESPONSE_EXCEPTION_ACTION_SEQUENCE_EXECUTION_STACK_LABEL" ) ); //$NON-NLS-1$

      // %ACTION_CLASS%
      templateFile = StringUtils.replace( templateFile, "%ACTION_CLASS%", StringEscapeUtils.escapeHtml( exception //$NON-NLS-1$
          .getActionClass() == null ? "" : exception.getActionClass() ) ); //$NON-NLS-1$
      templateFile = StringUtils.replace( templateFile, "%ACTION_CLASS_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_ACTION_CLASS_LABEL" ) ); //$NON-NLS-1$

      // %ACTION_DESC%
      templateFile = StringUtils.replace( templateFile, "%ACTION_DESC%", StringEscapeUtils.escapeHtml( exception //$NON-NLS-1$
          .getStepDescription() == null ? "" : exception.getStepDescription() ) ); //$NON-NLS-1$
      templateFile = StringUtils.replace( templateFile, "%ACTION_DESC_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_ACTION_DESC_LABEL" ) ); //$NON-NLS-1$

      // %STEP_NUM%
      templateFile =
          StringUtils.replace( templateFile, "%STEP_NUM%", StringEscapeUtils.escapeHtml( exception //$NON-NLS-1$
              .getStepNumber() == null ? Messages.getInstance().getString(
              "MessageFormatter.EXCEPTION_FIELD_NOT_APPLICABLE" ) : exception.getStepNumber().toString() ) ); //$NON-NLS-1$
      templateFile = StringUtils.replace( templateFile, "%STEP_NUM_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_STEP_NUM_LABEL" ) ); //$NON-NLS-1$

      // %STEP_NUM%
      templateFile =
          StringUtils.replace( templateFile, "%LOOP_INDEX%", StringEscapeUtils.escapeHtml( exception //$NON-NLS-1$
              .getLoopIndex() == null ? Messages.getInstance().getString(
              "MessageFormatter.EXCEPTION_FIELD_NOT_APPLICABLE" ) : exception.getLoopIndex().toString() ) ); //$NON-NLS-1$
      templateFile = StringUtils.replace( templateFile, "%LOOP_INDEX_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_LOOP_INDEX_LABEL" ) ); //$NON-NLS-1$

      // %STACK_TRACE%
      charWriter = new CharArrayWriter();
      printWriter = new PrintWriter( charWriter );
      exception.printStackTrace( printWriter );
      templateFile = StringUtils.replace( templateFile, "%STACK_TRACE%", StringEscapeUtils.escapeHtml( charWriter //$NON-NLS-1$
          .toString() ) );
      templateFile = StringUtils.replace( templateFile, "%STACK_TRACE_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_STACK_TRACE_LABEL" ) ); //$NON-NLS-1$

      // %EXCEPTION_MESSAGES%
      Stack<String> causes = new Stack<String>();
      buildCauses( causes, exception );
      charWriter = new CharArrayWriter();
      printWriter = new PrintWriter( charWriter );
      while ( !causes.empty() ) {
        printWriter.println( causes.pop() );
      }
      templateFile =
          StringUtils.replace( templateFile, "%EXCEPTION_MESSAGES%", StringEscapeUtils.escapeHtml( charWriter //$NON-NLS-1$
              .toString() ) );
      templateFile = StringUtils.replace( templateFile, "%EXCEPTION_MESSAGES_LABEL%", Messages.getInstance() //$NON-NLS-1$
          .getString( "MessageFormatter.RESPONSE_EXCEPTION_MESSAGES_LABEL" ) ); //$NON-NLS-1$

      // %SERVER_INFO% (if available)
      if ( PentahoSystem.getObjectFactory().objectDefined( IVersionHelper.class.getSimpleName() ) ) {
        IVersionHelper versionHelper = PentahoSystem.get( IVersionHelper.class );
        templateFile = StringUtils.replace( templateFile, "%SERVER_INFO%", Messages.getInstance().getString( //$NON-NLS-1$
            "MessageFormatter.USER_SERVER_VERSION", versionHelper.getVersionInformation( PentahoSystem.class ) ) ); //$NON-NLS-1$
      }

      messageBuffer.append( templateFile );
    }
  }

  private static void buildCauses( Stack<String> causes, Throwable cause ) {
    if ( cause != null ) {
      causes.push( cause.getClass().getSimpleName()
          + ": " + ( ( cause.getMessage() != null ) ? cause.getMessage() : "" ) ); //$NON-NLS-1$//$NON-NLS-2$
      buildCauses( causes, cause.getCause() );
    }
  }

  public void formatFailureMessage( final String mimeType, final IRuntimeContext context,
      final StringBuffer messageBuffer ) {
    formatFailureMessage( mimeType, context, messageBuffer, null );
  }

  @SuppressWarnings( { "deprecation", "null" } )
  public void formatResultSetAsHTMLRows( final IPentahoResultSet resultSet, final StringBuffer messageBuffer ) {
    boolean hasColumnHeaders = false;
    boolean hasRowHeaders = false;
    Object[][] columnHeaders = null;
    Object[][] rowHeaders = null;
    if ( resultSet.getMetaData() != null ) {
      columnHeaders = resultSet.getMetaData().getColumnHeaders();
      rowHeaders = resultSet.getMetaData().getRowHeaders();
      hasColumnHeaders = ( columnHeaders != null ) && ( columnHeaders.length > 0 );
      hasRowHeaders = ( rowHeaders != null ) && ( rowHeaders.length > 0 );
      if ( hasColumnHeaders ) {
        for ( Object[] element : columnHeaders ) {
          messageBuffer.append( "<tr>" ); //$NON-NLS-1$
          if ( hasRowHeaders ) {
            for ( int indent = 0; indent < rowHeaders[0].length; indent++ ) {
              messageBuffer.append( "<th></th>" ); //$NON-NLS-1$
            }
          }
          for ( int column = 0; column < element.length; column++ ) {
            messageBuffer.append( "<th>" ).append( element[column] ).append( "</th>" ); //$NON-NLS-1$//$NON-NLS-2$
          }
          messageBuffer.append( "</tr>" ); //$NON-NLS-1$
        }
      }

    }
    Object[] dataRow = resultSet.next();
    int currentRow = 0;
    while ( ( dataRow != null ) && ( currentRow < MessageFormatter.MAX_RESULT_THRESHOLD ) ) {
      messageBuffer.append( "<tr>" ); //$NON-NLS-1$
      if ( hasRowHeaders ) {
        for ( int rowHeaderCol = rowHeaders[currentRow].length - 1; rowHeaderCol >= 0; rowHeaderCol-- ) {
          messageBuffer.append( "<th>" ).append( rowHeaders[currentRow][rowHeaderCol].toString() ).append( "</th>" ); //$NON-NLS-1$//$NON-NLS-2$
        }
      }
      NumberFormat nf = NumberFormat.getInstance();
      for ( Object element : dataRow ) {
        if ( element != null ) {
          Object value = element;
          if ( value instanceof Number ) {
            Number numVal = (Number) value;
            value = nf.format( numVal );
            messageBuffer.append( "<td align=\"right\">" ).append( value.toString() ).append( "</td>" ); //$NON-NLS-1$//$NON-NLS-2$
          } else {
            messageBuffer.append( "<td>" ).append( value.toString() ).append( "</td>" ); //$NON-NLS-1$ //$NON-NLS-2$
          }
        } else {
          messageBuffer.append( "<td>---</td>" ); //$NON-NLS-1$
        }
      }
      messageBuffer.append( "</tr>" ); //$NON-NLS-1$
      dataRow = resultSet.next();
      currentRow++;
    }
  }

  public void formatSuccessMessage( final String mimeType, final IRuntimeContext context,
      final StringBuffer messageBuffer, final boolean doMessages ) {
    formatSuccessMessage( mimeType, context, messageBuffer, doMessages, true );
  }

  @SuppressWarnings( { "deprecation", "null" } )
  public void formatSuccessMessage( final String mimeType, final IRuntimeContext context,
      final StringBuffer messageBuffer, final boolean doMessages, final boolean doWrapper ) {

    if ( context == null ) {
      // something went badly wrong
      formatFailureMessage( mimeType, context, messageBuffer );
    } else if ( mimeType.equalsIgnoreCase( MessageFormatter.HTML_MIME_TYPE ) ) {
      // TODO make this template or XSL based
      if ( doWrapper ) {
        messageBuffer
            .append( "<html><head><title>" ) //$NON-NLS-1$
            .append( Messages.getInstance().getString( "MessageFormatter.USER_START_ACTION" ) ) //$NON-NLS-1$
            .append(
                "</title><link rel=\"stylesheet\" type=\"text/css\" href=\"/pentaho-style/active/default.css\"></head>" ) //$NON-NLS-1$
            .append( "<body dir=\"" ).append( LocaleHelper.getTextDirection() ).append( "\"><table cellspacing=\"10\"><tr><td class=\"portlet-section\" colspan=\"3\">" ) //$NON-NLS-1$ //$NON-NLS-2$
            .append( Messages.getInstance().getString( "MessageFormatter.USER_ACTION_SUCCESSFUL" ) ) //$NON-NLS-1$
            .append( "<hr size=\"1\"/></td></tr><tr><td class=\"portlet-font\" valign=\"top\">" ); //$NON-NLS-1$
      }

      // hmm do we need this to be ordered?
      Set outputNames = context.getOutputNames();
      Iterator outputNameIterator = outputNames.iterator();
      while ( outputNameIterator.hasNext() ) {
        String outputName = (String) outputNameIterator.next();
        Object value = context.getOutputParameter( outputName ).getValue();
        if ( value == null ) {
          value = ""; //$NON-NLS-1$
        } else if ( value instanceof IPentahoResultSet ) {
          formatResultSetAsHTMLRows( (IPentahoResultSet) value, messageBuffer );
        } else {
          // Temporary fix for BISERVER-3348
          ReturnParameter rpm = (ReturnParameter) context.getParameterManager().getReturnParameters().get( outputName );
          //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
          if ( ( rpm != null ) && ( "response".equalsIgnoreCase( rpm.destinationName ) ) //$NON-NLS-1$
              && ( "header".equalsIgnoreCase( rpm.destinationParameter ) ) ) { //$NON-NLS-1$
            // we don't want to output response header parameters to the browser...
          } else {

            if ( doWrapper ) {
              messageBuffer.append( outputName ).append( "=" ); //$NON-NLS-1$
            }
            messageBuffer.append( value.toString() );
            if ( doWrapper ) {
              messageBuffer.append( "<br/>" ); //$NON-NLS-1$
            }

          }
        }
      }
      if ( doMessages ) {
        if ( doWrapper ) {
          messageBuffer.append( "<p><br size=\"1\">" ); //$NON-NLS-1$
        }
        List messages = context.getMessages();
        Iterator messageIterator = messages.iterator();
        while ( messageIterator.hasNext() ) {
          messageBuffer.append( (String) messageIterator.next() );
          if ( doWrapper ) {
            messageBuffer.append( "<br/>" ); //$NON-NLS-1$
          }
        }
      }

      if ( doWrapper ) {
        messageBuffer.append( "</td></tr></table></body></html>" ); //$NON-NLS-1$
      }
    } else if ( mimeType.equalsIgnoreCase( MessageFormatter.TEXT_MIME_TYPE ) ) {
      messageBuffer.append( Messages.getInstance().getString( "MessageFormatter.USER_START_ACTION" + "\n" ) ) //$NON-NLS-1$ //$NON-NLS-2$
          .append( Messages.getInstance().getString( "MessageFormatter.USER_ACTION_SUCCESSFUL" + "\n" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      // hmm do we need this to be ordered?
      Set outputNames = context.getOutputNames();
      Iterator outputNameIterator = outputNames.iterator();
      while ( outputNameIterator.hasNext() ) {
        String outputName = (String) outputNameIterator.next();
        Object value = context.getOutputParameter( outputName ).getValue();
        if ( value == null ) {
          value = ""; //$NON-NLS-1$
        } else if ( value instanceof IPentahoResultSet ) {
          IPentahoResultSet resultSet = (IPentahoResultSet) value;
          Object[][] columnHeaders = resultSet.getMetaData().getColumnHeaders();
          Object[][] rowHeaders = resultSet.getMetaData().getRowHeaders();
          boolean hasColumnHeaders = columnHeaders != null;
          boolean hasRowHeaders = rowHeaders != null;
          if ( hasColumnHeaders ) {
            for ( Object[] element : columnHeaders ) {
              for ( int column = 0; column < element.length; column++ ) {
                if ( hasRowHeaders ) {
                  for ( int indent = 0; indent < rowHeaders[0].length; indent++ ) {
                    messageBuffer.append( "\t" ); //$NON-NLS-1$
                  }
                }
                messageBuffer.append( element[column] ).append( "\t" ); //$NON-NLS-1$
              }
              messageBuffer.append( "\n" ); //$NON-NLS-1$
            }
          }
          int headerRow = 0;
          Object[] dataRow = resultSet.next();
          int currentRow = 0;
          while ( ( dataRow != null ) && ( currentRow < MessageFormatter.MAX_RESULT_THRESHOLD ) ) {
            if ( hasRowHeaders ) {
              for ( int rowHeaderCol = 0; rowHeaderCol < rowHeaders[headerRow].length; rowHeaderCol++ ) {
                messageBuffer.append( rowHeaders[headerRow][rowHeaderCol].toString() ).append( "\t" ); //$NON-NLS-1$
              }
            }
            for ( Object element : dataRow ) {
              messageBuffer.append( element.toString() ).append( "\t" ); //$NON-NLS-1$
            }
            dataRow = resultSet.next();
            currentRow++;
          }
        } else {
          // Temporary fix for BISERVER-3348
          ReturnParameter rpm = (ReturnParameter) context.getParameterManager().getReturnParameters().get( outputName );
          //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
          if ( ( rpm != null ) && ( "response".equalsIgnoreCase( rpm.destinationName ) ) //$NON-NLS-1$
              && ( "header".equalsIgnoreCase( rpm.destinationParameter ) ) ) { //$NON-NLS-1$
            // we don't want to output response header parameters to the browser...
          } else {
            messageBuffer.append( outputName ).append( "=" ).append( value.toString() ).append( "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
      }
      if ( doMessages ) {
        List messages = context.getMessages();
        Iterator messageIterator = messages.iterator();
        while ( messageIterator.hasNext() ) {
          messageBuffer.append( (String) messageIterator.next() ).append( "\n" ); //$NON-NLS-1$
        }
      }
    }
  }

}
