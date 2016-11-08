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

package org.pentaho.platform.plugin.services.webservices;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.AxisConfigBuilder;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractAxisConfigurator extends PentahoBase implements AxisConfigurator {

  private static final long serialVersionUID = 1931282897052453845L;

  protected AxisConfiguration axisConfig = null;

  protected IPentahoSession session; // Session to use during initialization

  protected boolean loaded = false;

  // map of the web service wrappers
  private Map<String, IServiceConfig> services = new HashMap<String, IServiceConfig>();

  public abstract Log getLogger();

  public abstract void addService( IServiceConfig ws );

  public AbstractAxisConfigurator() {
    init();
  }

  public void reloadServices() throws AxisFault {
    unloadServices();
    loadServices();
  }

  public void unloadServices() throws AxisFault {
    Set<String> keys = services.keySet();
    List<String> removed = new ArrayList<String>();
    // iterate through the list of web service wrappers
    for ( String key : keys ) {
      IServiceConfig wrapper = services.get( key );

      // use the service name to remove them from the Axis system
      String serviceName = wrapper.getServiceClass().getSimpleName();
      axisConfig.removeService( serviceName );
      // build a list of the ones removed
      removed.add( serviceName );
    }
    // now remove the wrappers from the services list
    for ( String serviceName : removed ) {
      services.remove( serviceName );
    }
    loaded = false;
  }

  public IServiceConfig getWebServiceDefinition( String name ) {
    return services.get( name );
  }

  /**
   * Creates the AxisConfiguration object using an XML document. Subclasses of this class must provide the XML via an
   * input stream. The concrete implementation can store the XML file wherever it wants as we only need an InputStream
   */
  public AxisConfiguration getAxisConfiguration() throws AxisFault {
    if ( axisConfig != null ) {
      // we have already initialized
      return axisConfig;
    }
    try {

      // create a new AxisConfiguration
      axisConfig = new AxisConfiguration();

      // get the config XML input stream
      InputStream in = getConfigXml();
      // build the configuration
      AxisConfigBuilder builder = new AxisConfigBuilder( in, axisConfig, null );
      builder.populateConfig();
    } catch ( Exception e ) {
      e.printStackTrace();
      throw AxisFault.makeFault( e );
    }
    // set this object as the Axis configurator. Axis will call loadServices().
    axisConfig.setConfigurator( this );
    return axisConfig;
  }

  /*
   * These are the abstract methods
   */
  public abstract InputStream getConfigXml();

  public abstract boolean setEnabled( String name, boolean enabled ) throws AxisFault;

  public abstract void init();

  /**
   * Adds any implementation-specific web service endpoints for a Axis service
   * 
   * @param axisService
   *          The Axis web service to add end points to
   */
  protected abstract void addServiceEndPoints( AxisService axisService );

  /**
   * Adds any implementation-specific transports for a Axis service
   * 
   * @param axisService
   *          The Axis web service to add end points to
   */
  protected abstract void addTransports( AxisService axisService );

  /**
   * Returns a list of the web service wrappers for this implmentation
   * 
   * @return
   */
  protected abstract List<IServiceConfig> getWebServiceDefinitions();

  /**
   * Load the web services from the list of web service wrappers
   */
  public void loadServices() {

    // JD - only setup the Axis services once
    if ( loaded ) {
      // we have done this already
      return;
    }

    List<IServiceConfig> wsDfns = getWebServiceDefinitions();

    for ( IServiceConfig wsDef : wsDfns ) {
      try {
        loadService( wsDef );
      } catch ( Exception e ) {
        // Axis cannot handle a typed exception from this method, we must just log the error and continue on
        Logger.error( getClass().getName(), Messages.getInstance().getErrorString(
          "AbstractAxisConfigurator.ERROR_0001_COULD_NOT_LOAD_SERVICE", wsDef.getId() ), e ); //$NON-NLS-1$
      }
    }
    loaded = true;

  }

  /**
   * Loads a web service from a web service wrapper
   * 
   * @param wrapper
   *          Web service wrapper
   * @throws Exception
   */
  protected void loadService( IServiceConfig wsDef ) throws Exception {

    // first create the service
    String serviceId = wsDef.getId();
    AxisService axisService = AxisUtil.createService( wsDef, getAxisConfiguration() );

    // add any additional transports
    addTransports( axisService );

    // add any end points
    addServiceEndPoints( axisService );

    // JD - don't create the WSDL yet, do it on demand. Just store the wsDef for now
    Parameter wsDefParam = new Parameter();
    wsDefParam.setName( "wsDefParam" ); //$NON-NLS-1$
    wsDefParam.setValue( wsDef ); //$NON-NLS-1$
    axisService.addParameter( wsDefParam );

    // add the wrapper to the service list
    services.put( serviceId, wsDef );

    // start the service
    axisConfig.addService( axisService );
    axisConfig.startService( axisService.getName() );

    // enable or disable the service as the wrapper dictates
    axisService.setActive( wsDef.isEnabled() );

  }

  /**
   * An AxisConfigurator method that we don't need
   */
  public void engageGlobalModules() throws AxisFault {

  }

  /**
   * An AxisConfigurator method that we don't need
   */
  public void cleanup() {

  }

  public void setSession( IPentahoSession session ) {
    this.session = session;
  }

}
