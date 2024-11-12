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

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.engine.ServiceInitializationException;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The default implementation of {@link IServiceManager}
 * 
 * @author aaron
 * 
 */
public class DefaultServiceManager implements IServiceManager {

  public Map<String, IServiceTypeManager> serviceManagerMap = new HashMap<String, IServiceTypeManager>();

  public void setServiceTypeManagers( Collection<IServiceTypeManager> serviceTypeManagers ) {
    for ( IServiceTypeManager handler : serviceTypeManagers ) {
      String type = handler.getSupportedServiceType();
      if ( type == null ) {
        throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "DefaultServiceManager.ERROR_0001_INVALID_SERVICE_TYPE" ) ); //$NON-NLS-1$
      }
      serviceManagerMap.put( type, handler );
      Logger.info( getClass().toString(), Messages.getInstance().getString(
          "DefaultServiceManager.REGISTERED_SERVICE_TYPES", handler.getSupportedServiceType() ) ); //$NON-NLS-1$
    }
  }

  public void registerService( final IServiceConfig config ) throws ServiceException {
    validate( config );
    String type = config.getServiceType();
    IServiceTypeManager mgr = serviceManagerMap.get( type );
    if ( mgr == null ) {
      String availableTypes =
          StringUtils.join( serviceManagerMap.keySet().iterator(), Messages.getInstance().getString( "," ) ); //$NON-NLS-1$
      throw new ServiceException(
          Messages
              .getInstance()
              .getErrorString(
                "DefaultServiceManager.ERROR_0002_NO_SERVICE_MANAGER_FOR_TYPE", config.getId(), type.toString(),
                availableTypes ) ); //$NON-NLS-1$
    }
    mgr.registerService( config );
  }

  private static void validate( IServiceConfig config ) {
    if ( StringUtils.isEmpty( config.getId() ) ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "DefaultServiceManager.ERROR_0003_INVALID_SERVICE_CONFIG", "id" ) ); //$NON-NLS-1$//$NON-NLS-2$
    }
    if ( config.getServiceClass() == null ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "DefaultServiceManager.ERROR_0003_INVALID_SERVICE_CONFIG", "class" ) ); //$NON-NLS-1$//$NON-NLS-2$
    }
    if ( config.getServiceType() == null ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "DefaultServiceManager.ERROR_0003_INVALID_SERVICE_CONFIG", "type" ) ); //$NON-NLS-1$//$NON-NLS-2$
    }
  }

  public Object getServiceBean( String serviceType, String serviceId ) throws ServiceException {
    return serviceManagerMap.get( serviceType ).getServiceBean( serviceId );
  }

  public IServiceConfig getServiceConfig( String serviceType, String serviceId ) {
    return serviceManagerMap.get( serviceType ).getServiceConfig( serviceId );
  }

  public void initServices() throws ServiceInitializationException {
    for ( IServiceTypeManager handler : serviceManagerMap.values() ) {
      handler.initServices();
    }
  }
}
