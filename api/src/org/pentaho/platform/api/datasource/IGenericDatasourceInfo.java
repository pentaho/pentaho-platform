package org.pentaho.platform.api.datasource;

import java.io.Serializable;

/**
 * Basic information about a datasource object
 * 
 *
 */
public interface IGenericDatasourceInfo extends Serializable{

  /**
   * Returns a datasource id
   * @return id
   */
  public String getId();

  /**
   * Returns a datasource type
   * @return type
   */
  public String getType();

  /**
   * Stores a datasource id
   * @param id
   */
  public void setId(String id);
  
  /**
   * Stored a datasource type
   * @param type
   */
  public void setType(String type);

}
