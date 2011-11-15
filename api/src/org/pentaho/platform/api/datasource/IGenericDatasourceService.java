package org.pentaho.platform.api.datasource;

import java.util.List;

/**
 * This is a wrapper service which provides a basic a CRUD operations 
 * for a datasource service
 */
public interface IGenericDatasourceService {
  
  /**
   * Returns the datsource type
   * @return datasource type
   */
  public String getType();
  
  /**
   * Add a new datasource to the platform repository
   * @param datasource
   * @throws GenericDatasourceServiceException
   */
  public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException;
  
  /**
   * Returns a datasource object with a managed object wrapped inside
   * @param id
   * @return datasource object
   */
  public IGenericDatasource get(String id);
  
  /**
   * Removes a selected datasource from the platform repository
   * @param id
   * @throws GenericDatasourceServiceException
   */
  public void remove(String id)throws GenericDatasourceServiceException;

  /**
   * Edits a selected datasource in a platform repository
   * @param datasource
   * @throws GenericDatasourceServiceException
   */
  public void edit(IGenericDatasource datasource)throws GenericDatasourceServiceException;
  
  /**
   * Returns all datasources of a particular type
   * @return list of datasource
   */
  public List<IGenericDatasource> getAll();
  
  /**
   * Returns all datasource ids for a particular type
   * @return list of datasource ids
   */
  public List<IGenericDatasourceInfo> getIds();
}
