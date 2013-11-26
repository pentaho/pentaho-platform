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

package org.pentaho.mantle.login.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.login.client.messages.Messages;

public class MantleLoginEntryPoint implements EntryPoint, IResourceBundleLoadCallback {

  private MantleLoginDialog loginDialog;
  private String returnLocation;

  private Timer popupWarningTimer = new Timer() {
    public void run() {
      MessageDialogBox message =
          new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "popupWarning" ), true, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
      message.center();
    }
  };

  public void onModuleLoad() {
    ResourceBundle messages = new ResourceBundle();
    Messages.setResourceBundle( messages );
    messages.loadBundle( GWT.getModuleBaseURL() + "messages/", "MantleLoginMessages", true, MantleLoginEntryPoint.this ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void bundleLoaded( String bundleName ) {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onFailure( Throwable err ) {
        MessageDialogBox dialog =
            new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "loginError" ), false, true, true ); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setCallback( new IDialogCallback() {
          public void cancelPressed() {
          }

          public void okPressed() {
            loginDialog.show();
          }
        } );
        dialog.center();
      }

      public void onSuccess( Boolean newWindow ) {
        if ( newWindow ) {
          String URL =
              ( !StringUtils.isEmpty( returnLocation ) )
                  ? returnLocation.replace( "Login", "Home" ) : Window.Location.getPath().replace( "Login", "Home" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

          Window.open( URL, "puc", "menubar=no,location=no,resizable=yes,scrollbars=yes,status=no" ); //$NON-NLS-1$ //$NON-NLS-2$
          // Schedule checking of new Window (Popup checker).
          popupWarningTimer.schedule( 5000 );

        } else if ( !StringUtils.isEmpty( returnLocation ) ) { //$NON-NLS-1$
          Window.Location.assign( returnLocation.replace( "Login", "Home" ) );
        } else {
          Window.Location.replace( Window.Location.getPath().replace( "Login", "Home" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }

    };

    loginDialog = new MantleLoginDialog( callback, true );

    setupNativeHooks( loginDialog, this );
  }

  public void setReturnLocation( String str ) {
    returnLocation = str;
    loginDialog.setReturnLocation( returnLocation );
  }

  public void cancelPopupAlertTimer() {
    popupWarningTimer.cancel();
    Window.Location.reload();
  }

  public native void setupNativeHooks( MantleLoginDialog dialog, MantleLoginEntryPoint entry )
  /*-{
     $wnd.openLoginDialog = function(location) {
       entry.@org.pentaho.mantle.login.client.MantleLoginEntryPoint::setReturnLocation(Ljava/lang/String;)(location);
       dialog.@org.pentaho.mantle.login.client.MantleLoginDialog::center()();
     }
       
     $wnd.reportWindowOpened = function(){
       entry.@org.pentaho.mantle.login.client.MantleLoginEntryPoint::cancelPopupAlertTimer()();
     }
   }-*/;

}
