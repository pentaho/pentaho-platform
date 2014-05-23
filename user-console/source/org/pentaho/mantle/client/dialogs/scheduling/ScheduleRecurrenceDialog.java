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

package org.pentaho.mantle.client.dialogs.scheduling;

import java.util.Date;
import java.util.List;

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
import org.pentaho.mantle.client.dialogs.scheduling.RecurrenceEditor.DailyRecurrenceEditor;
import org.pentaho.mantle.client.dialogs.scheduling.RecurrenceEditor.MonthlyRecurrenceEditor;
import org.pentaho.mantle.client.dialogs.scheduling.RecurrenceEditor.WeeklyRecurrenceEditor;
import org.pentaho.mantle.client.dialogs.scheduling.RecurrenceEditor.YearlyRecurrenceEditor;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor.DurationValues;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor.ScheduleType;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.workspace.BlockoutPanel;
import org.pentaho.mantle.client.workspace.JsBlockStatus;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobParam;
import org.pentaho.mantle.client.workspace.JsJobTrigger;
import org.pentaho.mantle.login.client.MantleLoginDialog;

/**
 * @author wseyler
 * 
 */
public class ScheduleRecurrenceDialog extends AbstractWizardDialog {

  private final String moduleBaseURL = GWT.getModuleBaseURL();
  private final String moduleName = GWT.getModuleName();
  private final String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

  protected String filePath;
  protected String outputLocation;
  protected String scheduleName;

  private IDialogCallback callback;

  boolean isBlockoutDialog = false;
  private ScheduleEmailDialog scheduleEmailDialog;
  private ScheduleParamsDialog scheduleParamsDialog;
  private ScheduleEditorWizardPanel scheduleEditorWizardPanel;

  protected JsJob editJob;
  private Boolean done = false;
  private boolean hasParams = false;
  private boolean isEmailConfValid = false;
  private boolean showSuccessDialog = true;

  private ScheduleEditor scheduleEditor;

  private PromptDialogBox parentDialog;

  public ScheduleRecurrenceDialog( PromptDialogBox parentDialog, JsJob jsJob, IDialogCallback callback,
      boolean hasParams, boolean isEmailConfValid, final ScheduleDialogType type ) {
    super( type, type != ScheduleDialogType.BLOCKOUT
        ? Messages.getString( "editSchedule" ) : Messages.getString( "editBlockoutSchedule" ), null, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
    isBlockoutDialog = ( type == ScheduleDialogType.BLOCKOUT );
    setCallback( callback );
    editJob = jsJob;
    this.parentDialog = parentDialog;
    constructDialog( jsJob.getFullResourceName(), jsJob.getOutputPath(), jsJob.getJobName(), hasParams,
        isEmailConfValid, jsJob );
  }

  public ScheduleRecurrenceDialog( PromptDialogBox parentDialog, String filePath, String outputLocation,
      String scheduleName, IDialogCallback callback, boolean hasParams, boolean isEmailConfValid ) {
    super( ScheduleDialogType.SCHEDULER, Messages.getString( "newSchedule" ), null, false, true ); //$NON-NLS-1$
    isBlockoutDialog = false;
    setCallback( callback );
    this.parentDialog = parentDialog;
    constructDialog( filePath, outputLocation, scheduleName, hasParams, isEmailConfValid, null );
  }

  public ScheduleRecurrenceDialog( PromptDialogBox parentDialog, ScheduleDialogType type, String title,
      String filePath, String outputLocation, String scheduleName, IDialogCallback callback, boolean hasParams,
      boolean isEmailConfValid ) {
    super( type, title, null, false, true );
    isBlockoutDialog = ( type == ScheduleDialogType.BLOCKOUT );
    setCallback( callback );
    this.parentDialog = parentDialog;
    constructDialog( filePath, outputLocation, scheduleName, hasParams, isEmailConfValid, null );
  }

  @Override
  public boolean onKeyDownPreview( char key, int modifiers ) {
    if ( key == KeyCodes.KEY_ESCAPE ) {
      hide();
    }
    return true;
  }

  public void setParentDialog( PromptDialogBox parentDialog ) {
    this.parentDialog = parentDialog;
  }

  public void addCustomPanel( Widget w, DockPanel.DockLayoutConstant position ) {
    scheduleEditorWizardPanel.add( w, position );
  }

  private void constructDialog( String filePath, String outputLocation, String scheduleName, boolean hasParams,
      boolean isEmailConfValid, JsJob jsJob ) {
    this.hasParams = hasParams;
    this.filePath = filePath;
    this.isEmailConfValid = isEmailConfValid;
    this.outputLocation = outputLocation;
    this.scheduleName = scheduleName;
    scheduleEditorWizardPanel = new ScheduleEditorWizardPanel( getDialogType() );
    scheduleEditor = scheduleEditorWizardPanel.getScheduleEditor();
    String url = GWT.getHostPageBaseURL() + "api/scheduler/blockout/hasblockouts?ts=" + System.currentTimeMillis(); //$NON-NLS-1$
    RequestBuilder hasBlockoutsRequest = new RequestBuilder( RequestBuilder.GET, url );
    hasBlockoutsRequest.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    hasBlockoutsRequest.setHeader( "accept", "text/plain" );
    try {
      hasBlockoutsRequest.sendRequest( url, new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox( Messages.getString( "error" ), exception.toString(), false, false, true ); //$NON-NLS-1$
          dialogBox.center();
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          Boolean hasBlockouts = Boolean.valueOf( response.getText() );
          if ( hasBlockouts ) {
            scheduleEditor.setBlockoutButtonHandler( new ClickHandler() {
              @Override
              public void onClick( final ClickEvent clickEvent ) {
                PromptDialogBox box =
                    new PromptDialogBox( Messages.getString( "blockoutTimes" ), Messages.getString( "close" ), null,
                        null, false, true, new BlockoutPanel( false ) );
                box.center();
              }
            } );
          }
          scheduleEditor.getBlockoutCheckButton().setVisible( hasBlockouts );
        }
      } );
    } catch ( RequestException e ) {
      MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), e.toString(), //$NON-NLS-1$
          false, false, true );
      dialogBox.center();
    }
    IWizardPanel[] wizardPanels = { scheduleEditorWizardPanel };
    setWizardPanels( wizardPanels );
    setPixelSize( 475, 465 );
    center();
    if ( ( hasParams || isEmailConfValid ) && ( isBlockoutDialog == false ) ) {
      finishButton.setText( Messages.getString( "nextStep" ) ); //$NON-NLS-1$
    } else {
      finishButton.setText( Messages.getString( "ok" ) ); //$NON-NLS-1$
    }
    setupExisting( jsJob );

    wizardDeckPanel.getElement().getParentElement().addClassName( "schedule-dialog-content" );
    wizardDeckPanel.getElement().getParentElement().removeClassName( "dialog-content" );

    //setHeight("100%"); //$NON-NLS-1$
    setSize( "650px", "450px" );
    addStyleName( "schedule-recurrence-dialog" );
  }

  private void setupExisting( JsJob jsJob ) {
    if ( jsJob != null && !jsJob.equals( "" ) ) { //$NON-NLS-1$
      JsJobTrigger jsJobTrigger = jsJob.getJobTrigger();
      ScheduleType scheduleType = ScheduleType.valueOf( jsJobTrigger.getScheduleType() );
      // scheduleEditor.setScheduleName(jsJob.getJobName());
      scheduleEditor.setScheduleType( scheduleType );
      if ( scheduleType == ScheduleType.CRON || jsJobTrigger.getType().equals( "cronJobTrigger" ) ) { //$NON-NLS-1$
        scheduleEditor.getCronEditor().setCronString( jsJobTrigger.getCronString() );
      } else if ( jsJobTrigger.getType().equals( "simpleJobTrigger" ) ) { //$NON-NLS-1$
        if ( jsJobTrigger.getRepeatCount() == -1 ) {
          // Recurring simple Trigger
          int interval = jsJobTrigger.getRepeatInterval();

          scheduleEditor.setRepeatInSecs( interval );
          if ( scheduleType == ScheduleType.DAILY ) {
            DailyRecurrenceEditor dailyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getDailyEditor();
            dailyRecurrenceEditor.setEveryNDays();
          }
        }
      } else if ( jsJobTrigger.getType().equals( "complexJobTrigger" ) ) { //$NON-NLS-1$
        if ( scheduleType == ScheduleType.DAILY ) {
          // Daily
          DailyRecurrenceEditor dailyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getDailyEditor();
          if ( jsJobTrigger.isWorkDaysInWeek() ) {
            dailyRecurrenceEditor.setEveryWeekday();
          } else {
            dailyRecurrenceEditor.setEveryNDays();
          }
        } else if ( scheduleType == ScheduleType.WEEKLY ) {
          int[] daysOfWeek = jsJobTrigger.getDayOfWeekRecurrences();
          WeeklyRecurrenceEditor weeklyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getWeeklyEditor();
          String strDays = ""; //$NON-NLS-1$
          for ( int element2 : daysOfWeek ) {
            strDays += Integer.toString( element2 ) + ","; //$NON-NLS-1$
          }
          weeklyRecurrenceEditor.setCheckedDaysAsString( strDays, 1 );
        } else if ( scheduleType == ScheduleType.MONTHLY ) {
          MonthlyRecurrenceEditor monthlyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getMonthlyEditor();
          if ( jsJobTrigger.isQualifiedDayOfWeekRecurrence() ) {
            // Run Every on ___day of Nth week every month
            monthlyRecurrenceEditor.setDayOfWeek( TimeUtil.DayOfWeek.valueOf( jsJobTrigger.getQualifiedDayOfWeek() ) );
            monthlyRecurrenceEditor
                .setWeekOfMonth( TimeUtil.WeekOfMonth.valueOf( jsJobTrigger.getDayOfWeekQualifier() ) );
            monthlyRecurrenceEditor.setNthDayNameOfMonth();
          } else {
            // Run on Nth day of the month
            monthlyRecurrenceEditor.setDayOfMonth( Integer.toString( jsJobTrigger.getDayOfMonthRecurrences()[0] ) );
          }
        } else if ( scheduleType == ScheduleType.YEARLY ) {
          YearlyRecurrenceEditor yearlyRecurrenceEditor = scheduleEditor.getRecurrenceEditor().getYearlyEditor();
          if ( jsJobTrigger.isQualifiedDayOfWeekRecurrence() ) {
            // Run Every on ___day of Nth week of the month M yearly
            yearlyRecurrenceEditor.setDayOfWeek( TimeUtil.DayOfWeek.valueOf( jsJobTrigger.getQualifiedDayOfWeek() ) );
            yearlyRecurrenceEditor
                .setWeekOfMonth( TimeUtil.WeekOfMonth.valueOf( jsJobTrigger.getDayOfWeekQualifier() ) );
            yearlyRecurrenceEditor.setMonthOfYear1( TimeUtil.MonthOfYear
                .get( jsJobTrigger.getMonthlyRecurrences()[0] - 1 ) );
            yearlyRecurrenceEditor.setNthDayNameOfMonthName();
          } else {
            // Run on Nth day of the month M yearly
            yearlyRecurrenceEditor.setDayOfMonth( Integer.toString( jsJobTrigger.getDayOfMonthRecurrences()[0] ) );
            yearlyRecurrenceEditor.setMonthOfYear0( TimeUtil.MonthOfYear
                .get( jsJobTrigger.getMonthlyRecurrences()[0] - 1 ) );
            yearlyRecurrenceEditor.setEveryMonthOnNthDay();
          }
        }
      }
      scheduleEditor.setStartDate( jsJobTrigger.getStartTime() );
      scheduleEditor.setStartTime( DateTimeFormat.getFormat( PredefinedFormat.HOUR_MINUTE_SECOND ).format(
          jsJobTrigger.getStartTime() ) );
      if ( jsJobTrigger.getEndTime() == null ) {
        scheduleEditor.setNoEndDate();
      } else {
        scheduleEditor.setEndDate( jsJobTrigger.getEndTime() );
        scheduleEditor.setEndBy();
      }

      if ( isBlockoutDialog ) {
        scheduleEditor.setDurationFields( jsJobTrigger.getBlockDuration() );
      }
    }
  }

  protected JSONObject getJsonSimpleTrigger( int repeatCount, int interval, Date startDate, Date endDate ) {
    JSONObject trigger = new JSONObject();
    trigger.put( "uiPassParam", new JSONString( scheduleEditorWizardPanel.getScheduleType().name() ) ); //$NON-NLS-1$
    trigger.put( "repeatInterval", new JSONNumber( interval ) ); //$NON-NLS-1$
    trigger.put( "repeatCount", new JSONNumber( repeatCount ) ); //$NON-NLS-1$
    addJsonStartEnd( trigger, startDate, endDate );
    return trigger;
  }

  protected JSONObject getJsonCronTrigger( String cronString, Date startDate, Date endDate ) {
    JSONObject trigger = new JSONObject();
    trigger.put( "uiPassParam", new JSONString( scheduleEditorWizardPanel.getScheduleType().name() ) ); //$NON-NLS-1$
    trigger.put( "cronString", new JSONString( cronString ) ); //$NON-NLS-1$
    addJsonStartEnd( trigger, startDate, endDate );
    return trigger;
  }

  protected JSONObject getJsonComplexTrigger( ScheduleType scheduleType, MonthOfYear month, WeekOfMonth weekOfMonth,
      List<DayOfWeek> daysOfWeek, Date startDate, Date endDate ) {
    JSONObject trigger = new JSONObject();
    trigger.put( "uiPassParam", new JSONString( scheduleEditorWizardPanel.getScheduleType().name() ) ); //$NON-NLS-1$
    if ( month != null ) {
      JSONArray jsonArray = new JSONArray();
      jsonArray.set( 0, new JSONString( Integer.toString( month.ordinal() ) ) );
      trigger.put( "monthsOfYear", jsonArray ); //$NON-NLS-1$
    }
    if ( weekOfMonth != null ) {
      JSONArray jsonArray = new JSONArray();
      jsonArray.set( 0, new JSONString( Integer.toString( weekOfMonth.ordinal() ) ) );
      trigger.put( "weeksOfMonth", jsonArray ); //$NON-NLS-1$
    }
    if ( daysOfWeek != null ) {
      JSONArray jsonArray = new JSONArray();
      int index = 0;
      for ( DayOfWeek dayOfWeek : daysOfWeek ) {
        jsonArray.set( index++, new JSONString( Integer.toString( dayOfWeek.ordinal() ) ) );
      }
      trigger.put( "daysOfWeek", jsonArray ); //$NON-NLS-1$
    }
    addJsonStartEnd( trigger, startDate, endDate );
    return trigger;
  }

  protected JSONObject getJsonComplexTrigger( ScheduleType scheduleType, MonthOfYear month, int dayOfMonth,
      Date startDate, Date endDate ) {
    JSONObject trigger = new JSONObject();
    trigger.put( "uiPassParam", new JSONString( scheduleEditorWizardPanel.getScheduleType().name() ) ); //$NON-NLS-1$

    if ( month != null ) {
      JSONArray jsonArray = new JSONArray();
      jsonArray.set( 0, new JSONString( Integer.toString( month.ordinal() ) ) );
      trigger.put( "monthsOfYear", jsonArray ); //$NON-NLS-1$
    }

    JSONArray jsonArray = new JSONArray();
    jsonArray.set( 0, new JSONString( Integer.toString( dayOfMonth ) ) );
    trigger.put( "daysOfMonth", jsonArray ); //$NON-NLS-1$

    addJsonStartEnd( trigger, startDate, endDate );
    return trigger;
  }

  /**
   * Returns an object suitable for posting into quartz via the the "JOB" rest service.
   * 
   * @return
   */
  @SuppressWarnings( "deprecation" )
  public JSONObject getSchedule() {
    ScheduleType scheduleType = scheduleEditorWizardPanel.getScheduleType();
    Date startDate = scheduleEditorWizardPanel.getStartDate();
    String startTime = scheduleEditorWizardPanel.getStartTime();

    // For blockout periods, we need the blockout start time.
    if ( isBlockoutDialog ) {
      startTime = scheduleEditorWizardPanel.getBlockoutStartTime();
    }

    int startHour = getStartHour( startTime );
    int startMin = getStartMin( startTime );
    int startYear = startDate.getYear();
    int startMonth = startDate.getMonth();
    int startDay = startDate.getDate();
    Date startDateTime = new Date( startYear, startMonth, startDay, startHour, startMin );
    Date endDate = scheduleEditorWizardPanel.getEndDate();
    MonthOfYear monthOfYear = scheduleEditor.getRecurrenceEditor().getSelectedMonth();
    List<DayOfWeek> daysOfWeek = scheduleEditor.getRecurrenceEditor().getSelectedDaysOfWeek();
    Integer dayOfMonth = scheduleEditor.getRecurrenceEditor().getSelectedDayOfMonth();
    WeekOfMonth weekOfMonth = scheduleEditor.getRecurrenceEditor().getSelectedWeekOfMonth();

    JSONObject schedule = new JSONObject();
    schedule.put( "jobName", new JSONString( scheduleName ) ); //$NON-NLS-1$

    if ( scheduleType == ScheduleType.RUN_ONCE ) { // Run once types
      schedule.put( "simpleJobTrigger", getJsonSimpleTrigger( 0, 0, startDateTime, null ) ); //$NON-NLS-1$
    } else if ( ( scheduleType == ScheduleType.SECONDS ) || ( scheduleType == ScheduleType.MINUTES )
        || ( scheduleType == ScheduleType.HOURS ) ) {
      int repeatInterval = 0;
      try { // Simple Trigger Types
        repeatInterval = Integer.parseInt( scheduleEditorWizardPanel.getRepeatInterval() );
      } catch ( Exception e ) {
        // ignored
      }
      schedule.put( "simpleJobTrigger", getJsonSimpleTrigger( -1, repeatInterval, startDateTime, endDate ) ); //$NON-NLS-1$
    } else if ( scheduleType == ScheduleType.DAILY ) {
      if ( scheduleEditor.getRecurrenceEditor().isEveryNDays() ) {
        int repeatInterval = 0;
        try { // Simple Trigger Types
          repeatInterval = Integer.parseInt( scheduleEditorWizardPanel.getRepeatInterval() );
        } catch ( Exception e ) {
          // ignored
        }
        schedule.put( "simpleJobTrigger", getJsonSimpleTrigger( -1, repeatInterval, startDateTime, endDate ) ); //$NON-NLS-1$
      } else {
        schedule
            .put(
                "complexJobTrigger", getJsonComplexTrigger( scheduleType, null, null, scheduleEditor.getRecurrenceEditor().getSelectedDaysOfWeek(), startDateTime, endDate ) ); //$NON-NLS-1$
      }
    } else if ( scheduleType == ScheduleType.CRON ) { // Cron jobs
      schedule.put( "cronJobTrigger", getJsonCronTrigger( scheduleEditor.getCronString(), startDateTime, endDate ) ); //$NON-NLS-1$
    } else if ( ( scheduleType == ScheduleType.WEEKLY ) && ( daysOfWeek.size() > 0 ) ) {
      schedule
          .put(
              "complexJobTrigger", getJsonComplexTrigger( scheduleType, null, null, scheduleEditor.getRecurrenceEditor().getSelectedDaysOfWeek(), startDateTime, endDate ) ); //$NON-NLS-1$
    } else if ( ( scheduleType == ScheduleType.MONTHLY )
        || ( ( scheduleType == ScheduleType.YEARLY ) && ( monthOfYear != null ) ) ) {
      if ( dayOfMonth != null ) {
        // YEARLY Run on specific day in year or MONTHLY Run on specific day in month
        schedule.put( "complexJobTrigger", getJsonComplexTrigger( scheduleType, monthOfYear, dayOfMonth, startDateTime,
            endDate ) );
      } else if ( ( daysOfWeek.size() > 0 ) && ( weekOfMonth != null ) ) {
        // YEARLY
        schedule.put( "complexJobTrigger", getJsonComplexTrigger( scheduleType, monthOfYear, weekOfMonth, daysOfWeek,
            startDateTime, endDate ) );
      }
    }
    schedule.put( "inputFile", new JSONString( filePath ) ); //$NON-NLS-1$ 
    schedule.put( "outputFile", new JSONString( outputLocation ) ); //$NON-NLS-1$
    return schedule;
  }

  @SuppressWarnings( "deprecation" )
  private JSONObject addJsonStartEnd( JSONObject trigger, Date startDate, Date endDate ) {
    trigger.put(
        "startTime", new JSONString( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( startDate ) ) ); //$NON-NLS-1$
    if ( endDate != null ) {
      endDate.setHours( 23 );
      endDate.setMinutes( 59 );
      endDate.setSeconds( 59 );
    }
    trigger.put( "endTime", endDate == null ? JSONNull.getInstance() : new JSONString( DateTimeFormat.getFormat(
        PredefinedFormat.ISO_8601 ).format( endDate ) ) );
    return trigger;
  }

  private long calculateBlockoutDuration() {
    long second = 1000;
    long minute = second * 60;
    long hour = minute * 60;
    long day = hour * 24;

    long durationMilli = -1;

    if ( scheduleEditor.getBlockoutEndsType().equals( ScheduleEditor.ENDS_TYPE.TIME ) ) {
      final String startTime = scheduleEditorWizardPanel.getBlockoutStartTime();
      final String endTime = scheduleEditorWizardPanel.getBlockoutEndTime();

      long start = getStartHour( startTime ) * hour + getStartMin( startTime ) * minute;
      long end = getStartHour( endTime ) * hour + getStartMin( endTime ) * minute;

      durationMilli = Math.abs( end - start );

    } else {
      DurationValues durationValues = scheduleEditor.getDurationValues();

      long minutes = durationValues.minutes * minute;
      long hours = durationValues.hours * hour;
      long days = durationValues.days * day;

      durationMilli = minutes + hours + days;
    }

    return durationMilli;
  }

  @SuppressWarnings( "deprecation" )
  public JsJobTrigger getJsJobTrigger() {
    JsJobTrigger jsJobTrigger = JsJobTrigger.instance();

    ScheduleType scheduleType = scheduleEditorWizardPanel.getScheduleType();
    Date startDate = scheduleEditorWizardPanel.getStartDate();
    String startTime = scheduleEditorWizardPanel.getStartTime();

    int startHour = getStartHour( startTime );
    int startMin = getStartMin( startTime );
    int startYear = startDate.getYear();
    int startMonth = startDate.getMonth();
    int startDay = startDate.getDate();
    Date startDateTime = new Date( startYear, startMonth, startDay, startHour, startMin );

    Date endDate = scheduleEditorWizardPanel.getEndDate();
    MonthOfYear monthOfYear = scheduleEditor.getRecurrenceEditor().getSelectedMonth();
    List<DayOfWeek> daysOfWeek = scheduleEditor.getRecurrenceEditor().getSelectedDaysOfWeek();
    Integer dayOfMonth = scheduleEditor.getRecurrenceEditor().getSelectedDayOfMonth();
    WeekOfMonth weekOfMonth = scheduleEditor.getRecurrenceEditor().getSelectedWeekOfMonth();

    if ( isBlockoutDialog ) {
      jsJobTrigger.setBlockDuration( calculateBlockoutDuration() );
    } else {
      // blockDuration is only valid for blockouts
      jsJobTrigger.setBlockDuration( new Long( -1 ) );
    }

    if ( scheduleType == ScheduleType.RUN_ONCE ) { // Run once types
      jsJobTrigger.setType( "simpleJobTrigger" ); //$NON-NLS-1$
      jsJobTrigger.setRepeatInterval( 0 );
      jsJobTrigger.setRepeatCount( 0 );
      jsJobTrigger.setNativeStartTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( startDateTime ) );
    } else if ( ( scheduleType == ScheduleType.SECONDS ) || ( scheduleType == ScheduleType.MINUTES )
        || ( scheduleType == ScheduleType.HOURS ) ) {
      int repeatInterval = 0;
      try { // Simple Trigger Types
        repeatInterval = Integer.parseInt( scheduleEditorWizardPanel.getRepeatInterval() );
      } catch ( Exception e ) {
        // ignored
      }
      jsJobTrigger.setType( "simpleJobTrigger" ); //$NON-NLS-1$
      jsJobTrigger.setRepeatInterval( repeatInterval );
      jsJobTrigger.setRepeatCount( -1 );
      jsJobTrigger.setNativeStartTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( startDateTime ) );
      if ( endDate != null ) {
        jsJobTrigger.setNativeEndTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( endDate ) );
      }
    } else if ( scheduleType == ScheduleType.DAILY ) {
      if ( scheduleEditor.getRecurrenceEditor().isEveryNDays() ) {
        int repeatInterval = 0;
        try { // Simple Trigger Types
          repeatInterval = Integer.parseInt( scheduleEditorWizardPanel.getRepeatInterval() );
        } catch ( Exception e ) {
          // ignored
        }
        jsJobTrigger.setType( "simpleJobTrigger" ); //$NON-NLS-1$
        jsJobTrigger.setRepeatInterval( repeatInterval );
        jsJobTrigger.setRepeatCount( -1 );
        jsJobTrigger.setNativeStartTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( startDateTime ) );
        if ( endDate != null ) {
          jsJobTrigger.setNativeEndTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( endDate ) );
        }
      } else {
        JsArrayInteger jsDaysOfWeek = (JsArrayInteger) JavaScriptObject.createArray();
        int i = 0;
        for ( DayOfWeek dayOfWeek : daysOfWeek ) {
          jsDaysOfWeek.set( i++, dayOfWeek.ordinal() + 1 );
        }
        JsArrayInteger hours = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set( 0, startHour );
        JsArrayInteger minutes = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set( 0, startMin );
        JsArrayInteger seconds = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set( 0, 0 );

        jsJobTrigger.setType( "complexJobTrigger" ); //$NON-NLS-1$
        jsJobTrigger.setDayOfWeekRecurrences( jsDaysOfWeek );
        jsJobTrigger.setHourRecurrences( hours );
        jsJobTrigger.setMinuteRecurrences( minutes );
        jsJobTrigger.setSecondRecurrences( seconds );
        jsJobTrigger.setNativeStartTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( startDateTime ) );
        if ( endDate != null ) {
          jsJobTrigger.setNativeEndTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( endDate ) );
        }
      }
    } else if ( scheduleType == ScheduleType.CRON ) { // Cron jobs
      jsJobTrigger.setType( "cronJobTrigger" ); //$NON-NLS-1$
    } else if ( ( scheduleType == ScheduleType.WEEKLY ) && ( daysOfWeek.size() > 0 ) ) {
      JsArrayInteger jsDaysOfWeek = (JsArrayInteger) JavaScriptObject.createArray();
      int i = 0;
      for ( DayOfWeek dayOfWeek : daysOfWeek ) {
        jsDaysOfWeek.set( i++, dayOfWeek.ordinal() + 1 );
      }
      JsArrayInteger hours = (JsArrayInteger) JavaScriptObject.createArray();
      hours.set( 0, startHour );
      JsArrayInteger minutes = (JsArrayInteger) JavaScriptObject.createArray();
      hours.set( 0, startMin );
      JsArrayInteger seconds = (JsArrayInteger) JavaScriptObject.createArray();
      hours.set( 0, 0 );

      jsJobTrigger.setType( "complexJobTrigger" ); //$NON-NLS-1$
      jsJobTrigger.setDayOfWeekRecurrences( jsDaysOfWeek );
      jsJobTrigger.setHourRecurrences( hours );
      jsJobTrigger.setMinuteRecurrences( minutes );
      jsJobTrigger.setSecondRecurrences( seconds );
      jsJobTrigger.setNativeStartTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( startDateTime ) );
      if ( endDate != null ) {
        jsJobTrigger.setNativeEndTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( endDate ) );
      }
    } else if ( ( scheduleType == ScheduleType.MONTHLY )
        || ( ( scheduleType == ScheduleType.YEARLY ) && ( monthOfYear != null ) ) ) {
      jsJobTrigger.setType( "complexJobTrigger" ); //$NON-NLS-1$

      if ( dayOfMonth != null ) {
        JsArrayInteger jsDaysOfMonth = (JsArrayInteger) JavaScriptObject.createArray();
        jsDaysOfMonth.set( 0, dayOfMonth );

        JsArrayInteger hours = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set( 0, startHour );
        JsArrayInteger minutes = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set( 0, startMin );
        JsArrayInteger seconds = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set( 0, 0 );

        jsJobTrigger.setType( "complexJobTrigger" ); //$NON-NLS-1$
        if ( monthOfYear != null ) {
          JsArrayInteger jsMonthsOfYear = (JsArrayInteger) JavaScriptObject.createArray();
          jsMonthsOfYear.set( 0, monthOfYear.ordinal() + 1 );
          jsJobTrigger.setMonthlyRecurrences( jsMonthsOfYear );
        }
        jsJobTrigger.setDayOfMonthRecurrences( jsDaysOfMonth );
        jsJobTrigger.setHourRecurrences( hours );
        jsJobTrigger.setMinuteRecurrences( minutes );
        jsJobTrigger.setSecondRecurrences( seconds );
        jsJobTrigger.setNativeStartTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( startDateTime ) );
        if ( endDate != null ) {
          jsJobTrigger.setNativeEndTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( endDate ) );
        }
      } else if ( ( daysOfWeek.size() > 0 ) && ( weekOfMonth != null ) ) {
        JsArrayInteger jsDaysOfWeek = (JsArrayInteger) JavaScriptObject.createArray();
        int i = 0;
        for ( DayOfWeek dayOfWeek : daysOfWeek ) {
          jsDaysOfWeek.set( i++, dayOfWeek.ordinal() + 1 );
        }

        JsArrayInteger hours = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set( 0, startHour );
        JsArrayInteger minutes = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set( 0, startMin );
        JsArrayInteger seconds = (JsArrayInteger) JavaScriptObject.createArray();
        hours.set( 0, 0 );

        jsJobTrigger.setType( "complexJobTrigger" ); //$NON-NLS-1$
        if ( monthOfYear != null ) {
          JsArrayInteger jsMonthsOfYear = (JsArrayInteger) JavaScriptObject.createArray();
          jsMonthsOfYear.set( 0, monthOfYear.ordinal() + 1 );
          jsJobTrigger.setMonthlyRecurrences( jsMonthsOfYear );
        }
        jsJobTrigger.setHourRecurrences( hours );
        jsJobTrigger.setMinuteRecurrences( minutes );
        jsJobTrigger.setSecondRecurrences( seconds );
        jsJobTrigger.setQualifiedDayOfWeek( daysOfWeek.get( 0 ).name() );
        jsJobTrigger.setDayOfWeekQualifier( weekOfMonth.name() );
        jsJobTrigger.setNativeStartTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( startDateTime ) );
        if ( endDate != null ) {
          jsJobTrigger.setNativeEndTime( DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( endDate ) );
        }
      }
    }
    return jsJobTrigger;
  }

  protected boolean addBlockoutPeriod( final JSONObject schedule, final JsJobTrigger trigger, String urlSuffix ) {
    String url = GWT.getHostPageBaseURL() + "api/scheduler/blockout/" + urlSuffix; //$NON-NLS-1$

    RequestBuilder addBlockoutPeriodRequest = new RequestBuilder( RequestBuilder.POST, url );
    addBlockoutPeriodRequest.setHeader( "accept", "text/plain" ); //$NON-NLS-1$ //$NON-NLS-2$
    addBlockoutPeriodRequest.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$ //$NON-NLS-2$
    addBlockoutPeriodRequest.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );

    // Create a unique blockout period name
    final Long duration = trigger.getBlockDuration();
    final String blockoutPeriodName = trigger.getScheduleType() + Random.nextInt() + ":" + //$NON-NLS-1$
        /* PentahoSessionHolder.getSession().getName() */"admin" + ":" + duration; //$NON-NLS-1$ //$NON-NLS-2$

    // Add blockout specific parameters
    JSONObject addBlockoutParams = schedule;
    addBlockoutParams.put( "jobName", new JSONString( blockoutPeriodName ) ); //$NON-NLS-1$
    addBlockoutParams.put( "duration", new JSONNumber( duration ) ); //$NON-NLS-1$
    addBlockoutParams.put( "timeZone", new JSONString( scheduleEditorWizardPanel.getTimeZone() ) );

    try {
      addBlockoutPeriodRequest.sendRequest( addBlockoutParams.toString(), new RequestCallback() {
        @Override
        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox( Messages.getString( "error" ), exception.toString(), false, false, true ); //$NON-NLS-1$
          dialogBox.center();
          setDone( false );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            if ( null != callback ) {
              callback.okPressed();
            }
          }
        }
      } );
    } catch ( RequestException e ) {
      // ignored
    }

    return true;
  }

  private void promptDueToBlockoutConflicts( final boolean alwaysConflict, final boolean conflictsSometimes,
      final JSONObject schedule, final JsJobTrigger trigger ) {
    StringBuffer conflictMessage = new StringBuffer();

    final String updateScheduleButtonText = Messages.getString( "blockoutUpdateSchedule" ); //$NON-NLS-1$
    final String continueButtonText = Messages.getString( "blockoutContinueSchedule" ); //$NON-NLS-1$

    boolean showContinueButton = conflictsSometimes;
    boolean isScheduleConflict = alwaysConflict || conflictsSometimes;

    if ( conflictsSometimes ) {
      conflictMessage.append( Messages.getString( "blockoutPartialConflict" ) ); //$NON-NLS-1$
      conflictMessage.append( "\n" ); //$NON-NLS-1$
      conflictMessage.append( Messages.getString( "blockoutPartialConflictContinue" ) ); //$NON-NLS-1$
    } else {
      conflictMessage.append( Messages.getString( "blockoutTotalConflict" ) ); //$NON-NLS-1$
    }

    if ( isScheduleConflict ) {
      final MessageDialogBox dialogBox =
          new MessageDialogBox( Messages.getString( "blockoutTimeExists" ), //$NON-NLS-1$
              conflictMessage.toString(), false, false, true, updateScheduleButtonText, showContinueButton
                  ? continueButtonText : null, null );
      dialogBox.setCallback( new IDialogCallback() {
        // If user clicked on 'Continue' we want to add the schedule. Otherwise we dismiss the dialog
        // and they have to modify the recurrence schedule
        @Override
        public void cancelPressed() {
          // User clicked on continue, so we need to proceed adding the schedule
          handleWizardPanels( schedule, trigger );
        }

        @Override
        public void okPressed() {
          // Update Schedule Button pressed
          dialogBox.setVisible( false );
        }
      } );

      dialogBox.center();
    }
  }

  /**
   * Before creating a new schedule, we want to check to see if the schedule that is being created is going to conflict
   * with any one of the blockout periods if one is provisioned.
   * 
   * @param schedule
   * @param trigger
   */
  protected void verifyBlockoutConflict( final JSONObject schedule, final JsJobTrigger trigger ) {
    String url = GWT.getHostPageBaseURL() + "api/scheduler/blockout/blockstatus"; //$NON-NLS-1$

    RequestBuilder blockoutConflictRequest = new RequestBuilder( RequestBuilder.POST, url );
    blockoutConflictRequest.setHeader( "accept", "application/json" ); //$NON-NLS-1$ //$NON-NLS-2$
    blockoutConflictRequest.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$ //$NON-NLS-2$
    blockoutConflictRequest.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );

    final JSONObject verifyBlockoutParams = schedule;
    verifyBlockoutParams.put( "jobName", new JSONString( scheduleName ) ); //$NON-NLS-1$

    try {
      blockoutConflictRequest.sendRequest( verifyBlockoutParams.toString(), new RequestCallback() {
        @Override
        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox( Messages.getString( "error" ), exception.toString(), false, false, true ); //$NON-NLS-1$
          dialogBox.center();
          setDone( false );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            JsBlockStatus statusResponse =
                (JsBlockStatus) parseJson( JsonUtils.escapeJsonForEval( response.getText() ) );

            // Determine if this schedule conflicts all the time or some of the time
            boolean partiallyBlocked = Boolean.parseBoolean( statusResponse.getPartiallyBlocked() );
            boolean totallyBlocked = Boolean.parseBoolean( statusResponse.getTotallyBlocked() );
            if ( partiallyBlocked || totallyBlocked ) {
              promptDueToBlockoutConflicts( totallyBlocked, partiallyBlocked, schedule, trigger );
            } else {
              // Continue with other panels in the wizard (params, email)
              handleWizardPanels( schedule, trigger );
            }
          } else {
            handleWizardPanels( schedule, trigger );
          }
        }
      } );
    } catch ( RequestException e ) {
      // ignored
    }

    super.nextClicked();
  }

  private void handleWizardPanels( final JSONObject schedule, final JsJobTrigger trigger ) {
    if ( hasParams ) {
      showScheduleParamsDialog( trigger, schedule );
    } else if ( isEmailConfValid ) {
      showScheduleEmailDialog( schedule );
    } else {
      // submit
      JSONObject scheduleRequest = (JSONObject) JSONParser.parseStrict( schedule.toString() );

      if ( editJob != null ) {
        JSONArray scheduleParams = new JSONArray();

        for ( int i = 0; i < editJob.getJobParams().length(); i++ ) {
          JsJobParam param = editJob.getJobParams().get( i );
          JsArrayString paramValue = (JsArrayString) JavaScriptObject.createArray().cast();
          paramValue.push( param.getValue() );
          JsSchedulingParameter p = (JsSchedulingParameter) JavaScriptObject.createObject().cast();
          p.setName( param.getName() );
          p.setType( "string" ); //$NON-NLS-1$
          p.setStringValue( paramValue );
          scheduleParams.set( i, new JSONObject( p ) );
        }

        scheduleRequest.put( "jobParameters", scheduleParams ); //$NON-NLS-1$

        String actionClass = editJob.getJobParamValue( "ActionAdapterQuartzJob-ActionClass" ); //$NON-NLS-1$
        if ( !StringUtils.isEmpty( actionClass ) ) {
          scheduleRequest.put( "actionClass", new JSONString( actionClass ) ); //$NON-NLS-1$
        }

      }

      RequestBuilder scheduleFileRequestBuilder =
          new RequestBuilder( RequestBuilder.POST, contextURL + "api/scheduler/job" ); //$NON-NLS-1$
      scheduleFileRequestBuilder.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$
      scheduleFileRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );

      try {
        scheduleFileRequestBuilder.sendRequest( scheduleRequest.toString(), new RequestCallback() {
          @Override
          public void onError( Request request, Throwable exception ) {
            MessageDialogBox dialogBox =
                new MessageDialogBox( Messages.getString( "error" ), exception.toString(), false, false, true ); //$NON-NLS-1$
            dialogBox.center();
            setDone( false );
          }

          @Override
          public void onResponseReceived( Request request, Response response ) {
            if ( response.getStatusCode() == 200 ) {
              setDone( true );
              ScheduleRecurrenceDialog.this.hide();
              if ( callback != null ) {
                callback.okPressed();
              }
              if ( showSuccessDialog ) {
                if ( !PerspectiveManager.getInstance().getActivePerspective().getId().equals(
                    PerspectiveManager.SCHEDULES_PERSPECTIVE ) ) {
                  ScheduleCreateStatusDialog successDialog = new ScheduleCreateStatusDialog();
                  successDialog.center();
                } else {
                  MessageDialogBox dialogBox =
                      new MessageDialogBox(
                          Messages.getString( "scheduleUpdatedTitle" ), Messages.getString( "scheduleUpdatedMessage" ), //$NON-NLS-1$ //$NON-NLS-2$ 
                          false, false, true );
                  dialogBox.center();
                }
              }
            } else {
              MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), //$NON-NLS-1$
                  response.getText(), false, false, true );
              dialogBox.center();
              setDone( false );
            }
          }
        } );
      } catch ( RequestException e ) {
        MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), e.toString(), //$NON-NLS-1$
            false, false, true );
        dialogBox.center();
        setDone( false );
      }

      setDone( true );
    }
  }

  private final native JavaScriptObject parseJson( String json )
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
    if ( !super.enableFinish( getIndex() ) ) {
      return false;
    }
    // DO NOT DELETE - verifyBlockoutConflict(schedule, trigger);
    JsJobTrigger trigger = getJsJobTrigger();
    JSONObject schedule = getSchedule();

    handleWizardPanels( schedule, trigger );
    return true;
  }

  private void showScheduleEmailDialog( final JSONObject schedule ) {
    try {
      final String url = GWT.getHostPageBaseURL() + "api/mantle/isAuthenticated"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      requestBuilder.setHeader( "accept", "text/plain" ); //$NON-NLS-1$ //$NON-NLS-2$
      requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      requestBuilder.sendRequest( null, new RequestCallback() {

        @Override
        public void onError( Request request, Throwable caught ) {
          MantleLoginDialog.performLogin( new AsyncCallback<Boolean>() {

            @Override
            public void onFailure( Throwable caught ) {
            }

            @Override
            public void onSuccess( Boolean result ) {
              showScheduleEmailDialog( schedule );
            }
          } );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          JSONObject scheduleRequest = (JSONObject) JSONParser.parseStrict( schedule.toString() );
          if ( scheduleEmailDialog == null ) {
            scheduleEmailDialog =
                new ScheduleEmailDialog( ScheduleRecurrenceDialog.this, filePath, scheduleRequest, null, editJob );
            scheduleEmailDialog.setCallback( callback );
          } else {
            scheduleEmailDialog.setJobSchedule( scheduleRequest );
          }
          scheduleEmailDialog.center();
          hide();
        }

      } );
    } catch ( RequestException e ) {
      Window.alert( e.getMessage() );
    }

  }

  private void showScheduleParamsDialog( final JsJobTrigger trigger, final JSONObject schedule ) {
    try {
      final String url = GWT.getHostPageBaseURL() + "api/mantle/isAuthenticated"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      requestBuilder.setHeader( "accept", "text/plain" ); //$NON-NLS-1$ //$NON-NLS-2$
      requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      requestBuilder.sendRequest( null, new RequestCallback() {

        @Override
        public void onError( Request request, Throwable caught ) {
          MantleLoginDialog.performLogin( new AsyncCallback<Boolean>() {

            @Override
            public void onFailure( Throwable caught ) {
            }

            @Override
            public void onSuccess( Boolean result ) {
              showScheduleParamsDialog( trigger, schedule );
            }
          } );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          if ( scheduleParamsDialog == null ) {
            scheduleParamsDialog = new ScheduleParamsDialog( ScheduleRecurrenceDialog.this, isEmailConfValid, editJob );
            scheduleParamsDialog.setCallback( callback );
          } else {
            scheduleParamsDialog.setJobSchedule( schedule );
          }
          scheduleParamsDialog.center();
          hide();
        }

      } );
    } catch ( RequestException e ) {
      Window.alert( e.getMessage() );
    }

  }

  /**
   * @param startTime
   * @return
   */
  private int getStartMin( String startTime ) {
    if ( startTime == null || startTime.length() < 1 ) {
      return 0;
    }
    int firstSeparator = startTime.indexOf( ':' );
    int secondSeperator = startTime.indexOf( ':', firstSeparator + 1 );
    int min = Integer.parseInt( startTime.substring( firstSeparator + 1, secondSeperator ) );
    return min;
  }

  /**
   * @param startTime
   * @return
   */
  private int getStartHour( String startTime ) {
    if ( startTime == null || startTime.length() < 1 ) {
      return 0;
    }
    int afternoonOffset = startTime.endsWith( "PM" ) ? 12 : 0; //$NON-NLS-1$
    int hour = Integer.parseInt( startTime.substring( 0, startTime.indexOf( ':' ) ) );
    hour += afternoonOffset;
    return hour;
  }

  public Boolean getDone() {
    return done;
  }

  public void setDone( Boolean done ) {
    this.done = done;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onNext(org.pentaho.gwt.widgets.client.wizards.
   * IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onNext( IWizardPanel nextPanel, IWizardPanel previousPanel ) {
    return super.enableNext( getIndex() );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onPrevious(org.pentaho.gwt.widgets.client.wizards
   * .IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onPrevious( IWizardPanel previousPanel, IWizardPanel currentPanel ) {
    return true;
  }

  @Override
  protected void backClicked() {
    hide();
    if ( parentDialog != null ) {
      parentDialog.center();
    }
  }

  @Override
  public void center() {
    super.center();
    Timer t = new Timer() {
      @Override
      public void run() {
        if ( scheduleEditorWizardPanel.isAttached() && scheduleEditorWizardPanel.isVisible() ) {
          cancel();
        }
      }
    };
    t.scheduleRepeating( 250 );
  }

  @Override
  protected boolean enableBack( int index ) {
    return parentDialog != null;
  }

  @Override
  protected boolean showBack( int index ) {
    return parentDialog != null;
  }

  @Override
  protected boolean showFinish( int index ) {
    return true;
  }

  @Override
  protected boolean showNext( int index ) {
    return false;
  }

  public void setCallback( IDialogCallback callback ) {
    this.callback = callback;
  }

  public IDialogCallback getCallback() {
    return callback;
  }

  public boolean isShowSuccessDialog() {
    return showSuccessDialog;
  }

  public void setShowSuccessDialog( boolean showSuccessDialog ) {
    this.showSuccessDialog = showSuccessDialog;
  }
}
