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
