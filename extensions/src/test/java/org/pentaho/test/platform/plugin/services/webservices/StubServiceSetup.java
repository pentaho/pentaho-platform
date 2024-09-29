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


package org.pentaho.test.platform.plugin.services.webservices;

import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.ServiceConfig;
import org.pentaho.platform.plugin.services.webservices.SystemSolutionAxisConfigurator;
import org.pentaho.test.platform.utils.TestResourceLocation;

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
      File f = new File( TestResourceLocation.TEST_RESOURCES + "/webservices-solution/system/axis2_config.xml" ); //$NON-NLS-1$
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
