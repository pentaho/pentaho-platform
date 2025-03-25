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
 * @author Diogo Mariano
 */
public class ShowHiddenFilesEvent extends GwtEvent<ShowHiddenFilesEventHandler> {

  public static Type<ShowHiddenFilesEventHandler> TYPE = new Type<ShowHiddenFilesEventHandler>();

  public ShowHiddenFilesEvent() {
  }

  private boolean value;

  public boolean getValue() {
    return value;
  }

  public void setValue( boolean value ) {
    this.value = value;
  }

  @Override
  public Type<ShowHiddenFilesEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch( ShowHiddenFilesEventHandler showHiddenFilesEventHandler ) {
    showHiddenFilesEventHandler.onEdit( this );
  }
}
