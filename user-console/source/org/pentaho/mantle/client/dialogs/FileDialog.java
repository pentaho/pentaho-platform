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

package org.pentaho.mantle.client.dialogs;

import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.FileFilter;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import java.util.ArrayList;

/*
 * Convenience class for showing FileChooserDialog while maintaining a last browsed location.
 * 
 */
public class FileDialog {

  private static String lastPath = "";
  private ArrayList<FileChooserListener> listeners = new ArrayList<FileChooserListener>();
  private RepositoryFileTree repositoryFileTree;
  private String title, okText;
  private String[] fileTypes;
  private String path;

  public FileDialog( RepositoryFileTree repositoryFileTree, String title, String okText, String[] fileTypes ) {
    this.repositoryFileTree = repositoryFileTree;
    this.title = title;
    this.okText = okText;
    this.fileTypes = fileTypes;
  }

  public FileDialog( RepositoryFileTree repositoryFileTree, String path,
                     String title, String okText, String[] fileTypes ) {
    this( repositoryFileTree, title, okText, fileTypes );
    this.path = path;
  }

  public void show() {
    String pathToShow = ( path != null ) ? path : FileDialog.lastPath;

    final SolutionBrowserPanel solutionBrowserPerspective = SolutionBrowserPanel.getInstance();

    final FileChooserDialog dialog =
        new FileChooserDialog( FileChooserMode.OPEN, pathToShow, repositoryFileTree, false, true, title, okText,
            solutionBrowserPerspective.getSolutionTree().isShowHiddenFiles() ) {

          @Override
          public void hide() {
            super.hide();
            GlassPane.getInstance().hide();
          }

        };
    dialog.setSubmitOnEnter( MantleApplication.submitOnEnter );
    dialog.addFileChooserListener( new FileChooserListener() {

      public void fileSelected( RepositoryFile file, String filePath, String fileName, String title ) {
        dialog.hide();
        for ( FileChooserListener listener : listeners ) {
          listener.fileSelected( file, filePath, fileName, title );
        }
      }

      public void fileSelectionChanged( RepositoryFile file, String filePath, String fileName, String title ) {
      }

      public void dialogCanceled() {

      }
    } );
    dialog.setFileFilter( new FileFilter() {

      public boolean accept( String name, boolean isDirectory, boolean isVisible ) {
        if ( isDirectory && isVisible ) {
          return true;
        }
        if ( name.indexOf( "." ) == -1 ) {
          return false;
        }
        String extension = name.substring( name.lastIndexOf( "." ) + 1 );

        for ( int i = 0; i < fileTypes.length; i++ ) {
          if ( fileTypes[i].trim().equalsIgnoreCase( extension ) && isVisible ) {
            return true;
          }
        }
        return false;
      }

    } );

    GlassPane.getInstance().show();
    dialog.center();
  }

  public void addFileChooserListener( FileChooserListener listener ) {
    if ( !listeners.contains( listeners ) ) {
      listeners.add( listener );
    }
  }
}
