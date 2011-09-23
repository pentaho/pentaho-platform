package org.pentaho.test.platform.web.ui;

import com.mockrunner.mock.web.MockHttpSession;
import com.mockrunner.mock.web.MockServletContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.*;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.ui.IThemeManager;
import org.pentaho.platform.api.ui.ThemeResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * User: nbaker
 * Date: 5/25/11
 */
public class ThemeManagerTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";

   public String getSolutionPath() {
       return SOLUTION_PATH;
   }

  @Before
  public void setup(){
    super.setUp();
  }
  @Test
  public void testThemes() throws Exception {
    // setup mock context
    MockServletContext context = new MockServletContext();
    context.addResourcePaths("/", Arrays.asList("test-module/"));
    context.addResourcePaths("/test-module/", Arrays.asList("themes.xml"));
    context.setResource("/test-module/themes.xml", getClass().getResource("/solution/system/themeplugin/themes.xml"));
    context.setResourceAsStream("/test-module/themes.xml", getClass().getResourceAsStream("/solution/system/themeplugin/themes.xml"));
    PentahoSystem.getApplicationContext().setContext(context);


    PentahoSystem.get(IPluginManager.class).reload(new StandaloneSession());

    IThemeManager themeManager = PentahoSystem.get(IThemeManager.class);
    
    assertTrue(themeManager.getSystemThemeIds().contains("core"));
    assertNotNull(themeManager.getModuleThemeInfo("themeplugin"));
    assertEquals(1, themeManager.getModuleThemeInfo("themeplugin").getSystemThemes().size());
    Set<ThemeResource> resources = themeManager.getModuleThemeInfo("themeplugin").getSystemThemes().get(0).getResources();
    assertEquals(3, resources.size());

    assertNotNull(themeManager.getModuleThemeInfo("test-module"));
    assertEquals(1, themeManager.getModuleThemeInfo("test-module").getSystemThemes().size());
    resources = themeManager.getModuleThemeInfo("test-module").getSystemThemes().get(0).getResources();
    assertEquals(3, resources.size());
  }
}
