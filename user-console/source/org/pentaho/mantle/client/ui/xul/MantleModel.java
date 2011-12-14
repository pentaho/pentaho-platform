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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 * 
 */
package org.pentaho.mantle.client.ui.xul;

import java.util.HashMap;

import org.pentaho.mantle.client.commands.FilePropertiesCommand;
import org.pentaho.mantle.client.commands.OpenDocCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.commands.RefreshWorkspaceCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class MantleModel extends XulEventSourceAdapter implements SolutionBrowserListener {

  private MantleXul main;
  private boolean saveEnabled;
  private boolean saveAsEnabled;
  private boolean newAnalysisEnabled;
  private boolean contentEditEnabled;
  private boolean contentEditSelected;
  private boolean showBrowserSelected;
  private boolean propertiesEnabled;

  private FileItem selectedFileItem;
  private JavaScriptObject callback;

  public MantleModel(MantleXul main) {
    SolutionBrowserPanel.getInstance().addSolutionBrowserListener(this);
    this.main = main;
  }

  @Bindable
  public boolean isPropertiesEnabled() {
    return this.propertiesEnabled;
  }

  @Bindable
  public void setPropertiesEnabled(Boolean enabled) {
    boolean prevVal = this.propertiesEnabled;
    propertiesEnabled = enabled;
    this.firePropertyChange("propertiesEnabled", prevVal, propertiesEnabled);
  }

  @Bindable
  public boolean isSaveEnabled() {
    return this.saveEnabled;
  }

  @Bindable
  public void setSaveEnabled(Boolean enabled) {
    boolean prevVal = this.saveEnabled;
    saveEnabled = enabled;
    this.firePropertyChange("saveEnabled", prevVal, saveEnabled);
  }

  @Bindable
  public boolean isSaveAsEnabled() {
    return this.saveAsEnabled;
  }

  @Bindable
  public void setSaveAsEnabled(Boolean enabled) {
    boolean prevVal = this.saveAsEnabled;
    saveAsEnabled = enabled;
    this.firePropertyChange("saveAsEnabled", prevVal, saveAsEnabled);
  }

  @Bindable
  public void executePropertiesCommand() {
    FilePropertiesCommand propertiesCommand = new FilePropertiesCommand(selectedFileItem.getRepositoryFile());
    propertiesCommand.execute();
  }

  @Bindable
  public void executeSaveCommand() {
    SaveCommand saveCommand = new SaveCommand();
    saveCommand.execute();
  }

  @Bindable
  public void executeSaveAsCommand() {
    SaveCommand saveAsCommand = new SaveCommand(true);
    saveAsCommand.execute();
  }

  @Bindable
  public void executeEditContent() {
    OpenFileCommand cmd = new OpenFileCommand(COMMAND.EDIT);
    cmd.execute();
  }

  @Bindable
  public void executeShareContent() {
    OpenFileCommand cmd = new OpenFileCommand(COMMAND.SHARE);
    cmd.execute();
  }

  @Bindable
  public void executeScheduleContent() {
    OpenFileCommand cmd = new OpenFileCommand(COMMAND.SCHEDULE_NEW);
    cmd.execute();
  }

  @Bindable
  public void toggleShowBrowser() {
    boolean showing = SolutionBrowserPanel.getInstance().isNavigatorShowing();
    SolutionBrowserPanel.getInstance().setNavigatorShowing(!showing);
  }

  @Bindable
  public void toggleShowWorkspace() {
    IPluginPerspective perspective = PerspectiveManager.getInstance().getActivePerspective();
    boolean showing = perspective.getId().equalsIgnoreCase("workspace.perspective");
    if (showing) {
      PerspectiveManager.getInstance().setPerspective("default.perspective");
    } else {
      PerspectiveManager.getInstance().setPerspective("workspace.perspective");
    }
  }

  @Bindable
  public void refreshContent() {
    if ("workspace.perspective".equals(PerspectiveManager.getInstance().getActivePerspective().getId())) {
      Command cmd = new RefreshWorkspaceCommand();
      cmd.execute();
    } else {
      Command cmd = new RefreshRepositoryCommand();
      cmd.execute();
    }
  }

  @Bindable
  public void toggleUseDescriptionsForTooltips() {
    SolutionBrowserPanel.getInstance().toggleUseDescriptionCommand.execute();
  }

  @Bindable
  public void toggleShowHideFiles() {
    SolutionBrowserPanel.getInstance().toggleShowHideFilesCommand.execute();
  }

  @Bindable
  public void openDocumentation() {
    MantleSettingsManager.getInstance().fetchMantleSettings(new AsyncCallback<HashMap<String, String>>() {

      public void onSuccess(HashMap<String, String> result) {
        OpenDocCommand cmd = new OpenDocCommand(result.get("documentation-url"));
        cmd.execute();
      }

      public void onFailure(Throwable caught) {
      }
    }, false);

  }

  @Bindable
  public void setNewAnalysisEnabled(Boolean enabled) {
    boolean prevVal = this.newAnalysisEnabled;
    newAnalysisEnabled = enabled;

    this.firePropertyChange("newAnalysisEnabled", prevVal, newAnalysisEnabled);
  }

  @Bindable
  public void executeOpenFileCommand() {
    OpenFileCommand openFileCommand = new OpenFileCommand();
    openFileCommand.execute();
  }

  @Bindable
  public void executeAnalysisViewCommand() {
    Command analysisViewCommand = PluginOptionsHelper.getNewAnalysisViewCommand();
    analysisViewCommand.execute();
  }

  /**
   * Process incoming events from the SolutionBrowser here
   * 
   * @TODO Move this listener to a controller where it really belongs, models shouldn't do this.
   */
  public void solutionBrowserEvent(SolutionBrowserListener.EventType type, Widget panel, FileItem selectedFileItem) {

    this.selectedFileItem = selectedFileItem;
    setPropertiesEnabled(selectedFileItem != null && selectedFileItem.getRepositoryFile() != null);
    setSaveEnabled(selectedFileItem != null && selectedFileItem.getRepositoryFile() != null);
    setSaveAsEnabled(selectedFileItem != null && selectedFileItem.getRepositoryFile() != null);

    boolean saveEnabled = false;
    boolean editIsEnabled = false;
    boolean editSelected = false;
    JavaScriptObject callback = null;

    if (panel != null && panel instanceof IFrameTabPanel) {
      IFrameTabPanel tbp = (IFrameTabPanel) panel;
      saveEnabled = tbp.isSaveEnabled();
      editIsEnabled = tbp.isEditEnabled();
      editSelected = tbp.isEditSelected();
    }

    setSaveEnabled(saveEnabled);
    setSaveAsEnabled(saveEnabled);
    setContentEditEnabled(editIsEnabled);
    setContentEditSelected(editSelected);
    setCallback(callback);

    setShowBrowserSelected(SolutionBrowserPanel.getInstance().isNavigatorShowing());

    if (panel instanceof IFrameTabPanel) {
      if (SolutionBrowserListener.EventType.OPEN.equals(type) || SolutionBrowserListener.EventType.SELECT.equals(type)) {
        if (panel != null) {
          main.applyOverlays(((IFrameTabPanel) panel).getOverlayIds());
        }
      } else if (SolutionBrowserListener.EventType.CLOSE.equals(type) || SolutionBrowserListener.EventType.DESELECT.equals(type)) {
        if (panel != null) {
          main.removeOverlays(((IFrameTabPanel) panel).getOverlayIds());
        }
      }
    }
  }

  @Bindable
  public boolean isShowBrowserSelected() {
    return showBrowserSelected;
  }

  @Bindable
  public void setShowBrowserSelected(boolean showBrowserSelected) {
    boolean prevVal = this.showBrowserSelected;

    this.showBrowserSelected = showBrowserSelected;
    this.firePropertyChange("showBrowserSelected", prevVal, showBrowserSelected);
  }

  @Bindable
  public void setContentEditEnabled(boolean enable) {
    boolean prevVal = this.contentEditEnabled;
    contentEditEnabled = enable;
    this.firePropertyChange("contentEditEnabled", prevVal, contentEditEnabled);
  }

  @Bindable
  public void setContentEditSelected(boolean selected) {
    boolean prevVal = this.contentEditSelected;
    contentEditSelected = selected;
    this.firePropertyChange("contentEditSelected", prevVal, contentEditSelected);
  }

  @Bindable
  public boolean isContentEditSelected() {
    return this.contentEditSelected;
  }

  @Bindable
  public void setContentEditToggled() {
    setContentEditSelected(!this.contentEditSelected);
  }

  @Bindable
  public boolean isContentEditEnabled() {
    return contentEditEnabled;
  }

  public JavaScriptObject getCallback() {
    return callback;
  }

  public void setCallback(JavaScriptObject callback) {
    this.callback = callback;
  }

}
