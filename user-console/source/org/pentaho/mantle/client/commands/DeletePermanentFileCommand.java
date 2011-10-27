/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Oct 21, 2011 
 * @author wseyler
 */

package org.pentaho.mantle.client.commands;

import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author wseyler
 *
 */
public class DeletePermanentFileCommand extends AbstractCommand {
  String moduleBaseURL = GWT.getModuleBaseURL();

  String moduleName = GWT.getModuleName();

  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));

  List<RepositoryFile> repositoryFiles;

  /**
   * @param fileSummary
   */
  public DeletePermanentFileCommand(List<RepositoryFile> selectedItemsClone) {
    repositoryFiles = selectedItemsClone;
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.Command#execute()
   */
  protected void performOperation(boolean feedback) {
    if (repositoryFiles == null || repositoryFiles.size() < 1) {
      return; // No files to delete
    }
    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString("deleteQuestion", Integer.toString(repositoryFiles.size())))); //$NON-NLS-1$
    final PromptDialogBox deleteConfirmDialog = new PromptDialogBox(Messages.getString("deleteConfirm"), Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        deleteConfirmDialog.hide();
      }

      public void okPressed() {
        String temp = "";

        for (RepositoryFile fileItem : repositoryFiles) {
          temp += fileItem.getId() + ","; //$NON-NLS-1$
        }
        // remove trailing ","
        temp = temp.substring(0, temp.length() - 1);
        final String filesList = temp;

        String deleteFilesURL = contextURL + "api/repo/files/deletepermanent"; //$NON-NLS-1$
        RequestBuilder deleteFilesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, deleteFilesURL);
        deleteFilesRequestBuilder.setHeader("Content-Type", "text/plain"); //$NON-NLS-1$//$NON-NLS-2$
        try {
          deleteFilesRequestBuilder.sendRequest(filesList, new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDelete"), //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true);
              dialogBox.center();
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == 200) {
                new RefreshRepositoryCommand().execute(false);
              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDelete"), //$NON-NLS-1$ //$NON-NLS-2$
                    false, false, true);
                dialogBox.center();
              }
            }

          });
        } catch (RequestException e) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDelete"), //$NON-NLS-1$ //$NON-NLS-2$
              false, false, true);
          dialogBox.center();
        }
      }
    };
    if (!feedback) {
      callback.okPressed();
    } else {
      deleteConfirmDialog.setCallback(callback);
      deleteConfirmDialog.center();
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation()
   */
  @Override
  protected void performOperation() {
    performOperation(true);
  }

}
