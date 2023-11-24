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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
        controller.updatePassword( getUsername(), newPassword, oldPassword, ChangePasswordByUserDialog.this );
      }
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
