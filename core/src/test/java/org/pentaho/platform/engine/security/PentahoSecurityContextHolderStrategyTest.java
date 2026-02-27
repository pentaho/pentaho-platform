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
