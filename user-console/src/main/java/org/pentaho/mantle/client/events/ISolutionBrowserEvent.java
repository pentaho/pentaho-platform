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



package org.pentaho.mantle.client.events;

import com.google.gwt.user.client.ui.Widget;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import java.util.List;

public interface ISolutionBrowserEvent {
  public Widget getWidget();

  public void setWidget( Widget widget );

  public List<FileItem> getFileItems();

  public void setFileItems( List<FileItem> fileItems );
}
