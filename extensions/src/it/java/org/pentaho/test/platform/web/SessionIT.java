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

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpSession;
import org.apache.commons.logging.Log;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.web.http.session.PentahoHttpSessionListener;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Locale;

public class SessionIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/web-solution";

  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/web-solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  public void testStandAloneSession() {
    startTest();

    SimpleParameterProvider parameters = new SimpleParameterProvider();
    parameters.setParameter( "actionname", "ViewAction" ); //$NON-NLS-1$ //$NON-NLS-2$
    StandaloneSession session = new StandaloneSession( "BaseTest.DEBUG_JUNIT_SESSION" ); //$NON-NLS-1$
    Log log = session.getLogger();
    System.out.println( "Action Name for the Session is  " + session.getActionName() ); //$NON-NLS-1$
    session.setNotAuthenticated();
    log.info( "Session is active" ); //$NON-NLS-1$
    session.destroy();
    assertTrue( true );
    finishTest();
  }

  public void testUserSessionWithAuthentication() {
    startTest();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    parameters.setParameter( "actionname", "ViewAction" ); //$NON-NLS-1$ //$NON-NLS-2$
    UserSession usession = new UserSession( "Admin", Locale.US, true, parameters ); //$NON-NLS-1$
    usession.setActionName( "ViewAction" );
    usession.doStartupActions( parameters );
    assertEquals( usession.getActionName(), "ViewAction" ); //$NON-NLS-1$

    finishTest();
  }

  public void testUserSessionWithOutAuthentication() {
    startTest();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    parameters.setParameter( "actionname", "ViewAction" ); //$NON-NLS-1$ //$NON-NLS-2$
    UserSession usession = new UserSession( "Admin", Locale.US, parameters ); //$NON-NLS-1$
    Log log = usession.getLogger();
    usession.setActionName( "ViewAction" );
    usession.doStartupActions( parameters );
    log.info( "Session is active" ); //$NON-NLS-1$
    assertEquals( usession.getActionName(), "ViewAction" ); //$NON-NLS-1$

    finishTest();
  }

  public void testPentahoHttpSession() {
    startTest();
    // @TOTO Not sure how to test PentahoHttpSession. In a request object to be present
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    request.setSession( session );
    request.setupAddParameter( "solution", "samples" ); //$NON-NLS-1$//$NON-NLS-2$
    request.setupAddParameter( "path", "steel-wheels/reports" ); //$NON-NLS-1$ //$NON-NLS-2$
    request.setupAddParameter( "action", "Inventory List.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$
    request.setupAddParameter( "details", "all" ); //$NON-NLS-1$ //$NON-NLS-2$
    HttpSession httpSession = session;

    Enumeration attrNames = httpSession.getAttributeNames();
    while ( attrNames.hasMoreElements() ) {
      System.out.println( "Attribute Name " + attrNames.nextElement() ); //$NON-NLS-1$
    }
    httpSession.setAttribute( "solution", "samples" ); //$NON-NLS-1$ //$NON-NLS-2$
    httpSession.removeAttribute( "solution" ); //$NON-NLS-1$

    finishTest();
  }

  public void testPentahoHttpSessionListener() {
    startTest();
    MockHttpSession session = new MockHttpSession();
    HttpSession httpSession = session;
    HttpSessionEvent event = new HttpSessionEvent( httpSession );

    PentahoHttpSessionListener httpSessionListener = new PentahoHttpSessionListener();
    httpSessionListener.sessionCreated( event );
    PentahoHttpSessionListener.registerHttpSession( session.getId(), null, null, null,
        "Admin", session.getId(), Long.parseLong( "100000" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    PentahoHttpSessionListener.deregisterHttpSession( session.getId() );
    httpSessionListener.sessionDestroyed( event );

    finishTest();
  }

  public void testPentahoHttpSessionListener2() {
    startTest();
    MockHttpSession session = new MockHttpSession();
    HttpSession httpSession = session;
    HttpSessionEvent event = new HttpSessionEvent( httpSession );

    PentahoHttpSessionListener httpSessionListener = new PentahoHttpSessionListener();
    httpSessionListener.sessionCreated( event );
    PentahoHttpSessionListener.registerHttpSession( session.getId(),
        "3543453453", "34534535345", "533453535345", "Admin", "4674564564", Long.parseLong( "100000" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    PentahoHttpSessionListener.deregisterHttpSession( session.getId() );
    session.setAttribute( PentahoSystem.PENTAHO_SESSION_KEY, null ); //$NON-NLS-1$
    httpSessionListener.sessionDestroyed( event );

    finishTest();
  }
}
