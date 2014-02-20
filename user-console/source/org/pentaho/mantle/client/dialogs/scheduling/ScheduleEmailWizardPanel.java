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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobParam;

public class ScheduleEmailWizardPanel extends AbstractWizardPanel {

  private static final String PENTAHO_SCHEDULE = "pentaho-schedule-create"; //$NON-NLS-1$

  private RadioButton yes = new RadioButton( "SCH_EMAIL_YESNO", "Yes" );
  private RadioButton no = new RadioButton( "SCH_EMAIL_YESNO", "No" );
  private TextBox toAddressTextBox = new TextBox();
  private TextBox subjectTextBox = new TextBox();
  private TextBox attachmentNameTextBox = new TextBox();
  private TextArea messageTextArea = new TextArea();

  private String filePath;
  private JSONObject jobSchedule;

  public ScheduleEmailWizardPanel( String filePath, JSONObject jobSchedule, JsJob job ) {
    super();
    this.filePath = filePath;
    this.jobSchedule = jobSchedule;
    layout( job );
  }

  private native JsArray<JsSchedulingParameter> getParams( String to, String cc, String bcc, String subject,
      String message, String attachmentName )
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
    if ( yes.getValue() ) {
      return getParams( toAddressTextBox.getText(), "", "", subjectTextBox.getText(), messageTextArea.getText(),
          attachmentNameTextBox.getText() );
    } else {
      return null;
    }
  }

  private void layout( JsJob job ) {
    this.addStyleName( PENTAHO_SCHEDULE );

    final FlexTable emailSchedulePanel = new FlexTable();
    emailSchedulePanel.setVisible( false );
    HorizontalPanel emailYesNoPanel = new HorizontalPanel();
    emailYesNoPanel.add( ( new Label( Messages.getString( "wouldYouLikeToEmail" ) ) ) );
    no.addClickHandler( new ClickHandler() {
      public void onClick( ClickEvent event ) {
        emailSchedulePanel.setVisible( !no.getValue() );
        setCanContinue( isValidConfig() );
        setCanFinish( isValidConfig() );
      }
    } );
    yes.addClickHandler( new ClickHandler() {
      public void onClick( ClickEvent event ) {
        emailSchedulePanel.setVisible( yes.getValue() );
        setCanContinue( isValidConfig() );
        setCanFinish( isValidConfig() );
        setFocus();
      }
    } );
    no.setValue( true );
    yes.setValue( false );
    emailYesNoPanel.add( no );
    emailYesNoPanel.add( yes );
    this.add( emailYesNoPanel, NORTH );

    toAddressTextBox.setVisibleLength( 95 );
    subjectTextBox.setVisibleLength( 95 );
    attachmentNameTextBox.setVisibleLength( 95 );

    boolean hasExtension = filePath.lastIndexOf( "." ) != -1;
    String friendlyFileName = filePath.substring( filePath.lastIndexOf( "/" ) + 1 );
    if ( hasExtension ) {
      // remove it
      friendlyFileName = friendlyFileName.substring( 0, friendlyFileName.lastIndexOf( "." ) );
    }
    subjectTextBox.setText( Messages.getString( "scheduleDefaultSubject", friendlyFileName ) );
    if ( job != null ) {
      attachmentNameTextBox.setText( job.getJobName() );
    } else {
      attachmentNameTextBox.setText( jobSchedule.get( "jobName" ).isString().stringValue() );
    }

    Label toLabel = new Label( Messages.getString( "toColon" ) );
    // toLabel.setWidth("130px");
    toLabel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
    Label toAddressLabel = new Label( Messages.getString( "scheduleAddressSeparatorMessage" ) );
    toAddressLabel.setStyleName( "msg-Label" );

    HorizontalPanel toLabelPanel = new HorizontalPanel();
    toLabelPanel.add( toLabel );
    toLabelPanel.add( toAddressLabel );

    toAddressTextBox.addKeyUpHandler( new KeyUpHandler() {
      public void onKeyUp( KeyUpEvent event ) {
        setCanContinue( isValidConfig() );
        setCanFinish( isValidConfig() );
      }
    } );

    emailSchedulePanel.setWidget( 0, 0, toLabelPanel );
    toAddressTextBox.getElement().getStyle().setHeight( 25, Unit.PX );
    toAddressTextBox.getElement().getStyle().setPaddingLeft( 5, Unit.PX );
    emailSchedulePanel.setWidget( 1, 0, toAddressTextBox );

    Label subjectLabel = new Label( Messages.getString( "subjectColon" ) );
    subjectLabel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
    subjectTextBox.getElement().getStyle().setHeight( 25, Unit.PX );
    subjectTextBox.getElement().getStyle().setPaddingLeft( 5, Unit.PX );
    emailSchedulePanel.setWidget( 3, 0, subjectLabel );
    emailSchedulePanel.setWidget( 4, 0, subjectTextBox );

    Label attachmentLabel = new Label( Messages.getString( "attachmentNameColon" ) );
    attachmentLabel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
    attachmentNameTextBox.getElement().getStyle().setHeight( 25, Unit.PX );
    attachmentNameTextBox.getElement().getStyle().setPaddingLeft( 5, Unit.PX );
    emailSchedulePanel.setWidget( 5, 0, attachmentLabel );
    emailSchedulePanel.setWidget( 6, 0, attachmentNameTextBox );

    messageTextArea.setVisibleLines( 5 );
    messageTextArea.setWidth( "100%" );
    Label messageLabel = new Label( Messages.getString( "scheduleEmailMessage" ) );
    emailSchedulePanel.setWidget( 7, 0, messageLabel );
    emailSchedulePanel.setWidget( 8, 0, messageTextArea );

    if ( job != null ) {
      JsArray<JsJobParam> jparams = job.getJobParams();
      for ( int i = 0; i < jparams.length(); i++ ) {
        if ( "_SCH_EMAIL_TO".equals( jparams.get( i ).getName() ) ) {
          yes.setValue( true );
          no.setValue( false );
          emailSchedulePanel.setVisible( true );
          toAddressTextBox.setText( jparams.get( i ).getValue() );
        } else if ( "_SCH_EMAIL_SUBJECT".equals( jparams.get( i ).getName() ) ) {
          subjectTextBox.setText( jparams.get( i ).getValue() );
        } else if ( "_SCH_EMAIL_MESSAGE".equals( jparams.get( i ).getName() ) ) {
          messageTextArea.setText( jparams.get( i ).getValue() );
        } else if ( "_SCH_EMAIL_ATTACHMENT_NAME".equals( jparams.get( i ).getName() ) ) {
          attachmentNameTextBox.setText( jparams.get( i ).getValue() );
        }
      }
    }

    this.add( emailSchedulePanel, CENTER );

    panelWidgetChanged( null );
  }

  public String getName() {
    return Messages.getString( "schedule.scheduleEdit" );
  }

  private boolean isValidConfig() {
    if ( no.getValue() ) {
      return true;
    }
    boolean empty = StringUtils.isEmpty( toAddressTextBox.getText() );
    if ( !empty ) {
      int at = toAddressTextBox.getText().indexOf( "@" );
      if ( at > 0 && at < toAddressTextBox.getText().length() - 1 ) {
        return true;
      }
    }
    return false;
  }

  protected void panelWidgetChanged( Widget changedWidget ) {
    setCanContinue( isValidConfig() );
    setCanFinish( isValidConfig() );
  }

  public void setFocus() {
    Timer t = new Timer() {
      public void run() {
        toAddressTextBox.getElement().blur();
        toAddressTextBox.setFocus( false );
        toAddressTextBox.setFocus( true );
        toAddressTextBox.getElement().focus();
        if ( toAddressTextBox.isAttached() && toAddressTextBox.isVisible() ) {
          cancel();
        }
      }
    };
    t.scheduleRepeating( 250 );
  }

}
