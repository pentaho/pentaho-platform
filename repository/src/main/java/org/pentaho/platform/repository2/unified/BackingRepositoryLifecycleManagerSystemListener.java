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
