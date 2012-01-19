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
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.http.client.URL;

public class NewFolderCommand extends AbstractCommand {

  RepositoryFile destinationFolder;
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));

  private RepositoryFile parentFolder;

  public NewFolderCommand(RepositoryFile parentFolder) {
    this.parentFolder = parentFolder;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    final TextBox folderNameTextBox = new TextBox();
    folderNameTextBox.setTabIndex(1);
    folderNameTextBox.setVisibleLength(40);

    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString("newFolderName"))); //$NON-NLS-1$
    vp.add(folderNameTextBox);
    final PromptDialogBox newFolderDialog = new PromptDialogBox(
        Messages.getString("newFolder"), Messages.getString("ok"), Messages.getString("cancel"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    newFolderDialog.setFocusWidget(folderNameTextBox);
    folderNameTextBox.setFocus(true);

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        newFolderDialog.hide();
      }

      public void okPressed() {
        String createDirUrl = contextURL + "api/repo/dirs/" + SolutionBrowserPanel.pathToId(parentFolder.getPath() + "/" + URL.encodeComponent(folderNameTextBox.getText())); //$NON-NLS-1$
        RequestBuilder createDirRequestBuilder = new RequestBuilder(RequestBuilder.PUT, createDirUrl);

        try {
          createDirRequestBuilder.sendRequest("", new RequestCallback() {

            @Override
            public void onError(Request createFolderRequest, Throwable exception) {
              MessageDialogBox dialogBox = new MessageDialogBox(
                  Messages.getString("error"), Messages.getString("couldNotCreateFolder", folderNameTextBox.getText()), //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true);
              dialogBox.center();

            }

            @Override
            public void onResponseReceived(Request createFolderRequest, Response createFolderResponse) {
              if (createFolderResponse.getStatusText().equalsIgnoreCase("OK")) { //$NON-NLS-1$
                new RefreshRepositoryCommand().execute(false);
              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(
                    Messages.getString("error"), Messages.getString("couldNotCreateFolder", folderNameTextBox.getText()), //$NON-NLS-1$ //$NON-NLS-2$
                    false, false, true);
                dialogBox.center();
              }
            }

          });
        } catch (RequestException e) {
          Window.alert(e.getLocalizedMessage());
        }

      }
    };
    newFolderDialog.setCallback(callback);
    newFolderDialog.center();
  }

}
