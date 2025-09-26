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

public class SolutionBrowserUndefinedEvent extends GwtEvent<SolutionBrowserUndefinedEventHandler> implements
    ISolutionBrowserEvent {

  public static Type<SolutionBrowserUndefinedEventHandler> TYPE = new Type<SolutionBrowserUndefinedEventHandler>();

  public static final String TYPE_STR = "SolutionBrowserUndefinedEvent";

  private Widget widget;
  private List<FileItem> fileItems;

  public SolutionBrowserUndefinedEvent() {
  }

  public SolutionBrowserUndefinedEvent( Widget widget, List<FileItem> fileItems ) {
    this.widget = widget;
    this.fileItems = fileItems;
  }

  public Type<SolutionBrowserUndefinedEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch( SolutionBrowserUndefinedEventHandler handler ) {
    handler.onUndefinedEvent( this );
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
