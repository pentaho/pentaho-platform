package org.pentaho.platform.repository.webservices;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;

public class DatasourceMgmtToWebServiceAdapter implements IDatasourceMgmtService{

  private IDatasourceMgmtWebService datasourceMgmtWebService;
  private DatabaseConnectionAdapter databaseConnectionAdapter = new DatabaseConnectionAdapter();
  
  public DatasourceMgmtToWebServiceAdapter(IDatasourceMgmtWebService datasourceMgmtWebService) {
    super();
    this.datasourceMgmtWebService = datasourceMgmtWebService;
  }
  
  @Override
  public void init(IPentahoSession session) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void createDatasource(IDatabaseConnection databaseConnection) {
    try {
      datasourceMgmtWebService.createDatasource(databaseConnectionAdapter.marshal((DatabaseConnection)databaseConnection));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void deleteDatasourceByName(String name) {
    datasourceMgmtWebService.deleteDatasourceByName(name);
  }

  @Override
  public IDatabaseConnection getDatasourceByName(String name) {
    try {
      return databaseConnectionAdapter.unmarshal(datasourceMgmtWebService.getDatasourceByName(name));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      return null;
    }
  }

  @Override
  public List<IDatabaseConnection> getDatasources(){
    List<IDatabaseConnection> databaseConnections = new ArrayList<IDatabaseConnection>();
    for(DatabaseConnectionDto databaseConnection:datasourceMgmtWebService.getDatasources()) {
      try {
        databaseConnections.add(databaseConnectionAdapter.unmarshal(databaseConnection));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        return null;
      }
    }
    return databaseConnections;
    
  }

  @Override
  public void updateDatasourceByName(String name, IDatabaseConnection databaseConnection){
    try {
      datasourceMgmtWebService.updateDatasourceByName(name, databaseConnectionAdapter.marshal((DatabaseConnection)databaseConnection));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
