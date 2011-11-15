package org.pentaho.platform.api.datasource;

/**
 * Contains a managed object for a particular datasource tyoe
 * 
 *
 */
public interface IGenericDatasource extends IGenericDatasourceInfo{

  /**
   * Returns the managed datasource
   * @return
   */
  public Object getDatasource();

  /**
   * Stores the managed datasource
   * @param datasource
   */
  public void setDatasource(Object datasource);
}
