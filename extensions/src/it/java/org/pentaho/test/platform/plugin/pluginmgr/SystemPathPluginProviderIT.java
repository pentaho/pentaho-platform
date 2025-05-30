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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanDefinition;
import org.pentaho.platform.api.engine.PluginServiceDefinition;
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.PluginMessageLogger;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.pluginmgr.perspective.DefaultPluginPerspectiveManager;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings( "nls" )
public class SystemPathPluginProviderIT {
  private MicroPlatform microPlatform = null;

  SystemPathXmlPluginProvider provider = null;

  @Before
  public void init() {
    microPlatform = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/SystemPathPluginProviderTest/" );
    microPlatform.define( ISolutionEngine.class, SolutionEngine.class );
    microPlatform.define( IPluginPerspectiveManager.class, DefaultPluginPerspectiveManager.class );

    provider = new SystemPathXmlPluginProvider();
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testLoad_Good() throws PlatformPluginRegistrationException {
    microPlatform.init();

    PluginMessageLogger.clear();

    List<IPlatformPlugin> plugins = provider.getPlugins( new StandaloneSession() );

    // should successfully load good-plugin1 and good-plugin2 and not load bad-plugin. The fact
    // that bad-plugin does not load should not prevent the good ones from being loaded
    IPlatformPlugin plugin1 = (IPlatformPlugin) CollectionUtils.find( plugins,
      new PluginNameMatcherPredicate( "Plugin 1" ) );
    // plugin1 has the valid resource bundle class name
    assertEquals( "Invalid plugin resource bundle classname", "messages",
      plugin1.getResourceBundleClassName() );
    // plugin1 has the valid title and description for translation and interpolation
    assertEquals( "Invalid plugin title", "${pluginTitle}", plugin1.getTitle() );
    assertEquals( "Invalid plugin description", "${pluginDescription} - ${pluginDescription}",
      plugin1.getDescription() );

    assertTrue( "plugin2 was not found", CollectionUtils.exists( plugins,
      new PluginNameMatcherPredicate( "Plugin 2" ) ) );

    // make sure that the bad plugin caused an error message to be logged
    assertEquals( "bad plugin did not log an error message", 1, PluginMessageLogger
      .count( "SystemPathXmlPluginProvider.ERROR_0001" ) );

    for ( String msg : PluginMessageLogger.getAll() ) {
      System.err.println( msg );
    }
  }

  @SuppressWarnings( "deprecation" )
  @Test( expected = PlatformPluginRegistrationException.class )
  public void testLoad_BadSolutionPath() throws PlatformPluginRegistrationException {
    MicroPlatform mp = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/SystemPathPluginProviderTest/system" );
    mp.define( ISolutionEngine.class, SolutionEngine.class );
    mp.init();

    provider.getPlugins( new StandaloneSession() );
  }

  static class PluginNameMatcherPredicate implements Predicate {
    private final String pluginNameToMatch;

    public PluginNameMatcherPredicate( String pluginNameToMatch ) {
      this.pluginNameToMatch = pluginNameToMatch;
    }

    public boolean evaluate( Object object ) {
      return pluginNameToMatch.equals( ( (IPlatformPlugin) object ).getId() );
    }
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void tesLoadtLifecycleListener() throws PlatformPluginRegistrationException {
    microPlatform.init();

    PluginMessageLogger.clear();

    List<IPlatformPlugin> plugins = provider.getPlugins( new StandaloneSession() );

    // first make sure Plugin 1 was loaded, otherwise our check for lifcycle class will never happen
    assertTrue( "plugin1 was not found", CollectionUtils.exists( plugins,
      new PluginNameMatcherPredicate( "Plugin 1" ) ) );

    for ( IPlatformPlugin plugin : plugins ) {
      if ( plugin.getId().equals( "Plugin 1" ) ) {
        assertEquals( List.of( "org.pentaho.test.platform.plugin.pluginmgr.FooInitializer" ), plugin
          .getLifecycleListenerClassnames() );
      }
      if ( plugin.getId().equals( "Plugin 2" ) ) {
        // no listener defined to for Plugin 2
        assertEquals( 0, plugin.getLifecycleListenerClassnames().size() );
      }
    }
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testLoadBeanDefinition() throws PlatformPluginRegistrationException {
    microPlatform.init();

    List<IPlatformPlugin> plugins = provider.getPlugins( new StandaloneSession() );

    IPlatformPlugin plugin =
      (IPlatformPlugin) CollectionUtils.find( plugins, new PluginNameMatcherPredicate( "Plugin 1" ) );
    assertNotNull( "Plugin 1 should have been found", plugin );

    Collection<PluginBeanDefinition> beans = plugin.getBeans();

    assertEquals( "FooComponent was not loaded", 1, CollectionUtils.countMatches( beans, object -> {
      PluginBeanDefinition bean = (PluginBeanDefinition) object;
      return bean.getBeanId().equals( "FooComponent" )
        && bean.getClassname().equals( "org.pentaho.test.platform.plugin.pluginmgr.FooComponent" );
    } ) );
    assertEquals( "genericBean was not loaded", 1, CollectionUtils.countMatches( beans, object -> {
      PluginBeanDefinition bean = (PluginBeanDefinition) object;
      return bean.getBeanId().equals( "genericBean" ) && bean.getClassname().equals( "java.lang.Object" );
    } ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testLoadLifeCycleListener() throws PlatformPluginRegistrationException {
    microPlatform.init();
    List<IPlatformPlugin> plugins = provider.getPlugins( new StandaloneSession() );

    IPlatformPlugin plugin =
      (IPlatformPlugin) CollectionUtils.find( plugins, new PluginNameMatcherPredicate( "Plugin 1" ) );
    assertNotNull( "Plugin 1 should have been found", plugin );

    assertEquals(
      List.of( "org.pentaho.test.platform.plugin.pluginmgr.FooInitializer" ),
      plugin.getLifecycleListenerClassnames() );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testLoadWebservices() throws PlatformPluginRegistrationException {
    microPlatform.init();
    List<IPlatformPlugin> plugins = provider.getPlugins( new StandaloneSession() );

    System.out.println( PluginMessageLogger.getAll() );

    IPlatformPlugin plugin =
      (IPlatformPlugin) CollectionUtils.find( plugins, new PluginNameMatcherPredicate( "Plugin 1" ) );
    assertNotNull( "Plugin 1 should have been found", plugin );

    Collection<PluginServiceDefinition> webservices = plugin.getServices();

    Object wsObj = CollectionUtils.find( webservices, object -> {
      PluginServiceDefinition ws = (PluginServiceDefinition) object;
      return ws.getTitle().equals( "%TestWS1.TITLE%" );
    } );

    assertNotNull( "Webservice \"%TestWS1.TITLE%\" should have been loaded", wsObj );

    PluginServiceDefinition wsDfn = (PluginServiceDefinition) wsObj;

    assertEquals( "org.pentaho.test.platform.engine.core.EchoServiceBean", wsDfn.getServiceClass() );
    assertEquals( "xml", wsDfn.getTypes()[ 0 ] );
    assertEquals( "gwt", wsDfn.getTypes()[ 1 ] );
    assertEquals( "A test webservice", wsDfn.getDescription() );
    assertEquals( 1, wsDfn.getExtraClasses().size() );
    assertEquals( "java.lang.String", wsDfn.getExtraClasses().iterator().next() );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testLoadContentGenerators() throws PlatformPluginRegistrationException {
    microPlatform.init();
    List<IPlatformPlugin> plugins = provider.getPlugins( new StandaloneSession() );

    IPlatformPlugin plugin =
      (IPlatformPlugin) CollectionUtils.find( plugins, new PluginNameMatcherPredicate( "content-generator-plugin" ) );
    assertNotNull( "content-generator-plugin should have been found", plugin );

    List<IContentInfo> contentTypes = plugin.getContentInfos();

    Object contentType = CollectionUtils.find( contentTypes, object -> {
      IContentInfo type = (IContentInfo) object;
      return type.getTitle().equals( "Good Test Type" );
    } );
    assertNotNull( "\"Good Test Type\" should have been loaded", contentType );
    assertEquals(
      "\"Good Test Type\" extension definition is incorrect",
      "good-test-type",
      ( (IContentInfo) contentType ).getExtension() );

    IContentInfo contentInfo = (IContentInfo) contentType;
    IPluginOperation operation = contentInfo.getOperations().listIterator().next();
    assertEquals( "Missing perspective", "custom-perspective", operation.getPerspective() );

    assertEquals( "\"Test Type Missing type\" should not have been loaded", 0, CollectionUtils.countMatches(
      contentTypes, object -> {
        IContentInfo type = (IContentInfo) object;
        return type.getTitle().equals( "Test Type Missing type" );
      } ) );

    assertEquals( "\"test-type-missing-title\" should not have been loaded", 0, CollectionUtils.countMatches(
      contentTypes, object -> {
        IContentInfo type = (IContentInfo) object;
        return type.getExtension().equals( "test-type-missing-title" );
      } ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testLoadPerspectives() throws PlatformPluginRegistrationException {
    microPlatform.init();
    List<IPlatformPlugin> plugins = provider.getPlugins( new StandaloneSession() );

    IPlatformPlugin plugin =
      (IPlatformPlugin) CollectionUtils.find( plugins, new PluginNameMatcherPredicate( "Plugin 1" ) );
    assertNotNull( "Plugin 1 should have been found", plugin );

    assertEquals( 2, plugin.getPluginPerspectives().size() );
    IPluginPerspective perspective = plugin.getPluginPerspectives().get( 0 );
    assertEquals( "perspective1", perspective.getId() );
    assertEquals( "Test Perspective 1", perspective.getTitle() );
    assertEquals( 500, perspective.getLayoutPriority() );
  }
}
