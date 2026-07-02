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
