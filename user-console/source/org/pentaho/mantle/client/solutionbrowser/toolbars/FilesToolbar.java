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
 * @created Aug 20, 2008 
 * @author wseyler
 */
package org.pentaho.mantle.client.solutionbrowser.toolbars;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarComboButton;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarGroup;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.MantleMenuBar;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.IRepositoryFileProvider;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.filelist.IFileItemListener;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author wseyler
 * 
 */
public class FilesToolbar extends Toolbar implements IFileItemListener {
  private static final String SEPARATOR = "separator"; //$NON-NLS-1$
  protected String FILE_GROUP_STYLE_NAME = "filesToolbarGroup"; //$NON-NLS-1$
  private IRepositoryFileProvider repositoryFileProvider;

  private static final String menuItemNames[] = { "openInNewWindow", //$NON-NLS-1$
      "runInBackground", //$NON-NLS-1$
      // edit action is a advanced feature, hidden normally
      "editAction", //$NON-NLS-1$
      "delete", //$NON-NLS-1$
      SEPARATOR, "share", //$NON-NLS-1$
      "scheduleEllipsis", //$NON-NLS-1$
      SEPARATOR, "propertiesEllipsis" //$NON-NLS-1$ 
  };

  FileCommand.COMMAND menuCommands[] = { COMMAND.NEWWINDOW, COMMAND.BACKGROUND, COMMAND.EDIT_ACTION, COMMAND.DELETE, null, COMMAND.SHARE, COMMAND.SCHEDULE_NEW,
      null, COMMAND.PROPERTIES };

  ToolbarComboButton miscComboBtn;
  ToolbarButton runBtn, editBtn;
  FileCommand runCmd, editCmd;
  MenuItem menuItems[] = null;
  FileCommand menuFileCommands[] = null;
  boolean supportsACLs = false;

  MenuBar miscMenus = new MantleMenuBar(true);

  public FilesToolbar(IRepositoryFileProvider repositoryFileProvider) {
    super();
    this.repositoryFileProvider = repositoryFileProvider;

    // Formatting stuff
    setHorizontalAlignment(ALIGN_RIGHT);
    setStyleName("pentaho-titled-toolbar");
    setSize("100%", "29px"); //$NON-NLS-1$//$NON-NLS-2$

    createMenus();
  }

  /**
   * 
   */
  private void createMenus() {
    addSpacer(5);
    Label label = new Label(Messages.getString("files"));
    label.setStyleName("pentaho-titled-toolbar-label");
    add(label); //$NON-NLS-1$
    add(GLUE);
    Image runImage = new Image();
    MantleImages.images.run().applyTo(runImage);
    Image runDisabledImage = new Image();
    MantleImages.images.runDisabled().applyTo(runDisabledImage);
    runBtn = new ToolbarButton(runImage, runDisabledImage);
    runBtn.setId("filesToolbarRun");
    runCmd = new FileCommand(FileCommand.COMMAND.RUN, null, repositoryFileProvider);
    runBtn.setCommand(runCmd);
    runBtn.setToolTip(Messages.getString("open")); //$NON-NLS-1$
    add(runBtn);

    Image editImage = new Image();
    MantleImages.images.update().applyTo(editImage);
    Image editDisabledImage = new Image();
    MantleImages.images.updateDisabled().applyTo(editDisabledImage);
    editBtn = new ToolbarButton(editImage, editDisabledImage);
    editBtn.setId("filesToolbarEdit");
    editCmd = new FileCommand(FileCommand.COMMAND.EDIT, null, repositoryFileProvider);
    editBtn.setCommand(editCmd);
    editBtn.setToolTip(Messages.getString("edit")); //$NON-NLS-1$
    add(editBtn);

    Image miscImage = new Image();
    MantleImages.images.misc().applyTo(miscImage);
    Image miscDisabledImage = new Image();
    MantleImages.images.miscDisabled().applyTo(miscDisabledImage);
    miscComboBtn = new ToolbarComboButton(miscImage, miscDisabledImage);
    miscComboBtn.setId("filesToolbarOptions");
    MantleServiceCache.getService().repositorySupportsACLS(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        Window.alert("FilesToolbar begin");
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), caught.toString(), false, false, true); //$NON-NLS-1$
        dialogBox.center();
        createMenuItems(false);
        Window.alert("FilesToolbar end");
      }

      public void onSuccess(Boolean result) {
        createMenuItems(result);
      }

    });
    miscComboBtn.setToolTip(Messages.getString("options")); //$NON-NLS-1$
    miscComboBtn.setStylePrimaryName("mantle-toolbar-combo-button");
    add(miscComboBtn);
    setEnabled(false);
  }

  private void createMenuItems(final boolean supportsACLs) {
    this.supportsACLs = supportsACLs;
    menuItems = new MenuItem[menuCommands.length];
    menuFileCommands = new FileCommand[menuCommands.length];
    for (int i = 0; i < menuCommands.length; i++) {
      // skip sharing if we don't support acls
      if (!supportsACLs && menuCommands[i] == COMMAND.SHARE) {
        continue;
      }
      if (!MantleApplication.showAdvancedFeatures && menuCommands[i] == COMMAND.EDIT_ACTION) {
        continue;
      }
      if (menuCommands[i] == null) {
        miscMenus.addSeparator();
      } else {
        menuFileCommands[i] = new FileCommand(menuCommands[i], miscComboBtn.getPopup(), repositoryFileProvider);
        menuItems[i] = miscMenus.addItem(Messages.getString(menuItemNames[i]), menuFileCommands[i]);
        menuItems[i].getElement().setId(makeSafeId("file_toolbar_menuitem_" + Messages.getString(menuItemNames[i])));
      }
    }
    miscComboBtn.setMenu(miscMenus);
  }

  private String makeSafeId(final String id) {
    String safeid = id.replace(' ', '_').replaceAll("\\.", "").replaceAll(":", "");
    return safeid.toLowerCase();
  }

  @Override
  public void popupClosed(PopupPanel panel) {
    IFrameTabPanel iframeTab = SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame();
    if (iframeTab == null || iframeTab.getFrame() == null) {
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    FrameUtils.setEmbedVisibility(currentFrame, true);
  }

  @Override
  public void popupOpened(PopupPanel panel) {
    IFrameTabPanel iframeTab = SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame();
    if (iframeTab == null || iframeTab.getFrame() == null) {
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    if (ElementUtils.elementsOverlap(panel.getElement(), currentFrame.getElement())) {
      FrameUtils.setEmbedVisibility(currentFrame, false);
    }
  }

  public void itemSelected(FileItem item) {
    updateMenus(item);
  }
  
  /**
   * @param selectedFileItem
   */
  private void updateMenus(FileItem selectedFileItem) {
    setEnabled(selectedFileItem != null);
    runBtn.setEnabled(selectedFileItem != null && selectedFileItem.isCommandEnabled(COMMAND.RUN)); //$NON-NLS-1$
    editBtn.setEnabled(selectedFileItem != null && selectedFileItem.isCommandEnabled(COMMAND.EDIT)); //$NON-NLS-1$

    // iterate over the commands and enable / disable appropriately
    for (int i = 0; i < menuCommands.length; i++) {
      // skip sharing if not supporting acls, also skip separators
      if ((!supportsACLs && menuCommands[i] == COMMAND.SHARE) || menuCommands[i] == null || menuItems[i] == null) {
        continue;
      }

      if (selectedFileItem != null && selectedFileItem.isCommandEnabled(menuCommands[i])) {
        menuItems[i].setCommand(menuFileCommands[i]);
        menuItems[i].setStyleName("gwt-MenuItem"); //$NON-NLS-1$
      } else {
        menuItems[i].setCommand(null);
        menuItems[i].setStyleName("disabledMenuItem"); //$NON-NLS-1$
      }
    }
    miscComboBtn.setMenu(miscMenus);
  }

  /**
   * @author wseyler
   * 
   */
  public class FilesToolbarGroup extends ToolbarGroup {
    public FilesToolbarGroup(String groupName) {
      super(groupName);
    }

    /**
     * Changes the enabled status of the group. If enabled is false, the buttons will be disabled. If enabled is true, it will consult the buttons for their
     * current enabled state.
     * 
     * @param enabled
     *          boolena flag
     */
    public void setEnabled(boolean enabled) {
      super.setEnabled(true);
    }

    public void setTempDisabled(boolean disable) {
      super.setTempDisabled(false);
    }
  }

  public boolean getSupportsACLs() {
    return supportsACLs;
  }
}
