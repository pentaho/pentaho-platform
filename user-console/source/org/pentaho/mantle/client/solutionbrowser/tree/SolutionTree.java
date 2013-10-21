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

package org.pentaho.mantle.client.solutionbrowser.tree;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.gwt.widgets.client.filechooser.TreeItemComparator;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.UserSettingsLoadedEvent;
import org.pentaho.mantle.client.events.UserSettingsLoadedEventHandler;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.IRepositoryFileProvider;
import org.pentaho.mantle.client.solutionbrowser.IRepositoryFileTreeListener;
import org.pentaho.mantle.client.solutionbrowser.RepositoryFileTreeManager;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.usersettings.JsSetting;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SolutionTree extends Tree implements IRepositoryFileTreeListener, UserSettingsLoadedEventHandler,
    IRepositoryFileProvider {
  private boolean showLocalizedFileNames = true;
  private boolean showHiddenFiles = false;
  private boolean isAdministrator = false;
  private boolean createRootNode = false;
  private boolean useDescriptionsForTooltip = false;
  public RepositoryFileTree repositoryFileTree;
  public List<RepositoryFile> trashItems;
  public FileTreeItem trashItem;

  private TreeItem selectedItem = null;
  private String selectedPath = null;

  private FocusPanel focusable = new FocusPanel();

  public SolutionTree( boolean showTrash ) {
    super();
    setAnimationEnabled( true );
    sinkEvents( Event.ONDBLCLICK );
    DOM.setElementAttribute( getElement(), "oncontextmenu", "return false;" ); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute( focusable.getElement(), "fontSize", "0" ); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute( focusable.getElement(), "position", "absolute" ); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute( focusable.getElement(), "outline", "0px" ); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute( focusable.getElement(), "width", "1px" ); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute( focusable.getElement(), "height", "1px" ); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setElementAttribute( focusable.getElement(), "hideFocus", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setIntStyleAttribute( focusable.getElement(), "zIndex", -1 ); //$NON-NLS-1$
    DOM.appendChild( getElement(), focusable.getElement() );
    DOM.sinkEvents( focusable.getElement(), Event.FOCUSEVENTS );

    this.addSelectionHandler( new SelectionHandler<TreeItem>() {

      @Override
      public void onSelection( SelectionEvent<TreeItem> event ) {
        if ( selectedItem != null ) {
          Widget treeItemWidget = selectedItem.getWidget();
          if ( treeItemWidget != null && treeItemWidget instanceof LeafItemWidget ) {
            ( (LeafItemWidget) treeItemWidget ).getParent().removeStyleName( "selected" ); //$NON-NLS-1$
          } else {
            selectedItem.removeStyleName( "selected" ); //$NON-NLS-1$
          }
        }
        selectedItem = event.getSelectedItem();
        if ( selectedItem != null ) {
          Widget treeItemWidget = selectedItem.getWidget();
          if ( selectedItem instanceof FileTreeItem ) {
            RepositoryFile repositoryFile = ( (FileTreeItem) selectedItem ).getRepositoryFile();
            if ( repositoryFile != null && repositoryFile.isHidden() && !isShowHiddenFiles() ) {
              if ( treeItemWidget != null && treeItemWidget instanceof LeafItemWidget ) {
                ( (LeafItemWidget) treeItemWidget ).getParent().removeStyleName( "hidden" ); //$NON-NLS-1$
                ( (LeafItemWidget) treeItemWidget ).getParent().addStyleName( "selected" ); //$NON-NLS-1$
              } else {
                selectedItem.addStyleName( "hidden" ); //$NON-NLS-1$
                selectedItem.addStyleName( "selected" ); //$NON-NLS-1$                
              }
            } else {
              if ( treeItemWidget != null && treeItemWidget instanceof LeafItemWidget ) {
                ( (LeafItemWidget) treeItemWidget ).getParent().addStyleName( "selected" ); //$NON-NLS-1$
              } else {
                selectedItem.addStyleName( "selected" ); //$NON-NLS-1$
              }
            }
          } else {
            if ( treeItemWidget != null && treeItemWidget instanceof LeafItemWidget ) {
              ( (LeafItemWidget) treeItemWidget ).getParent().addStyleName( "selected" ); //$NON-NLS-1$
            } else {
              selectedItem.addStyleName( "selected" ); //$NON-NLS-1$
            }
          }
        }
      }
    } );
    // By default, expanding a node does not select it. Add that in here
    this.addOpenHandler( new OpenHandler<TreeItem>() {
      public void onOpen( OpenEvent<TreeItem> event ) {
        SolutionTree.this.setSelectedItem( event.getTarget() );
        selectedItem.addStyleName( "open" );
      }
    } );

    this.addCloseHandler( new CloseHandler<TreeItem>() {
      @Override
      public void onClose( CloseEvent<TreeItem> event ) {
        event.getTarget().removeStyleName( "open" );
      }
    } );

    getElement().setId( "solutionTree" ); //$NON-NLS-1$
    getElement().getStyle().setProperty( "margin", "29px 0px 10px 0px" ); //$NON-NLS-1$ //$NON-NLS-2$

    EventBusUtil.EVENT_BUS.addHandler( UserSettingsLoadedEvent.TYPE, this );
    UserSettingsManager.getInstance().getUserSettings( new AsyncCallback<JsArray<JsSetting>>() {

      public void onSuccess( JsArray<JsSetting> settings ) {
        onUserSettingsLoaded( new UserSettingsLoadedEvent( settings ) );
      }

      public void onFailure( Throwable caught ) {
      }
    }, false );
  }

  @Override
  public void onUserSettingsLoaded( UserSettingsLoadedEvent event ) {
    JsArray<JsSetting> settings = event.getSettings();
    if ( settings != null ) {
      for ( int i = 0; i < settings.length(); i++ ) {
        JsSetting setting = settings.get( i );
        if ( IMantleUserSettingsConstants.MANTLE_SHOW_LOCALIZED_FILENAMES.equals( setting.getName() ) ) {
          boolean showLocalizedFileNames = "true".equals( setting.getName() ); //$NON-NLS-1$
          setShowLocalizedFileNames( showLocalizedFileNames );
        } else if ( IMantleUserSettingsConstants.MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS.equals( setting.getName() ) ) {
          boolean useDescriptions = "true".equals( setting.getValue() ); //$NON-NLS-1$
          setUseDescriptionsForTooltip( useDescriptions );
        } else if ( IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES.equals( setting.getName() ) ) {
          boolean showHiddenFiles = "true".equals( setting.getValue() ); //$NON-NLS-1$
          setShowHiddenFiles( showHiddenFiles );
        }
      }
    }
    RepositoryFileTreeManager.getInstance().addRepositoryFileTreeListener( this, null, null, showHiddenFiles );
  }

  public void onBrowserEvent( Event event ) {
    int eventType = DOM.eventGetType( event );
    switch ( eventType ) {
      case Event.ONMOUSEDOWN:
        if ( DOM.eventGetButton( event ) == NativeEvent.BUTTON_RIGHT ) {
          TreeItem selectedItem = findSelectedItem( null, event.getClientX(), event.getClientY() );
          if ( selectedItem != null ) {
            setSelectedItem( selectedItem );
          }
        }
        break;
      case Event.ONMOUSEUP:
        break;
      case Event.ONCLICK:
        try {
          int[] scrollOffsets = ElementUtils.calculateScrollOffsets( getElement() );
          int[] offsets = ElementUtils.calculateOffsets( getElement() );
          DOM.setStyleAttribute( focusable.getElement(),
              "top", ( event.getClientY() + scrollOffsets[1] - offsets[1] ) + "px" ); //$NON-NLS-1$ //$NON-NLS-2$
        } catch ( Exception ignored ) {
          // ignore any exceptions fired by this. Most likely a result of the element
          // not being on the DOM
        }
        break;
    }

    try {

      if ( DOM.eventGetType( event ) == Event.ONDBLCLICK ) {
        getSelectedItem().setState( !getSelectedItem().getState(), true );
      } else {
        super.onBrowserEvent( event );
      }
    } catch ( Throwable t ) {
      // death to this browser event
    }
    TreeItem selItem = getSelectedItem();
    if ( selItem != null ) {
      DOM.scrollIntoView( selItem.getElement() );
    }
  }

  private TreeItem findSelectedItem( TreeItem item, int x, int y ) {
    if ( item == null ) {
      for ( int i = 0; i < getItemCount(); i++ ) {
        TreeItem selected = findSelectedItem( getItem( i ), x, y );
        if ( selected != null ) {
          return selected;
        }
      }
      return null;
    }

    for ( int i = 0; i < item.getChildCount(); i++ ) {
      TreeItem selected = findSelectedItem( item.getChild( i ), x, y );
      if ( selected != null ) {
        return selected;
      }
    }

    if ( x >= item.getAbsoluteLeft() && x <= item.getAbsoluteLeft() + item.getOffsetWidth()
        && y >= item.getAbsoluteTop() && y <= item.getAbsoluteTop() + item.getOffsetHeight() ) {
      return item;
    }
    return null;
  }

  protected void onLoad() {
    super.onLoad();
    fixLeafNodes();
    if ( trashItem != null ) {
      try {
        DOM.setStyleAttribute( trashItem.getElement(), "paddingLeft", "0px" ); //$NON-NLS-1$//$NON-NLS-2$
      } catch ( NullPointerException e ) {
        // This is sometimes thrown because the dom does not yet contain the trash items or the leaf nodes.
      }
    }
  }

  public void beforeFetchRepositoryFileTree() {
    WaitPopup.getInstance().setVisible( true );
    if ( getSelectedItem() != null ) {
      selectedItem = getSelectedItem();
    }
    clear();
    addItem( new FileTreeItem( Messages.getString( "loadingEllipsis" ) ) ); //$NON-NLS-1$
    WaitPopup.getInstance().setVisible( false );
  }

  public void onFetchRepositoryFileTree( RepositoryFileTree fileTree, List<RepositoryFile> repositoryTrashItems ) {

    if ( fileTree == null ) {
      WaitPopup.getInstance().setVisible( false );
      return;
    }
    repositoryFileTree = fileTree;
    trashItems = repositoryTrashItems;
    // remember selectedItem, so we can reselect it after the tree is loaded
    clear();
    // get document root item
    RepositoryFile rootRepositoryFile = repositoryFileTree.getFile();
    if ( !rootRepositoryFile.isHidden() || isShowHiddenFiles() ) {
      FileTreeItem rootItem = null;
      if ( createRootNode ) {
        rootItem = new FileTreeItem();
        rootItem.setText( rootRepositoryFile.getPath() );
        rootItem.setTitle( rootRepositoryFile.getPath() );
        rootItem.getElement().setId( rootRepositoryFile.getPath() );
        // added so we can traverse the true names
        rootItem.setFileName( "/" ); //$NON-NLS-1$
        rootItem.setRepositoryFile( rootRepositoryFile );
        addItem( rootItem );
        buildSolutionTree( rootItem, repositoryFileTree );
      } else {
        buildSolutionTree( null, repositoryFileTree );
        // sort the root elements
        ArrayList<TreeItem> roots = new ArrayList<TreeItem>();
        for ( int i = 0; i < getItemCount(); i++ ) {
          roots.add( getItem( i ) );
        }
        Collections.sort( roots, new TreeItemComparator() ); // BISERVER-9599 - Custom Sort
        clear();
        for ( TreeItem myRootItem : roots ) {
          addItem( myRootItem );
        }
      }
    }
    fixLeafNodes();

    if ( selectedPath != null ) {
      select( selectedPath );
    } else if ( selectedItem != null ) {
      ArrayList<TreeItem> parents = new ArrayList<TreeItem>();
      while ( selectedItem != null ) {
        parents.add( selectedItem );
        selectedItem = selectedItem.getParentItem();
      }
      Collections.reverse( parents );
      selectFromList( parents );
    } else {
      for ( int i = 0; i < getItemCount(); i++ ) {
        getItem( i ).setState( true );
      }
    }
    WaitPopup.getInstance().setVisible( false );
  }

  /**
   * 
   */
  private void fixLeafNodes() {
    List<FileTreeItem> allNodes = getAllNodes();
    for ( FileTreeItem treeItem : allNodes ) {
      RepositoryFileTree userObject = (RepositoryFileTree) treeItem.getUserObject();
      if ( userObject != null && userObject.getChildren().size() == 0 ) { // This is a leaf node so change the
                                                                          // widget
        treeItem
            .setWidget( new LeafItemWidget( treeItem.getText(), "icon-tree-node", "icon-tree-leaf", "icon-folder" ) ); //$NON-NLS-1$
      } else {
        treeItem.setWidget( new LeafItemWidget( treeItem.getText(), "icon-tree-node", "icon-folder" ) ); //$NON-NLS-1$
      }

      DOM.setStyleAttribute( treeItem.getElement(), "paddingLeft", "0px" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public List<FileTreeItem> getAllNodes() {
    ArrayList<FileTreeItem> nodeList = new ArrayList<FileTreeItem>();
    for ( int i = 0; i < this.getItemCount(); i++ ) {
      nodeList.add( (FileTreeItem) this.getItem( i ) );
      getAllNodes( (FileTreeItem) this.getItem( i ), nodeList );
    }
    return nodeList;
  }

  private void getAllNodes( FileTreeItem parent, ArrayList<FileTreeItem> nodeList ) {
    for ( int i = 0; i < parent.getChildCount(); i++ ) {
      FileTreeItem child = (FileTreeItem) parent.getChild( i );
      nodeList.add( child );
      getAllNodes( child, nodeList );
    }
  }

  public FileTreeItem getTrashItem() {
    return trashItem;
  }

  public List<RepositoryFile> getTrashItems() {
    return trashItems;
  }

  public void select( String path ) {
    this.selectedPath = path;
    ArrayList<String> pathSegments = new ArrayList<String>();
    if ( path != null ) {
      if ( path.startsWith( "/" ) ) { //$NON-NLS-1$
        path = path.substring( 1 );
      }
      StringTokenizer st = new StringTokenizer( path, '/' );
      for ( int i = 0; i < st.countTokens(); i++ ) {
        String token = st.tokenAt( i );
        pathSegments.add( token );
      }
    }
    TreeItem item = getTreeItem( pathSegments );
    selectedItem = item;
    ArrayList<TreeItem> parents = new ArrayList<TreeItem>();
    if ( item != null ) {
      this.setSelectedItem( item, false );
      parents.add( item );
      item = item.getParentItem();
      while ( item != null ) {
        parents.add( item );
        item = item.getParentItem();
      }
      Collections.reverse( parents );
      selectFromList( parents );
      // this.setSelectedItem(selectedItem, false);
      // selectedItem.setSelected(true);
    }
  }

  public ArrayList<String> getPathSegments( String path ) {
    ArrayList<String> pathSegments = new ArrayList<String>();
    if ( path != null ) {
      if ( path.startsWith( "/" ) ) { //$NON-NLS-1$
        path = path.substring( 1 );
      }
      StringTokenizer st = new StringTokenizer( path, '/' );
      for ( int i = 0; i < st.countTokens(); i++ ) {
        pathSegments.add( st.tokenAt( i ) );
      }
    }
    return pathSegments;
  }

  public FileTreeItem getTreeItem( final ArrayList<String> pathSegments ) {
    if ( pathSegments.size() > 0 ) {
      // the first path segment is going to be a 'root' in the tree
      String rootSegment = pathSegments.get( 0 );
      for ( int i = 0; i < getItemCount(); i++ ) {
        FileTreeItem root = (FileTreeItem) getItem( i );
        if ( root.getFileName().equalsIgnoreCase( rootSegment ) ) {
          @SuppressWarnings( "unchecked" )
          ArrayList<String> tmpPathSegs = (ArrayList<String>) pathSegments.clone();
          tmpPathSegs.remove( 0 );
          return getTreeItem( root, tmpPathSegs );
        }
      }
    }
    return null;
  }

  private FileTreeItem getTreeItem( final FileTreeItem root, final ArrayList<String> pathSegments ) {
    int depth = 0;
    FileTreeItem currentItem = root;
    while ( depth < pathSegments.size() ) {
      String pathSegment = pathSegments.get( depth );
      for ( int i = 0; i < currentItem.getChildCount(); i++ ) {
        FileTreeItem childItem = (FileTreeItem) currentItem.getChild( i );
        if ( childItem.getFileName().equalsIgnoreCase( pathSegment ) ) {
          currentItem = childItem;
        }
      }
      depth++;
    }
    // let's check if the currentItem matches our segments (it might point to the last item before
    // we eventually failed to find the complete match)
    FileTreeItem tmpItem = currentItem;
    depth = pathSegments.size() - 1;
    while ( tmpItem != null && depth >= 0 ) {
      if ( tmpItem.getFileName().equalsIgnoreCase( pathSegments.get( depth ) ) ) {
        tmpItem = (FileTreeItem) tmpItem.getParentItem();
        depth--;
      } else {
        // every item must match
        return null;
      }
    }

    return currentItem;
  }

  private void selectFromList( ArrayList<TreeItem> parents ) {
    TreeItem pathDown = null;
    for ( int i = 0; i < parents.size(); i++ ) {
      TreeItem parent = parents.get( i );
      if ( pathDown == null ) {
        for ( int j = 0; j < getItemCount(); j++ ) {
          TreeItem possibleItem = getItem( j );
          if ( ( possibleItem instanceof FileTreeItem ) && ( parent instanceof FileTreeItem )
              && ( (FileTreeItem) parent ).getFileName().equals( ( (FileTreeItem) possibleItem ).getFileName() ) ) {
            pathDown = possibleItem;
            pathDown.setState( true, true );
            pathDown.setSelected( true );
            break;
          }
        }
      } else {
        for ( int j = 0; j < pathDown.getChildCount(); j++ ) {
          TreeItem possibleItem = pathDown.getChild( j );
          if ( ( possibleItem instanceof FileTreeItem ) && ( parent instanceof FileTreeItem )
              && ( (FileTreeItem) parent ).getFileName().equals( ( (FileTreeItem) possibleItem ).getFileName() ) ) {
            pathDown = possibleItem;
            pathDown.setState( true, true );
            break;
          }
        }
      }
    }
    if ( pathDown != null ) {
      setSelectedItem( pathDown );
      pathDown.setState( true, true );
    }
  }

  private void buildSolutionTree( FileTreeItem parentTreeItem, RepositoryFileTree repositoryFileTree ) {
    List<RepositoryFileTree> children = repositoryFileTree.getChildren();

    // BISERVER-9599 - Custom Sort
    Collections.sort( children, new Comparator<RepositoryFileTree>() {
      @Override
      public int compare( RepositoryFileTree repositoryFileTree, RepositoryFileTree repositoryFileTree2 ) {
        return ( new TreeItemComparator() ).compare( repositoryFileTree.getFile().getTitle(), repositoryFileTree2
            .getFile().getTitle() );
      }
    } );

    for ( RepositoryFileTree treeItem : children ) {
      RepositoryFile file = treeItem.getFile();
      boolean isDirectory = file.isFolder();
      String fileName = file.getName();
      if ( ( !file.isHidden() || isShowHiddenFiles() ) && !StringUtils.isEmpty( fileName ) ) {

        // TODO Mapping Title to LocalizedName
        String localizedName = file.getTitle();
        String description = file.getDescription();
        FileTreeItem childTreeItem = new FileTreeItem();
        childTreeItem.setStylePrimaryName( "leaf-widget" );
        childTreeItem.getElement().setAttribute( "id", file.getPath() ); //$NON-NLS-1$
        childTreeItem.setUserObject( treeItem );
        childTreeItem.setRepositoryFile( file );
        if ( file.isHidden() && file.isFolder() ) {
          childTreeItem.addStyleDependentName( "hidden" );
        }

        if ( treeItem != null && treeItem.getChildren() != null ) {
          for ( RepositoryFileTree childItem : treeItem.getChildren() ) {
            if ( childItem.getFile().isFolder() ) {
              childTreeItem.addStyleName( "parent-widget" );
              break;
            }
          }
        }

        ElementUtils.killAllTextSelection( childTreeItem.getElement() );
        childTreeItem.setURL( fileName );
        if ( showLocalizedFileNames ) {
          childTreeItem.setText( localizedName );
          if ( isUseDescriptionsForTooltip() && !StringUtils.isEmpty( description ) ) {
            childTreeItem.setTitle( description );
          } else {
            childTreeItem.setTitle( fileName );
          }
        } else {
          childTreeItem.setText( fileName );
          if ( isUseDescriptionsForTooltip() && !StringUtils.isEmpty( description ) ) {
            childTreeItem.setTitle( description );
          } else {
            childTreeItem.setTitle( localizedName );
          }
        }
        childTreeItem.setFileName( fileName );
        if ( parentTreeItem == null && isDirectory ) {
          addItem( childTreeItem );
        } else {
          parentTreeItem.addItem( childTreeItem );
        }
        FileTreeItem tmpParent = childTreeItem;
        String pathToChild = tmpParent.getFileName();
        while ( tmpParent.getParentItem() != null ) {
          tmpParent = (FileTreeItem) tmpParent.getParentItem();
          pathToChild = tmpParent.getFileName() + "/" + pathToChild; //$NON-NLS-1$
        }
        /*
         * TODO Not sure what to do here if (parentTreeItem != null) { ArrayList<FileChooserRepositoryFile> files =
         * (ArrayList<FileChooserRepositoryFile>) parentTreeItem.getUserObject(); if (files == null) { files = new
         * ArrayList<FileChooserRepositoryFile>(); parentTreeItem.setUserObject(files); } files.add(file); }
         */
        if ( isDirectory ) {
          buildSolutionTree( childTreeItem, treeItem );
        } else {
          if ( parentTreeItem != null ) {
            parentTreeItem.removeItem( childTreeItem );
          }
        }
      }
    }
  }

  public void setShowLocalizedFileNames( boolean showLocalizedFileNames ) {
    this.showLocalizedFileNames = showLocalizedFileNames;
    // use existing tree and switch text/title
    for ( int i = 0; i < getItemCount(); i++ ) {
      toggleLocalizedFileNames( (FileTreeItem) getItem( i ) );
    }
  }

  private void toggleLocalizedFileNames( FileTreeItem parentTreeItem ) {
    String title = parentTreeItem.getTitle();
    String text = parentTreeItem.getText();
    parentTreeItem.setTitle( text );
    parentTreeItem.setText( title );
    for ( int i = 0; i < parentTreeItem.getChildCount(); i++ ) {
      toggleLocalizedFileNames( (FileTreeItem) parentTreeItem.getChild( i ) );
    }
  }

  public boolean isShowHiddenFiles() {
    return showHiddenFiles;
  }

  public void setShowHiddenFiles( boolean showHiddenFiles ) {
    this.showHiddenFiles = showHiddenFiles;
  }

  public boolean isShowLocalizedFileNames() {
    return showLocalizedFileNames;
  }

  public boolean isUseDescriptionsForTooltip() {
    return useDescriptionsForTooltip;
  }

  public void setUseDescriptionsForTooltip( boolean useDescriptionsForTooltip ) {
    this.useDescriptionsForTooltip = useDescriptionsForTooltip;
    onFetchRepositoryFileTree( repositoryFileTree, trashItems );
  }

  public boolean isAdministrator() {
    return isAdministrator;
  }

  public void setAdministrator( boolean isAdministrator ) {
    this.isAdministrator = isAdministrator;
  }

  public boolean isCreateRootNode() {
    return createRootNode;
  }

  public List<RepositoryFile> getRepositoryFiles() {
    final FileTreeItem selectedTreeItem = (FileTreeItem) getSelectedItem();
    List<RepositoryFile> values = new ArrayList<RepositoryFile>();
    values.add( ( (RepositoryFileTree) selectedTreeItem.getUserObject() ).getFile() );
    return values;
  }

}
