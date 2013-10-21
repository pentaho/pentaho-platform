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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.mantle.client.commands.NewFolderCommand;
import org.pentaho.mantle.client.images.ImageUtil;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.tree.FileTreeItem;
import org.pentaho.mantle.client.solutionbrowser.tree.SolutionTree;

public class SelectFolderDialog extends PromptDialogBox {

  private static class MySolutionTree extends SolutionTree {
    public SelectFolderDialog localThis;

    public MySolutionTree( boolean showTrash ) {
      super( showTrash );
      super.setScrollOnSelectEnabled( false );
    }

    public void onBrowserEvent( Event event ) {
      try {
        if ( DOM.eventGetType( event ) == Event.ONDBLCLICK && getSelectedItem().getChildCount() == 0 ) {
          localThis.onOk();
          event.stopPropagation();
          event.preventDefault();
        } else {
          super.onBrowserEvent( event );
        }
      } catch ( Throwable t ) {
        // Window.alert(t);
      }
    };
  }

  private static MySolutionTree tree = new MySolutionTree( false );

  public SelectFolderDialog() {
    super(
        Messages.getString( "selectFolder" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    if ( tree == null ) {
      tree = new MySolutionTree( false );
    }
    tree.localThis = this;

    SimplePanel treeWrapper = new SimplePanel( tree );
    treeWrapper.getElement().addClassName( "select-folder-tree" );
    tree.getElement().getStyle().setMargin( 0d, Unit.PX );

    Toolbar bar = new Toolbar();
    bar.setStyleName( "select-folder-toolbar" );
    bar.add( new Label( Messages.getString( "newFolderColon" ), false ) );
    bar.add( Toolbar.GLUE );

    ToolbarButton add = new ToolbarButton( ImageUtil.getThemeableImage( "icon-small", "pentaho-addbutton" ) );
    add.setToolTip( Messages.getString( "createNewFolder" ) );
    add.setCommand( new Command() {
      public void execute() {
        final NewFolderCommand nfc =
            new NewFolderCommand( ( (FileTreeItem) tree.getSelectedItem() ).getRepositoryFile() );
        nfc.setCallback( new ICallback<String>() {
          public void onHandle( String path ) {
            tree.select( path );
          }
        } );
        nfc.execute();
      }
    } );
    bar.add( add );

    VerticalPanel content = new VerticalPanel();
    content.add( bar );
    content.add( treeWrapper );

    setContent( content );
    TreeItem selItem = tree.getSelectedItem();
    if ( selItem != null ) {
      DOM.scrollIntoView( selItem.getElement() );
    }
  }

  public String getSelectedPath() {
    String selectedPath = ( (FileTreeItem) tree.getSelectedItem() ).getRepositoryFile().getPath();
    return selectedPath;
  }

}
