package org.pentaho.platform.api.datasource;

import java.util.List;

/**
 * Register, manage and provide basic listing functionality of platform datasources.  
 */

public interface IGenericDatasourceServiceManager {

  /**
   * This method registera a datasource service to the platform 
   * @param service
   */
  public void registerService(IGenericDatasourceService service);

  /**
   * Returns an instance of a service for a given datasource type
   * @param serviceType
   * @return datasource service
   */
  public IGenericDatasourceService getService(String datasourceType);

  /**
   * Returns all the datasource types that are registered with the service manager
   * @return list of datasource type
   */
  public List<String> getTypes();
  
  /**
   * Returns a list of datasource objects with a managed object wrapped inside
   * @return list of datasource
   */
  public List<IGenericDatasource> getAll();
  
  /**
   * Returns a list of datasource id
   * @return list of datasource id
   */
  public List<IGenericDatasourceInfo> getIds();

}
