package org.pentaho.platform.datasource;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.datasource.GenericDatasourceServiceException;
import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class JDBCDatasourceService implements IGenericDatasourceService{

  public static final String TYPE = "JDBC";
  IDatasourceMgmtService datasourceMgmtService;
  
  public JDBCDatasourceService() {
    datasourceMgmtService = PentahoSystem.get(IDatasourceMgmtService.class);
  }

  @Override
  public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException {
    try {
      if(datasource instanceof JDBCDatasource) {
        JDBCDatasource jdbcDatasource = (JDBCDatasource) datasource;
        datasourceMgmtService.createDatasource(jdbcDatasource.getDatasource());        
      } else {
        throw new GenericDatasourceServiceException("Object is not of type JDBCDatasource");
      }

    } catch (DuplicateDatasourceException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new GenericDatasourceServiceException(e);
    }
  }

  @Override
  public JDBCDatasource get(String id) {
    try {
      IDatabaseConnection databaseConnection = datasourceMgmtService.getDatasourceByName(id);
      return new JDBCDatasource(databaseConnection, databaseConnection.getName(), TYPE);
    } catch (DatasourceMgmtServiceException e) {
      return null;
    }
  }

  @Override
  public void remove(String id) throws GenericDatasourceServiceException {
    try {
      datasourceMgmtService.deleteDatasourceByName(id);
    } catch (NonExistingDatasourceException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new GenericDatasourceServiceException(e);
    }
  }

  @Override
  public void edit(IGenericDatasource datasource) throws GenericDatasourceServiceException {
    try {
      if(datasource instanceof JDBCDatasource) {
        JDBCDatasource jdbcDatasource = (JDBCDatasource) datasource;
        datasourceMgmtService.updateDatasourceByName(jdbcDatasource.getId(), jdbcDatasource.getDatasource());
      } else {
        throw new GenericDatasourceServiceException("Object is not of type JDBCDatasource");
      }
    } catch (NonExistingDatasourceException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new GenericDatasourceServiceException(e);
    }
  }

  @Override
  public List<IGenericDatasource> getAll() {
    List<IGenericDatasource> jdbcDatasourceList = new ArrayList<IGenericDatasource>();
    try {
      for(IDatabaseConnection databaseConnection:datasourceMgmtService.getDatasources()) {
        jdbcDatasourceList.add(new JDBCDatasource(databaseConnection, databaseConnection.getName(), TYPE));
      }
      return jdbcDatasourceList;
    } catch (DatasourceMgmtServiceException e) {
      return null;
    }
  }

  @Override
  public List<IGenericDatasourceInfo> getIds() {
    List<IGenericDatasourceInfo> datasourceInfoList = new ArrayList<IGenericDatasourceInfo>();
    try {
      for(IDatabaseConnection databaseConnection:datasourceMgmtService.getDatasources()) {
        datasourceInfoList.add(new GenericDatasourceInfo(databaseConnection.getName(), TYPE));
      }
      return datasourceInfoList;
    } catch (DatasourceMgmtServiceException e) {
      return null;
    }
  }
  @Override
  public String getType() {
    return TYPE;
  }

}
