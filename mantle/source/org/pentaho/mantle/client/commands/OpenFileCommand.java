/*
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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.mantle.client.solutionbrowser.RepositoryFileTreeManager;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class OpenFileCommand extends AbstractCommand {

  private static String lastPath = "/"; //$NON-NLS-1$

  private FileCommand.COMMAND openMethod = FileCommand.COMMAND.RUN;

  public OpenFileCommand() {
  }

  public OpenFileCommand(final FileCommand.COMMAND openMethod) {
    this.openMethod = openMethod;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    final SolutionBrowserPerspective solutionBrowserPerspective = SolutionBrowserPerspective.getInstance();

    RepositoryFileTreeManager.getInstance().fetchRepositoryFileTree(new AsyncCallback<RepositoryFileTree>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(RepositoryFileTree tree) {
        // TODO Uncomment the line below and delete the line after that once gwtwidets have been branched
        final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.OPEN, lastPath, tree, false, true);
        dialog.addFileChooserListener(new FileChooserListener() {

          public void dialogCanceled(){

          }

          @Override
          public void fileSelected(RepositoryFile repositoryFile, String filePath, String fileName, String title) {
            dialog.hide();
            solutionBrowserPerspective.openFile(filePath, fileName, openMethod);
          }

          @Override
          public void fileSelectionChanged(RepositoryFile repositoryFile, String filePath, String fileName, String title) {
            // TODO Auto-generated method stub
            
          }
        });
        dialog.center();
      }
    }, false);
  }
}
