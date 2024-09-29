/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.osgi;

import org.osgi.framework.BundleContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.OSGIObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard OSGI Activator class which is called when the OSGI environment is started. Work to integrate the OSGI
 * container with the PentahoSystem is started from this class
 */
public class PentahoOSGIActivator {

  private Logger logger = LoggerFactory.getLogger( getClass() );
  private static OSGIObjectFactory objectFactory;

  public void setBundleContext( BundleContext bundleContext ) throws Exception {
    logger.debug( "Registering OSGIObjectFactory" );

    if ( objectFactory != null ) {
      logger.debug( "De-Registering Previous OSGIObjectFactory" );
      PentahoSystem.deregisterObjectFactory( objectFactory );
    }

    objectFactory = new OSGIObjectFactory( bundleContext );
    PentahoSystem.registerObjectFactory( objectFactory );
    PentahoSystem.setBundleContext( bundleContext );
    logger.debug( "OSGIObjectFactory installed" );

  }

  public void shutdown() {
    if ( objectFactory != null ) {
      PentahoSystem.deregisterObjectFactory( objectFactory );
    }
  }

}
