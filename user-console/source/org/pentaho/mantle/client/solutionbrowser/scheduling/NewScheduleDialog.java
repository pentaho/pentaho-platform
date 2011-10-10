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

import org.pentaho.gwt.widgets.client.controls.schededitor.ScheduleEditor.ScheduleType;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.DayOfWeek;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.MonthOfYear;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.WeekOfMonth;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.gwt.widgets.client.wizards.IWizardPanel;
import org.pentaho.gwt.widgets.client.wizards.panels.ScheduleEditorWizardPanel;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.web.http.api.resources.SimpleJobScheduleRequest;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author wseyler
 *
 */
public class NewScheduleDialog extends AbstractWizardDialog {
  FileItem fileItem = null;
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
  
  ScheduleEditorWizardPanel scheduleEditorWizardPanel = new ScheduleEditorWizardPanel();
  
  String path;
  
  Boolean done = false;
  /**
   * @param path
   */
  public NewScheduleDialog(String path) {
    super(Messages.getString("newSchedule"), null, false, true); //$NON-NLS-1$
    this.path = path;
    
    IWizardPanel[] wizardPanels = {scheduleEditorWizardPanel};
    this.setWizardPanels(wizardPanels);
    setPixelSize(475, 465);
  }

  protected JSONObject getJsonSimpleTrigger(int repeatCount, int interval, Date startDate, Date endDate) {
    JSONObject trigger = new JSONObject();
    trigger.put("repeatInterval", new JSONNumber(interval)); //$NON-NLS-1$
    trigger.put("repeatCount", new JSONNumber(repeatCount)); //$NON-NLS-1$
    trigger.put("startTime", new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDate))); //$NON-NLS-1$
    if (endDate != null) {
      endDate.setHours(23);
      endDate.setMinutes(59);
      endDate.setSeconds(59);
    }
    trigger.put("endTime", endDate == null ? JSONNull.getInstance() : new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate))); //$NON-NLS-1$
    return trigger;
  }
  
  protected JSONObject getJsonComplexTrigger(MonthOfYear month, WeekOfMonth weekOfMonth, List<DayOfWeek> daysOfWeek, Date startDate, Date endDate) {
    JSONObject trigger = new JSONObject();
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
    
    trigger.put("startTime", new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDate))); //$NON-NLS-1$
    if (endDate != null) {
      endDate.setHours(23);
      endDate.setMinutes(59);
      endDate.setSeconds(59);
    }
    trigger.put("endTime", endDate == null ? JSONNull.getInstance() : new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate))); //$NON-NLS-1$
    return trigger;
  }
  
  protected JSONObject getJsonComplexTrigger(MonthOfYear month, int dayOfMonth, Date startDate, Date endDate) {
    JSONObject trigger = new JSONObject();
    
    if (month != null) {
      JSONArray jsonArray = new JSONArray();
      jsonArray.set(0, new JSONString(Integer.toString(month.ordinal())));
      trigger.put("monthsOfYear", jsonArray); //$NON-NLS-1$
    }
    
    JSONArray jsonArray = new JSONArray();
    jsonArray.set(0, new JSONString(Integer.toString(dayOfMonth)));
    trigger.put("daysOfMonth", jsonArray); //$NON-NLS-1$
    
    trigger.put("startTime", new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDate))); //$NON-NLS-1$
    if (endDate != null) {
      endDate.setHours(23);
      endDate.setMinutes(59);
      endDate.setSeconds(59);
    }
    trigger.put("endTime", endDate == null ? JSONNull.getInstance() : new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate))); //$NON-NLS-1$
    return trigger;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#finish()
   */
  @Override
  protected boolean onFinish() {
    String scheduleJobUrl = contextURL + "api/scheduler/job"; //$NON-NLS-1$
    
    ScheduleType scheduleType = scheduleEditorWizardPanel.getScheduleType();

    Date startDate = scheduleEditorWizardPanel.getStartDate();
    String startTime = scheduleEditorWizardPanel.getStartTime();
    int startHour = getStartHour(startTime);
    int startMin = getStartMin(startTime);
    int startYear = startDate.getYear();
    int startMonth = startDate.getMonth();
    int startDay = startDate.getDate();
    Date startDateTime = new Date(startYear, startMonth, startDay, startHour, startMin );
    Date endDate = scheduleEditorWizardPanel.getEndDate();
    MonthOfYear monthOfYear = scheduleEditorWizardPanel.getScheduleEditor().getRecurrenceEditor().getSelectedMonth();
    List<DayOfWeek> daysOfWeek = scheduleEditorWizardPanel.getScheduleEditor().getRecurrenceEditor().getSelectedDaysOfWeek();
    Integer dayOfMonth = scheduleEditorWizardPanel.getScheduleEditor().getRecurrenceEditor().getSelectedDayOfMonth();
    WeekOfMonth weekOfMonth = scheduleEditorWizardPanel.getScheduleEditor().getRecurrenceEditor().getSelectedWeekOfMonth();
    
    JSONObject scheduleRequest = new JSONObject();
    scheduleRequest.put("inputFile", new JSONString(path)); //$NON-NLS-1$ //$NON-NLS-2$
    scheduleRequest.put("outputFile", JSONNull.getInstance()); //$NON-NLS-1$
    
    if (scheduleType == ScheduleType.RUN_ONCE) { // Run once types 
      scheduleRequest.put("simpleJobTrigger", getJsonSimpleTrigger(0, 0, startDateTime, null)); //$NON-NLS-1$
    } else if ((scheduleType == ScheduleType.SECONDS) 
        || (scheduleType == ScheduleType.MINUTES) 
        || (scheduleType == ScheduleType.HOURS)) {
      int repeatInterval = 0;
      try { // Simple Trigger Types   
        repeatInterval = Integer.parseInt(scheduleEditorWizardPanel.getRepeatInterval());
      } catch (Exception e) {
      }
      scheduleRequest.put("simpleJobTrigger", getJsonSimpleTrigger(-1, repeatInterval, startDateTime, endDate)); //$NON-NLS-1$
    } else if (scheduleType == ScheduleType.DAILY) {
      if (scheduleEditorWizardPanel.getScheduleEditor().getRecurrenceEditor().isEveryNDays()) {
        int repeatInterval = 0;
        try { // Simple Trigger Types   
          repeatInterval = Integer.parseInt(scheduleEditorWizardPanel.getRepeatInterval());
        } catch (Exception e) {
        }
        scheduleRequest.put("simpleJobTrigger", getJsonSimpleTrigger(-1, repeatInterval, startDateTime, endDate)); //$NON-NLS-1$
      } else {
        scheduleRequest.put("complexJobTrigger", getJsonComplexTrigger(null, null, scheduleEditorWizardPanel.getScheduleEditor().getRecurrenceEditor().getSelectedDaysOfWeek(), startDateTime, endDate));//$NON-NLS-1$
      }      
    } else if (scheduleType == ScheduleType.CRON) { // Cron jobs 
//      MantleServiceCache.getService().createSimpleTriggerJob(triggerName, triggerGroup, description, startDateTime, endDate, repeatCount, repeatInterval, solutionName, path, actionName, scheduleCallback);
    } else if ((scheduleType == ScheduleType.WEEKLY) && (daysOfWeek.size() > 0)) {
        scheduleRequest.put("complexJobTrigger", getJsonComplexTrigger(null, null, scheduleEditorWizardPanel.getScheduleEditor().getRecurrenceEditor().getSelectedDaysOfWeek(), startDateTime, endDate));//$NON-NLS-1$
    } else if ((scheduleType == ScheduleType.MONTHLY) || ((scheduleType == ScheduleType.YEARLY) && (monthOfYear != null))) {
      if (dayOfMonth != null) {
        scheduleRequest.put("complexJobTrigger", getJsonComplexTrigger(monthOfYear, dayOfMonth, startDateTime, endDate));//$NON-NLS-1$
      } else if ((daysOfWeek.size() > 0) && (weekOfMonth != null)) {
        scheduleRequest.put("complexJobTrigger", getJsonComplexTrigger(monthOfYear, weekOfMonth, daysOfWeek, startDateTime, endDate));//$NON-NLS-1$
      }
    }

    RequestBuilder scheduleFileRequestBuilder = new RequestBuilder(RequestBuilder.POST, scheduleJobUrl);
    scheduleFileRequestBuilder.setHeader("Content-Type", "application/json");  //$NON-NLS-1$//$NON-NLS-2$

    try {
      scheduleFileRequestBuilder.sendRequest(scheduleRequest.toString(), new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.toString(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
          setDone(false);
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == 200) {
            MessageDialogBox dialogBox = new MessageDialogBox(
                Messages.getString("info"), Messages.getString("actionSequenceScheduledSuccess"),  //$NON-NLS-1$ //$NON-NLS-2$
                true, false, true);
            dialogBox.center();
            setDone(true);
            NewScheduleDialog.this.hide();
          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotRun"), //$NON-NLS-1$ //$NON-NLS-2$
                false, false, true);
            dialogBox.center();
            setDone(false);
          }                
        }
        
      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotRun"), //$NON-NLS-1$ //$NON-NLS-2$
          false, false, true);
      dialogBox.center();
      setDone(false);
    }
      
    return getDone();
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
    int secondSeperator = startTime.indexOf(':', firstSeparator+1);
    int min = Integer.parseInt(startTime.substring(firstSeparator+1, secondSeperator));
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

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onNext(org.pentaho.gwt.widgets.client.wizards.IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onNext(IWizardPanel nextPanel, IWizardPanel previousPanel) {
    // TODO Auto-generated method stub
    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onPrevious(org.pentaho.gwt.widgets.client.wizards.IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onPrevious(IWizardPanel previousPanel, IWizardPanel currentPanel) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public void center() {
    // TODO Auto-generated method stub
    super.center();
    scheduleEditorWizardPanel.setFocus();
  }
  
}
