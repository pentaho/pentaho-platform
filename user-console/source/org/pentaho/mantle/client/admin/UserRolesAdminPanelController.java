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

import java.util.ArrayList;

import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.gwt.tags.GwtConfirmBox;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;
import org.pentaho.ui.xul.util.XulDialogCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class UserRolesAdminPanelController extends UserRolesAdminPanel implements ISysAdminPanel, UpdatePasswordController {

  private static UserRolesAdminPanelController instance = new UserRolesAdminPanelController();

  public static UserRolesAdminPanelController getInstance() {
    return instance;
  }
  
	public UserRolesAdminPanelController() {
		super();

		usersListBox.addChangeHandler(new UsersListChangeListener());
		rolesListBox.addChangeHandler(new RolesListChangeListener());
		addRoleButton.addClickHandler(new AddRoleListener());
		removeRoleButton.addClickHandler(new RemoveRoleListener());
		addAllRolesButton.addClickHandler(new AddAllRolesListener());
		removeAllRolesButton.addClickHandler(new RemoveAllRolesListener());
		addUserButton.addClickHandler(new AddUserListener());
		removeUserButton.addClickHandler(new RemoveUserListener());
		addAllUsersButton.addClickHandler(new AddAllUsersListener());
		removeAllUsersButton.addClickHandler(new RemoveAllUsersListener());
		newUserButton.addClickHandler(new NewUserListener());
		deleteUserButton.addClickHandler(new DeleteUserListener());
		newRoleButton.addClickHandler(new NewRoleListener());
		deleteRoleButton.addClickHandler(new DeleteRoleListener());
		editPasswordButton.addClickHandler(new EditPasswordListener());

		activate();
	}

	public void saveUser(final String name, final String password) {
		String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/createUser?userName=" + name + "&password=" + password;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					initializeAvailableUsers(name);
					initializeAvailableRoles(rolesListBox.getValue(rolesListBox.getSelectedIndex()));
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	public void saveRole(final String name) {
		String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/createRole?roleName=" + name;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					initializeAvailableRoles(name);
					initializeAvailableUsers(usersListBox.getValue(usersListBox.getSelectedIndex()));
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	public void deleteRoles() {

		String selectedRoles = "";
		for (int i = 0; i < rolesListBox.getItemCount(); i++) {
			if (rolesListBox.isItemSelected(i)) {
				selectedRoles = selectedRoles + rolesListBox.getValue(i) + "|";
			}
		}

		String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/deleteRoles?roles=" + selectedRoles;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
				}

				public void onResponseReceived(Request request, Response response) {
				  checkForError(Messages.getString("Error"), response);
					roleNameTextBox.setText("");
					availableMembersListBox.clear();
					selectedMembersListBox.clear();
					initializeAvailableRoles(null);
					initializeAvailableUsers(usersListBox.getValue(usersListBox.getSelectedIndex()));
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}
	
	private void checkForError(String title, Response response) {
    if (response != null && response.getText() != null && response.getText().length() > 0) {
      GwtMessageBox messageBox = new GwtMessageBox();
      messageBox.setTitle(title);
      messageBox.setMessage(response.getText());
      messageBox.setButtons(new Object[GwtMessageBox.ACCEPT]);
      messageBox.setAcceptLabel(Messages.getString("close"));
      messageBox.setWidth(300);
      messageBox.show();
    }
	}

  private void displayErrorInMessageBox(String title, String message) {
      GwtMessageBox messageBox = new GwtMessageBox();
      messageBox.setTitle(title);
      messageBox.setMessage(message);
      messageBox.setButtons(new Object[GwtMessageBox.ACCEPT]);
      messageBox.setAcceptLabel(Messages.getString("close"));
      messageBox.setWidth(300);
      messageBox.show();
  }
	
	public void deleteUsers() {

		String selectedUsers = "";
		for (int i = 0; i < usersListBox.getItemCount(); i++) {
			if (usersListBox.isItemSelected(i)) {
				selectedUsers = selectedUsers + usersListBox.getValue(i) + "|";
			}
		}

		String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/deleteUsers?users=" + selectedUsers;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
				}

				public void onResponseReceived(Request request, Response response) {
				  checkForError(Messages.getString("Error"), response);
					userNameTextBox.setText("");
					userPasswordTextBox.setText("");
					availableRolesListBox.clear();
					selectedRolesListBox.clear();
					editPasswordButton.setEnabled(false);
					initializeAvailableUsers(null);
					initializeAvailableRoles(rolesListBox.getValue(rolesListBox.getSelectedIndex()));
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	public void updatePassword(String newPassword) {

		String userName = usersListBox.getValue(usersListBox.getSelectedIndex());
		String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/updatePassword?userName=" + userName + "&newPassword=" + newPassword;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
				}

				public void onResponseReceived(Request request, Response response) {
				  
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	private boolean hasMultiselection(ListBox listBox) {
		ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
		for (int i = 0; i < listBox.getItemCount(); i++) {
			if (listBox.isItemSelected(i)) {
				selectedIndices.add(i);
			}
		}
		return selectedIndices.size() > 1;
	}

	// -- Remote Calls.

	private void initializeAvailableRoles(final String defaultValue) {
		final String url = GWT.getHostPageBaseURL() + "api/roles";
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		executableTypesRequestBuilder.setHeader("accept", "application/xml");
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					rolesListBox.clear();
					NativeEvent event = com.google.gwt.dom.client.Document.get().createChangeEvent();
					String txt = response.getText();
					Document doc = XMLParser.parse(txt);
					NodeList roles = doc.getElementsByTagName("role");
					for (int i = 0; i < roles.getLength(); i++) {
						Node roleNode = roles.item(i);
						String role = roleNode.getFirstChild().getNodeValue();
						rolesListBox.addItem(role);
						if (!StringUtils.isEmpty(defaultValue)) {
							if (role.equals(defaultValue)) {
								rolesListBox.setSelectedIndex(i);
								DomEvent.fireNativeEvent(event, rolesListBox);
							}
						}
					}
					if (rolesListBox.getSelectedIndex() == -1 && rolesListBox.getItemCount() > 0) {
						rolesListBox.setSelectedIndex(0);
						DomEvent.fireNativeEvent(event, rolesListBox);
					}
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	private void initializeAvailableUsers(final String defaultValue) {
		final String url = GWT.getHostPageBaseURL() + "api/users";
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		executableTypesRequestBuilder.setHeader("accept", "application/xml");
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					usersListBox.clear();
					NativeEvent event = com.google.gwt.dom.client.Document.get().createChangeEvent();
					String txt = response.getText();
					Document doc = XMLParser.parse(txt);
					NodeList users = doc.getElementsByTagName("user");
					for (int i = 0; i < users.getLength(); i++) {
						Node userNode = users.item(i);
						String user = userNode.getFirstChild().getNodeValue();
						usersListBox.addItem(user);
						if (!StringUtils.isEmpty(defaultValue)) {
							if (user.equals(defaultValue)) {
								usersListBox.setSelectedIndex(i);
								DomEvent.fireNativeEvent(event, usersListBox);
							}
						}
					}
					if (usersListBox.getSelectedIndex() == -1 && usersListBox.getItemCount() > 0) {
						usersListBox.setSelectedIndex(0);
						DomEvent.fireNativeEvent(event, usersListBox);
					}
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	private void getRolesForUser(String user) {
		final String url = GWT.getHostPageBaseURL() + "api/userrole/getRolesForUser?user=" + user;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		executableTypesRequestBuilder.setHeader("accept", "application/xml");
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
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

					Timer t = new Timer() {
						public void run() {
							if (rolesListBox.getItemCount() > 0) {
								cancel();
								// availableRolesListBox =
								// rolesListBox - selectedRolesListBox
								availableRolesListBox.clear();
								for (int i = 0; i < rolesListBox.getItemCount(); i++) {
									String role = rolesListBox.getValue(i);
									boolean isSelected = false;
									for (int j = 0; j < selectedRolesListBox.getItemCount(); j++) {
										if (selectedRolesListBox.getValue(j).equals(role)) {
											isSelected = true;
										}
									}
									if (!isSelected) {
										availableRolesListBox.addItem(role);
									}
								}
							}
						}
					};
					t.scheduleRepeating(100);
				}
			});
		} catch (RequestException e) {
		    displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	private void getUsersInRole(String role) {
		final String url = GWT.getHostPageBaseURL() + "api/userrole/getUsersInRole?role=" + role;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		executableTypesRequestBuilder.setHeader("accept", "application/xml");
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
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

					Timer t = new Timer() {
						public void run() {
							if (usersListBox.getItemCount() > 0) {
								cancel();
								// availableMembersListBox =
								// usersListBox - selectedMembersListBox
								availableMembersListBox.clear();
								for (int i = 0; i < usersListBox.getItemCount(); i++) {
									String user = usersListBox.getValue(i);
									boolean isSelected = false;
									for (int j = 0; j < selectedMembersListBox.getItemCount(); j++) {
										if (selectedMembersListBox.getValue(j).equals(user)) {
											isSelected = true;
										}
									}
									if (!isSelected) {
										availableMembersListBox.addItem(user);
									}
								}
							}
						}
					};
					t.scheduleRepeating(100);
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	private void modifyUserRoles(final String userName, String serviceUrl) {
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
				}

				public void onResponseReceived(Request request, Response response) {
          checkForError(Messages.getString("Error"), response);
					getRolesForUser(userName);
					initializeAvailableRoles(rolesListBox.getValue(rolesListBox.getSelectedIndex()));
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	private void modifyRoleUsers(final String roleName, String serviceUrl) {
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
				  displayErrorInMessageBox(Messages.getString("Error"), exception.getLocalizedMessage());
				}

				public void onResponseReceived(Request request, Response response) {
          checkForError(Messages.getString("Error"), response);
					getUsersInRole(roleName);
					initializeAvailableUsers(usersListBox.getValue(usersListBox.getSelectedIndex()));
					
				}
			});
		} catch (RequestException e) {
		  displayErrorInMessageBox(Messages.getString("Error"), e.getLocalizedMessage());
		}
	}

	// -- ISysAdminPanel implementation.

	public void activate() {
		initializeAvailableUsers(null);
		initializeAvailableRoles(null);
	}

	public String getId() {
		return "userRolesAdminPanel";
	}

	public void passivate(final AsyncCallback<Boolean> callback) {
		userPasswordTextBox.setText("");
		roleNameTextBox.setText("");
		userNameTextBox.setText("");
		rolesListBox.clear();
		usersListBox.clear();
		selectedRolesListBox.clear();
		selectedMembersListBox.clear();
		availableMembersListBox.clear();
		availableRolesListBox.clear();
		editPasswordButton.setEnabled(false);
		callback.onSuccess(true);
	}

	// -- Event Listeners.

	class UsersListChangeListener implements ChangeHandler {
		public void onChange(ChangeEvent evt) {
			if (hasMultiselection(usersListBox)) {
				userNameTextBox.setText("");
				userPasswordTextBox.setText("");
				editPasswordButton.setEnabled(false);
				availableRolesListBox.clear();
				selectedRolesListBox.clear();
			} else {
				String user = usersListBox.getValue(usersListBox.getSelectedIndex());
				if (!StringUtils.isEmpty(user)) {
					getRolesForUser(user);
					userNameTextBox.setText(user);
					userPasswordTextBox.setText("fakepassword");
					editPasswordButton.setEnabled(true);
				}
			}
		}
	}

	class RolesListChangeListener implements ChangeHandler {
		public void onChange(ChangeEvent evt) {
			if (hasMultiselection(rolesListBox)) {
				roleNameTextBox.setText("");
				availableMembersListBox.clear();
				selectedMembersListBox.clear();
			} else {
				String role = rolesListBox.getValue(rolesListBox.getSelectedIndex());
				if (!StringUtils.isEmpty(role)) {
					getUsersInRole(role);
					roleNameTextBox.setText(role);
				}
			}
		}
	}

	class AddRoleListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			String userName = userNameTextBox.getText();

			String roleNames = "";
			for (int i = 0; i < availableRolesListBox.getItemCount(); i++) {
				if (availableRolesListBox.isItemSelected(i)) {
					roleNames = roleNames + availableRolesListBox.getValue(i) + "|";
				}
			}

			String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/assignRoleToUser?userName=" + userName + "&roleNames=" + roleNames;
			modifyUserRoles(userName, serviceUrl);
		}
	}

	class RemoveRoleListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			String userName = userNameTextBox.getText();

			String roleNames = "";
			for (int i = 0; i < selectedRolesListBox.getItemCount(); i++) {
				if (selectedRolesListBox.isItemSelected(i)) {
					roleNames = roleNames + selectedRolesListBox.getValue(i) + "|";
				}
			}

			String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/removeRoleFromUser?userName=" + userName + "&roleNames=" + roleNames;
			modifyUserRoles(userName, serviceUrl);
		}
	}

	class AddAllRolesListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			String userName = userNameTextBox.getText();
			String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/assignAllRolesToUser?userName=" + userName;
			modifyUserRoles(userName, serviceUrl);
		}
	}

	class RemoveAllRolesListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			String userName = userNameTextBox.getText();
			String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/removeAllRolesFromUser?userName=" + userName;
			modifyUserRoles(userName, serviceUrl);
		}
	}

	class AddUserListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			String roleName = roleNameTextBox.getText();

			String userNames = "";
			for (int i = 0; i < availableMembersListBox.getItemCount(); i++) {
				if (availableMembersListBox.isItemSelected(i)) {
					userNames = userNames + availableMembersListBox.getValue(i) + "|";
				}
			}

			String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/assignUserToRole?userNames=" + userNames + "&roleName=" + roleName;
			modifyRoleUsers(roleName, serviceUrl);
		}
	}

	class RemoveUserListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			String roleName = roleNameTextBox.getText();

			String userNames = "";
			for (int i = 0; i < selectedMembersListBox.getItemCount(); i++) {
				if (selectedMembersListBox.isItemSelected(i)) {
					userNames = userNames + selectedMembersListBox.getValue(i) + "|";
				}
			}

			String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/removeUserFromRole?userNames=" + userNames + "&roleName=" + roleName;
			modifyRoleUsers(roleName, serviceUrl);
		}
	}

	class AddAllUsersListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			String roleName = roleNameTextBox.getText();
			String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/assignAllUsersToRole?roleName=" + roleName;
			modifyRoleUsers(roleName, serviceUrl);
		}
	}

	class RemoveAllUsersListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			String roleName = roleNameTextBox.getText();
			String serviceUrl = GWT.getHostPageBaseURL() + "api/userrole/removeAllUsersFromRole?roleName=" + roleName;
			modifyRoleUsers(roleName, serviceUrl);
		}
	}

	class NewRoleListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			RoleDialog roleDialog = new RoleDialog(UserRolesAdminPanelController.this);
			roleDialog.show();
		}
	}

	class DeleteRoleListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			if (rolesListBox.getSelectedIndex() != -1) {
				GwtConfirmBox warning = new GwtConfirmBox();
				warning.setMessage(Messages.getString("deleteRoleMessage"));
				warning.setTitle(Messages.getString("deleteRoleTitle"));
				warning.addDialogCallback(new XulDialogCallback<String>() {
					public void onClose(XulComponent sender, Status returnCode, String retVal) {
						if (returnCode == Status.ACCEPT) {
							deleteRoles();
						}
					}

					public void onError(XulComponent sender, Throwable t) {
					 displayErrorInMessageBox(Messages.getString("Error"), t.getLocalizedMessage());
					}
				});
				warning.show();
			}
		}
	}

	class NewUserListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			UserDialog userDialog = new UserDialog(UserRolesAdminPanelController.this);
			userDialog.show();
		}
	}

	class DeleteUserListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			if (usersListBox.getSelectedIndex() != -1) {
				GwtConfirmBox warning = new GwtConfirmBox();
				warning.setHeight(117); 
				warning.setMessage(Messages.getString("deleteUserMessage"));
				warning.setTitle(Messages.getString("deleteUserTitle"));
				warning.addDialogCallback(new XulDialogCallback<String>() {
					public void onClose(XulComponent sender, Status returnCode, String retVal) {
						if (returnCode == Status.ACCEPT) {
							deleteUsers();
						}
					}

					public void onError(XulComponent sender, Throwable t) {
					 displayErrorInMessageBox(Messages.getString("Error"), t.getLocalizedMessage());
					}
				});
				warning.show();
			}
		}
	}

	class EditPasswordListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(UserRolesAdminPanelController.this);
			changePasswordDialog.show();
		}
	}
}
