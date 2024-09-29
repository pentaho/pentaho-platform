/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;

import java.util.HashMap;

/**
 * Executes the Open Kettle Status command.
 *
 */
public class OpenKettleStatusCommand extends AbstractCommand {

  private static final String KETTLE_STATUS_URL = "kettle/status";

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
        String hostBaseUrl = GWT.getHostPageBaseURL().endsWith( "/" )
          ? GWT.getHostPageBaseURL() : ( GWT.getHostPageBaseURL() + "/" );
        String kettleStatusUrl = hostBaseUrl + KETTLE_STATUS_URL;

        openNewWindow( kettleStatusUrl, "_blank", false );
      }

      public void onFailure( Throwable caught ) {
      }
    }, false );
  }

  /**
   * This method open a new browser tab with the provided url and set the focus to it
   *
   * http://stackoverflow.com/questions/3311293/javascript-bring-window-to-front-if-already-open-in-window-open#answer-24418324
   *
   * ".focus() command is likely not going to work in all browsers.
   * This used to work back in the day but not any more, mainly due to browsers working
   * to actively stop shady ad networks from pushing their popup ads to the foreground.
   *
   * In Mozilla Firefox in particular (depending on your version) there is a configuration setting that is turned on
   * by default that stops other windows (e.g. popups) from focusing themselves.
   *
   * You can find this setting in the about:config page (tread carefully!)
   *
   * Other browsers may implement something similar, but quite simply if 1 of the major browsers blocks the use of
   * .focus() by default then there's not much use in attempting to call it. As a result, the only solution I've seen
   * that works is to see if the window exists and is not already closed... and if so close it, then load the window
   * you want."
   *
   * @param url URL address to open
   * @param wname window name to apply for the new window to be open
   */
  public static native void openNewWindow( String url, String wname, boolean forceFocus )
  /*-{
    if( forceFocus ) {
      var w = window.open( url , wname );
      w.close();
    }
    window.open( url , wname );
  }-*/;

}
