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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.dialogs.OverwritePromptDialog;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserClipboard;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

/**
 * @author wseyler
 *
 */
public class PasteFilesCommand extends AbstractCommand {
  /**
   * 
   */
  private static final String NAME_NODE_TAG = "name"; //$NON-NLS-1$
  
  RepositoryFile destinationFolder;
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));

  /**
   * @param repositoryFile
   */
  public PasteFilesCommand(RepositoryFile destinationFolder) {
    this.destinationFolder = destinationFolder;
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
    final SolutionBrowserClipboard clipBoard = SolutionBrowserPanel.getInstance().getClipboard();
    @SuppressWarnings("unchecked")
    final List<FileItem> clipboardFileItems = (List<FileItem>) clipBoard.getData();
    final List<RepositoryFile> pasteFiles = new ArrayList<RepositoryFile>();
    for (FileItem fileItem : clipboardFileItems) {
      pasteFiles.add(fileItem.getRepositoryFile());
    }
    if (pasteFiles != null && pasteFiles.size() > 0 && destinationFolder != null) {
      String getChildrenUrl = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId(destinationFolder.getPath()) + "/children?depth=1"; //$NON-NLS-1$ //$NON-NLS-2$
      RequestBuilder childrenRequestBuilder = new RequestBuilder(RequestBuilder.GET, getChildrenUrl);
      try {
        childrenRequestBuilder.sendRequest(null, new RequestCallback() {
  
          @Override
          public void onError(Request getChildrenRequest, Throwable exception) {
            Window.alert(exception.toString());
          }
  
          @Override
          public void onResponseReceived(Request getChildrenRequest, Response getChildrenResponse) {
            if (getChildrenResponse.getStatusCode() >= 200 && getChildrenResponse.getStatusCode() < 300) {
              boolean promptForOptions = false;
              Document children = XMLParser.parse(getChildrenResponse.getText());
              NodeList childrenNameNodes = children.getElementsByTagName(NAME_NODE_TAG);
              List<String> childNames = new ArrayList<String>();
              for (int i=0; i<childrenNameNodes.getLength(); i++) {
                Node childNameNode = childrenNameNodes.item(i);
                childNames.add(childNameNode.getFirstChild().getNodeValue());
              }
              
              for (RepositoryFile repositoryFileDto : pasteFiles) {
                String pasteFileParentPath = repositoryFileDto.getPath();
                pasteFileParentPath = pasteFileParentPath.substring(0, pasteFileParentPath.lastIndexOf("/")); //$NON-NLS-1$
                if (childNames.contains(repositoryFileDto.getName()) && !destinationFolder.getPath().equals(pasteFileParentPath)) {
                  promptForOptions = true;
                  break;
                }
              }
              
              if (promptForOptions) {
                final OverwritePromptDialog overwriteDialog = new OverwritePromptDialog();
                final IDialogCallback callback = new IDialogCallback() {
                  public void cancelPressed() {
                    overwriteDialog.hide();
                  }
  
                  public void okPressed() {
                    performSave(clipBoard, overwriteDialog.getOverwriteMode());
                  }
                };
                overwriteDialog.setCallback(callback);
                overwriteDialog.center();
              } else {
                performSave(clipBoard, 2);
              }
            } else {
              Window.alert(getChildrenResponse.getText());
            }
          }
          
        });
      } catch (RequestException e) {
        Window.alert(e.getLocalizedMessage());
      }
    }
  }
  
  void performSave(final SolutionBrowserClipboard clipBoard, Integer overwriteMode) {
    @SuppressWarnings("unchecked")
    final List<FileItem> clipboardFileItems = (List<FileItem>) clipBoard.getData();
    String temp = ""; //$NON-NLS-1$
    for (FileItem fileItem : clipboardFileItems) {
      temp += fileItem.getRepositoryFile().getId() + ","; //$NON-NLS-1$
    }
    // remove trailing ","
    temp = temp.substring(0, temp.length()-1);
    final String filesList = temp;
    
    String pasteChildrenUrl = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId(destinationFolder.getPath()) + "/children?mode=" + overwriteMode;  //$NON-NLS-1$//$NON-NLS-2$
    RequestBuilder pasteChildrenRequestBuilder = new RequestBuilder(RequestBuilder.PUT, pasteChildrenUrl);
    pasteChildrenRequestBuilder.setHeader("Content-Type", "text/plain");  //$NON-NLS-1$//$NON-NLS-2$
    try {
      pasteChildrenRequestBuilder.sendRequest(filesList, new RequestCallback() {

        @Override
        public void onError(Request pasteChildrenRequest, Throwable exception) {
          Window.alert(exception.getLocalizedMessage());
        }

        @Override
        public void onResponseReceived(Request pasteChildrenRequest, Response pasteChildrenResponse) {
          SolutionBrowserClipboard.ClipboardAction action = SolutionBrowserPanel.getInstance().getClipboard().getClipboardAction();
          if (action == SolutionBrowserClipboard.ClipboardAction.CUT) {
            new DeleteFileCommand(clipboardFileItems).execute(false);
            clipBoard.clear();
          } else {
            new RefreshRepositoryCommand().execute(false);
          }
        }
        
      });
    } catch (RequestException e) {
      Window.alert(e.getLocalizedMessage());
    }        

  }
}
