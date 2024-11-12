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


package org.pentaho.mantle.client.commands;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SessionExpiredCommandTest {

  private SessionExpiredCommand command;

  private static final int ETALON_OFFSET = 500;

  @Before
  public void setUp() {
    command = Mockito.mock( SessionExpiredCommand.class );
    Mockito.when( command.getNextCheckShift() ).thenCallRealMethod();
    Mockito.doCallRealMethod().when( command ).setClientTimeOffset();
    Mockito.when( command.getPollingInterval() ).thenReturn( 1000 );
  }

  @Test
  public void testNoCookie() {
    Assert.assertEquals( 1000, command.getNextCheckShift() );
    Mockito.when( command.getCookie( "session-expiry" ) ).thenReturn( "10000" );
    Assert.assertEquals( 1000, command.getNextCheckShift() );
    Mockito.when( command.getCookie( "session-expiry" ) ).thenReturn( null );
    Mockito.when( command.getCookie( "client-time-offset" ) ).thenReturn( "300" );
    Assert.assertEquals( 1000, command.getNextCheckShift() );
  }

  @Test
  public void testInvalidCookie() {
    Assert.assertEquals( 1000, command.getNextCheckShift() );
    Mockito.when( command.getCookie( "session-expiry" ) ).thenReturn( "quack-quack" );
    Mockito.when( command.getCookie( "client-time-offset" ) ).thenReturn( "baaa-baaa" );
    Assert.assertEquals( 1000, command.getNextCheckShift() );
    Mockito.when( command.getCookie( "session-expiry" ) ).thenReturn( "10000" );
    Assert.assertEquals( 1000, command.getNextCheckShift() );
    Mockito.when( command.getCookie( "session-expiry" ) ).thenReturn( "quack-quack" );
    Mockito.when( command.getCookie( "client-time-offset" ) ).thenReturn( "300" );
    Assert.assertEquals( 1000, command.getNextCheckShift() );
  }

  @Test
  public void testNoServerTime() {
    command.setClientTimeOffset();
    Mockito.verify( command, Mockito.times( 1 ) ).setCookie( "client-time-offset", "0" );
  }

  @Test
  public void testInvalidServerTime() {
    Mockito.when( command.getCookie( "server-time" ) ).thenReturn( "moo-moo" );
    command.setClientTimeOffset();
    Mockito.verify( command, Mockito.times( 1 ) ).setCookie( "client-time-offset", "0" );
  }

  @Test
  public void testSetClientOffsetPlus() {
    Mockito.when( command.getCookie( "server-time" ) ).thenReturn( "10000" );
    Mockito.when( command.getClientTime() ).thenReturn( 10100L );
    command.setClientTimeOffset();
    Mockito.verify( command, Mockito.times( 1 ) ).setCookie( "client-time-offset", "100" );
  }

  @Test
  public void testSetClientOffsetMinus() {
    Mockito.when( command.getCookie( "server-time" ) ).thenReturn( "10000" );
    Mockito.when( command.getClientTime() ).thenReturn( 9900L );
    command.setClientTimeOffset();
    Mockito.verify( command, Mockito.times( 1 ) ).setCookie( "client-time-offset", "-100" );
  }

  @Test
  public void testShiftNoOffset() {
    Mockito.when( command.getCookie( "server-time" ) ).thenReturn( "5000" );
    Mockito.when( command.getCookie( "session-expiry" ) ).thenReturn( "5500" );
    Mockito.when( command.getClientTime() ).thenReturn( 5000L );
    Mockito.doAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
        Assert.assertEquals( "0", invocationOnMock.getArguments()[ 1 ] );
        Mockito.when( command.getCookie( (String) invocationOnMock.getArguments()[ 0 ] ) )
          .thenReturn( (String) invocationOnMock.getArguments()[ 1 ] );
        return invocationOnMock;
      }
    } ).when( command ).setCookie( Mockito.anyString(), Mockito.anyString() );
    command.setClientTimeOffset();
    Assert.assertEquals( ETALON_OFFSET, command.getNextCheckShift() );
  }

  @Test
  public void testShiftPlusOffset() {
    Mockito.when( command.getCookie( "server-time" ) ).thenReturn( "5000" );
    Mockito.when( command.getCookie( "session-expiry" ) ).thenReturn( "5500" );
    Mockito.when( command.getClientTime() ).thenReturn( 6000L );
    Mockito.doAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
        Assert.assertEquals( "1000", invocationOnMock.getArguments()[ 1 ] );
        Mockito.when( command.getCookie( (String) invocationOnMock.getArguments()[ 0 ] ) )
          .thenReturn( (String) invocationOnMock.getArguments()[ 1 ] );
        return invocationOnMock;
      }
    } ).when( command ).setCookie( Mockito.anyString(), Mockito.anyString() );
    command.setClientTimeOffset();
    Assert.assertEquals( ETALON_OFFSET, command.getNextCheckShift() );
  }

  @Test
  public void testShiftMinusOffset() {
    Mockito.when( command.getCookie( "server-time" ) ).thenReturn( "5000" );
    Mockito.when( command.getCookie( "session-expiry" ) ).thenReturn( "5500" );
    Mockito.when( command.getClientTime() ).thenReturn( 3000L );
    Mockito.doAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
        Assert.assertEquals( "-2000", invocationOnMock.getArguments()[ 1 ] );
        Mockito.when( command.getCookie( (String) invocationOnMock.getArguments()[ 0 ] ) )
          .thenReturn( (String) invocationOnMock.getArguments()[ 1 ] );
        return invocationOnMock;
      }
    } ).when( command ).setCookie( Mockito.anyString(), Mockito.anyString() );
    command.setClientTimeOffset();
    Assert.assertEquals( ETALON_OFFSET, command.getNextCheckShift() );
  }

}
