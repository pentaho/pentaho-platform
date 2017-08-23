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
 * Copyright (c) 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.workitem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.DateTimeDateFormat;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.workitem.messages.Messages;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This component listens for {@link WorkItemLifecycleEvent}s and writes the information stored within the event into
 * a log file.
 */
public class WorkItemLifecycleEventFileWriter implements ApplicationListener<WorkItemLifecycleEvent>  {

  private static final Log log = LogFactory.getLog( WorkItemLifecycleEventFileWriter.class );

  private static String WORK_ITEM_LOG_FILE = "work-item-status";

  // this path should be relative to the working dir, the tomcat bin directory
  private static String WORK_ITEM_LOG_FILE_PATH = ".." + File.separator + "logs" + File.separator
    + WORK_ITEM_LOG_FILE + ".log";

  private static List<String> DEFAULT_ENV_VARS = new ArrayList<String>();

  // use the default log4j date pattern for all work item status logs
  protected static DateFormat DATE_FORMAT = new DateTimeDateFormat();

  // the pattern of the log message
  private static String WORK_ITEM_LOG_CONVERSION_PATTERN = "%d %m%n";

  // the pattern used for the log name when daily rollover occurs
  private static String WORK_ITEM_LOG_FILE_NAME_PATTERN = "'.'yyyy-MM-dd";

  static {
    DEFAULT_ENV_VARS.add( "HOSTNAME" );
    DEFAULT_ENV_VARS.add( "HOST" );
    DEFAULT_ENV_VARS.add( "MESOS_TASK_ID" );
    DEFAULT_ENV_VARS.add( "MESOS_CONTAINER_NAME" );
  }

  private List<String> envVars = DEFAULT_ENV_VARS;

  public void setEnvVars( final List<String> envVars ) {
    this.envVars = envVars;
  }

  public List<String> getEnvVars() {
    return this.envVars;
  }

  @Override
  public void onApplicationEvent( final WorkItemLifecycleEvent workItemLifecycleEvent ) {

    if ( workItemLifecycleEvent == null ) {
      log.error( getMessageBundle().getErrorString( "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE" ) );
      return;
    }

    if ( log.isDebugEnabled() ) {
      log.debug( String.format( "%s received a WorkItemLifecycleEvent:: %s", this.getClass().getName(),
        workItemLifecycleEvent.toString() ) );
    }

    getLogger().info( getMessage( workItemLifecycleEvent ) );
  }

  /**
   * A convenience method that pads the string representation of this {@link WorkItemLifecyclePhase} such that all
   * padded names are of the same length. This allows us to line up values within logs and makes the logs slightly
   * more readable.
   *
   * @return {@link String} containing a padded string representation of this {@link WorkItemLifecyclePhase}
   */
  public static String getPaddedLifecyclePhaseNameName( final WorkItemLifecyclePhase phase ) {
    return String.format( "%-" + getMaxWorkItemLifecycleLength() + "s", phase.toString() );
  }

  private static int getMaxWorkItemLifecycleLength() {
    final WorkItemLifecyclePhase[] values = WorkItemLifecyclePhase.class.getEnumConstants();
    int maxNameLength = 0;
    for ( final WorkItemLifecyclePhase value : values ) {
      final int enumLength = value.toString().length();
      if ( enumLength > maxNameLength ) {
        maxNameLength = enumLength;
      }
    }
    return maxNameLength;
  }

  protected String getMessage( final WorkItemLifecycleEvent workItemLifecycleEvent ) {
    final StringBuilder message = new StringBuilder();
    message.append( workItemLifecycleEvent.getWorkItemUid() ).append( "|" );
    message.append( getPaddedLifecyclePhaseNameName( workItemLifecycleEvent.getWorkItemLifecyclePhase() ) )
      .append( "|" );
    message.append( DATE_FORMAT.format( workItemLifecycleEvent.getSourceTimestamp() ) ).append( "|" );
    message.append( workItemLifecycleEvent.getSourceHostName() ).append( "|" );
    message.append( workItemLifecycleEvent.getSourceHostIp() );
    // work item details may be empty, in which case there's no need to log it
    if ( !StringUtil.isEmpty( workItemLifecycleEvent.getWorkItemDetails() ) ) {
      message.append( "|" ).append( workItemLifecycleEvent.getWorkItemDetails() );
    }
    // lifecycle details may be empty, in which case there's no need to log it
    if ( !StringUtil.isEmpty( workItemLifecycleEvent.getLifecycleDetails() ) ) {
      message.append( "|" ).append( workItemLifecycleEvent.getLifecycleDetails() );
    }

    if ( envVars != null ) {
      for ( final String fieldName : envVars ) {
        if ( !StringUtil.isEmpty( getEnvVarValue( fieldName ) ) ) {
          message.append( "|" + getEnvVarValue( fieldName ) );
        }
      }
    }
    return message.toString();
  }

  protected String getEnvVarValue( final String envVarName ) {
    return  System.getenv( envVarName );
  }

  private Logger workItemLogger = null;

  protected Logger getLogger() {
    if ( workItemLogger == null ) {
      synchronized ( WorkItemLifecycleEventFileWriter.class ) {
        if ( workItemLogger == null ) {
          workItemLogger = Logger.getLogger( WorkItemLifecycleEventFileWriter.class );

          final PatternLayout layout = new PatternLayout();
          layout.setConversionPattern( WORK_ITEM_LOG_CONVERSION_PATTERN );

          final DailyRollingFileAppender fileAppender = new DailyRollingFileAppender();
          fileAppender.setFile( WORK_ITEM_LOG_FILE_PATH );
          fileAppender.setLayout( layout );
          fileAppender.activateOptions();
          fileAppender.setAppend( false );
          fileAppender.setDatePattern( WORK_ITEM_LOG_FILE_NAME_PATTERN );

          // The log level is arbitrary, it just needs to match the level used to log the work item lifecycle event
          workItemLogger.setLevel( Level.INFO );
          workItemLogger.addAppender( fileAppender );
        }
      }
    }
    return workItemLogger;
  }

  protected Messages getMessageBundle() {
    return Messages.getInstance();
  }
}
