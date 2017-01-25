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
