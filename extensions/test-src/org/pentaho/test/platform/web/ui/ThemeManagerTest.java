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

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Set;

/**
 * User: nbaker Date: 5/25/11
 */
public class ThemeManagerTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/web-solution";

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
