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

import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISystemSettings;

import java.util.List;

/**
 * This class exists for the sole purpose of providing a way for Spring to inject Spring-created objects into a
 * static {@link PentahoSystem}.
 * <p>
 * Please do not reference this class in your code.
 */
public class SpringPentahoSystemProxy {

  public void setAdministrationPlugins( List<IPentahoPublisher> administrationPlugins ) {
    PentahoSystem.setAdministrationPlugins( administrationPlugins );
  }

  public void setSystemListeners( List<IPentahoSystemListener> systemListeners ) {
    PentahoSystem.setSystemListeners( systemListeners );
  }

  public void setSessionStartupActions( List<ISessionStartupAction> registries ) {
    PentahoSystem.setSessionStartupActions( registries );
  }

  public void setSystemSettingsService( ISystemSettings systemSettingsService ) {
    PentahoSystem.setSystemSettingsService( systemSettingsService );
  }
}
