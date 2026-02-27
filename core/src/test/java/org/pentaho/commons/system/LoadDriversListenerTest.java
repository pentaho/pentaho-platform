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


package org.pentaho.commons.system;

import org.junit.Test;
import org.pentaho.di.core.util.Assert;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;


public class LoadDriversListenerTest {
  @Test
  public void testLoadsDrivers() throws Exception {
    LoadDriversListener listener = new LoadDriversListener();
    listener.startup( null );
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    while ( drivers.hasMoreElements() ) {
      Driver driver = drivers.nextElement();
      if ( "org.pentaho.commons.system.LoadDriversListenerTest$FakeDriver".equals( driver.getClass().getName() ) ) {
        return;
      }
    }
    Assert.assertFalse( true, "didn't find FakeDriver" );

  }

  public static class FakeDriver implements Driver {
    static {
      try {
        DriverManager.registerDriver( new FakeDriver() );
      } catch ( SQLException e ) {
        e.printStackTrace();
      }
    }

    @Override public Connection connect( String url, Properties info ) throws SQLException {
      return null;
    }

    @Override public boolean acceptsURL( String url ) throws SQLException {
      return false;
    }

    @Override public DriverPropertyInfo[] getPropertyInfo( String url, Properties info ) throws SQLException {
      return new DriverPropertyInfo[ 0 ];
    }

    @Override public int getMajorVersion() {
      return 0;
    }

    @Override public int getMinorVersion() {
      return 0;
    }

    @Override public boolean jdbcCompliant() {
      return false;
    }

    //don't add @Override annotation for Java 6 compatibility (class Driver doesn't have getParentLogger method in Java 6)
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
    }
  }

}
