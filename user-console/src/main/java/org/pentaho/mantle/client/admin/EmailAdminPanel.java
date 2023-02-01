/*!
 *
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
 *
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 *
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
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.platform.util.EmailConstants;

public class EmailAdminPanel extends SimplePanel {

  protected TextBox smtpHostTextBox;
  protected TextBox portTextBox;
  protected ListBox protocolsListBox;
  protected CheckBox useStartTLSCheckBox;
  protected CheckBox useSSLCheckBox;
  protected TextBox fromAddressTextBox;
  protected TextBox fromNameTextBox;
  protected TextBox userNameTextBox;
  protected PasswordTextBox passwordTextBox;
  protected Button testButton;
  protected Button saveButton;
  protected VerticalPanel authenticationPanel;
  protected TextBox tokenUrlTextBox;
  protected VerticalPanel oauthPanel;
  protected VerticalPanel passwordPanel;
  protected ListBox authenticationTypeLB;
  protected TextBox clientIdTextBox;
  protected PasswordTextBox clientSecretTextBox;
  protected TextBox scopeTextBox;
  protected ListBox grantTypeListBox;
  protected TextBox refreshTokenTextBox;
  protected VerticalPanel refreshTokenPanel;
  protected TextBox authorizationCodeTextBox;
  protected VerticalPanel authCodePanel;
  protected TextBox redirectUriTextBox;
  protected VerticalPanel smtpPanel;
  /**
   * The fixed width for all panels in Email admin setup page
   */
  private static final String PANEL_WIDTH = "220px";

  /**
   * The width between items in a HorizontalPanel
   */
  private static final String HSPACER_WIDTH = "15px";

  /**
   * The height between Items in Vertical Panel
   */
  private static final String VSPACER_HEIGHT_10 = "10px";
  /**
   * The height between Items in Vertical Panel
   */
  private static final String VSPACER_HEIGHT_20 = "20px";

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
    vSpacer.setHeight( VSPACER_HEIGHT_20 );
    mailPanel.add( vSpacer );

    Label serverSettingsLabel = new Label( Messages.getString( "serverSettings" ) );
    serverSettingsLabel.setStyleName( "pentaho-fieldgroup-minor" );
    mailPanel.add( serverSettingsLabel );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_20 );
    mailPanel.add( vSpacer );

    HorizontalPanel protocolHbox = new HorizontalPanel();
    protocolHbox.add( new Label( Messages.getString( "connectionProtocol" ) + ":" ) );
    protocolHbox.getElement().setId( "protocolPanel" );

    SimplePanel hSpacer = new SimplePanel();
    hSpacer.setWidth( HSPACER_WIDTH );
    protocolHbox.add( hSpacer );

    protocolsListBox = new ListBox();
    protocolsListBox.addItem( Messages.getString( "smtp" ), EmailConstants.PROTOCOL_SMTP );
    protocolsListBox.addItem( Messages.getString( "smtps" ), EmailConstants.PROTOCOL_SMTPS );
    protocolsListBox.addItem( Messages.getString( "graphApiLabel" ), EmailConstants.PROTOCOL_GRAPH_API );
    protocolHbox.add( protocolsListBox );
    mailPanel.add( protocolHbox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    mailPanel.add( vSpacer );

    smtpPanel = new VerticalPanel();
    smtpPanel.add( new Label( Messages.getString( "smtpHost" ) + ":" ) );
    smtpHostTextBox = new TextBox();
    smtpHostTextBox.setWidth( PANEL_WIDTH );
    smtpPanel.add( smtpHostTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    smtpPanel.add( vSpacer );

    smtpPanel.add( new Label( Messages.getString( "port" ) + ":" ) );
    portTextBox = new TextBox();
    portTextBox.setWidth( PANEL_WIDTH );
    smtpPanel.add( portTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    smtpPanel.add( vSpacer );

    mailPanel.add( smtpPanel );

    mailPanel.add( new Label( Messages.getString( "authenticationType" ) + ":" ) );
    authenticationTypeLB = new ListBox();
    authenticationTypeLB.addItem( Messages.getString( "oauthLabel" ), EmailConstants.AUTH_TYPE_XOAUTH2 );
    authenticationTypeLB.addItem( Messages.getString( "basicLabel" ), EmailConstants.AUTH_TYPE_BASIC );
    authenticationTypeLB.addItem( Messages.getString( "noAuthLabel" ), EmailConstants.AUTH_TYPE_NO_AUTH );
    authenticationTypeLB.setWidth( PANEL_WIDTH );
    mailPanel.add( authenticationTypeLB );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    mailPanel.add( vSpacer );

    authenticationPanel = new VerticalPanel();
    mailPanel.add( authenticationPanel );
    authenticationPanel.add( new Label( Messages.getString( "userName" ) + ":" ) );
    userNameTextBox = new TextBox();
    userNameTextBox.setWidth( PANEL_WIDTH );
    authenticationPanel.add( userNameTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    authenticationPanel.add( vSpacer );

    passwordPanel = new VerticalPanel();
    passwordPanel.add( new Label( Messages.getString( "password" ) + ":" ) );
    HorizontalPanel hPanel = new HorizontalPanel();
    passwordTextBox = new PasswordTextBox();
    passwordTextBox.setWidth( PANEL_WIDTH );
    hPanel.add( passwordTextBox );

    hSpacer = new SimplePanel();
    hSpacer.setWidth( HSPACER_WIDTH );
    hPanel.add( hSpacer );

    passwordPanel.add( hPanel );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    passwordPanel.add( vSpacer );

    authenticationPanel.add( passwordPanel );

    oauthPanel = new VerticalPanel();
    oauthPanel.add( new Label( Messages.getString( "clientId" ) + ":" ) );
    clientIdTextBox = new TextBox();
    clientIdTextBox.setWidth( PANEL_WIDTH );
    oauthPanel.add( clientIdTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    oauthPanel.add( vSpacer );

    oauthPanel.add( new Label( Messages.getString( "clientSecret" ) + ":" ) );
    clientSecretTextBox = new PasswordTextBox();
    clientSecretTextBox.setWidth( PANEL_WIDTH );
    oauthPanel.add( clientSecretTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    oauthPanel.add( vSpacer );

    oauthPanel.add( new Label( Messages.getString( "scope" ) + ":" ) );
    scopeTextBox = new TextBox();
    scopeTextBox.setWidth( PANEL_WIDTH );
    oauthPanel.add( scopeTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    oauthPanel.add( vSpacer );

    oauthPanel.add( new Label( Messages.getString( "grantType" ) + ":" ) );
    grantTypeListBox = new ListBox();
    grantTypeListBox.addItem( Messages.getString( "clientCredentialsLabel" ), EmailConstants.GRANT_TYPE_CLIENT_CREDENTIALS );
    grantTypeListBox.addItem( Messages.getString( "authorizationCodeLabel" ), EmailConstants.GRANT_TYPE_AUTH_CODE );
    grantTypeListBox.addItem( Messages.getString( "refreshTokenLabel" ), EmailConstants.GRANT_TYPE_REFRESH_TOKEN );
    grantTypeListBox.setWidth( PANEL_WIDTH );
    oauthPanel.add( grantTypeListBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    oauthPanel.add( vSpacer );

    oauthPanel.add( new Label( Messages.getString( "tokenUrl" ) + ":" ) );
    tokenUrlTextBox = new TextBox();
    tokenUrlTextBox.setWidth( PANEL_WIDTH );
    oauthPanel.add( tokenUrlTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    oauthPanel.add( vSpacer );

    refreshTokenPanel  = new VerticalPanel();
    refreshTokenPanel.add( new Label( Messages.getString( "refreshTokenLabel" ) + ":" ) );
    refreshTokenTextBox = new TextBox();
    refreshTokenTextBox.setWidth( PANEL_WIDTH );
    refreshTokenPanel.add( refreshTokenTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( "10px" );
    refreshTokenPanel.add( vSpacer );

    oauthPanel.add( refreshTokenPanel );

    authCodePanel = new VerticalPanel();
    authCodePanel.add( new Label( Messages.getString( "authorizationCodeLabel" ) + ":" ) );
    authorizationCodeTextBox = new TextBox();
    authorizationCodeTextBox.setWidth( PANEL_WIDTH );
    authCodePanel.add( authorizationCodeTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    authCodePanel.add( vSpacer );

    authCodePanel.add( new Label( Messages.getString( "redirectUri" ) + ":" ) );
    redirectUriTextBox = new TextBox();
    redirectUriTextBox.setWidth( PANEL_WIDTH );
    authCodePanel.add( redirectUriTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    authCodePanel.add( vSpacer );

    oauthPanel.add( authCodePanel );

    authenticationPanel.add( oauthPanel );

    Label emailOrginLabel = new Label( Messages.getString( "emailOriginLabel" ) );
    mailPanel.add( emailOrginLabel );
    fromAddressTextBox = new TextBox();
    fromAddressTextBox.setWidth( PANEL_WIDTH );
    mailPanel.add( fromAddressTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    mailPanel.add( vSpacer );

    Label emailFromName = new Label( Messages.getString( "emailFromNameLabel" ) );
    mailPanel.add( emailFromName );
    fromNameTextBox = new TextBox();
    fromNameTextBox.setWidth( PANEL_WIDTH );
    mailPanel.add( fromNameTextBox );

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_10 );
    mailPanel.add( vSpacer );

    useStartTLSCheckBox = new CheckBox( Messages.getString( "useStartTLS" ) );
    mailPanel.add( useStartTLSCheckBox );

    useSSLCheckBox = new CheckBox( Messages.getString( "useSSL" ) );
    mailPanel.add( useSSLCheckBox );

    // debuggingCheckBox = new CheckBox(Messages.getString("enableDebugging"));
    // mailPanel.add(debuggingCheckBox);

    vSpacer = new SimplePanel();
    vSpacer.setHeight( VSPACER_HEIGHT_20 );
    mailPanel.add( vSpacer );

    HorizontalPanel buttonsPanel = new HorizontalPanel();
    mailPanel.add( buttonsPanel );

    testButton = new Button( Messages.getString( "connectionTest.label" ) );
    testButton.setStylePrimaryName( "pentaho-button" );
    buttonsPanel.add( testButton );

    hSpacer = new SimplePanel();
    hSpacer.setWidth( HSPACER_WIDTH );
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

  protected boolean isSmtpMicrosoftOAuth( String protocol, String smtpHost, String authMechanism ) {
    return ( isSmtpProtocolFamily( protocol ) && ( smtpHost.contains( "outlook" ) || smtpHost.contains( "office" ) ) && authMechanism.equals( EmailConstants.AUTH_TYPE_XOAUTH2 ) );
  }

  protected boolean isSmtpProtocolFamily( String protocol ) {
    return ( protocol.equalsIgnoreCase( EmailConstants.PROTOCOL_SMTP ) ) || ( protocol.equalsIgnoreCase( EmailConstants.PROTOCOL_SMTPS ) );
  }

  protected void selectListBoxHelper( ListBox listbox, String selectedValue ) {
    if ( listbox != null ) {
      listbox.setSelectedIndex( -1 );
      if ( !StringUtils.isEmpty( selectedValue ) ) {
        for ( int i = 0; i < listbox.getItemCount(); ++i ) {
          if ( selectedValue.equalsIgnoreCase( listbox.getValue( i ) ) ) {
            listbox.setSelectedIndex( i );
            break;
          }
        }
      }
    }
  }
}
