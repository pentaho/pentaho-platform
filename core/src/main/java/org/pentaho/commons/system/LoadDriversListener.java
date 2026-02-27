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


package org.pentaho.commons.system;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;

import java.sql.Driver;
import java.util.ServiceLoader;

/**
 * This listener makes sure that all available JDBC Drivers are loaded so that we do not
 * need to call Class.forName on PentahoSystemDriver for example.
 */
public class LoadDriversListener implements IPentahoSystemListener {
  @SuppressWarnings( "StatementWithEmptyBody" )
  @Override
  public boolean startup( IPentahoSession session ) {
    for ( Driver driver : ServiceLoader.load( Driver.class ) ) {
      //empty on purpose.  All we need is for the driver to be loaded.
      //the assert is to avoid a checkstyle error
      assert true;
    }
    return true;
  }

  @Override public void shutdown() {

  }
}
