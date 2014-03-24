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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.messages.Messages;

public class EmailAdminPanelController extends EmailAdminPanel implements ISysAdminPanel, UpdatePasswordController {

  private boolean isDirty = false;
  private JsEmailConfiguration emailConfig;
  private static EmailAdminPanelController emailAdminPanelController;
  private EmailTester emailTester = null;
  private final EmailTestDialog etd = new EmailTestDialog();

  public static EmailAdminPanelController getInstance() {
    if ( emailAdminPanelController == null ) {
      emailAdminPanelController = new EmailAdminPanelController();
    }
    return emailAdminPanelController;
  }

  private EmailAdminPanelController() {
    activate();

    AsyncCallback<String> emailTestCallback = new AsyncCallback<String>() {
      @Override
      public void onFailure( Throwable err ) {
        MantleApplication.hideBusyIndicator();
        etd.show( err.getMessage() );

      }

      @Override
      public void onSuccess( String message ) {
        MantleApplication.hideBusyIndicator();
        etd.show( message );
      }
    };

    emailTester = new EmailTester( emailTestCallback );

    testButton.addClickHandler( new ClickHandler() {
      public void onClick( final ClickEvent clickEvent ) {
        testEmail();
      }
    } );

    authenticationCheckBox.addValueChangeHandler( new ValueChangeHandler<Boolean>() {
      public void onValueChange( final ValueChangeEvent<Boolean> booleanValueChangeEvent ) {
        emailConfig.setAuthenticate( booleanValueChangeEvent.getValue() );
        authenticationPanel.setVisible( booleanValueChangeEvent.getValue() );
        setDirty( true );
      }
    } );

    prepareTextBox( smtpHostTextBox, new ChangeHandler() {
      @Override
      public void onChange( ChangeEvent event ) {
        emailConfig.setSmtpHost( smtpHostTextBox.getValue() );
        setDirty( true );
      }
    } );

    prepareTextBox( portTextBox, new ChangeHandler() {
      @Override
      public void onChange( ChangeEvent event ) {
        setDirty( true );
      }
    } );
    portTextBox.addChangeHandler( new ChangeHandler() {
      @Override
      public void onChange( ChangeEvent event ) {
        if ( isPortValid( portTextBox.getValue() ) ) {
          emailConfig.setSmtpPort( Integer.parseInt( portTextBox.getValue() ) );
          setDirty( true );
        } else {
          new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "portValidationLength" ), false
              , false, true ).center();
        }
      }
    } );

    prepareTextBox( fromAddressTextBox, new ChangeHandler() {
      @Override
      public void onChange( ChangeEvent event ) {
        emailConfig.setDefaultFrom( fromAddressTextBox.getValue() );
        setDirty( true );
      }
    } );

    prepareTextBox( fromNameTextBox, new ChangeHandler() {
      @Override
      public void onChange( ChangeEvent event ) {
        emailConfig.setFromName( fromNameTextBox.getValue() );
        setDirty( true );
      }
    } );

    prepareTextBox( userNameTextBox, new ChangeHandler() {
      @Override
      public void onChange( ChangeEvent event ) {
        emailConfig.setUserId( userNameTextBox.getValue() );
        setDirty( true );
      }
    } );

    useSSLCheckBox.addValueChangeHandler( new ValueChangeHandler<Boolean>() {
      public void onValueChange( final ValueChangeEvent<Boolean> booleanValueChangeEvent ) {
        emailConfig.setUseSsl( useSSLCheckBox.getValue() );
        setDirty( true );
      }
    } );

    useStartTLSCheckBox.addValueChangeHandler( new ValueChangeHandler<Boolean>() {
      public void onValueChange( final ValueChangeEvent<Boolean> booleanValueChangeEvent ) {
        emailConfig.setUseStartTls( useStartTLSCheckBox.getValue() );
        setDirty( true );
      }
    } );

    protocolsListBox.addChangeHandler( new ChangeHandler() {
      public void onChange( final ChangeEvent changeEvent ) {
        emailConfig.setSmtpProtocol( protocolsListBox.getItemText( protocolsListBox.getSelectedIndex() ) );
        setDirty( true );
      }
    } );

    saveButton.addClickHandler( new ClickHandler() {
      public void onClick( final ClickEvent clickEvent ) {
        if ( isPortValid( portTextBox.getValue() ) ) {
          setEmailConfig();
        } else {
          new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "portValidationLength" ), false
              , false, true ).center();
        }
      }
    } );

    passwordTextBox.addKeyUpHandler( new KeyUpHandler() {
      public void onKeyUp( final KeyUpEvent keyUpEvent ) {
        emailConfig.setPassword( passwordTextBox.getValue() );
        setDirty( true );
      }
    } );
  }

  public void updatePassword( String password ) {
    emailConfig.setPassword( password );
    passwordTextBox.setValue( password );
    if ( !StringUtils.isEmpty( passwordTextBox.getValue() ) ) {
      passwordTextBox.setEnabled( false );
    }
    setDirty( true );
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
          authenticationCheckBox.setValue( Boolean.parseBoolean( emailConfig.isAuthenticate() + "" ) );
          authenticationPanel.setVisible( Boolean.parseBoolean( emailConfig.isAuthenticate() + "" ) );
          smtpHostTextBox.setValue( emailConfig.getSmtpHost() );
          portTextBox.setValue( emailConfig.getSmtpPort() + "" );
          useStartTLSCheckBox.setValue( Boolean.parseBoolean( emailConfig.isUseStartTls() + "" ) );
          useSSLCheckBox.setValue( Boolean.parseBoolean( emailConfig.isUseSsl() + "" ) );
          fromAddressTextBox.setValue( emailConfig.getDefaultFrom() );
          fromNameTextBox.setValue( emailConfig.getFromName() );
          userNameTextBox.setValue( emailConfig.getUserId() );

          // If password is non-empty.. disable the text-box
          String password = emailConfig.getPassword();
          passwordTextBox.setValue( password );

          String protocol = emailConfig.getSmtpProtocol();
          protocolsListBox.setSelectedIndex( -1 );
          if ( !StringUtils.isEmpty( protocol ) ) {
            for ( int i = 0; i < protocolsListBox.getItemCount(); ++i ) {
              if ( protocol.equalsIgnoreCase( protocolsListBox.getItemText( i ) ) ) {
                protocolsListBox.setSelectedIndex( i );
                break;
              }
            }
          }
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
      MessageDialogBox messageBox = new MessageDialogBox( Messages.getString( "confirm" )
          , Messages.getString( "dirtyStateMessage" ), false, false, true, Messages.getString( "yes" ), null
          , Messages.getString( "no" ) );
      messageBox.setCallback( new IDialogCallback() {

        @Override
        public void okPressed() {
          if ( isPortValid( portTextBox.getValue() ) ) {
            setEmailConfig();
            callback.onSuccess( true );
            setDirty( false );
          } else {
            new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "portValidationLength" ), false
                , false, true ).center();
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
    textBox.addKeyUpHandler( new KeyUpHandler() {
      @Override
      public void onKeyUp( KeyUpEvent event ) {
        changeHandler.onChange( null );
      }
    } );
    textBox.addMouseUpHandler( new MouseUpHandler() {
      @Override
      public void onMouseUp( MouseUpEvent event ) {
        final String oldValue = textBox.getValue();
        new Timer() { // set timer for IE 'x' clear input button.
          @Override
          public void run() {
            if ( !oldValue.equals( textBox.getValue() ) ) {
              changeHandler.onChange( null );
            }
          }
        } .schedule( 100 );
      }
    } );
  }
}
