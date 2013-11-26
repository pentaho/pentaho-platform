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

import org.apache.axis2.engine.AxisConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.AxisWebServiceManager;

import static org.junit.Assert.assertNotNull;

public class AxisConfiguratorTest {

  @Test
  @Ignore
  public void testInit() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession( session );

    @SuppressWarnings( "unused" )
    AxisConfiguration config = AxisWebServiceManager.currentAxisConfiguration;
    assertNotNull( "AxisConfig is null", AxisWebServiceManager.currentAxisConfiguration ); //$NON-NLS-1$

    // IWebServiceConfigurator axisConfigurator = config.getAxisConfigurator();
    //
    //    assertNotNull( "axisConfigurator is null", axisConfigurator ); //$NON-NLS-1$
    //
    // axisConfigurator.setSession( session );
    //
    //    AxisService service = config.getConfigurationContext().getAxisConfiguration().getService( "StubService" ); //$NON-NLS-1$
    //    assertNotNull( "test service is missing", service ); //$NON-NLS-1$
    //
    //    WebServiceDefinition wsDef = axisConfigurator.getWebServiceDefinition( "StubService" ); //$NON-NLS-1$
    //    assertNotNull( "wsDef is null", wsDef ); //$NON-NLS-1$
    //
    // //TODO: the definition bean no longer has access to the service object
    // // assertEquals( service, wsDef.getService() );
    // config.reset();
    //
    //    service = config.getConfigurationContext().getAxisConfiguration().getService( "StubService" ); //$NON-NLS-1$
    //    assertNotNull( "test service is missing after reset", service ); //$NON-NLS-1$
    //
    // axisConfigurator.unloadServices();
    //
    //    service = config.getConfigurationContext().getAxisConfiguration().getService( "StubService" ); //$NON-NLS-1$
    //    assertNull( "test service is still there", service ); //$NON-NLS-1$
    //
    // axisConfigurator.reloadServices();
    //
    //    service = config.getConfigurationContext().getAxisConfiguration().getService( "StubService" ); //$NON-NLS-1$
    //    assertNotNull( "test service is missing after reset", service ); //$NON-NLS-1$
    //
    // axisConfigurator.cleanup();

  }

}
