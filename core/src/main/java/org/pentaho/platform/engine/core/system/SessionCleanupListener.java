/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemEntryPoint;
import org.pentaho.platform.api.engine.IPentahoSystemExitPoint;
import org.pentaho.platform.api.engine.IPentahoSystemListener;

/**
 * This class is responsible for making sure the thread local that hangs on to the current session gets cleared
 * when a system exit point happens.
 * 
 * @deprecated this is now handled by HttpSessionPentahoSessionIntegrationFilter
 */
@Deprecated
public class SessionCleanupListener implements IPentahoSystemListener, IPentahoSystemExitPoint,
    IPentahoSystemEntryPoint {

  public void shutdown() {
  }

  public boolean startup( IPentahoSession session ) {
    IApplicationContext applicationContext = PentahoSystem.getApplicationContext();
    applicationContext.addEntryPointHandler( this );
    applicationContext.addExitPointHandler( this );
    return true;
  }

  public void systemExitPoint() {
    PentahoSessionHolder.removeSession();
  }

  public void systemEntryPoint() {
  }

}
