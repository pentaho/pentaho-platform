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

package org.pentaho.mantle.client.workspace;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.DayOfWeek;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.MonthOfYear;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.WeekOfMonth;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor.ScheduleType;
import org.pentaho.mantle.client.messages.Messages;

import java.util.Date;

public class JsJobTrigger extends JavaScriptObject {

  // Overlay types always have protected, zero argument constructors.
  protected JsJobTrigger() {
  }

  public static JsJobTrigger instance() {
    return (JsJobTrigger) JavaScriptObject.createObject();
  }

  // JSNI methods to get job type.
  public final native String getType()
  /*-{ 
    return this['@type']; 
  }-*/;

  public final native void setType( String type )
  /*-{
    this['@type'] = type;
  }-*/;

  public final String getScheduleType() {
    String s = getUiPassParamRaw();
    if ( s != null && !s.equals( "" ) ) {
      return s;
    }
    return calcScheduleType().name();
  }

  private final native String getUiPassParamRaw()
  /*-{  
    return this.uiPassParam; 
  }-*/;

  public final native void setScheduleType( String scheduleType )
  /*-{
    this.ScheduleType = scheduleType;
  }-*/;

  public final native int getRepeatCount()
  /*-{ 
    return parseInt(this.repeatCount); 
  }-*/;

  public final native void setRepeatCount( int count )
  /*-{
    this.repeatCount = count;
  }-*/;

  public final native int getRepeatInterval()
  /*-{ 
    return parseInt(this.repeatInterval); 
  }-*/;

  public final native void setRepeatInterval( int interval )
  /*-{
    this.repeatInterval = interval;
  }-*/;

  public final native void setBlockDuration( Long duration )
  /*-{
    this.duration = duration.toString();
  }-*/;

  public final Long getBlockDuration() {
    return Long.parseLong( getDuration() );
  }

  private final native String getDuration()
  /*-{
      return this.duration;
  }-*/;

  private final native String getNativeStartTime()
  /*-{ 
    return this.startTime; 
  }-*/;

  public final native void setNativeStartTime( String iso8601TimeString )
  /*-{
    this.startTime = iso8601TimeString;
  }-*/;

  private final native String getNativeEndTime()
  /*-{ 
    return this.endTime; 
  }-*/;

  public final native void setNativeEndTime( String iso8601TimeString )
  /*-{
    this.endTime = iso8601TimeString;
  }-*/;

  public final Date getStartTime() {
    if ( StringUtils.isEmpty( getNativeStartTime() ) ) {
      return new Date();
    }
    return JsJob.formatDate( getNativeStartTime() );
  }

  public final Date getEndTime() {
    return JsJob.formatDate( getNativeEndTime() );
  }

  public final native int[] getSecondRecurrences()
  /*-{ 
    return this.secondRecurrences.recurrenceList.values; 
  }-*/;

  public final native void setSecondRecurrences( JsArrayInteger seconds )
  /*-{ 
    if (!('secondRecurrences' in this) || !this.secondRecurrences) {
      this.secondRecurrences = {};
    }
    if (!('recurrenceList' in this.secondRecurrences) || !this.secondRecurrences.recurrenceList) {
      this.secondRecurrences.recurrenceList = {};
    }
    this.secondRecurrences.recurrenceList.values = seconds; 
  }-*/;

  public final native int[] getMinuteRecurrences()
  /*-{ 
    return this.minuteRecurrences.recurrenceList.values; 
  }-*/;

  public final native void setMinuteRecurrences( JsArrayInteger minutes )
  /*-{ 
    if (!('minuteRecurrences' in this) || !this.minuteRecurrences) {
      this.minuteRecurrences = {};
    }
    if (!('recurrenceList' in this.minuteRecurrences) || !this.minuteRecurrences.recurrenceList) {
      this.minuteRecurrences.recurrenceList = {};
    }
    this.minuteRecurrences.recurrenceList.values = minutes; 
  }-*/;

  public final native int[] getHourRecurrences()
  /*-{ 
    return this.hourlyRecurrences.recurrenceList.values; 
  }-*/;

  public final native void setHourRecurrences( JsArrayInteger hours )
  /*-{ 
    if (!('hourRecurrences' in this) || !this.hourRecurrences) {
      this.hourRecurrences = {};
    }
    if (!('recurrenceList' in this.hourRecurrences) || !this.hourRecurrences.recurrenceList) {
      this.hourRecurrences.recurrenceList = {};
    }
    this.hourRecurrences.recurrenceList.values = hours; 
  }-*/;

  public final int[] getDayOfWeekRecurrences() {
    if ( getDayOfWeekRecurrencesRaw() == null ) {
      return null;
    } else {
      return convertJsArrayStringToIntArray( getDayOfWeekRecurrencesRaw() );
    }
  }

  private final native JsArrayString getDayOfWeekRecurrencesRaw()
  /*-{
    if ('dayOfWeekRecurrences' in this && this.dayOfWeekRecurrences != null) {
      if ('recurrenceList' in this.dayOfWeekRecurrences) {
        return this.dayOfWeekRecurrences.recurrenceList.values;
      }
    }
    return null;
  }-*/;

  /**
   * Converts javascript integer arrays that were stored as quoted numbers in the JSON as an int[] array.
   * 
   * @param jsArrayString
   *          = Json Array with the integer elements quoted
   * @return int array
   */
  public final int[] convertJsArrayStringToIntArray( JsArrayString jsArrayString ) {
    if ( jsArrayString == null ) {
      return null;
    } else {
      int[] intArray = new int[jsArrayString.length()];
      StringTokenizer tokenizer = new StringTokenizer( jsArrayString.toString(), "," );
      for ( int i = 0; i < tokenizer.countTokens(); i++ ) {
        try {
          String value = tokenizer.tokenAt( i );
          intArray[i] = Integer.parseInt( value );
        } catch ( Throwable t ) {
          Window.alert( t.getMessage() );
        }
      }
      return intArray;
    }
  }

  public final native void setDayOfWeekRecurrences( JsArrayInteger days )
  /*-{ 
    if (!('dayOfWeekRecurrences' in this) || !this.dayOfWeekRecurrences) {
      this.dayOfWeekRecurrences = {};
    }
    if (!('recurrenceList' in this.dayOfWeekRecurrences) || !this.dayOfWeekRecurrences.recurrenceList) {
      this.dayOfWeekRecurrences.recurrenceList = {};
    }
    this.dayOfWeekRecurrences.recurrenceList.values = days; 
  }-*/;

  public final native boolean isQualifiedDayOfWeekRecurrence()
  /*-{ 
    return this.dayOfWeekRecurrences != null && this.dayOfWeekRecurrences.qualifiedDayOfWeek != null; 
  }-*/;

  public final native String getDayOfWeekQualifier()
  /*-{ 
    return this.dayOfWeekRecurrences.qualifiedDayOfWeek.qualifier; 
  }-*/;

  public final native void setDayOfWeekQualifier( String qualifier )
  /*-{ 
    if (!('dayOfWeekRecurrences' in this) || !this.dayOfWeekRecurrences) {
      this.dayOfWeekRecurrences = {};
    }
    if (!('qualifiedDayOfWeek' in this.dayOfWeekRecurrences) || !this.dayOfWeekRecurrences.qualifiedDayOfWeek) {
      this.dayOfWeekRecurrences.qualifiedDayOfWeek = {};
    }
    this.dayOfWeekRecurrences.qualifiedDayOfWeek.qualifier = qualifier; 
  }-*/;

  public final native String getQualifiedDayOfWeek()
  /*-{ 
    return this.dayOfWeekRecurrences.qualifiedDayOfWeek.dayOfWeek; 
  }-*/;

  public final native void setQualifiedDayOfWeek( String dayOfWeek )
  /*-{ 
    if (!('dayOfWeekRecurrences' in this) || !this.dayOfWeekRecurrences) {
      this.dayOfWeekRecurrences = {};
    }
    if (!('qualifiedDayOfWeek' in this.dayOfWeekRecurrences) || !this.dayOfWeekRecurrences.qualifiedDayOfWeek) {
      this.dayOfWeekRecurrences.qualifiedDayOfWeek = {};
    }
    this.dayOfWeekRecurrences.qualifiedDayOfWeek.dayOfWeek = dayOfWeek; 
  }-*/;

  public final int[] getDayOfMonthRecurrences() {
    return convertJsArrayStringToIntArray( getDayOfMonthRecurrencesRaw() );
  }

  private final native JsArrayString getDayOfMonthRecurrencesRaw()
  /*-{ 
    if (this.dayOfMonthRecurrences != null && ('recurrenceList' in this.dayOfMonthRecurrences)) {
      return this.dayOfMonthRecurrences.recurrenceList.values;
    } else {
      return null;
    }
  }-*/;

  public final native void setDayOfMonthRecurrences( JsArrayInteger days )
  /*-{ 
    if (!('dayOfMonthRecurrences' in this) || !this.dayOfMonthRecurrences) {
      this.dayOfMonthRecurrences = {};
    }
    if (!('recurrenceList' in this.dayOfMonthRecurrences) || !this.dayOfMonthRecurrences.recurrenceList) {
      this.dayOfMonthRecurrences.recurrenceList = {};
    }
    this.dayOfMonthRecurrences.recurrenceList.values = days; 
  }-*/;

  public final int[] getMonthlyRecurrences() {
    return convertJsArrayStringToIntArray( getMonthlyRecurrencesRaw() );
  }

  private final native JsArrayString getMonthlyRecurrencesRaw()
  /*-{
    if (this.monthlyRecurrences != null && ('recurrenceList' in this.monthlyRecurrences)){
      return this.monthlyRecurrences.recurrenceList.values;
    } else {
      return null;
    }
  }-*/;

  public final native void setMonthlyRecurrences( JsArrayInteger months )
  /*-{ 
    if (!('monthlyRecurrences' in this) || !this.monthlyRecurrences) {
      this.monthlyRecurrences = {};
    }
    if (!('recurrenceList' in this.monthlyRecurrences) || !this.monthlyRecurrences.recurrenceList) {
      this.monthlyRecurrences.recurrenceList = {};
    }
    this.monthlyRecurrences.recurrenceList.values = months; 
  }-*/;

  public final native int[] getYearlyRecurrences()
  /*-{ 
    return this.yearlyRecurrences.recurrenceList.values; 
  }-*/;

  public final native void setYearlyRecurrences( JsArrayInteger years )
  /*-{ 
    if (!('yearlyRecurrences' in this) || !this.yearlyRecurrences) {
      this.yearlyRecurrences = {};
    }
    if (!('recurrenceList' in this.yearlyRecurrences) || !this.yearlyRecurrences.recurrenceList) {
      this.yearlyRecurrences.recurrenceList = {};
    }
    this.yearlyRecurrences.recurrenceList.values = years; 
  }-*/;

  public final native String getCronString()
  /*-{ 
    return this.cronString;
  }-*/;

  public final native void setCronString( String cronString )
  /*-{ 
    this.cronString = cronString;
  }-*/;

  public final String getDescription() {
    String trigDesc = "";
    ScheduleType scheduleType = ScheduleType.valueOf( getScheduleType() );
    if ( scheduleType == ScheduleType.RUN_ONCE ) {
      return "Run Once";
    }
    if ( "cronJobTrigger".equals( getType() ) || ( getUiPassParamRaw()
      != null && getUiPassParamRaw().equals( "CRON" ) ) ) {
      trigDesc += "CRON: " + getCronString();
    } else if ( "complexJobTrigger".equals( getType() ) ) {
      // need to digest the recurrences
      int[] monthsOfYear = getMonthlyRecurrences();
      int[] daysOfMonth = getDayOfMonthRecurrences();

      // we are "YEARLY" if
      // monthsOfYear, daysOfMonth OR
      // monthsOfYear, qualifiedDayOfWeek
      if ( monthsOfYear != null && monthsOfYear.length > 0 ) {
        if ( isQualifiedDayOfWeekRecurrence() ) {
          // monthsOfYear, qualifiedDayOfWeek
          String qualifier = getDayOfWeekQualifier();
          String dayOfWeek = getQualifiedDayOfWeek();
          trigDesc =
              Messages.getString( "the" ) + " " + WeekOfMonth.valueOf( qualifier ) + " "
                  + DayOfWeek.valueOf( dayOfWeek ) + " " + Messages.getString( "of" ) + " "
                  + MonthOfYear.get( monthsOfYear[0] - 1 );
        } else {
          // monthsOfYear, daysOfMonth
          trigDesc =
              Messages.getString( "every" ) + " " + MonthOfYear.get( monthsOfYear[0] - 1 ) + " " + daysOfMonth[0];
        }
      } else if ( daysOfMonth != null && daysOfMonth.length > 0 ) {
        // MONTHLY: Day N of every month
        trigDesc = Messages.getString( "day" ) + " " + daysOfMonth[0] + " " + Messages.getString( "ofEveryMonth" );
      } else if ( isQualifiedDayOfWeekRecurrence() ) {
        // MONTHLY: The <qualifier> <dayOfWeek> of every month at <time>
        String qualifier = getDayOfWeekQualifier();
        String dayOfWeek = getQualifiedDayOfWeek();

        trigDesc =
            Messages.getString( "the" ) + " " + WeekOfMonth.valueOf( qualifier ) + " " + DayOfWeek.valueOf( dayOfWeek )
                + " " + Messages.getString( "ofEveryMonth" );
      } else if ( getDayOfWeekRecurrences().length > 0 ) {
        // WEEKLY: Every week on <day>..<day> at <time>
        // check if weekdays first
        if ( getDayOfWeekRecurrences().length == 5 && getDayOfWeekRecurrences()[0] == 2
            && getDayOfWeekRecurrences()[4] == 6 ) {
          trigDesc = Messages.getString( "every" ) + " " + Messages.getString( "weekday" );
        } else {
          trigDesc =
              Messages.getString( "every" ) + " "
                  + Messages.getString( DayOfWeek.get( getDayOfWeekRecurrences()[0] - 1 )
                    .toString().trim() );
          for ( int i = 1; i < getDayOfWeekRecurrences().length; i++ ) {
            trigDesc += ", " + Messages.getString( DayOfWeek.get( getDayOfWeekRecurrences()[i] - 1 )
              .toString().trim() );
          }
        }
      }
      DateTimeFormat timeFormat = DateTimeFormat.getFormat( PredefinedFormat.TIME_MEDIUM );
      trigDesc += " at " + timeFormat.format( getStartTime() );
    } else if ( "simpleJobTrigger".equals( getType() ) ) {
      // if (getRepeatInterval() > 0) {
      trigDesc = getSimpleDescription();

      // if (getStartTime() != null) {
      // trigDesc += " from " + getStartTime();
      // }
      // if (getEndTime() != null) {
      // trigDesc += " until " + getEndTime();
      // }
    }
    return trigDesc;
  }

  public final String getSimpleDescription() {
    ScheduleType scheduleType = getSimpleScheduleType();
    String trigDesc;
    String intervalUnits = "";
    int intervalSeconds = 1;
    if ( scheduleType == ScheduleType.DAILY ) {
      intervalSeconds = 86400;
      intervalUnits = timeUnitText( intervalSeconds, "day" );
      // DateTimeFormat timeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM);
      if ( getRepeatInterval() == intervalSeconds ) {
        intervalUnits = Messages.getString( "dayAtLowercase" );
      }
      // else {
      // intervalUnits += " " + Messages.getString("at");
      // }
      // intervalUnits += " " + timeFormat.format(getStartTime());
    } else if ( scheduleType == ScheduleType.HOURS ) {
      intervalSeconds = 3600;
      intervalUnits = timeUnitText( intervalSeconds, "hour" );
    } else if ( scheduleType == ScheduleType.MINUTES ) {
      intervalSeconds = 60;
      intervalUnits = timeUnitText( intervalSeconds, "minute" );
    } else if ( scheduleType == ScheduleType.SECONDS ) {
      intervalSeconds = 1;
      intervalUnits = timeUnitText( intervalSeconds, "second" );
    } else if ( scheduleType == ScheduleType.WEEKLY ) {
      intervalSeconds = 604800;
      intervalUnits = Messages.getString( "weekly" );
    }
    DateTimeFormat timeFormat = DateTimeFormat.getFormat( PredefinedFormat.TIME_MEDIUM );
    if ( scheduleType == ScheduleType.WEEKLY ) {
      int repeatInterval = getRepeatInterval();
      trigDesc =
          Messages.getString( "every" ) + " " + ( repeatInterval / 86400 ) + " " + Messages.getString( "daysLower" );
      trigDesc += " at " + timeFormat.format( getStartTime() );
    } else if ( intervalSeconds != getRepeatInterval() ) {
      trigDesc = Messages.getString( "every" ) + " " + intervalUnits;
      trigDesc += " at " + timeFormat.format( getStartTime() );
    } else {
      trigDesc = Messages.getString( "every" ) + " " + intervalUnits;
      trigDesc += " at " + timeFormat.format( getStartTime() );
    }
    if ( getRepeatCount() > 0 ) {
      trigDesc += "; " + Messages.getString( "run" ) + " " + getRepeatCount() + " " + Messages.getString( "times" );
      // }
    }

    // if (getStartTime() != null) {
    // trigDesc += " from " + getStartTime();
    // }
    // if (getEndTime() != null) {
    // trigDesc += " until " + getEndTime();
    // }
    return trigDesc;
  }

  public final String timeUnitText( int intervalSeconds, String timeUnit ) {
    if ( getRepeatInterval() == intervalSeconds ) {
      return Messages.getString( timeUnit );
    } else {
      return "" + getRepeatInterval() / intervalSeconds + " " + Messages.getString( timeUnit + "s" );
    }
  }

  public final String oldGetScheduleType() {
    if ( "complexJobTrigger".equals( getType() ) ) {
      // need to digest the recurrences
      int[] monthsOfYear = getMonthlyRecurrences();
      int[] daysOfMonth = getDayOfMonthRecurrences();

      // we are "YEARLY" if
      // monthsOfYear, daysOfMonth OR
      // monthsOfYear, qualifiedDayOfWeek
      if ( monthsOfYear.length > 0 ) {
        return "YEARLY";
      } else if ( daysOfMonth.length > 0 ) {
        // MONTHLY: Day N of every month
        return "MONTHLY";
      } else if ( isQualifiedDayOfWeekRecurrence() ) {
        // MONTHLY: The <qualifier> <dayOfWeek> of every month at <time>
        return "MONTHLY";
      } else if ( getDayOfWeekRecurrences().length > 0 ) {
        // WEEKLY: Every week on <day>..<day> at <time>
        return "WEEKLY";
      }
    } else if ( "simpleJobTrigger".equals( getType() ) ) {
      if ( getRepeatInterval() < 86400 ) {
        return "HOURLY";
      } else if ( getRepeatInterval() < 604800 ) {
        return "DAILY";
      } else if ( getRepeatInterval() == 604800 ) {
        return "WEEKLY";
      }
    }
    return null;
  }

  /**
   * Intended to deduce the ScheduleType if not already set. This method should only be called if the schedule type
   * is unassigned.
   * 
   * @return
   */
  public final ScheduleType calcScheduleType() {
    if ( "complexJobTrigger".equals( getType() ) ) {
      // need to digest the recurrences
      int[] monthsOfYear = getMonthlyRecurrences();
      int[] daysOfMonth = getDayOfMonthRecurrences();
      // we are "YEARLY" if
      // monthsOfYear, daysOfMonth OR
      // monthsOfYear, qualifiedDayOfWeek
      if ( monthsOfYear != null && monthsOfYear.length > 0 ) {
        return ScheduleType.YEARLY;
      } else if ( daysOfMonth != null && daysOfMonth.length > 0 ) {
        // MONTHLY: Day N of every month
        return ScheduleType.MONTHLY;
      } else if ( isQualifiedDayOfWeekRecurrence() ) {
        // MONTHLY: The <qualifier> <dayOfWeek> of every month at <time>
        return ScheduleType.MONTHLY;

      } else if ( isWorkDaysInWeek() ) {
        return ScheduleType.DAILY;
      } else {
        return ScheduleType.WEEKLY;
      }
    } else if ( "simpleJobTrigger".equals( getType() ) ) {
      return getSimpleScheduleType();
    } else {
      return ScheduleType.CRON; // cron trigger
    }
  }

  private ScheduleType getSimpleScheduleType() {
    if ( getRepeatInterval() == 0 ) {
      return ScheduleType.RUN_ONCE;
    } else if ( getRepeatInterval() % 604800 == 0 ) {
      return ScheduleType.WEEKLY;
    } else if ( getRepeatInterval() % 86400 == 0 ) {
      return ScheduleType.DAILY;
    } else if ( getRepeatInterval() % 3600 == 0 ) {
      return ScheduleType.HOURS;
    } else if ( getRepeatInterval() % 60 == 0 ) {
      return ScheduleType.MINUTES;
    } else if ( getRepeatInterval() > 0 ) {
      return ScheduleType.SECONDS;
    } else {
      return ScheduleType.RUN_ONCE;
    }
  }

  public final boolean isWorkDaysInWeek() {
    int[] daysOfWeek = getDayOfWeekRecurrences();
    if ( daysOfWeek == null || daysOfWeek.length != 5 ) {
      return false;
    } else {
      for ( int i = 0; i < 5; i++ ) {
        if ( daysOfWeek[i] != i + 2 ) {
          return false;
        }
      }
      return true;
    }
  }

  public final native Date getNextFireTime() /*-{ return this.nextFireTime; }-*/;

  public final native String getName() /*-{ return this.name; }-*/;
}
