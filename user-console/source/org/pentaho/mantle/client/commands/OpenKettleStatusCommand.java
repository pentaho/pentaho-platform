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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;

import java.util.HashMap;

/**
 * Executes the Open Kettle Status command.
 *
 */
public class OpenKettleStatusCommand extends AbstractCommand {

  private static final String KETTLE_STATUS_URL = "/kettle/status";

  public OpenKettleStatusCommand() {

  }

  /**
   * Executes the command to open the kettle status page.
   */
  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    MantleSettingsManager.getInstance().getMantleSettings( new AsyncCallback<HashMap<String, String>>() {

      public void onSuccess( HashMap<String, String> result ) {
        // we're working with a relative URL, this is relative to the web-app not the GWT module
        String kettleStatusUrl = GWT.getHostPageBaseURL() + KETTLE_STATUS_URL;
        /*
         * Open in a named tab as in opposed to '_blank' or '_self';
         * We want to keep PUC open and in addition to that we want to have one single 'Kettle Status' page open
         */
        Window.open( kettleStatusUrl, "KettleStatus", "" ); //$NON-NLS-1$ //$NON-NLS-2$
      }

      public void onFailure( Throwable caught ) {
      }
    }, false );
  }

}
