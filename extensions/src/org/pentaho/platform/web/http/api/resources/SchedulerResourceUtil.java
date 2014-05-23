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

import java.util.Calendar;
import java.util.Date;

public class SchedulerResourceUtil {

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
}
