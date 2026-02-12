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

public class PerspectivesLoadedEvent extends GwtEvent<PerspectivesLoadedEventHandler> {

  public static Type<PerspectivesLoadedEventHandler> TYPE = new Type<PerspectivesLoadedEventHandler>();

  public PerspectivesLoadedEvent() {
  }

  public Type<PerspectivesLoadedEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch( PerspectivesLoadedEventHandler handler ) {
    handler.onPerspectivesLoaded( this );
  }

}
