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

package org.pentaho.platform.plugin.action.mondrian;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.BasePublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;

public class MondrianCachePublisher extends BasePublisher {

  private static final long serialVersionUID = -4584778481507215709L;

  private static final Log logger = LogFactory.getLog( MondrianCachePublisher.class );

  @Override
  public Log getLogger() {
    return MondrianCachePublisher.logger;
  }

  public String getName() {
    return Messages.getInstance().getString( "SettingsPublisher.USER_SYSTEM_SETTINGS" ); //$NON-NLS-1$
  }

  public String getDescription() {
    return Messages
        .getInstance()
        .getString(
            "SettingsPublisher.USER_DESCRIPTION", PentahoSystem.getApplicationContext().getSolutionPath( "system" ).replace( '\\', '/' ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public String publish( final IPentahoSession localSession ) {
    try {
      PentahoSystem.refreshSettings();
    } catch ( Throwable t ) {
      error( Messages.getInstance().getErrorString( "SettingsPublisher.ERROR_0001_PUBLISH_FAILED" ), t ); //$NON-NLS-1$
      return Messages.getInstance().getString( "SettingsPublisher.USER_ERROR_PUBLISH_FAILED" ) + t.getLocalizedMessage(); //$NON-NLS-1$
    }
    return Messages.getInstance().getString( "SettingsPublisher.USER_SYSTEM_SETTINGS_UPDATED" ); //$NON-NLS-1$
  }
}
