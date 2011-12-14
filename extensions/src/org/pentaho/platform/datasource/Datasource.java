package org.pentaho.platform.datasource;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.datasource.IDatasource;

@XmlRootElement
public class Datasource implements IDatasource{
  private DatasourceInfo datasourceInfo;
  private String datasource;
  
  public Datasource() {
    datasourceInfo = new DatasourceInfo();
    datasource = new String();
  }
  public Datasource(DatasourceInfo datasourceInfo, String datasource) {
    this();
    this.setDatasourceInfo(datasourceInfo);
    this.setDatasource(datasource);
  }
  
  

  public void setDatasourceInfo(DatasourceInfo datasourceInfo) {
    this.datasourceInfo = datasourceInfo;
  }

  public DatasourceInfo getDatasourceInfo() {
    return datasourceInfo;
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  public String getDatasource() {
    return datasource;
  }
}
