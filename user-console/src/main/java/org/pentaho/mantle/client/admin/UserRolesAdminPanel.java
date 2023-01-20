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

import com.google.gwt.aria.client.Roles;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.buttons.ThemeableImageButton;
import org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.ui.custom.HorizontalScrollWrapper;
import org.pentaho.mantle.client.ui.custom.ListBoxTitle;

public class UserRolesAdminPanel extends SimplePanel {

  protected PasswordTextBox userPasswordTextBox;
  protected ListBox rolesListBox;
  protected ListBox systemRolesListBox;
  protected ListBox usersListBox;
  protected ListBox selectedRolesListBox;
  protected ListBox selectedMembersListBox;
  protected ListBox availableMembersListBox;
  protected ListBox availableRolesListBox;
  protected ThemeableImageButton addUserButton;
  protected ThemeableImageButton removeUserButton;
  protected ThemeableImageButton addAllUsersButton;
  protected ThemeableImageButton removeAllUsersButton;
  protected ThemeableImageButton addRoleButton;
  protected ThemeableImageButton removeRoleButton;
  protected ThemeableImageButton addAllRolesButton;
  protected ThemeableImageButton removeAllRolesButton;
  protected ThemeableImageButton newRoleButton;
  protected ThemeableImageButton deleteRoleButton;
  protected ThemeableImageButton newUserButton;
  protected ThemeableImageButton deleteUserButton;
  protected Button editPasswordButton;
  protected PermissionsPanel rolesPermissionsPanel;
  protected PermissionsPanel systemRolesPermissionsPanel;
  protected PentahoTabPanel mainTabPanel;
  protected HorizontalPanel usersPanel;
  protected VerticalPanel usersLabelPanel;

  private static final String[] addButtonStyles = new String[] { "pentaho-addbutton" };
  private static final String[] removeButtonStyles = new String[] { "pentaho-deletebutton" };

  private static final String[] accumAddButtonStyles = new String[] { "icon-small", "icon-accum-add" };
  private static final String[] accumAddAllButtonStyles = new String[] { "icon-small", "icon-accum-add-all" };
  private static final String[] accumRemoveButtonStyles = new String[] { "icon-small", "icon-accum-remove" };
  private static final String[] accumRemoveAllButtonStyles = new String[] { "icon-small", "icon-accum-remove-all" };

  private static UserRolesAdminPanel instance = new UserRolesAdminPanel();

  public static UserRolesAdminPanel getInstance() {
    return instance;
  }

  protected UserRolesAdminPanel() {
    VerticalPanel mainPanel = new VerticalPanel();
    Label usersRolesLabel = new Label( Messages.getString( "users" ) + " / " + Messages.getString( "roles" ) );
    usersRolesLabel.setStyleName( "pentaho-fieldgroup-major" );
    mainPanel.add( usersRolesLabel );

    SimplePanel vSpacer = new SimplePanel();
    vSpacer.setHeight( "20px" );
    mainPanel.add( vSpacer );

    mainTabPanel = new PentahoTabPanel();
    mainTabPanel.addTab( Messages.getString( "manageUsers" ), "", false, createUsersPanel() );
    mainTabPanel.addTab( Messages.getString( "manageRoles" ), "", false, createRolesPanel() );
    mainTabPanel.addTab( Messages.getString( "systemRoles" ), "", false, createSystemRolesPanel() );
    mainPanel.add( mainTabPanel );
    setWidget( mainPanel );
  }

  private Widget createUsersPanel() {
    HorizontalPanel mainUsersPanel = new HorizontalPanel();
    mainUsersPanel.getElement().setId( "admin-users-panel" );
    Roles.getTabpanelRole().set( mainUsersPanel.getElement() );

    // mainUsersPanel.setWidth("376px");
    SimplePanel hSpacer = new SimplePanel();
    hSpacer.setWidth( "15px" );
    mainUsersPanel.add( hSpacer );

    VerticalPanel availablePanel = new VerticalPanel();
    mainUsersPanel.add( availablePanel );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "15px" );
    availablePanel.add( hSpacer );

    HorizontalPanel labelAndButtonsPanel = new HorizontalPanel();
    labelAndButtonsPanel.setWidth( "100%" );
    availablePanel.add( labelAndButtonsPanel );
    Label usersColonLabel = new Label( Messages.getString( "usersColon" ) );
    labelAndButtonsPanel.add( usersColonLabel );
    labelAndButtonsPanel.setCellWidth( usersColonLabel, "100%" );
    hSpacer = new SimplePanel();
    hSpacer.setWidth( "100%" );
    labelAndButtonsPanel.add( hSpacer );
    labelAndButtonsPanel.setCellWidth( hSpacer, "100%" );
    newUserButton = new ThemeableImageButton( addButtonStyles, null, Messages.getString( "new" ) );
    labelAndButtonsPanel.add( newUserButton );
    hSpacer = new SimplePanel();
    hSpacer.setWidth( "7px" );
    labelAndButtonsPanel.add( hSpacer );
    deleteUserButton = new ThemeableImageButton( removeButtonStyles, null, Messages.getString( "delete" ) );
    labelAndButtonsPanel.add( deleteUserButton );

    usersListBox = new ListBoxTitle( true );
    usersListBox.addStyleName( "users-roles-list" );
    HorizontalScrollWrapper usersListBoxWrapper = new HorizontalScrollWrapper( usersListBox );
    availablePanel.add( usersListBoxWrapper );
    availablePanel.setCellHeight( usersListBoxWrapper, "100%" );
    usersListBox.setVisibleItemCount( 20 );

    hSpacer = new SimplePanel();
    hSpacer.setWidth( "24px" );
    mainUsersPanel.add( hSpacer );

    VerticalPanel detailsPanel = new VerticalPanel();
    mainUsersPanel.add( detailsPanel );
    mainUsersPanel.setCellWidth( detailsPanel, "100%" );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "32px" );
    detailsPanel.add( hSpacer );

    detailsPanel.add( new Label( Messages.getString( "password" ) + ":" ) );

    userPasswordTextBox = new PasswordTextBox();
    userPasswordTextBox.setEnabled( false );
    userPasswordTextBox.setWidth( "200px" );
    HorizontalPanel passwordPanel = new HorizontalPanel();
    passwordPanel.add( userPasswordTextBox );
    hSpacer = new SimplePanel();
    hSpacer.setWidth( "10px" );
    passwordPanel.add( hSpacer );
    editPasswordButton = new Button( Messages.getString( "edit" ) + "..." );
    editPasswordButton.setStylePrimaryName( "pentaho-button" );
    editPasswordButton.setEnabled( false );
    passwordPanel.add( editPasswordButton );
    detailsPanel.add( passwordPanel );

    hSpacer = new SimplePanel();
    hSpacer.setHeight( "15px" );
    detailsPanel.add( hSpacer );

    Label roleLabel = new Label( Messages.getString( "roles" ) );
    roleLabel.setStyleName( "pentaho-fieldgroup-minor" );
    detailsPanel.add( roleLabel );

    hSpacer = new SimplePanel();
    hSpacer.setHeight( "15px" );
    detailsPanel.add( hSpacer );

    HorizontalPanel groupsPanel = new HorizontalPanel();
    groupsPanel.getElement().setId( "groups-panel" );
    detailsPanel.add( groupsPanel );

    VerticalPanel availableRolesPanel = new VerticalPanel();
    groupsPanel.add( availableRolesPanel );
    availableRolesPanel.add( new Label( Messages.getString( "available" ) + ":" ) );
    availableRolesListBox = new ListBoxTitle( true );
    HorizontalScrollWrapper availableRolesListBoxWrapper = new HorizontalScrollWrapper( availableRolesListBox );
    availableRolesPanel.add( availableRolesListBoxWrapper );
    availableRolesListBox.setVisibleItemCount( 20 );
    availableRolesListBox.addStyleName( "users-roles-selection-list" );

    VerticalPanel vSpacer = new VerticalPanel();
    vSpacer.setWidth( "15px" );
    groupsPanel.add( vSpacer );

    VerticalPanel arrowsPanel = new VerticalPanel();
    groupsPanel.add( arrowsPanel );
    arrowsPanel.setWidth( "35px" );

    hSpacer = new SimplePanel();
    hSpacer.setHeight( "110px" );
    arrowsPanel.add( hSpacer );

    addRoleButton = new ThemeableImageButton( accumAddButtonStyles, null, null );
    arrowsPanel.add( addRoleButton );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "10px" );
    arrowsPanel.add( hSpacer );

    removeRoleButton = new ThemeableImageButton( accumRemoveButtonStyles, null, null );
    arrowsPanel.add( removeRoleButton );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "30px" );
    arrowsPanel.add( hSpacer );

    addAllRolesButton = new ThemeableImageButton( accumAddAllButtonStyles, null, null );
    arrowsPanel.add( addAllRolesButton );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "10px" );
    arrowsPanel.add( hSpacer );

    removeAllRolesButton = new ThemeableImageButton( accumRemoveAllButtonStyles, null, null );
    arrowsPanel.add( removeAllRolesButton );

    VerticalPanel selectedRolesPanel = new VerticalPanel();
    groupsPanel.add( selectedRolesPanel );
    selectedRolesPanel.add( new Label( Messages.getString( "selected" ) + ":" ) );
    selectedRolesListBox = new ListBoxTitle( true );
    HorizontalScrollWrapper selectedRolesListBoxWrapper = new HorizontalScrollWrapper( selectedRolesListBox );
    selectedRolesPanel.add( selectedRolesListBoxWrapper );
    selectedRolesListBox.setVisibleItemCount( 20 );
    selectedRolesListBox.addStyleName( "users-roles-selection-list" );

    return mainUsersPanel;
  }

  private Widget createSystemRolesPanel() {
    HorizontalPanel mainSystemRolesPanel = new HorizontalPanel();
    Roles.getTabpanelRole().set( mainSystemRolesPanel.getElement() );
    mainSystemRolesPanel.getElement().setId( "admin-system-roles-panel" );
    SimplePanel hSpacer = new SimplePanel();
    hSpacer.setWidth( "15px" );
    mainSystemRolesPanel.add( hSpacer );

    VerticalPanel availablePanel = new VerticalPanel();
    mainSystemRolesPanel.add( availablePanel );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "15px" );
    availablePanel.add( hSpacer );

    HorizontalPanel labelAndButtonsPanel = new HorizontalPanel();
    availablePanel.add( labelAndButtonsPanel );
    labelAndButtonsPanel.add( new Label( Messages.getString( "rolesColon" ) ) );
    labelAndButtonsPanel.setStyleName( "pentaho-fieldgroup-minor" );

    systemRolesListBox = new ListBoxTitle( true );
    HorizontalScrollWrapper systemRolesListBoxWrapper = new HorizontalScrollWrapper( systemRolesListBox );
    availablePanel.add( systemRolesListBoxWrapper );
    availablePanel.setCellHeight( systemRolesListBoxWrapper, "100%" );
    systemRolesListBox.setVisibleItemCount( 20 );
    systemRolesListBox.addStyleName( "users-roles-list" );

    hSpacer = new SimplePanel();
    hSpacer.setWidth( "24px" );
    mainSystemRolesPanel.add( hSpacer );

    VerticalPanel detailsPanel = new VerticalPanel();
    detailsPanel.getElement().setId( "details-panel" );
    mainSystemRolesPanel.add( detailsPanel );
    mainSystemRolesPanel.setCellWidth( detailsPanel, "100%" );


    hSpacer = new SimplePanel();
    hSpacer.setHeight( "15px" );
    detailsPanel.add( hSpacer );

    //todo check if replacement required
    systemRolesPermissionsPanel = new PermissionsPanel( systemRolesListBox );
    detailsPanel.add( systemRolesPermissionsPanel );
    detailsPanel.setCellHeight( systemRolesPermissionsPanel, "100%" );

    return mainSystemRolesPanel;
  }

  private Widget createRolesPanel() {

    HorizontalPanel mainRolesPanel = new HorizontalPanel();
    Roles.getTabpanelRole().set( mainRolesPanel.getElement() );
    mainRolesPanel.getElement().setId( "admin-roles-panel" );
    SimplePanel hSpacer = new SimplePanel();
    hSpacer.setWidth( "15px" );
    mainRolesPanel.add( hSpacer );

    VerticalPanel availablePanel = new VerticalPanel();
    mainRolesPanel.add( availablePanel );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "15px" );
    availablePanel.add( hSpacer );

    HorizontalPanel labelAndButtonsPanel = new HorizontalPanel();
    labelAndButtonsPanel.setWidth( "100%" );
    availablePanel.add( labelAndButtonsPanel );
    Label rolesLabel = new Label( Messages.getString( "roles" ) );
    labelAndButtonsPanel.add( rolesLabel );
    labelAndButtonsPanel.setCellWidth( rolesLabel, "100%" );
    hSpacer = new SimplePanel();
    hSpacer.setWidth( "100%" );
    labelAndButtonsPanel.add( hSpacer );
    labelAndButtonsPanel.setCellWidth( hSpacer, "100%" );
    newRoleButton = new ThemeableImageButton( addButtonStyles, null, Messages.getString( "new" ) );
    labelAndButtonsPanel.add( newRoleButton );
    hSpacer = new SimplePanel();
    hSpacer.setWidth( "7px" );
    labelAndButtonsPanel.add( hSpacer );
    deleteRoleButton = new ThemeableImageButton( removeButtonStyles, null, Messages.getString( "delete" ) );
    labelAndButtonsPanel.add( deleteRoleButton );

    rolesListBox = new ListBoxTitle( true );
    HorizontalScrollWrapper rolesListBoxWrapper = new HorizontalScrollWrapper( rolesListBox );
    availablePanel.add( rolesListBoxWrapper );
    availablePanel.setCellHeight( rolesListBoxWrapper, "100%" );
    rolesListBox.setVisibleItemCount( 20 );
    rolesListBox.addStyleName( "users-roles-list" );

    hSpacer = new SimplePanel();
    hSpacer.setWidth( "24px" );
    mainRolesPanel.add( hSpacer );

    VerticalPanel detailsPanel = new VerticalPanel();
    mainRolesPanel.add( detailsPanel );
    mainRolesPanel.setCellWidth( detailsPanel, "100%" );

    hSpacer = new SimplePanel();
    hSpacer.setHeight( "15px" );
    detailsPanel.add( hSpacer );

    //todo check if replacement required
    rolesPermissionsPanel = new PermissionsPanel( rolesListBox );
    detailsPanel.add( rolesPermissionsPanel );

    usersLabelPanel = new VerticalPanel();
    Label usersLabel = new Label( Messages.getString( "users" ) );
    usersLabel.setStyleName( "pentaho-fieldgroup-minor" );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "15px" );
    usersLabelPanel.add( hSpacer );
    usersLabelPanel.add( usersLabel );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "15px" );
    usersLabelPanel.add( hSpacer );
    detailsPanel.add( usersLabelPanel );

    usersPanel = new HorizontalPanel();
    usersPanel.getElement().setId( "users-panel" );
    detailsPanel.add( usersPanel );

    VerticalPanel availableMembersPanel = new VerticalPanel();
    usersPanel.add( availableMembersPanel );

    availableMembersPanel.add( new Label( Messages.getString( "available" ) + ":" ) );
    availableMembersListBox = new ListBoxTitle( true );
    HorizontalScrollWrapper availableMembersListBoxWrapper = new HorizontalScrollWrapper( availableMembersListBox );
    availableMembersPanel.add( availableMembersListBoxWrapper );
    availableMembersListBox.setVisibleItemCount( 20 );
    availableMembersListBox.addStyleName( "users-roles-selection-list" );

    VerticalPanel vSpacer = new VerticalPanel();
    vSpacer.setWidth( "15px" );
    usersPanel.add( vSpacer );

    VerticalPanel arrowsPanel = new VerticalPanel();
    usersPanel.add( arrowsPanel );
    arrowsPanel.setWidth( "35px" );

    hSpacer = new SimplePanel();
    hSpacer.setHeight( "80px" );
    arrowsPanel.add( hSpacer );

    addUserButton = new ThemeableImageButton( accumAddButtonStyles, null, null );
    arrowsPanel.add( addUserButton );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "10px" );
    arrowsPanel.add( hSpacer );

    removeUserButton = new ThemeableImageButton( accumRemoveButtonStyles, null, null );
    arrowsPanel.add( removeUserButton );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "30px" );
    arrowsPanel.add( hSpacer );

    addAllUsersButton = new ThemeableImageButton( accumAddAllButtonStyles, null, null );
    arrowsPanel.add( addAllUsersButton );
    hSpacer = new SimplePanel();
    hSpacer.setHeight( "10px" );
    arrowsPanel.add( hSpacer );

    removeAllUsersButton = new ThemeableImageButton( accumRemoveAllButtonStyles, null, null );
    arrowsPanel.add( removeAllUsersButton );

    VerticalPanel selectedMembersPanel = new VerticalPanel();
    usersPanel.add( selectedMembersPanel );
    selectedMembersPanel.add( new Label( Messages.getString( "selected" ) + ":" ) );
    selectedMembersListBox = new ListBoxTitle( true );
    HorizontalScrollWrapper selectedMembersListBoxWrapper = new HorizontalScrollWrapper( selectedMembersListBox );
    selectedMembersPanel.add( selectedMembersListBoxWrapper );
    selectedMembersListBox.setVisibleItemCount( 20 );
    selectedMembersListBox.addStyleName( "users-roles-selection-list" );

    return mainRolesPanel;
  }
}
