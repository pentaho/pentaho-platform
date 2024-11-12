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

import com.google.gwt.user.client.Command;

public class LogoutCommand implements Command {

  public LogoutCommand() {
  }

  public native void execute()
  /*-{
    var loc = $wnd.location.href.substring(0, $wnd.location.href.lastIndexOf('/')) + "/Logout";
    if($wnd.opener != null){
      try{
        if($wnd.opener.location.href.indexOf($wnd.location.host) > -1){
          $wnd.opener.location.href = loc;
          $wnd.close();
          return;
        }
      } catch(e){
        //XSS exception when original window changes domain
      }
    }
    $wnd.open(loc, "_top","");
  }-*/;

}
