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


package org.pentaho.platform.api.engine;

/**
 * A service manager allows a POJO to be exposed as various types of services by constructing a simple
 * {@link ServiceConfig} and calling {@link #registerService(ServiceConfig)}. An implementation of
 * {@link IServiceManager} acts as a depot of service objects in the Pentaho BI platform.
 * <p>
 * For information on providing services via platform plugins, see the wiki link below.
 * 
 * @see {@link https
 *      ://pentaho-community.atlassian.net/wiki/display/ServerDoc2x/Developing+Plugins#DevelopingPlugins-Definingawebservice
 *      (newinCitrus)}
 * 
 * @author aphillips
 * 
 */
public interface IServiceManager {

  /**
   * Registers a service with the service manager. The service may not become active until
   * {@link #initServices(IPentahoSession)} has been called.
   * 
   * @param wsDefinition
   *          the web service definition
   * @throws ServiceException
   */
  public void registerService( final IServiceConfig config ) throws ServiceException;

  /**
   * Activates the services that have been registered with the service manager.
   * 
   * @param session
   *          the current session
   * @throws ServiceInitializationException
   */
  public void initServices() throws ServiceInitializationException;

  /**
   * Returns an instance of a registered servicing object.
   * 
   * @param serviceType
   *          the type of the service, used to lookup the correct service class
   * @param serviceId
   *          the unique id of the service
   * @return an instance of the servicing object
   * @throws ServiceException
   *           if no service object can be found or there was a problem retrieving the service object
   */
  public Object getServiceBean( String serviceType, String serviceId ) throws ServiceException;

  /**
   * Gets the configuration for the requested service.
   * 
   * @param serviceType
   *          the type of the service, used to lookup the correct service class
   * @param serviceId
   *          the unique id of the service
   * @return configuration of the service
   */
  public IServiceConfig getServiceConfig( String serviceType, String serviceId );

}
