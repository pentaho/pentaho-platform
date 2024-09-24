/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.pluginmgr;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.config.SystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.pluginmgr.DefaultPluginManagerIT;
import org.pentaho.test.platform.utils.TestResourceLocation;
import org.pentaho.ui.xul.XulOverlay;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * This will only run when the working directory is set to that of the open Extensions project
 * Created by nbaker on 4/22/14.
 */
public class PentahoSystemPluginManagerIT extends DefaultPluginManagerIT {

  protected String solutionPath = StringUtils.defaultIfEmpty( System.getProperty( "CE_INSTALL" ), ".." ) + "/extensions/src/test/resources/PluginManagerTest";

  public void init0() {
    microPlatform = new MicroPlatform( solutionPath );
    microPlatform.define( ISolutionEngine.class, SolutionEngine.class );
    microPlatform.define( ISystemConfig.class, SystemConfig.class );
    microPlatform.define( IPluginProvider.class, SystemPathXmlPluginProvider.class );
    microPlatform.define( IPluginResourceLoader.class, PluginResourceLoader.class );
    microPlatform
        .define( IServiceManager.class, DefaultServiceManager.class, IPentahoDefinableObjectFactory.Scope.GLOBAL );
    microPlatform.define( IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class,
        IPentahoDefinableObjectFactory.Scope.GLOBAL );
    FileSystemBackedUnifiedRepository repo =
        (FileSystemBackedUnifiedRepository) PentahoSystem.get( IUnifiedRepository.class );
    repo.setRootDir( new File( TestResourceLocation.TEST_RESOURCES + "/PluginManagerTest" ) );

    session = new StandaloneSession();
    pluginManager = new PentahoSystemPluginManager();
  }

  @Override
  public String getSolutionPath() {
    return solutionPath;
  }

  @Test
  public void testPluginSettings() throws Exception {
    init0();
    microPlatform.start();

    PluginMessageLogger.clear();
    pluginManager.reload();

    // Valid plugin and property
    Object setting = pluginManager.getPluginSetting( "Plugin 1", "settings/test-property", "ABCD" );
    assertEquals( "1234", setting.toString() );

    // Valid plugin but missing property. Return Default
    setting = pluginManager.getPluginSetting( "Plugin 1", "settings/missing-property", "5432" );
    assertEquals( "5432", setting.toString() );

    // Invalid plugin. Return default
    setting = pluginManager.getPluginSetting( "Non-Existant Plugin", "settings/test-property", "ABCD" );
    assertEquals( "ABCD", setting.toString() );
  }

  @Test
  public void testOverlayUnRegistration() throws Exception {
    init0();
    microPlatform.start();

    PluginMessageLogger.clear();
    pluginManager.reload();
    System.err.println( PluginMessageLogger.prettyPrint() );

    List<XulOverlay> overlays = PentahoSystem.getAll( XulOverlay.class );

    assertEquals( "Wrong number of overlays", 3, overlays.size() );
    pluginManager.unloadAllPlugins();
    overlays = PentahoSystem.getAll( XulOverlay.class );
    assertEquals( "Overlays should have be deregistered.", 0, overlays.size() );

  }

  @Test
  public void testPerspectiveUnRegistration() throws Exception {
    PentahoSystem.clearObjectFactory();
    PentahoSystem.registerObject( new IPluginProvider() {
      @Override
      public List<IPlatformPlugin> getPlugins( IPentahoSession session )
        throws PlatformPluginRegistrationException {
        return Arrays.asList( (IPlatformPlugin) new PlatformPlugin() {
          @Override
          public List<IPluginPerspective> getPluginPerspectives() {
            return Arrays.asList( mock( IPluginPerspective.class ) );
          }

          @Override
          public String getId() {
            return "foo";
          }
        } );
      }
    }, IPluginProvider.class );
    pluginManager = new PentahoSystemPluginManager();
    pluginManager.reload();
    assertEquals( 1, PentahoSystem.getAll( IPluginPerspective.class ).size() );
    assertEquals( 1, PentahoSystem.getAll( IPlatformPlugin.class ).size() );
    pluginManager.unloadAllPlugins();

    assertEquals( 0, PentahoSystem.getAll( IPluginPerspective.class ).size() );
    assertEquals( 0, PentahoSystem.getAll( IPlatformPlugin.class ).size() );
    pluginManager.reload();
    assertEquals( 1, PentahoSystem.getAll( IPluginPerspective.class ).size() );
  }

  @Test
  public void testClosedAppContext() throws Exception {
    init0();
    microPlatform.start();
    microPlatform.define( IPluginProvider.class, Tst5PluginProvider.class ).start();

    // reload should register the beans
    pluginManager.reload();
    assertNotNull( pluginManager.getClassLoader( "good-plugin1" ) );

    System.err.println( PluginMessageLogger.prettyPrint() );

    List<IContentInfo> contentInfos = PentahoSystem.getAll( IContentInfo.class );

    assertEquals( "Wrong number of contentInfos", 1, contentInfos.size() );
    pluginManager.unloadAllPlugins();
    contentInfos = PentahoSystem.getAll( IContentInfo.class );
    assertEquals( "ContentInfo should have be deregistered.", 0, contentInfos.size() );
  }

  @Test()
  @Override
  public void test5c_getBeanBadClassname() throws PluginBeanException, PlatformInitializationException {
    microPlatform.define( IPluginProvider.class, Tst5DefaultPluginProviderBadClass.class ).start();

    // reload should register the beans
    pluginManager.reload();

    try {
      assertNotNull( pluginManager.getBean( "TestDefaultClassNotFoundComponent" ) );
      fail( "We should have gotten a PluginBeanException for the TestDefaultClassNotFoundComponent plugin" );
    } catch ( Exception ex ) {
      assertTrue( ex instanceof PluginBeanException );
    }
  }
}
