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

import mondrian.olap.Util;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.PasswordHelper;
import org.pentaho.platform.util.logging.Logger;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

public class Olap4jSystemListener implements IPentahoSystemListener {
  private static final String PASSWORD = "password";
  private List<Properties> olap4jConnectionList;
  private List<String> removeList;
  private boolean isSecured = false;

  @Override public boolean startup( IPentahoSession session ) {
    try {
      if ( PentahoSystem.get( IUserRoleListService.class ) != null ) {
        isSecured = true;
      }
    } catch ( Throwable t ) {
      // That's ok. The API throws an exception and there is no method to check
      // if security is on or off.
    }

    IOlapService olapService = getOlapService( session );
    if ( olapService != null ) {
      addCatalogs( olapService );
      removeCatalogs( olapService );
    }
    return true;
  }

  private void removeCatalogs( final IOlapService olapService ) {
    for ( final String catalogName : removeList ) {
      final Callable<Void> callable = new Callable<Void>() {
        public Void call() throws Exception {
          olapService.removeCatalog( catalogName, PentahoSessionHolder.getSession() );
          return null;
        }
      };
      try {
        if ( isSecured ) {
          SecurityHelper.getInstance().runAsSystem( callable );
        } else {
          // There's no security available.
          callable.call();
        }
      } catch ( Exception e ) {
        Logger.warn( this, Messages.getInstance().getString(
          "Olap4jSystemListener.ERROR_00002_REMOVE_ERROR", catalogName ), e );
      }
    }
  }

  private void addCatalogs( final IOlapService olapService ) {
    for ( final Properties properties : olap4jConnectionList ) {
      final Callable<Void> callable = new Callable<Void>() {
        public Void call() throws Exception {
          final String password = properties.getProperty( PASSWORD );
          olapService.addOlap4jCatalog(
            properties.getProperty( "name" ),
            properties.getProperty( "className" ),
            getConnectString( properties ),
            properties.getProperty( "user" ),
            password == null
              ? null
              : getPassword( password ),
            new Properties(),
            true,
            PentahoSessionHolder.getSession() );
          return null;
        }
      };
      try {
        if ( isSecured ) {
          SecurityHelper.getInstance().runAsSystem( callable );
        } else {
          // There's no security available.
          callable.call();
        }
      } catch ( Exception e ) {
        Logger.warn( this, Messages.getInstance().getString(
          "Olap4jSystemListener.ERROR_00001_ADD_ERROR", properties.getProperty( "name" ) ), e );
      }
    }
  }

  private String getConnectString( Properties properties ) {
    String connectString = properties.getProperty( "connectString" );
    Util.PropertyList connectMap = Util.parseConnectString( connectString );
    if ( connectMap.get( PASSWORD ) != null ) {
      connectMap.put( PASSWORD, getPassword( connectMap.get( PASSWORD ) ) );
    }
    return connectMap.toString();
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
