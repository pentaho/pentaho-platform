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

package org.pentaho.platform.repository2.unified;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Initializes the backing repository.
 * 
 * @author wseyler
 * @author mlowery
 */
public class BackingRepositoryLifecycleManagerSystemListener implements IPentahoSystemListener {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( BackingRepositoryLifecycleManagerSystemListener.class );

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public BackingRepositoryLifecycleManagerSystemListener() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  @Override
  public boolean startup( IPentahoSession session ) {
    try {
      IBackingRepositoryLifecycleManager lcm = PentahoSystem.get( IBackingRepositoryLifecycleManager.class );
      if ( lcm != null ) {
        lcm.startup();
      }

      return true;
    } catch ( Exception e ) {
      logger.error( "", e ); //$NON-NLS-1$
      return false;
    }
  }

  @Override
  public void shutdown() {

  }

}
