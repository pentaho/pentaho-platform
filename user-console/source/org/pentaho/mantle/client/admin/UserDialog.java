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

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.ui.xul.gwt.tags.GwtDialog;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UserDialog extends GwtDialog {

  private UserRolesAdminPanelController controller;
  private TextBox nameTextBox;
  private PasswordTextBox passwordTextBox;
  private PasswordTextBox reTypePasswordTextBox;
  private Button acceptBtn = new Button( Messages.getString( "ok" ) );
  private Button cancelBtn = new Button( Messages.getString( "cancel" ) );

  public UserDialog( UserRolesAdminPanelController controller ) {
    setWidth( 260 );
    setHeight( 220 );
    getButtonPanel();
    setTitle( Messages.getString( "newUser" ) );

    acceptBtn.setEnabled( false );
    nameTextBox = new TextBox();
    nameTextBox.setWidth( "240px" );
    passwordTextBox = new PasswordTextBox();
    passwordTextBox.setWidth( "240px" );
    reTypePasswordTextBox = new PasswordTextBox();
    reTypePasswordTextBox.setWidth( "240px" );

    TextBoxValueChangeHandler textBoxChangeHandler = new TextBoxValueChangeHandler();
    nameTextBox.addKeyUpHandler( textBoxChangeHandler );
    passwordTextBox.addKeyUpHandler( textBoxChangeHandler );
    reTypePasswordTextBox.addKeyUpHandler( textBoxChangeHandler );

    acceptBtn.setStylePrimaryName( "pentaho-button" );
    acceptBtn.addClickHandler( new AcceptListener() );
    cancelBtn.setStylePrimaryName( "pentaho-button" );
    cancelBtn.addClickHandler( new CancelListener() );

    this.controller = controller;
  }

  public Panel getButtonPanel() {
    HorizontalPanel hp = new HorizontalPanel();
    hp.add( acceptBtn );
    hp.setCellWidth( acceptBtn, "100%" );
    hp.setCellHorizontalAlignment( acceptBtn, HorizontalPanel.ALIGN_RIGHT );
    hp.add( cancelBtn );
    return hp;
  }

  public Panel getDialogContents() {

    HorizontalPanel hp = new HorizontalPanel();
    SimplePanel hspacer = new SimplePanel();
    hspacer.setWidth( "10px" );
    hp.add( hspacer );

    VerticalPanel vp = new VerticalPanel();
    hp.add( vp );

    SimplePanel vspacer = new SimplePanel();
    vspacer.setHeight( "10px" );
    vp.add( vspacer );

    Label nameLabel = new Label( Messages.getString( "userName" ) + ":" );
    vp.add( nameLabel );
    vp.add( nameTextBox );

    Label passwordLabel = new Label( Messages.getString( "password" ) + ":" );
    vp.add( passwordLabel );
    vp.add( passwordTextBox );

    Label reTypePasswordLabel = new Label( Messages.getString( "retypePassword" ) + ":" );
    vp.add( reTypePasswordLabel );
    vp.add( reTypePasswordTextBox );

    return hp;
  }

  private boolean isValidName( String name, String reservedSymbols ) {
    return !StringUtils.containsAnyChars( name, reservedSymbols );
  }

  private void showErrorMessage( String userName, String reservedCharacters ) {
    GwtMessageBox messageBox = new GwtMessageBox();
    messageBox.setTitle( Messages.getString( "error" ) );
    messageBox.setMessage( Messages.getString( "prohibitedNameSymbols", userName, reservedCharacters ) );
    messageBox.setButtons( new Object[GwtMessageBox.ACCEPT] );
    messageBox.setWidth( 300 );
    messageBox.show();
  }

  private void performSave() throws RequestException {
    String url = GWT.getHostPageBaseURL() + "api/repo/files/reservedCharacters";
    RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    requestBuilder.sendRequest( "", new RequestCallback() {

      @Override
      public void onResponseReceived( Request request, Response response ) {
        String userName = nameTextBox.getText();
        String password = passwordTextBox.getText();
        String reservedCharacters = response.getText();

        if ( isValidName( userName, reservedCharacters ) ) {
          controller.saveUser( userName, password );
          hide();
        } else {
          showErrorMessage( userName, reservedCharacters );
        }
      }

      @Override
      public void onError( Request request, Throwable exception ) {
        hide();
      }

    } );
  }

  class AcceptListener implements ClickHandler {
    public void onClick( ClickEvent event ) {
      try {
        performSave();
      } catch ( RequestException e ) {
        MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), e.toString(), //$NON-NLS-1$
            false, false, true );
        dialogBox.center();
      }
    }
  }

  class CancelListener implements ClickHandler {
    public void onClick( ClickEvent event ) {
      hide();
    }
  }

  class TextBoxValueChangeHandler implements KeyUpHandler {
    public void onKeyUp( KeyUpEvent evt ) {
      String name = nameTextBox.getText();
      String password = passwordTextBox.getText();
      String reTypePassword = reTypePasswordTextBox.getText();
      boolean isEnabled =
          !StringUtils.isEmpty( name ) && !StringUtils.isEmpty( password ) && password.equals( reTypePassword );
      acceptBtn.setEnabled( isEnabled );
    }
  }
}
