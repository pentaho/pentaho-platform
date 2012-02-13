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
 * @created Mar 24, 2011 
 * @author wseyler
 */


package org.pentaho.mantle.client.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.dialogs.GeneratedContentDialog;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;


/**
 * @author wseyler
 *
 */
public class ShowGeneratedContentCommand extends AbstractCommand {

  private RepositoryFile repositoryFile;
  
  /**
   * @param selectedItems
   */
  public ShowGeneratedContentCommand(FileItem selectedItem) {
    super();
    this.repositoryFile = selectedItem.getRepositoryFile();
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation()
   */
  @Override
  protected void performOperation() {
    performOperation(false);
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation(boolean)
   */
  @Override
  protected void performOperation(boolean feedback) {
//    GeneratedContentDialog dialog = new GeneratedContentDialog();
//    dialog.show();
    final String moduleBaseURL = GWT.getModuleBaseURL();
    final String moduleName = GWT.getModuleName();
    final String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    final String workspaceDirURL = contextURL + "api/session/userWorkspaceDir"; //$NON-NLS-1$
    RequestBuilder workspacePathRequestBuilder = new RequestBuilder(RequestBuilder.GET, workspaceDirURL);
    try {
      workspacePathRequestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          showError(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            String workspacePath = response.getText();
            String childrenURL = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId(workspacePath) + "/children";
            RequestBuilder workspaceFilesRequestBuilder = new RequestBuilder(RequestBuilder.GET, childrenURL);
            workspaceFilesRequestBuilder.setHeader("accept", "application/json");
            try {
              workspaceFilesRequestBuilder.sendRequest(null, new RequestCallback() {

               public void onError(Request request, Throwable exception) {
                  showError(exception);
                }

                public void onResponseReceived(Request request, Response response) {
                  if (response.getStatusCode() == Response.SC_OK) {
                    List<RepositoryFile> workspaceFiles = parseWorkspaceFiles(response.getText());
                    GeneratedContentDialog dialog = new GeneratedContentDialog(repositoryFile, workspaceFiles);
                    dialog.center();
                  } else {
                    showServerError(response);
                  }
                }
                
              });
            } catch (RequestException e) {
              showError(e);
            }
          } else {
            showServerError(response);
          }
        }
      });
    } catch (RequestException e) {
      showError(e);
    }
  }
  
  private void showError(Throwable throwable) {
    MessageDialogBox dialogBox = new MessageDialogBox(
        Messages.getString("error"), throwable.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
    dialogBox.center();
  }
  
  private void showServerError(Response response) {
    MessageDialogBox dialogBox = new MessageDialogBox(
        Messages.getString("error"), Messages.getString("serverErrorColon") + " " + response.getStatusCode(), false, false, true);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    dialogBox.center();    
  }
  
  private List<RepositoryFile> parseWorkspaceFiles(String JSONString) {
    List<RepositoryFile> files = new ArrayList<RepositoryFile>();
    @SuppressWarnings("deprecation")
    JSONValue value = JSONParser.parse(JSONString);
    
    JSONObject repositoryFileTreeDtoObject = value.isObject();
    JSONArray childrenArray = repositoryFileTreeDtoObject.get("children").isArray();
    if (childrenArray != null) {
      for (int i=0; i<childrenArray.size(); i++) {
        JSONObject rftdo = childrenArray.get(i).isObject();
        JSONObject repositoryFileJSON = rftdo.get("file").isObject();
        Boolean isFolder = false;
        JSONValue temp = repositoryFileJSON.get("folder");
        if (temp != null) {
          isFolder = Boolean.valueOf(temp.isString().stringValue());
        }
        String creatorId = "";
        temp = repositoryFileJSON.get("creatorId");
        if (temp != null) {
          creatorId = temp.isString().stringValue();
        }
        if (!isFolder && creatorId.equals(repositoryFile.getId())) {
          RepositoryFile newRepositoryFile = new RepositoryFile();
          newRepositoryFile.setFolder(isFolder);
          temp = repositoryFileJSON.get("description");
          if (temp != null) {
            newRepositoryFile.setDescription(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("createdDate");
          if (temp != null) {
            newRepositoryFile.setCreatedDate(parseDateTime(temp.isString().stringValue()));
          }
          temp = repositoryFileJSON.get("deletedDate");
          if (temp != null) {
            newRepositoryFile.setDeletedDate(parseDateTime(temp.isString().stringValue()));
          }
          temp = repositoryFileJSON.get("lastModifiedDate");
          if (temp != null) {
            newRepositoryFile.setLastModifiedDate(parseDateTime(temp.isString().stringValue()));
          }
          temp = repositoryFileJSON.get("lockDate");
          if (temp != null) {
            newRepositoryFile.setLockDate(parseDateTime(temp.isString().stringValue()));
          }
          temp = repositoryFileJSON.get("fileSize");
          if (temp != null) {
            newRepositoryFile.setFileSize(Long.valueOf(temp.isString().stringValue()));
          }
          temp = repositoryFileJSON.get("hidden");
          if (temp != null) {
            newRepositoryFile.setHidden(Boolean.valueOf(temp.isString().stringValue()));
          }
          temp = repositoryFileJSON.get("id");
          if (temp != null) {
            newRepositoryFile.setId(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("locale");
          if (temp != null) {
            newRepositoryFile.setLocale(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("locked");
          if (temp != null) {
            newRepositoryFile.setLocked(Boolean.valueOf(temp.isString().stringValue()));
          }
          temp = repositoryFileJSON.get("lockMessage");
          if (temp != null) {
            newRepositoryFile.setLockMessage(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("lockOwner");
          if (temp != null) {
            newRepositoryFile.setLockOwner(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("name");
          if (temp != null) {
            newRepositoryFile.setName(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("originalParentFolderId");
          if (temp != null) {
            newRepositoryFile.setOriginalParentFolderId(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("originalParentFolderPath");
          if (temp != null) {
            newRepositoryFile.setOriginalParentFolderPath(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("owner");
          if (temp != null) {
            newRepositoryFile.setOwner(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("ownerType");
          if (temp != null) {
            newRepositoryFile.setOwnerType(Integer.parseInt(temp.isString().stringValue()));
          }
          temp = repositoryFileJSON.get("path");
          if (temp != null) {
            newRepositoryFile.setPath(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("title");
          if (temp != null) {
            newRepositoryFile.setTitle(temp.isString().stringValue());
          }
          temp = repositoryFileJSON.get("versioned");
          if (temp != null) {
            newRepositoryFile.setVersioned(Boolean.valueOf(temp.isString().stringValue()));
          }
          temp = repositoryFileJSON.get("versionId");
          if (temp != null) {
            newRepositoryFile.setVersionId(temp.isString().stringValue());
          }
          files.add(newRepositoryFile);
        }
      }
    }
    Collections.sort(files, new Comparator<RepositoryFile>() {
      @Override
      public int compare(RepositoryFile o1, RepositoryFile o2) {
        return o2.getCreatedDate().compareTo(o1.getCreatedDate());
      }
      
    });
    return files;
  }
  
  private Date parseDateTime(String dateTimeString) { 
    // parse the date
    Date date = null;
      try {
        date = DateTimeFormat.getFormat("MM/dd/yyyy HH:mm:ss").parse(dateTimeString);
      } catch (Exception e) {
        return null;
      }
    return date;     
  }

}
