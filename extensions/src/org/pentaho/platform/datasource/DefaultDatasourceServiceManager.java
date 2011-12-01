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

import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceService;
import org.pentaho.platform.api.datasource.IDatasourceServiceManager;

public class DefaultDatasourceServiceManager implements IDatasourceServiceManager{

  Map<String, IDatasourceService> serviceMap = new HashMap<String, IDatasourceService>();
  
  public DefaultDatasourceServiceManager() {
    
  }
  
  public DefaultDatasourceServiceManager(List<IDatasourceService> services) {
    for(IDatasourceService  service:services) {
      registerService(service);
    }
  }
  @Override
  public void registerService(IDatasourceService service) {
    serviceMap.put(service.getType(), service);
  }

  @Override
  public IDatasourceService getService(String serviceType) {
    return serviceMap.get(serviceType);
  }

  @Override
  public List<IDatasourceInfo> getIds() {
    List<IDatasourceInfo> datasourceList = new ArrayList<IDatasourceInfo>();
    for(IDatasourceService service:serviceMap.values()) {
      try {
        List<IDatasourceInfo> infoList = service.getIds();
        if(infoList != null && infoList.size() > 0) {
          datasourceList.addAll(service.getIds());
        }
      } catch(Throwable th) {
        continue;
      }
    }
    return datasourceList;
  }

  @Override
  public List<String> getTypes() {
    return new ArrayList<String>(serviceMap.keySet());
  }

}
