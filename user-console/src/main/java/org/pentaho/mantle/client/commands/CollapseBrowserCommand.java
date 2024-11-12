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


package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import org.pentaho.mantle.client.EmptyRequestCallback;
import org.pentaho.mantle.client.csrf.CsrfRequestBuilder;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;

/**
 * User: nbaker Date: 3/17/12
 */
public class CollapseBrowserCommand extends AbstractCommand {

  public CollapseBrowserCommand() {
  }

  protected void performOperation() {
    performOperation( false );
  }

  protected void performOperation( boolean feedback ) {

    final SolutionBrowserPanel solutionBrowserPerspective = SolutionBrowserPanel.getInstance();
    if ( !solutionBrowserPerspective.isNavigatorShowing() ) {
      PerspectiveManager.getInstance().setPerspective( PerspectiveManager.OPENED_PERSPECTIVE );
    }
    solutionBrowserPerspective.setNavigatorShowing( false );

    String url = GWT.getHostPageBaseURL() + "api/user-settings/MANTLE_SHOW_NAVIGATOR";
    RequestBuilder builder = new CsrfRequestBuilder( RequestBuilder.POST, url );
    try {
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.sendRequest( "false", EmptyRequestCallback.getInstance() );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }
}
