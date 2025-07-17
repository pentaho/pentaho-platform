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

import java.util.HashMap;

public class MantleSettingsLoadedEvent extends GwtEvent<MantleSettingsLoadedEventHandler> {

  public static Type<MantleSettingsLoadedEventHandler> TYPE = new Type<MantleSettingsLoadedEventHandler>();

  public static final String TYPE_STR = "MantleSettingsLoadedEvent";

  private HashMap<String, String> settings;

  public MantleSettingsLoadedEvent() {
  }

  public MantleSettingsLoadedEvent( final HashMap<String, String> settings ) {
    this.settings = settings;
  }

  public Type<MantleSettingsLoadedEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch( MantleSettingsLoadedEventHandler handler ) {
    handler.onMantleSettingsLoaded( this );
  }

  public HashMap<String, String> getSettings() {
    return settings;
  }

  public void setSettings( HashMap<String, String> settings ) {
    this.settings = settings;
  }
}
