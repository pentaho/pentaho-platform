package org.pentaho.platform.datasource;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.datasource.IGenericDatasource;

public class MetadataDatasource extends GenericDatasourceInfo implements IGenericDatasource{

  private static final long serialVersionUID = 1L;
  private Domain datasource;

  public MetadataDatasource(Domain datasource, String name, String id, String type) {
    super(name, id, type);
    this.datasource = datasource;
  }
  @Override
  public Domain getDatasource() {
    return this.datasource;
  }

  @Override
  public void setDatasource(Object datasource) {
    if(datasource instanceof Domain) {
      this.datasource = (Domain)datasource;      
    }
  }



}
