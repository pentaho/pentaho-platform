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


package org.pentaho.test.platform.web;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.dom4j.Document;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.servlet.HttpWebService;
import org.pentaho.test.platform.engine.core.BaseTestCase;
import org.pentaho.test.platform.utils.TestResourceLocation;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.HttpWebService</code>.
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class HttpWebServiceIT extends BaseTestCase {
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
    request.addParameter( "action", "securitydetails" ); //$NON-NLS-1$//$NON-NLS-2$
    request.addParameter( "details", "all" ); //$NON-NLS-1$ //$NON-NLS-2$
    MockHttpServletResponse response = new MockHttpServletResponse();
    HttpWebService servlet = new HttpWebService();
    servlet.doGet( request, response );
    assertTrue( "missing or invalid SOAP wrapper elements", //$NON-NLS-1$
        isSoapValid( response.getContentAsString() ) );
    assertTrue( "missing or invalid users, roles, or acls elements", //$NON-NLS-1$
        isBodyValid( response.getContentAsString() ) );
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
