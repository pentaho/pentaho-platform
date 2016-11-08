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
