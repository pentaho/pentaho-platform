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


package org.pentaho.test.platform.plugin.pluginmgr;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.GwtRpcServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.IServiceTypeManager;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.ServiceConfig;
import org.pentaho.test.platform.engine.core.EchoServiceBean;

import java.util.Arrays;

import static org.junit.Assert.*;

@SuppressWarnings( "nls" )
public class DefaultServiceManagerTest {

  DefaultServiceManager serviceManager;

  @Before
  public void init() {
    serviceManager = new DefaultServiceManager();
    IServiceTypeManager gwtHandler = new GwtRpcServiceManager();
    serviceManager.setServiceTypeManagers( Arrays.asList( gwtHandler ) );
  }

  @Test
  public void testServiceRegistration() throws ServiceException {
    ServiceConfig config = new ServiceConfig();
    config.setId( "testId" );
    config.setServiceClass( EchoServiceBean.class );
    config.setServiceType( "gwt" );
    serviceManager.registerService( config );

    assertNotNull( serviceManager.getServiceConfig( "gwt", "testId" ) );
  }

  @Test
  public void testGetServiceBean() throws ServiceException {
    testServiceRegistration();

    Object serviceBean = serviceManager.getServiceBean( "gwt", "testId" );
    assertNotNull( serviceBean );
    assertTrue( serviceBean instanceof EchoServiceBean );
  }

  @Test
  public void testGetServiceConfig() throws ServiceException {
    testServiceRegistration();

    IServiceConfig config = serviceManager.getServiceConfig( "gwt", "testId" );
    assertNotNull( config );
    assertEquals( "testId", config.getId() );
    assertEquals( EchoServiceBean.class, config.getServiceClass() );
    assertEquals( "gwt", config.getServiceType() );
  }

  @Test( expected = IllegalStateException.class )
  public void testRegisterInvalidService() throws ServiceException {
    ServiceConfig config = new ServiceConfig();
    serviceManager.registerService( config );
  }
}
