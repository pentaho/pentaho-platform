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

package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek.DayOfWeek;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek.DayOfWeekQualifier;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

public class SchedulerResourceUtil {

  private static final Log logger = LogFactory.getLog( SchedulerResourceUtil.class );

  public static IJobTrigger
  convertScheduleRequestToJobTrigger( JobScheduleRequest scheduleRequest, IScheduler scheduler )
    throws SchedulerException, UnifiedRepositoryException {

    // Used to determine if created by a RunInBackgroundCommand
    boolean runInBackground =
        scheduleRequest.getSimpleJobTrigger() == null && scheduleRequest.getComplexJobTrigger() == null
            && scheduleRequest.getCronJobTrigger() == null;

    // add 10 seconds to the RIB to ensure execution (see PPP-3264)
    IJobTrigger jobTrigger =
        runInBackground ? new SimpleJobTrigger( new Date( System.currentTimeMillis() + 10000 ), null, 0, 0 ) : scheduleRequest.getSimpleJobTrigger();

    if ( scheduleRequest.getSimpleJobTrigger() != null ) {
      SimpleJobTrigger simpleJobTrigger = scheduleRequest.getSimpleJobTrigger();

      if ( simpleJobTrigger.getStartTime() == null ) {
        simpleJobTrigger.setStartTime( new Date() );
      }

      jobTrigger = simpleJobTrigger;

    } else if ( scheduleRequest.getComplexJobTrigger() != null ) {

      ComplexJobTriggerProxy proxyTrigger = scheduleRequest.getComplexJobTrigger();
      ComplexJobTrigger complexJobTrigger = new ComplexJobTrigger();
      complexJobTrigger.setStartTime( proxyTrigger.getStartTime() );
      complexJobTrigger.setEndTime( proxyTrigger.getEndTime() );

      if ( proxyTrigger.getDaysOfWeek().length > 0 ) {
        if ( proxyTrigger.getWeeksOfMonth().length > 0 ) {
          for ( int dayOfWeek : proxyTrigger.getDaysOfWeek() ) {
            for ( int weekOfMonth : proxyTrigger.getWeeksOfMonth() ) {

              QualifiedDayOfWeek qualifiedDayOfWeek = new QualifiedDayOfWeek();
              qualifiedDayOfWeek.setDayOfWeek( DayOfWeek.values()[dayOfWeek] );

              if ( weekOfMonth == JobScheduleRequest.LAST_WEEK_OF_MONTH ) {
                qualifiedDayOfWeek.setQualifier( DayOfWeekQualifier.LAST );
              } else {
                qualifiedDayOfWeek.setQualifier( DayOfWeekQualifier.values()[weekOfMonth] );
              }
              complexJobTrigger.addDayOfWeekRecurrence( qualifiedDayOfWeek );
            }
          }
        } else {
          for ( int dayOfWeek : proxyTrigger.getDaysOfWeek() ) {
            complexJobTrigger.addDayOfWeekRecurrence( dayOfWeek + 1 );
          }
        }
      } else if ( proxyTrigger.getDaysOfMonth().length > 0 ) {

        for ( int dayOfMonth : proxyTrigger.getDaysOfMonth() ) {
          complexJobTrigger.addDayOfMonthRecurrence( dayOfMonth );
        }
      }

      for ( int month : proxyTrigger.getMonthsOfYear() ) {
        complexJobTrigger.addMonthlyRecurrence( month + 1 );
      }

      for ( int year : proxyTrigger.getYears() ) {
        complexJobTrigger.addYearlyRecurrence( year );
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime( complexJobTrigger.getStartTime() );
      complexJobTrigger.setHourlyRecurrence( calendar.get( Calendar.HOUR_OF_DAY ) );
      complexJobTrigger.setMinuteRecurrence( calendar.get( Calendar.MINUTE ) );
      complexJobTrigger.setUiPassParam( scheduleRequest.getComplexJobTrigger().getUiPassParam() );
      jobTrigger = complexJobTrigger;

    } else if ( scheduleRequest.getCronJobTrigger() != null ) {

      if ( scheduler instanceof QuartzScheduler ) {
        String cronString = scheduleRequest.getCronJobTrigger().getCronString();

        String delims = "[ ]+"; //$NON-NLS-1$
        String[] tokens = cronString.split( delims );
        if ( tokens.length < 7 ) {
          cronString += " *";
        }

        ComplexJobTrigger complexJobTrigger = QuartzScheduler.createComplexTrigger( cronString );
        complexJobTrigger.setStartTime( scheduleRequest.getCronJobTrigger().getStartTime() );
        complexJobTrigger.setEndTime( scheduleRequest.getCronJobTrigger().getEndTime() );
        complexJobTrigger.setUiPassParam( scheduleRequest.getCronJobTrigger().getUiPassParam() );
        jobTrigger = complexJobTrigger;
      } else {
        throw new IllegalArgumentException();
      }
    }

    return jobTrigger;
  }

  public static void updateStartDateForTimeZone( JobScheduleRequest request ) {
    if ( request.getSimpleJobTrigger() != null ) {
      if ( request.getSimpleJobTrigger().getStartTime() != null ) {
        Date origStartDate = request.getSimpleJobTrigger().getStartTime();
        Date serverTimeZoneStartDate = convertDateToServerTimeZone( origStartDate, request.getTimeZone() );
        request.getSimpleJobTrigger().setStartTime( serverTimeZoneStartDate );
      }
    } else if ( request.getComplexJobTrigger() != null ) {
      if ( request.getComplexJobTrigger().getStartTime() != null ) {
        Date origStartDate = request.getComplexJobTrigger().getStartTime();
        Date serverTimeZoneStartDate = convertDateToServerTimeZone( origStartDate, request.getTimeZone() );
        request.getComplexJobTrigger().setStartTime( serverTimeZoneStartDate );
      }
    } else if ( request.getCronJobTrigger() != null ) {
      if ( request.getCronJobTrigger().getStartTime() != null ) {
        Date origStartDate = request.getCronJobTrigger().getStartTime();
        Date serverTimeZoneStartDate = convertDateToServerTimeZone( origStartDate, request.getTimeZone() );
        request.getCronJobTrigger().setStartTime( serverTimeZoneStartDate );
      }
    }
  }

  public static Date convertDateToServerTimeZone( Date dateTime, String timeZone ) {
    Calendar userDefinedTime = Calendar.getInstance();
    userDefinedTime.setTime( dateTime );
    if ( !TimeZone.getDefault().getID().equalsIgnoreCase( timeZone ) ) {
      logger.warn( "original defined time: " + userDefinedTime.getTime().toString() + " on tz:" + timeZone );
      Calendar quartzStartDate = new GregorianCalendar( TimeZone.getTimeZone( timeZone ) );
      quartzStartDate.set( Calendar.YEAR, userDefinedTime.get( Calendar.YEAR ) );
      quartzStartDate.set( Calendar.MONTH, userDefinedTime.get( Calendar.MONTH ) );
      quartzStartDate.set( Calendar.DAY_OF_MONTH, userDefinedTime.get( Calendar.DAY_OF_MONTH ) );
      quartzStartDate.set( Calendar.HOUR_OF_DAY, userDefinedTime.get( Calendar.HOUR_OF_DAY ) );
      quartzStartDate.set( Calendar.MINUTE, userDefinedTime.get( Calendar.MINUTE ) );
      quartzStartDate.set( Calendar.SECOND, userDefinedTime.get( Calendar.SECOND ) );
      quartzStartDate.set( Calendar.MILLISECOND, userDefinedTime.get( Calendar.MILLISECOND ) );
      logger.warn( "adapted time for " + TimeZone.getDefault().getID() + ": " + quartzStartDate.getTime().toString() );
      return quartzStartDate.getTime();
    } else {
      return dateTime;
    }
  }


  public static HashMap<String, Serializable> handlePDIScheduling( RepositoryFile file,
                                                                   HashMap<String, Serializable> parameterMap ) {

    if ( file != null && isPdiFile( file ) ) {

      HashMap<String, Serializable> convertedParameterMap = new HashMap<String, Serializable>();
      Map<String, String> pdiParameterMap = new HashMap<String, String>();
      convertedParameterMap.put( "directory", FilenameUtils.getPathNoEndSeparator( file.getPath() ) );

      String type = isTransformation( file ) ? "transformation" : "job";
      convertedParameterMap.put( type, FilenameUtils.getBaseName( file.getPath() ) );

      Iterator<String> it = parameterMap.keySet().iterator();

      while ( it.hasNext() ) {

        String param = (String) it.next();

        if ( !StringUtils.isEmpty( param ) && parameterMap.containsKey( param ) ) {
          pdiParameterMap.put( param, parameterMap.get( param ).toString() );
        }
      }

      convertedParameterMap.put( "parameters", (Serializable) pdiParameterMap );
      return convertedParameterMap;
    }
    return parameterMap;
  }

  public static boolean isPdiFile( RepositoryFile file ) {
    return isTransformation( file ) || isJob( file );
  }

  public static boolean isTransformation( RepositoryFile file ) {
    return file != null && "ktr".equalsIgnoreCase( FilenameUtils.getExtension( file.getName() ) );
  }

  public static boolean isJob( RepositoryFile file ) {
    return file != null && "kjb".equalsIgnoreCase( FilenameUtils.getExtension( file.getName() ) );
  }

}
