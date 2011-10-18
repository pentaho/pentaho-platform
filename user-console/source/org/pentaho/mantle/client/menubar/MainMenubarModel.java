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
package org.pentaho.mantle.client.menubar;

import java.util.HashMap;

import org.pentaho.mantle.client.commands.FilePropertiesCommand;
import org.pentaho.mantle.client.commands.OpenDocCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.commands.RefreshWorkspaceCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class MainMenubarModel extends XulEventSourceAdapter implements SolutionBrowserListener {

  private boolean propertiesEnabled;
  private boolean saveEnabled;
  private boolean saveAsEnabled;
  private FileItem selectedFileItem;

  public MainMenubarModel() {
    SolutionBrowserPanel.getInstance().addSolutionBrowserListener(this);
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
    boolean showing = SolutionBrowserPanel.getInstance().isWorkspaceShowing();
    if (showing) {
      SolutionBrowserPanel.getInstance().showContent();
    } else {
      SolutionBrowserPanel.getInstance().showWorkspace();
    }
  }

  @Bindable
  public void refreshContent() {
    Command cmd = SolutionBrowserPanel.getInstance().isWorkspaceShowing() ? new RefreshWorkspaceCommand() : new RefreshRepositoryCommand();
    cmd.execute();
  }

  @Bindable
  public void toggleUseDescriptionsForTooltips() {
    SolutionBrowserPanel.getInstance().toggleUseDescriptionCommand.execute();
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

  public void solutionBrowserEvent(EventType type, Widget panel, FileItem selectedFileItem) {
    this.selectedFileItem = selectedFileItem;
    setPropertiesEnabled(selectedFileItem != null && selectedFileItem.getRepositoryFile() != null);
    setSaveEnabled(selectedFileItem != null && selectedFileItem.getRepositoryFile() != null);
    setSaveAsEnabled(selectedFileItem != null && selectedFileItem.getRepositoryFile() != null);
  }

}
