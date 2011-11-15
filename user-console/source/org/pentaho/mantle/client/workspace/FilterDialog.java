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
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
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
  private ListBox scheduleStateList = new ListBox(false);
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

  public void initUI(JsArray<JsJob> jobs) {
    if (jobs != null) {
      for (int i = 0; i < jobs.length(); i++) {
        resourceOracle.add(jobs.get(i).getShortResourceName());
      }
    }

    resourceSuggestBox.setWidth("200px");
    CaptionPanel resourceFilterPanel = new CaptionPanel(Messages.getString("scheduledResource"));
    resourceFilterPanel.add(resourceSuggestBox);

    // next execution filter
    CaptionPanel executionFilterCaptionPanel = new CaptionPanel(Messages.getString("executionTime"));
    FlexTable executionFilterPanel = new FlexTable();
    executionFilterPanel.setWidget(0, 0, afterCheckBox);
    executionFilterPanel.setWidget(0, 1, afterDateBox);
    executionFilterPanel.setWidget(1, 0, beforeCheckBox);
    executionFilterPanel.setWidget(1, 1, beforeDateBox);
    executionFilterCaptionPanel.add(executionFilterPanel);

    // user filter
    CaptionPanel userFilterCaptionPanel = new CaptionPanel(Messages.getString("_user"));
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
    userFilterCaptionPanel.add(userListBox);

    // state filter
    scheduleStateList.setVisibleItemCount(1);
    selectedIndex = scheduleStateList.getSelectedIndex();
    scheduleStateList.clear();
    // NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED, UNKNOWN
    scheduleStateList.addItem("ALL");
    scheduleStateList.addItem("NORMAL");
    scheduleStateList.addItem("PAUSED");
    scheduleStateList.addItem("COMPLETE");
    scheduleStateList.addItem("ERROR");
    scheduleStateList.addItem("BLOCKED");
    scheduleStateList.addItem("UNKNOWN");
    scheduleStateList.setSelectedIndex(selectedIndex);
    CaptionPanel scheduleStatePanel = new CaptionPanel(Messages.getString("scheduleState"));
    scheduleStatePanel.add(scheduleStateList);

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
    CaptionPanel scheduleTypePanel = new CaptionPanel(Messages.getString("scheduleType"));
    scheduleTypePanel.add(scheduleTypeListBox);

    FlexTable filterPanel = new FlexTable();
    filterPanel.setWidget(0, 0, resourceFilterPanel);
    filterPanel.setWidget(0, 1, executionFilterCaptionPanel);
    filterPanel.getFlexCellFormatter().setRowSpan(0, 1, 2);
    filterPanel.setWidget(1, 0, scheduleStatePanel);
    filterPanel.setWidget(2, 0, userFilterCaptionPanel);
    filterPanel.setWidget(3, 0, scheduleTypePanel);

    setContent(filterPanel);
  }

  public String getUserFilter() {
    return userListBox.getItemText(userListBox.getSelectedIndex());
  }

  public String getTypeFilter() {
    return scheduleTypeListBox.getItemText(scheduleTypeListBox.getSelectedIndex());
  }

  public String getStateFilter() {
    return scheduleStateList.getItemText(scheduleStateList.getSelectedIndex());
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
