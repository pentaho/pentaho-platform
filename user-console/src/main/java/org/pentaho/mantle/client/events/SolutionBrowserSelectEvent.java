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

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import java.util.List;

public class SolutionBrowserSelectEvent extends GwtEvent<SolutionBrowserSelectEventHandler> implements
    ISolutionBrowserEvent {

  public static Type<SolutionBrowserSelectEventHandler> TYPE = new Type<SolutionBrowserSelectEventHandler>();

  public static final String TYPE_STR = "SolutionBrowserSelectEvent";

  private Widget widget;
  private List<FileItem> fileItems;

  public SolutionBrowserSelectEvent() {
  }

  public SolutionBrowserSelectEvent( Widget widget, List<FileItem> fileItems ) {
    this.widget = widget;
    this.fileItems = fileItems;
  }

  public Type<SolutionBrowserSelectEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch( SolutionBrowserSelectEventHandler handler ) {
    handler.onTabSelected( this );
  }

  public Widget getWidget() {
    return widget;
  }

  public void setWidget( Widget widget ) {
    this.widget = widget;
  }

  public List<FileItem> getFileItems() {
    return fileItems;
  }

  public void setFileItems( List<FileItem> fileItems ) {
    this.fileItems = fileItems;
  }

}
