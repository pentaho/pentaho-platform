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


package org.pentaho.platform.web.http.session;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

public class PentahoCacheSessionListener implements HttpSessionListener {

  public void sessionCreated( final HttpSessionEvent event ) {
    // Nothing to do here.
  }

  public void sessionDestroyed( final HttpSessionEvent event ) {
    HttpSession session = event.getSession();
    Object obj = session.getAttribute( PentahoSystem.PENTAHO_SESSION_KEY ); //$NON-NLS-1$
    if ( obj != null ) {
      IPentahoSession userSession = (IPentahoSession) obj;
      ICacheManager cacheManager = PentahoSystem.getCacheManager( userSession );
      if ( null != cacheManager ) {
        IPentahoSession pentahoSession = (IPentahoSession) obj;
        if ( pentahoSession != null ) {
          cacheManager.removeRegionCache( pentahoSession.getId() );
        }

      }
    }
  }

}
