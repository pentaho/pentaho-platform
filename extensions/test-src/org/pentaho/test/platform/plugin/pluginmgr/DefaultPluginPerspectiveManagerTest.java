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
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.perspective.DefaultPluginPerspectiveManager;
import org.pentaho.platform.plugin.services.pluginmgr.perspective.pojo.DefaultPluginPerspective;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings( "nls" )
public class DefaultPluginPerspectiveManagerTest {

  @Before
  public void init0() {
    MicroPlatform microPlatform = new MicroPlatform( "test-res/PluginManagerTest" );
    microPlatform.define( IPluginPerspectiveManager.class, DefaultPluginPerspectiveManager.class );
  }

  private IPluginPerspective createTestPerspective( final String id, final String title ) {
    IPluginPerspective perspective = new DefaultPluginPerspective();
    perspective.setId( id );
    perspective.setTitle( title );
    perspective.setContentUrl( "test-content-url" );
    perspective.setLayoutPriority( 500 );
    return perspective;
  }

  @Test
  public void testPerspectiveManager() {
    IPluginPerspectiveManager manager = PentahoSystem.get( IPluginPerspectiveManager.class );
    assertNotNull( manager );

    IPluginPerspective testPerspective = createTestPerspective( "test-perspective-id", "test-perspective-title" );
    manager.addPluginPerspective( testPerspective );

    assertEquals( 1, manager.getPluginPerspectives().size() );
    assertEquals( "test-perspective-id", manager.getPluginPerspectives().get( 0 ).getId() );
    assertEquals( "test-perspective-title", manager.getPluginPerspectives().get( 0 ).getTitle() );
    assertEquals( "test-content-url", manager.getPluginPerspectives().get( 0 ).getContentUrl() );

    manager.getPluginPerspectives().get( 0 ).setContentUrl( "different" );
    assertEquals( "different", manager.getPluginPerspectives().get( 0 ).getContentUrl() );

    manager.removePluginPerspective( testPerspective );
    assertEquals( 0, manager.getPluginPerspectives().size() );

    manager.addPluginPerspective( createTestPerspective( "test-perspective-id-1", "test-perspective-title-1" ) );
    manager.addPluginPerspective( createTestPerspective( "test-perspective-id-2", "test-perspective-title-2" ) );
    manager.addPluginPerspective( createTestPerspective( "test-perspective-id-3", "test-perspective-title-3" ) );
    manager.addPluginPerspective( createTestPerspective( "test-perspective-id-4", "test-perspective-title-4" ) );
    manager.addPluginPerspective( createTestPerspective( "test-perspective-id-5", "test-perspective-title-5" ) );
    assertEquals( 5, manager.getPluginPerspectives().size() );

    for ( int i = 0; i < 5; i++ ) {
      assertEquals( "test-perspective-id-" + ( i + 1 ), manager.getPluginPerspectives().get( i ).getId() );
      assertEquals( "test-perspective-title-" + ( i + 1 ), manager.getPluginPerspectives().get( i ).getTitle() );
      assertEquals( 500, manager.getPluginPerspectives().get( i ).getLayoutPriority() );
    }

    manager.clearPluginPerspectives();
    assertEquals( 0, manager.getPluginPerspectives().size() );
  }

}
