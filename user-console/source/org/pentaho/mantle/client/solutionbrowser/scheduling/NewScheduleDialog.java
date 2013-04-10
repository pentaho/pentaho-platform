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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jul 30, 2008 
 * @author wseyler
 */
package org.pentaho.mantle.client.solutionbrowser.scheduling;

import java.util.Date;
import java.util.List;

import org.pentaho.gwt.widgets.client.controls.schededitor.RecurrenceEditor.DailyRecurrenceEditor;
import org.pentaho.gwt.widgets.client.controls.schededitor.RecurrenceEditor.MonthlyRecurrenceEditor;
import org.pentaho.gwt.widgets.client.controls.schededitor.RecurrenceEditor.WeeklyRecurrenceEditor;
import org.pentaho.gwt.widgets.client.controls.schededitor.RecurrenceEditor.YearlyRecurrenceEditor;
import org.pentaho.gwt.widgets.client.controls.schededitor.ScheduleEditor;
import org.pentaho.gwt.widgets.client.controls.schededitor.ScheduleEditor.ScheduleType;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.TimeUtil;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.DayOfWeek;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.MonthOfYear;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.WeekOfMonth;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.gwt.widgets.client.wizards.IWizardPanel;
import org.pentaho.gwt.widgets.client.wizards.panels.JsSchedulingParameter;
import org.pentaho.gwt.widgets.client.wizards.panels.ScheduleEditorWizardPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.workspace.BlockoutPanel;
import org.pentaho.mantle.client.workspace.JsBlockStatus;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobParam;
import org.pentaho.mantle.client.workspace.JsJobTrigger;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 * 
 */
public class NewScheduleDialog extends AbstractWizardDialog {
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
  String filePath;
  IDialogCallback callback;
  boolean isBlockoutDialog = false;

  ScheduleEmailDialog scheduleEmailDialog;
  ScheduleParamsDialog scheduleParamsDialog;
  ScheduleEditorWizardPanel scheduleEditorWizardPanel;
  JsJob editJob;

  Boolean done = false;
  boolean hasParams = false;
  boolean isEmailConfValid = false;
  private ScheduleEditor scheduleEditor;

  public NewScheduleDialog(JsJob jsJob, IDialogCallback callback, boolean hasParams, boolean isEmailConfValid, boolean showScheduleName, final ScheduleDialogType type) {
    super(type, Messages.getString("editSchedule"), null, false, true); //$NON-NLS-1$
    isBlockoutDialog = (type == ScheduleDialogType.BLOCKOUT);
    setCallback(callback);
    this.editJob = jsJob;
    constructDialog(jsJob.getFullResourceName(), hasParams, isEmailConfValid, showScheduleName, jsJob);
  }

  public NewScheduleDialog(String filePath, IDialogCallback callback, boolean hasParams, boolean isEmailConfValid) {
    super(ScheduleDialogType.SCHEDULER, Messages.getString("newSchedule"), null, false, true); //$NON-NLS-1$
    isBlockoutDialog = false;
    setCallback(callback);
    constructDialog(filePath, hasParams, isEmailConfValid, true, null);
  }

  public NewScheduleDialog(ScheduleDialogType type, String title, String filePath, IDialogCallback callback, boolean hasParams, boolean isEmailConfValid) {
    super(type, title, null, false, true);
    isBlockoutDialog = (type == ScheduleDialogType.BLOCKOUT);
    setCallback(callback);
    constructDialog(filePath, hasParams, isEmailConfValid, true, null);
  }

  public boolean onKeyDownPreview(char key, int modifiers) {
    if (key == KeyCodes.KEY_ESCAPE) {
      hide();
    }
    return true;
  }

  public void addCustomPanel(Widget w, DockPanel.DockLayoutConstant position) {
    scheduleEditorWizardPanel.add(w, position);
  }
  
  private void constructDialog(String filePath, boolean hasParams, boolean isEmailConfValid, boolean showScheduleName, JsJob jsJob) {
    this.hasParams = hasParams;
    this.filePath = filePath;
    this.isEmailConfValid = isEmailConfValid;
    scheduleEditorWizardPanel = new ScheduleEditorWizardPanel(getDialogType(), showScheduleName);
    scheduleEditor = scheduleEditorWizardPanel.getScheduleEditor();
    scheduleEditor.setBlockoutButtonHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent clickEvent) {
        PromptDialogBox box = new PromptDialogBox(Messages.getString("blockoutTimes"), Messages.getString("close"),
                                                  null, null, false, true, new BlockoutPanel(false));
        box.center();
      }
    });
    IWizardPanel[] wizardPanels = { scheduleEditorWizardPanel };
    this.setWizardPanels(wizardPanels);
    setPixelSize(475, 465);
    center();
    if ((hasParams || isEmailConfValid) && (isBlockoutDialog == false)) {
      finishButton.setText(Messages.getString("nextStep"));
    } else {
      finishButton.setText(Messages.getString("ok"));
    }
    setupExisting(jsJob);
    setHeight("100%");
  }

  private void setupExisting(JsJob jsJob) {
    if (jsJob != null && !jsJob.equals("")) {
      JsJobTrigger jsJobTrigger = jsJob.getJobTrigger();
      ScheduleType scheduleType = ScheduleType.valueOf(jsJobTrigger.getScheduleType());
      scheduleEditor.setScheduleName(jsJob.getJobName());
      scheduleEditor.setScheduleType(scheduleType);
      if (scheduleType == ScheduleType.CRON || jsJobTrigger.getType().equals("cronJobTrigger")) {
        scheduleEditor.getCronEditor().setCronString(jsJobTrigger.getCronString());
      } else if (jsJobTrigger.getType().equals("simpleJobTrigger")) {
        if (jsJobTrigger.getRepeatCount() == -1) {
          // Recurring simple Trigger
          int interval = jsJobTrigger.getRepeatInterval();

          scheduleEditor.setRepeatInSecs(interval);
          if (scheduleType == ScheduleType.DAILY) {
            DailyRecurrenceEditor dailyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getDailyEditor();
            dailyRecurrenceEditor.setEveryNDays();
          } else if (scheduleType == ScheduleType.MONTHLY) {

          }
        }
      } else if (jsJobTrigger.getType().equals("complexJobTrigger")) {
        if (scheduleType == ScheduleType.DAILY) {
          // Daily
          DailyRecurrenceEditor dailyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getDailyEditor();
          if (jsJobTrigger.isWorkDaysInWeek()) {
            dailyRecurrenceEditor.setEveryWeekday();
          } else {
            dailyRecurrenceEditor.setEveryNDays();
          }
        } else if (scheduleType == ScheduleType.WEEKLY) {
          int[] daysOfWeek = jsJobTrigger.getDayOfWeekRecurrences();
          WeeklyRecurrenceEditor weeklyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getWeeklyEditor();
          String strDays = "";
          for (int i = 0; i < daysOfWeek.length; i++) {
            strDays += Integer.toString(daysOfWeek[i]) + ",";
          }
          weeklyRecurrenceEditor.setCheckedDaysAsString(strDays, 1);
        } else if (scheduleType == ScheduleType.MONTHLY) {
          MonthlyRecurrenceEditor monthlyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getMonthlyEditor();
          if (jsJobTrigger.isQualifiedDayOfWeekRecurrence()) {
            // Run Every on ___day of Nth week every month
            monthlyRecurrenceEditor.setDayOfWeek(TimeUtil.DayOfWeek.valueOf(jsJobTrigger.getQualifiedDayOfWeek()));
            monthlyRecurrenceEditor.setWeekOfMonth(TimeUtil.WeekOfMonth.valueOf(jsJobTrigger.getDayOfWeekQualifier()));
            monthlyRecurrenceEditor.setNthDayNameOfMonth();
          } else {
            // Run on Nth day of the month
            monthlyRecurrenceEditor.setDayOfMonth(Integer.toString(jsJobTrigger.getDayOfMonthRecurrences()[0]));
          }
        } else if (scheduleType == ScheduleType.YEARLY) {
          YearlyRecurrenceEditor yearlyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getYearlyEditor();
          if (jsJobTrigger.isQualifiedDayOfWeekRecurrence()) {
            // Run Every on ___day of Nth week of the month M yearly
            yearlyRecurrenceEditor.setDayOfWeek(TimeUtil.DayOfWeek.valueOf(jsJobTrigger.getQualifiedDayOfWeek()));
            yearlyRecurrenceEditor.setWeekOfMonth(TimeUtil.WeekOfMonth.valueOf(jsJobTrigger.getDayOfWeekQualifier()));
            yearlyRecurrenceEditor.setMonthOfYear1(TimeUtil.MonthOfYear.get(jsJobTrigger.getMonthlyRecurrences()[0] - 1));
            yearlyRecurrenceEditor.setNthDayNameOfMonthName();
          } else {
            // Run on Nth day of the month M yearly
            yearlyRecurrenceEditor.setDayOfMonth(Integer.toString(jsJobTrigger.getDayOfMonthRecurrences()[0]));
            yearlyRecurrenceEditor.setMonthOfYear0(TimeUtil.MonthOfYear.get(jsJobTrigger.getMonthlyRecurrences()[0] - 1));
            yearlyRecurrenceEditor.setEveryMonthOnNthDay();
          }
        }
      }
      scheduleEditor.setStartDate(jsJobTrigger.getStartTime());
      scheduleEditor.setStartTime(DateTimeFormat.getFormat(PredefinedFormat.HOUR_MINUTE_SECOND).format(jsJobTrigger.getStartTime()));
      if (jsJobTrigger.getEndTime() == null) {
        scheduleEditor.setNoEndDate();
      } else {
        scheduleEditor.setEndDate(jsJobTrigger.getEndTime());
        scheduleEditor.setEndBy();
      }

      if(isBlockoutDialog) {
        scheduleEditor.setBlockoutEndTime(
          DateTimeFormat.getFormat(
            PredefinedFormat.HOUR_MINUTE_SECOND).format(
            new Date(jsJobTrigger.getStartTime().getTime() + jsJobTrigger.getBlockDuration())));
      }
    }
  }

  protected JSONObject getJsonSimpleTrigger(int repeatCount, int interval, Date startDate, Date endDate) {
    JSONObject trigger = new JSONObject();
    trigger.put("uiPassParam", new JSONString(scheduleEditorWizardPanel.getScheduleType().name())); //$NON-NLS-1$
    trigger.put("repeatInterval", new JSONNumber(interval)); //$NON-NLS-1$
    trigger.put("repeatCount", new JSONNumber(repeatCount)); //$NON-NLS-1$
    addJsonStartEnd(trigger, startDate, endDate);
    return trigger;
  }

  protected JSONObject getJsonCronTrigger(String cronString, Date startDate, Date endDate) {
    JSONObject trigger = new JSONObject();
    trigger.put("uiPassParam", new JSONString(scheduleEditorWizardPanel.getScheduleType().name())); //$NON-NLS-1$
    trigger.put("cronString", new JSONString(cronString)); //$NON-NLS-1$
    addJsonStartEnd(trigger, startDate, endDate);
    return trigger;
  }

  protected JSONObject getJsonComplexTrigger(ScheduleType scheduleType, MonthOfYear month, WeekOfMonth weekOfMonth, List<DayOfWeek> daysOfWeek, Date startDate,
      Date endDate) {
    JSONObject trigger = new JSONObject();
    trigger.put("uiPassParam", new JSONString(scheduleEditorWizardPanel.getScheduleType().name())); //$NON-NLS-1$
    if (month != null) {
      JSONArray jsonArray = new JSONArray();
      jsonArray.set(0, new JSONString(Integer.toString(month.ordinal())));
      trigger.put("monthsOfYear", jsonArray); //$NON-NLS-1$
    }
    if (weekOfMonth != null) {
      JSONArray jsonArray = new JSONArray();
      jsonArray.set(0, new JSONString(Integer.toString(weekOfMonth.ordinal())));
      trigger.put("weeksOfMonth", jsonArray); //$NON-NLS-1$
    }
    if (daysOfWeek != null) {
      JSONArray jsonArray = new JSONArray();
      int index = 0;
      for (DayOfWeek dayOfWeek : daysOfWeek) {
        jsonArray.set(index++, new JSONString(Integer.toString(dayOfWeek.ordinal())));
      }
      trigger.put("daysOfWeek", jsonArray); //$NON-NLS-1$
    }
    addJsonStartEnd(trigger, startDate, endDate);
    return trigger;
  }

  protected JSONObject getJsonComplexTrigger(ScheduleType scheduleType, MonthOfYear month, int dayOfMonth, Date startDate, Date endDate) {
    JSONObject trigger = new JSONObject();
    trigger.put("uiPassParam", new JSONString(scheduleEditorWizardPanel.getScheduleType().name())); //$NON-NLS-1$

    if (month != null) {
      JSONArray jsonArray = new JSONArray();
      jsonArray.set(0, new JSONString(Integer.toString(month.ordinal())));
      trigger.put("monthsOfYear", jsonArray); //$NON-NLS-1$
    }

    JSONArray jsonArray = new JSONArray();
    jsonArray.set(0, new JSONString(Integer.toString(dayOfMonth)));
    trigger.put("daysOfMonth", jsonArray); //$NON-NLS-1$

    addJsonStartEnd(trigger, startDate, endDate);
    return trigger;
  }

  /**
   * Returns an object suitable for posting into quartz via the the "JOB" rest service.
   * 
   * @return
   */
  @SuppressWarnings("deprecation")
  public JSONObject getSchedule() {
    ScheduleType scheduleType = scheduleEditorWizardPanel.getScheduleType();
    Date startDate = scheduleEditorWizardPanel.getStartDate();
    String startTime = scheduleEditorWizardPanel.getStartTime();

    // For blockout periods, we need the blockout start time.
    if (isBlockoutDialog) {
      startTime = scheduleEditorWizardPanel.getBlockoutStartTime();
    }

    int startHour = getStartHour(startTime);
    int startMin = getStartMin(startTime);
    int startYear = startDate.getYear();
    int startMonth = startDate.getMonth();
    int startDay = startDate.getDate();
    Date startDateTime = new Date(startYear, startMonth, startDay, startHour, startMin);
    Date endDate = scheduleEditorWizardPanel.getEndDate();
    MonthOfYear monthOfYear = scheduleEditor.getRecurrenceEditor().getSelectedMonth();
    List<DayOfWeek> daysOfWeek = scheduleEditor.getRecurrenceEditor().getSelectedDaysOfWeek();
    Integer dayOfMonth = scheduleEditor.getRecurrenceEditor().getSelectedDayOfMonth();
    WeekOfMonth weekOfMonth = scheduleEditor.getRecurrenceEditor().getSelectedWeekOfMonth();

    JSONObject schedule = new JSONObject();
    schedule.put("jobName", new JSONString(scheduleEditor.getScheduleName()));


    if (scheduleType == ScheduleType.RUN_ONCE) { // Run once types
      schedule.put("simpleJobTrigger", getJsonSimpleTrigger(0, 0, startDateTime, null)); //$NON-NLS-1$
    } else if ((scheduleType == ScheduleType.SECONDS) || (scheduleType == ScheduleType.MINUTES) || (scheduleType == ScheduleType.HOURS)) {
      int repeatInterval = 0;
      try { // Simple Trigger Types
        repeatInterval = Integer.parseInt(scheduleEditorWizardPanel.getRepeatInterval());
      } catch (Exception e) {
      }
      schedule.put("simpleJobTrigger", getJsonSimpleTrigger(-1, repeatInterval, startDateTime, endDate)); //$NON-NLS-1$
    } else if (scheduleType == ScheduleType.DAILY) {
      if (scheduleEditor.getRecurrenceEditor().isEveryNDays()) {
        int repeatInterval = 0;
        try { // Simple Trigger Types
          repeatInterval = Integer.parseInt(scheduleEditorWizardPanel.getRepeatInterval());
        } catch (Exception e) {
        }
        schedule.put("simpleJobTrigger", getJsonSimpleTrigger(-1, repeatInterval, startDateTime, endDate)); //$NON-NLS-1$
      } else {
        schedule
            .put(
                "complexJobTrigger", getJsonComplexTrigger(scheduleType, null, null, scheduleEditor.getRecurrenceEditor().getSelectedDaysOfWeek(), startDateTime, endDate));//$NON-NLS-1$
      }
    } else if (scheduleType == ScheduleType.CRON) { // Cron jobs
      schedule.put("cronJobTrigger", getJsonCronTrigger(scheduleEditor.getCronString(), startDateTime, endDate));//$NON-NLS-1$
    } else if ((scheduleType == ScheduleType.WEEKLY) && (daysOfWeek.size() > 0)) {
      schedule
          .put(
              "complexJobTrigger", getJsonComplexTrigger(scheduleType, null, null, scheduleEditor.getRecurrenceEditor().getSelectedDaysOfWeek(), startDateTime, endDate));//$NON-NLS-1$
    } else if ((scheduleType == ScheduleType.MONTHLY) || ((scheduleType == ScheduleType.YEARLY) && (monthOfYear != null))) {
      if (dayOfMonth != null) {
        // YEARLY Run on specific day in year or MONTHLY Run on specific day in month
        schedule.put("complexJobTrigger", getJsonComplexTrigger(scheduleType, monthOfYear, dayOfMonth, startDateTime, endDate));//$NON-NLS-1$
      } else if ((daysOfWeek.size() > 0) && (weekOfMonth != null)) {
        // YEARLY
        schedule.put("complexJobTrigger", getJsonComplexTrigger(scheduleType, monthOfYear, weekOfMonth, daysOfWeek, startDateTime, endDate));//$NON-NLS-1$
      }
    }
    schedule.put("inputFile", new JSONString(filePath)); //$NON-NLS-1$ //$NON-NLS-2$
    schedule.put("outputFile", JSONNull.getInstance()); //$NON-NLS-1$
    return schedule;
  }

  @SuppressWarnings("deprecation")
  private JSONObject addJsonStartEnd(JSONObject trigger, Date startDate, Date endDate) {
    trigger.put("startTime", new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDate))); //$NON-NLS-1$
    if (endDate != null) {
      endDate.setHours(23);
      endDate.setMinutes(59);
      endDate.setSeconds(59);
    }
    trigger.put("endTime", endDate == null ? JSONNull.getInstance() : new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate))); //$NON-NLS-1$
    return trigger;
  }

  private Integer calculateBlockoutDuration() {
    final String endTime = scheduleEditorWizardPanel.getBlockoutEndTime();
    final Date today = new Date();

    if (endTime != null) {
      // We only care about the interval from start time to end time.
      final String startTime = scheduleEditorWizardPanel.getBlockoutStartTime();
      Date blackoutStartDate = new Date(today.getYear(), today.getMonth(), today.getDate(),
                                        getStartHour(startTime), getStartMin(startTime));

      Date blackoutEndDate = new Date(today.getYear(), today.getMonth(), today.getDate(),
                                      getStartHour(endTime), getStartMin(endTime));

      final int durationMilli = new Long(Math.abs(blackoutEndDate.getTime() - blackoutStartDate.getTime())).intValue();

//      // Debug only
//      long seconds = durationMilli / 1000;
//      System.out.println("******* Seconds: " + seconds + " and mintues = " + (seconds * 60) + " hours = " + (seconds * 60 * 60) + " days = " + (seconds * 60 * 60 * 24));
//      System.out.println("Seconds To Minute: " + TimeUtil.secsToMinutes(seconds));
//      System.out.println("Seconds to Hours: " + TimeUtil.secsToHours(seconds));
//      System.out.println("Seconds to Days: " + TimeUtil.secsToDays(seconds));

      return durationMilli;
    } else {
      return -1;
    }
  }

  @SuppressWarnings("deprecation")
  public JsJobTrigger getJsJobTrigger() {
    JsJobTrigger jsJobTrigger = JsJobTrigger.instance();

    ScheduleType scheduleType = scheduleEditorWizardPanel.getScheduleType();
    Date startDate = scheduleEditorWizardPanel.getStartDate();
    String startTime = scheduleEditorWizardPanel.getStartTime();

    int startHour = getStartHour(startTime);
    int startMin = getStartMin(startTime);
    int startYear = startDate.getYear();
    int startMonth = startDate.getMonth();
    int startDay = startDate.getDate();
    Date startDateTime = new Date(startYear, startMonth, startDay, startHour, startMin);

    Date endDate = scheduleEditorWizardPanel.getEndDate();
    MonthOfYear monthOfYear = scheduleEditor.getRecurrenceEditor().getSelectedMonth();
    List<DayOfWeek> daysOfWeek = scheduleEditor.getRecurrenceEditor().getSelectedDaysOfWeek();
    Integer dayOfMonth = scheduleEditor.getRecurrenceEditor().getSelectedDayOfMonth();
    WeekOfMonth weekOfMonth = scheduleEditor.getRecurrenceEditor().getSelectedWeekOfMonth();

    if (isBlockoutDialog) {
      jsJobTrigger.setBlockDuration(calculateBlockoutDuration());
    } else {
      // blockDuration is only valid for blockouts
      jsJobTrigger.setBlockDuration(-1);
    }

    if (scheduleType == ScheduleType.RUN_ONCE) { // Run once types
      jsJobTrigger.setType("simpleJobTrigger");
      jsJobTrigger.setRepeatInterval(0);
      jsJobTrigger.setRepeatCount(0);
      jsJobTrigger.setNativeStartTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDateTime));
    } else if ((scheduleType == ScheduleType.SECONDS) || (scheduleType == ScheduleType.MINUTES) || (scheduleType == ScheduleType.HOURS)) {
      int repeatInterval = 0;
      try { // Simple Trigger Types
        repeatInterval = Integer.parseInt(scheduleEditorWizardPanel.getRepeatInterval());
      } catch (Exception e) {
      }
      jsJobTrigger.setType("simpleJobTrigger");
      jsJobTrigger.setRepeatInterval(repeatInterval);
      jsJobTrigger.setRepeatCount(-1);
      jsJobTrigger.setNativeStartTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDateTime));
      if (endDate != null) {
        jsJobTrigger.setNativeEndTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate));
      }
    } else if (scheduleType == ScheduleType.DAILY) {
      if (scheduleEditor.getRecurrenceEditor().isEveryNDays()) {
        int repeatInterval = 0;
        try { // Simple Trigger Types
          repeatInterval = Integer.parseInt(scheduleEditorWizardPanel.getRepeatInterval());
        } catch (Exception e) {
        }
        jsJobTrigger.setType("simpleJobTrigger");
        jsJobTrigger.setRepeatInterval(repeatInterval);
        jsJobTrigger.setRepeatCount(-1);
        jsJobTrigger.setNativeStartTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDateTime));
        if (endDate != null) {
          jsJobTrigger.setNativeEndTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate));
        }
      } else {
        JsArrayInteger jsDaysOfWeek = (JsArrayInteger) JavaScriptObject.createArray();
        int i = 0;
        for (DayOfWeek dayOfWeek : daysOfWeek) {
          jsDaysOfWeek.set(i++, dayOfWeek.ordinal() + 1);
        }
        JsArrayInteger hours = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set(0, startHour);
        JsArrayInteger minutes = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set(0, startMin);
        JsArrayInteger seconds = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set(0, 0);

        jsJobTrigger.setType("complexJobTrigger");
        jsJobTrigger.setDayOfWeekRecurrences(jsDaysOfWeek);
        jsJobTrigger.setHourRecurrences(hours);
        jsJobTrigger.setMinuteRecurrences(minutes);
        jsJobTrigger.setSecondRecurrences(seconds);
        jsJobTrigger.setNativeStartTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDateTime));
        if (endDate != null) {
          jsJobTrigger.setNativeEndTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate));
        }
      }
    } else if (scheduleType == ScheduleType.CRON) { // Cron jobs
      jsJobTrigger.setType("cronJobTrigger");
    } else if ((scheduleType == ScheduleType.WEEKLY) && (daysOfWeek.size() > 0)) {
      JsArrayInteger jsDaysOfWeek = (JsArrayInteger) JavaScriptObject.createArray();
      int i = 0;
      for (DayOfWeek dayOfWeek : daysOfWeek) {
        jsDaysOfWeek.set(i++, dayOfWeek.ordinal() + 1);
      }
      JsArrayInteger hours = (JsArrayInteger) JavaScriptObject.createArray();
      hours.set(0, startHour);
      JsArrayInteger minutes = (JsArrayInteger) JavaScriptObject.createArray();
      hours.set(0, startMin);
      JsArrayInteger seconds = (JsArrayInteger) JavaScriptObject.createArray();
      hours.set(0, 0);

      jsJobTrigger.setType("complexJobTrigger");
      jsJobTrigger.setDayOfWeekRecurrences(jsDaysOfWeek);
      jsJobTrigger.setHourRecurrences(hours);
      jsJobTrigger.setMinuteRecurrences(minutes);
      jsJobTrigger.setSecondRecurrences(seconds);
      jsJobTrigger.setNativeStartTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDateTime));
      if (endDate != null) {
        jsJobTrigger.setNativeEndTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate));
      }
    } else if ((scheduleType == ScheduleType.MONTHLY) || ((scheduleType == ScheduleType.YEARLY) && (monthOfYear != null))) {
      jsJobTrigger.setType("complexJobTrigger");

      if (dayOfMonth != null) {
        JsArrayInteger jsDaysOfMonth = (JsArrayInteger) JavaScriptObject.createArray();
        jsDaysOfMonth.set(0, dayOfMonth);

        JsArrayInteger hours = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set(0, startHour);
        JsArrayInteger minutes = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set(0, startMin);
        JsArrayInteger seconds = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set(0, 0);

        jsJobTrigger.setType("complexJobTrigger");
        if (monthOfYear != null) {
          JsArrayInteger jsMonthsOfYear = (JsArrayInteger) JavaScriptObject.createArray();
          jsMonthsOfYear.set(0, monthOfYear.ordinal() + 1);
          jsJobTrigger.setMonthlyRecurrences(jsMonthsOfYear);
        }
        jsJobTrigger.setDayOfMonthRecurrences(jsDaysOfMonth);
        jsJobTrigger.setHourRecurrences(hours);
        jsJobTrigger.setMinuteRecurrences(minutes);
        jsJobTrigger.setSecondRecurrences(seconds);
        jsJobTrigger.setNativeStartTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDateTime));
        if (endDate != null) {
          jsJobTrigger.setNativeEndTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate));
        }
      } else if ((daysOfWeek.size() > 0) && (weekOfMonth != null)) {
        JsArrayInteger jsDaysOfWeek = (JsArrayInteger) JavaScriptObject.createArray();
        int i = 0;
        for (DayOfWeek dayOfWeek : daysOfWeek) {
          jsDaysOfWeek.set(i++, dayOfWeek.ordinal() + 1);
        }

        JsArrayInteger hours = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set(0, startHour);
        JsArrayInteger minutes = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set(0, startMin);
        JsArrayInteger seconds = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set(0, 0);

        jsJobTrigger.setType("complexJobTrigger");
        if (monthOfYear != null) {
          JsArrayInteger jsMonthsOfYear = (JsArrayInteger) JavaScriptObject.createArray();
          jsMonthsOfYear.set(0, monthOfYear.ordinal() + 1);
          jsJobTrigger.setMonthlyRecurrences(jsMonthsOfYear);
        }
        jsJobTrigger.setHourRecurrences(hours);
        jsJobTrigger.setMinuteRecurrences(minutes);
        jsJobTrigger.setSecondRecurrences(seconds);
        jsJobTrigger.setQualifiedDayOfWeek(daysOfWeek.get(0).name());
        jsJobTrigger.setDayOfWeekQualifier(weekOfMonth.name());
        jsJobTrigger.setNativeStartTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDateTime));
        if (endDate != null) {
          jsJobTrigger.setNativeEndTime(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate));
        }
      }
    }
    return jsJobTrigger;
  }

  protected boolean addBlockoutPeriod(final JSONObject schedule, final JsJobTrigger trigger, String urlSuffix) {
    String url = GWT.getHostPageBaseURL() + "api/scheduler/blockout/" + urlSuffix; //$NON-NLS-1$

    RequestBuilder addBlockoutPeriodRequest = new RequestBuilder(RequestBuilder.POST, url);
    addBlockoutPeriodRequest.setHeader("accept", "text/plain");
    addBlockoutPeriodRequest.setHeader("Content-Type", "application/json");

    // Create a unique blockout period name
    final Integer duration = trigger.getBlockDuration();
    final String blockoutPeriodName = trigger.getScheduleType() + Random.nextInt() + ":" +
                                      /*PentahoSessionHolder.getSession().getName()*/  "admin" + ":" + duration;

    // Add blockout specific parameters
    JSONObject addBlockoutParams = schedule;
    addBlockoutParams.put("jobName", new JSONString(blockoutPeriodName)); //$NON-NLS-1$
    addBlockoutParams.put("duration", new JSONNumber(duration)); //$NON-NLS-1$

    System.out.println("The add blockout json: " + addBlockoutParams.toString());

    try {
      addBlockoutPeriodRequest.sendRequest(addBlockoutParams.toString(), new RequestCallback()
      {
        public void onError(Request request, Throwable exception)
        {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.toString(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
          setDone(false);
        }

        public void onResponseReceived(Request request, Response response)
        {
          if (response.getStatusCode() == Response.SC_OK)
          {
            System.out.println("****** Got a valid response after adding a blockout period: " + response.getStatusCode());
          }
        }
      });
    } catch (RequestException e) {
    }

    return true;
  }

  private void promptDueToBlockoutConflicts(final boolean alwaysConflict, final boolean conflictsSometimes,
                                            final JSONObject schedule, final JsJobTrigger trigger) {
    StringBuffer conflictMessage = new StringBuffer();

    final String updateScheduleButtonText = Messages.getString("blockoutUpdateSchedule");
    final String continueButtonText = Messages.getString("blockoutContinueSchedule");

    boolean showContinueButton =  conflictsSometimes;
    boolean isScheduleConflict = alwaysConflict || conflictsSometimes;

    if (conflictsSometimes) {
      conflictMessage.append(Messages.getString("blockoutPartialConflict"));
      conflictMessage.append("\n");
      conflictMessage.append(Messages.getString("blockoutPartialConflictContinue"));
    } else {
      conflictMessage.append(Messages.getString("blockoutTotalConflict"));
    }

    if (isScheduleConflict) {
      final MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("blockoutTimeExists"),
                                                              conflictMessage.toString(),
                                                              false, false, true,
                                                              updateScheduleButtonText,
                                                              showContinueButton ? continueButtonText : null,
                                                              null); //$NON-NLS-1$ //$NON-NLS-2$
      dialogBox.setCallback(new IDialogCallback() {
        // If user clicked on 'Continue' we want to add the schedule.  Otherwise we dismiss the dialog
        // and they have to modify the recurrence schedule
        public void cancelPressed() {
          // User clicked on continue, so we need to proceed adding the schedule
          handleWizardPanels(schedule, trigger);
        }

        public void okPressed() {
          // Update Schedule Button pressed
          dialogBox.setVisible(false);
        }
      });

      dialogBox.center();
    }
  }


  /**
   * Before creating a new schedule, we want to check to see if the schedule that is being
   * created is going to conflict with any one of the blockout periods if one is provisioned.
   * @param schedule
   * @param trigger
   */
  protected void verifyBlockoutConflict(final JSONObject schedule, final JsJobTrigger trigger) {
    String url = GWT.getHostPageBaseURL() + "api/scheduler/blockout/blockstatus"; //$NON-NLS-1$

    RequestBuilder blockoutConflictRequest = new RequestBuilder(RequestBuilder.POST, url);
    blockoutConflictRequest.setHeader("accept", "application/json");
    blockoutConflictRequest.setHeader("Content-Type", "application/json");

    final JSONObject verifyBlockoutParams = schedule;
    verifyBlockoutParams.put("jobName", new JSONString(scheduleEditorWizardPanel.getScheduleEditor().getScheduleName())); //$NON-NLS-1$

//    System.out.println("The verify blockout conflict json: " + verifyBlockoutParams.toString());

    try {
      blockoutConflictRequest.sendRequest(verifyBlockoutParams.toString(), new RequestCallback()
      {
        public void onError(Request request, Throwable exception)
        {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.toString(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
          setDone(false);
        }

        public void onResponseReceived(Request request, Response response)
        {
          if (response.getStatusCode() == Response.SC_OK)
          {
            JsBlockStatus statusResponse = (JsBlockStatus) parseJson(JsonUtils.escapeJsonForEval(response.getText()));

            // Determine if this schedule conflicts all the time or some of the time
            boolean partiallyBlocked =  Boolean.parseBoolean(statusResponse.getPartiallyBlocked());
            boolean totallyBlocked = Boolean.parseBoolean(statusResponse.getTotallyBlocked());
            if (partiallyBlocked || totallyBlocked) {
              promptDueToBlockoutConflicts(totallyBlocked, partiallyBlocked, schedule, trigger);
            } else {
              // Continue with other panels in the wizard (params, email)
              handleWizardPanels(schedule, trigger);
            }
          } else {
            handleWizardPanels(schedule, trigger);
          }
        }
      });
    } catch (RequestException e) {
    }

    super.nextClicked();
  }

  private void handleWizardPanels(final JSONObject schedule, final JsJobTrigger trigger)
  {
    if (hasParams)
    {
      showScheduleParamsDialog(trigger, schedule);
    }
    else if (isEmailConfValid)
    {
      showScheduleEmailDialog(schedule);
    }
    else
    {
      // submit
      JSONObject scheduleRequest = (JSONObject) JSONParser.parseStrict(schedule.toString());

      if (editJob != null)
      {
        JSONArray scheduleParams = new JSONArray();

        for (int i = 0; i < editJob.getJobParams().length(); i++)
        {
          JsJobParam param = editJob.getJobParams().get(i);
          JsArrayString paramValue = (JsArrayString) JavaScriptObject.createArray().cast();
          paramValue.push(param.getValue());
          JsSchedulingParameter p = (JsSchedulingParameter) JavaScriptObject.createObject().cast();
          p.setName(param.getName());
          p.setType("string");
          p.setStringValue(paramValue);
          scheduleParams.set(i, new JSONObject(p));
        }

        scheduleRequest.put("jobParameters", scheduleParams); //$NON-NLS-1$

        String actionClass = editJob.getJobParam("ActionAdapterQuartzJob-ActionClass");
        if (!StringUtils.isEmpty(actionClass))
        {
          scheduleRequest.put("actionClass", new JSONString(actionClass));
        }

      }

      RequestBuilder scheduleFileRequestBuilder = new RequestBuilder(RequestBuilder.POST, contextURL + "api/scheduler/job");
      scheduleFileRequestBuilder.setHeader("Content-Type", "application/json"); //$NON-NLS-1$//$NON-NLS-2$

      try
      {
        scheduleFileRequestBuilder.sendRequest(scheduleRequest.toString(), new RequestCallback()
        {
          public void onError(Request request, Throwable exception)
          {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.toString(), false, false, true); //$NON-NLS-1$
            dialogBox.center();
            setDone(false);
          }

          public void onResponseReceived(Request request, Response response)
          {
            if (response.getStatusCode() == 200)
            {
              setDone(true);
              NewScheduleDialog.this.hide();
              if (callback != null)
              {
                callback.okPressed();
              }
            }
            else
            {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"),
                                                                response.getText(), //$NON-NLS-1$ 
                                                                false, false, true);
              dialogBox.center();
              setDone(false);
            }
          }
        });
      }
      catch (RequestException e)
      {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.toString(), //$NON-NLS-1$
                                                          false, false, true);
        dialogBox.center();
        setDone(false);
      }

      setDone(true);
    }
  }

  private final native JavaScriptObject parseJson(String json)
  /*-{
      if (null == json || "" == json) {
          return null;
      }
      var obj = eval('(' + json + ')');
      return obj;
  }-*/;


  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#finish()
   */
  @Override
  protected boolean onFinish() {
    JsJobTrigger trigger = getJsJobTrigger();
    JSONObject schedule = getSchedule();

    String name = scheduleEditorWizardPanel.getScheduleEditor().getScheduleName();
    String alphaNumeric = "^[a-zA-Z0-9_\\.\\- ]+$";
    // make sure it matches regex
    if (name.matches(alphaNumeric)) {
      verifyBlockoutConflict(schedule, trigger);
      return true;
    } else {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("enterAlphaNumeric", name), false, false, true); //$NON-NLS-1$
      dialogBox.center();
      return false;
    }
  }

  private void showScheduleEmailDialog(final JSONObject schedule) {
    try {
      final String url = GWT.getHostPageBaseURL() + "api/mantle/isAuthenticated"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
      requestBuilder.setHeader("accept", "text/plain");
      requestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable caught) {
          MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

            public void onFailure(Throwable caught) {
            }

            public void onSuccess(Boolean result) {
              showScheduleEmailDialog(schedule);
            }
          });
        }

        public void onResponseReceived(Request request, Response response) {
          JSONObject scheduleRequest = (JSONObject) JSONParser.parseStrict(schedule.toString());
          if (scheduleEmailDialog == null) {
            scheduleEmailDialog = new ScheduleEmailDialog(NewScheduleDialog.this, filePath, scheduleRequest, null, editJob);
            scheduleEmailDialog.setCallback(callback);
          } else {
            scheduleEmailDialog.setJobSchedule(scheduleRequest);
          }
          scheduleEmailDialog.center();
          hide();
        }

      });
    } catch (RequestException e) {
      Window.alert(e.getMessage());
    }

  }

  private void showScheduleParamsDialog(final JsJobTrigger trigger, final JSONObject schedule) {
    try {
      final String url = GWT.getHostPageBaseURL() + "api/mantle/isAuthenticated"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
      requestBuilder.setHeader("accept", "text/plain");
      requestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable caught) {
          MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

            public void onFailure(Throwable caught) {
            }

            public void onSuccess(Boolean result) {
              showScheduleParamsDialog(trigger, schedule);
            }
          });
        }

        public void onResponseReceived(Request request, Response response) {
          if (scheduleParamsDialog == null) {
            scheduleParamsDialog = new ScheduleParamsDialog(NewScheduleDialog.this, isEmailConfValid, editJob);
            scheduleParamsDialog.setCallback(callback);
          } else {
            scheduleParamsDialog.setJobSchedule(schedule);
          }
          if (trigger.getDescription() != null) {
            String description = Messages.getString("scheduleWillRun", trigger.getDescription().toLowerCase());
            scheduleParamsDialog.setScheduleDescription(description);
          }
          scheduleParamsDialog.center();
          hide();
        }

      });
    } catch (RequestException e) {
      Window.alert(e.getMessage());
    }

  }

  /**
   * @param startTime
   * @return
   */
  private int getStartMin(String startTime) {
    if (startTime == null || startTime.length() < 1) {
      return 0;
    }
    int firstSeparator = startTime.indexOf(':');
    int secondSeperator = startTime.indexOf(':', firstSeparator + 1);
    int min = Integer.parseInt(startTime.substring(firstSeparator + 1, secondSeperator));
    return min;
  }

  /**
   * @param startTime
   * @return
   */
  private int getStartHour(String startTime) {
    if (startTime == null || startTime.length() < 1) {
      return 0;
    }
    int afternoonOffset = startTime.endsWith("PM") ? 12 : 0; //$NON-NLS-1$
    int hour = Integer.parseInt(startTime.substring(0, startTime.indexOf(':')));
    hour += afternoonOffset;
    return hour;
  }

  public Boolean getDone() {
    return done;
  }

  public void setDone(Boolean done) {
    this.done = done;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onNext(org.pentaho.gwt.widgets.client.wizards.IWizardPanel,
   * org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onNext(IWizardPanel nextPanel, IWizardPanel previousPanel) {
    // TODO Auto-generated method stub
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onPrevious(org.pentaho.gwt.widgets.client.wizards.IWizardPanel,
   * org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onPrevious(IWizardPanel previousPanel, IWizardPanel currentPanel) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public void center() {
    super.center();
    Timer t = new Timer() {
      public void run() {
        scheduleEditorWizardPanel.setFocus();
        if (scheduleEditorWizardPanel.isAttached() && scheduleEditorWizardPanel.isVisible()) {
          cancel();
        }
      }
    };
    t.scheduleRepeating(250);
  }

  protected boolean enableNext(int index) {
    return true;
  }

  protected boolean showBack(int index) {
    return false;
  }

  protected boolean showFinish(int index) {
    // TODO Auto-generated method stub
    return true;
  }

  protected boolean showNext(int index) {
    // TODO Auto-generated method stub
    return false;
  }

  public void setCallback(IDialogCallback callback) {
    this.callback = callback;
  }

  public IDialogCallback getCallback() {
    return callback;
  }

}
