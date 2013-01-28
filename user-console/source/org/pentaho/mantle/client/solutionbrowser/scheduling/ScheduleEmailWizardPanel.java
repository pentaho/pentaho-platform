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
package org.pentaho.mantle.client.solutionbrowser.scheduling;

import org.pentaho.gwt.widgets.client.i18n.WidgetsLocalizedMessages;
import org.pentaho.gwt.widgets.client.i18n.WidgetsLocalizedMessagesSingleton;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardPanel;
import org.pentaho.gwt.widgets.client.wizards.panels.JsSchedulingParameter;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ScheduleEmailWizardPanel extends AbstractWizardPanel {

  private static final WidgetsLocalizedMessages MSGS = WidgetsLocalizedMessagesSingleton.getInstance().getMessages();

  private static final String PENTAHO_SCHEDULE = "pentaho-schedule-create"; //$NON-NLS-1$

  private RadioButton yes = new RadioButton("SCH_EMAIL_YESNO", "Yes");
  private RadioButton no = new RadioButton("SCH_EMAIL_YESNO", "No");
  private TextBox toAddressTextBox = new TextBox();
  private TextBox subjectTextBox = new TextBox();
  private TextBox attachmentNameTextBox = new TextBox();
  private TextArea messageTextArea = new TextArea();

  private String filePath;

  public ScheduleEmailWizardPanel(String filePath) {
    super();
    this.filePath = filePath;
    init();
    layout();
  }

  /**
   * 
   */
  private void init() {
    // ICallback<IChangeHandler> chHandler = new ICallback<IChangeHandler>() {
    // public void onHandle(IChangeHandler se) {
    // panelWidgetChanged(ScheduleEmailWizardPanel.this);
    // }
    // };
    // scheduleEditor.setOnChangeHandler( chHandler );
  }

  private native JsArray<JsSchedulingParameter> getParams(String to, String cc, String bcc, String subject, String message, String attachmentName)
  /*-{
    var paramEntries = new Array();
    paramEntries.push({
      name: '_SCH_EMAIL_TO',
      stringValue: to,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_CC',
      stringValue: cc,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_BCC',
      stringValue: bcc,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_SUBJECT',
      stringValue: subject,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_MESSAGE',
      stringValue: message,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_ATTACHMENT_NAME',
      stringValue: attachmentName,
      type: 'string'
    });
    return paramEntries;
  }-*/;

  public JsArray<JsSchedulingParameter> getEmailParams() {
    if (yes.getValue()) {
      return getParams(toAddressTextBox.getText(), "", "", subjectTextBox.getText(), messageTextArea.getText(), attachmentNameTextBox.getText());
    } else {
      return null;
    }
  }

  private void layout() {
    this.addStyleName(PENTAHO_SCHEDULE);

    final FlexTable emailSchedulePanel = new FlexTable();
    emailSchedulePanel.setVisible(false);
    HorizontalPanel emailYesNoPanel = new HorizontalPanel();
    emailYesNoPanel.add((new Label(Messages.getString("wouldYouLikeToEmail"))));
    no.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        emailSchedulePanel.setVisible(!no.getValue());
      }
    });
    yes.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        emailSchedulePanel.setVisible(yes.getValue());
      }
    });
    no.setValue(true);
    yes.setValue(false);
    emailYesNoPanel.add(no);
    emailYesNoPanel.add(yes);
    this.add(emailYesNoPanel, NORTH);

    toAddressTextBox.setVisibleLength(75);
    subjectTextBox.setVisibleLength(75);
    attachmentNameTextBox.setVisibleLength(75);

    boolean hasExtension = filePath.lastIndexOf(".") != -1;
    String friendlyFileName = filePath.substring(filePath.lastIndexOf("/") + 1);
    if (hasExtension) {
      // remove it
      friendlyFileName = friendlyFileName.substring(0, friendlyFileName.lastIndexOf("."));
    }
    subjectTextBox.setText(Messages.getString("scheduleDefaultSubject", friendlyFileName));
    attachmentNameTextBox.setText(friendlyFileName);

    Label toLabel = new Label(Messages.getString("toColon"));
    toLabel.setWidth("130px");
    toLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    emailSchedulePanel.setWidget(0, 0, toLabel);
    ((FlexCellFormatter) emailSchedulePanel.getCellFormatter()).setWidth(0, 0, "130px");
    emailSchedulePanel.setWidget(0, 1, toAddressTextBox);
    emailSchedulePanel.setWidget(1, 1, new Label(Messages.getString("scheduleAddressSeparatorMessage")));

    Label subjectLabel = new Label(Messages.getString("subjectColon"));
    subjectLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    emailSchedulePanel.setWidget(2, 0, subjectLabel);
    emailSchedulePanel.setWidget(2, 1, subjectTextBox);

    Label attachmentLabel = new Label(Messages.getString("attachmentNameColon"));
    attachmentLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    emailSchedulePanel.setWidget(3, 0, attachmentLabel);
    emailSchedulePanel.setWidget(3, 1, attachmentNameTextBox);

    messageTextArea.setVisibleLines(5);
    messageTextArea.setWidth("100%");
    emailSchedulePanel.setWidget(4, 0, new Label(Messages.getString("scheduleEmailMessage")));
    emailSchedulePanel.setWidget(5, 0, messageTextArea);
    ((FlexCellFormatter) emailSchedulePanel.getCellFormatter()).setColSpan(5, 0, 2);

    this.add(emailSchedulePanel, CENTER);

    panelWidgetChanged(null);
  }

  public String getName() {
    // TODO Auto-generated method stub
    return MSGS.scheduleEdit();
  }

  protected void panelWidgetChanged(Widget changedWidget) {
    this.setCanContinue(true);
    this.setCanFinish(true);
  }

}
