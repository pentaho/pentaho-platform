/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
import org.pentaho.platform.plugin.services.webservices.SystemSolutionAxisConfigurator;

public class AxisWebServiceManager extends AbstractServiceTypeManager {

  public static ConfigurationContext currentAxisConfigContext;

  public static AxisConfiguration currentAxisConfiguration;

  /**
   * This method will throw an {@link UnsupportedOperationException} if called.
   * @param executeServiceId
   */
  @Deprecated
  public void setExecuteServiceId( String executeServiceId ) {
    throw new UnsupportedOperationException();
  }

  /**
   * This method will throw an {@link UnsupportedOperationException} if called.
   * @param wsdlServiceId
   */
  @Deprecated
  public void setWsdlServiceId( String wsdlServiceId ) {
    throw new UnsupportedOperationException();
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
