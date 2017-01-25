/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
