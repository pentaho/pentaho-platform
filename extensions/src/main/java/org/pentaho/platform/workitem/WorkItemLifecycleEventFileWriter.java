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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.pentaho.platform.spring.ApplicationEventDispatcher;
import org.pentaho.platform.workitem.messages.Messages;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This component listens for {@link WorkItemLifecycleEvent}s and writes the information stored within the event into
 * a log file.
 */
@Component
public class WorkItemLifecycleEventFileWriter {

  private static final Log log = LogFactory.getLog( WorkItemLifecycleEventFileWriter.class );

  private static String WORK_ITEM_LOG_FILE = "work-item-status";

  private static List<String> DEFAULT_ENV_VARS = new ArrayList<String>();

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
    return CollectionUtils.isEmpty( this.envVars ) ? DEFAULT_ENV_VARS : this.envVars;
  }

  @EventListener
  @Async( ApplicationEventDispatcher.ASYNC_ANNOTATION_QUALIFIER )
  public void onWorkItemLifecycleEvent( final WorkItemLifecycleEvent workItemLifecycleEvent ) {

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

  private String getMessage( final WorkItemLifecycleEvent workItemLifecycleEvent ) {
    final StringBuilder message = new StringBuilder();
    message.append( workItemLifecycleEvent.getTargetTimestamp().getTime() ).append( "|" );
    message.append( workItemLifecycleEvent.getWorkItemUid() ).append( "|" );
    message.append( workItemLifecycleEvent.getWorkItemLifecyclePhase().getName() ).append( "|" );
    message.append( workItemLifecycleEvent.getSourceTimestamp().getTime() ).append( "|" );
    message.append( workItemLifecycleEvent.getSourceHostName() ).append( "|" );
    message.append( workItemLifecycleEvent.getSourceHostIp() ).append( "|" );
    if ( workItemLifecycleEvent.getWorkItemDetails() != null ) {
      message.append( workItemLifecycleEvent.getWorkItemDetails() ).append( "|" );
    }
    message.append( workItemLifecycleEvent.getWorkItemDetails() );

    for ( final String fieldName : envVars ) {
      if ( System.getenv( fieldName )  != null ) {
        message.append( "|" +  System.getenv( fieldName ) );
      }
    }
    return message.toString();
  }

  private Logger workItemLogger = null;

  private Logger getLogger() {
    if ( workItemLogger == null ) {
      synchronized ( WorkItemLifecycleEventFileWriter.class ) {
        if ( workItemLogger == null ) {
          workItemLogger = Logger.getLogger( WorkItemLifecycleEventFileWriter.class );

          final PatternLayout layout = new PatternLayout();
          layout.setConversionPattern( "%m%n" );

          final DailyRollingFileAppender fileAppender = new DailyRollingFileAppender();
          fileAppender.setFile( ".." + File.separator + "logs" + File.separator + WORK_ITEM_LOG_FILE + ".log" );
          fileAppender.setLayout( layout );
          fileAppender.activateOptions();
          fileAppender.setAppend( false );
          fileAppender.setDatePattern( "'.'yyyy-MM-dd" );

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
