/*
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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 * 
 */
package org.pentaho.mantle.client.workspace;

import java.util.Date;

import org.pentaho.gwt.widgets.client.utils.TimeUtil.DayOfWeek;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.MonthOfYear;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.WeekOfMonth;
import org.pentaho.mantle.client.messages.Messages;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class JsJobTrigger extends JavaScriptObject {

  // Overlay types always have protected, zero argument constructors.
  protected JsJobTrigger() {
  }

  public static JsJobTrigger instance() {
    return (JsJobTrigger)JavaScriptObject.createObject();
  }
  
  // JSNI methods to get job type.
  public final native String getType() /*-{ return this['@type']; }-*/; //
  
  public final native void setType(String type) /*-{
    this['@type'] = type;
  }-*/;

  public final native int getRepeatCount() /*-{ return this.repeatCount; }-*/; //

  public final native void setRepeatCount(int count) /*-{
    this.repeatCount = count;
  }-*/;
  
  public final native int getRepeatInterval() /*-{ return this.repeatInterval; }-*/; //

  public final native void setRepeatInterval(int interval) /*-{
    this.repeatInterval = interval;
  }-*/;
  
  private final native String getNativeStartTime() /*-{ return this.startTime; }-*/; //

  public final native void setNativeStartTime(String iso8601TimeString) /*-{
    this.startTime = iso8601TimeString;
  }-*/;
  
  private final native String getNativeEndTime() /*-{ return this.endTime; }-*/; //

  public final native void setNativeEndTime(String iso8601TimeString) /*-{
    this.endTime = iso8601TimeString;
  }-*/;

  public final Date getStartTime() {
    return JsJob.formatDate(getNativeStartTime());
  }

  public final Date getEndTime() {
    return JsJob.formatDate(getNativeEndTime());
  }

  public final native int[] getSecondRecurrences()
  /*-{ 
    return this.secondRecurrences.recurrenceList.values; 
  }-*/;

  public final native void setSecondRecurrences(JsArrayInteger seconds)
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

  public final native void setMinuteRecurrences(JsArrayInteger minutes)
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

  public final native void setHourRecurrences(JsArrayInteger hours)
  /*-{ 
    if (!('hourRecurrences' in this) || !this.hourRecurrences) {
      this.hourRecurrences = {};
    }
    if (!('recurrenceList' in this.hourRecurrences) || !this.hourRecurrences.recurrenceList) {
      this.hourRecurrences.recurrenceList = {};
    }
    this.hourRecurrences.recurrenceList.values = hours; 
  }-*/;
  
  public final native int[] getDayOfWeekRecurrences()
  /*-{ 
    return this.dayOfWeekRecurrences.recurrenceList.values; 
  }-*/;

  public final native void setDayOfWeekRecurrences(JsArrayInteger days)
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

  public final native void setDayOfWeekQualifier(String qualifier)
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

  public final native void setQualifiedDayOfWeek(String dayOfWeek)
  /*-{ 
    if (!('dayOfWeekRecurrences' in this) || !this.dayOfWeekRecurrences) {
      this.dayOfWeekRecurrences = {};
    }
    if (!('qualifiedDayOfWeek' in this.dayOfWeekRecurrences) || !this.dayOfWeekRecurrences.qualifiedDayOfWeek) {
      this.dayOfWeekRecurrences.qualifiedDayOfWeek = {};
    }
    this.dayOfWeekRecurrences.qualifiedDayOfWeek.dayOfWeek = dayOfWeek; 
  }-*/;
  
  public final native int[] getDayOfMonthRecurrences()
  /*-{ 
    return this.dayOfMonthRecurrences.recurrenceList.values; 
  }-*/;

  public final native void setDayOfMonthRecurrences(JsArrayInteger days)
  /*-{ 
    if (!('dayOfMonthRecurrences' in this) || !this.dayOfMonthRecurrences) {
      this.dayOfMonthRecurrences = {};
    }
    if (!('recurrenceList' in this.dayOfMonthRecurrences) || !this.dayOfMonthRecurrences.recurrenceList) {
      this.dayOfMonthRecurrences.recurrenceList = {};
    }
    this.dayOfMonthRecurrences.recurrenceList.values = days; 
  }-*/;
  
  public final native int[] getMonthlyRecurrences()
  /*-{ 
    return this.monthlyRecurrences.recurrenceList.values; 
  }-*/;

  public final native void setMonthlyRecurrences(JsArrayInteger months)
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

  public final native void setYearlyRecurrences(JsArrayInteger years)
  /*-{ 
    if (!('yearlyRecurrences' in this) || !this.yearlyRecurrences) {
      this.yearlyRecurrences = {};
    }
    if (!('recurrenceList' in this.yearlyRecurrences) || !this.yearlyRecurrences.recurrenceList) {
      this.yearlyRecurrences.recurrenceList = {};
    }
    this.yearlyRecurrences.recurrenceList.values = years; 
  }-*/;

  public final String getDescription() {
    String trigDesc = "";
    if ("complexJobTrigger".equals(getType())) {
      // need to digest the recurrences
      int[] monthsOfYear = getMonthlyRecurrences();
      int[] daysOfMonth = getDayOfMonthRecurrences();

      // we are "YEARLY" if
      // monthsOfYear, daysOfMonth OR
      // monthsOfYear, qualifiedDayOfWeek
      if (monthsOfYear.length > 0) {
        if (isQualifiedDayOfWeekRecurrence()) {
          // monthsOfYear, qualifiedDayOfWeek
          String qualifier = getDayOfWeekQualifier();
          String dayOfWeek = getQualifiedDayOfWeek();
          trigDesc = Messages.getString("the") + " " + WeekOfMonth.valueOf(qualifier) + " " + DayOfWeek.valueOf(dayOfWeek) + " " + Messages.getString("of")
              + " " + MonthOfYear.get(monthsOfYear[0] - 1);
        } else {
          // monthsOfYear, daysOfMonth
          trigDesc = Messages.getString("every") + " " + MonthOfYear.get(monthsOfYear[0] - 1) + " " + daysOfMonth[0];
        }
      } else if (daysOfMonth.length > 0) {
        // MONTHLY: Day N of every month
        trigDesc = Messages.getString("day") + " " + daysOfMonth[0] + " " + Messages.getString("ofEveryMonth");
      } else if (isQualifiedDayOfWeekRecurrence()) {
        // MONTHLY: The <qualifier> <dayOfWeek> of every month at <time>
        String qualifier = getDayOfWeekQualifier();
        String dayOfWeek = getQualifiedDayOfWeek();

        if ("THU".equalsIgnoreCase(dayOfWeek)) {
          dayOfWeek = "THUR";
        } else if ("TUE".equalsIgnoreCase(dayOfWeek)) {
          dayOfWeek = "TUES";
        }

        trigDesc = Messages.getString("the") + " " + WeekOfMonth.valueOf(qualifier) + " " + DayOfWeek.valueOf(dayOfWeek) + " "
            + Messages.getString("ofEveryMonth");
      } else if (getDayOfWeekRecurrences().length > 0) {
        // WEEKLY: Every week on <day>..<day> at <time>
        trigDesc = Messages.getString("every") + " " + DayOfWeek.get(getDayOfWeekRecurrences()[0] - 1).toString().trim();
        for (int i = 1; i < getDayOfWeekRecurrences().length; i++) {
          trigDesc += ", " + DayOfWeek.get(getDayOfWeekRecurrences()[i] - 1).toString().trim();
        }
      }
      DateTimeFormat timeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM);
      trigDesc += " at " + timeFormat.format(getStartTime());
    } else if ("simpleJobTrigger".equals(getType())) {
      if (getRepeatCount() > 0) {
        trigDesc += Messages.getString("run") + " " + getRepeatCount() + " " + Messages.getString("times");
      }
      if (getRepeatInterval() > 0) {
        String intervalUnits = " seconds";
        float interval = getRepeatInterval();
        if (getRepeatInterval() < 60) {
          if (interval == 1) {
            intervalUnits = " " + Messages.getString("second");
          } else {
            intervalUnits = " " + Messages.getString("seconds");
          }
        } else if (getRepeatInterval() < 3600) {
          interval = interval / 60f;
          if (interval == 1) {
            intervalUnits = " " + Messages.getString("minute");
          } else {
            intervalUnits = " " + Messages.getString("minutes");
          }
        } else if (getRepeatInterval() < 86400) {
          interval = interval / 3600f;
          if (interval == 1) {
            intervalUnits = " " + Messages.getString("hour");
          } else {
            intervalUnits = " " + Messages.getString("hours");
          }
        } else if (getRepeatInterval() < 604800) {
          DateTimeFormat timeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM);
          interval = interval / 86400f;
          if (interval == 1) {
            if ("".equals(trigDesc)) {
              intervalUnits = Messages.getString("dailyAt") + " " + timeFormat.format(getStartTime());
            } else {
              intervalUnits = " " + Messages.getString("dailyAt") + " " + timeFormat.format(getStartTime());
            }
          } else {
            intervalUnits = " " + Messages.getString("days");
          }
        } else if (getRepeatInterval() == 604800) {
          if ("".equals(trigDesc)) {
            intervalUnits = Messages.getString("weekly");
          } else {
            intervalUnits = " " + Messages.getString("weekly");
          }
        } else if (getRepeatInterval() > 604800) {
          intervalUnits = " " + Messages.getString("days");
          interval = interval / 86400f;
        }
        if ("".equals(trigDesc)) {
          if (interval == 1) {
            trigDesc += intervalUnits;
          } else {
            trigDesc += Messages.getString("every") + " " + interval + intervalUnits;
          }
        } else {
          if (interval == 1) {
            trigDesc += intervalUnits;
          } else {
            trigDesc += " " + Messages.getString("every").toLowerCase() + " " + interval + intervalUnits;
          }
        }
      }
      // if (getStartTime() != null) {
      // trigDesc += " from " + getStartTime();
      // }
      // if (getEndTime() != null) {
      // trigDesc += " until " + getEndTime();
      // }
    } else {
      // cron trigger
    }
    return trigDesc;
  }

  public final String getScheduleType() {
    if ("complexJobTrigger".equals(getType())) {
      // need to digest the recurrences
      int[] monthsOfYear = getMonthlyRecurrences();
      int[] daysOfMonth = getDayOfMonthRecurrences();

      // we are "YEARLY" if
      // monthsOfYear, daysOfMonth OR
      // monthsOfYear, qualifiedDayOfWeek
      if (monthsOfYear.length > 0) {
        return "YEARLY";
      } else if (daysOfMonth.length > 0) {
        // MONTHLY: Day N of every month
        return "MONTHLY";
      } else if (isQualifiedDayOfWeekRecurrence()) {
        // MONTHLY: The <qualifier> <dayOfWeek> of every month at <time>
        return "MONTHLY";
      } else if (getDayOfWeekRecurrences().length > 0) {
        // WEEKLY: Every week on <day>..<day> at <time>
        return "WEEKLY";
      }
    } else if ("simpleJobTrigger".equals(getType())) {
      if (getRepeatInterval() < 86400) {
        return "HOURLY";
      } else if (getRepeatInterval() < 604800) {
        return "DAILY";
      } else if (getRepeatInterval() == 604800) {
        return "WEEKLY";
      } else if (getRepeatInterval() > 604800) {
      }
    } else {
      // cron trigger
    }
    return null;
  }

}