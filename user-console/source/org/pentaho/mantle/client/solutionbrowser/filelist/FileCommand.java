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

package org.pentaho.mantle.client.solutionbrowser.filelist;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.commands.ExportFileCommand;
import org.pentaho.mantle.client.commands.FilePropertiesCommand;
import org.pentaho.mantle.client.commands.ImportFileCommand;
import org.pentaho.mantle.client.commands.NewFolderCommand;
import org.pentaho.mantle.client.commands.RunInBackgroundCommand;
import org.pentaho.mantle.client.commands.ShareFileCommand;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleHelper;
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
      new RunInBackgroundCommand( selectedItem ).execute( true );
    } else if ( mode == COMMAND.SCHEDULE_NEW ) {
      ScheduleHelper.createSchedule( selectedItem.getRepositoryFile() );
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

}
