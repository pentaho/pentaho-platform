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
