package org.pentaho.platform.plugin.action.xmla;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class XMLABaseComponentTest {

  private boolean runMethodDetermineProvider( String param ) {
    XMLABaseComponent mock = mock( XMLABaseComponent.class );

    try {
      Method method = XMLABaseComponent.class.getDeclaredMethod( "determineProvider", String.class );
      method.setAccessible( true );
      method.invoke( mock, param );
    } catch ( Exception e ) {
      return false;
    }
    return true;
  }

  @Test
  public void testCase1() throws Exception {
    Assert.assertTrue( "error during method invocation", runMethodDetermineProvider( "PROVIDER=MONDRIAN" ) );
  }

  @Test
  public void testCase2() throws Exception {
    Locale.setDefault( Locale.US );
    Assert.assertTrue( "error during method invocation", runMethodDetermineProvider( "provider=mondrian" ) );
  }

  @Test
  public void testCase3() throws Exception {
    Locale.setDefault( new Locale( "tr" ) );
    Assert.assertTrue( "error during method invocation", runMethodDetermineProvider( "provider=mondrian" ) );
  }

}
