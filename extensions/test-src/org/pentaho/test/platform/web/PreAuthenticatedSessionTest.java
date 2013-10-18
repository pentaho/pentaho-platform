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

import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.web.http.PreAuthenticatedSessionHolder;

import static org.junit.Assert.*;

/**
 * User: nbaker Date: 6/28/12
 */
public class PreAuthenticatedSessionTest {

  @Test
  public void testSessionStore() {
    PreAuthenticatedSessionHolder holder = new PreAuthenticatedSessionHolder();

    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );

    String key = holder.captureSession();

    assertNotNull( key );

    PentahoSessionHolder.setSession( session );

    assertTrue( holder.restoreSession( key ) );
    IPentahoSession outSession = PentahoSessionHolder.getSession();
    assertEquals( outSession, session );
    holder.close();
  }

  @Test
  public void testSessionNotFound() {
    PreAuthenticatedSessionHolder holder = new PreAuthenticatedSessionHolder();

    assertFalse( holder.restoreSession( "not real" ) );
  }

  @Test
  public void testExpiration() {

    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );

    PreAuthenticatedSessionHolder holder = new PreAuthenticatedSessionHolder( 1, 1 );

    String key = holder.captureSession();
    assertTrue( holder.restoreSession( key ) );
    try {
      Thread.sleep( 3000 );
    } catch ( InterruptedException e ) {
      //ignored
    }
    assertFalse( holder.restoreSession( key ) );

  }
}
