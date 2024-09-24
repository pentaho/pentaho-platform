/*!
 *
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
 *
 * Copyright (c) 2002 - 2020 Hitachi Vantara. All rights reserved.
 *
 */
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
