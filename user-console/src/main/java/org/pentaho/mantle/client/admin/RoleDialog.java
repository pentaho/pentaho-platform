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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.DialogBox;
import org.pentaho.gwt.widgets.client.panel.HorizontalFlexPanel;
import org.pentaho.gwt.widgets.client.panel.VerticalFlexPanel;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.ui.xul.gwt.tags.GwtDialog;

public class RoleDialog extends GwtDialog {

  private TextBox roleNameTextBox;
  private UserRolesAdminPanelController controller;
  private Button acceptBtn = new Button( Messages.getString( "ok" ) );
  private Button cancelBtn = new Button( Messages.getString( "cancel" ) );

  public RoleDialog( UserRolesAdminPanelController controller ) {
    setWidth( 260 );
    setHeight( 140 );
    getButtonPanel();
    setTitle( Messages.getString( "newRole" ) );

    acceptBtn.setEnabled( false );

    roleNameTextBox = new TextBox();
    roleNameTextBox.setWidth( "240px" );
    TextBoxValueChangeHandler textBoxChangeHandler = new TextBoxValueChangeHandler();
    roleNameTextBox.addKeyUpHandler( textBoxChangeHandler );

    acceptBtn.setStylePrimaryName( "pentaho-button" );
    acceptBtn.addClickHandler( new AcceptListener() );
    cancelBtn.setStylePrimaryName( "pentaho-button" );
    cancelBtn.addClickHandler( new CancelListener() );

    setResponsive( true );
    setWidthCategory( WidthCategory.EXTRASMALL );
    setMinimumHeightCategory( MinimumHeightCategory.CONTENT );

    this.controller = controller;
  }

  @Override
  protected DialogBox createManagedDialog() {
    DialogBox dialog = super.createManagedDialog();
    dialog.setStyleDependentName( "new-role", true );

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

    Label nameLabel = new Label( Messages.getString( "name" ) + ":" );
    vp.add( nameLabel );
    vp.add( roleNameTextBox );

    return hp;
  }

  class AcceptListener implements ClickHandler {
    public void onClick( ClickEvent event ) {
      String name = roleNameTextBox.getText();
      controller.saveRole( name );
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
      String name = roleNameTextBox.getText();
      boolean isEnabled = !StringUtils.isEmpty( name );
      acceptBtn.setEnabled( isEnabled );
    }
  }
}
