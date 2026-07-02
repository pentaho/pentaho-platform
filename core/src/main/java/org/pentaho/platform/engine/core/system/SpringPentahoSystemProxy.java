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
