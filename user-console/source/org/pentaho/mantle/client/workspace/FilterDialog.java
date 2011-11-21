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
 */
package org.pentaho.mantle.client.workspace;

import java.util.Date;
import java.util.HashSet;

import org.pentaho.gwt.widgets.client.controls.DateTimePicker;
import org.pentaho.gwt.widgets.client.controls.DateTimePicker.Layout;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;

public class FilterDialog extends PromptDialogBox {

  private MultiWordSuggestOracle resourceOracle = new MultiWordSuggestOracle();
  private SuggestBox resourceSuggestBox = new SuggestBox(resourceOracle);

  private CheckBox afterCheckBox = new CheckBox(Messages.getString("after"));
  private CheckBox beforeCheckBox = new CheckBox(Messages.getString("before"));
  private DateTimePicker afterDateBox = new DateTimePicker(Layout.HORIZONTAL);
  private DateTimePicker beforeDateBox = new DateTimePicker(Layout.HORIZONTAL);

  private ListBox userListBox = new ListBox(false);
  private ListBox scheduleStateListBox = new ListBox(false);
  private ListBox scheduleTypeListBox = new ListBox(false);

  public FilterDialog() {
    super(Messages.getString("filterSchedules"), Messages.getString("ok"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public FilterDialog(JsArray<JsJob> jobs, IDialogCallback callback) {
    super(Messages.getString("filterSchedules"), Messages.getString("ok"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    initUI(jobs);
    // setSize("800px", "500px");
    setCallback(callback);
  }

  /**
   * @param jobs
   */
  public void initUI(JsArray<JsJob> jobs) {
    if (jobs != null) {
      for (int i = 0; i < jobs.length(); i++) {
        resourceOracle.add(jobs.get(i).getShortResourceName());
      }
    }

    resourceSuggestBox.setWidth("160px");

    // next execution filter
    CaptionPanel executionFilterCaptionPanel = new CaptionPanel(Messages.getString("executionTime"));
    FlexTable executionFilterPanel = new FlexTable();
    executionFilterPanel.setWidget(0, 0, beforeCheckBox);
    executionFilterPanel.setWidget(0, 1, beforeDateBox);
    executionFilterPanel.setWidget(1, 0, afterCheckBox);
    executionFilterPanel.setWidget(1, 1, afterDateBox);
    executionFilterCaptionPanel.add(executionFilterPanel);

    afterCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        afterDateBox.setEnabled(event.getValue());
      }
    });

    beforeCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        beforeDateBox.setEnabled(event.getValue());
      }
    });
    beforeDateBox.setEnabled(beforeCheckBox.getValue());
    afterDateBox.setEnabled(afterCheckBox.getValue());

    // user filter
    int selectedIndex = userListBox.getSelectedIndex();
    userListBox.clear();
    userListBox.addItem("ALL");
    HashSet<String> uniqueUsers = new HashSet<String>();
    if (jobs != null) {
      for (int i = 0; i < jobs.length(); i++) {
        uniqueUsers.add(jobs.get(i).getUserName());
      }
    }
    for (String user : uniqueUsers) {
      userListBox.addItem(user);
    }
    userListBox.setSelectedIndex(selectedIndex);

    // state filter
    scheduleStateListBox.setVisibleItemCount(1);
    selectedIndex = scheduleStateListBox.getSelectedIndex();
    scheduleStateListBox.clear();
    // NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED, UNKNOWN
    scheduleStateListBox.addItem("ALL");
    scheduleStateListBox.addItem("NORMAL");
    scheduleStateListBox.addItem("PAUSED");
    scheduleStateListBox.addItem("COMPLETE");
    scheduleStateListBox.addItem("ERROR");
    scheduleStateListBox.addItem("BLOCKED");
    scheduleStateListBox.addItem("UNKNOWN");
    scheduleStateListBox.setSelectedIndex(selectedIndex);

    // state filter
    scheduleTypeListBox.setVisibleItemCount(1);
    selectedIndex = scheduleTypeListBox.getSelectedIndex();
    scheduleTypeListBox.clear();
    // NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED, UNKNOWN
    scheduleTypeListBox.addItem("ALL");
    scheduleTypeListBox.addItem("DAILY");
    scheduleTypeListBox.addItem("WEEKLY");
    scheduleTypeListBox.addItem("MONTHLY");
    scheduleTypeListBox.addItem("YEARLY");
    scheduleTypeListBox.setSelectedIndex(selectedIndex);

    FlexTable filterPanel = new FlexTable();
    filterPanel.setWidget(0, 0, new Label(Messages.getString("scheduledResource")));
    filterPanel.setWidget(0, 1, new Label(Messages.getString("_user")));

    filterPanel.setWidget(1, 0, resourceSuggestBox);
    filterPanel.setWidget(1, 1, userListBox);

    filterPanel.setWidget(2, 0, new Label(Messages.getString("scheduleState")));
    filterPanel.setWidget(2, 1, new Label(Messages.getString("scheduleType")));

    filterPanel.setWidget(3, 0, scheduleStateListBox);
    filterPanel.setWidget(3, 1, scheduleTypeListBox);

    filterPanel.setWidget(4, 0, executionFilterCaptionPanel);
    filterPanel.getFlexCellFormatter().setColSpan(4, 0, 2);

    setContent(filterPanel);
  }

  public String getUserFilter() {
    return userListBox.getItemText(userListBox.getSelectedIndex());
  }

  public String getTypeFilter() {
    return scheduleTypeListBox.getItemText(scheduleTypeListBox.getSelectedIndex());
  }

  public String getStateFilter() {
    return scheduleStateListBox.getItemText(scheduleStateListBox.getSelectedIndex());
  }

  public Date getBeforeDate() {
    if (beforeCheckBox.getValue()) {
      return beforeDateBox.getDate();
    }
    return null;
  }

  public Date getAfterDate() {
    if (afterCheckBox.getValue()) {
      return afterDateBox.getDate();
    }
    return null;
  }

  public String getResourceName() {
    return resourceSuggestBox.getText();
  }

  public CheckBox getAfterCheckBox() {
    return afterCheckBox;
  }

}
