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
