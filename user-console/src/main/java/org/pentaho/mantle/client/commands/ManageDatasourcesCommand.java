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

public class ManageDatasourcesCommand extends AbstractCommand {

  public ManageDatasourcesCommand() {
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    nativeManageDatasources();
  }

  private native void nativeManageDatasources()
  /*-{
    $wnd.pho.showDatasourceManageDialog($wnd.datasourceEditorCallback);
  }-*/;
}
