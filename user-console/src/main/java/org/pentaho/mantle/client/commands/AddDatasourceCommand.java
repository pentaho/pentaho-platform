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

public class AddDatasourceCommand extends AbstractCommand {

  public AddDatasourceCommand() {
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    nativeManageDatasources();
  }

  private native void nativeManageDatasources()
  /*-{
    $wnd.pho.openDatasourceEditor($wnd.datasourceEditorCallback);
  }-*/;
}
