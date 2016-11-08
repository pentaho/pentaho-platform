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

package org.pentaho.platform.repository.webservices;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;

import java.util.ArrayList;
import java.util.List;

public class DatasourceMgmtToWebServiceAdapter implements IDatasourceMgmtService {

  private IDatasourceMgmtWebService datasourceMgmtWebService;
  private DatabaseConnectionAdapter databaseConnectionAdapter = new DatabaseConnectionAdapter();

  public DatasourceMgmtToWebServiceAdapter( IDatasourceMgmtWebService datasourceMgmtWebService ) {
    super();
    this.datasourceMgmtWebService = datasourceMgmtWebService;
  }

  @Override
  public void init( IPentahoSession session ) {
    // TODO Auto-generated method stub

  }

  @Override
  public String createDatasource( IDatabaseConnection databaseConnection ) {
    try {
      return datasourceMgmtWebService.createDatasource( databaseConnectionAdapter
          .marshal( (DatabaseConnection) databaseConnection ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public void deleteDatasourceByName( String name ) {
    datasourceMgmtWebService.deleteDatasourceByName( name );
  }

  @Override
  public IDatabaseConnection getDatasourceByName( String name ) {
    try {
      return databaseConnectionAdapter.unmarshal( datasourceMgmtWebService.getDatasourceByName( name ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public List<IDatabaseConnection> getDatasources() {
    List<IDatabaseConnection> databaseConnections = new ArrayList<IDatabaseConnection>();
    for ( DatabaseConnectionDto databaseConnection : datasourceMgmtWebService.getDatasources() ) {
      try {
        databaseConnections.add( databaseConnectionAdapter.unmarshal( databaseConnection ) );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }
    return databaseConnections;

  }

  @Override
  public String updateDatasourceByName( String name, IDatabaseConnection databaseConnection ) {
    try {
      return datasourceMgmtWebService.updateDatasourceByName( name, databaseConnectionAdapter
          .marshal( (DatabaseConnection) databaseConnection ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public void deleteDatasourceById( String id ) {
    datasourceMgmtWebService.deleteDatasourceById( id );
  }

  @Override
  public IDatabaseConnection getDatasourceById( String id ) {
    try {
      return databaseConnectionAdapter.unmarshal( datasourceMgmtWebService.getDatasourceById( id ) );
    } catch ( Exception e ) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public List<String> getDatasourceIds() {
    return datasourceMgmtWebService.getDatasourceIds();
  }

  @Override
  public String updateDatasourceById( String id, IDatabaseConnection databaseConnection ) {
    try {
      return datasourceMgmtWebService.updateDatasourceById( id, databaseConnectionAdapter
          .marshal( (DatabaseConnection) databaseConnection ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

}
