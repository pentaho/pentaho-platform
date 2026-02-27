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


package org.pentaho.platform.web.http.context;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class PentahoCacheContextListener implements ServletContextListener {

  public void contextInitialized( final ServletContextEvent event ) {
    // Nothing to do here...
  }

  public void contextDestroyed( final ServletContextEvent event ) {
    // NOTE: if the cacheManager has been configured to have session creation scope
    // getCacheManager will return null, which is fine, since PentahoCacheSessionListener
    // should have cleaned up the session scoped caches. If the cacheManager
    // has been created with global scope, getCacheManager will return a non-null instance.
    ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
    if ( cacheManager != null ) {
      cacheManager.cacheStop();
    }
  }

}
