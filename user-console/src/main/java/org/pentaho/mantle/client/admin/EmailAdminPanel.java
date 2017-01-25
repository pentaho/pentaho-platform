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

package org.pentaho.mantle.client.admin;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.mantle.client.messages.Messages;

public class EmailAdminPanel extends SimplePanel {

  protected TextBox smtpHostTextBox;
  protected TextBox portTextBox;
  protected ListBox protocolsListBox;
  protected CheckBox useStartTLSCheckBox;
  protected CheckBox useSSLCheckBox;
  protected TextBox fromAddressTextBox;
  protected TextBox fromNameTextBox;
  protected CheckBox authenticationCheckBox;
  protected TextBox userNameTextBox;
  protected PasswordTextBox passwordTextBox;
  protected Button testButton;
  protected Button saveButton;
  protected VerticalPanel authenticationPanel;

  public EmailAdminPanel() {
    this.setWidth( "100%" );
    this.setHeight( "100%" );
    setWidget( createEmailPanel() );
    saveButton.setEnabled( true );
  }

  private Widget createEmailPanel() {
    VerticalPanel mailPanel = new VerticalPanel();

    Label mailServerLabel = new Label( Messages.getString( "mailServer" ) );
    mailServerLabel.setStyleName( "pentaho-fieldgroup-major" );
    mailPanel.add( mailServerLabel );

    SimplePanel vSpacer = new SimplePanel();
    vSpacer.setHeight( "20px" );
    mailPanel.add( vSpacer );

    Label serverSettingsLabel = new Label( Messages.getString( "serverSettings" ) );
    serverSettingsLabel.setStyleName( "pentaho-fieldgroup-minor" );
    mailPanel.add( serverSettingsLabel );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "20px" );
    mailPanel.add( vSpacer );

    mailPanel.add( new Label( Messages.getString( "smtpHost" ) + ":" ) );
    smtpHostTextBox = new TextBox();
    smtpHostTextBox.setWidth( "220px" );
    mailPanel.add( smtpHostTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "10px" );
    mailPanel.add( vSpacer );

    mailPanel.add( new Label( Messages.getString( "port" ) + ":" ) );
    portTextBox = new TextBox();
    portTextBox.setWidth( "220px" );
    mailPanel.add( portTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "10px" );
    mailPanel.add( vSpacer );

    authenticationCheckBox = new CheckBox( Messages.getString( "useAuthentication" ) );
    mailPanel.add( authenticationCheckBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "10px" );
    mailPanel.add( vSpacer );

    authenticationPanel = new VerticalPanel();
    mailPanel.add( authenticationPanel );
    authenticationPanel.add( new Label( Messages.getString( "userName" ) + ":" ) );
    userNameTextBox = new TextBox();
    userNameTextBox.setWidth( "220px" );
    authenticationPanel.add( userNameTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "10px" );
    authenticationPanel.add( vSpacer );

    authenticationPanel.add( new Label( Messages.getString( "password" ) + ":" ) );
    HorizontalPanel hPanel = new HorizontalPanel();
    passwordTextBox = new PasswordTextBox();
    passwordTextBox.setWidth( "220px" );
    hPanel.add( passwordTextBox );

    SimplePanel hSpacer = new SimplePanel();
    hSpacer = new SimplePanel();
    hSpacer.setWidth( "15px" );
    hPanel.add( hSpacer );

    authenticationPanel.add( hPanel );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "10px" );
    mailPanel.add( vSpacer );

    HorizontalPanel protocolHbox = new HorizontalPanel();
    protocolHbox.add( new Label( Messages.getString( "protocol" ) + ":" ) );

    hSpacer = new SimplePanel();
    hSpacer.setWidth( "15px" );
    protocolHbox.add( hSpacer );

    protocolsListBox = new ListBox();
    protocolsListBox.addItem( Messages.getString( "smtp" ) );
    protocolsListBox.addItem( Messages.getString( "smtps" ) );
    protocolHbox.add( protocolsListBox );
    mailPanel.add( protocolHbox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "10px" );
    mailPanel.add( vSpacer );

    Label emailOrginLabel = new Label( Messages.getString( "emailOriginLabel" ) );
    mailPanel.add( emailOrginLabel );
    fromAddressTextBox = new TextBox();
    fromAddressTextBox.setWidth( "220px" );
    mailPanel.add( fromAddressTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "10px" );
    mailPanel.add( vSpacer );

    Label emailFromName = new Label( Messages.getString( "emailFromNameLabel" ) );
    mailPanel.add( emailFromName );
    fromNameTextBox = new TextBox();
    fromNameTextBox.setWidth( "220px" );
    mailPanel.add( fromNameTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "10px" );
    mailPanel.add( vSpacer );

    useStartTLSCheckBox = new CheckBox( Messages.getString( "useStartTLS" ) );
    mailPanel.add( useStartTLSCheckBox );

    useSSLCheckBox = new CheckBox( Messages.getString( "useSSL" ) );
    mailPanel.add( useSSLCheckBox );

    // debuggingCheckBox = new CheckBox(Messages.getString("enableDebugging"));
    // mailPanel.add(debuggingCheckBox);

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "20px" );
    mailPanel.add( vSpacer );

    HorizontalPanel buttonsPanel = new HorizontalPanel();
    mailPanel.add( buttonsPanel );

    testButton = new Button( Messages.getString( "connectionTest.label" ) );
    testButton.setStylePrimaryName( "pentaho-button" );
    buttonsPanel.add( testButton );

    hSpacer = new SimplePanel();
    hSpacer.setWidth( "10px" );
    buttonsPanel.add( hSpacer );

    saveButton = new Button( Messages.getString( "save" ) );
    saveButton.setStylePrimaryName( "pentaho-button" );
    buttonsPanel.add( saveButton );

    return mailPanel;
  }

  protected boolean isPortValid( String portValue ) {
    try {
      int portValueInt = Integer.parseInt( portValue );
      return portValueInt >= 0 && portValueInt <= 65535;
    } catch ( NumberFormatException e ) {
      return false;
    }
  }
}
