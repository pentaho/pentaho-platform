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
