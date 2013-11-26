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
    importDialog = new ImportDialog( repositoryFile );
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
