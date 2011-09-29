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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import java.util.ArrayList;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class SelectUserOrRoleDialog extends PromptDialogBox {

  private static FlexTable contentTable = new FlexTable();
  private static ListBox usersListBox = new ListBox(false);
  private static ListBox rolesListBox = new ListBox(false);

  static {
    usersListBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        rolesListBox.setSelectedIndex(-1);
      }      
    });
    rolesListBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        usersListBox.setSelectedIndex(-1);
      }      
    });
  }

  public SelectUserOrRoleDialog(ArrayList<String> existing, final IUserRoleSelectedCallback callback) {
    super(Messages.getString("selectUserOrRole"), Messages.getString("ok"), Messages.getString("cancel"), false, true, contentTable); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    setCallback(new IDialogCallback() {

      public void cancelPressed() {
      }

      public void okPressed() {
        if (getSelectedUser() != null) {
          callback.userSelected(getSelectedUser());
        } else {
          callback.roleSelected(getSelectedRole());
        }
      }
    });
    
    // Unique ids are important for test automation
    contentTable.getElement().setId("userOrRoleDialogContentTable");
    usersListBox.getElement().setId("userOrRoleDialogUsersList");
    rolesListBox.getElement().setId("userOrRoleDialogRolesList");
    okButton.getElement().setId("userOrRoleDialogOkButton");
    cancelButton.getElement().setId("userOrRoleDialogCancelButton");

    usersListBox.setVisibleItemCount(5);
    rolesListBox.setVisibleItemCount(5);
    rolesListBox.setWidth("100%"); //$NON-NLS-1$
    usersListBox.setWidth("100%"); //$NON-NLS-1$
    contentTable.clear();
    contentTable.setWidth("100%"); //$NON-NLS-1$
    contentTable.setWidget(0, 0, new Label(Messages.getString("users"))); //$NON-NLS-1$
    contentTable.setWidget(1, 0, usersListBox);
    contentTable.setWidget(2, 0, new Label(Messages.getString("roles"))); //$NON-NLS-1$
    contentTable.setWidget(3, 0, rolesListBox);
    fetchAllUsers(existing);
    fetchAllRoles(existing);
    setWidth("200px"); //$NON-NLS-1$
  }

  public void fetchAllRoles(final ArrayList<String> existing) {
    AsyncCallback<ArrayList<String>> callback = new AsyncCallback<ArrayList<String>>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetRoles"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialogBox.center();
      }

      public void onSuccess(ArrayList<String> roles) {
        // filter out existing
        rolesListBox.clear();
        for (String role : roles) {
          if (!existing.contains(role)) {
            rolesListBox.addItem(role);
          }
        }

      }
    };
    MantleServiceCache.getService().getAllRoles(callback);
  }

  public void fetchAllUsers(final ArrayList<String> existing) {
    AsyncCallback<ArrayList<String>> callback = new AsyncCallback<ArrayList<String>>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetUsers"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialogBox.center();
      }

      public void onSuccess(ArrayList<String> users) {
        // filter out existing
        usersListBox.clear();
        for (String user : users) {
          if (!existing.contains(user)) {
            usersListBox.addItem(user);
          }
        }
      }
    };
    MantleServiceCache.getService().getAllUsers(callback);
  }

  public static String getSelectedUser() {
    if (usersListBox.getSelectedIndex() >= 0) {
      return usersListBox.getItemText(usersListBox.getSelectedIndex());
    }
    return null;
  }

  public static String getSelectedRole() {
    if (rolesListBox.getSelectedIndex() >= 0) {
      return rolesListBox.getItemText(rolesListBox.getSelectedIndex());
    }
    return null;
  }

}
