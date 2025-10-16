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

public class FavoritesChangedEvent extends GwtEvent<FavoritesChangedEventHandler> {

  public static Type<FavoritesChangedEventHandler> TYPE = new Type<FavoritesChangedEventHandler>();

  public static final String TYPE_STR = "FavoritesChangedEvent";

  public FavoritesChangedEvent() {
  }

  public Type<FavoritesChangedEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch( FavoritesChangedEventHandler handler ) {
    handler.onFavoritesChanged( this );
  }

}
