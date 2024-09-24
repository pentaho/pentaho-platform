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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

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
