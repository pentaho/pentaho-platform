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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.mantle.client.dialogs.scheduling;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog.ScheduleDialogType;
import org.pentaho.mantle.client.dialogs.SelectFolderDialog;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.workspace.JsJob;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class NewScheduleDialog extends PromptDialogBox {

  private String filePath;
  private IDialogCallback callback;
  private boolean hasParams;
  private boolean isEmailConfValid;
  private JsJob jsJob;

  private ScheduleRecurrenceDialog recurrenceDialog = null;
  
  private TextBox scheduleNameTextBox = new TextBox();
  private TextBox scheduleLocationTextBox = new TextBox();

  public NewScheduleDialog(JsJob jsJob, IDialogCallback callback, boolean hasParams, boolean isEmailConfValid) {
    super(Messages.getString("newSchedule"), Messages.getString("nextStep"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    this.jsJob = jsJob;
    this.filePath = jsJob.getFullResourceName();
    this.callback = callback;
    this.hasParams = hasParams;
    this.isEmailConfValid = isEmailConfValid;
    createUI();
  }

  public NewScheduleDialog(String filePath, IDialogCallback callback, boolean hasParams, boolean isEmailConfValid) {
    super(Messages.getString("newSchedule"), Messages.getString("nextStep"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    this.filePath = filePath;
    this.callback = callback;
    this.hasParams = hasParams;
    this.isEmailConfValid = isEmailConfValid;
    createUI();
  }

  private void createUI() {
    VerticalPanel content = new VerticalPanel();
    
    HorizontalPanel scheduleNameLabelPanel = new HorizontalPanel();
    Label scheduleNameLabel = new Label(Messages.getString("scheduleName"));
    scheduleNameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    
    Label scheduleNameInfoLabel = new Label(Messages.getString("scheduleNameInfo"));
    scheduleNameInfoLabel.setStyleName("msg-Label");
    
    scheduleNameLabelPanel.add(scheduleNameLabel);
    scheduleNameLabelPanel.add(scheduleNameInfoLabel);
    
    String defaultName = filePath.substring(filePath.lastIndexOf("/")+1, filePath.lastIndexOf("."));
    scheduleNameTextBox.setVisibleLength(60);
    scheduleNameTextBox.setText(defaultName);
    
    content.add(scheduleNameLabelPanel);
    content.add(scheduleNameTextBox);

    Label scheduleLocationLabel = new Label(Messages.getString("generatedContentLocation"));
    scheduleLocationLabel.setStyleName(ScheduleEditor.SCHEDULE_LABEL);
    content.add(scheduleLocationLabel);
    
    Button browseButton = new Button(Messages.getString("select"));
    browseButton.addClickHandler(new ClickHandler() {
      
      public void onClick(ClickEvent event) {
        final SelectFolderDialog selectFolder = new SelectFolderDialog();
        selectFolder.setCallback(new IDialogCallback() {
          public void okPressed() {
            scheduleLocationTextBox.setText(selectFolder.getSelectedPath());
          }
          
          public void cancelPressed() {
          }
        });
        selectFolder.center();
      }
    });
    browseButton.setStyleName("pentaho-button");
    
    scheduleLocationTextBox.setVisibleLength(60);
    HorizontalPanel locationPanel = new HorizontalPanel();
    scheduleLocationTextBox.setEnabled(false);
    locationPanel.add(scheduleLocationTextBox);
    locationPanel.add(browseButton);
    
    content.add(locationPanel);

    if (jsJob != null) {
      scheduleNameTextBox.setText(jsJob.getJobName());
      scheduleLocationTextBox.setText(jsJob.getOutputPath());
    }
    
    setContent(content);
  }
  
  protected void onOk() {
    if (jsJob != null) {
      jsJob.setJobName(scheduleNameTextBox.getText());
      jsJob.setOutputPath(scheduleLocationTextBox.getText());
      if (recurrenceDialog == null) {
        recurrenceDialog = new ScheduleRecurrenceDialog(this, jsJob, callback, hasParams, isEmailConfValid, ScheduleDialogType.SCHEDULER);
      }
    } else if (recurrenceDialog == null) {
      recurrenceDialog = new ScheduleRecurrenceDialog(this, filePath, scheduleLocationTextBox.getText(), scheduleNameTextBox.getText(), callback, hasParams,
          isEmailConfValid);
    } else {
      recurrenceDialog.scheduleName = scheduleNameTextBox.getText();
      recurrenceDialog.outputLocation = scheduleLocationTextBox.getText();
    }
    recurrenceDialog.setParentDialog(this);
    recurrenceDialog.center();
    super.onOk();
  }

  public void setFocus() {
    scheduleNameTextBox.setFocus(true);
  }

  public String getScheduleName() {
    return scheduleNameTextBox.getText();
  }

  public void setScheduleName(String scheduleName) {
    scheduleNameTextBox.setText(scheduleName);
  }

}