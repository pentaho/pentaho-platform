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
import org.pentaho.gwt.widgets.client.listbox.CustomListBox;
import org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UserRolesAdminPanel extends SimplePanel implements ISysAdminPanel {

	private String moduleBaseURL = GWT.getModuleBaseURL();
	private String moduleName = GWT.getModuleName();
	private String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));

	public UserRolesAdminPanel() {
		FlexTable mainPanel = new FlexTable();

		mainPanel.setWidget(0, 0, new Label(Messages.getString("users") + "/" + Messages.getString("roles")));

		PentahoTabPanel mainTabPanel = new PentahoTabPanel();
		mainTabPanel.setWidth("715px");
		mainTabPanel.setHeight("515px");
		mainTabPanel.addTab(Messages.getString("users"), "", false, createUsersPanel());
		mainTabPanel.addTab(Messages.getString("roles"), "", false, createRolesPanel());
		mainPanel.setWidget(1, 0, mainTabPanel);

		setWidget(mainPanel);
	}

	private Widget createUsersPanel() {

		HorizontalPanel mainPanel = new HorizontalPanel();
		SimplePanel hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		mainPanel.add(hSpacer);

		VerticalPanel availablePanel = new VerticalPanel();
		mainPanel.add(availablePanel);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		availablePanel.add(hSpacer);

		HorizontalPanel labelAndButtonsPanel = new HorizontalPanel();
		availablePanel.add(labelAndButtonsPanel);
		labelAndButtonsPanel.add(new Label(Messages.getString("available") + ":"));
		hSpacer = new SimplePanel();
		hSpacer.setWidth("103px");
		labelAndButtonsPanel.add(hSpacer);
		labelAndButtonsPanel.add(new ImageButton(moduleBaseURL + "images/Add.png", "", ""));
		hSpacer = new SimplePanel();
		hSpacer.setWidth("7px");
		labelAndButtonsPanel.add(hSpacer);
		labelAndButtonsPanel.add(new ImageButton(moduleBaseURL + "images/Remove.png", "", ""));

		CustomListBox usersListBox = new CustomListBox();
		availablePanel.add(usersListBox);
		usersListBox.setVisibleRowCount(20);
		usersListBox.setWidth("200px");
		usersListBox.setHeight("432px");

		hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		mainPanel.add(hSpacer);

		VerticalPanel detailsPanel = new VerticalPanel();
		mainPanel.add(detailsPanel);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("32px");
		detailsPanel.add(hSpacer);

		detailsPanel.add(new Label(Messages.getString("name") + ":"));
		TextBox userNameTextBox = new TextBox();
		detailsPanel.add(userNameTextBox);

		detailsPanel.add(new Label(Messages.getString("password") + ":"));
		PasswordTextBox userPasswordTextBox = new PasswordTextBox();
		detailsPanel.add(userPasswordTextBox);

		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		detailsPanel.add(hSpacer);

		detailsPanel.add(new Label(Messages.getString("role") + ":"));

		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		detailsPanel.add(hSpacer);

		HorizontalPanel groupsPanel = new HorizontalPanel();
		detailsPanel.add(groupsPanel);

		VerticalPanel availableRolesPanel = new VerticalPanel();
		groupsPanel.add(availableRolesPanel);
		availableRolesPanel.add(new Label(Messages.getString("available") + ":"));
		CustomListBox availableRolesListBox = new CustomListBox();
		availableRolesPanel.add(availableRolesListBox);
		availableRolesListBox.setVisibleRowCount(20);
		availableRolesListBox.setWidth("200px");
		availableRolesListBox.setHeight("285px");

		VerticalPanel vSpacer = new VerticalPanel();
		vSpacer.setWidth("15px");
		groupsPanel.add(vSpacer);

		VerticalPanel arrowsPanel = new VerticalPanel();
		groupsPanel.add(arrowsPanel);
		arrowsPanel.setWidth("35px");

		hSpacer = new SimplePanel();
		hSpacer.setHeight("110px");
		arrowsPanel.add(hSpacer);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_add.png", "", ""));
		hSpacer = new SimplePanel();
		hSpacer.setHeight("10px");
		arrowsPanel.add(hSpacer);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_remove.png", "", ""));
		hSpacer = new SimplePanel();
		hSpacer.setHeight("30px");
		arrowsPanel.add(hSpacer);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_add_all.png", "", ""));
		hSpacer = new SimplePanel();
		hSpacer.setHeight("10px");
		arrowsPanel.add(hSpacer);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_remove_all.png", "", ""));

		VerticalPanel selectedRolesPanel = new VerticalPanel();
		groupsPanel.add(selectedRolesPanel);
		selectedRolesPanel.add(new Label(Messages.getString("selected") + ":"));
		CustomListBox selectedRolesListBox = new CustomListBox();
		selectedRolesPanel.add(selectedRolesListBox);
		selectedRolesListBox.setVisibleRowCount(20);
		selectedRolesListBox.setWidth("200px");
		selectedRolesListBox.setHeight("285px");

		return mainPanel;
	}

	private Widget createRolesPanel() {

		HorizontalPanel mainPanel = new HorizontalPanel();
		SimplePanel hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		mainPanel.add(hSpacer);

		VerticalPanel availablePanel = new VerticalPanel();
		mainPanel.add(availablePanel);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		availablePanel.add(hSpacer);

		HorizontalPanel labelAndButtonsPanel = new HorizontalPanel();
		availablePanel.add(labelAndButtonsPanel);
		labelAndButtonsPanel.add(new Label(Messages.getString("available") + ":"));
		hSpacer = new SimplePanel();
		hSpacer.setWidth("103px");
		labelAndButtonsPanel.add(hSpacer);
		labelAndButtonsPanel.add(new ImageButton(moduleBaseURL + "images/Add.png", "", ""));
		hSpacer = new SimplePanel();
		hSpacer.setWidth("7px");
		labelAndButtonsPanel.add(hSpacer);
		labelAndButtonsPanel.add(new ImageButton(moduleBaseURL + "images/Remove.png", "", ""));

		CustomListBox rolesListBox = new CustomListBox();
		availablePanel.add(rolesListBox);
		rolesListBox.setVisibleRowCount(20);
		rolesListBox.setWidth("200px");
		rolesListBox.setHeight("432px");

		hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		mainPanel.add(hSpacer);

		VerticalPanel detailsPanel = new VerticalPanel();
		mainPanel.add(detailsPanel);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("32px");
		detailsPanel.add(hSpacer);

		detailsPanel.add(new Label(Messages.getString("name") + ":"));
		TextBox roleNameTextBox = new TextBox();
		detailsPanel.add(roleNameTextBox);

		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		detailsPanel.add(hSpacer);

		detailsPanel.add(new Label(Messages.getString("members") + ":"));

		hSpacer = new SimplePanel();
		hSpacer.setHeight("15px");
		detailsPanel.add(hSpacer);

		HorizontalPanel groupsPanel = new HorizontalPanel();
		detailsPanel.add(groupsPanel);

		VerticalPanel availableMembersPanel = new VerticalPanel();
		groupsPanel.add(availableMembersPanel);
		availableMembersPanel.add(new Label(Messages.getString("available") + ":"));
		CustomListBox availableMembersListBox = new CustomListBox();
		availableMembersPanel.add(availableMembersListBox);
		availableMembersListBox.setVisibleRowCount(20);
		availableMembersListBox.setWidth("200px");
		availableMembersListBox.setHeight("328px");

		VerticalPanel vSpacer = new VerticalPanel();
		vSpacer.setWidth("15px");
		groupsPanel.add(vSpacer);

		VerticalPanel arrowsPanel = new VerticalPanel();
		groupsPanel.add(arrowsPanel);
		arrowsPanel.setWidth("35px");

		hSpacer = new SimplePanel();
		hSpacer.setHeight("130px");
		arrowsPanel.add(hSpacer);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_add.png", "", ""));
		hSpacer = new SimplePanel();
		hSpacer.setHeight("10px");
		arrowsPanel.add(hSpacer);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_remove.png", "", ""));
		hSpacer = new SimplePanel();
		hSpacer.setHeight("30px");
		arrowsPanel.add(hSpacer);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_add_all.png", "", ""));
		hSpacer = new SimplePanel();
		hSpacer.setHeight("10px");
		arrowsPanel.add(hSpacer);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_remove_all.png", "", ""));

		VerticalPanel selectedMembersPanel = new VerticalPanel();
		groupsPanel.add(selectedMembersPanel);
		selectedMembersPanel.add(new Label(Messages.getString("selected") + ":"));
		CustomListBox selectedMembersListBox = new CustomListBox();
		selectedMembersPanel.add(selectedMembersListBox);
		selectedMembersListBox.setVisibleRowCount(20);
		selectedMembersListBox.setWidth("200px");
		selectedMembersListBox.setHeight("328px");

		return mainPanel;
	}

	public void activate() {

	}

	public String getId() {
		return "userRolesAdminPanel";
	}

	public void passivate(final AsyncCallback<Boolean> callback) {
		callback.onSuccess(true);
	}
}
