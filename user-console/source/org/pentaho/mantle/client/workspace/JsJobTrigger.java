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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class JsJobTrigger extends JavaScriptObject {

  // Overlay types always have protected, zero argument constructors.
  protected JsJobTrigger() {
  }

  // JSNI methods to get job type.
  public final native String getType() /*-{ return this['@type']; }-*/; //

  public final native int getRepeatCount() /*-{ return this.repeatCount; }-*/; //

  public final native int getRepeatInterval() /*-{ return this.repeatInterval; }-*/; //

  private final native String getNativeStartTime() /*-{ return this.startTime; }-*/; //

  private final native String getNativeEndTime() /*-{ return this.endTime; }-*/; //

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

  public final native int[] getMinuteRecurrences()
  /*-{ 
    return this.minuteRecurrences.recurrenceList.values; 
  }-*/;

  public final native int[] getHourRecurrences()
  /*-{ 
    return this.hourlyRecurrences.recurrenceList.values; 
  }-*/;

  public final native int[] getDayOfWeekRecurrences()
  /*-{ 
    var arr = [];
    if (this.dayOfWeekRecurrences != null && this.dayOfWeekRecurrences.recurrenceList != null) {
      if (this.dayOfWeekRecurrences.recurrenceList.values.constructor.toString().indexOf("Array") == -1) {
        arr.push(this.dayOfWeekRecurrences.recurrenceList.values);
      } else {
        arr = this.dayOfWeekRecurrences.recurrenceList.values; 
      }
    }
    return arr;
  }-*/;

  public final native boolean isQualifiedDayOfWeekRecurrence()
  /*-{ 
    return this.dayOfWeekRecurrences != null && this.dayOfWeekRecurrences.qualifiedDayOfWeek != null; 
  }-*/;

  public final native String getDayOfWeekQualifier()
  /*-{ 
    return this.dayOfWeekRecurrences.qualifiedDayOfWeek.qualifier; 
  }-*/;

  public final native String getQualifiedDayOfWeek()
  /*-{ 
    return this.dayOfWeekRecurrences.qualifiedDayOfWeek.dayOfWeek; 
  }-*/;

  public final native int[] getDayOfMonthRecurrences()
  /*-{ 
    var arr = [];
    if (this.dayOfMonthRecurrences != null && this.dayOfMonthRecurrences.recurrenceList != null) {
      if (this.dayOfMonthRecurrences.recurrenceList.values.constructor.toString().indexOf("Array") == -1) {
        arr.push(this.dayOfMonthRecurrences.recurrenceList.values);
      } else {
        arr = this.dayOfMonthRecurrences.recurrenceList.values; 
      }
    }
    return arr;
  }-*/;

  public final native int[] getMonthlyRecurrences()
  /*-{ 
    var arr = [];
    if (this.monthlyRecurrences != null && this.monthlyRecurrences.recurrenceList != null) {
      if (this.monthlyRecurrences.recurrenceList.values.constructor.toString().indexOf("Array") == -1) {
        arr.push(this.monthlyRecurrences.recurrenceList.values);
      } else {
        arr = this.monthlyRecurrences.recurrenceList.values; 
      }
    }
    return arr;
  }-*/;

  public final native int[] getYearlyRecurrences()
  /*-{ 
  var arr = [];
  if (this.yearlyRecurrences != null && this.yearlyRecurrences.recurrenceList != null) {
    if (this.yearlyRecurrences.recurrenceList.values.constructor.toString().indexOf("Array") == -1) {
      arr.push(this.yearlyRecurrences.recurrenceList.values);
    } else {
      arr = this.yearlyRecurrences.recurrenceList.values; 
    }
  }
  return arr;
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
        // MONTHLY: The <qualifier> <dayOfWeek> of every month
        String qualifier = getDayOfWeekQualifier();
        String dayOfWeek = getQualifiedDayOfWeek();
        trigDesc = Messages.getString("the") + " " + WeekOfMonth.valueOf(qualifier) + " " + DayOfWeek.valueOf(dayOfWeek) + " "
            + Messages.getString("ofEveryMonth");
      } else if (getDayOfWeekRecurrences().length > 0) {
        // WEEKLY: Every week on <day>..<day>
        trigDesc = Messages.getString("every") + " " + DayOfWeek.get(getDayOfWeekRecurrences()[0] - 1).toString().trim();
        for (int i = 1; i < getDayOfWeekRecurrences().length; i++) {
          trigDesc += ", " + DayOfWeek.get(getDayOfWeekRecurrences()[i] - 1).toString().trim();
        }
      }
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
}