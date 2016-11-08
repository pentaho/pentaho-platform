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
