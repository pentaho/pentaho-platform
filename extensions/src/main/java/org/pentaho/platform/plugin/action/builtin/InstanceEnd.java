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


package org.pentaho.platform.plugin.action.builtin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.services.solution.ComponentBase;

public class InstanceEnd extends ComponentBase {

  private static final long serialVersionUID = -1193493564794051700L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( InstanceEnd.class );
  }

  @Override
  protected boolean validateAction() {
    // if we got this far then we should be ok...
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    // nothing to do here
    return true;
  }

  @Override
  public void done() {
    // update the runtime data object and flush it to the runtime repository

    // set a flag indicating that this runtime data is complete
    // TODO hook up to the method in the runtime context when it is available

    // flush the object to the repository

    // audit this completion
    audit( MessageTypes.INSTANCE_END, getInstanceId(), "", 0 ); //$NON-NLS-1$

  }

  @Override
  protected boolean executeAction() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean init() {
    // nothing to do here
    return true;
  }

}
