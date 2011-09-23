/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * Copyright 2008 - 2009 Pentaho Corporation. All rights reserved.
 * 
*/
package org.pentaho.test.platform.engine.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.solution.CustomSettingsParameterProvider;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings({"all"})
public class SystemParameterProviderTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";
	  public String getSolutionPath() {
	       return SOLUTION_PATH;  
	  }
		public void testSystemParameter() {
		    startTest();
        SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();
        assertEquals( "Output is not correct", "server.log", provider.getStringParameter("pentaho.xml{pentaho-system/log-file}", null) );
	        finishTest();
		}
		
		public void testCustomParameter() {
		    startTest();
	        IPentahoSession session = new StandaloneSession("joe");
          CustomSettingsParameterProvider provider = new CustomSettingsParameterProvider();
          provider.setSession( session );
          
          assertEquals( "Output is not correct", "value1", provider.getStringParameter("settings-{$user}.xml{personal-settings/setting1}", null ) );
	        finishTest();
		}
}
