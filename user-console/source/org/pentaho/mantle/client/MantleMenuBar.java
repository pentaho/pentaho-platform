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

package org.pentaho.mantle.client;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

public class MantleMenuBar extends MenuBar {

  public MantleMenuBar() {
    super();
  }

  public MantleMenuBar( boolean vertical ) {
    super( vertical );
  }

  @Override
  public void onBrowserEvent( Event event ) {
    super.onBrowserEvent( event );

    final MenuItem item = getSelectedItem();
    switch ( DOM.eventGetType( event ) ) {
      case Event.ONCLICK: {
        if ( item != null ) {
          hidePDFFrames( item );
        }
        break;
      }
      case Event.ONMOUSEOVER: {
        if ( item != null ) {
          hidePDFFrames( item );
        }
        break;
      }
    }
  }

  private void hidePDFFrames( MenuItem item ) {
    Frame frame = getActiveBrowserPerspectiveFrame();
    if ( frame == null ) {
      return;
    }
    if ( item.getSubMenu() != null && item.getSubMenu().isVisible() ) {
      if ( ElementUtils.elementsOverlap( item.getSubMenu().getElement(), getActiveBrowserPerspectiveFrame()
          .getElement() ) ) {
        FrameUtils.setEmbedVisibility( getActiveBrowserPerspectiveFrame(), false );
      }
    } else if ( item.getParentMenu() != null ) { // popups
      if ( ElementUtils.elementsOverlap( item.getParentMenu().getElement(), getActiveBrowserPerspectiveFrame()
          .getElement() ) ) {
        FrameUtils.setEmbedVisibility( getActiveBrowserPerspectiveFrame(), false );
      }
    }
  }

  private Frame getActiveBrowserPerspectiveFrame() {
    IFrameTabPanel panel = SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame();
    if ( panel == null ) {
      return null;
    } else {
      return panel.getFrame();
    }
  }

  @Override
  public void onPopupClosed( PopupPanel sender, boolean autoClosed ) {
    super.addCloseHandler( new CloseHandler<PopupPanel>() {

      public void onClose( CloseEvent<PopupPanel> event ) {
        Frame frame = getActiveBrowserPerspectiveFrame();
        if ( frame == null ) {
          return;
        }
        FrameUtils.setEmbedVisibility( getActiveBrowserPerspectiveFrame(), true );
      }
    } );

  }
}
