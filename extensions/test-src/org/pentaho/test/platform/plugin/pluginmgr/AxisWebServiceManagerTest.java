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
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginServiceDefinition;
import org.pentaho.platform.api.engine.ServiceInitializationException;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PlatformPlugin;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.AxisWebServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.IServiceTypeManager;
import org.pentaho.platform.plugin.services.webservices.content.StyledHtmlAxisServiceLister;
import org.pentaho.test.platform.engine.core.EchoServiceBean;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.services.ContentGeneratorUtil;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings( "nls" )
public class AxisWebServiceManagerTest {

  private MicroPlatform microPlatform;

  /*
   * Wire up an in-memory platform to register and expose plug-in web services.
   */
  @SuppressWarnings( "deprecation" )
  @Before
  public void init0() throws ServiceInitializationException {
    // set solution path to a place that hosts an axis config file
    microPlatform = new MicroPlatform( "test-res/AxisWebServiceManagerTest/" );
    assertNotNull( PentahoSystem.getObjectFactory() );
    microPlatform.define( ISolutionEngine.class, SolutionEngine.class );
    assertNotNull( PentahoSystem.getObjectFactory() );
    microPlatform.define( IPluginManager.class, DefaultPluginManager.class, Scope.GLOBAL );
    microPlatform.define( IServiceManager.class, DefaultServiceManager.class, Scope.GLOBAL );
    microPlatform.define( IPluginProvider.class, TstPluginProvider.class );

    IServiceTypeManager axisManager = new AxisWebServiceManager();
    DefaultServiceManager sm = (DefaultServiceManager) PentahoSystem.get( IServiceManager.class );
    sm.setServiceTypeManagers( Arrays.asList( axisManager ) );

    microPlatform.init();

    new StandaloneSession();

    PentahoSystem.get( IPluginManager.class ).reload( PentahoSessionHolder.getSession() );
  }

  /*
   * The following tests are checking that the HtmlServiceLister (content generator) outputs the correct meta
   * information about the services defined in the test plugin. They are integration tests in the sense that all the
   * work of registering the plugin that defines the services and content generators is done by actual platform modules
   * as it would normally happen, and is not mocked for these tests.
   */

  @Test
  public void testExecuteUrlListed() throws Exception {

    IContentGenerator serviceLister = new StyledHtmlAxisServiceLister();

    String html = ContentGeneratorUtil.getContentAsString( serviceLister );
    System.out.println( html );

    assertTrue( "Run URL is missing", html.contains( "/content/ws-run/echoService" ) );
  }

  @Test
  public void testWsdlUrlListed() throws Exception {
    IContentGenerator serviceLister = new StyledHtmlAxisServiceLister();

    String html = ContentGeneratorUtil.getContentAsString( serviceLister );
    System.out.println( html );

    assertTrue( "WSDL URL is missing", html.contains( "/content/ws-wsdl/echoService" ) );
  }

  @Test
  @Ignore
  public void testListingPageStyled() throws Exception {
    IContentGenerator serviceLister = new StyledHtmlAxisServiceLister();

    String html = ContentGeneratorUtil.getContentAsString( serviceLister );
    System.out.println( html );

    assertTrue( "style is missing", html.contains( ".h1" ) );
    assertTrue( "style is missing", html.contains( "text/css" ) );
  }

  @Test
  public void testMetaInf() throws Exception {
    IContentGenerator serviceLister = new StyledHtmlAxisServiceLister();

    String html = ContentGeneratorUtil.getContentAsString( serviceLister );
    System.out.println( html );

    assertTrue( "title is not displayed", html.contains( "junit echo service" ) );
  }

  public static class TstPluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setId( "testPlugin" );
      p.setSourceDescription( "" );

      ContentGeneratorInfo cg1 = new ContentGeneratorInfo();
      cg1.setDescription( "Mock web service execution generator" );
      cg1.setId( "ws-run" );
      cg1.setType( "ws-run" ); // type is used as the key to verify that there is a cg that can handle a ws request
      cg1.setTitle( "Mock web service execution generator" );
      cg1.setClassname( "org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1" );
      p.addContentGenerator( cg1 );

      ContentGeneratorInfo cg2 = new ContentGeneratorInfo();
      cg2.setDescription( "Mock WSDL generator" );
      cg2.setId( "ws-wsdl" );
      cg2.setType( "ws-wsdl" ); // type is used as the key to verify that there is a cg that can handle a wsdl request
      cg2.setTitle( "Mock WSDL generator" );
      cg2.setClassname( "org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1" );
      p.addContentGenerator( cg2 );

      PluginServiceDefinition ws = new PluginServiceDefinition();
      ws.setId( "echoService" );
      ws.setServiceClass( EchoServiceBean.class.getName() );
      ws.setTypes( new String[] { "xml" } );
      ws.setTitle( "junit echo service" );
      p.addWebservice( ws );

      return Arrays.asList( (IPlatformPlugin) p );
    }
  }
}
