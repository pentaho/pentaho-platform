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



package org.pentaho.platform.engine.core.system;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.ILogger;

public interface IPentahoLoggingConnection extends IPentahoConnection {
  public void setLogger( ILogger logger );
}
