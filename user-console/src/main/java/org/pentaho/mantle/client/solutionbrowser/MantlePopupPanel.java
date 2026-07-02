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
