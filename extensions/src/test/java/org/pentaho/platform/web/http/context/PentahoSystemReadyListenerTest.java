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

package org.pentaho.platform.web.http.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPlatformReadyListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoSystemReadyListenerTest {

  PentahoSystemReadyListener systemReadyListener;

  @Mock ServletContextEvent contextEvent;
  @Mock IPluginManager pluginManager;
  @Mock IPluginProvider pluginProvider;
  @Mock IPlatformPlugin pluginA;
  @Mock IPlatformPlugin pluginB;
  @Mock ClassLoader classLoader;

  List<IPlatformPlugin> pluginList;

  @Before
  public void setUp() throws Exception {
    systemReadyListener = new PentahoSystemReadyListener();
    PentahoSystem.registerObject( pluginManager );
    PentahoSystem.registerObject( pluginProvider );
    pluginList = new ArrayList<>();
    pluginList.add( pluginA );
    pluginList.add( pluginB );

  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testContextInitialized() throws Exception {
    when( pluginProvider.getPlugins( any( IPentahoSession.class ) ) ).thenReturn( pluginList );

    when( pluginA.getLifecycleListenerClassname() ).thenReturn( TestReadyListener.class.getName() );
    when( pluginA.getId() ).thenReturn( "PluginA" );
    when( pluginB.getLifecycleListenerClassname() ).thenReturn( "" );

    when( pluginManager.getClassLoader( "PluginA" ) ).thenReturn( classLoader );
    Class clazz = TestReadyListener.class;
    when( classLoader.loadClass( TestReadyListener.class.getName() ) ).thenReturn( clazz );

    systemReadyListener.contextInitialized( contextEvent );
    assertTrue( TestReadyListener.readyCalled );
  }

  @Test
  public void testContextDestroyed() throws Exception {
    // no-op, calling for code coverage
    systemReadyListener.contextDestroyed( contextEvent );
  }

  static class TestReadyListener implements IPlatformReadyListener {
    static boolean readyCalled = false;

    public TestReadyListener() {
      readyCalled = false;
    }

    @Override public void ready() throws PluginLifecycleException {
      // to verify this is called in the new instance created within the listener, we are going to
      // set a static property that we can check. we don't have the luxury of getting at the actual instance.
      readyCalled = true;
    }
  }

}
