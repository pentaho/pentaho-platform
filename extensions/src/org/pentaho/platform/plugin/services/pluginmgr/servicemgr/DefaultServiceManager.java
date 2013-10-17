/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
