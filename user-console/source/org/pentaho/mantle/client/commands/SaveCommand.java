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
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.gwt.widgets.client.tabs.PentahoTab;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.solutionbrowser.RepositoryFileTreeManager;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class SaveCommand extends AbstractCommand {

  boolean isSaveAs = false;
  private String name;
  private String path;
  private SolutionFileInfo.Type type;
  private String tabName;
  private String solution;

  public SaveCommand() {
  }

  public SaveCommand(boolean isSaveAs) {
    this.isSaveAs = isSaveAs;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    final SolutionBrowserPanel navigatorPerspective = SolutionBrowserPanel.getInstance();

    retrieveCachedValues(navigatorPerspective.getContentTabPanel().getCurrentFrame());

    RepositoryFileTreeManager.getInstance().fetchRepositoryFileTree(new AsyncCallback<RepositoryFileTree>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(RepositoryFileTree tree) {
        if (isSaveAs || name == null) {
          final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.SAVE, "", tree, false, true, Messages.getString("save"), Messages.getString("save")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          dialog.setSubmitOnEnter(MantleApplication.submitOnEnter);
          if (isSaveAs) {
            dialog.setTitle(Messages.getString("saveAs")); //$NON-NLS-1$
            dialog.setText(Messages.getString("saveAs")); //$NON-NLS-1$
          } else {
            dialog.setTitle(Messages.getString("save")); //$NON-NLS-1$
            dialog.setText(Messages.getString("save")); //$NON-NLS-1$
          }
          // TODO Uncomment the line below and delete the line after that once gwtwidets have been branched
          dialog.addFileChooserListener(new FileChooserListener() {

            public void dialogCanceled(){

            }

            @Override
            public void fileSelected(RepositoryFile file, String filePath, String fileName, String title) {
              SaveCommand.this.type = SolutionFileInfo.Type.XACTION; 
              SaveCommand.this.name = fileName;
              SaveCommand.this.path = filePath;
              tabName = name;
              if (tabName.indexOf("analysisview.xaction") != -1) {
                // trim off the analysisview.xaction from the localized-name
                tabName = tabName.substring(0, tabName.indexOf("analysisview.xaction") - 1);
              } 

               if (dialog.doesSelectedFileExist()) {
                dialog.hide();
                PromptDialogBox overWriteDialog = new PromptDialogBox(Messages.getString("question"), Messages.getString("yes"), Messages.getString("no"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    false, true);
                overWriteDialog.setContent(new Label(Messages.getString("fileExistsOverwrite"), false)); //$NON-NLS-1$
                overWriteDialog.setCallback(new IDialogCallback() {
                  public void okPressed() {
                    doSaveAs(navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, path, type, true);
                    Window.setTitle(Messages.getString("productName") + " - " + name); //$NON-NLS-1$ //$NON-NLS-2$
                  }

                  public void cancelPressed() {
                    dialog.show();
                  }
                });
                overWriteDialog.center();
              } else {
                doSaveAs(navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, path, type, true);
                Window.setTitle(Messages.getString("productName") + " - " + name); //$NON-NLS-1$ //$NON-NLS-2$
                persistFileInfoInFrame();
                clearValues();
              }
            }
            @Override
            public void fileSelectionChanged(RepositoryFile file, String filePath, String fileName, String title) {
              // TODO Auto-generated method stub
              
            }

          });
          dialog.center();
        } else {
          doSaveAs(navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, path, type, true);
          clearValues();
        }
      }
    }, false, null, null, SolutionBrowserPanel.getInstance().getSolutionTree().isShowHiddenFiles());
  }

  /**
   * @param elementId Id of the PUC tab containing the frame to look for a possible extensions callback in
   * @return All possible extensions provided by the frame.
   */
  private native JsArrayString getPossibleExtensions(String elementId)/*-{
   var frame = $doc.getElementById(elementId);
   frame = frame.contentWindow;
   frame.focus();
   if (frame.gCtrlr.repositoryBrowserController.getPossibleFileExtensions) {
     return frame.gCtrlr.repositoryBrowserController.getPossibleFileExtensions();
   }
   return [];
  }-*/;

  private void persistFileInfoInFrame() {
    SolutionFileInfo fileInfo = new SolutionFileInfo();
    fileInfo.setName(this.name);
    fileInfo.setPath(this.path);
    fileInfo.setType(this.type);
    SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame().setFileInfo(fileInfo);
  }

  private void clearValues() {
    name = null;
    path = null;
    type = null;
  }

  private void retrieveCachedValues(IFrameTabPanel tabPanel) {
    clearValues();
    SolutionFileInfo info = tabPanel.getFileInfo();
    if (info != null) {
      this.name = info.getName();
      this.path = info.getPath();
      this.type = info.getType();
    }
  }

  private void doSaveAs(String elementId, String filename, String path, SolutionFileInfo.Type type, boolean overwrite, boolean showBusy) {
    WaitPopup.getInstance().setVisible(true);
    this.doSaveAs(elementId, filename, path, type, overwrite);
    WaitPopup.getInstance().setVisible(false);
  }

  
  /**
   * This method will call saveReportSpecAs(string filename, string solution, string path, bool overwrite)
   * 
   * @param elementId
   */
  private native void doSaveAs(String elementId, String filename, String path, SolutionFileInfo.Type type, boolean overwrite)
  /*-{
    var frame = $doc.getElementById(elementId);
    frame = frame.contentWindow;
    frame.focus();                                
                
    if(frame.pivot_initialized) {
      // do jpivot save
      var actualFileName = filename;
      if (filename.indexOf("analysisview.xaction") == -1) {
        actualFileName = filename + ".analysisview.xaction";
      } else {
        // trim off the analysisview.xaction from the localized-name
        filename = filename.substring(0, filename.indexOf("analysisview.xaction")-1);
      }
      frame.controller.saveAs(actualFileName, filename, path, overwrite);
    } else if (frame.handle_puc_save) {
      try {
        var result = frame.handle_puc_save(path, filename, overwrite);
        //if(result) {
          this.@org.pentaho.mantle.client.commands.SaveCommand::doTabRename()();
        //}        
      } catch (e) {
        //TODO: externalize message once a solution to do so is found.
        $wnd.mantle_showMessage("Error","Error encountered while saving: "+e);
      }
    } else {
    	$wnd.mantle_showMessage("Error","The plugin has not defined a handle_puc_save function to handle the save of the content");    
    }
  }-*/;

  // used via JSNI
  private void doTabRename() {
    if (tabName != null) { // Save-As does not modify the name of the tab.
      PentahoTab tab = SolutionBrowserPanel.getInstance().getContentTabPanel().getSelectedTab();
      tab.setLabelText(tabName);
      tab.setLabelTooltip(tabName);
    }
  }

}
