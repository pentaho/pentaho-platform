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
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

public class PrintCommand implements Command {

  public PrintCommand() {
  }

  public void execute() {
    printFrame( SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrameElementId() );
  }

  /**
   * This method will print the frame with the given element id.
   * 
   * @param elementId
   */
  public static native void printFrame( String elementId ) /*-{
    var frame = $doc.getElementById( elementId );
    if (!frame) {
      $wnd.alert("Error: Can't find printing frame. Please try again.");
      return;
    }
    frame = frame.contentWindow;
    frame.focus();
    frame.print();
  }-*/;

}
