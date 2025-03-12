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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.GwtEvent;
import org.pentaho.mantle.client.usersettings.JsSetting;

public class UserSettingsLoadedEvent extends GwtEvent<UserSettingsLoadedEventHandler> {

  public static Type<UserSettingsLoadedEventHandler> TYPE = new Type<UserSettingsLoadedEventHandler>();

  public static final String TYPE_STR = "UserSettingsLoadedEvent";

  private JsArray<JsSetting> settings;

  public UserSettingsLoadedEvent() {
  }

  public UserSettingsLoadedEvent( final JsArray<JsSetting> settings ) {
    this.settings = settings;
  }

  public Type<UserSettingsLoadedEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch( UserSettingsLoadedEventHandler handler ) {
    handler.onUserSettingsLoaded( this );
  }

  public JsArray<JsSetting> getSettings() {
    return settings;
  }

  public void setSettings( JsArray<JsSetting> settings ) {
    this.settings = settings;
  }
}
