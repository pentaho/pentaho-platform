/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.plugin.services.pluginmgr.servicemgr;

import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.api.engine.ServiceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractServiceTypeManager implements IServiceTypeManager {

  protected Collection<IServiceConfig> registeredServiceConfigs = new ArrayList<IServiceConfig>();
  protected Map<String, Class<?>> serviceClassMap = new HashMap<String, Class<?>>();
  protected Map<String, Object> serviceInstanceMap = new HashMap<String, Object>();

  public void registerService( final IServiceConfig wsConfig ) {
    serviceClassMap.put( wsConfig.getId(), wsConfig.getServiceClass() );
    registeredServiceConfigs.add( wsConfig );
  }

  public IServiceConfig getServiceConfig( String serviceId ) {
    for ( IServiceConfig config : registeredServiceConfigs ) {
      if ( config.getId().equals( serviceId ) ) {
        return config;
      }
    }
    return null;
  }

  public Object getServiceBean( String serviceId ) throws ServiceException {
    Object serviceInstance = serviceInstanceMap.get( serviceId );
    if ( serviceInstance == null ) {
      try {
        serviceInstance = serviceClassMap.get( serviceId ).newInstance();
        serviceInstanceMap.put( serviceId, serviceInstance );
      } catch ( InstantiationException e ) {
        throw new ServiceException( e );
      } catch ( IllegalAccessException e ) {
        throw new ServiceException( e );
      }
    }
    return serviceInstance;
  }
}
