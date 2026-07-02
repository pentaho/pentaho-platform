/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
