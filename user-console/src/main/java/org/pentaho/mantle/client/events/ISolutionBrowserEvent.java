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
