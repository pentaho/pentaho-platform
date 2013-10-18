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

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import org.dom4j.Document;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.servlet.HttpWebService;
import org.pentaho.test.platform.engine.core.BaseTestCase;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.HttpWebService</code>.
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class HttpWebServiceTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/web-servlet-solution";

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
    request.setupAddParameter( "action", "securitydetails" ); //$NON-NLS-1$//$NON-NLS-2$
    request.setupAddParameter( "details", "all" ); //$NON-NLS-1$ //$NON-NLS-2$
    MockHttpServletResponse response = new MockHttpServletResponse();
    HttpWebService servlet = new HttpWebService();
    servlet.doGet( request, response );
    assertTrue( "missing or invalid SOAP wrapper elements", //$NON-NLS-1$
        isSoapValid( response.getOutputStreamContent() ) );
    assertTrue( "missing or invalid users, roles, or acls elements", //$NON-NLS-1$
        isBodyValid( response.getOutputStreamContent() ) );
    // System.out.println(response.getOutputStreamContent());
  }

  protected boolean isSoapValid( final String outputStreamContent ) {
    Document doc = null;
    try {
      doc = XmlDom4JHelper.getDocFromString( outputStreamContent, null );
    } catch ( XmlParseException e ) {
      assertFalse( e.getMessage(), true );
      return false;
    }
    if ( null != doc ) {
      return null != doc.selectSingleNode( "/SOAP-ENV:Envelope" ) //$NON-NLS-1$
          && null != doc.selectSingleNode( "/SOAP-ENV:Envelope/SOAP-ENV:Body" ); //$NON-NLS-1$
    } else {
      return false;
    }
  }

  protected boolean isBodyValid( final String outputStreamContent ) {
    Document doc = null;
    try {
      doc = XmlDom4JHelper.getDocFromString( outputStreamContent, null );
    } catch ( Exception e ) {
      assertFalse( e.getMessage(), true );
      return false;
    }

    if ( null != doc ) {
      return null != doc.selectSingleNode( "//content/users" ) //$NON-NLS-1$
          && null != doc.selectSingleNode( "//content/roles" ) //$NON-NLS-1$
          && null != doc.selectSingleNode( "//content/acls" ); //$NON-NLS-1$
    } else {
      return false;
    }
  }

}
