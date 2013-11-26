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

import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.mantle.client.images.ImageUtil;
import org.pentaho.mantle.client.solutionbrowser.FileTypeEnabledOptions;
import org.pentaho.mantle.client.solutionbrowser.IFileSummary;
import org.pentaho.mantle.client.solutionbrowser.MantlePopupPanel;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FileItem extends FlexTable implements HasAllMouseHandlers, IFileSummary {

  public static final String ANALYSIS_VIEW_SUFFIX = ".analysisview.xaction"; //$NON-NLS-1$
  public static final String XACTION_SUFFIX = ".xaction"; //$NON-NLS-1$
  public static final String URL_SUFFIX = ".url"; //$NON-NLS-1$

  // by creating a single popupMenu, we're reducing total # of widgets used
  // and we can be sure to hide any existing ones by calling hide
  static PopupPanel popupMenu = new MantlePopupPanel( true );

  private Label fileLabel = new Label();
  private FilesListPanel filesListPanel;
  private RepositoryFile repositoryFile;
  private ArrayList<IFileItemListener> listeners = new ArrayList<IFileItemListener>();
  private FileTypeEnabledOptions options;
  private String url;
  private String iconStr;
  private Image dropIndicator = ImageUtil.getThemeableImage( "icon-small", "icon-drop-invalid" );
  private boolean canDrop = false;

  public FileItem( RepositoryFile repositoryFile, FilesListPanel filesListPanel, FileTypeEnabledOptions options,
      boolean supportsACLs, String fileIconStr ) {

    this.filesListPanel = filesListPanel;
    this.iconStr = fileIconStr;

    sinkEvents( Event.ONCLICK | Event.ONDBLCLICK | Event.ONMOUSEUP );
    DOM.setElementAttribute( getElement(), "oncontextmenu", "return false;" ); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setElementAttribute( popupMenu.getElement(), "oncontextmenu", "return false;" ); //$NON-NLS-1$ //$NON-NLS-2$

    fileLabel.setWordWrap( false );
    fileLabel.setText( repositoryFile.getTitle() );
    String description = repositoryFile.getDescription();
    if ( description == null ) {
      description = repositoryFile.getTitle();
    }
    fileLabel.setTitle( description );
    if ( repositoryFile.isHidden() ) {
      setStyleName( "hiddenFileLabel" ); //$NON-NLS-1$
    } else {
      setStyleName( "fileLabel" ); //$NON-NLS-1$
    }

    setCellPadding( 0 );
    setCellSpacing( 0 );
    ElementUtils.preventTextSelection( fileLabel.getElement() );
    String name = repositoryFile.getName();
    Image fileIcon = new Image();
    if ( fileIconStr != null ) {
      fileIcon.setUrl( fileIconStr );
    } else if ( name.endsWith( ANALYSIS_VIEW_SUFFIX ) ) {
      fileIcon = ImageUtil.getThemeableImage( "icon-small", "icon-analysis" );
    } else if ( name.endsWith( XACTION_SUFFIX ) ) {
      fileIcon = ImageUtil.getThemeableImage( "icon-small", "icon-xaction" );
    } else if ( name.endsWith( URL_SUFFIX ) ) {
      fileIcon = ImageUtil.getThemeableImage( "icon-small", "icon-url" );
    } else {
      fileIcon = ImageUtil.getThemeableImage( "icon-small", "icon-file" );
    }
    fileIcon.setWidth( "16px" ); //$NON-NLS-1$
    fileLabel.setWidth( "100%" ); //$NON-NLS-1$
    setWidget( 0, 0, fileIcon );
    getCellFormatter().setWidth( 0, 0, "16px" ); //$NON-NLS-1$
    setWidget( 0, 1, fileLabel );
    getCellFormatter().setWidth( 0, 1, "100%" ); //$NON-NLS-1$
    this.options = options;
    this.repositoryFile = repositoryFile;
  }

  public void select() {
    if ( filesListPanel.getSelectedFileItems().contains( this ) ) {
      return;
    } else {
      if ( this.getStyleName().equalsIgnoreCase( "fileLabelCut" ) ) {
        this.setStyleName( "fileLabelCutSelected" );
      } else {
        if ( repositoryFile.isHidden() ) {
          this.setStyleName( "hiddenFileLabelSelected" ); //$NON-NLS-1$
        } else {
          this.setStyleName( "fileLabelSelected" ); //$NON-NLS-1$
        }
      }
      filesListPanel.getSelectedFileItems().add( this ); // and add it to the list of selected files.
    }
  }

  public void deselect() {
    if ( !filesListPanel.getSelectedFileItems().contains( this ) ) {
      return;
    } else {
      if ( this.getStyleName().equalsIgnoreCase( "fileLabelCutSelected" ) ) {
        this.setStyleName( "fileLabelCut" );
      } else {
        if ( repositoryFile.isHidden() ) {
          this.setStyleName( "hiddenFileLabel" ); //$NON-NLS-1$
        } else {
          this.setStyleName( "fileLabel" ); //$NON-NLS-1$
        }
      }
      filesListPanel.getSelectedFileItems().remove( this ); // Remove it from the selected list
    }
  }

  public void toggleSelect( Boolean addSelection, Boolean extendSelection ) {
    if ( extendSelection ) {
      extendSelection();
      return;
    }
    if ( addSelection ) {
      Boolean isSelected = filesListPanel.getSelectedFileItems().contains( this );
      if ( isSelected ) { // then toggle it to not selected
        deselect();
      } else {
        select();
      }
    } else {
      for ( FileItem fileItem : filesListPanel.getAllFileItems() ) { // Set all the file Items to a unselected
                                                                     // style
        fileItem.deselect();
      }
      filesListPanel.getSelectedFileItems().clear(); // Remove all the files from the selected list
      select();
    }
  }

  private void extendSelection() {
    if ( filesListPanel.getSelectedFileItems() == null
      || filesListPanel.getSelectedFileItems().size() < 1 ) { // nothing is selected so do a simple select
      toggleSelect( false, false );
    } else {
      int currentItemIndex = filesListPanel.getFileItemIndex( this );
      int maxSelectedIndex = -1;
      int minSelectedIndex = filesListPanel.getFileCount() - 1;

      for ( int i = 0; i < filesListPanel.getSelectedFileItems().size(); i++ ) {
        FileItem testItem = filesListPanel.getSelectedFileItems().get( i );
        int textIdx = filesListPanel.getFileItemIndex( testItem );
        maxSelectedIndex = Math.max( maxSelectedIndex, textIdx );
        minSelectedIndex = Math.min( minSelectedIndex, textIdx );
      }

      boolean forwardSelect = currentItemIndex > maxSelectedIndex;
      int startIdx;
      int endIdx;
      if ( forwardSelect ) {
        startIdx = maxSelectedIndex + 1;
        endIdx = currentItemIndex;
      } else {
        startIdx = currentItemIndex;
        endIdx = minSelectedIndex - 1;
      }
      for ( int i = startIdx; i <= endIdx; i++ ) {
        filesListPanel.getFileItem( i ).toggleSelect( true, false );
      }
    }
  }

  public void onBrowserEvent( Event event ) {
    Boolean metaKeyDown = DOM.eventGetMetaKey( event ) || DOM.eventGetCtrlKey( event );
    Boolean shiftKeyDown = DOM.eventGetShiftKey( event );
    if ( ( DOM.eventGetType( event ) & Event.ONDBLCLICK ) == Event.ONDBLCLICK ) {
      toggleSelect( false, false );
      FileItem selectedFileItem = filesListPanel.getSelectedFileItems().get( 0 );
      if ( !selectedFileItem.isInTrash() ) {
        SolutionBrowserPanel.getInstance().openFile(
            filesListPanel.getSelectedFileItems().get( 0 ).getRepositoryFile(), COMMAND.RUN );
      }
    } else if ( ( DOM.eventGetType( event ) & Event.ONCLICK ) == Event.ONCLICK ) {
      toggleSelect( metaKeyDown, shiftKeyDown );
      fireFileSelectionEvent();
    }
    super.onBrowserEvent( event );
  }

  public boolean isCommandEnabled( COMMAND command, HashMap<String, String> metadataPerms ) {
    return options != null && options.isCommandEnabled( command, metadataPerms );
  }

  public String getName() {
    return this.repositoryFile.getName();
  }

  public void setName( String name ) {
    this.repositoryFile.setName( name );
  }

  public Date getLastModifiedDate() {
    return this.repositoryFile.getLastModifiedDate();
  }

  public void setLastModifiedDate( Date lastModifiedDate ) {
    this.repositoryFile.setLastModifiedDate( lastModifiedDate );
  }

  // TODO LocalizedName and NonLocalizedName is the same. Do we need this extra method in there

  public String getLocalizedName() {
    return repositoryFile.getTitle();
  }

  public void setLocalizedName( String localizedName ) {
    this.repositoryFile.setTitle( localizedName );
  }

  public RepositoryFile getRepositoryFile() {
    return repositoryFile;
  }

  public void setRepositoryFile( RepositoryFile repositoryFile ) {
    this.repositoryFile = repositoryFile;
  }

  public void fireFileSelectionEvent() {
    for ( IFileItemListener listener : listeners ) {
      listener.itemSelected( this );
    }
  }

  public void addFileSelectionChangedListener( IFileItemListener listener ) {
    listeners.add( listener );
  }

  public void removeFileSelectionChangedListener( IFileItemListener listener ) {
    listeners.remove( listener );
  }

  public String getPath() {
    return this.repositoryFile.getPath();
  }

  public String getURL() {
    return url;
  }

  public void setURL( String url ) {
    this.url = url;
  }

  public String getIcon() {
    return this.iconStr;
  }

  /**
   * DND required methods below
   */
  public HandlerRegistration addMouseUpHandler( MouseUpHandler handler ) {
    return addDomHandler( handler, MouseUpEvent.getType() );
  }

  public HandlerRegistration addMouseOutHandler( MouseOutHandler handler ) {
    return addDomHandler( handler, MouseOutEvent.getType() );
  }

  public HandlerRegistration addMouseMoveHandler( MouseMoveHandler handler ) {
    return addDomHandler( handler, MouseMoveEvent.getType() );
  }

  public HandlerRegistration addMouseWheelHandler( MouseWheelHandler handler ) {
    return addDomHandler( handler, MouseWheelEvent.getType() );
  }

  public HandlerRegistration addMouseOverHandler( MouseOverHandler handler ) {
    return addDomHandler( handler, MouseOverEvent.getType() );
  }

  public HandlerRegistration addMouseDownHandler( MouseDownHandler handler ) {
    return addDomHandler( handler, MouseDownEvent.getType() );
  }

  public FileItem makeDragProxy() {
    FileItem f = new FileItem( getRepositoryFile(), filesListPanel, options, false, getIcon() );
    f.enableDrag();
    return f;
  }

  public void enableDrag() {
    setWidget( 0, 0, dropIndicator );
    addStyleName( "fileItemDragProxy" ); //$NON-NLS-1$
    setDroppable( false );
    dropIndicator.removeStyleName( "icon-drop-valid" );
    dropIndicator.addStyleName( "icon-drop-invalid" );

  }

  public void setDroppable( boolean canDrop ) {
    if ( this.canDrop == canDrop ) {
      return;
    }
    if ( canDrop ) {

      dropIndicator.addStyleName( "icon-drop-valid" );
      dropIndicator.removeStyleName( "icon-drop-invalid" );
      addStyleName( "validDrop" ); //$NON-NLS-1$
    } else {
      dropIndicator.removeStyleName( "icon-drop-valid" );
      dropIndicator.addStyleName( "icon-drop-invalid" );
      removeStyleName( "validDrop" ); //$NON-NLS-1$
    }

    this.canDrop = canDrop;
  }

  public boolean isInTrash() {
    return repositoryFile.getPath().contains( "/.trash/pho:" );
  }

}
