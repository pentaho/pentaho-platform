/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.dialogs.WaitPopup;

public class HideWaitPopupCommand extends AbstractCommand {

  public HideWaitPopupCommand() {
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    WaitPopup.getInstance().setVisible( false );
  }

}
