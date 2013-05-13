/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.events;

import java.util.List;

import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

public class SolutionBrowserCloseEvent extends GwtEvent<SolutionBrowserCloseEventHandler> implements ISolutionBrowserEvent {

  public static Type<SolutionBrowserCloseEventHandler> TYPE = new Type<SolutionBrowserCloseEventHandler>();

  public static final String TYPE_STR = "SolutionBrowserCloseEvent";

  private Widget widget;
  private List<FileItem> fileItems;

  public SolutionBrowserCloseEvent() {
  }
  
  public SolutionBrowserCloseEvent(Widget widget, List<FileItem> fileItems) {
    this.widget = widget;
    this.fileItems = fileItems;
  }

  public Type<SolutionBrowserCloseEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch(SolutionBrowserCloseEventHandler handler) {
    handler.onTabClosed(this);
  }

  public Widget getWidget() {
    return widget;
  }

  public void setWidget(Widget widget) {
    this.widget = widget;
  }

  public List<FileItem> getFileItems() {
    return fileItems;
  }

  public void setFileItems(List<FileItem> fileItems) {
    this.fileItems = fileItems;
  }

}
