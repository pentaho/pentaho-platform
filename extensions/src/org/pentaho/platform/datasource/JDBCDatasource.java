package org.pentaho.platform.datasource;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.datasource.IGenericDatasource;

public class JDBCDatasource extends GenericDatasourceInfo implements IGenericDatasource{

  private static final long serialVersionUID = 1L;
  private IDatabaseConnection datasource;
  
  public JDBCDatasource(IDatabaseConnection datasource, String name, String id, String type) {
    super(name, id, type);
    this.datasource = datasource;
  }
  @Override
  public IDatabaseConnection getDatasource() {
    return this.datasource;
  }
  @Override
  public void setDatasource(Object datasource) {
    if(datasource instanceof IDatabaseConnection) {
      this.datasource = (IDatabaseConnection)datasource;      
    }
  }
}
