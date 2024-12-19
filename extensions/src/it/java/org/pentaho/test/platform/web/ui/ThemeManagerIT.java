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


package org.pentaho.test.platform.web.ui;

import com.mockrunner.mock.web.MockServletContext;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.ui.IThemeManager;
import org.pentaho.platform.api.ui.ThemeResource;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Set;

/**
 * User: nbaker Date: 5/25/11
 */
public class ThemeManagerIT extends BaseTest {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/web-solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  @Before
  public void setup() {
    super.setUp();
  }

  @Test
  public void testThemes() throws Exception {
    // setup mock context
    MockServletContext context = new MockServletContext();
    context.addResourcePaths( "/", Arrays.asList( "test-module/" ) );
    context.addResourcePaths( "/test-module/", Arrays.asList( "themes.xml" ) );
    File themesDotXML = new File( getSolutionPath() + "/system/themeplugin/themes.xml" );
    context.setResource( "/test-module/themes.xml", themesDotXML.toURI().toURL() );
    context.setResourceAsStream( "/test-module/themes.xml", new FileInputStream( themesDotXML ) );
    PentahoSystem.getApplicationContext().setContext( context );

    StandaloneSession session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );
    PentahoSystem.get( IPluginManager.class ).reload();

    IThemeManager themeManager = PentahoSystem.get( IThemeManager.class );

    assertTrue( themeManager.getSystemThemeIds().contains( "core" ) );
    assertNotNull( themeManager.getModuleThemeInfo( "themeplugin" ) );
    assertEquals( 1, themeManager.getModuleThemeInfo( "themeplugin" ).getSystemThemes().size() );
    Set<ThemeResource> resources =
        themeManager.getModuleThemeInfo( "themeplugin" ).getSystemThemes().get( 0 ).getResources();
    assertEquals( 3, resources.size() );

    assertNotNull( themeManager.getModuleThemeInfo( "test-module" ) );
    assertEquals( 1, themeManager.getModuleThemeInfo( "test-module" ).getSystemThemes().size() );
    resources = themeManager.getModuleThemeInfo( "test-module" ).getSystemThemes().get( 0 ).getResources();
    assertEquals( 3, resources.size() );
  }
}
