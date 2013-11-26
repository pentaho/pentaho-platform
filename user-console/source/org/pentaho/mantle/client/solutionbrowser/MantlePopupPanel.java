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

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

public class MantlePopupPanel extends PopupPanel {

  private static MantlePopupPanel autoHideInstance;
  private static MantlePopupPanel instance;

  public MantlePopupPanel() {
    this( true );
  }

  public MantlePopupPanel( boolean autohide ) {
    super( autohide );

    // This catches auto-hiding initiated closes
    addCloseHandler( new CloseHandler<PopupPanel>() {
      public void onClose( CloseEvent<PopupPanel> event ) {
        IFrameTabPanel iframeTab = SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame();
        if ( iframeTab == null || iframeTab.getFrame() == null ) {
          return;
        }
        Frame currentFrame = iframeTab.getFrame();
        FrameUtils.setEmbedVisibility( currentFrame, true );
      }
    } );

    DOM.setElementAttribute( getElement(), "oncontextmenu", "return false;" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  // singleton use, if needed
  public static MantlePopupPanel getInstance( boolean autohide ) {
    if ( autohide ) {
      if ( autoHideInstance == null ) {
        autoHideInstance = new MantlePopupPanel( true );
      }
      return autoHideInstance;
    } else {
      if ( instance == null ) {
        instance = new MantlePopupPanel( false );
      }
      return instance;
    }
  }

  // singleton use, if needed
  public static MantlePopupPanel getInstance() {
    return getInstance( true );
  }

  @Override
  public void hide() {
    super.hide();

    IFrameTabPanel iframeTab = SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame();
    if ( iframeTab == null || iframeTab.getFrame() == null ) {
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    FrameUtils.setEmbedVisibility( currentFrame, true );
  }

  @Override
  public void show() {
    super.show();
    IFrameTabPanel iframeTab = SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame();
    if ( iframeTab == null || iframeTab.getFrame() == null ) {
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    if ( ElementUtils.elementsOverlap( this.getElement(), currentFrame.getElement() ) ) {
      FrameUtils.setEmbedVisibility( currentFrame, false );
    }
  }
}
