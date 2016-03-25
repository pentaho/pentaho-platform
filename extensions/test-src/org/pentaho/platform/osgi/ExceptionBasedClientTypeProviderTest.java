package org.pentaho.platform.osgi;

import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.*;

/**
 * Created by nbaker on 3/25/16.
 */
public class ExceptionBasedClientTypeProviderTest {

  private ExceptionBasedClientTypeProvider provider;

  @Test
  public void testGetClientType() throws Exception {
    provider = new ExceptionBasedClientTypeProvider();
    provider.setTargetClass( ExceptionBasedClientTypeProviderTest.class );
    ExceptionBasedHelper helper = new ExceptionBasedHelper();
    helper.callme( this );
  }

  @Test
  /**
   * In this case the stacktrace does not contain the target class, "default" should return
   */
  public void testDefault() throws Exception {
    provider = new ExceptionBasedClientTypeProvider();
    provider.setTargetClass( PentahoSystem.class );
    String clientType = provider.getClientType();
    assertEquals( "default", clientType );
  }

  public void callback(){
    String clientType = provider.getClientType();
    assertEquals( "exceptionbasedhelper", clientType );
  }
}