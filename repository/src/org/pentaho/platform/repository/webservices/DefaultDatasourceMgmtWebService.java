package org.pentaho.platform.repository.webservices;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

@WebService(endpointInterface = "org.pentaho.platform.repository.webservices.IDatasourceMgmtWebService", serviceName = "datasourceMgmtService", portName = "datasourceMgmtServicePort", targetNamespace = "http://www.pentaho.org/ws/1.0")
public class DefaultDatasourceMgmtWebService implements IDatasourceMgmtWebService{

  protected IDatasourceMgmtService datasourceMgmtService;
  private DatabaseConnectionAdapter databaseConnectionAdapter = new DatabaseConnectionAdapter();
  /**
   * No-arg constructor for when in Pentaho BI Server.
   */
  public DefaultDatasourceMgmtWebService() {
    super();
    datasourceMgmtService = PentahoSystem.get(IDatasourceMgmtService.class);
    if (datasourceMgmtService == null) {
      throw new IllegalStateException();
    }
  }

  public DefaultDatasourceMgmtWebService(final IDatasourceMgmtService datasourceMgmtService) {
    super();
    this.datasourceMgmtService = datasourceMgmtService;
  }

  @Override
  public void createDatasource(DatabaseConnectionDto dbConnDto){
    try {
      datasourceMgmtService.createDatasource(databaseConnectionAdapter.unmarshal(dbConnDto));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteDatasourceByName(String name) {
    try {
      datasourceMgmtService.deleteDatasourceByName(name);
    } catch (NonExistingDatasourceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (DatasourceMgmtServiceException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DatabaseConnectionDto getDatasourceByName(String name) {
    try {
      return databaseConnectionAdapter.marshal((DatabaseConnection) datasourceMgmtService.getDatasourceByName(name));
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public List<DatabaseConnectionDto> getDatasources() {
    List<DatabaseConnectionDto> databaseConnections = new ArrayList<DatabaseConnectionDto>();
    try {
      for(IDatabaseConnection databaseConnection:datasourceMgmtService.getDatasources()) {
        try {
          databaseConnections.add(databaseConnectionAdapter.marshal((DatabaseConnection)databaseConnection));
        } catch (Exception e) {
        }
      }
    } catch (DatasourceMgmtServiceException e) {
      throw new RuntimeException(e);
    }
    return databaseConnections;
  }

  @Override
  public void updateDatasourceByName(String name, DatabaseConnectionDto databaseConnectionDto) {
    try {
      datasourceMgmtService.updateDatasourceByName(name, databaseConnectionAdapter.unmarshal(databaseConnectionDto));
    } catch (Exception e) {
      throw new RuntimeException(e);      
    }
  }

}
