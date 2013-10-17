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
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PluginAdapter;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import static org.junit.Assert.*;

@SuppressWarnings( "nls" )
public class PluginAdapterTest {
  private MicroPlatform microPlatform;
  private IPentahoSession session;
  private PluginAdapter pluginAdapter;

  @SuppressWarnings( "deprecation" )
  @Before
  public void init0() {
    microPlatform = new MicroPlatform( "test-res/PluginManagerTest/" );
    microPlatform.define( ISolutionEngine.class, SolutionEngine.class );
    microPlatform.define( IPluginManager.class, DefaultPluginManager.class );
    microPlatform.define( IPluginProvider.class, SystemPathXmlPluginProvider.class );
    microPlatform.define( IServiceManager.class, DefaultServiceManager.class );

    session = new StandaloneSession();

    pluginAdapter = new PluginAdapter();

    microPlatform.addAdminAction( pluginAdapter );

    microPlatform.init();
  }

  @Test
  public void testPluginAdapterViaPublish() throws Exception {
    String str = PentahoSystem.publish( session, "org.pentaho.platform.plugin.services.pluginmgr.PluginAdapter" ); //$NON-NLS-1$
    // If we see 'Discovered plugin' anywhere in the result string we know that the plugin adapter was able to invoke
    // the plugin manager properly
    assertTrue( "Result string '" + str + "' did not contain 'Discovered plugin'",
        str.indexOf( "Discovered plugin" ) > 0 );
  }

  @SuppressWarnings( "cast" )
  @Test
  public void testPluginAdapterAsPublisher() throws Exception {
    IPentahoPublisher asPublisher = (IPentahoPublisher) pluginAdapter;

    assertEquals( Messages.getInstance().getString( "PluginAdapter.USER_PLUGIN_MANAGER" ), asPublisher.getName() ); //$NON-NLS-1$
    assertNotSame( "!PluginAdapter.USER_PLUGIN_MANAGER!", asPublisher.getName() ); //$NON-NLS-1$

    assertEquals(
        Messages.getInstance().getString( "PluginAdapter.USER_REFRESH_PLUGINS" ), asPublisher.getDescription() ); //$NON-NLS-1$
    assertNotSame( "!PluginAdapter.USER_REFRESH_PLUGINS!", asPublisher.getName() ); //$NON-NLS-1$

    String str = asPublisher.publish( session, ILogger.DEBUG );
    // If we see 'Discovered plugin' anywhere in the result string we know that the plugin adapter was able to invoke
    // the plugin manager properly
    assertTrue( "Result string '" + str + "' did not contain 'Discovered plugin'",
        str.indexOf( "Discovered plugin" ) > 0 );
  }

  @SuppressWarnings( "cast" )
  @Test
  public void testPluginAdapterAsSystemListener() throws Exception {
    IPentahoSystemListener listener = (IPentahoSystemListener) pluginAdapter;

    assertTrue( listener.startup( session ) );

    // this does not do anything but it shouldn't error
    listener.shutdown();
  }
}
