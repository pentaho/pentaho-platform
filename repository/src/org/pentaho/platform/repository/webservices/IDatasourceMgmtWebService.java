package org.pentaho.platform.repository.webservices;

import java.util.List;

import javax.jws.WebService;

@WebService
public interface IDatasourceMgmtWebService {

  public void createDatasource(DatabaseConnectionDto databaseConnection);

  public void deleteDatasourceByName(String name) ;

  public DatabaseConnectionDto getDatasourceByName(String name);

  public List<DatabaseConnectionDto> getDatasources();
  
  public void updateDatasourceByName(String name, DatabaseConnectionDto databaseConnection);

}
