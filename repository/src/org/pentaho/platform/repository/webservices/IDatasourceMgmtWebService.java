package org.pentaho.platform.repository.webservices;

import java.util.List;

import javax.jws.WebService;


@WebService
public interface IDatasourceMgmtWebService {

  public String createDatasource(DatabaseConnectionDto databaseConnection);

  public void deleteDatasourceByName(String name) ;

  public DatabaseConnectionDto getDatasourceByName(String name);

  public List<DatabaseConnectionDto> getDatasources();
  
  public String updateDatasourceByName(String name, DatabaseConnectionDto databaseConnection);
  
  public void deleteDatasourceById(String id);

  public DatabaseConnectionDto getDatasourceById(String id);

  public List<String> getDatasourceIds();

  public String updateDatasourceById(String id, DatabaseConnectionDto databaseConnection);

}
