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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.mantle.client.commands;

import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.messages.Messages;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;

/**
 * @author wseyler
 *
 */
public class RestoreFileCommand implements Command {
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
  
  List<RepositoryFile> repositoryFiles;

  String fileList;

  public String getFileList() {
    return fileList;
  }

  public void setFileList(String fileList) {
    this.fileList = fileList;
  }

  public RestoreFileCommand() {
  }
  
  /**
   * @param fileSummary
   */
  public RestoreFileCommand(List<RepositoryFile> selectedItemsClone) {
    repositoryFiles = selectedItemsClone;
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.Command#execute()
   */
  @Override
  public void execute() {
    String temp = "";
    if(repositoryFiles!=null){
       for (RepositoryFile repoFile : repositoryFiles) {
        temp += repoFile.getId() + ","; //$NON-NLS-1$
      }
    }

    //Add file names from js
    temp=temp+fileList;

    // remove trailing ","
    temp = temp.substring(0, temp.length()-1);


    final String filesList = temp;

    String deleteFilesURL = contextURL + "api/repo/files/restore"; //$NON-NLS-1$
    RequestBuilder deleteFilesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, deleteFilesURL);
    deleteFilesRequestBuilder.setHeader("Content-Type", "text/plain");  //$NON-NLS-1$//$NON-NLS-2$
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
}
