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

@SuppressWarnings( "unused" )
public class PentahoHomeCommand implements Command {
  private static final String URL = "https://www.pentaho.com";

  public PentahoHomeCommand() {
    // ignore
  }

  public void execute() {
    Window.open( URL, "_blank", "" );
  }
}
