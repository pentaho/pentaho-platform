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

package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Command;
import org.pentaho.mantle.client.EmptyRequestCallback;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;

public class ShowBrowserCommand implements Command {
  private boolean state;

  public ShowBrowserCommand() {
    final SolutionBrowserPanel solutionBrowserPerspective = SolutionBrowserPanel.getInstance();
    this.state = !solutionBrowserPerspective.isNavigatorShowing();
  }

  public ShowBrowserCommand( boolean flag ) {
    this.state = flag;
  }

  public void execute() {
    final SolutionBrowserPanel solutionBrowserPerspective = SolutionBrowserPanel.getInstance();
    solutionBrowserPerspective.setNavigatorShowing( state );
    if ( solutionBrowserPerspective != null ) {
      if ( PerspectiveManager.getInstance().getActivePerspective().getId().equalsIgnoreCase(
          PerspectiveManager.OPENED_PERSPECTIVE ) ) {
        PerspectiveManager.getInstance().setPerspective( PerspectiveManager.OPENED_PERSPECTIVE );
      }
    }
    final String url = GWT.getHostPageBaseURL() + "api/user-settings/MANTLE_SHOW_NAVIGATOR"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
    try {
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.sendRequest( "" + state, EmptyRequestCallback.getInstance() );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

}
