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
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.IUnifiedRepositoryWebServiceCache;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DeleteFolderCommand extends AbstractCommand {
  RepositoryFileDto repositoryFile;

  public DeleteFolderCommand() {
  }

  public DeleteFolderCommand(RepositoryFileDto repositoryFile) {
    this.repositoryFile = repositoryFile;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    // delete folder
    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString("deleteQuestion", repositoryFile.getTitle()))); //$NON-NLS-1$
    final PromptDialogBox deleteConfirmDialog = new PromptDialogBox(
        Messages.getString("deleteConfirm"), Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        deleteConfirmDialog.hide();
      }

      public void okPressed() {
        String url = GWT.getModuleBaseURL();
        url = url.substring(0, url.lastIndexOf("mantle"));
        IUnifiedRepositoryWebServiceCache.getServiceRelativeToUrl(url).deleteFile(repositoryFile.getId(), null,  new AsyncCallback<Void>() { 

          public void onFailure(Throwable arg0) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDelete", repositoryFile.getTitle()), //$NON-NLS-1$ //$NON-NLS-2$
                false, false, true);
            dialogBox.center();
          }

          public void onSuccess(Void obj) {
            RefreshRepositoryCommand cmd = new RefreshRepositoryCommand();
            cmd.execute(false);
          }
        });
        }
    };
    deleteConfirmDialog.setCallback(callback);
    deleteConfirmDialog.center();
  }

  public RepositoryFileDto getRepositoryFile() {
    return repositoryFile;
  }

  public void setRepositoryFile(RepositoryFileDto repositoryFile) {
    this.repositoryFile = repositoryFile;
  }
}

