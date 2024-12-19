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
import org.pentaho.platform.api.engine.ServiceInitializationException;

/**
 * The platform's service manager, {@link IServiceManager}, may delegate the handling of particular types of services to
 * an implemention of this interface. Having service types handled by their own managers will allow us manage (e.g.
 * enable/disable) all services of a particular type.
 */
public interface IServiceTypeManager {

  /**
   * Registers a service with this svc type manager
   * 
   * @see IServiceManager#registerService(IServiceConfig)
   */
  public void registerService( final IServiceConfig wsDefinition ) throws ServiceException;

  /**
   * Returns an instance of a registered servicing object.
   * 
   * @param serviceId
   *          the unique id of the service
   * @return an instance of the servicing object
   * @throws ServiceException
   *           if no service object can be found or there was a problem retrieving the service object
   */
  public Object getServiceBean( String serviceId ) throws ServiceException;

  /**
   * Returns the service type
   */
  public String getSupportedServiceType();

  /**
   * Performs any initialization this service type requires
   * 
   * @throws ServiceInitializationException
   */
  public void initServices() throws ServiceInitializationException;

  /**
   * Retrieves the config for a particular service
   * 
   * @param serviceId
   * @return the config
   */
  public IServiceConfig getServiceConfig( String serviceId );

}
