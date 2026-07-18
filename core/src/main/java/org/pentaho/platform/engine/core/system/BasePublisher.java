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

import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;

public abstract class BasePublisher extends PentahoBase implements IPentahoPublisher {

  private static final long serialVersionUID = -8079266498445883700L;
  public static final boolean debug = PentahoSystem.debug;

  public abstract String publish( IPentahoSession session );

  public String publish( final IPentahoSession session, final int pLoggingLevel ) {

    setLoggingLevel( pLoggingLevel );
    return publish( session );
  }

}
