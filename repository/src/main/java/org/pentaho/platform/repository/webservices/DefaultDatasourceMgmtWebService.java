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
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.jws.WebService;
import java.util.ArrayList;
import java.util.List;

@WebService( endpointInterface = "org.pentaho.platform.repository.webservices.IDatasourceMgmtWebService",
    serviceName = "datasourceMgmtService", portName = "datasourceMgmtServicePort",
    targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class DefaultDatasourceMgmtWebService implements IDatasourceMgmtWebService {

  protected IDatasourceMgmtService datasourceMgmtService;
  private DatabaseConnectionAdapter databaseConnectionAdapter = new DatabaseConnectionAdapter();

  /**
   * No-arg constructor for when in Pentaho BI Server.
   */
  public DefaultDatasourceMgmtWebService() {
    super();
    datasourceMgmtService = PentahoSystem.get( IDatasourceMgmtService.class );
    if ( datasourceMgmtService == null ) {
      throw new IllegalStateException();
    }
  }

  public DefaultDatasourceMgmtWebService( final IDatasourceMgmtService datasourceMgmtService ) {
    super();
    this.datasourceMgmtService = datasourceMgmtService;
  }

  @Override
  public String createDatasource( DatabaseConnectionDto dbConnDto ) {
    try {
      return datasourceMgmtService.createDatasource( databaseConnectionAdapter.unmarshal( dbConnDto ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public void deleteDatasourceByName( String name ) {
    try {
      datasourceMgmtService.deleteDatasourceByName( name );
    } catch ( NonExistingDatasourceException e ) {
      e.printStackTrace();
    } catch ( DatasourceMgmtServiceException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public DatabaseConnectionDto getDatasourceByName( String name ) {
    try {
      return databaseConnectionAdapter.marshal( (DatabaseConnection) datasourceMgmtService
        .getDatasourceByName( name ) );
    } catch ( Exception e ) {
      return null;
    }
  }

  @Override
  public List<DatabaseConnectionDto> getDatasources() {
    List<DatabaseConnectionDto> databaseConnections = new ArrayList<DatabaseConnectionDto>();
    try {
      for ( IDatabaseConnection databaseConnection : datasourceMgmtService.getDatasources() ) {
        try {
          databaseConnections.add( databaseConnectionAdapter.marshal( (DatabaseConnection) databaseConnection ) );
        } catch ( Exception e ) {
          // CHECKSTYLES IGNORE
        }
      }
    } catch ( DatasourceMgmtServiceException e ) {
      throw new RuntimeException( e );
    }
    return databaseConnections;
  }

  @Override
  public String updateDatasourceByName( String name, DatabaseConnectionDto databaseConnectionDto ) {
    try {
      return datasourceMgmtService.updateDatasourceByName( name, databaseConnectionAdapter
          .unmarshal( databaseConnectionDto ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public void deleteDatasourceById( String id ) {
    try {
      datasourceMgmtService.deleteDatasourceById( id );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public DatabaseConnectionDto getDatasourceById( String id ) {
    try {
      return databaseConnectionAdapter.marshal( (DatabaseConnection) datasourceMgmtService.getDatasourceById( id ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public List<String> getDatasourceIds() {
    try {
      return datasourceMgmtService.getDatasourceIds();
    } catch ( DatasourceMgmtServiceException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public String updateDatasourceById( String id, DatabaseConnectionDto databaseConnection ) {
    try {
      return datasourceMgmtService.updateDatasourceById( id,
        databaseConnectionAdapter.unmarshal( databaseConnection ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public void logout() {
    // no-op, handled in PentahoWSSpringServlet
  }

}
