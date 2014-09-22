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

package org.pentaho.test.platform.web;

import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.test.platform.engine.core.BaseTestCase;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.ViewAction</code>.
 * 
 * @author mlowery
 */
public class ViewActionServletTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/web-servlet-solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void setUp() {
    System.setProperty( PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY, getSolutionPath() + "/system/"
        + SystemSettings.PENTAHOSETTINGSFILENAME );
    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( getSolutionPath(), "" ); //$NON-NLS-1$
    PentahoSystem.init( applicationContext, getRequiredListeners() );
  }

  protected Map getRequiredListeners() {
    HashMap listeners = new HashMap();
    listeners.put( "globalObjects", "globalObjects" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void testDoGet() throws ServletException, IOException {
    // Getting not class def error.. need to figure that out
    /*
     * setUp(); MockHttpServletRequest request = new MockHttpServletRequest(); MockHttpSession session = new
     * MockHttpSession(); request.setSession(session); request.setupAddParameter("solution", "samples");
     * //$NON-NLS-1$//$NON-NLS-2$ request.setupAddParameter("path", "getting-started"); //$NON-NLS-1$ //$NON-NLS-2$
     * request.setupAddParameter("action", "HelloWorld.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ MockServletContext context
     * = new MockServletContext(); context.setServletContextName("pentaho"); MockServletConfig config = new
     * MockServletConfig(); config.setServletContext(context); config.setServletName("viewAction");
     * request.setContextPath("pentaho"); MockHttpServletResponse response = new MockHttpServletResponse(); ViewAction
     * servlet = new ViewAction(); servlet.init(config); servlet.service(request, response);
     */
    // System.out.println(response.getOutputStreamContent());
  }

}
