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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.events.SolutionFolderActionEvent;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Label;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

public class DeleteFolderCommand extends AbstractCommand {
  
  String moduleBaseURL = GWT.getModuleBaseURL();

  String moduleName = GWT.getModuleName();

  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
  
  RepositoryFile repositoryFile;

  public DeleteFolderCommand() {
  }

  public DeleteFolderCommand(RepositoryFile repositoryFile) {
    this.repositoryFile = repositoryFile;
  }

  private String solutionPath = null;

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath(String solutionPath) {
    this.solutionPath = solutionPath;
  }

  protected void performOperation() {
    if(this.getSolutionPath() != null){
      SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
      sbp.getFile(this.getSolutionPath(), new SolutionFileHandler() {
        @Override
        public void handle(RepositoryFile repositoryFile) {
          DeleteFolderCommand.this.repositoryFile = repositoryFile;
          performOperation(true);
        }
      });
    }
    else{
      performOperation(true);
    }
  }

  protected void performOperation(boolean feedback) {

    final SolutionFolderActionEvent event = new SolutionFolderActionEvent();
    event.setAction(this.getClass().getName());

    final String filesList = repositoryFile.getId();
    final Label messageTextBox = new Label(Messages.getString("deleteFolderWarning", repositoryFile.getName()));
    final PromptDialogBox folderDeleteWarningDialogBox = new PromptDialogBox(Messages.getString("delete"), Messages.getString("yesDelete"), Messages.getString("no"), true, true);
    folderDeleteWarningDialogBox.setContent(messageTextBox);

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        folderDeleteWarningDialogBox.hide();
      }

      public void okPressed() {
        String deleteFilesURL = contextURL + "api/repo/files/delete"; //$NON-NLS-1$
        RequestBuilder deleteFilesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, deleteFilesURL);
        deleteFilesRequestBuilder.setHeader("Content-Type", "text/plain"); //$NON-NLS-1$//$NON-NLS-2$
        try {
          deleteFilesRequestBuilder.sendRequest(filesList, new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDeleteFolder"), //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true);
              dialogBox.center();
              event.setMessage(Messages.getString("couldNotDeleteFolder"));
              EventBusUtil.EVENT_BUS.fireEvent(event);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == 200) {
                new RefreshRepositoryCommand().execute(false);
                event.setMessage("Success");
                EventBusUtil.EVENT_BUS.fireEvent(event);
              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDeleteFolder"), //$NON-NLS-1$ //$NON-NLS-2$
                    false, false, true);
                dialogBox.center();
                event.setMessage(Messages.getString("couldNotDeleteFolder"));
                EventBusUtil.EVENT_BUS.fireEvent(event);
              }
            }

          });
        } catch (RequestException e) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDelete"), //$NON-NLS-1$ //$NON-NLS-2$
              false, false, true);
          dialogBox.center();
          event.setMessage(Messages.getString("couldNotDelete"));
          EventBusUtil.EVENT_BUS.fireEvent(event);
        }
      }
    };
    folderDeleteWarningDialogBox.setCallback(callback);
    folderDeleteWarningDialogBox.center();



  }

}
