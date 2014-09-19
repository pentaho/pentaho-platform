package org.pentaho.platform.engine.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;

public class PentahoSecurityContextHolderStrategyTest {

  @Test
  public void testGetContext() throws Exception {
    final PentahoSecurityContextHolderStrategy strategy = new PentahoSecurityContextHolderStrategy();
    SecurityContext context = strategy.getContext();
    assertNotNull( context );
    final Authentication authentication = mock( Authentication.class );
    context.setAuthentication( authentication );

    assertSame( authentication, strategy.getContext().getAuthentication() );
    Thread thread = new Thread( new Runnable() {
      @Override
      public void run() {
        assertSame( authentication, strategy.getContext().getAuthentication() );
        Authentication authentication2 = mock( Authentication.class );
        strategy.getContext().setAuthentication( authentication2 );
        assertSame( authentication2, strategy.getContext().getAuthentication() );
        synchronized ( this ) {
          notify();
        }
      }
    } );
    thread.start();
    synchronized ( thread ) {
      thread.wait();
    }
    assertSame( authentication, strategy.getContext().getAuthentication() );
  }
}
