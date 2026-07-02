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


package org.pentaho.platform.engine.security.event;

import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

@RunWith( MockitoJUnitRunner.class )
public class PentahoSessionStartupAuthenticationSuccessListenerTest {

  @Mock
  private IPentahoSession session;
  @Mock
  private Authentication authentication;

  private ByteArrayOutputStream baos;
  private PrintStream oldOut;

  @Before
  public void setUp() {
    baos = new ByteArrayOutputStream();
    oldOut = System.out;
    System.setOut( new PrintStream( baos ) );

    PentahoSessionHolder.setSession( session );
  }

  @After
  public void tearDown() {
    System.setOut( oldOut );
  }

  @Test
  public void testOnApplicationEvent_onlyInteractiveAuthenticationSuccessEvent() {
    ApplicationEvent event = new InteractiveAuthenticationSuccessEvent( authentication, InteractiveAuthenticationSuccessEvent.class );

    PentahoSessionStartupAuthenticationSuccessListener listener = new PentahoSessionStartupAuthenticationSuccessListener();
    listener.onApplicationEvent( event );
    System.out.flush();

    assertNotNull( baos );
    assertTrue( baos.toString().contains( "calling PentahoSystem.sessionStartup" ) );
  }

  @Test
  public void testOnApplicationEvent_AuthenticationEvent() {
    ApplicationEvent event = mock( ApplicationEvent.class );

    PentahoSessionStartupAuthenticationSuccessListener listener = new PentahoSessionStartupAuthenticationSuccessListener();
    listener.onApplicationEvent( event );
    System.out.flush();

    assertNotNull( baos );
    assertFalse( baos.toString().contains( "calling PentahoSystem.sessionStartup" ) );
  }
}
