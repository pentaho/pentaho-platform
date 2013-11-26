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
