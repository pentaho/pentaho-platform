/*
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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 03, 2012
 * @author Ezequiel Cuellar
 */

package org.pentaho.mantle.client.admin;

import org.pentaho.gwt.widgets.client.buttons.ImageButton;
import org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UserRolesAdminPanel extends SimplePanel {

	private String moduleBaseURL = GWT.getModuleBaseURL();
	protected PasswordTextBox userPasswordTextBox;
	protected ListBox rolesListBox;
	protected ListBox systemRolesListBox;
	protected ListBox usersListBox;
	protected ListBox selectedRolesListBox;
	protected ListBox selectedMembersListBox;
	protected ListBox availableMembersListBox;
	protected ListBox availableRolesListBox;
	protected ImageButton addUserButton;
	protected ImageButton removeUserButton;
	protected ImageButton addAllUsersButton;
	protected ImageButton removeAllUsersButton;
	protected ImageButton addRoleButton;
	protected ImageButton removeRoleButton;
	protected ImageButton addAllRolesButton;
	protected ImageButton removeAllRolesButton;
	protected ImageButton newRoleButton;
	protected ImageButton deleteRoleButton;
	protected ImageButton newUserButton;
	protected ImageButton deleteUserButton;
	protected Button editPasswordButton;
	protected PermissionsPanel rolesPermissionsPanel;
	protected PermissionsPanel systemRolesPermissionsPanel;
	protected PentahoTabPanel mainTabPanel;
	protected HorizontalPanel usersPanel;
	protected VerticalPanel usersLabelPanel;
	
	private static UserRolesAdminPanel instance = new UserRolesAdminPanel();

	public static UserRolesAdminPanel getInstance() {
		return instance;
	}

	protected UserRolesAdminPanel() {
		VerticalPanel mainPanel = new VerticalPanel();
		Label usersRolesLabel = new Label(Messages.getString("users") + " / " + Messages.getString("roles"));
		usersRolesLabel.setStyleName("pentaho-fieldgroup-major");
		mainPanel.add(usersRolesLabel);

		SimplePanel vSpacer = new SimplePanel();
    vSpacer.setHeight("20px");
    mainPanel.add(vSpacer);
		
		mainTabPanel = new PentahoTabPanel();
		mainTabPanel.setWidth("715px");
		mainTabPanel.setHeight("510px");
		mainTabPanel.addTab(Messages.getString("manageUsers"), "", false, createUsersPanel());
		mainTabPanel.addTab(Messages.getString("manageRoles"), "", false, createRolesPanel());
		mainTabPanel.addTab(Messages.getString("systemRoles"), "", false, createSystemRolesPanel());
    mainPanel.add(mainTabPanel);
		setWidget(mainPanel);
	}

	private Widget createUsersPanel() {
		HorizontalPanel mainUsersPanel = new HorizontalPanel();
		//mainUsersPanel.setWidth("376px");
		SimplePanel hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		mainUsersPanel.add(hSpacer);

		VerticalPanel availablePanel = new VerticalPanel();
		mainUsersPanel.add(availablePanel);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		availablePanel.add(hSpacer);

		HorizontalPanel labelAndButtonsPanel = new HorizontalPanel();
		labelAndButtonsPanel.setWidth("100%");
		availablePanel.add(labelAndButtonsPanel);
		labelAndButtonsPanel.add(new Label(Messages.getString("usersColon")));
    hSpacer = new SimplePanel();
    hSpacer.setWidth("100%");
    labelAndButtonsPanel.add(hSpacer);
    labelAndButtonsPanel.setCellWidth(hSpacer, "100%");
		newUserButton = new ImageButton(moduleBaseURL + "images/add_icon.png", "", "", 15, 15);
		labelAndButtonsPanel.add(newUserButton);
		hSpacer = new SimplePanel();
		hSpacer.setWidth("7px");
		labelAndButtonsPanel.add(hSpacer);
		deleteUserButton = new ImageButton(moduleBaseURL + "images/remove_icon.png", "", "", 16, 16);
		labelAndButtonsPanel.add(deleteUserButton);

		usersListBox = new ListBox(true);
		availablePanel.add(usersListBox);
		usersListBox.setVisibleItemCount(20);
		usersListBox.setWidth("200px");
		usersListBox.setHeight("432px");

		hSpacer = new SimplePanel();
		hSpacer.setWidth("24px");
		mainUsersPanel.add(hSpacer);

		VerticalPanel detailsPanel = new VerticalPanel();
		mainUsersPanel.add(detailsPanel);
		mainUsersPanel.setCellWidth(detailsPanel, "100%");
		hSpacer = new SimplePanel();
		hSpacer.setHeight("32px");
		detailsPanel.add(hSpacer);

		detailsPanel.add(new Label(Messages.getString("password") + ":"));

		userPasswordTextBox = new PasswordTextBox();
		userPasswordTextBox.setEnabled(false);
		userPasswordTextBox.setWidth("200px");
		HorizontalPanel passwordPanel = new HorizontalPanel();
		passwordPanel.add(userPasswordTextBox);
		hSpacer = new SimplePanel();
		hSpacer.setWidth("10px");
		passwordPanel.add(hSpacer);
		editPasswordButton = new Button(Messages.getString("edit") + "...");
		editPasswordButton.setStylePrimaryName("pentaho-button");
		editPasswordButton.setEnabled(false);
		passwordPanel.add(editPasswordButton);
		detailsPanel.add(passwordPanel);

		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		detailsPanel.add(hSpacer);

		Label roleLabel = new Label(Messages.getString("roles"));
		roleLabel.setStyleName("pentaho-fieldgroup-minor");
		detailsPanel.add(roleLabel);

		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		detailsPanel.add(hSpacer);

		HorizontalPanel groupsPanel = new HorizontalPanel();
		detailsPanel.add(groupsPanel);

		VerticalPanel availableRolesPanel = new VerticalPanel();
		groupsPanel.add(availableRolesPanel);
		availableRolesPanel.add(new Label(Messages.getString("available") + ":"));
		availableRolesListBox = new ListBox(true);
		availableRolesPanel.add(availableRolesListBox);
		availableRolesListBox.setVisibleItemCount(20);
		availableRolesListBox.setWidth("200px");
		availableRolesListBox.setHeight("324px");

		VerticalPanel vSpacer = new VerticalPanel();
		vSpacer.setWidth("15px");
		groupsPanel.add(vSpacer);

		VerticalPanel arrowsPanel = new VerticalPanel();
		groupsPanel.add(arrowsPanel);
		arrowsPanel.setWidth("35px");

		hSpacer = new SimplePanel();
		hSpacer.setHeight("110px");
		arrowsPanel.add(hSpacer);

		addRoleButton = new ImageButton(moduleBaseURL + "images/accum_add.png", "", "", 14, 13);
		arrowsPanel.add(addRoleButton);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("10px");
		arrowsPanel.add(hSpacer);

		removeRoleButton = new ImageButton(moduleBaseURL + "images/accum_remove.png", "", "", 14, 13);
		arrowsPanel.add(removeRoleButton);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("30px");
		arrowsPanel.add(hSpacer);

		addAllRolesButton = new ImageButton(moduleBaseURL + "images/accum_add_all.png", "", "", 14, 13);
		arrowsPanel.add(addAllRolesButton);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("10px");
		arrowsPanel.add(hSpacer);

		removeAllRolesButton = new ImageButton(moduleBaseURL + "images/accum_remove_all.png", "", "", 14, 13);
		arrowsPanel.add(removeAllRolesButton);

		VerticalPanel selectedRolesPanel = new VerticalPanel();
		groupsPanel.add(selectedRolesPanel);
		selectedRolesPanel.add(new Label(Messages.getString("selected") + ":"));
		selectedRolesListBox = new ListBox(true);
		selectedRolesPanel.add(selectedRolesListBox);
		selectedRolesListBox.setVisibleItemCount(20);
		selectedRolesListBox.setWidth("200px");
		selectedRolesListBox.setHeight("324px");

		return mainUsersPanel;
	}
	
	private Widget createSystemRolesPanel() {
		HorizontalPanel mainSystemRolesPanel = new HorizontalPanel();
		SimplePanel hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		mainSystemRolesPanel.add(hSpacer);

		VerticalPanel availablePanel = new VerticalPanel();
		mainSystemRolesPanel.add(availablePanel);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		availablePanel.add(hSpacer);

		HorizontalPanel labelAndButtonsPanel = new HorizontalPanel();
		availablePanel.add(labelAndButtonsPanel);
		labelAndButtonsPanel.add(new Label(Messages.getString("rolesColon")));

		systemRolesListBox = new ListBox(true);
		availablePanel.add(systemRolesListBox);
		systemRolesListBox.setVisibleItemCount(20);
		systemRolesListBox.setWidth("200px");
		systemRolesListBox.setHeight("435px");

		hSpacer = new SimplePanel();
		hSpacer.setWidth("24px");
		mainSystemRolesPanel.add(hSpacer);

		VerticalPanel detailsPanel = new VerticalPanel();
		mainSystemRolesPanel.add(detailsPanel);
		mainSystemRolesPanel.setCellWidth(detailsPanel, "100%");
		
		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		detailsPanel.add(hSpacer);
		
		systemRolesPermissionsPanel = new PermissionsPanel(systemRolesListBox);
		detailsPanel.add(systemRolesPermissionsPanel);
		
		return mainSystemRolesPanel;
	}

	private Widget createRolesPanel() {

		HorizontalPanel mainRolesPanel = new HorizontalPanel();
		SimplePanel hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		mainRolesPanel.add(hSpacer);

		VerticalPanel availablePanel = new VerticalPanel();
		mainRolesPanel.add(availablePanel);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		availablePanel.add(hSpacer);

		HorizontalPanel labelAndButtonsPanel = new HorizontalPanel();
		labelAndButtonsPanel.setWidth("100%");
		availablePanel.add(labelAndButtonsPanel);
		labelAndButtonsPanel.add(new Label(Messages.getString("roles")));
		hSpacer = new SimplePanel();
		hSpacer.setWidth("100%");
		labelAndButtonsPanel.add(hSpacer);
		labelAndButtonsPanel.setCellWidth(hSpacer, "100%");
		newRoleButton = new ImageButton(moduleBaseURL + "images/add_icon.png", "", "", 15, 15);
		labelAndButtonsPanel.add(newRoleButton);
		hSpacer = new SimplePanel();
		hSpacer.setWidth("7px");
		labelAndButtonsPanel.add(hSpacer);
		deleteRoleButton = new ImageButton(moduleBaseURL + "images/remove_icon.png", "", "", 16, 16);
		labelAndButtonsPanel.add(deleteRoleButton);

		rolesListBox = new ListBox(true);
		availablePanel.add(rolesListBox);
		rolesListBox.setVisibleItemCount(20);
		rolesListBox.setWidth("200px");
		rolesListBox.setHeight("432px");

		hSpacer = new SimplePanel();
		hSpacer.setWidth("24px");
		mainRolesPanel.add(hSpacer);

		VerticalPanel detailsPanel = new VerticalPanel();
		mainRolesPanel.add(detailsPanel);
		mainRolesPanel.setCellWidth(detailsPanel, "100%");

		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		detailsPanel.add(hSpacer);
		
		rolesPermissionsPanel = new PermissionsPanel(rolesListBox);
		detailsPanel.add(rolesPermissionsPanel);

		usersLabelPanel = new VerticalPanel();
		Label usersLabel = new Label(Messages.getString("users"));
		usersLabel.setStyleName("pentaho-fieldgroup-minor");
    hSpacer = new SimplePanel();
    hSpacer.setHeight("15px");
    usersLabelPanel.add(hSpacer);
		usersLabelPanel.add(usersLabel);
    hSpacer = new SimplePanel();
    hSpacer.setHeight("15px");
    usersLabelPanel.add(hSpacer);
		detailsPanel.add(usersLabelPanel);

		usersPanel = new HorizontalPanel();
		detailsPanel.add(usersPanel);

		VerticalPanel availableMembersPanel = new VerticalPanel();
		usersPanel.add(availableMembersPanel);
		availableMembersPanel.add(new Label(Messages.getString("available") + ":"));
		availableMembersListBox = new ListBox(true);
		availableMembersPanel.add(availableMembersListBox);
		availableMembersListBox.setVisibleItemCount(20);
		availableMembersListBox.setWidth("200px");
		availableMembersListBox.setHeight("265px");

		VerticalPanel vSpacer = new VerticalPanel();
		vSpacer.setWidth("15px");
		usersPanel.add(vSpacer);

		VerticalPanel arrowsPanel = new VerticalPanel();
		usersPanel.add(arrowsPanel);
		arrowsPanel.setWidth("35px");

		hSpacer = new SimplePanel();
		hSpacer.setHeight("80px");
		arrowsPanel.add(hSpacer);

		addUserButton = new ImageButton(moduleBaseURL + "images/accum_add.png", "", "", 14, 13);
		arrowsPanel.add(addUserButton);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("10px");
		arrowsPanel.add(hSpacer);

		removeUserButton = new ImageButton(moduleBaseURL + "images/accum_remove.png", "", "", 14, 13);
		arrowsPanel.add(removeUserButton);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("30px");
		arrowsPanel.add(hSpacer);

		addAllUsersButton = new ImageButton(moduleBaseURL + "images/accum_add_all.png", "", "", 14, 13);
		arrowsPanel.add(addAllUsersButton);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("10px");
		arrowsPanel.add(hSpacer);

		removeAllUsersButton = new ImageButton(moduleBaseURL + "images/accum_remove_all.png", "", "", 14, 13);
		arrowsPanel.add(removeAllUsersButton);

		VerticalPanel selectedMembersPanel = new VerticalPanel();
		usersPanel.add(selectedMembersPanel);
		selectedMembersPanel.add(new Label(Messages.getString("selected") + ":"));
		selectedMembersListBox = new ListBox(true);
		selectedMembersPanel.add(selectedMembersListBox);
		selectedMembersListBox.setVisibleItemCount(20);
		selectedMembersListBox.setWidth("200px");
		selectedMembersListBox.setHeight("265px");

		return mainRolesPanel;
	}
}
