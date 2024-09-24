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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.versionchecker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.solution.ComponentBase;

/**
 * Version Check Component This component makes a call to pentaho's server to see if a new version is a vailable.
 *
 * Uses reflection helper so that versioncheck.jar can be deleted without problems
 *
 * input param "ignoreExistingUpdates" - if true, ignore existing updates discovered
 *
 * @author Will Gorman
 *
 */
public class PentahoVersionCheckComponent extends ComponentBase {

  private static final long serialVersionUID = 8178713714323100555L;

  private static final String DOCUMENT = "document"; //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return LogFactory.getLog( PentahoVersionCheckComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  protected boolean validateAction() {
    return true;
  }

  @Override
  public void done() {

  }

  @Override
  protected boolean executeAction() {
    getLogger().warn( "Version Checker has been DEPRECATED and will be deleted in a upcoming release." );
    return true;
  }

  @Override
  public boolean init() {
    return true;
  }

}
