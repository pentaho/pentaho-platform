package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.service.EmptyCallback;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;

/**
 * User: nbaker
 * Date: 3/17/12
 */
public class CollapseBrowserCommand extends AbstractCommand{
  public CollapseBrowserCommand() {
  }

  protected void performOperation() {
    performOperation(false);
  }

  protected void performOperation(boolean feedback) {final SolutionBrowserPanel solutionBrowserPerspective = SolutionBrowserPanel.getInstance();
    if (!solutionBrowserPerspective.isNavigatorShowing()) {
      PerspectiveManager.getInstance().setPerspective("default.perspective");
    }
    solutionBrowserPerspective.setNavigatorShowing(false);
    MantleServiceCache.getService().setShowNavigator(false, EmptyCallback.getInstance());
  }
}
