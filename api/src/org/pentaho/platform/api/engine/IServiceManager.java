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

package org.pentaho.platform.api.engine;

/**
 * A service manager allows a POJO to be exposed as various types of services by constructing a simple
 * {@link ServiceConfig} and calling {@link #registerService(ServiceConfig)}. An implementation of
 * {@link IServiceManager} acts as a depot of service objects in the Pentaho BI platform.
 * <p>
 * For information on providing services via platform plugins, see the wiki link below.
 * 
 * @see {@link http 
 *      ://wiki.pentaho.com/display/ServerDoc2x/Developing+Plugins#DevelopingPlugins-Definingawebservice
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
