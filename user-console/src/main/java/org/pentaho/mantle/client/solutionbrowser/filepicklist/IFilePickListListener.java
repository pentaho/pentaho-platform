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



package org.pentaho.mantle.client.solutionbrowser.filepicklist;

public interface IFilePickListListener<T extends IFilePickItem> {

  public void itemsChanged( AbstractFilePickList<T> filePickList );

  public void onSaveComplete( AbstractFilePickList<T> filePickList );

}
