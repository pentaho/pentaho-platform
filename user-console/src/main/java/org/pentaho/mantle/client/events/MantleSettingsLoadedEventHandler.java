/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface MantleSettingsLoadedEventHandler extends EventHandler {
  void onMantleSettingsLoaded( MantleSettingsLoadedEvent event );
}
