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


package org.pentaho.test.platform.web;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PlatformPlugin;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.LocalizationServlet;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Tests for {@link org.pentaho.platform.web.servlet.LocalizationServlet}
 */
public class LocalizationServletIT {

  private static MicroPlatform microPlatform;

  @BeforeClass
  public static void init() throws PlatformInitializationException {
    StandaloneSession session = new StandaloneSession();
    microPlatform = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/web-servlet-solution" );
    microPlatform.define( ISolutionEngine.class, SolutionEngine.class );
    microPlatform.define( IServiceManager.class, DefaultServiceManager.class,
        IPentahoDefinableObjectFactory.Scope.GLOBAL );
    microPlatform.define( IPluginResourceLoader.class, PluginResourceLoader.class );
    microPlatform.define( IPluginProvider.class, SystemPathXmlPluginProvider.class );
    microPlatform
        .define( IPluginManager.class, DefaultPluginManager.class, IPentahoDefinableObjectFactory.Scope.GLOBAL );

    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
    PentahoSessionHolder.setSession( session );

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
    microPlatform.define( IPluginProvider.class, TestPluginProvider.class );
    microPlatform.start();

    pluginManager.reload( session );
  }

  @AfterClass
  public static void kill() {
    microPlatform.stop();
  }

  @Test
  public void getBundle() {
    LocalizationServlet ls = new LocalizationServlet();
    String json = ls.getJSONBundle( TestPluginProvider.TEST_PLUGIN, "messages/messages" );
    assertNotNull( json );
    assertTrue( json.contains( "\"2\":\"two\"" ) );
  }

  @Test
  public void getBundle_invalid_plugin() {
    LocalizationServlet ls = new LocalizationServlet();
    try {
      ls.getJSONBundle( "invalid-plugin", "messages/messages" );
      fail( "Expected exception" );
    } catch ( RuntimeException ex ) {
      assertTrue( ex.getCause() instanceof IllegalArgumentException );
      assertTrue( "Unexpected exception: " + ex.getCause().getMessage(), ex.getCause().getMessage().contains(
          "LocalizationServlet.ERROR_0001" ) );
    }
  }

  @Test
  public void getBundle_invalid_name() {
    LocalizationServlet ls = new LocalizationServlet();
    try {
      ls.getJSONBundle( "test-plugin", null );
      fail( "Expected exception" );
    } catch ( RuntimeException ex ) {
      assertTrue( ex.getCause() instanceof IllegalArgumentException );
      assertTrue( "Unexpected exception: " + ex.getCause().getMessage(), ex.getCause().getMessage().contains(
          "LocalizationServlet.ERROR_0002" ) );
    }

    try {
      ls.getJSONBundle( "test-plugin", "" );
      fail( "Expected exception" );
    } catch ( RuntimeException ex ) {
      assertTrue( ex.getCause() instanceof IllegalArgumentException );
      assertTrue( "Unexpected exception: " + ex.getCause().getMessage(), ex.getCause().getMessage().contains(
          "LocalizationServlet.ERROR_0002" ) );
    }
  }

  @Test
  public void getBundle_es() {
    LocalizationServlet ls = new LocalizationServlet();
    try {
      LocaleHelper.setThreadLocaleBase( new Locale( "es" ) );
      String json = ls.getJSONBundle( TestPluginProvider.TEST_PLUGIN, "messages/messages" );
      assertNotNull( json );
      assertTrue( json.contains( "\"2\":\"dos\"" ) );
    } finally {
      LocaleHelper.setThreadLocaleBase( null );
    }
  }

  @Test
  public void isMessageCachingEnabled() {
    LocalizationServlet ls = new LocalizationServlet();

    assertTrue( ls.isMessageCachingEnabled( PentahoSystem.get( IPluginManager.class ),
      TestPluginProvider.TEST_PLUGIN ) );
    assertFalse( ls.isMessageCachingEnabled( PentahoSystem.get( IPluginManager.class ),
        TestPluginProvider.TEST_PLUGIN_2 ) );
  }

  @Test
  public void doGet() throws IOException, ServletException {
    LocalizationServlet ls = new LocalizationServlet();
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse resp = new MockHttpServletResponse();
    req.setParameter( "plugin", "test-plugin" );
    req.setParameter( "name", "messages/messages" );
    ls.doGet( req, resp );
  }

  @Test
  public void doGet_invalid_plugin() throws IOException, ServletException {
    LocalizationServlet ls = new LocalizationServlet();
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse resp = new MockHttpServletResponse();
    req.setParameter( "plugin", "invalid-plugin" );
    ls.doGet( req, resp );
    assertEquals( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp.getStatus() );
  }

  public static class TestPluginProvider implements IPluginProvider {
    public static final String TEST_PLUGIN = "test-plugin";
    public static final String TEST_PLUGIN_2 = "test-plugin-2";

    @Override
    public List<IPlatformPlugin> getPlugins( IPentahoSession iPentahoSession )
      throws PlatformPluginRegistrationException {
      PlatformPlugin p1 = new PlatformPlugin();
      // need to set source description - this is what will be used to look up the PluginClassLoader
      p1.setId( TEST_PLUGIN );
      p1.setSourceDescription( TEST_PLUGIN );

      PlatformPlugin p2 = new PlatformPlugin();
      p2.setId( TEST_PLUGIN_2 );
      p2.setSourceDescription( TEST_PLUGIN_2 );

      return Arrays.asList( (IPlatformPlugin) p1, (IPlatformPlugin) p2 );
    }
  }
}
