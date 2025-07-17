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

package org.pentaho.platform.plugin.services.pluginmgr;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPlatformPlugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoSystemPluginManagerTest {
  @Test
  public void testSetResourceBundleProvider() {
    IPlatformPlugin platformPlugin = Mockito.mock( IPlatformPlugin.class );
    when( platformPlugin.getResourceBundleClassName() ).thenReturn( "messages" );

    ClassLoader classLoader = Mockito.mock( ClassLoader.class );
    PentahoSystemPluginManager pluginManager = new PentahoSystemPluginManager();

    pluginManager.setResourceBundleProvider( platformPlugin, classLoader );

    verify( platformPlugin ).setResourceBundleProvider( any() );
  }

  @Test
  public void testSetResourceBundleProviderEmptyResourceBundleClassName() {
    IPlatformPlugin platformPlugin = Mockito.mock( IPlatformPlugin.class );
    when( platformPlugin.getResourceBundleClassName() ).thenReturn( "" );

    ClassLoader classLoader = Mockito.mock( ClassLoader.class );
    PentahoSystemPluginManager pluginManager = new PentahoSystemPluginManager();

    pluginManager.setResourceBundleProvider( platformPlugin, classLoader );

    verify( platformPlugin, never() ).setResourceBundleProvider( any() );
  }

  @Test
  public void testSetResourceBundleProviderNullResourceBundleClassName() {
    IPlatformPlugin platformPlugin = Mockito.mock( IPlatformPlugin.class );
    when( platformPlugin.getResourceBundleClassName() ).thenReturn( null );

    ClassLoader classLoader = Mockito.mock( ClassLoader.class );
    PentahoSystemPluginManager pluginManager = new PentahoSystemPluginManager();

    pluginManager.setResourceBundleProvider( platformPlugin, classLoader );

    verify( platformPlugin, never() ).setResourceBundleProvider( any() );
  }
}
