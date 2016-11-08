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

package org.pentaho.mantle.client.solutionbrowser;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.mantle.client.commands.DeleteFolderCommand;
import org.pentaho.mantle.client.commands.DeletePermanentFileCommand;
import org.pentaho.mantle.client.commands.ExportFileCommand;
import org.pentaho.mantle.client.commands.FilePropertiesCommand;
import org.pentaho.mantle.client.commands.ImportFileCommand;
import org.pentaho.mantle.client.commands.NewFolderCommand;
import org.pentaho.mantle.client.commands.PasteFilesCommand;

public class FolderCommand implements Command {

  public static enum COMMAND {
    DELETE, PROPERTIES, CREATE_FOLDER, EXPORT, IMPORT, PASTE, EMPTY_TRASH
  };

  COMMAND mode;
  PopupPanel popupMenu;
  RepositoryFile repositoryFile;

  public FolderCommand( COMMAND inMode, PopupPanel popupMenu, RepositoryFile repositoryFile ) {
    this.mode = inMode;
    this.popupMenu = popupMenu;
    this.repositoryFile = repositoryFile;
  }

  public void execute() {
    if ( popupMenu != null ) {
      popupMenu.hide();
    }

    SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();

    if ( mode == COMMAND.PROPERTIES ) {
      new FilePropertiesCommand( repositoryFile ).execute();
    } else if ( mode == COMMAND.DELETE ) {
      TreeItem item = sbp.getSolutionTree().getSelectedItem();
      RepositoryFileTree tree = (RepositoryFileTree) item.getUserObject();
      new DeleteFolderCommand( tree.getFile() ).execute();
    } else if ( mode == COMMAND.CREATE_FOLDER ) {
      TreeItem item = sbp.getSolutionTree().getSelectedItem();
      RepositoryFileTree tree = (RepositoryFileTree) item.getUserObject();
      new NewFolderCommand( tree.getFile() ).execute();
    } else if ( mode == COMMAND.EXPORT ) {
      new ExportFileCommand( repositoryFile ).execute();
    } else if ( mode == COMMAND.IMPORT ) {
      new ImportFileCommand( repositoryFile ).execute();
    } else if ( mode == COMMAND.PASTE ) {
      new PasteFilesCommand().execute();
    } else if ( mode == COMMAND.EMPTY_TRASH ) {
      new DeletePermanentFileCommand().execute();
    }
  }

}
