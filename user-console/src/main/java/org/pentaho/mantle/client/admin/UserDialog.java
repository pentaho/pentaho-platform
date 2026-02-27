/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.mantle.client.admin;

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
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.DialogBox;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.panel.HorizontalFlexPanel;
import org.pentaho.gwt.widgets.client.panel.VerticalFlexPanel;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.ui.xul.gwt.tags.GwtDialog;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;

import java.util.HashSet;
import java.util.Set;

public class UserDialog extends GwtDialog {

  private UserRolesAdminPanelController controller;
  private TextBox nameTextBox;
  private PasswordTextBox passwordTextBox;
  private PasswordTextBox reTypePasswordTextBox;
  private Button acceptBtn = new Button( Messages.getString( "ok" ) );
  private Button cancelBtn = new Button( Messages.getString( "cancel" ) );
  private static final String ALLOWED_CHARS = "^[a-zA-Z0-9_.,:;<>|!@#$%^&*()\\[\\]-]+$";
  private static final RegExp ALLOWED_CHARS_REGEXP = RegExp.compile( ALLOWED_CHARS );
  private static final String ALLOWED_CHARS_LIST = "a-z A-Z 0-9 _ . , : ; < > | ! @ # $ % ^ & * ( ) [ ] -";

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

    setResponsive( true );
    setWidthCategory( WidthCategory.EXTRASMALL );

    this.controller = controller;
  }

  @Override
  protected DialogBox createManagedDialog() {
    DialogBox dialog = super.createManagedDialog();
    dialog.setStyleDependentName( "new-user", true );

    return dialog;
  }

  public Panel getButtonPanel() {
    HorizontalPanel hp = new HorizontalFlexPanel();
    hp.add( acceptBtn );
    hp.setCellWidth( acceptBtn, "100%" );
    hp.setCellHorizontalAlignment( acceptBtn, HorizontalPanel.ALIGN_RIGHT );
    hp.add( cancelBtn );
    return hp;
  }

  public Panel getDialogContents() {

    HorizontalPanel hp = new HorizontalFlexPanel();
    SimplePanel hspacer = new SimplePanel();
    hspacer.setWidth( "10px" );
    hp.add( hspacer );

    VerticalPanel vp = new VerticalFlexPanel();
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

  private void showErrorMessage( String value, String reservedCharacters, String message ) {
    GwtMessageBox messageBox = new GwtMessageBox();
    messageBox.setTitle( Messages.getString( "error" ) );
    messageBox.setMessage( Messages.getString( message, value, reservedCharacters ) );
    messageBox.setButtons( new Object[ ACCEPT ] );
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

        if ( !isValidName( userName, reservedCharacters ) ) {
          showErrorMessage( userName, reservedCharacters, "prohibitedNameSymbols" );
          return;
        }

        if ( !isValidPassword( password ) ) {
          String nonMatchingChars = getNonMatchingCharacters( password );
          showErrorMessage( nonMatchingChars, ALLOWED_CHARS_LIST, "allowedNameCharacters" );
          return;
        }

        controller.saveUser( userName, password );
        hide();
      }

      private boolean isValidPassword( String password ) {
        return ALLOWED_CHARS_REGEXP.test( password );
      }

      private String getNonMatchingCharacters( String value ) {
        Set<Character> seen = new HashSet<>(); // Allows to identify unique non matching characters
        StringBuilder nonMatchingChars = new StringBuilder();

        for ( char c : value.toCharArray() ) {
          if ( !ALLOWED_CHARS_REGEXP.test( String.valueOf( c ) )
            && seen.add( c ) ) {
            if (nonMatchingChars.length() > 0) {
              nonMatchingChars.append(" ");
            }
            nonMatchingChars.append( c );
          }
        }
        return nonMatchingChars.toString();
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
