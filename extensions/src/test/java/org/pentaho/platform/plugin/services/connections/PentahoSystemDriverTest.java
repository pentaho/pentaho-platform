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


package org.pentaho.platform.plugin.services.connections;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.util.logging.Logger;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PentahoSystemDriverTest {

  @Before
  public void beforeTest (){
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    try {
      while ( drivers.hasMoreElements() ) {
        DriverManager.deregisterDriver( drivers.nextElement() );
      }


    }catch (SQLException e){
      Logger.error(this.getClass(), e.getLocalizedMessage());
    }
  }
  @Test
  public void testPentahoSystemDriversAreRegistered() throws Exception {
    final Driver mockDriver = Mockito.mock( Driver.class );
    final Driver mockDriver2 = Mockito.mock( Driver.class );
    final Connection mockConn = Mockito.mock( Connection.class );
    final Connection mockConn2 = Mockito.mock( Connection.class );
    PentahoSystemDriver driver = new PentahoSystemDriver() {
      @Override List<Driver> getAllDrivers() {
        return Arrays.asList( mockDriver, mockDriver2 );
      }
    };
    DriverManager.registerDriver( driver );
    String url = "jdbc:testing123:morestuff";
    String url2 = "jdbc:alternate:morestuff";
    Mockito.when( mockDriver.connect( url, new Properties() ) ).thenReturn( mockConn );
    Mockito.when( mockDriver.acceptsURL( url ) ).thenReturn( true );
    Mockito.when( mockDriver.connect( url2, new Properties() ) ).thenReturn( null );
    Mockito.when( mockDriver.acceptsURL( url2 ) ).thenReturn( false );
    Mockito.when( mockDriver2.connect( url, new Properties() ) ).thenReturn( null );
    Mockito.when( mockDriver2.acceptsURL( url ) ).thenReturn( false );
    Mockito.when( mockDriver2.connect( url2, new Properties() ) ).thenReturn( mockConn2 );
    Mockito.when( mockDriver2.acceptsURL( url2 ) ).thenReturn( true );
    Connection conn = DriverManager.getConnection( url );
    Assert.assertSame( mockConn, conn );
    Connection conn2 = DriverManager.getConnection( url2 );
    Assert.assertSame( mockConn2, conn2 );
    DriverManager.deregisterDriver( driver );
    mockConn.close();
    conn.close();
  }

  @Test
  public void testSystemDriversCanBeUsedWithAlternateJdbcIdentifier()  throws Exception {
    final Driver mockDriver = Mockito.mock( Driver.class );
    final Connection mockConn = Mockito.mock( Connection.class );
    PentahoSystemDriver driver = new PentahoSystemDriver() {
      @Override List<Driver> getAllDrivers() {
        return Arrays.asList( mockDriver );
      }

      @Override Map<String, String> getTranslationMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "driver4", "driver1" );
        return map;
      }
    };
    DriverManager.registerDriver( driver );
    String url = "jdbc:driver4:morestuff";
    String url2 = "jdbc:driver1:morestuff";
    Mockito.when( mockDriver.connect( url2, new Properties() ) ).thenReturn( mockConn );
    Mockito.when( mockDriver.acceptsURL( url2 ) ).thenReturn( true );
    Connection conn = DriverManager.getConnection( url );
    Assert.assertSame( mockConn, conn );
    DriverManager.deregisterDriver( driver );
    mockConn.close();
    conn.close();
  }

  @Test
  public void testSystemDriversConflictingOnSameUrlSuccess()  throws Exception {
    final Driver mockDriver1 = Mockito.mock( Driver.class );
    final Driver mockDriver2 = Mockito.mock( Driver.class );
    final Connection mockConn = Mockito.mock( Connection.class );

    PentahoSystemDriver driver = new PentahoSystemDriver() {
      @Override List<Driver> getAllDrivers() {
        return Arrays.asList( mockDriver1,mockDriver2 );
      }

      @Override Map<String, String> getTranslationMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "driver1", "driver1" );
        map.put( "driver2", "driver2" );
        return map;
      }
    };
    DriverManager.registerDriver( driver );
    String url = "jdbc:testing123:morestuff";
    //both accept url
    Mockito.when( mockDriver1.acceptsURL( url ) ).thenReturn( true );
    Mockito.when( mockDriver2.acceptsURL( url ) ).thenReturn( true );

    Mockito.when( mockDriver1.connect( url, new Properties() ) ).thenThrow( new SQLException("TestException1") );
    Mockito.when( mockDriver2.connect( url, new Properties() ) ).thenReturn( mockConn);
    Connection conn = DriverManager.getConnection( url );

    Assert.assertSame( mockConn, conn );

    DriverManager.deregisterDriver( driver );
  }

  @Test(expected=SQLException.class)
  public void testSystemDriversConflictingOnSameUrlShouldThrowException()  throws Exception {
    final Driver mockDriver1 = Mockito.mock( Driver.class );
    final Driver mockDriver2 = Mockito.mock( Driver.class );
    PentahoSystemDriver driver = new PentahoSystemDriver() {
      @Override List<Driver> getAllDrivers() {
        return Arrays.asList( mockDriver1,mockDriver2 );
      }

      @Override Map<String, String> getTranslationMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "driver1", "driver2" );
        return map;
      }
    };
    DriverManager.registerDriver( driver );
    String url = "jdbc:testing123:morestuff";
    //both accept url
    Mockito.when( mockDriver1.acceptsURL( url ) ).thenReturn( true );
    Mockito.when( mockDriver2.acceptsURL( url ) ).thenReturn( true );

    Mockito.when( mockDriver1.connect( url, new Properties() ) ).thenThrow( new SQLException("TestException1") );
    Mockito.when( mockDriver2.connect( url, new Properties() ) ).thenThrow( new SQLException("TestException2") );

    Connection conn = DriverManager.getConnection( url );

    DriverManager.deregisterDriver( driver );
  }

  @Test
  public void testSystemDriversConflictingOnSameUrlShouldThrowTwoLevelException()  throws Exception {
    final Driver mockDriver1 = Mockito.mock( Driver.class );
    final Driver mockDriver2 = Mockito.mock( Driver.class );
    PentahoSystemDriver driver = new PentahoSystemDriver() {
      @Override List<Driver> getAllDrivers() {
        return Arrays.asList( mockDriver1,mockDriver2 );
      }

      @Override Map<String, String> getTranslationMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "driver1", "driver2" );
        return map;
      }
    };
    DriverManager.registerDriver( driver );
    String url = "jdbc:testing123:morestuff";
    //both accept url
    Mockito.when( mockDriver1.acceptsURL( url ) ).thenReturn( true );
    Mockito.when( mockDriver2.acceptsURL( url ) ).thenReturn( true );

    Mockito.when( mockDriver1.connect( url, new Properties() ) ).thenThrow( new SQLException("TestException1") );
    Mockito.when( mockDriver2.connect( url, new Properties() ) ).thenThrow( new SQLException("TestException2") );
    try {
      Connection conn = DriverManager.getConnection( url );
    }catch (SQLException e) {
      Assert.assertTrue(e.getLocalizedMessage().contains( "There were failing connections to the url" ));
      Assert.assertTrue(e.getNextException().getLocalizedMessage().contains( "TestException1" ));
      Assert.assertTrue(e.getNextException().getNextException().getLocalizedMessage().contains( "TestException2" ));
    }

    DriverManager.deregisterDriver( driver );
  }
}
