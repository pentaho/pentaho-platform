package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.EmptyRequestCallback;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

/**
 * User: nbaker Date: 3/17/12
 */
public class CollapseBrowserCommand extends AbstractCommand {
  public CollapseBrowserCommand() {
  }

  protected void performOperation() {
    performOperation(false);
  }

  protected void performOperation(boolean feedback) {
    final SolutionBrowserPanel solutionBrowserPerspective = SolutionBrowserPanel.getInstance();
    if (!solutionBrowserPerspective.isNavigatorShowing()) {
      PerspectiveManager.getInstance().setPerspective("default.perspective");
    }
    solutionBrowserPerspective.setNavigatorShowing(false);

    final String url = GWT.getHostPageBaseURL() + "api/user-settings/MANTLE_SHOW_NAVIGATOR"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
    try {
      builder.sendRequest("false", EmptyRequestCallback.getInstance());
    } catch (RequestException e) {
      // showError(e);
    }

  }
}
