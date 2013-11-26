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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TreeItem;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.mantle.client.dialogs.FileDialog;
import org.pentaho.mantle.client.solutionbrowser.IRepositoryFileProvider;
import org.pentaho.mantle.client.solutionbrowser.IRepositoryFileTreeListener;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper.ContentTypePlugin;
import org.pentaho.mantle.client.solutionbrowser.RepositoryFileTreeManager;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.toolbars.FilesToolbar;
import org.pentaho.mantle.client.solutionbrowser.tree.SolutionTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wseyler
 * 
 */
public class FilesListPanel extends FlowPanel implements IRepositoryFileTreeListener, IRepositoryFileProvider {
  protected String FILES_LABEL_STYLE_NAME = "filesPanelMenuLabel"; //$NON-NLS-1$

  private FlexTable filesList = new FlexTable();
  private FilesToolbar toolbar;
  private List<FileItem> selectedFileItems = new ArrayList<FileItem>();
  private boolean showHiddenFiles = false;

  public FilesListPanel() {
    super();
    // Create the toolbar
    toolbar = new FilesToolbar();
    SimplePanel toolbarWrapper = new SimplePanel();
    toolbarWrapper.add( toolbar );
    toolbarWrapper.setStyleName( "files-toolbar" ); //$NON-NLS-1$
    add( toolbarWrapper );

    SimplePanel filesListWrapper = new SimplePanel();
    FocusPanel fp = new FocusPanel( filesList ) {
      public void onBrowserEvent( Event event ) {
        if ( ( DOM.eventGetType( event ) & Event.ONKEYDOWN ) == Event.ONKEYDOWN ) {
          if ( event.getKeyCode() == KeyCodes.KEY_UP ) {
            selectPreviousItem( selectedFileItems );
          } else if ( event.getKeyCode() == KeyCodes.KEY_DOWN ) {
            selectNextItem( selectedFileItems );
          } else if ( event.getKeyCode() == KeyCodes.KEY_ENTER ) {
            SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
            FilesListPanel flp = sbp.getFilesListPanel();
            List<FileItem> items = flp.getSelectedFileItems();
            if ( items != null && items.size() == 1 ) {
              sbp.openFile( items.get( 0 ).getRepositoryFile(), COMMAND.RUN );
            }
          }
        }
        super.onBrowserEvent( event );
      }
    };
    filesList.setCellPadding( 1 );
    filesList.setWidth( "100%" );
    AbsolutePanel bounderyPanel = new AbsolutePanel();
    bounderyPanel.add( fp );

    fp.sinkEvents( Event.KEYEVENTS );

    filesListWrapper.add( bounderyPanel );
    fp.getElement().getStyle().setProperty( "margin", "29px 0px 10px 0px" ); //$NON-NLS-1$ //$NON-NLS-2$
    filesListWrapper.setStyleName( "files-list-panel" ); //$NON-NLS-1$
    add( filesListWrapper );

    setStyleName( "panelWithTitledToolbar" ); //$NON-NLS-1$  
    setWidth( "100%" ); //$NON-NLS-1$

    getElement().setId( "filesListPanel" );

    setupNativeHooks( this );
  }

  public void beforeFetchRepositoryFileTree() {
    filesList.clear();
  }

  public boolean isShowHiddenFiles() {
    return showHiddenFiles;
  }

  public void setShowHiddenFiles( boolean showHiddenFiles ) {
    this.showHiddenFiles = showHiddenFiles;
  }

  private void showOpenFileDialog( final JavaScriptObject callback, final String path, final String title,
      final String okText, final String fileTypes, final Boolean showHidden ) {
    RepositoryFileTreeManager.getInstance().fetchRepositoryFileTree( new AsyncCallback<RepositoryFileTree>() {
      public void onFailure( Throwable caught ) {
      }

      public void onSuccess( RepositoryFileTree repositoryFileTree ) {
        FileDialog dialog = new FileDialog( repositoryFileTree, path, title, okText, fileTypes.split( "," ) );
        dialog.addFileChooserListener( new FileChooserListener() {

          public void fileSelected( RepositoryFile repositoryFile, String filePath, String fileName, String title ) {
            notifyOpenFileCallback( callback, repositoryFile, filePath, fileName, title );
          }

          public void fileSelectionChanged( RepositoryFile repositoryFile, String filePath, String fileName,
              String title ) {
          }

          public void dialogCanceled() {

          }

        } );
        dialog.show();
      }
    }, false, null, null, showHidden );
  }

  private native void notifyOpenFileCallback( JavaScriptObject obj, RepositoryFile repositoryFile, String filePath,
      String fileName, String title )
  /*-{
    obj.fileSelected(repositoryFile, filePath, fileName, title);
  }-*/;

  private static native void setupNativeHooks( FilesListPanel filesListPanel )
  /*-{
    $wnd.openFileDialog = function(callback,title, okText, fileTypes) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      filesListPanel.@org.pentaho.mantle.client.solutionbrowser.filelist.FilesListPanel::showOpenFileDialog(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;)(callback, null, title, okText, fileTypes, null);      
    }
    $wnd.openFileDialogWithPath = function(callback, path, title, okText, fileTypes) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      filesListPanel.@org.pentaho.mantle.client.solutionbrowser.filelist.FilesListPanel::showOpenFileDialog(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;)(callback, path, title, okText, fileTypes, null);      
    }
  }-*/;

  public void populateFilesList( SolutionBrowserPanel perspective, SolutionTree solutionTree, TreeItem item,
      JsArrayString filters ) {
    filesList.clear();
    List<RepositoryFile> files;

    if ( item == solutionTree.getTrashItem() ) { // If we're populating from the trash then
      files = solutionTree.getTrashItems();
    } else {
      files = new ArrayList<RepositoryFile>();
      // Get the user object.
      RepositoryFileTree tree = (RepositoryFileTree) item.getUserObject();
      // Since we are only listing the files here. Get to each item of the tree and get the file from it
      for ( RepositoryFileTree treeItem : tree.getChildren() ) {
        String fileName = treeItem.getFile().getName();
        if ( filters != null ) {
          for ( int i = 0; i < filters.length(); i++ ) {
            if ( fileName.endsWith( filters.get( i ) ) ) {
              files.add( treeItem.getFile() );
            }
          }
        }
      }
    }
    // let's sort this list based on localized name
    Collections.sort( files, new RepositoryFileComparator() ); // BISERVER-9599 - Custom Sort

    if ( files != null ) {
      int rowCounter = 0;
      for ( RepositoryFile file : files ) {
        if ( ( item == solutionTree.getTrashItem() )
            || ( !file.isFolder() && ( isShowHiddenFiles() || !file.isHidden() ) ) ) {
          // TODO Currently Old solution repository stores url type files. New repository does not have that
          // concept. What do we need to do here
          //String url = fileElement.getAttribute("url"); //$NON-NLS-1$
          ContentTypePlugin plugin = PluginOptionsHelper.getContentTypePlugin( file.getName() );
          String icon = null;
          if ( plugin != null ) {
            icon = plugin.getFileIcon();
          }
          if ( item == solutionTree.getTrashItem() && file.isFolder() ) {
            icon = "mantle/images/folderIcon.png"; //$NON-NLS-1$
          }

          final FileItem fileLabel =
              new FileItem( file, this, PluginOptionsHelper.getEnabledOptions( file.getName() ), true, icon );
          // BISERVER-2317: Request for more IDs for Mantle UI elements
          // set element id as the filename
          fileLabel.getElement().setId( file.getPath() ); //$NON-NLS-1$
          fileLabel.addFileSelectionChangedListener( toolbar );
          fileLabel.setWidth( "100%" ); //$NON-NLS-1$
          try {
            perspective.getDragController().makeDraggable( fileLabel );
          } catch ( Throwable e ) {
            Throwable throwable = e;
            String text = "Uncaught exception: ";
            while ( throwable != null ) {
              StackTraceElement[] stackTraceElements = throwable.getStackTrace();
              text += throwable.toString() + "\n";
              for ( int ii = 0; ii < stackTraceElements.length; ii++ ) {
                text += "    at " + stackTraceElements[ii] + "\n";
              }
              throwable = throwable.getCause();
              if ( throwable != null ) {
                text += "Caused by: ";
              }
            }
            DialogBox dialogBox = new DialogBox( true );
            DOM.setStyleAttribute( dialogBox.getElement(), "backgroundColor", "#ABCDEF" );
            System.err.print( text );
            text = text.replaceAll( " ", "&nbsp;" );
            dialogBox.setHTML( "<pre>" + text + "</pre>" );
            dialogBox.center();
          }

          fileLabel.setRepositoryFile( file );
          filesList.setWidget( rowCounter++, 0, fileLabel );

          if ( selectedFileItems != null && selectedFileItems.size() > 0 ) {
            for ( FileItem fileItem : selectedFileItems ) {
              if ( fileItem.getRepositoryFile().equals( fileLabel.getRepositoryFile() ) ) {
                if ( file.isHidden() ) {
                  fileLabel.setStyleName( "hiddenFileLabelSelected" );
                } else {
                  fileLabel.setStyleName( "fileLabelSelected" ); //$NON-NLS-1$  
                }
                selectedFileItems.add( fileLabel );
                // if we do not break this loop, it will go forever! (we added an item)
                break;
              }
            }
          } else {
            if ( file.isHidden() ) {
              fileLabel.setStyleName( "hiddenFileLabel" ); //$NON-NLS-1$
            } else {
              fileLabel.setStyleName( "fileLabel" ); //$NON-NLS-1$  
            }
          }
        }
      }
    }
  }

  public void deselect() {
    for ( int i = 0; i < filesList.getRowCount(); i++ ) {
      FileItem item = (FileItem) filesList.getWidget( i, 0 );
      RepositoryFile file = item.getRepositoryFile();
      if ( file.isHidden() ) {
        item.setStyleName( "hiddenFileLabel" ); //$NON-NLS-1$
      } else {
        item.setStyleName( "fileLabel" ); //$NON-NLS-1$ 
      }

    }
  }

  public List<FileItem> getSelectedFileItems() {
    return selectedFileItems;
  }

  public void setSelectedFileItems( List<FileItem> fileItems ) {
    // Deselect all prior selections
    for ( FileItem fileItem : selectedFileItems ) {
      RepositoryFile file = fileItem.getRepositoryFile();
      if ( file.isHidden() ) {
        fileItem.setStyleName( "hiddenFileLabel" );
      } else {
        fileItem.setStyleName( "fileLabel" );
      }

    }
    // clear the prior selections list
    selectedFileItems.clear();
    // Add all the new items
    selectedFileItems.addAll( fileItems );
    // and make sure they're selected
    for ( FileItem fileItem : selectedFileItems ) {
      RepositoryFile file = fileItem.getRepositoryFile();
      if ( file.isHidden() ) {
        fileItem.setStyleName( "hiddenFileLabelSelected" );
      } else {
        fileItem.setStyleName( "fileLabelSelected" );
      }
    }
  }

  public void selectNextItem( List<FileItem> currentItems ) {
    if ( currentItems == null || currentItems.size() < 1 ) {
      return;
    }
    FileItem currentItem = currentItems.get( currentItems.size() - 1 );
    RepositoryFile currentRepositoryFile = currentItem.getRepositoryFile();
    int myIndex = -1;
    for ( int i = 0; i < getFileCount(); i++ ) {
      FileItem fileItem = getFileItem( i );
      if ( fileItem == currentItem ) {
        myIndex = i;
      }
    }
    if ( myIndex >= 0 && myIndex < getFileCount() - 1 ) {
      if ( currentRepositoryFile.isHidden() ) {
        currentItem.setStyleName( "hiddenFileLabel" ); //$NON-NLS-1$
      } else {
        currentItem.setStyleName( "fileLabel" ); //$NON-NLS-1$  
      }

      FileItem nextItem = getFileItem( myIndex + 1 );
      RepositoryFile nextRepositoryFile = nextItem.getRepositoryFile();
      if ( nextRepositoryFile.isHidden() ) {
        nextItem.setStyleName( "hiddenFileLabelSelected" ); //$NON-NLS-1$
      } else {
        nextItem.setStyleName( "fileLabelSelected" ); //$NON-NLS-1$  
      }

      List<FileItem> fileItems = new ArrayList<FileItem>();
      fileItems.add( nextItem );
      setSelectedFileItems( fileItems );
      nextItem.fireFileSelectionEvent();
    }
  }

  public void selectPreviousItem( List<FileItem> currentItems ) {
    if ( currentItems == null || currentItems.size() < 1 ) {
      return;
    }
    FileItem currentItem = currentItems.get( 0 );
    RepositoryFile currentRepositoryFile = currentItem.getRepositoryFile();
    int myIndex = -1;
    for ( int i = 0; i < getFileCount(); i++ ) {
      FileItem fileItem = getFileItem( i );
      if ( fileItem == currentItem ) {
        myIndex = i;
      }
    }
    if ( myIndex > 0 && myIndex < getFileCount() ) {
      if ( currentRepositoryFile.isHidden() ) {
        currentItem.setStyleName( "hiddenFileLabel" ); //$NON-NLS-1$
      } else {
        currentItem.setStyleName( "fileLabel" ); //$NON-NLS-1$  
      }
      FileItem previousItem = getFileItem( myIndex - 1 );
      RepositoryFile previousCurrentRepositoryFile = previousItem.getRepositoryFile();
      if ( previousCurrentRepositoryFile.isHidden() ) {
        previousItem.setStyleName( "hiddenFileLabelSelected" ); //$NON-NLS-1$
      } else {
        previousItem.setStyleName( "fileLabelSelected" ); //$NON-NLS-1$  
      }

      List<FileItem> fileItems = new ArrayList<FileItem>();
      fileItems.add( previousItem );
      setSelectedFileItems( fileItems );
      previousItem.fireFileSelectionEvent();
    }
  }

  public FileItem getFileItem( int index ) {
    return (FileItem) filesList.getWidget( index, 0 );
  }

  public List<FileItem> getAllFileItems() {
    List<FileItem> value = new ArrayList<FileItem>();
    for ( int i = 0; i < getFileCount(); i++ ) {
      FileItem fileItem = getFileItem( i );
      if ( fileItem != null ) {
        value.add( getFileItem( i ) );
      }
    }

    return value;
  }

  public int getFileItemIndex( FileItem fileItem ) {
    int value = -1; // default to not found
    for ( int i = 0; i < getFileCount(); i++ ) {
      FileItem testItem = getFileItem( i );
      if ( fileItem == testItem ) {
        value = i;
        break;
      }
    }
    return value;
  }

  /**
   * @return
   */
  public int getFileCount() {
    return filesList.getRowCount();
  }

  /**
   * @return
   */
  public Toolbar getToolbar() {
    return toolbar;
  }

  public void onFetchRepositoryFileTree( RepositoryFileTree fileTree, List<RepositoryFile> trashItems ) {
    // TODO Auto-generated method stub

  }

  public List<RepositoryFile> getRepositoryFiles() {
    List<FileItem> fileItems = getSelectedFileItems();
    List<RepositoryFile> values = new ArrayList<RepositoryFile>();
    if ( fileItems != null && fileItems.size() > 0 ) {
      for ( FileItem fileItem : fileItems ) {
        values.add( fileItem.getRepositoryFile() );
      }
    }
    return values;
  }

}
