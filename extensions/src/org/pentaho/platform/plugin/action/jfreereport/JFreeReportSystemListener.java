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

package org.pentaho.platform.plugin.action.jfreereport;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoReportConfiguration;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;

public class JFreeReportSystemListener implements IPentahoSystemListener {
  public JFreeReportSystemListener() {
  }

  public boolean startup( final IPentahoSession session ) {
    try {
      synchronized ( ClassicEngineBoot.class ) {
        ClassicEngineBoot.setUserConfig( new PentahoReportConfiguration() );
        ClassicEngineBoot.getInstance().start();
      }
    } catch ( Exception ex ) {
      Logger.warn( JFreeReportSystemListener.class.getName(), Messages.getInstance().getErrorString(
          "JFreeReportSystemListener.ERROR_0001_JFREEREPORT_INITIALIZATION_FAILED" ), //$NON-NLS-1$
          ex );
    }
    return true;
  }

  public void shutdown() {
    // Nothing required
  }

}
