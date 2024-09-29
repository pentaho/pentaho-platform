/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.security;

import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class PentahoSecurityContextHolderStrategyTest {

  @Test
  public void testGetContext() throws Exception {
    final PentahoSecurityContextHolderStrategy strategy = new PentahoSecurityContextHolderStrategy();
    SecurityContext context = strategy.getContext();
    assertNotNull( context );
    final Authentication authentication = mock( Authentication.class );
    context.setAuthentication( authentication );

    final CountDownLatch doneSignal = new CountDownLatch( 1 );

    assertSame( authentication, strategy.getContext().getAuthentication() );
    Thread thread = new Thread( new Runnable() {
      @Override public void run() {
        assertSame( authentication, strategy.getContext().getAuthentication() );
        Authentication authentication2 = mock( Authentication.class );
        strategy.getContext().setAuthentication( authentication2 );
        assertSame( authentication2, strategy.getContext().getAuthentication() );
        doneSignal.countDown();
      }
    });
    thread.start();
    doneSignal.await();
    assertSame( authentication, strategy.getContext().getAuthentication() );
  }
}
