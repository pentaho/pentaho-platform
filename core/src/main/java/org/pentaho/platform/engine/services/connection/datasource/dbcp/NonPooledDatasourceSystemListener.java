/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.sql.DataSource;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;

public class NonPooledDatasourceSystemListener implements IPentahoSystemListener {

  public boolean startup( final IPentahoSession session ) {
    try {

      Logger.debug( this, "DatasourceSystemListener: called for startup ..." ); //$NON-NLS-1$

      ICacheManager cacheManager = addCacheRegions();

      List<IDatabaseConnection> databaseConnections = getListOfDatabaseConnections( session );

      String dsName = "";
      DataSource ds = null;

      for ( IDatabaseConnection databaseConnection : databaseConnections ) {

        if ( databaseConnection != null ) {

          Logger.debug( this, "  Setting up datasource - " + databaseConnection ); //$NON-NLS-1$

          dsName = databaseConnection.getName();

          //isPortUsedByServer should NOT be called on a JNDI data source
          //http://jira.pentaho.com/browse/BISERVER-12244
          if ( !databaseConnection.getAccessType().equals( DatabaseAccessType.JNDI ) ) {
            // if connection's port used by server there is no sense to get DataSource for this
            ds = isPortUsedByServer( databaseConnection ) ? null : setupDataSourceForConnection( databaseConnection );
          } else {
            Logger.debug( this, "(Datasource \"" + IDBDatasourceService.JDBC_DATASOURCE //$NON-NLS-1$
                + dsName + "\" not cached)" ); //$NON-NLS-1$
            continue;
          }

          cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName, ds );

          Logger.debug( this, "(Storing datasource under key \"" + IDBDatasourceService.JDBC_DATASOURCE //$NON-NLS-1$
              + dsName + "\")" ); //$NON-NLS-1$
        }

      }

      Logger.debug( this, "DatasourceSystemListener: Completed startup." ); //$NON-NLS-1$

      return true;

    } catch ( ObjectFactoryException objface ) {

      Logger.error( this, Messages.getInstance().getErrorString(
          "DatasourceSystemListener.ERROR_0001_UNABLE_TO_INSTANTIATE_OBJECT" ), objface ); //$NON-NLS-1$

      return false;

    } catch ( DatasourceMgmtServiceException dmse ) {

      Logger.error( this, Messages.getInstance().getErrorString(
          "DatasourceSystemListener.ERROR_0002_UNABLE_TO_GET_DATASOURCE" ), dmse ); //$NON-NLS-1$

      return false;
    }
  }

  public void shutdown() {

    ICacheManager cacheManager = PentahoSystem.getCacheManager( null );

    Logger.debug( this, "DatasourceSystemListener: Called for shutdown ..." ); //$NON-NLS-1$

    cacheManager.removeRegionCache( IDBDatasourceService.JDBC_DATASOURCE );

    Logger.debug( this, "DatasourceSystemListener: Completed shutdown." ); //$NON-NLS-1$

  }

  protected DataSource getDataSource( IDatabaseConnection connection ) throws DBDatasourceServiceException {
    return PooledDatasourceHelper.convert( connection );
  }

  protected ICacheManager addCacheRegions() {
    ICacheManager cacheManager = PentahoSystem.getCacheManager( null );

    Logger.debug( this, "Adding caching regions ..." ); //$NON-NLS-1$

    if ( !cacheManager.cacheEnabled( IDBDatasourceService.JDBC_DATASOURCE ) ) {
      cacheManager.addCacheRegion( IDBDatasourceService.JDBC_DATASOURCE );
    }

    return cacheManager;
  }

  @VisibleForTesting
  protected List<IDatabaseConnection> getListOfDatabaseConnections( final IPentahoSession session )
    throws ObjectFactoryException, DatasourceMgmtServiceException {
    IDatasourceMgmtService datasourceMgmtSvc =
        (IDatasourceMgmtService) PentahoSystem.getObjectFactory().get( IDatasourceMgmtService.class, session );

    List<IDatabaseConnection> databaseConnections = datasourceMgmtSvc.getDatasources();
    return databaseConnections;
  }

  @VisibleForTesting
  protected boolean isPortUsedByServer( IDatabaseConnection databaseConnection ) {
    // get connection IP address
    String connectionHostName = databaseConnection.getHostname();
    InetAddress connectionAddress = null;
    try {
      connectionAddress = getAdressFromString( connectionHostName );
    } catch ( UnknownHostException e ) {
      Logger.warn( this, Messages.getInstance().getErrorString(
          "DatasourceSystemListener.WARN_0001_UNABLE_TO_GET_CONNECTION_ADDRESS" ), e ); //$NON-NLS-1$
      return false;
    }
    // get connection port
    String stringConnectionPort = databaseConnection.getDatabasePort();

    // get server URL
    String fullyQualifiedServerURL = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
    URL url = null;
    try {
      url = new URL( fullyQualifiedServerURL );
    } catch ( MalformedURLException e ) {
      Logger.warn( this, Messages.getInstance().getErrorString(
          "DatasourceSystemListener.WARN_0002_UNABLE_TO_PARSE_SERVER_URL" ), e ); //$NON-NLS-1$
      return false;
    }
    // get server IP address
    String hostNameUsedByServer = url.getHost();
    InetAddress serverAddress = null;
    try {
      serverAddress = getAdressFromString( hostNameUsedByServer );
    } catch ( UnknownHostException e ) {
      Logger.warn( this, Messages.getInstance().getErrorString(
          "DatasourceSystemListener.WARN_0003_UNABLE_TO_GET_SERVER_ADDRESS" ), e ); //$NON-NLS-1$
      return false;
    }
    // get server port
    int portUsedByServer = url.getPort();

    boolean isAddressesEquals = connectionAddress.equals( serverAddress );

    boolean isPortsEquals = false;
    try {
      Integer connectionPort = Integer.valueOf( stringConnectionPort );
      isPortsEquals = connectionPort.equals( portUsedByServer );
    } catch ( NumberFormatException e ) {
      Logger.warn( this, Messages.getInstance().getErrorString(
          "DatasourceSystemListener.WARN_0004_UNABLE_TO_GET_PORT_NUMBER" ), e ); //$NON-NLS-1$
      return false;
    }

    return isAddressesEquals && isPortsEquals;
  }

  @VisibleForTesting
  protected DataSource setupDataSourceForConnection( IDatabaseConnection databaseConnection ) {
    DataSource ds = null;
    try {
      ds = getDataSource( databaseConnection );
    } catch ( DBDatasourceServiceException e ) {
      Logger.error( this, "Error retrieving DataSource", e );
    }

    return ds;
  }

  private InetAddress getAdressFromString( String connectionHostName ) throws UnknownHostException {
    InetAddress address = null;
    address = InetAddress.getByName( connectionHostName );
    return address;
  }
}
