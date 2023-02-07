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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextBox;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.platform.util.EmailConstants;

public class EmailAdminPanelController extends EmailAdminPanel implements ISysAdminPanel {

  private static EmailAdminPanelController emailAdminPanelController;
  private final EmailTestDialog etd = new EmailTestDialog();
  private boolean isDirty = false;
  private JsEmailConfiguration emailConfig;
  private EmailTester emailTester = null;
  private static final String ERROR_RESOURCE = "error";
  private static final String PORT_VALIDATION_LENGTH_RESOURCE  = "portValidationLength";

  private static final String OAUTH2_SUPPORT_ERROR_RESOURCE = "oauthSupportError";

  private EmailAdminPanelController() {
    activate();

    AsyncCallback<String> emailTestCallback = new AsyncCallback<String>() {
      @Override
      public void onFailure( Throwable err ) {
        MantleApplication.hideBusyIndicator();
        etd.getElement().setId( "emailTestDialog" );
        etd.show( err.getMessage() );

      }

      @Override
      public void onSuccess( String message ) {
        getEmailConfig();
        MantleApplication.hideBusyIndicator();
        etd.getElement().setId( "emailTestDialog" );
        etd.show( message );

      }
    };

    emailTester = new EmailTester( emailTestCallback );

    testButton.addClickHandler( clickEvent -> testEmail() );

    prepareTextBox( smtpHostTextBox, event -> {
      emailConfig.setSmtpHost( smtpHostTextBox.getValue() );
      setDirty( true );
    } );

    prepareTextBox( portTextBox, event -> setDirty( true ) );
    portTextBox.addChangeHandler( event -> {
      if ( isPortValid( portTextBox.getValue() ) ) {
        emailConfig.setSmtpPort( Integer.parseInt( portTextBox.getValue() ) );
        setDirty( true );
      } else {
        new MessageDialogBox( Messages.getString( ERROR_RESOURCE ), Messages.getString( PORT_VALIDATION_LENGTH_RESOURCE ),
                false, false, true ).center();
      }
    } );

    prepareTextBox( fromAddressTextBox, event -> {
      emailConfig.setDefaultFrom( fromAddressTextBox.getValue() );
      setDirty( true );
    } );

    prepareTextBox( fromNameTextBox, event -> {
      emailConfig.setFromName( fromNameTextBox.getValue() );
      setDirty( true );
    } );

    prepareTextBox( userNameTextBox, event -> {
      emailConfig.setUserId( userNameTextBox.getValue() );
      setDirty( true );
    } );

    useSSLCheckBox.addValueChangeHandler( booleanValueChangeEvent -> {
      emailConfig.setUseSsl( useSSLCheckBox.getValue() );
      setDirty( true );
    } );

    useStartTLSCheckBox.addValueChangeHandler( booleanValueChangeEvent -> {
      emailConfig.setUseStartTls( useStartTLSCheckBox.getValue() );
      setDirty( true );
    } );

    protocolsListBox.addChangeHandler( changeEvent -> {
      String protocol = protocolsListBox.getValue( protocolsListBox.getSelectedIndex() );
      boolean isSmtp = isSmtpProtocolFamily( protocol );
      smtpPanel.setVisible( isSmtp );
      useSSLCheckBox.setVisible( isSmtp );
      useStartTLSCheckBox.setVisible( isSmtp );
      if ( protocol.equals( EmailConstants.PROTOCOL_GRAPH_API ) ) {
        authenticationTypeLB.setSelectedIndex( 0 );
        onAuthenticationTypeChanged();
        authenticationTypeLB.setEnabled( false );
      } else {
        authenticationTypeLB.setEnabled( true );
      }
      emailConfig.setSmtpProtocol( protocol );
      setDirty( true );
    } );

    saveButton.addClickHandler( clickEvent -> {
      if ( isPortValid( portTextBox.getValue() ) ) {
        String protocol = protocolsListBox.getValue( protocolsListBox.getSelectedIndex() );
        String smtpHost =  smtpHostTextBox.getValue();
        String authMechanism = authenticationTypeLB.getValue( authenticationTypeLB.getSelectedIndex() );
        if ( isSmtpMicrosoftOAuth( protocol, smtpHost, authMechanism ) ) {
          new MessageDialogBox( Messages.getString( ERROR_RESOURCE ), Messages.getString( OAUTH2_SUPPORT_ERROR_RESOURCE ),
                  false, false, true ).center();
        } else {
          setEmailConfig();
        }
      } else {
        new MessageDialogBox( Messages.getString( ERROR_RESOURCE ), Messages.getString( PORT_VALIDATION_LENGTH_RESOURCE ),
                false, false, true ).center();
      }
    } );

    passwordTextBox.addKeyUpHandler( keyUpEvent -> {
      emailConfig.setPassword( "ENC:" + b64encode( passwordTextBox.getValue() ) );
      setDirty( true );
    } );

    prepareTextBox( clientIdTextBox, event -> {
      emailConfig.setClientId( clientIdTextBox.getValue() );
      setDirty( true );
    } );

    prepareTextBox( clientSecretTextBox, event -> {
      emailConfig.setClientSecret( clientSecretTextBox.getValue() );
      setDirty( true );
    } );

    prepareTextBox( scopeTextBox, event -> {
      emailConfig.setScope( scopeTextBox.getValue() );
      setDirty( true );
    } );

    prepareTextBox( tokenUrlTextBox, event -> {
      emailConfig.setTokenUrl( tokenUrlTextBox.getValue() );
      setDirty( true );
    } );

    prepareTextBox( authorizationCodeTextBox, event -> {
      emailConfig.setAuthorizationCode( authorizationCodeTextBox.getValue() );
      setDirty( true );
    } );

    prepareTextBox( refreshTokenTextBox, event -> {
      emailConfig.setRefreshToken( refreshTokenTextBox.getValue() );
      setDirty( true );
    } );

    prepareTextBox( redirectUriTextBox, event -> {
      emailConfig.setRedirectUri( redirectUriTextBox.getValue() );
      setDirty( true );
    } );

    authenticationTypeLB.addChangeHandler( changeEvent -> {
      onAuthenticationTypeChanged();
      setDirty( true );
    } );

    grantTypeListBox.addChangeHandler( changeEvent -> {
      String grantType = grantTypeListBox.getValue( grantTypeListBox.getSelectedIndex() );
      refreshTokenPanel.setVisible( grantType.equals( EmailConstants.GRANT_TYPE_REFRESH_TOKEN ) );
      authCodePanel.setVisible( grantType.equalsIgnoreCase( EmailConstants.GRANT_TYPE_AUTH_CODE ) );
      emailConfig.setGrantType( grantType );
      setDirty( true );
    } );
  }

  private void onAuthenticationTypeChanged() {
    String authMechanism = authenticationTypeLB.getValue( authenticationTypeLB.getSelectedIndex() );
    oauthPanel.setVisible( authMechanism.equals( EmailConstants.AUTH_TYPE_XOAUTH2 ) );
    passwordPanel.setVisible( authMechanism.equals( EmailConstants.AUTH_TYPE_BASIC ) );
    emailConfig.setAuthMechanism( authMechanism );
    emailConfig.setAuthenticate( !authMechanism.equals( EmailConstants.AUTH_TYPE_NO_AUTH ) );
    authenticationPanel.setVisible( emailConfig.isAuthenticate() );
  }

  private static native String b64encode( String a ) /*-{
    return window.btoa(a);
  }-*/;

  public static EmailAdminPanelController getInstance() {
    if ( emailAdminPanelController == null ) {
      emailAdminPanelController = new EmailAdminPanelController();
    }
    return emailAdminPanelController;
  }

  // -- Remote Calls.

  private void setEmailConfig() {
    String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/setEmailConfig";
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.PUT, serviceUrl );
    try {
      executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      executableTypesRequestBuilder.setHeader( "Content-Type", "application/json" );
      executableTypesRequestBuilder.sendRequest( emailConfig.getJSONString(), new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
        }

        public void onResponseReceived( Request request, Response response ) {
          setDirty( false );
        }
      } );
    } catch ( RequestException e ) {
      //ignored
    }
  }

  private void testEmail() {

    // show the busy indicator...
    MantleApplication.showBusyIndicator( Messages.getString( "pleaseWait" ), Messages
            .getString( "connectionTest.inprog" ) );
    emailTester.test( emailConfig );

  }

  private void getEmailConfig() {
    String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/getEmailConfig?cb=" + System.currentTimeMillis();
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, serviceUrl );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    executableTypesRequestBuilder.setHeader( "accept", "application/json" );
    try {
      executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
        }

        public void onResponseReceived( Request request, Response response ) {
          emailConfig = JsEmailConfiguration.parseJsonString( response.getText() );
          String protocol = emailConfig.getSmtpProtocol();
          selectListBoxHelper( protocolsListBox, protocol );
          authenticationPanel.setVisible( Boolean.parseBoolean( emailConfig.isAuthenticate() + "" ) );
          boolean isSmtp = isSmtpProtocolFamily( protocol );
          smtpPanel.setVisible( isSmtp );
          smtpHostTextBox.setValue( emailConfig.getSmtpHost() );
          portTextBox.setValue( emailConfig.getSmtpPort() + "" );
          String authMechanism = emailConfig.getAuthMechanism();
          selectListBoxHelper( authenticationTypeLB, authMechanism );

          authenticationTypeLB.setEnabled( !protocol.equals( EmailConstants.PROTOCOL_GRAPH_API ) );
          useStartTLSCheckBox.setVisible( isSmtp );
          useStartTLSCheckBox.setValue( Boolean.parseBoolean( emailConfig.isUseStartTls() + "" ) );
          useSSLCheckBox.setVisible( isSmtp );
          useSSLCheckBox.setValue( Boolean.parseBoolean( emailConfig.isUseSsl() + "" ) );
          fromAddressTextBox.setValue( emailConfig.getDefaultFrom() );
          fromNameTextBox.setValue( emailConfig.getFromName() );
          userNameTextBox.setValue( emailConfig.getUserId() );

          // If password is non-empty.. disable the text-box
          String password = emailConfig.getPassword();
          passwordPanel.setVisible( authMechanism.equals( EmailConstants.AUTH_TYPE_BASIC ) );
          passwordTextBox.setValue( password );

          oauthPanel.setVisible( authMechanism.equals( EmailConstants.AUTH_TYPE_XOAUTH2 ) );
          clientIdTextBox.setValue( emailConfig.getClientId() );
          clientSecretTextBox.setValue( emailConfig.getClientSecret() );
          scopeTextBox.setValue( emailConfig.getScope() );

          String grantType = emailConfig.getGrantType();
          selectListBoxHelper( grantTypeListBox, grantType );

          tokenUrlTextBox.setValue( emailConfig.getTokenUrl() );

          refreshTokenPanel.setVisible( grantType.equals( EmailConstants.GRANT_TYPE_REFRESH_TOKEN ) );
          refreshTokenTextBox.setValue( emailConfig.getRefreshToken() );

          authCodePanel.setVisible( grantType.equals( EmailConstants.GRANT_TYPE_AUTH_CODE ) );
          authorizationCodeTextBox.setValue( emailConfig.getAuthorizationCode() );
          redirectUriTextBox.setValue( emailConfig.getRedirectUri() );

        }
      } );
    } catch ( RequestException e ) {
      //ignored
    }
  }

  // -- ISysAdminPanel implementation.

  public void activate() {
    setDirty( false );
    getEmailConfig();
  }

  public String getId() {
    return "emailAdminPanel";
  }

  public void passivate( final AsyncCallback<Boolean> callback ) {
    if ( isDirty ) {
      MessageDialogBox messageBox = new MessageDialogBox( Messages.getString( "confirm" ),
              Messages.getString( "dirtyStateMessage" ), false, false, true,
              Messages.getString( "yes" ), null, Messages.getString( "no" ) );
      messageBox.setCallback( new IDialogCallback() {

        @Override
        public void okPressed() {
          if ( isPortValid( portTextBox.getValue() ) ) {
            setEmailConfig();
            callback.onSuccess( true );
            setDirty( false );
          } else {
            new MessageDialogBox( Messages.getString( ERROR_RESOURCE ), Messages.getString( PORT_VALIDATION_LENGTH_RESOURCE ),
                    false, false, true ).center();
          }
        }

        @Override
        public void cancelPressed() {
          callback.onSuccess( true );
          setDirty( false );
        }
      } );
      messageBox.center();
    } else {
      callback.onSuccess( true );
    }
  }

  private void setDirty( boolean isDirty ) {
    this.isDirty = isDirty;
    saveButton.setEnabled( isDirty );
  }

  private void prepareTextBox( final TextBox textBox, final ChangeHandler changeHandler ) {
    textBox.addKeyUpHandler( event -> changeHandler.onChange( null ) );
    textBox.addMouseUpHandler( event -> {
      final String oldValue = textBox.getValue();
      new Timer() { // set timer for IE 'x' clear input button.
        @Override
        public void run() {
          if ( !oldValue.equals( textBox.getValue() ) ) {
            changeHandler.onChange( null );
          }
        }
      }.schedule( 100 );
    } );
  }
}
