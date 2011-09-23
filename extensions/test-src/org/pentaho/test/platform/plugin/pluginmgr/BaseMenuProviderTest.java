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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class BaseMenuProviderTest {
  
  private MicroPlatform microPlatform;
  private StandaloneSession session;
  private MenuProvider menuProvider;

  @SuppressWarnings("deprecation")
  @Before
  public void init0() {
    microPlatform = new MicroPlatform("plugin-mgr/test-res/BaseMenuProviderTest/");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IPluginManager.class, DefaultPluginManager.class);
    microPlatform.init();
    session = new StandaloneSession();
    
    menuProvider = new MenuProvider();
  }


  @Test
  public void testMenu() throws Exception {
	    String correctXulPath = "system/ui/menubar.xul"; //$NON-NLS-1$
	    String badXulPath = "bogus"; //$NON-NLS-1$
	    
	    Object obj = menuProvider.getMenuBar("menu", correctXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertTrue("menu does not contain 'file-menu': ["+obj.toString()+"]", obj.toString().indexOf( "file-menu" ) > 0 ); //$NON-NLS-1$

	    obj = menuProvider.getMenuBar("bogus", correctXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertEquals( "", obj.toString() ); //$NON-NLS-1$

	    obj = menuProvider.getMenuBar("menu", badXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertEquals( "", obj.toString() ); //$NON-NLS-1$

	    obj = menuProvider.getPopupMenu( "testpopup" , correctXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertTrue( obj.toString().indexOf( "test command" ) > 0 ); //$NON-NLS-1$

	    obj = menuProvider.getPopupMenu("bogus", correctXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertEquals( "", obj.toString() ); //$NON-NLS-1$

	    obj = menuProvider.getPopupMenu("testpopup", badXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertEquals( "", obj.toString() ); //$NON-NLS-1$
	  }

}
