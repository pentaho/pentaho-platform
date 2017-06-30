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
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.ObjectSerializationUtil;
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

  protected static final String ENV_VAR_PREFIX = "env:";

  private static String WORK_ITEM_LOG_FILE = "work-item-status";

  private static final String DEFAULT_FIELD_DELIMITER = "|";

  private static List<String> DEFAULT_EVENT_MESSAGE_FIELDS = new ArrayList<String>();

  static {
    DEFAULT_EVENT_MESSAGE_FIELDS.add( "targetTimestamp.time" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( "workItemUid" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( "workItemLifecyclePhase.name" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( "lifecycleDetails" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( "sourceTimestamp.time" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( "sourceHostName" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( "sourceHostIp" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( "workItemDetails." + ActionUtil.INVOKER_ACTIONUSER );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( "workItemDetails." + ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( ENV_VAR_PREFIX + "HOSTNAME" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( ENV_VAR_PREFIX + "HOST" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( ENV_VAR_PREFIX + "MESOS_TASK_ID" );
    DEFAULT_EVENT_MESSAGE_FIELDS.add( ENV_VAR_PREFIX + "MESOS_CONTAINER_NAME" );
  }

  private String fieldDelimiter = DEFAULT_FIELD_DELIMITER;
  private List<String> messageEventFields = DEFAULT_EVENT_MESSAGE_FIELDS;

  public void setMessageEventFields( final List<String> messageEventFields ) {
    this.messageEventFields = messageEventFields;
  }

  public List<String> getMessageEventFields() {
    return CollectionUtils.isEmpty( this.messageEventFields ) ? DEFAULT_EVENT_MESSAGE_FIELDS : this.messageEventFields;
  }

  public void setFieldDelimiter( final String fieldDelimiter ) {
    this.fieldDelimiter = fieldDelimiter;
  }

  public String getFieldDelimiter() {
    return this.fieldDelimiter == null ? DEFAULT_FIELD_DELIMITER : this.fieldDelimiter;
  }

  @EventListener
  @Async
  public void onWorkItemLifecycleEvent( final WorkItemLifecycleEvent workItemLifecycleEvent ) {

    if ( workItemLifecycleEvent == null ) {
      log.error( getMessageBundle().getErrorString( "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE" ) );
      return;
    }

    if ( log.isDebugEnabled() ) {
      log.debug( String.format( "%s received a WorkItemLifecycleEvent:: %s", this.getClass().getName(),
        workItemLifecycleEvent.toString() ) );
    }

    final String actualMessagePattern = getContent( workItemLifecycleEvent, getMessageEventFields(),
      getFieldDelimiter() );
    getLogger().info( actualMessagePattern );
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

          // TODO: why are the logs written to std out? remove...

          // The log level is arbitrary, it just needs to match the level used to log the work item lifecycle event
          workItemLogger.setLevel( Level.INFO );
          workItemLogger.addAppender( fileAppender );
        }
      }
    }
    return workItemLogger;
  }

  protected String getContent( final WorkItemLifecycleEvent workItemLifecycleEvent ) {
    return getContent( workItemLifecycleEvent, getMessageEventFields(), getFieldDelimiter() );
  }

  protected static String getContent( final Object data, final List<String> messageEventFields, final String
    fieldDelimiter ) {
    final StringBuilder content = new StringBuilder();
    try {
      String actualDelimiter = "";
      for ( final String fieldName : messageEventFields ) {
        Object fieldContent;
        if ( fieldName.startsWith( ENV_VAR_PREFIX ) ) {
          fieldContent = System.getenv( fieldName.substring( ENV_VAR_PREFIX.length() ) );
          // if an environment variable is null, we can continue, field indices will be preserved, as long as they're
          // at the end of the param list
          if ( fieldContent == null ) {
            continue;
          }
        } else {
          fieldContent = ObjectSerializationUtil.getPropertyValue( data, fieldName );
        }
        content.append( actualDelimiter );
        content.append( fieldContent );
        actualDelimiter = fieldDelimiter;
      }
      return content.toString();
    } catch ( final Exception e ) {
      log.error( e.getLocalizedMessage() );
      return null;
    }
  }

  protected Messages getMessageBundle() {
    return Messages.getInstance();
  }
}
