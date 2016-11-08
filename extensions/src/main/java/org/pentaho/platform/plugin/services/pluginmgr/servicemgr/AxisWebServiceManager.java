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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.api.engine.ServiceInitializationException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.services.webservices.AbstractAxisConfigurator;
import org.pentaho.platform.plugin.services.webservices.AxisUtil;
import org.pentaho.platform.plugin.services.webservices.SystemSolutionAxisConfigurator;

public class AxisWebServiceManager extends AbstractServiceTypeManager {

  public static ConfigurationContext currentAxisConfigContext;

  public static AxisConfiguration currentAxisConfiguration;

  public void setExecuteServiceId( String executeServiceId ) {
    AxisUtil.WS_EXECUTE_SERVICE_ID = executeServiceId;
  }

  public void setWsdlServiceId( String wsdlServiceId ) {
    AxisUtil.WSDL_SERVICE_ID = wsdlServiceId;
  }

  private SystemSolutionAxisConfigurator configurator = new SystemSolutionAxisConfigurator();

  protected AbstractAxisConfigurator getConfigurator() {
    return configurator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.plugin.services.pluginmgr.IServiceManager#defineService(org.pentaho.platform.plugin.services
   * .pluginmgr.WebServiceDefinition)
   */
  @Override
  public void registerService( final IServiceConfig wsConfig ) {
    super.registerService( wsConfig );
    getConfigurator().addService( wsConfig );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.plugin.services.pluginmgr.IServiceManager#initServices()
   */
  public void initServices() throws ServiceInitializationException {
    getConfigurator().setSession( PentahoSessionHolder.getSession() );
    AxisConfigurator axisConfigurator = getConfigurator();

    // create the axis configuration and make it accessible to content generators via static member
    ConfigurationContext configContext = null;
    try {
      configContext = ConfigurationContextFactory.createConfigurationContext( axisConfigurator );
    } catch ( AxisFault e ) {
      throw new ServiceInitializationException( e );
    }
    configContext.setProperty( Constants.CONTAINER_MANAGED, Constants.VALUE_TRUE );

    currentAxisConfigContext = configContext;
    currentAxisConfiguration = configContext.getAxisConfiguration();

    // now load the services
    axisConfigurator.loadServices();
  }

  public String getSupportedServiceType() {
    return "xml"; //$NON-NLS-1$
  }
}
