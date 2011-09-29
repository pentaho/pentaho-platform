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

import org.pentaho.gwt.widgets.client.controls.schededitor.ScheduleEditor.ScheduleType;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.gwt.widgets.client.wizards.IWizardPanel;
import org.pentaho.gwt.widgets.client.wizards.panels.ScheduleEditorWizardPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author wseyler
 *
 */
public class NewScheduleDialog extends AbstractWizardDialog {
  FileItem fileItem = null;
  
  ScheduleEditorWizardPanel scheduleEditorWizardPanel = new ScheduleEditorWizardPanel();
  
  String solutionName;
  String path;
  String actionName;
  
  Boolean done = false;
  /**
   * @param solutionName
   * @param path
   * @param actionName
   */
  public NewScheduleDialog(String solutionName, String path, String actionName) {
    super(Messages.getString("newSchedule"), null, false, true); //$NON-NLS-1$
    this.solutionName = solutionName;
    this.path = path;
    this.actionName = actionName;
    
    IWizardPanel[] wizardPanels = {scheduleEditorWizardPanel};
    this.setWizardPanels(wizardPanels);
    setPixelSize(475, 465);
  }

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#finish()
   */
  @Override
  protected boolean onFinish() {
    AsyncCallback scheduleCallback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), caught.toString(), false, false, true); //$NON-NLS-1$
        dialogBox.center();
        setDone(false);
      }

      public void onSuccess(Object result) {
        MessageDialogBox dialogBox = new MessageDialogBox(
            Messages.getString("info"), Messages.getString("actionSequenceScheduledSuccess"),  //$NON-NLS-1$ //$NON-NLS-2$
            true, false, true);
        dialogBox.center();
        setDone(true);
        NewScheduleDialog.this.hide();
      }
      
    };
    ScheduleType scheduleType = scheduleEditorWizardPanel.getScheduleType();
    String triggerName = scheduleEditorWizardPanel.getTriggerName();
    String triggerGroup = scheduleEditorWizardPanel.getTriggerGroup();
    String description = scheduleEditorWizardPanel.getDescription();
    String cronExpression = scheduleEditorWizardPanel.getCronString();
    Date startDate = scheduleEditorWizardPanel.getStartDate();
    Date endDate = scheduleEditorWizardPanel.getEndDate();
    String startTime = scheduleEditorWizardPanel.getStartTime();
    int startHour = getStartHour(startTime);
    int startMin = getStartMin(startTime);
    int startYear = startDate.getYear();
    int startMonth = startDate.getMonth();
    int startDay = startDate.getDate();
    Date startDateTime = new Date(startYear, startMonth, startDay, startHour, startMin );
    int repeatCount = scheduleEditorWizardPanel.getRepeatCount();
    int repeatInterval = 0;
    try {
      repeatInterval = Integer.parseInt(scheduleEditorWizardPanel.getRepeatInterval()) * 1000;
    } catch (Exception e) {
      // There must have been no repeat interval
      repeatInterval = 0;
    }

    if (scheduleType == ScheduleType.RUN_ONCE) { // Run once types
      MantleServiceCache.getService().createSimpleTriggerJob(triggerName, triggerGroup, description, startDateTime, null, 0, 0, solutionName, path, actionName, scheduleCallback);
    } else if (cronExpression == null) { // Simple Trigger Types   
      MantleServiceCache.getService().createSimpleTriggerJob(triggerName, triggerGroup, description, startDateTime, endDate, repeatCount, repeatInterval, solutionName, path, actionName, scheduleCallback);
    } else {  // Cron jobs     
      MantleServiceCache.getService().createCronJob(solutionName, path, actionName, triggerName, triggerGroup, description, cronExpression, scheduleCallback);
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
