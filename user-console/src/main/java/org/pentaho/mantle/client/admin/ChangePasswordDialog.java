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
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.ui.xul.gwt.tags.GwtDialog;

public class ChangePasswordDialog extends GwtDialog {

  private UpdatePasswordController controller;
  private PasswordTextBox newPasswordTextBox;
  private PasswordTextBox reTypePasswordTextBox;
  private Button acceptBtn = new Button( Messages.getString( "ok" ) );
  private Button cancelBtn = new Button( Messages.getString( "cancel" ) );

  public ChangePasswordDialog( UpdatePasswordController controller ) {
    setWidth( 260 );
    setHeight( 180 );
    getButtonPanel();
    setTitle( Messages.getString( "changePassword" ) );

    acceptBtn.setEnabled( false );
    newPasswordTextBox = new PasswordTextBox();
    newPasswordTextBox.setWidth( "240px" );
    reTypePasswordTextBox = new PasswordTextBox();
    reTypePasswordTextBox.setWidth( "240px" );

    TextBoxValueChangeHandler textBoxChangeHandler = new TextBoxValueChangeHandler();
    newPasswordTextBox.addKeyUpHandler( textBoxChangeHandler );
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

    Label nameLabel = new Label( Messages.getString( "newPassword" ) + ":" );
    vp.add( nameLabel );
    vp.add( newPasswordTextBox );

    Label passwordLabel = new Label( Messages.getString( "retypePassword" ) + ":" );
    vp.add( passwordLabel );
    vp.add( reTypePasswordTextBox );

    return hp;
  }

  class AcceptListener implements ClickHandler {
    public void onClick( ClickEvent event ) {
      String newPassword = newPasswordTextBox.getText();
      controller.updatePassword( newPassword );
      hide();
    }
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
      boolean isEnabled = !StringUtils.isEmpty( password ) && password.equals( reTypePassword );
      acceptBtn.setEnabled( isEnabled );
    }
  }
}
