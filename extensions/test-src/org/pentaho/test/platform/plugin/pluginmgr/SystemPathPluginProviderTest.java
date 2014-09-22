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

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings( "nls" )
public class SystemPathPluginProviderTest {
  private MicroPlatform microPlatform = null;

  SystemPathXmlPluginProvider provider = null;

  @Before
  public void init() {
    microPlatform = new MicroPlatform( "test-res/SystemPathPluginProviderTest/" );
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
    assertTrue( "plugin1 was not found", CollectionUtils.exists( plugins,
      new PluginNameMatcherPredicate( "Plugin 1" ) ) );
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
    MicroPlatform mp = new MicroPlatform( "test-res/SystemPathPluginProviderTest/system" );
    mp.define( ISolutionEngine.class, SolutionEngine.class );
    mp.init();

    provider.getPlugins( new StandaloneSession() );
  }

  class PluginNameMatcherPredicate implements Predicate {
    private String pluginNameToMatch;

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
        assertEquals( "org.pentaho.test.platform.plugin.pluginmgr.FooInitializer", plugin
            .getLifecycleListenerClassname() );
      }
      if ( plugin.getId().equals( "Plugin 2" ) ) {
        // no listener defined to for Plugin 2
        assertNull( plugin.getLifecycleListenerClassname() );
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

    assertEquals( "FooComponent was not loaded", 1, CollectionUtils.countMatches( beans, new Predicate() {
      public boolean evaluate( Object object ) {
        PluginBeanDefinition bean = (PluginBeanDefinition) object;
        return bean.getBeanId().equals( "FooComponent" )
            && bean.getClassname().equals( "org.pentaho.test.platform.plugin.pluginmgr.FooComponent" );
      }
    } ) );
    assertEquals( "genericBean was not loaded", 1, CollectionUtils.countMatches( beans, new Predicate() {
      public boolean evaluate( Object object ) {
        PluginBeanDefinition bean = (PluginBeanDefinition) object;
        return bean.getBeanId().equals( "genericBean" ) && bean.getClassname().equals( "java.lang.Object" );
      }
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

    assertEquals( "org.pentaho.test.platform.plugin.pluginmgr.FooInitializer", plugin.getLifecycleListenerClassname() );
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

    Object wsobj = CollectionUtils.find( webservices, new Predicate() {
      public boolean evaluate( Object object ) {
        PluginServiceDefinition ws = (PluginServiceDefinition) object;
        boolean ret = ws.getTitle().equals( "%TestWS1.TITLE%" );
        return ret;
      }
    } );

    assertNotNull( "Webservice \"%TestWS1.TITLE%\" should have been loaded", wsobj );

    PluginServiceDefinition wsDfn = (PluginServiceDefinition) wsobj;

    assertEquals( "org.pentaho.test.platform.engine.core.EchoServiceBean", wsDfn.getServiceClass() );
    assertEquals( "xml", wsDfn.getTypes()[0] );
    assertEquals( "gwt", wsDfn.getTypes()[1] );
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

    Object contentType = CollectionUtils.find( contentTypes, new Predicate() {
      public boolean evaluate( Object object ) {
        IContentInfo type = (IContentInfo) object;
        return type.getTitle().equals( "Good Test Type" );
      }
    } );
    assertNotNull( "\"Good Test Type\" should have been loaded", contentType );
    assertNotNull( "\"Good Test Type\" extension definition is incorrect", ( (IContentInfo) contentType )
        .getExtension().equals( "good-content-type" ) );

    IContentInfo contentInfo = (IContentInfo) contentType;
    IPluginOperation operation = contentInfo.getOperations().listIterator().next();
    assertEquals( "Missing perspective", "custom-perspective", operation.getPerspective() );

    assertEquals( "\"Test Type Missing type\" should not have been loaded", 0, CollectionUtils.countMatches(
        contentTypes, new Predicate() {
          public boolean evaluate( Object object ) {
            IContentInfo type = (IContentInfo) object;
            return type.getTitle().equals( "Test Type Missing type" );
          }
        } ) );

    assertEquals( "\"test-type-missing-title\" should not have been loaded", 0, CollectionUtils.countMatches(
        contentTypes, new Predicate() {
          public boolean evaluate( Object object ) {
            IContentInfo type = (IContentInfo) object;
            return type.getExtension().equals( "test-type-missing-title" );
          }
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
    assertEquals( perspective.getId(), "perspective1" );
    assertEquals( perspective.getTitle(), "Test Perspective 1" );
    assertEquals( perspective.getLayoutPriority(), 500 );
  }
}
