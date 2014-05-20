/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Rowell Belen
 */
public class SolutionFolderActionEvent extends GwtEvent<SolutionFolderActionEventHandler> {

  public static Type<SolutionFolderActionEventHandler> TYPE = new Type<SolutionFolderActionEventHandler>();

  public SolutionFolderActionEvent() {
  }

  public SolutionFolderActionEvent( String action ) {
    this.action = action;
  }

  private String action;
  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public String getAction() {
    return action;
  }

  public void setAction( String action ) {
    this.action = action;
  }

  @Override
  public Type<SolutionFolderActionEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch( SolutionFolderActionEventHandler solutionEditEventHandler ) {
    solutionEditEventHandler.onEdit( this );
  }
}
