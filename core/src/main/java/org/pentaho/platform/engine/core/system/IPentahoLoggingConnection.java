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


package org.pentaho.platform.engine.core.system;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.ILogger;

public interface IPentahoLoggingConnection extends IPentahoConnection {
  public void setLogger( ILogger logger );
}
