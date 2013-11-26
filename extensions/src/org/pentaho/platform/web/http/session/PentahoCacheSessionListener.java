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

package org.pentaho.platform.web.http.session;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

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
