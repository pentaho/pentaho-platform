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

package org.pentaho.test.platform.plugin.services.webservices;

import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.ServiceConfig;
import org.pentaho.platform.plugin.services.webservices.SystemSolutionAxisConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StubServiceSetup extends SystemSolutionAxisConfigurator {

  private static final long serialVersionUID = 3383802441135983726L;

  protected static StubServiceSetup instance = null;

  private static final String BASE_URL = "http://testhost:8080/testcontext/"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog( StubServiceSetup.class );

  public StubServiceSetup() {
    super();
  }

  public void init() {

  }

  public Log getLogger() {
    return logger;
  }

  @Override
  public InputStream getConfigXml() {

    try {
      File f = new File( "test-src/webservices-solution/system/axis2_config.xml" ); //$NON-NLS-1$
      return new FileInputStream( f );
    } catch ( Exception e ) {
      // TODO log this
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public boolean setEnabled( String name, boolean enabled ) {
    ServiceConfig wrapper = (ServiceConfig) getWebServiceDefinition( name );
    wrapper.setEnabled( enabled );
    // FIXME: service is not available through the definition bean
    // AxisService axisService = wrapper.getService( );
    // axisService.setActive( enabled );
    return true;
  }

  @Override
  protected List<IServiceConfig> getWebServiceDefinitions() {
    List<IServiceConfig> wrappers = new ArrayList<IServiceConfig>();
    wrappers.add( new StubServiceWrapper() );
    wrappers.add( new StubService2Wrapper() );
    wrappers.add( new StubService3Wrapper() );
    return wrappers;
  }

  @Override
  protected void addTransports( AxisService axisService ) {

    ArrayList<String> transports = new ArrayList<String>();
    transports.add( "http" ); //$NON-NLS-1$
    axisService.setExposedTransports( transports );
  }

  @Override
  protected void addServiceEndPoints( AxisService axisService ) {
    String endPoint1 = BASE_URL + "content/ws-run/" + axisService.getName(); //$NON-NLS-1$
    String endPoint2 = "http:test"; //$NON-NLS-1$

    ArrayList<String> transports = new ArrayList<String>();
    transports.add( "http" ); //$NON-NLS-1$
    axisService.setExposedTransports( transports );
    axisService.setEPRs( new String[] { endPoint1, endPoint2 } );
  }

}
