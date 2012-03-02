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
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class UserRolesAdminPanel extends SimplePanel implements ISysAdminPanel {

	private String moduleBaseURL = GWT.getModuleBaseURL();
	private ListBox rolesListBox;
	private ListBox usersListBox;
	private ListBox selectedRolesListBox;
	private ListBox selectedMembersListBox;
	private ListBox availableMembersListBox;
	private ListBox availableRolesListBox;
	private PasswordTextBox userPasswordTextBox;
	private TextBox roleNameTextBox;
	private TextBox userNameTextBox;

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

		initializeAvailableUsers();
		initializeAvailableRoles();
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

		usersListBox = new ListBox();
		availablePanel.add(usersListBox);
		usersListBox.setVisibleItemCount(20);
		usersListBox.setWidth("200px");
		usersListBox.setHeight("432px");
		usersListBox.addChangeHandler(new UsersListChangeListener());

		hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		mainPanel.add(hSpacer);

		VerticalPanel detailsPanel = new VerticalPanel();
		mainPanel.add(detailsPanel);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("32px");
		detailsPanel.add(hSpacer);

		detailsPanel.add(new Label(Messages.getString("name") + ":"));
		userNameTextBox = new TextBox();
		userNameTextBox.setEnabled(false);
		detailsPanel.add(userNameTextBox);

		detailsPanel.add(new Label(Messages.getString("password") + ":"));
		userPasswordTextBox = new PasswordTextBox();
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
		availableRolesListBox = new ListBox();
		availableRolesPanel.add(availableRolesListBox);
		availableRolesListBox.setVisibleItemCount(20);
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
		selectedRolesListBox = new ListBox();
		selectedRolesPanel.add(selectedRolesListBox);
		selectedRolesListBox.setVisibleItemCount(20);
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

		rolesListBox = new ListBox();
		availablePanel.add(rolesListBox);
		rolesListBox.setVisibleItemCount(20);
		rolesListBox.setWidth("200px");
		rolesListBox.setHeight("432px");
		rolesListBox.addChangeHandler(new RolesListChangeListener());

		hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		mainPanel.add(hSpacer);

		VerticalPanel detailsPanel = new VerticalPanel();
		mainPanel.add(detailsPanel);
		hSpacer = new SimplePanel();
		hSpacer.setHeight("32px");
		detailsPanel.add(hSpacer);

		detailsPanel.add(new Label(Messages.getString("name") + ":"));
		roleNameTextBox = new TextBox();
		roleNameTextBox.setEnabled(false);
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
		availableMembersListBox = new ListBox();
		availableMembersPanel.add(availableMembersListBox);
		availableMembersListBox.setVisibleItemCount(20);
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
		selectedMembersListBox = new ListBox();
		selectedMembersPanel.add(selectedMembersListBox);
		selectedMembersListBox.setVisibleItemCount(20);
		selectedMembersListBox.setWidth("200px");
		selectedMembersListBox.setHeight("328px");

		return mainPanel;
	}

	public void activate() {
		initializeAvailableUsers();
		initializeAvailableRoles();
	}

	public String getId() {
		return "userRolesAdminPanel";
	}

	public void passivate(final AsyncCallback<Boolean> callback) {
		callback.onSuccess(true);
	}

	private void initializeAvailableUsers() {
		final String url = GWT.getHostPageBaseURL() + "api/users";
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		executableTypesRequestBuilder.setHeader("accept", "application/xml");
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
				}

				public void onResponseReceived(Request request, Response response) {
					usersListBox.clear();
					String txt = response.getText();
					Document doc = XMLParser.parse(txt);
					NodeList users = doc.getElementsByTagName("user");
					for (int i = 0; i < users.getLength(); i++) {
						Node userNode = users.item(i);
						String user = userNode.getFirstChild().getNodeValue();
						usersListBox.addItem(user);
					}
				}
			});
		} catch (RequestException e) {
		}
	}

	private void initializeAvailableRoles() {
		final String url = GWT.getHostPageBaseURL() + "api/roles";
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		executableTypesRequestBuilder.setHeader("accept", "application/xml");
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
				}

				public void onResponseReceived(Request request, Response response) {
					rolesListBox.clear();
					String txt = response.getText();
					Document doc = XMLParser.parse(txt);
					NodeList roles = doc.getElementsByTagName("role");
					for (int i = 0; i < roles.getLength(); i++) {
						Node roleNode = roles.item(i);
						String role = roleNode.getFirstChild().getNodeValue();
						rolesListBox.addItem(role);
					}
				}
			});
		} catch (RequestException e) {
		}
	}

	private void getRolesForUser(String user) {
		final String url = GWT.getHostPageBaseURL() + "api/userrole/getRolesForUser?user=" + user;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		executableTypesRequestBuilder.setHeader("accept", "application/xml");
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
				}

				public void onResponseReceived(Request request, Response response) {
					selectedRolesListBox.clear();
					String txt = response.getText();
					Document doc = XMLParser.parse(txt);
					NodeList roles = doc.getElementsByTagName("role");
					for (int i = 0; i < roles.getLength(); i++) {
						Node roleNode = roles.item(i);
						String role = roleNode.getFirstChild().getNodeValue();
						selectedRolesListBox.addItem(role);
					}

					// availableRolesListBox = rolesListBox - selectedRolesListBox
					availableRolesListBox.clear();
					for (int i = 0; i < rolesListBox.getItemCount(); i++) {
						String role = rolesListBox.getValue(i);
						boolean isSelected = false;
						for (int j = 0; j < selectedRolesListBox.getItemCount(); j++) {
							if (selectedRolesListBox.getValue(j).equals(role)) {
								isSelected = true;
							}
						}
						if(!isSelected) {
							availableRolesListBox.addItem(role);
						}
					}
				}
			});
		} catch (RequestException e) {
		}
	}

	private void getUsersInRole(String role) {
		final String url = GWT.getHostPageBaseURL() + "api/userrole/getUsersInRole?role=" + role;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		executableTypesRequestBuilder.setHeader("accept", "application/xml");
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
				}

				public void onResponseReceived(Request request, Response response) {
					selectedMembersListBox.clear();
					String txt = response.getText();
					Document doc = XMLParser.parse(txt);
					NodeList users = doc.getElementsByTagName("user");
					for (int i = 0; i < users.getLength(); i++) {
						Node userNode = users.item(i);
						String user = userNode.getFirstChild().getNodeValue();
						selectedMembersListBox.addItem(user);
					}

					// availableMembersListBox = usersListBox - selectedMembersListBox
					availableMembersListBox.clear();
					for (int i = 0; i < usersListBox.getItemCount(); i++) {
						String user = usersListBox.getValue(i);
						boolean isSelected = false;
						for (int j = 0; j < selectedMembersListBox.getItemCount(); j++) {
							if (selectedMembersListBox.getValue(j).equals(user)) {
								isSelected = true;
							}
						}
						if(!isSelected) {
							availableMembersListBox.addItem(user);
						}
					}
				}
			});
		} catch (RequestException e) {
		}
	}

	class UsersListChangeListener implements ChangeHandler {
		public void onChange(ChangeEvent evt) {
			String user = usersListBox.getValue(usersListBox.getSelectedIndex());
			if (!StringUtils.isEmpty(user)) {
				getRolesForUser(user);
				userNameTextBox.setText(user);
			}
		}
	}

	class RolesListChangeListener implements ChangeHandler {
		public void onChange(ChangeEvent evt) {
			String role = rolesListBox.getValue(rolesListBox.getSelectedIndex());
			if (!StringUtils.isEmpty(role)) {
				getUsersInRole(role);
				roleNameTextBox.setText(role);
			}
		}
	}
}
