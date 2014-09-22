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
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanDefinition;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.PluginServiceDefinition;
import org.pentaho.platform.api.engine.SolutionFileMetaAdapter;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.solution.FileInfo;
import org.pentaho.platform.engine.core.solution.PluginOperation;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PlatformPlugin;
import org.pentaho.platform.plugin.services.pluginmgr.PluginMessageLogger;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.GwtRpcServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.IServiceTypeManager;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.EchoServiceBean;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.ui.xul.XulOverlay;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@SuppressWarnings( "nls" )
public class DefaultPluginManagerTest {

  protected MicroPlatform microPlatform;

  protected StandaloneSession session;

  protected IPluginManager pluginManager;

  protected String solutionPath = "test-res/PluginManagerTest";

  @Before
  public void init0() {
    PentahoSystem.clearObjectFactory();
    microPlatform = new MicroPlatform( getSolutionPath() );
    microPlatform.define( ISolutionEngine.class, SolutionEngine.class );
    microPlatform.define( IPluginProvider.class, SystemPathXmlPluginProvider.class );
    microPlatform.define( IServiceManager.class, DefaultServiceManager.class, Scope.GLOBAL );
    microPlatform.define( IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class, Scope.GLOBAL );
    FileSystemBackedUnifiedRepository repo =
      (FileSystemBackedUnifiedRepository) PentahoSystem.get( IUnifiedRepository.class );
    repo.setRootDir( new File( getSolutionPath() ) );

    session = new StandaloneSession();
    pluginManager = new DefaultPluginManager();
  }

  @Test
  public void INTEGRATION_test1_Reload() throws PluginBeanException, PlatformInitializationException {
    microPlatform.start();

    pluginManager.reload();

    // one of the plugins serves a content generator with id=test1. Make sure we can load it.
    assertNotNull( "The plugin serving content generator with id=test1 was not loaded", pluginManager
      .getContentGenerator( "test-type-3", null ) );
  }

  @Test
  public void test2_Plugin1ReceivesLifecycleEvents() throws PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst2PluginProvider.class ).start();

    CheckingLifecycleListener.clearFlags();
    pluginManager.reload();

    assertFalse( "unload was called", CheckingLifecycleListener.unloadedCalled );
    assertTrue( "init was not called", CheckingLifecycleListener.initCalled );
    assertTrue( "loaded was not called", CheckingLifecycleListener.loadedCalled );

    // reload again, this time we expect the plugin to be unloaded first
    CheckingLifecycleListener.clearFlags();
    pluginManager.reload();

    assertTrue( "unload was not called", CheckingLifecycleListener.unloadedCalled );
    assertTrue( "init was not called", CheckingLifecycleListener.initCalled );
    assertTrue( "loaded was not called", CheckingLifecycleListener.loadedCalled );
  }

  @Test
  public void test3_Plugin3FailsToLoad() throws PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst3PluginProvider.class ).start();

    PluginMessageLogger.clear();

    pluginManager.reload();

    System.err.println( PluginMessageLogger.prettyPrint() );

    assertEquals( "bad plugin Plugin 3 did not fail to load", 1,
      PluginMessageLogger.count( "PluginManager.ERROR_0011" ) );
  }

  @Test
  public void INTEGRATION_test4_GetOverlays() throws Exception {
    microPlatform.start();

    PluginMessageLogger.clear();
    pluginManager.reload();
    System.err.println( PluginMessageLogger.prettyPrint() );

    List<XulOverlay> overlays = pluginManager.getOverlays();

    assertNotNull( "Overlays is null", overlays ); //$NON-NLS-1$

    System.err.println( overlays );

    assertEquals( "Wrong number of overlays", 3, overlays.size() ); //$NON-NLS-1$
    XulOverlay overlay = overlays.get( 0 );
    assertEquals( "Wrong overlay id", "overlay1", overlay.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Wrong overlay resource uri", "uri1", overlay.getResourceBundleUri() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue( "Wrong overlay content", overlay.getSource().indexOf( "<node1" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue( "Wrong overlay content", overlay.getSource().indexOf( "<node2" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue( "Wrong overlay content", overlay.getSource().indexOf( "<node3" ) == -1 ); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue( "Wrong overlay content", overlay.getSource().indexOf( "<node4" ) == -1 ); //$NON-NLS-1$ //$NON-NLS-2$
    assertNull( "Overlay URI should be null", overlay.getOverlayUri() ); //$NON-NLS-1$

    overlay = overlays.get( 1 );

    assertEquals( "Wrong overlay id", "overlay2", overlay.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Wrong overlay resource uri", "uri2", overlay.getResourceBundleUri() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue( "Wrong overlay content", overlay.getSource().indexOf( "<node1" ) == -1 ); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue( "Wrong overlay content", overlay.getSource().indexOf( "<node2" ) == -1 ); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue( "Wrong overlay content", overlay.getSource().indexOf( "<node3" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue( "Wrong overlay content", overlay.getSource().indexOf( "<node4" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
    assertNull( "Overlay URI should be null", overlay.getOverlayUri() ); //$NON-NLS-1$
  }

  @Test
  public void test5a_getBean() throws PluginBeanException, PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst5PluginProvider.class ).start();

    // reload should register the beans
    pluginManager.reload();

    assertTrue( "TestMockComponent should have been registered", pluginManager
      .isBeanRegistered( "TestMockComponent" ) );
    assertTrue( "TestPojo should have been registered", pluginManager.isBeanRegistered( "TestPojo" ) );

    Object obj = pluginManager.getBean( "TestMockComponent" );
    assertTrue( obj instanceof IComponent );

    Object pojo = pluginManager.getBean( "TestPojo" );
    assertTrue( pojo instanceof String );
  }

  @Test
  public void testGetClassloader() throws PluginBeanException, PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst5PluginProvider.class ).start();

    // reload should register the beans
    pluginManager.reload();
    assertNotNull( pluginManager.getClassLoader( "good-plugin1" ) );

  }

  @Test( expected = PluginBeanException.class )
  public void test5b_getUnregisteredBean() throws PluginBeanException, PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst5PluginProvider.class ).start();

    // reload should register the beans
    pluginManager.reload();

    assertFalse( "IWasNotRegistered should not have been registered", pluginManager
      .isBeanRegistered( "IWasNotRegistered" ) );

    pluginManager.getBean( "IWasNotRegistered" );
  }

  @Test( expected = PluginBeanException.class )
  public void test5c_getBeanBadClassname() throws PluginBeanException, PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst5PluginProvider.class ).start();

    // reload should register the beans
    pluginManager.reload();

    assertTrue( "TestClassNotFoundComponent should have been registered", pluginManager
      .isBeanRegistered( "TestClassNotFoundComponent" ) );

    assertNotNull( pluginManager.getBean( "TestClassNotFoundComponent" ) );
  }

  @Test
  public void test6_beanNameCollision() throws PluginBeanException, PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst6PluginProvider.class ).start();

    PluginMessageLogger.clear();

    // reload should register the beans
    pluginManager.reload();

    assertNotNull( pluginManager.getBean( "bean1" ) );
    assertTrue(
      "The first plugin to register by this id is a String, it should have remained the registered bean for this id",
      pluginManager.getBean( "bean1" ) instanceof String );

    // TODO: we should be able to test that the plugin was not loaded, indicated by bean1 not being registered, but
    // we cannot until plugin registration becomes transactional
  }

  @Test
  public void test8_getBeanFromPluginClassloader()
    throws PluginBeanException, PlatformInitializationException, BeansException {
    microPlatform.define( IPluginProvider.class, Tst8PluginProvider.class ).start();

    // reload should register the beans
    pluginManager.reload();

    try {
      Class.forName( "org.pentaho.nowhere.PluginOnlyClass" );
      fail( "PluginOnlyClass should only be available through the plugin "
        + "lib dir in order for this test to be valid" );
    } catch ( ClassNotFoundException e ) {
      // ignore
    }

    assertTrue( "PluginOnlyClass should have been registered", pluginManager.isBeanRegistered( "PluginOnlyClass" ) );

    assertNotNull( "PluginOnlyClass bean should have been loaded from test-jar.jar in the plugin lib directory",
      pluginManager.getBean( "PluginOnlyClass" ) );

    // Just making sure that a bean defined in the old world way (IPluginProvider) can be accessed through
    // the externally facing bean factory
    assertNotNull(
      "PluginOnlyClass should also be accessible through the bean factory.  Why wasn't it registered there?",
      pluginManager.getBeanFactory( "test8Plugin" ).getBean( "PluginOnlyClass" ) );
  }

  @Test
  public void test8b_getBeanFromPluginClassloader_altSolutionPath() throws PluginBeanException,
    PlatformInitializationException {
    // This test is to validate a bug that had existed where a solution path ending in '/' was causing
    // the PluginClassLoader to not be able to open plugin jars, thus you would get ClassNotFound exceptions
    // when accessing plugin classes.
    MicroPlatform mp = new MicroPlatform( getSolutionPath() + "/" );
    mp.define( ISolutionEngine.class, SolutionEngine.class );
    mp.define( IServiceManager.class, DefaultServiceManager.class );
    mp.define( IPluginProvider.class, Tst8PluginProvider.class ).start();

    // reload should register the beans
    pluginManager.reload();

    try {
      Class.forName( "org.pentaho.nowhere.PluginOnlyClass" );
      fail( "PluginOnlyClass needs to be available only through "
        + "the plugin lib dir in order for this test to be valid" );
    } catch ( ClassNotFoundException e ) {
      // ignore
    }

    assertTrue( "PluginOnlyClass should have been registered", pluginManager.isBeanRegistered( "PluginOnlyClass" ) );
    assertNotNull( "PluginOnlyClass bean should have been loaded from test-jar.jar in the plugin lib directory",
      pluginManager.getBean( "PluginOnlyClass" ) );
  }

  @Test
  public void test8c_loadClass() throws PlatformInitializationException, PluginBeanException {
    MicroPlatform mp = new MicroPlatform( getSolutionPath() + "/" );
    mp.define( IPluginProvider.class, Tst8PluginProvider.class ).start();
    pluginManager.reload();
    pluginManager.loadClass( "PluginOnlyClass" );
    pluginManager.loadClass( "PluginOnlyClassSpringFile" );
  }

  /**
   * This test is exactly like test8 except that the bean definition is embedded in the bean factory (native style)
   * whereas in test8 the bean definition is being derived from the IPlatformPlugin and being programmatically added to
   * the bean factory.
   */
  @Test
  public void test16_getBeanFromPluginClassloader_usingBeanFactory() throws PluginBeanException,
    PlatformInitializationException, BeansException {
    microPlatform.define( IPluginProvider.class, Tst16PluginProvider.class ).start();

    // reload should register the beans
    pluginManager.reload();

    try {
      Class.forName( "org.pentaho.nowhere.PluginOnlyClass" );
      fail( "PluginOnlyClass should only be available through the plugin lib dir in order for this test to be valid" );
    } catch ( ClassNotFoundException e ) {
      // ignore
    }

    assertTrue( "PluginOnlyClass should have been registered", pluginManager.isBeanRegistered( "PluginOnlyClass" ) );

    assertNotNull( "PluginOnlyClass bean should have been loaded from test-jar.jar in the plugin lib directory",
      pluginManager.getBean( "PluginOnlyClass" ) );

    assertNotNull( "PluginOnlyClass should be accessible through the bean factory.  Why wasn't it registered there?",
      pluginManager.getBeanFactory( "test16Plugin" ).getBean( "PluginOnlyClass" ) );
  }

  @Test
  public void INTEGRATION_getBeanFromPluginClassloader() throws PluginBeanException, PlatformInitializationException {
    microPlatform.start();

    // reload should register the beans
    pluginManager.reload();

    assertTrue( "PluginOnlyClass should have been registered", pluginManager.isBeanRegistered( "PluginOnlyClass" ) );
    assertNotNull( "PluginOnlyClass bean should have been loaded from test-jar.jar in the plugin lib directory",
      pluginManager.getBean( "PluginOnlyClass" ) );
    assertTrue( "TestClassForClassloader should have been registered", pluginManager
      .isBeanRegistered( "TestClassForClassloader" ) );
    assertNotNull(
      "TestClassForClassloader bean should have been loaded from test-jar.jar in the plugin lib directory",
      pluginManager.getBean( "TestClassForClassloader" ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void test9_ContentGenerationRegistration() throws ObjectFactoryException, PluginBeanException,
    PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst9PluginProvider.class ).start();

    PluginMessageLogger.clear();

    pluginManager.reload();

    System.err.println( PluginMessageLogger.prettyPrint() );

    // see if we can get a content generator instance by id (old way)
    assertNotNull( "Could not get content generator test9 by id", pluginManager.getBean( "test9id" ) );
    assertNotNull( "Could not get content generator test9b by id", pluginManager.getBean( "test9bid" ) );
    // see if we can get a content generator instance by type (new way)
    assertNotNull( "Could not get content generator test9 by id",
      pluginManager.getContentGenerator( "test9type", null ) );
    assertNotNull( "Could not get content generator test9b by id",
      pluginManager.getContentGenerator( "test9btype",
        null ) );

    // see if we can access the content generator by type
    IContentGenerator contentGenerator = pluginManager.getContentGeneratorForType( "test9type", session );
    assertNotNull( "Should have gotten an instance of a cg for content type", contentGenerator );
    assertTrue( contentGenerator instanceof ContentGenerator1 );
  }

  @Test
  public void test10_ContentTypeRegistration() throws PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst10PluginProvider.class ).start();

    pluginManager.reload();

    Set<String> types = pluginManager.getContentTypes();
    // FIXME: getContentTypes returns the list of types configured by content generators, not the list
    // of types defined by IContentInfo's. Is this really what we want? If a type has no content
    // generator configured, then it is invisible through this API.
    assertTrue( "test10type1 should be registered", types.contains( "test10type1-ext" ) );
    assertTrue( "test10type2 should be registered", types.contains( "test10type2-ext" ) );

    IContentInfo contentInfo = pluginManager.getContentTypeInfo( "test10type1-ext" );
    assertNotNull( "type should be registered for extension test10type1-ext", contentInfo );
    assertEquals( "test10type1-title", contentInfo.getTitle() );
    assertEquals( "test10type1-description", contentInfo.getDescription() );
    assertEquals( "test10type1-ext", contentInfo.getExtension() );
    assertEquals( "test10type1-url", contentInfo.getIconUrl() );

    List<IPluginOperation> ops = contentInfo.getOperations();
    assertNotNull( "Operations are null", ops );
    assertEquals( "Wrong number of ops", 2, ops.size() );

    assertEquals( "Operation name is wrong", "test10type1-oper1-id", ops.get( 0 ).getId() );

    assertEquals( "Operation name is wrong", "test10type1-oper2-id", ops.get( 1 ).getId() );
    assertEquals( "Operation command is wrong", "test10type1-oper2-perspective", ops.get( 1 ).getPerspective() );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void test13_LEGACY_getServicePlugin() throws PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst13PluginProvider.class );
    microPlatform.start();

    pluginManager.reload();

    // test exact match to content generator
    String plugin1 = pluginManager.getServicePlugin( "test13cgId" );
    assertNotNull( "Should have found a plugin to serve resource 'test13id'", plugin1 );
    // test inexact match to content generator
    plugin1 = pluginManager.getServicePlugin( "/test13cgId/blah/blah" );
    assertNotNull( "Should have found a plugin to serve resource '/test13cgId/blah/blah'", plugin1 );

    // test exact match to a plugin static resource
    String plugin2 = pluginManager.getServicePlugin( "/test/13/static/url" );
    assertNotNull( "Should have found a plugin to serve resource '/test/13/static/url'", plugin2 );
    // test inexact match to a plugin static resource
    plugin2 = pluginManager.getServicePlugin( "/test/13/static/url/blah/blah/blah" );
    assertNotNull( "Should have found a plugin to serve resource '/test/13/static/url/blah/blah/blah'", plugin2 );

    assertEquals( "The service plugin should have been the same for both paths", plugin1, plugin2 );

    //
    // Test proper request resolution: that the "ext" plugin handles the request to /test-ext/...
    // , not the plugin servicing /test/...
    //
    String plugin3 = pluginManager.getServicePlugin( "/test-ext/13/static/url/blah/blah/blah" );
    assertEquals( "Wrong plugin servicing resource '/test-ext/13/static/url/blah/blah/blah'", "test13Plugin-ext",
      plugin3 );
  }

  @Test
  public void test14_webservice_registration() throws PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst14PluginProvider.class );

    microPlatform.start();

    // register the gwt service handler
    IServiceTypeManager gwtHandler = new GwtRpcServiceManager();
    DefaultServiceManager sm = (DefaultServiceManager) PentahoSystem.get( IServiceManager.class );
    sm.setServiceTypeManagers( Arrays.asList( gwtHandler ) );

    PluginMessageLogger.clear();

    pluginManager.reload();

    // print messages before assert so we can see what went wrong if assert fails
    System.out.println( PluginMessageLogger.prettyPrint() );

    assertEquals( "Errors occurred during webservice registration (see log)", 0, PluginMessageLogger
      .count( "PluginManager.ERR" ) );
    // at this point we know that no errors were logged, but we need to make sure the service was registered
    // with the service manager. We'll use a mock service manager to test this, since the default service manager
    // is a heavy Axis-backed impl, requiring an http server

    IServiceConfig config = gwtHandler.getServiceConfig( "EchoServiceBean" );
    assertNotNull( "The GWT service manager should have a service registered by name 'EchoServiceBean'", config );
    assertEquals( "gwt", config.getServiceType() );
    assertEquals( EchoServiceBean.class, config.getServiceClass() );
  }

  @Test
  public void test15_pluginNameCollision_DistinctNames() throws PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst15PluginProvider_DistinctNames.class );
    microPlatform.start();

    assertTrue( pluginManager.reload() );

    System.out.println( PluginMessageLogger.prettyPrint() );
  }

  @Test
  public void test15_pluginNameCollision_DupNames() throws PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst15PluginProvider_DupNames.class );
    microPlatform.start();

    assertFalse( pluginManager.reload() );

    System.out.println( PluginMessageLogger.prettyPrint() );
  }

  @Test
  public void test17_getPluginIdForType() throws PlatformInitializationException, PluginBeanException {
    IPluginProvider provider = new IPluginProvider() {
      public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
        PlatformPlugin p = new PlatformPlugin( new DefaultListableBeanFactory() );
        p.setId( "testPlugin" );

        ContentGeneratorInfo cg1 = new ContentGeneratorInfo();
        cg1.setDescription( "test 9 plugin description" );
        cg1.setId( "oldworldCGid" );
        cg1.setType( "oldworldCGtype" );
        cg1.setTitle( "test" );
        cg1.setClassname( "org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1" );
        // cg1.setFileInfoGeneratorClassname("org.pentaho.test.platform.plugin.pluginmgr.FileInfoGenerator");
        p.addContentGenerator( cg1 );

        BeanDefinition beanDef =
          BeanDefinitionBuilder.rootBeanDefinition( "org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1" )
            .setScope( BeanDefinition.SCOPE_PROTOTYPE ).getBeanDefinition();
        p.getBeanFactory().registerBeanDefinition( "springDefinedCGid", beanDef );
        p.getBeanFactory().registerAlias( "springDefinedCGid", "springDefinedCGtype" );

        return Arrays.asList( (IPlatformPlugin) p );
      }
    };

    microPlatform.defineInstance( IPluginProvider.class, provider ).start();

    pluginManager.reload();

    assertEquals( "testPlugin", pluginManager.getPluginIdForType( "oldworldCGtype" ) );
    assertEquals( "testPlugin", pluginManager.getPluginIdForType( "springDefinedCGtype" ) );
  }

  public String getSolutionPath() {
    return solutionPath;
  }

  public static class CheckingLifecycleListener implements IPluginLifecycleListener {
    public static boolean initCalled, loadedCalled, unloadedCalled;

    public static void clearFlags() {
      initCalled = false;
      loadedCalled = false;
      unloadedCalled = false;
    }

    public void init() {
      initCalled = true;
      loadedCalled = false;
    }

    public void loaded() {
      if ( !initCalled ) {
        throw new IllegalStateException( "init() should have been called prior to loaded()" );
      }
      loadedCalled = true;
    }

    public void unLoaded() {
      unloadedCalled = true;
      loadedCalled = false;
      initCalled = false;
    }
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void test16_pluginExternalResources() {
    microPlatform.define( IPluginProvider.class, Tst16Plugin.class );
    microPlatform.init();

    pluginManager.reload( session );

    assertEquals( 1, pluginManager.getExternalResourcesForContext( "area1" ).size() );
    assertEquals( "testScript.js", pluginManager.getExternalResourcesForContext( "area1" ).get( 0 ) );

    System.out.println( PluginMessageLogger.prettyPrint() );
  }

  public static class Tst3PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setId( "Plugin 3" );
      p.setLifecycleListenerClassname( "bogus.classname" );
      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst2PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setId( "test2Plugin" );
      p.setLifecycleListenerClassname( CheckingLifecycleListener.class.getName() );
      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst5PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      // need to set source description - classloader needs it
      p.setId( "good-plugin1" );
      p.setSourceDescription( "good-plugin1" );
      p.addBean( new PluginBeanDefinition( "TestMockComponent",
        "org.pentaho.test.platform.engine.core.MockComponent" ) );
      p.addBean( new PluginBeanDefinition( "TestPojo", "java.lang.String" ) );
      p.addBean( new PluginBeanDefinition( "TestClassNotFoundComponent", "org.pentaho.test.NotThere" ) );
      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst6PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setId( "test6Plugin" );
      p.addBean( new PluginBeanDefinition( "bean1", "java.lang.String" ) );
      p.addBean( new PluginBeanDefinition( "bean1", "java.lang.Object" ) );
      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst8PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin( new DefaultListableBeanFactory() );
      p.setId( "test8Plugin" );
      // need to set source description - classloader needs it
      p.setSourceDescription( "good-plugin1" );
      p.addBean( new PluginBeanDefinition( "PluginOnlyClass", "org.pentaho.nowhere.PluginOnlyClass" ) );
      // PluginOnlyClassSpringFile
      BeanDefinition beanDef =
        BeanDefinitionBuilder.rootBeanDefinition( "org.pentaho.nowhere.PluginOnlyClass" ).setScope(
          BeanDefinition.SCOPE_PROTOTYPE ).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition( "PluginOnlyClassSpringFile", beanDef );
      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst9PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin( new DefaultListableBeanFactory() );
      p.setId( "test9Plugin" );

      ContentGeneratorInfo cg1 = new ContentGeneratorInfo();
      cg1.setDescription( "test 9 plugin description" );
      cg1.setId( "test9id" );
      cg1.setType( "test9type" );
      cg1.setTitle( "Test Generator 9" );
      cg1.setUrl( "/test9url" );
      cg1.setClassname( "org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1" );
      // cg1.setFileInfoGeneratorClassname("org.pentaho.test.platform.plugin.pluginmgr.FileInfoGenerator");
      p.addContentGenerator( cg1 );

      BeanDefinition beanDef =
        BeanDefinitionBuilder.rootBeanDefinition( "org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1" )
          .setScope( BeanDefinition.SCOPE_PROTOTYPE ).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition( "test9bid", beanDef );
      p.getBeanFactory().registerAlias( "test9bid", "test9btype" );

      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst10PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setId( "test10Plugin" );

      ContentInfo type = new ContentInfo();
      type.setDescription( "test10type1-description" );
      type.setExtension( "test10type1-ext" );
      type.setMimeType( "test10type1-mimeType" );
      type.setTitle( "test10type1-title" );
      type.setIconUrl( "test10type1-url" );

      type.addOperation( new PluginOperation( "test10type1-oper1-id" ) );
      PluginOperation oper2 = new PluginOperation( "test10type1-oper2-id" );
      oper2.setPerspective( "test10type1-oper2-perspective" );
      type.addOperation( oper2 );

      p.addContentInfo( type );

      type = new ContentInfo();
      type.setExtension( "test10type2-ext" );
      p.addContentInfo( type );

      // //////////////////
      // For 10b only
      //
      p.getMetaProviderMap().put( "test10type1-ext", Tst10bMetaProvider.class.getName() );

      ContentGeneratorInfo cg1 = new ContentGeneratorInfo();
      cg1.setDescription( "test 10b plugin description" );
      cg1.setId( "test10type1-ext" ); // setting to same string as extension to verify that names do not collide causing
      // classcastexception
      cg1.setType( "test10type1-ext" );
      cg1.setTitle( "Test Generator 10b" );
      cg1.setUrl( "/test10burl" );
      cg1.setClassname( "org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1" );
      p.addContentGenerator( cg1 );
      //
      // //////////////////

      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst10bMetaProvider extends SolutionFileMetaAdapter {
    public IFileInfo getFileInfo( ISolutionFile solutionFile, InputStream in ) {
      FileInfo fileInfo = new FileInfo();
      fileInfo.setTitle( "test10b-title" );
      fileInfo.setAuthor( "test10b-author" );
      fileInfo.setDescription( "test10b-description" );
      return fileInfo;
    }
  }

  public static class Tst11PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setId( "test11Plugin" );
      PluginServiceDefinition pws = new PluginServiceDefinition();
      pws.setTitle( "ws11title" );
      pws.setDescription( "ws11description" );
      pws.setServiceBeanId( "org.pentaho.test.platform.engine.core.EchoServiceBean" );
      p.addWebservice( pws );

      // defining bean with null id, the classname will be used as the id
      p.addBean( new PluginBeanDefinition( null, "org.pentaho.test.platform.engine.core.EchoServiceBean" ) );

      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst12PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setId( "test12Plugin" );
      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst13PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();

      p.setId( "test13Plugin" );

      ContentGeneratorInfo cg1 = new ContentGeneratorInfo();
      cg1.setDescription( "test 9 plugin description" );
      cg1.setId( "test13cgId" );
      cg1.setType( "test13type" );
      cg1.setTitle( "Test Generator 13" );
      cg1.setUrl( "/test13url" );
      cg1.setClassname( "org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1" );
      p.addContentGenerator( cg1 );

      p.addStaticResourcePath( "/test/13/static/url", "/tmp" );

      PlatformPlugin pExt = new PlatformPlugin();

      pExt.setId( "test13Plugin-ext" );
      pExt.addStaticResourcePath( "/test-ext/13/static/url", "/tmp" );

      return Arrays.asList( (IPlatformPlugin) p, (IPlatformPlugin) pExt );
    }
  }

  public static class Tst14PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setId( "test14Plugin" );
      PluginServiceDefinition pws = new PluginServiceDefinition();
      pws.setTypes( new String[] { "gwt" } );
      pws.setTitle( "ws14title" );
      pws.setDescription( "ws14description" );
      pws.setServiceClass( "org.pentaho.test.platform.engine.core.EchoServiceBean" );
      p.addWebservice( pws );

      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst15PluginProvider_DistinctNames implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      List<IPlatformPlugin> plugins = new ArrayList<IPlatformPlugin>();

      PlatformPlugin p = new PlatformPlugin();
      p.setId( "distinctTest15Plugin1" );
      plugins.add( p );

      PlatformPlugin p2 = new PlatformPlugin();
      p2.setId( "distinctTest15Plugin2" );
      plugins.add( p2 );

      return plugins;
    }
  }

  public static class Tst15PluginProvider_DupNames implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      List<IPlatformPlugin> plugins = new ArrayList<IPlatformPlugin>();

      PlatformPlugin p = new PlatformPlugin();
      p.setId( "dupTest15Plugin" );
      plugins.add( p );

      PlatformPlugin p2 = new PlatformPlugin();
      p2.setId( "dupTest15Plugin" );
      plugins.add( p2 );

      return plugins;
    }
  }

  public static class Tst16PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin( new DefaultListableBeanFactory() );
      BeanDefinition def =
        BeanDefinitionBuilder.rootBeanDefinition( "org.pentaho.nowhere.PluginOnlyClass" ).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition( "PluginOnlyClass", def );
      p.setId( "test16Plugin" );
      // need to set source description - classloader needs it
      p.setSourceDescription( "good-plugin1" );

      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  public static class Tst16Plugin implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      List<IPlatformPlugin> plugins = new ArrayList<IPlatformPlugin>();

      PlatformPlugin p = new PlatformPlugin();
      p.setId( "externalPlugin" );
      p.addExternalResource( "area1", "testScript.js" );
      plugins.add( p );

      return plugins;
    }
  }

}
