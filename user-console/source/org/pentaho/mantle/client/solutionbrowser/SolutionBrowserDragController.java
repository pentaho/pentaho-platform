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

package org.pentaho.mantle.client.solutionbrowser;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.ui.tabs.MantleTabPanel;

/**
 * Used by GWT-DND to manage drag and drop events in the solution browser.
 * 
 * User: nbaker Date: Jun 25, 2010
 */
public class SolutionBrowserDragController extends PickupDragController {
  private MantleTabPanel contentTabPanel;

  public SolutionBrowserDragController( MantleTabPanel tabs ) {
    super( RootPanel.get(), false );
    this.contentTabPanel = tabs;
    setBehaviorDragProxy( true );
    setBehaviorDragStartSensitivity( 5 );
  }

  private FileItem preview;
  private FileItem currentDragItem;

  @Override
  protected void restoreSelectedWidgetsStyle() {
  }

  @Override
  protected void saveSelectedWidgetsLocationAndStyle() {
  }

  @Override
  protected void restoreSelectedWidgetsLocation() {
  }

  @Override
  public void dragEnd() {
    if ( currentFrameAcceptsDrops() == false ) {
      return;
    }
    FileItem item = (FileItem) context.draggable;
    context.vetoException = new Exception();
    super.dragEnd();
    preview.removeFromParent();
    preview = null;
    MantleApplication.overlayPanel.setVisible( false );
    notifyContentDragEnd( item.getPath(), item.getName(), item.getLocalizedName() );
    currentDragItem = null;
  }

  @Override
  public void dragMove() {
    if ( currentFrameAcceptsDrops() == false ) {
      return;
    }
    super.dragMove();
    boolean validDropLocation = notifyContentDragMove();
    preview.setDroppable( validDropLocation );

  }

  @Override
  public void dragStart() {
    if ( currentFrameAcceptsDrops() == false ) {
      return;
    }
    currentDragItem = (FileItem) context.draggable;

    super.dragStart();
    MantleApplication.overlayPanel.setVisible( true );
  }

  @Override
  protected Widget newDragProxy( DragContext context ) {
    FileItem item = (FileItem) context.draggable;
    FileItem newItem = item.makeDragProxy();
    preview = newItem;
    return newItem;
  }

  /**
   * Modifies mouse coordinates relative to the position of the active content panel (iframe) then sends a JSNI
   * call to that iframe with the coordinates.
   * 
   * @return valid drop flag
   */
  private boolean notifyContentDragMove() {
    if ( contentTabPanel.getCurrentFrame() == null ) {
      return false;
    }
    int rawX = this.context.mouseX;
    int rawY = this.context.mouseY;
    int offsetX = rawX - contentTabPanel.getCurrentFrame().getFrame().getAbsoluteLeft();
    int offsetY = rawY - contentTabPanel.getCurrentFrame().getFrame().getAbsoluteTop();
    boolean validDropLocation =
        notifyFrameOfDrag( contentTabPanel.getCurrentFrame().getFrame().getElement(), offsetX, offsetY, currentDragItem
            .getName() );
    return validDropLocation;
  }

  private boolean currentFrameAcceptsDrops() {
    return !( contentTabPanel.getCurrentFrame() == null || currentFrameHasDropJs( contentTabPanel.getCurrentFrame()
        .getFrame().getElement() ) == false );
  }

  private native boolean currentFrameHasDropJs( Element e )/*-{
                                                           return e.contentWindow.mantleDragEvent != undefined;
                                                           }-*/;

  private native boolean notifyFrameOfDrag( Element e, int x, int y, String name )/*-{
      try{
        return e.contentWindow.mantleDragEvent(x,y, name);
      } catch(e){
        //alert(e);
      }
      return false;
    }-*/;

  private native void notifyFrameOfDragEnd( Element e, int x, int y, String path,
                                            String name, String localizedName )/*-{
      try{
        e.contentWindow.mantleDragEnd(x,y, "", path, name, localizedName);
      } catch(e){
        //alert(e);
      }
    }-*/;

  /**
   * Modifies mouse coordinates relative to the position of the active content panel (iframe) then sends a JSNI
   * call to that iframe with the coordinates and Solution File Info.
   * 
   * @return valid drop flag
   */
  private void notifyContentDragEnd( String path, String name, String localizedName ) {
    if ( contentTabPanel.getCurrentFrame() == null ) {
      return;
    }
    int rawX = this.context.mouseX;
    int rawY = this.context.mouseY;
    int offsetX = rawX - contentTabPanel.getCurrentFrame().getFrame().getAbsoluteLeft();
    int offsetY = rawY - contentTabPanel.getCurrentFrame().getFrame().getAbsoluteTop();
    notifyFrameOfDragEnd( contentTabPanel.getCurrentFrame().getFrame().getElement(), offsetX, offsetY, path, name,
        localizedName );
  }

  @Override
  public void previewDragEnd() throws VetoDragException {

  }
}
