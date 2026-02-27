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
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.services.MessageFormatter;
import org.pentaho.platform.web.servlet.UIServlet;
import org.pentaho.test.platform.engine.core.BaseTestCase;
import org.pentaho.test.platform.utils.TestResourceLocation;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.UIServlet</code>.
 * 
 * @author mlowery
 */
public class UIServletIT extends BaseTestCase {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/web-servlet-solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void setUp() {
    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( getSolutionPath(), "" ); //$NON-NLS-1$
    PentahoSystem.init( applicationContext, getRequiredListeners() );
    IMessageFormatter msgFormatter = new MessageFormatter();
    PentahoSystem.registerObject( msgFormatter );
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
    request.addParameter( "solution", "samples" ); //$NON-NLS-1$//$NON-NLS-2$
    request.addParameter( "path", "steel-wheels/reports" ); //$NON-NLS-1$ //$NON-NLS-2$
    request.addParameter( "action", "Inventory List.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$
    request.addParameter( "details", "all" ); //$NON-NLS-1$ //$NON-NLS-2$

    MockHttpServletResponse response = new MockHttpServletResponse();
    UIServlet servlet = new UIServlet();
    servlet.service( request, response );

    // System.out.println(response.getOutputStreamContent());
  }
}
