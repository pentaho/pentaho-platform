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

import java.util.HashMap;
import java.util.Map;

import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.FavoritePickList;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

/**
 * This class defines the options available for individual file extensions. Individual file extensions are
 * configured in the SolutionBrowserPersepective.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class FileTypeEnabledOptions {

  private String fileExtension;

  // eventually replace with treeset, once GWT 2906 is fixed
  private Map<COMMAND, Boolean> enabledOptions = new HashMap<COMMAND, Boolean>();

  public FileTypeEnabledOptions( String fileExtension ) {
    this.fileExtension = fileExtension;
  }

  public void applyOptions( String options ) {
    String[] opts = options.split( "," );
    for ( String option : opts ) {
      enabledOptions.put( COMMAND.valueOf( option ), true );
    }
  }

  public void addCommand( COMMAND command ) {
    enabledOptions.put( command, true );
  }

  public boolean isCommandEnabled( COMMAND command, HashMap<String, String> metadataPerms ) {

    boolean validSelectionCount = true;
    switch ( command ) {
      case CUT:
      case COPY:
      case DELETE:
        validSelectionCount = SolutionBrowserPanel.getInstance().getFilesListPanel().getSelectedFileItems().size() > 0;
        break;
      case FAVORITE:
        validSelectionCount =
            !isFavorite() && SolutionBrowserPanel.getInstance().getFilesListPanel().getSelectedFileItems().size() > 0;
        break;
      case FAVORITE_REMOVE:
        validSelectionCount =
            isFavorite() && SolutionBrowserPanel.getInstance().getFilesListPanel().getSelectedFileItems().size() > 0;
        break;
      case SCHEDULE_NEW:
        validSelectionCount =
            ( SolutionBrowserPanel.getInstance().isScheduler()
                && SolutionBrowserPanel.getInstance().getFilesListPanel().getSelectedFileItems().size() == 1
                && ( metadataPerms == null || ( metadataPerms != null && Boolean.parseBoolean( metadataPerms.get(
                    RepositoryFile.SCHEDULABLE_KEY ) ) ) ) );
        break;
      case BACKGROUND:
      case CREATE_FOLDER:
      case EDIT:
      case EDIT_ACTION:
      case EXPORT:
      case IMPORT:
      case NEWWINDOW:
      case PROPERTIES:
      case DELETEPERMANENT:
      case RESTORE:
      case RUN:
      case SCHEDULE_CUSTOM:
      case SHARE:
      case SUBSCRIBE:
      case GENERATED_CONTENT:
        validSelectionCount = SolutionBrowserPanel.getInstance().getFilesListPanel().getSelectedFileItems().size() == 1;
        break;
    }
    if ( command == COMMAND.CUT || command == COMMAND.COPY || command == COMMAND.DELETE
        || command == COMMAND.GENERATED_CONTENT || command == COMMAND.DELETEPERMANENT || command == COMMAND.RESTORE ) {
      return validSelectionCount;
    }

    if ( enabledOptions.containsKey( command ) ) {
      return validSelectionCount;
    }

    return false;
  }

  public boolean isSupportedFile( String filename ) {
    // default FileType
    if ( fileExtension == null ) {
      return true;
    } else {
      return filename != null && filename.endsWith( fileExtension );
    }
  }

  private boolean isFavorite() {
    if ( SolutionBrowserPanel.getInstance().getFilesListPanel().getSelectedFileItems().size() > 0 ) {
      return FavoritePickList.getInstance().contains(
          SolutionBrowserPanel.getInstance().getFilesListPanel().getSelectedFileItems().get( 0 ).getPath() );
    } else {
      return false;
    }
  }
}
