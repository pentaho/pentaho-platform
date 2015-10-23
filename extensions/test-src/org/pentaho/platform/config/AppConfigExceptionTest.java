package org.pentaho.platform.config;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by rfellows on 10/20/15.
 */
public class AppConfigExceptionTest {
  @Test
  public void testEmptyConstructor() throws Exception {
    AppConfigException ex = new AppConfigException();
    assertEquals( null, ex.getMessage() );
  }

  @Test
  public void testConstructor_message() throws Exception {
    AppConfigException ex = new AppConfigException( "error message" );
    assertEquals( "error message", ex.getMessage() );
  }

  @Test
  public void testConstructor_messageAndThrowable() throws Exception {
    RuntimeException rte = mock( RuntimeException.class );
    AppConfigException ex = new AppConfigException( "error message", rte );
    assertEquals( "error message", ex.getMessage() );
    assertEquals( rte, ex.getCause() );
  }

  @Test
  public void testConstructor_throwable() throws Exception {
    RuntimeException rte = mock( RuntimeException.class );
    AppConfigException ex = new AppConfigException( rte );
    assertEquals( null, ex.getMessage() );
    assertEquals( rte, ex.getCause() );
  }
}
