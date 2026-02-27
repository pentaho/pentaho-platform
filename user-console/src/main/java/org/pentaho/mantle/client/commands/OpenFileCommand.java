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

import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;

public class OpenFileCommand extends AbstractCommand {

  private static String lastPath = "/";
  private static final String spinnerId = "OpenCommand";

  private FileCommand.COMMAND openMethod = FileCommand.COMMAND.RUN;

  public OpenFileCommand() {
  }

  public OpenFileCommand( final FileCommand.COMMAND openMethod ) {
    this.openMethod = openMethod;
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    final IPluginPerspective activePerspective = PerspectiveManager.getInstance().getActivePerspective();
    final SolutionBrowserPanel solutionBrowserPerspective = SolutionBrowserPanel.getInstance();

    final boolean showHiddenFiles = solutionBrowserPerspective.getSolutionTree().isShowHiddenFiles();
    final FileChooserDialog dialog = new FileChooserDialog( FileChooserMode.OPEN,
      lastPath, null, false, true, showHiddenFiles );

    dialog.setSubmitOnEnter( MantleApplication.submitOnEnter );
    dialog.addFileChooserListener( new FileChooserListener() {

      public void dialogCanceled() {
        // retain current active perspective
        PerspectiveManager.getInstance().setPerspective( activePerspective.getId() );
      }

      @Override
      public void fileSelected( RepositoryFile repositoryFile, String filePath, String fileName, String title ) {
        dialog.hide();
        solutionBrowserPerspective.openFile( repositoryFile, openMethod );
      }

      @Override
      public void fileSelectionChanged( RepositoryFile repositoryFile, String filePath, String fileName,
          String title ) {
        // TODO Auto-generated method stub
      }
    } );
  }
}
