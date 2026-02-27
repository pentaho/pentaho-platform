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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.DialogBox;
import org.pentaho.gwt.widgets.client.panel.HorizontalFlexPanel;
import org.pentaho.gwt.widgets.client.panel.VerticalFlexPanel;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.ui.xul.MantleController;
import org.pentaho.ui.xul.gwt.tags.GwtDialog;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;

import java.util.HashSet;
import java.util.Set;

//This rule is triggered when the class has more than 5 parents. in this case most of the parents are third party classes that can't be changed.
@SuppressWarnings( "squid:S110" )
public class ChangePasswordByUserDialog extends GwtDialog implements ServiceCallback {

  private MantleController controller;
  private PasswordTextBox newPasswordTextBox;
  private PasswordTextBox reTypePasswordTextBox;
  private PasswordTextBox oldPasswordTextBox;
  private Button acceptBtn = new Button( Messages.getString( "ok" ) );
  private Button cancelBtn = new Button( Messages.getString( "cancel" ) );
  private static final String TEXT_BOX_WIDTH = "260px";
  private static final String SPACER_STYLE_NAME = "spacer";
  private boolean acceptBtnEnabled = false;
  private static final String ALLOWED_CHARS = "^[a-zA-Z0-9_.,:;<>|!@#$%^&*()\\[\\]-]+$";
  private static final RegExp ALLOWED_CHARS_REGEXP = RegExp.compile( ALLOWED_CHARS );
  private static final String ALLOWED_CHARS_LIST = "a-z A-Z 0-9 _ . , : ; < > | ! @ # $ % ^ & * ( ) [ ] -";

  public ChangePasswordByUserDialog( MantleController controller ) {
    setWidth( 260 );
    setHeight( 240 );
    getButtonPanel();
    setTitle( Messages.getString( "changePassword" ) );

    disableAcceptBtn();
    newPasswordTextBox = new PasswordTextBox();
    newPasswordTextBox.setWidth( TEXT_BOX_WIDTH );
    reTypePasswordTextBox = new PasswordTextBox();
    reTypePasswordTextBox.setWidth( TEXT_BOX_WIDTH );
    oldPasswordTextBox = new PasswordTextBox();
    oldPasswordTextBox.setWidth( TEXT_BOX_WIDTH );

    TextBoxValueChangeHandler textBoxChangeHandler = new TextBoxValueChangeHandler();
    newPasswordTextBox.addKeyUpHandler( textBoxChangeHandler );
    reTypePasswordTextBox.addKeyUpHandler( textBoxChangeHandler );
    oldPasswordTextBox.addKeyUpHandler( textBoxChangeHandler );

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
    dialog.setStyleDependentName( "change-password", true );

    return dialog;
  }

  @Override
  public Panel getButtonPanel() {
    HorizontalFlexPanel hp = new HorizontalFlexPanel();
    hp.add( acceptBtn );
    hp.setCellWidth( acceptBtn, "100%" );
    hp.setCellHorizontalAlignment( acceptBtn, HorizontalPanel.ALIGN_RIGHT );
    hp.add( cancelBtn );
    return hp;
  }

  @Override
  public Panel getDialogContents() {

    VerticalPanel vp = new VerticalFlexPanel();

    Label oldPasswordLabel = new Label( Messages.getString( "oldPassword" ) + ":" );
    oldPasswordTextBox.setTitle( oldPasswordLabel.getText() );
    vp.add( oldPasswordLabel );
    vp.add( oldPasswordTextBox );
    addSpacer( vp, false );
    addSpacer( vp, true );
    Label newPasswordLabel = new Label( Messages.getString( "newPassword" ) + ":" );
    newPasswordTextBox.setTitle( newPasswordLabel.getText() );
    vp.add( newPasswordLabel );
    vp.add( newPasswordTextBox );
    addSpacer( vp, false );
    Label reTypePasswordLabel = new Label( Messages.getString( "retypePassword" ) + ":" );
    reTypePasswordTextBox.setTitle( reTypePasswordLabel.getText() );
    vp.add( reTypePasswordLabel );
    vp.add( reTypePasswordTextBox );

    return vp;
  }

  private void addSpacer( VerticalPanel vp, boolean addLineSeparator ) {
    SimplePanel separatorSpacer = new SimplePanel();
    separatorSpacer.setStylePrimaryName( SPACER_STYLE_NAME );
    if ( addLineSeparator ) {
      separatorSpacer.addStyleDependentName( "border-top" );
    }
    vp.add( separatorSpacer );
  }

  @Override
  public void serviceResult( boolean ok ) {
    if ( ok ) {
      this.hide();
    } else {
      newPasswordTextBox.setText( "" );
      reTypePasswordTextBox.setText( "" );
      oldPasswordTextBox.setText( "" );
      cancelBtn.setEnabled( true );
    }
  }

  private void enableAcceptBtn() {
    acceptBtn.removeStyleName( "disabled" );
    acceptBtnEnabled = true;
  }

  private void disableAcceptBtn() {
    acceptBtn.addStyleName( "disabled" );
    acceptBtnEnabled = false;
  }

  class AcceptListener implements ClickHandler {
    public void onClick( ClickEvent event ) {
      if ( acceptBtnEnabled ) {
        disableAcceptBtn();
        String newPassword = newPasswordTextBox.getText();
        String oldPassword = oldPasswordTextBox.getText();

        if ( !isValidPassword( newPassword ) ) {
          String nonMatchingChars = getNonMatchingCharacters( newPassword );
          showErrorMessage( nonMatchingChars, ALLOWED_CHARS_LIST );
          return;
        }

        controller.updatePassword( getUsername(), newPassword, oldPassword, ChangePasswordByUserDialog.this );
      }
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

    private void showErrorMessage( String value, String allowedCharacters ) {
      GwtMessageBox messageBox = new GwtMessageBox();
      messageBox.setTitle( Messages.getString( "error" ) );
      messageBox.setMessage( Messages.getString( "allowedNameCharacters", value, allowedCharacters ) );
      messageBox.setButtons( new Object[ ACCEPT ] );
      messageBox.setWidth( 300 );
      messageBox.show();
    }

    private native String getUsername()
    /*-{
      return window.parent.SESSION_NAME;
    }-*/;
  }

  class CancelListener implements ClickHandler {
    public void onClick( ClickEvent event ) {
      hide();
    }
  }

  class TextBoxValueChangeHandler implements KeyUpHandler {
    public void onKeyUp( KeyUpEvent evt ) {
      String password = newPasswordTextBox.getText();
      String reTypePassword = reTypePasswordTextBox.getText();
      String administratorPassword = oldPasswordTextBox.getText();
      boolean isEnabled = !StringUtils.isEmpty( administratorPassword ) && !StringUtils.isEmpty( password ) && password.equals( reTypePassword );
      if ( isEnabled ) {
        enableAcceptBtn();
      } else {
        disableAcceptBtn();
      }
    }
  }
}
