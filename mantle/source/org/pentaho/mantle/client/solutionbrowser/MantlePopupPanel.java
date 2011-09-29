package org.pentaho.mantle.client.solutionbrowser;

import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;

public class MantlePopupPanel extends PopupPanel {

  private static MantlePopupPanel autoHideInstance;
  private static MantlePopupPanel instance;

  public MantlePopupPanel() {
    this(true);
  }

  public MantlePopupPanel(boolean autohide) {
    super(autohide);

    // This catches auto-hiding initiated closes
    addCloseHandler(new CloseHandler<PopupPanel>() {
      public void onClose(CloseEvent<PopupPanel> event) {
        IFrameTabPanel iframeTab = SolutionBrowserPerspective.getInstance().getContentTabPanel().getCurrentFrame();
        if (iframeTab == null || iframeTab.getFrame() == null) {
          return;
        }
        Frame currentFrame = iframeTab.getFrame();
        FrameUtils.setEmbedVisibility(currentFrame, true);
      }
    });

    DOM.setElementAttribute(getElement(), "oncontextmenu", "return false;"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  // singleton use, if needed
  public static MantlePopupPanel getInstance(boolean autohide) {
    if (autohide) {
      if (autoHideInstance == null) {
        autoHideInstance = new MantlePopupPanel(true);
      }
      return autoHideInstance;
    } else {
      if (instance == null) {
        instance = new MantlePopupPanel(false);
      }
      return instance;
    }
  }

  // singleton use, if needed
  public static MantlePopupPanel getInstance() {
    return getInstance(true);
  }

  @Override
  public void hide() {
    super.hide();

    IFrameTabPanel iframeTab = SolutionBrowserPerspective.getInstance().getContentTabPanel().getCurrentFrame();
    if (iframeTab == null || iframeTab.getFrame() == null) {
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    FrameUtils.setEmbedVisibility(currentFrame, true);
  }

  @Override
  public void show() {
    super.show();
    IFrameTabPanel iframeTab = SolutionBrowserPerspective.getInstance().getContentTabPanel().getCurrentFrame();
    if (iframeTab == null || iframeTab.getFrame() == null) {
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    if (ElementUtils.elementsOverlap(this.getElement(), currentFrame.getElement())) {
      FrameUtils.setEmbedVisibility(currentFrame, false);
    }
  }
}