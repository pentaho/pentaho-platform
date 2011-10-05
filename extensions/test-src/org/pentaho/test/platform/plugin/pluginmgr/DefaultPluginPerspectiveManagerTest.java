/*
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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPluginPerspective;
import org.pentaho.platform.api.engine.IPluginPerspectiveManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginPerspectiveManager;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.ui.xul.XulOverlay;

@SuppressWarnings("nls")
public class DefaultPluginPerspectiveManagerTest {

  @Before
  public void init0() {
    MicroPlatform microPlatform = new MicroPlatform("test-res/PluginManagerTest");
    microPlatform.define(IPluginPerspectiveManager.class, DefaultPluginPerspectiveManager.class);
  }

  private IPluginPerspective createTestPerspective(final String pname) {
    return new IPluginPerspective() {
      private String name = pname;
      
      public void setToolBarOverlay(XulOverlay arg0) {
      }
      
      public void setName(String name) {
        this.name = name;
      }
      
      public void setMenuBarOverlay(XulOverlay arg0) {
      }
      
      public void setContentUrl(String arg0) {
      }
      
      public XulOverlay getToolBarOverlay() {
        return null;
      }
      
      public String getName() {
        return name;
      }
      
      public XulOverlay getMenuBarOverlay() {
        return null;
      }
      
      public String getContentUrl() {
        return "test-content-url";
      }
    };
  }
  
  @Test
  public void asdf() {
    IPluginPerspectiveManager manager = PentahoSystem.get(IPluginPerspectiveManager.class);
    assertNotNull(manager);
    
    IPluginPerspective testPerspective = createTestPerspective("test-perspective-name");
    manager.addPluginPerspective(testPerspective);
    
    assertEquals(1, manager.getPluginPerspectives().size());
    assertEquals("test-perspective-name", manager.getPluginPerspectives().get(0).getName());
    assertEquals("test-content-url", manager.getPluginPerspectives().get(0).getContentUrl());
    
    manager.removePluginPerspective(testPerspective);
    assertEquals(0, manager.getPluginPerspectives().size());

    manager.addPluginPerspective(createTestPerspective("test-perspective-name-1"));
    manager.addPluginPerspective(createTestPerspective("test-perspective-name-2"));
    manager.addPluginPerspective(createTestPerspective("test-perspective-name-3"));
    manager.addPluginPerspective(createTestPerspective("test-perspective-name-4"));
    manager.addPluginPerspective(createTestPerspective("test-perspective-name-5"));
    assertEquals(5, manager.getPluginPerspectives().size());

    for (int i=0;i<5;i++) {
      assertEquals("test-perspective-name-" + (i+1), manager.getPluginPerspectives().get(i).getName());
    }
    
    manager.clearPluginPerspectives();
    assertEquals(0, manager.getPluginPerspectives().size());
  }
  
}
