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


package org.pentaho.mantle.client.commands;

import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.dialogs.ImportDialog;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

public class ImportFileCommand extends AbstractCommand {

  private RepositoryFile repositoryFile;
  private ImportDialog importDialog;

  public ImportFileCommand() {
  }

  public ImportFileCommand( RepositoryFile repositoryFile ) {
    this.repositoryFile = repositoryFile;
  }

  private String solutionPath = null;

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  protected void performOperation() {

    if ( this.getSolutionPath() != null ) {
      SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
      sbp.getFile( this.getSolutionPath(), new SolutionFileHandler() {
        @Override
        public void handle( RepositoryFile repositoryFile ) {
          ImportFileCommand.this.repositoryFile = repositoryFile;
          performOperation( true );
        }
      } );
    } else {
      performOperation( true );
    }
  }

  protected void performOperation( boolean feedback ) {
    // delete file
    importDialog = new ImportDialog( repositoryFile, SolutionBrowserPanel.getInstance().isAdministrator() );
    importDialog.getForm().addSubmitCompleteHandler( new SubmitCompleteHandler() {
      @Override
      public void onSubmitComplete( SubmitCompleteEvent sce ) {
        new RefreshRepositoryCommand().execute( false );
      }
    } );
    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        importDialog.hide();
      }

      public void okPressed() {
        importDialog.getForm().submit();
      }
    };
    importDialog.setCallback( callback );
    importDialog.center();
  }

}
