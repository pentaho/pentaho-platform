package org.pentaho.test.platform.web;

import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.web.http.PreAuthenticatedSessionHolder;

import static org.junit.Assert.*;

/**
 * User: nbaker
 * Date: 6/28/12
 */
public class PreAuthenticatedSessionTest {

  @Test
  public void testSessionStore(){
    PreAuthenticatedSessionHolder holder = new PreAuthenticatedSessionHolder();

    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);

    String key = holder.captureSession();

    assertNotNull(key);

    PentahoSessionHolder.setSession(session);

    assertTrue(holder.restoreSession(key));
    IPentahoSession outSession = PentahoSessionHolder.getSession();
    assertEquals(outSession, session);
    holder.close();
  }


  @Test
  public void testSessionNotFound(){
    PreAuthenticatedSessionHolder holder = new PreAuthenticatedSessionHolder();

    assertFalse(holder.restoreSession("not real"));
  }

  @Test
  public void testExpiration(){

    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);

    PreAuthenticatedSessionHolder holder = new PreAuthenticatedSessionHolder(1, 1);

    String key = holder.captureSession();
    assertTrue(holder.restoreSession(key));
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {}
    assertFalse(holder.restoreSession(key));

  }
}
