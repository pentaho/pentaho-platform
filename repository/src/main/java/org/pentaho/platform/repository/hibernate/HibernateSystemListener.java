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



package org.pentaho.platform.repository.hibernate;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;

public class HibernateSystemListener implements IPentahoSystemListener {

  public boolean startup( final IPentahoSession session ) {
    return HibernateUtil.initialize();
  }

  public void shutdown() {
    // TODO Auto-generated method stub

  }

}
