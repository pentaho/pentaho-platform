/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.web;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.web.servlet.HttpMimeTypeListener;
import org.pentaho.test.platform.engine.core.BaseTestCase;
import org.pentaho.test.platform.utils.TestResourceLocation;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.HttpMimeTypeListener</code>.
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class HttpMimeTypeListenerIT extends BaseTestCase {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/web-servlet-solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void setUp() {
    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( getSolutionPath(), "" ); //$NON-NLS-1$
    PentahoSystem.init( applicationContext, getRequiredListeners() );
  }

  protected Map getRequiredListeners() {
    HashMap listeners = new HashMap();
    listeners.put( "globalObjects", "globalObjects" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void testDoGet() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    request.setSession( session );
    request.setupAddParameter( "solution", "samples" ); //$NON-NLS-1$//$NON-NLS-2$
    request.setupAddParameter( "path", "steel-wheels/reports" ); //$NON-NLS-1$ //$NON-NLS-2$
    request.setupAddParameter( "action", "Inventory List.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$
    request.setupAddParameter( "details", "all" ); //$NON-NLS-1$ //$NON-NLS-2$

    MockHttpServletResponse response = new MockHttpServletResponse();
    HttpMimeTypeListener listener = new HttpMimeTypeListener( request, response, "MyOwnReport" ); //$NON-NLS-1$
    listener.setMimeType( "text/html" ); //$NON-NLS-1$
  }
}
