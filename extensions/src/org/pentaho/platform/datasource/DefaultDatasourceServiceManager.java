package org.pentaho.platform.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;
import org.pentaho.platform.api.datasource.IGenericDatasourceServiceManager;

public class DefaultDatasourceServiceManager implements IGenericDatasourceServiceManager{

  Map<String, IGenericDatasourceService> serviceMap = new HashMap<String, IGenericDatasourceService>();
  
  public DefaultDatasourceServiceManager() {
    
  }
  
  public DefaultDatasourceServiceManager(List<IGenericDatasourceService> services) {
    for(IGenericDatasourceService  service:services) {
      registerService(service);
    }
  }
  @Override
  public void registerService(IGenericDatasourceService service) {
    serviceMap.put(service.getType(), service);
  }

  @Override
  public IGenericDatasourceService getService(String serviceType) {
    return serviceMap.get(serviceType);
  }

  @Override
  public List<IGenericDatasource> getAll() {
    List<IGenericDatasource> genericDatasourceList = new ArrayList<IGenericDatasource>();
    for(IGenericDatasourceService service:serviceMap.values()) {
      try {
        genericDatasourceList.addAll(service.getAll());
      } catch(Throwable th) {
        continue;
      }
    }

    return genericDatasourceList;
  }

  @Override
  public List<IGenericDatasourceInfo> getIds() {
    List<IGenericDatasourceInfo> genericDatasourceList = new ArrayList<IGenericDatasourceInfo>();
    for(IGenericDatasourceService service:serviceMap.values()) {
      try {
        List<IGenericDatasourceInfo> infoList = service.getIds();
        if(infoList != null && infoList.size() > 0) {
          genericDatasourceList.addAll(service.getIds());
        }
      } catch(Throwable th) {
        continue;
      }
    }
    return genericDatasourceList;
  }

  @Override
  public List<String> getTypes() {
    return new ArrayList<String>(serviceMap.keySet());
  }

}
