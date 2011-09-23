package org.pentaho.test.platform.engine.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Hashtable;
import java.util.Map;

import org.jmock.Mockery;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;

@SuppressWarnings("nls")
public class PentahoSessionHolderTest {
  
  Mockery mockery = new Mockery();
  
  static Map<Thread,Boolean> threadFailureMap = new Hashtable<Thread, Boolean>();

  @Test
  public void testStandaloneSessionLifecycle() {
    assertNull("session should be null at first", PentahoSessionHolder.getSession());
    
    StandaloneSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);
    assertSame("StandaloneSession was not set as the thread-bound session", session, PentahoSessionHolder.getSession());
    
    PentahoSessionHolder.removeSession();
    assertNull("session should be null after a remove", PentahoSessionHolder.getSession());
  }
  
  @Test
  public void testNullSession() {
    final IPentahoSession mockSession = mockery.mock(IPentahoSession.class);
    PentahoSessionHolder.setSession(mockSession);
    assertSame(mockSession, PentahoSessionHolder.getSession());
    PentahoSessionHolder.setSession(null);
    assertNull("session should be null", PentahoSessionHolder.getSession());
  }
  
  @Test
  public void testThreadsManageOwnSessions() throws InterruptedException {
    final IPentahoSession mockSession = mockery.mock(IPentahoSession.class);
    final IPentahoSession threadMockSession = mockery.mock(IPentahoSession.class, "threadMockSession");
    
    PentahoSessionHolder.setSession(mockSession);
    assertSame(mockSession, PentahoSessionHolder.getSession());
    
    Thread t = new Thread("testThreadsManageOwnSessions-1") {
      public void run() {
        PentahoSessionHolder.setSession(threadMockSession);
        if(PentahoSessionHolder.getSession() != threadMockSession) {
          threadFailureMap.put(this, true);
        }
        try {
          sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
    assertSame("(while child thread runs) parent thread should still have it's original session", mockSession, PentahoSessionHolder.getSession());
    t.join(2000);
    assertSame("(when child thread has terminated) parent thread should still have it's original session", mockSession, PentahoSessionHolder.getSession());
    
    for(boolean failure : threadFailureMap.values()) {
      assertFalse("An assertion in a thread failed.  Check the log for what failed",failure);
    }
  }
  
  @Test
  public void testThreadedInheritsSession() throws InterruptedException {
    final IPentahoSession mockSession = mockery.mock(IPentahoSession.class);
    PentahoSessionHolder.setSession(mockSession);
    
    Thread t = new Thread("testThreadedInheritsSession-1") {
      public void run() {
        if(PentahoSessionHolder.getSession() != mockSession) {
          System.err.println(getName()+" - should have inherited session from parent thread");
          threadFailureMap.put(this, true);
        }
      }
    };
    t.start();
    t.join(5000);
    
    for(boolean failure : threadFailureMap.values()) {
      assertFalse("An assertion in a thread failed.  Check the log for what failed",failure);
    }
  }
}
