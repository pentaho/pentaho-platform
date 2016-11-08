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

package org.pentaho.platform.scheduler2.versionchecker;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.util.versionchecker.PentahoVersionCheckReflectHelper;

public class VersionCheckerAction implements IAction {

  public static final String VERSION_REQUEST_FLAGS = "versionRequestFlags"; //$NON-NLS-1$

  private int requestFlags;

  public Log getLogger() {
    return LogFactory.getLog( VersionCheckerAction.class );
  }

  public void setVersionRequestFlags( int value ) {
    this.requestFlags = value;
  }

  public int getVersionRequestFlags() {
    return this.requestFlags;
  }

  public void execute() {
    List results = PentahoVersionCheckReflectHelper.performVersionCheck( false, this.getVersionRequestFlags() );
    if ( results != null ) {
      PentahoVersionCheckReflectHelper.logVersionCheck( results, getLogger() );
    }
  }
}
