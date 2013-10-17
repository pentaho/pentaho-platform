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

package org.pentaho.platform.web.http.context;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
