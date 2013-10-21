/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.solutionbrowser.toolbars;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarComboButton;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarGroup;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;
import org.pentaho.mantle.client.MantleMenuBar;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.filelist.IFileItemListener;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

/**
 * @author wseyler
 * 
 */
public class FilesToolbar extends Toolbar implements IFileItemListener {
  protected String FILE_GROUP_STYLE_NAME = "filesToolbarGroup"; //$NON-NLS-1$

  FileCommand.COMMAND[] menuCommands = { COMMAND.NEWWINDOW, COMMAND.BACKGROUND, COMMAND.EDIT_ACTION, COMMAND.DELETE,
    null, COMMAND.SHARE, COMMAND.SCHEDULE_NEW, null, COMMAND.GENERATED_CONTENT, COMMAND.PROPERTIES };

  ToolbarComboButton miscComboBtn;
  ToolbarButton runBtn, editBtn;
  FileCommand runCmd, editCmd;
  MenuItem[] menuItems = null;
  FileCommand[] menuFileCommands = null;

  MenuBar miscMenus = new MantleMenuBar( true );

  public FilesToolbar() {
    super();

    // Formatting stuff
    setHorizontalAlignment( ALIGN_RIGHT );
    setStyleName( "pentaho-titled-toolbar" );
    setSize( "100%", "29px" ); //$NON-NLS-1$//$NON-NLS-2$

    createMenus();
  }

  /**
   * 
   */
  private void createMenus() {
    addSpacer( 5 );
    Label label = new Label( Messages.getString( "files" ) );
    label.setStyleName( "pentaho-titled-toolbar-label" );
    add( label ); //$NON-NLS-1$
    add( GLUE );
    setEnabled( false );
  }

  @Override
  public void popupClosed( PopupPanel panel ) {
    IFrameTabPanel iframeTab = SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame();
    if ( iframeTab == null || iframeTab.getFrame() == null ) {
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    FrameUtils.setEmbedVisibility( currentFrame, true );
  }

  @Override
  public void popupOpened( PopupPanel panel ) {
    IFrameTabPanel iframeTab = SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame();
    if ( iframeTab == null || iframeTab.getFrame() == null ) {
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    if ( ElementUtils.elementsOverlap( panel.getElement(), currentFrame.getElement() ) ) {
      FrameUtils.setEmbedVisibility( currentFrame, false );
    }
  }

  public void itemSelected( FileItem item ) {

  }

  /**
   * @author wseyler
   * 
   */
  public class FilesToolbarGroup extends ToolbarGroup {
    public FilesToolbarGroup( String groupName ) {
      super( groupName );
    }

    /**
     * Changes the enabled status of the group. If enabled is false, the buttons will be disabled. If enabled is
     * true, it will consult the buttons for their current enabled state.
     * 
     * @param enabled
     *          boolena flag
     */
    public void setEnabled( boolean enabled ) {
      super.setEnabled( true );
    }

    public void setTempDisabled( boolean disable ) {
      super.setTempDisabled( false );
    }
  }

}
