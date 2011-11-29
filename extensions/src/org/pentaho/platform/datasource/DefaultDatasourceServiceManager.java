/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved. 
 *
 *
 * @created Nov 12, 2011
 * @author Ramaiz Mansoor
 */
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
