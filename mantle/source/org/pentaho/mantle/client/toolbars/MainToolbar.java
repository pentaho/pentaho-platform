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
 * @created Sep 4, 2008 
 * @author mdamour
 */

package org.pentaho.mantle.client.toolbars;

import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarToggleButton;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.commands.ToggleWorkspaceCommand;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author mdamour
 * 
 * @deprecated uses Xul instead... see org.pentaho.mantle.public.xul.main_toolbar.xul
 */
@Deprecated
public class MainToolbar extends Toolbar implements SolutionBrowserListener {
  protected String MAIN_TOOBAR_STYLE_NAME = "mainToolbar"; //$NON-NLS-1$

  ToolbarButton openFileButton;
  ToolbarButton newAnalysisButton;
  ToolbarButton saveButton;
  ToolbarButton saveAsButton;
  ToolbarButton showBrowserToggleButton;
  Image browseShowImage = new Image();
  Image browseHideImage = new Image();
  ToolbarToggleButton workspaceToggleButton;

  public MainToolbar() {
    super();
    this.setStylePrimaryName(MAIN_TOOBAR_STYLE_NAME);

    final SolutionBrowserPerspective solutionBrowser = SolutionBrowserPerspective.getInstance();
    
    Image openImage = new Image();
    MantleImages.images.open_32().applyTo(openImage);
    openFileButton = new ToolbarButton(openImage);
    openFileButton.setCommand(new OpenFileCommand());
    openFileButton.setToolTip(Messages.getString("openEllipsis")); //$NON-NLS-1$

    
    Image newAnalysisImage = new Image();
    MantleImages.images.new_analysis_32().applyTo(newAnalysisImage);
    newAnalysisButton = new ToolbarButton(newAnalysisImage);
    newAnalysisButton.setCommand(PluginOptionsHelper.getNewAnalysisViewCommand());
    newAnalysisButton.setToolTip(Messages.getString("newAnalysisViewEllipsis")); //$NON-NLS-1$

    Image saveButtonImage = new Image();
    MantleImages.images.save_32().applyTo(saveButtonImage);
    Image saveDisabledImage = new Image();
    MantleImages.images.save_32_disabled().applyTo(saveDisabledImage);
    saveButton = new ToolbarButton(saveButtonImage, saveDisabledImage);
    saveButton.setCommand(new SaveCommand(false));
    saveButton.setToolTip(Messages.getString("save")); //$NON-NLS-1$
    saveButton.setEnabled(false);

    Image saveAsButtonImage = new Image();
    MantleImages.images.saveAs_32().applyTo(saveAsButtonImage);
    Image saveAsDisabledImage = new Image();
    MantleImages.images.saveAs_32_disabled().applyTo(saveAsDisabledImage);
    saveAsButton = new ToolbarButton(saveAsButtonImage, saveAsDisabledImage);
    saveAsButton.setCommand(new SaveCommand(true));
    saveAsButton.setToolTip(Messages.getString("saveAs")); //$NON-NLS-1$
    saveAsButton.setEnabled(false);

    Image toggleWorkspaceImage = new Image();
    MantleImages.images.workspace_32().applyTo(toggleWorkspaceImage);
    Image toggleWorkspaceImageDisabled = saveAsDisabledImage = new Image();
    MantleImages.images.workspace_32().applyTo(toggleWorkspaceImageDisabled);
    workspaceToggleButton = new ToolbarToggleButton(toggleWorkspaceImage, toggleWorkspaceImageDisabled, solutionBrowser.isNavigatorShowing());
    final Command workspaceCmd = new ToggleWorkspaceCommand();
    workspaceToggleButton.setCommand(new Command() {
     public void execute() {
         workspaceCmd.execute();
      }  
    });
    toggleWorkspaceButton();
    workspaceToggleButton.setToolTip(Messages.getString("workspace")); //$NON-NLS-1$

    MantleImages.images.browser_show_32().applyTo(browseShowImage);
    MantleImages.images.browser_hide_32().applyTo(browseHideImage);
    showBrowserToggleButton = new ToolbarButton(browseShowImage, browseShowImage);
    toggleBrowserButton();
    showBrowserToggleButton.setCommand(new Command() {
      public void execute() {
        Command cmd = new ShowBrowserCommand();
        cmd.execute();
        toggleBrowserButton();
      }
    });
    showBrowserToggleButton.setToolTip(Messages.getString("toggleSolutionBrowser")); //$NON-NLS-1$

    addSpacer(10);
    add(openFileButton);
    addSpacer(20);
    add(newAnalysisButton);
    addSpacer(20);
    add(saveButton);
    add(saveAsButton);
    addSpacer(20);
    add(workspaceToggleButton);
    add(showBrowserToggleButton);
    
  }

  public void toggleWorkspaceButton() {
    final SolutionBrowserPerspective solutionBrowser = SolutionBrowserPerspective.getInstance();
    workspaceToggleButton.setSelected(solutionBrowser.isWorkspaceShowing(), false);
  }
  
  public void toggleBrowserButton() {
    final SolutionBrowserPerspective solutionBrowser = SolutionBrowserPerspective.getInstance();
    showBrowserToggleButton.setEnabled(false);
    if (solutionBrowser.isNavigatorShowing()) {
      showBrowserToggleButton.setImage(browseHideImage);
    } else {
      showBrowserToggleButton.setImage(browseShowImage);
    }
    showBrowserToggleButton.setEnabled(true);
  }
  
  public void solutionBrowserEvent(SolutionBrowserListener.EventType type, Widget panel, FileItem selectedFileItem) {
    toggleBrowserButton();
    toggleWorkspaceButton();
    
    String selectedTabURL = null;
    boolean saveEnabled = false;
    
    if(panel != null && panel instanceof IFrameTabPanel){
      selectedTabURL = ((IFrameTabPanel)panel).getUrl();
      saveEnabled = ((IFrameTabPanel)panel).isSaveEnabled();
    }
    
    saveButton.setEnabled(saveEnabled);
    saveAsButton.setEnabled(saveEnabled);
  }
  
  public void enableSave(boolean enable){
    saveButton.setEnabled(enable);
    saveAsButton.setEnabled(enable);
  }

}
