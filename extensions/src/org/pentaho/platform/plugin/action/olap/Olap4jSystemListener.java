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
package org.pentaho.platform.plugin.action.olap;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.PasswordHelper;
import org.pentaho.platform.util.logging.Logger;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

public class Olap4jSystemListener implements IPentahoSystemListener {
  private List<Properties> olap4jConnectionList;
  private List<String> removeList;

  @Override public boolean startup( IPentahoSession session ) {
    IOlapService olapService = getOlapService( session );
    if ( olapService != null ) {
      addCatalogs( session, olapService );
      removeCatalogs( session, olapService );
    }
    return true;
  }

  private void removeCatalogs( final IPentahoSession session, final IOlapService olapService ) {
    for ( final String catalogName : removeList ) {
      try {
        SecurityHelper.getInstance().runAsSystem(
          new Callable<Void>() {
            public Void call() throws Exception {
              olapService.removeCatalog( catalogName, session );
              return null;
            }
          });
      } catch ( Exception e ) {
        Logger.warn( this, Messages.getInstance().getString(
          "Olap4jSystemListener.ERROR_00002_REMOVE_ERROR", catalogName ), e );
      }
    }
  }

  private void addCatalogs( final IPentahoSession session, final IOlapService olapService ) {
    for ( final Properties properties : olap4jConnectionList ) {
      try {
        SecurityHelper.getInstance().runAsSystem(
          new Callable<Void>() {
            public Void call() throws Exception {
              final String password = properties.getProperty( "password" );
              olapService.addOlap4jCatalog(
                properties.getProperty( "name" ),
                properties.getProperty( "className" ),
                properties.getProperty( "connectString" ),
                properties.getProperty( "user" ),
                password == null
                  ? null
                  : getPassword( password ),
                new Properties(),
                true,
                session );
              return null;
            }
          });
      } catch ( Exception e ) {
        Logger.warn( this, Messages.getInstance().getString(
          "Olap4jSystemListener.ERROR_00001_ADD_ERROR", properties.getProperty( "name" ) ), e );
      }
    }
  }

  private String getPassword( String password ) {
    return getPasswordHelper().getPassword( password );
  }

  PasswordHelper getPasswordHelper() {
    return new PasswordHelper();
  }

  IOlapService getOlapService( IPentahoSession session ) {
    return PentahoSystem.get( IOlapService.class, session );
  }

  @Override public void shutdown() {
  }

  public void setOlap4jConnectionList( List<Properties> olap4jConnectionList ) {
    this.olap4jConnectionList = olap4jConnectionList;
  }

  public void setOlap4jConnectionRemoveList( List<String> removeList ) {
    this.removeList = removeList;
  }
}
