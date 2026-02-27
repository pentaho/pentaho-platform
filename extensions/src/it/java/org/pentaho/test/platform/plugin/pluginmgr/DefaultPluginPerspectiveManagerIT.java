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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.perspective.DefaultPluginPerspectiveManager;
import org.pentaho.platform.plugin.services.pluginmgr.perspective.pojo.DefaultPluginPerspective;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings( "nls" )
public class DefaultPluginPerspectiveManagerIT {

  @Before
  public void init0() {
    MicroPlatform microPlatform = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/PluginManagerTest" );
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
