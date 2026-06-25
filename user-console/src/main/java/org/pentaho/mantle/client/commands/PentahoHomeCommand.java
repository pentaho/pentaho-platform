/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.mantle.client.commands;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class PentahoHomeCommand implements Command {

  public PentahoHomeCommand() {
  }

  public void execute() {
    Window.open( "https://www.hitachivantara.com/go/pentaho.html?source=pentaho-puc", "_blank", "" );
    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

}
