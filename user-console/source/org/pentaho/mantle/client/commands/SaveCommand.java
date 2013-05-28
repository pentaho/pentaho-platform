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

import java.util.ArrayList;
import java.util.List;

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
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.RepositoryFileTreeManager;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper.ContentTypePlugin;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

import com.google.gwt.core.client.GWT;
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
            public void fileSelected(final RepositoryFile file, String filePath, String fileName, String title) {
              SaveCommand.this.type = SolutionFileInfo.Type.XACTION; 
              SaveCommand.this.name = fileName;
              SaveCommand.this.path = filePath;
              // final String fullPathWithName = filePath + "/" + fileName;
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
                    //navigatorPerspective.addRecent(fullPathWithName, name);
                  }

                  public void cancelPressed() {
                    dialog.show();
                  }
                });
                overWriteDialog.center();
              } else {
                
                //[Fix for PIR-833]
                //1) create a new interactive report. Save it in directory home as report1 
                //2) click "save as" button and wait for the file chooser to pop up. 
                //3) browse to directory home, and select report1. 
                //4) change the value in the filechooser's filename field to be report2. 
                //5) Ok the dialog. Observe the error message: "Unable to save your file."
                
                //filePath: /home/admin/report1.prpt
                //fileName: report2
                //title: report1
                //file.getName(): report1.prpt
                
                if(file != null 
                    && !file.isFolder()
                    && !fileName.equals(title)
                    && filePath.endsWith(file.getName())){
                  SaveCommand.this.path = filePath.substring(0, filePath.lastIndexOf("/"+file.getName()));
                }                
                
                doSaveAs(navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, path, type, true);
                Window.setTitle(Messages.getString("productName") + " - " + name); //$NON-NLS-1$ //$NON-NLS-2$
                persistFileInfoInFrame();
                //navigatorPerspective.addRecent(fullPathWithName, name);
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

  private void doSaveAs(String elementId, String filename, String path, SolutionFileInfo.Type type, boolean overwrite) {

    String unableToSaveMessage = Messages.getString("unableToSaveMessage");
    String save = Messages.getString("save");

    doSaveAsNativeWrapper(elementId, filename, path, type, overwrite, save, unableToSaveMessage);

  }
  /**
   * This method will call saveReportSpecAs(string filename, string solution, string path, bool overwrite)
   * 
   * @param elementId
   */
  private native void doSaveAsNativeWrapper(String elementId, String filename, String path, SolutionFileInfo.Type type, boolean overwrite, String save, String unableToSaveMessage)
  /*-{

    var errorCallback = function() {
      window.top.mantle_showMessage(save, unableToSaveMessage);
    }

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
        var result = frame.handle_puc_save(path, filename, overwrite, errorCallback);
        //if(result) {
          this.@org.pentaho.mantle.client.commands.SaveCommand::doTabRename()();
          this.@org.pentaho.mantle.client.commands.SaveCommand::updateFrameURL(Ljava/lang/String;)(decodeURIComponent(result));
          this.@org.pentaho.mantle.client.commands.SaveCommand::addToRecentList(Ljava/lang/String;)(decodeURIComponent(result));
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
  
  //used via JSNI
  private void addToRecentList(String fullPathWithName){
	  if(fullPathWithName != null && fullPathWithName.contains(name)){
		SolutionBrowserPanel.getInstance().addRecent(fullPathWithName, name);
	  }
  }
  //used via JSNI - JIRA BISERVER-9063
  //update URL context after save for deep linking
  @SuppressWarnings("static-access")
  private void updateFrameURL(String fullPathWithName){
    String currentUrl =  SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame().getUrl();          
    ContentTypePlugin plugin = PluginOptionsHelper.getContentTypePlugin(fullPathWithName);
    if (plugin != null && plugin.hasCommand(COMMAND.EDIT)) {
      // load the editor for this plugin
      String extension = ""; //$NON-NLS-1$     
      if (fullPathWithName.lastIndexOf(".") > 0) { //$NON-NLS-1$
        extension = fullPathWithName.substring(fullPathWithName.lastIndexOf(".") + 1); //$NON-NLS-1$
      }
      if(SolutionBrowserPanel.getInstance().getExecutableFileExtensions().contains(extension) ){
        String editor =  (plugin != null && (plugin.getCommandPerspective(COMMAND.EDIT) != null) ? plugin.getCommandPerspective(COMMAND.EDIT) : "editor");
        String editUrl = SolutionBrowserPanel.getInstance().getPath()
         + "api/repos/" + SolutionBrowserPanel.getInstance().pathToId(fullPathWithName) + "/" +  editor; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame().setUrl(editUrl );
      }
    }
  }

}
