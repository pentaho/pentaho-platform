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


package org.pentaho.platform.api.engine;

public interface IPentahoSystem {

  public int getLoggingLevel();

  public void setLoggingLevel( int loggingLevel );

  public IPentahoSystemStartupActions getStartupActions();

  public void setStartupActions( IPentahoSystemStartupActions startupActions );

  public ISystemSettings getSystemSettingsService();

  public void setSystemSettingsService( ISystemSettings systemSettingsService );

  public IPentahoObjectFactory getPentahoObjectFactory();

  public void setPentahoObjectFactory( IPentahoObjectFactory pentahoObjectFactory );

  public IApplicationContext getApplicationContext();

  public void setApplicationContext( IApplicationContext applicationContext );

  public IPentahoSystemAdminPlugins getAdminPlugins();

  public void setAdminPlugins( IPentahoSystemAdminPlugins adminPlugins );

  public IPentahoSystemListeners getSystemListeners();

  public void setSystemListeners( IPentahoSystemListeners systemListeners );

  public IPentahoSystemAclHelper getPentahoSystemAclHelper();

  public void setPentahoSystemAclHelper( IPentahoSystemAclHelper pentahoSystemAclHelper );

  public IPentahoSystemInitializer getPentahoSystemInitializer();

  public void setPentahoSystemInitializer( IPentahoSystemInitializer pentahoSystemInitializer );

  public IPentahoSystemHelper getPentahoSystemHelper();

  public void setPentahoSystemHelper( IPentahoSystemHelper pentahoSystemHelper );

}
