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
