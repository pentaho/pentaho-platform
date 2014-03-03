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

package org.pentaho.mantle.login.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.mantle.login.client.messages.Messages;

import java.util.Date;

public class MantleLoginDialog extends PromptDialogBox {

  private AsyncCallback<Boolean> outerCallback; // from outside context
  private final TextBox userTextBox = new TextBox();
  private final ListBox usersListBox = new ListBox();
  private final PasswordTextBox passwordTextBox = new PasswordTextBox();
  private CheckBox newWindowChk = new CheckBox();
  private String returnLocation = null;

  private static boolean showNewWindowOption = true;

  public MantleLoginDialog() {
    super( Messages.getString( "login" ), Messages.getString( "login" ), Messages.getString( "cancel" ), false, true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    IDialogCallback myCallback = new IDialogCallback() {

      public void cancelPressed() {
      }

      @SuppressWarnings( "deprecation" )
      public void okPressed() {
        String path = Window.Location.getPath();
        if ( !path.endsWith( "/" ) ) { //$NON-NLS-1$
          path = path.substring( 0, path.lastIndexOf( "/" ) + 1 ); //$NON-NLS-1$
        }
        RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, path + "j_spring_security_check" ); //$NON-NLS-1$
        builder.setHeader( "Content-Type", "application/x-www-form-urlencoded" ); //$NON-NLS-1$ //$NON-NLS-2$
        builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
        RequestCallback callback = new RequestCallback() {

          public void onError( Request request, Throwable exception ) {
            outerCallback.onFailure( exception );
          }

          public void onResponseReceived( Request request, Response response ) {

            try {
              final String url = GWT.getHostPageBaseURL() + "api/mantle/isAuthenticated"; //$NON-NLS-1$
              RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
              requestBuilder.setHeader( "accept", "text/plain" );
              requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
              requestBuilder.sendRequest( null, new RequestCallback() {

                public void onError( Request request, final Throwable caught ) {
                  MessageDialogBox errBox =
                      new MessageDialogBox(
                          Messages.getString( "loginError" ), Messages.getString( "authFailed" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
                  errBox.setCallback( new IDialogCallback() {
                    public void cancelPressed() {
                    }

                    public void okPressed() {
                      outerCallback.onFailure( caught );
                    }
                  } );
                  errBox.show();
                }

                public void onResponseReceived( Request request, Response response ) {
                  if ( "true".equalsIgnoreCase( response.getText() ) ) {
                    long year = 1000 * 60 * 60 * 24 * 365;
                    // one year into the future
                    Date expirationDate = new Date( System.currentTimeMillis() + year );
                    Cookies.setCookie( "loginNewWindowChecked", "" + newWindowChk.getValue(), expirationDate ); //$NON-NLS-1$ //$NON-NLS-2$
                    outerCallback.onSuccess( newWindowChk != null && newWindowChk.getValue() );
                  } else {
                    outerCallback.onFailure( new Throwable( Messages.getString( "authFailed" ) ) ); //$NON-NLS-1$
                  }
                }

              } );
            } catch ( final RequestException e ) {
              MessageDialogBox errBox =
                  new MessageDialogBox(
                      Messages.getString( "loginError" ), Messages.getString( "authFailed" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
              errBox.setCallback( new IDialogCallback() {
                public void cancelPressed() {
                }

                public void okPressed() {
                  outerCallback.onFailure( e );
                }
              } );
              errBox.show();
            }
          }
        };
        try {
          String username = userTextBox.getText();
          String password = passwordTextBox.getText();
          builder
              .sendRequest(
                  "j_username=" + URL.encodeComponent( username ) + "&j_password=" + URL.encodeComponent( password ),
                  callback ); //$NON-NLS-1$ //$NON-NLS-2$
        } catch ( RequestException e ) {
          e.printStackTrace();
        }
      }

    };
    setCallback( myCallback );
    super.setStylePrimaryName( "pentaho-dialog" );
  }

  public MantleLoginDialog( AsyncCallback<Boolean> callback, boolean showNewWindowOption ) {
    this();
    setCallback( callback );
    setShowNewWindowOption( showNewWindowOption );
  }

  public void setShowNewWindowOption( boolean show ) {
    showNewWindowOption = show;
  }

  public static void performLogin( final AsyncCallback<Boolean> callback ) {
    // let's only login if we are not actually logged in
    try {
      final String url = GWT.getHostPageBaseURL() + "api/mantle/isAuthenticated"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      requestBuilder.setHeader( "accept", "text/plain" );
      requestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable caught ) {
          MantleLoginDialog dialog = new MantleLoginDialog( callback, false );
          dialog.show();
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( !"true".equalsIgnoreCase( response.getText() ) ) {
            MantleLoginDialog dialog = new MantleLoginDialog( callback, false );
            dialog.show();
          }
        }

      } );
    } catch ( RequestException e ) {
      MantleLoginDialog dialog = new MantleLoginDialog( callback, false );
      dialog.show();
    }
  }

  private Widget buildLoginPanel( boolean openInNewWindowDefault ) {
    userTextBox.setWidth( "100%" ); //$NON-NLS-1$
    passwordTextBox.setWidth( "100%" ); //$NON-NLS-1$
    usersListBox.setWidth( "100%" ); //$NON-NLS-1$

    userTextBox.setStyleName( "login-panel-label" );
    passwordTextBox.setStyleName( "login-panel-label" );
    newWindowChk.setStyleName( "login-panel-label" );

    VerticalPanel credentialsPanel = new VerticalPanel();

    credentialsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
    credentialsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
    SimplePanel spacer;

    Label usernameLabel = new Label( Messages.getString( "username" ) + ":" );
    usernameLabel.setStyleName( "login-panel-label" );

    credentialsPanel.add( usernameLabel ); //$NON-NLS-1$ //$NON-NLS-2$
    credentialsPanel.add( userTextBox );

    spacer = new SimplePanel();
    spacer.setHeight( "8px" ); //$NON-NLS-1$
    credentialsPanel.add( spacer );

    credentialsPanel.setCellHeight( spacer, "8px" ); //$NON-NLS-1$
    HTML passwordLabel = new HTML( Messages.getString( "password" ) + ":" );
    passwordLabel.setStyleName( "login-panel-label" );
    credentialsPanel.add( passwordLabel ); //$NON-NLS-1$ //$NON-NLS-2$
    credentialsPanel.add( passwordTextBox );

    boolean reallyShowNewWindowOption = showNewWindowOption;

    String showNewWindowOverride = Window.Location.getParameter( "showNewWindowOption" ); //$NON-NLS-1$
    if ( showNewWindowOverride != null && !"".equals( showNewWindowOverride ) ) { //$NON-NLS-1$
      // if the override is set, we MUST obey it above all else
      reallyShowNewWindowOption = "true".equals( showNewWindowOverride ); //$NON-NLS-1$
    } else if ( getReturnLocation() != null && !"".equals( getReturnLocation() ) ) { //$NON-NLS-1$
      StringTokenizer st = new StringTokenizer( getReturnLocation(), "?&" ); //$NON-NLS-1$
      // first token will be ignored, it is 'up to the ?'
      for ( int i = 1; i < st.countTokens(); i++ ) {
        StringTokenizer paramTokenizer = new StringTokenizer( st.tokenAt( i ), "=" ); //$NON-NLS-1$
        if ( paramTokenizer.countTokens() == 2 ) {
          // we've got a name=value token
          if ( paramTokenizer.tokenAt( 0 ).equalsIgnoreCase( "showNewWindowOption" ) ) { //$NON-NLS-1$
            reallyShowNewWindowOption = "true".equals( paramTokenizer.tokenAt( 1 ) ); //$NON-NLS-1$
            break;
          }
        }
      }
    }

    // New Window checkbox
    if ( reallyShowNewWindowOption ) {
      spacer = new SimplePanel();
      spacer.setHeight( "8px" ); //$NON-NLS-1$
      credentialsPanel.add( spacer );
      credentialsPanel.setCellHeight( spacer, "8px" ); //$NON-NLS-1$

      newWindowChk.setText( Messages.getString( "launchInNewWindow" ) ); //$NON-NLS-1$

      String cookieCheckedVal = Cookies.getCookie( "loginNewWindowChecked" ); //$NON-NLS-1$
      if ( cookieCheckedVal != null ) {
        newWindowChk.setValue( Boolean.parseBoolean( cookieCheckedVal ) );
      } else {
        // default is false, per BISERVER-2384
        newWindowChk.setValue( openInNewWindowDefault );
      }

      credentialsPanel.add( newWindowChk );
    }

    userTextBox.setTabIndex( 1 );
    passwordTextBox.setTabIndex( 2 );
    if ( reallyShowNewWindowOption ) {
      newWindowChk.setTabIndex( 3 );
    }
    passwordTextBox.setText( "" ); //$NON-NLS-1$

    setFocusWidget( userTextBox );

    Image lockImage = new Image( GWT.getModuleBaseURL() + "images/icon_login_lock.png" );
    HorizontalPanel loginPanel = new HorizontalPanel();
    loginPanel.setSpacing( 5 );
    loginPanel.setStyleName( "login-panel" );
    loginPanel.add( lockImage );
    loginPanel.add( credentialsPanel );

    return loginPanel;
  }

  public void setCallback( AsyncCallback<Boolean> callback ) {
    outerCallback = callback;
  }

  public String getReturnLocation() {
    return returnLocation;
  }

  public void setReturnLocation( String returnLocation ) {
    this.returnLocation = returnLocation;
    // the return location might have a parameter in the url to configure options,
    // so we must rebuild the UI if the return location is changed
    setContent( buildLoginPanel( false ) );
  }

}
