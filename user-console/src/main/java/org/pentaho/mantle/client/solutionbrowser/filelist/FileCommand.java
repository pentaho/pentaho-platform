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


package org.pentaho.mantle.client.solutionbrowser.filelist;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.commands.ExportFileCommand;
import org.pentaho.mantle.client.commands.FilePropertiesCommand;
import org.pentaho.mantle.client.commands.ImportFileCommand;
import org.pentaho.mantle.client.commands.NewFolderCommand;
import org.pentaho.mantle.client.commands.ShareFileCommand;
import org.pentaho.mantle.client.solutionbrowser.IRepositoryFileProvider;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.FavoritePickList;

import java.util.List;

public class FileCommand implements Command {

  public static enum COMMAND {
    RUN, EDIT, DELETE, PROPERTIES, BACKGROUND, NEWWINDOW, SCHEDULE_CUSTOM,
    SCHEDULE_NEW, SUBSCRIBE, SHARE, EDIT_ACTION, CREATE_FOLDER, IMPORT, EXPORT, COPY,
    CUT, GENERATED_CONTENT, RESTORE, DELETEPERMANENT, FAVORITE, FAVORITE_REMOVE
  }

  COMMAND mode = COMMAND.RUN;
  PopupPanel popupMenu;
  private IRepositoryFileProvider fileSummaryProvider;
  private RepositoryFile fileSummary;

  /**
   * Suitable when an {@code IFileSummary} instance is not available at construction time.
   */
  public FileCommand( COMMAND inMode, PopupPanel popupMenu, IRepositoryFileProvider fileSummaryProvider ) {
    this.mode = inMode;
    this.popupMenu = popupMenu;
    this.fileSummaryProvider = fileSummaryProvider;
  }

  /**
   * Suitable when an {@code IFileSummary} instance is available at construction time.
   */
  public FileCommand( COMMAND inMode, PopupPanel popupMenu, RepositoryFile fileSummary ) {
    this.mode = inMode;
    this.popupMenu = popupMenu;
    this.fileSummary = fileSummary;
  }

  public void execute() {
    if ( popupMenu != null ) {
      popupMenu.hide();
    }

    SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
    FilesListPanel flp = sbp.getFilesListPanel();
    if ( flp.getSelectedFileItems() == null || flp.getSelectedFileItems().size() < 1 ) {
      return;
    }

    List<FileItem> selectedItems = flp.getSelectedFileItems();
    FileItem selectedItem = selectedItems.get( 0 );

    if ( mode == COMMAND.RUN || mode == COMMAND.NEWWINDOW ) {
      if ( selectedItem != null ) {
        sbp.openFile( selectedItem.getRepositoryFile(), mode );
      }
    } else if ( mode == COMMAND.PROPERTIES ) {
      new FilePropertiesCommand( fileSummary == null ? fileSummaryProvider.getRepositoryFiles().get( 0 ) : fileSummary )
          .execute();
    } else if ( mode == COMMAND.EDIT ) {
      sbp.editFile();
    } else if ( mode == COMMAND.CREATE_FOLDER ) {
      new NewFolderCommand( fileSummary ).execute();
    } else if ( mode == COMMAND.BACKGROUND ) {
      runInBackground( selectedItem.getRepositoryFile() );
    } else if ( mode == COMMAND.SCHEDULE_NEW ) {
      createSchedule( selectedItem.getRepositoryFile() );
    } else if ( mode == COMMAND.SHARE ) {
      new ShareFileCommand().execute();
    } else if ( mode == COMMAND.IMPORT ) {
      new ImportFileCommand( fileSummary == null ? fileSummaryProvider.getRepositoryFiles().get( 0 ) : fileSummary )
          .execute();
    } else if ( mode == COMMAND.EXPORT ) {
      new ExportFileCommand( fileSummary == null ? fileSummaryProvider.getRepositoryFiles().get( 0 ) : fileSummary )
          .execute();
    } else if ( mode == COMMAND.FAVORITE ) {
      sbp.addFavorite( selectedItem.getRepositoryFile().getPath(), selectedItem.getRepositoryFile().getTitle() );
      FavoritePickList.getInstance().save( "favorites" );
    } else if ( mode == COMMAND.FAVORITE_REMOVE ) {
      sbp.removeFavorite( selectedItem.getRepositoryFile().getPath() );
      FavoritePickList.getInstance().save( "favorites" );
    }
  }

  private void createSchedule( final RepositoryFile repositoryFile ) {
    createSchedule( repositoryFile.getId(), repositoryFile.getPath() );
  }

  private void runInBackground( final RepositoryFile repositoryFile ) {
    runInBackground( repositoryFile.getId(), repositoryFile.getPath() );
  }

  private native void createSchedule( final String repositoryFileId, final String repositoryFilePath )/*-{
    $wnd.pho.createSchedule( repositoryFileId, repositoryFilePath );
  }-*/;

  private native void runInBackground( final String repositoryFileId, final String repositoryFilePath )/*-{
    $wnd.pho.runInBackground( repositoryFileId, repositoryFilePath );
  }-*/;
}
