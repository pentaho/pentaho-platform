package org.pentaho.platform.datasource;

import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;

public class MondrianDatasource extends GenericDatasourceInfo implements IGenericDatasource{

  private static final long serialVersionUID = 1L;
  private MondrianCatalog datasource;;
  
  
  public MondrianDatasource(MondrianCatalog datasource, String id, String type) {
    super(id, type);
    this.datasource = datasource;
  }
  @Override
  public MondrianCatalog getDatasource() {
    return this.datasource;
  }
  @Override
  public void setDatasource(Object datasource) {
    if(datasource instanceof MondrianCatalog) {
      this.datasource = (MondrianCatalog)datasource;      
    }
  }
}
