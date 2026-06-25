/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface FavoritesChangedEventHandler extends EventHandler {
  void onFavoritesChanged( FavoritesChangedEvent event );
}
