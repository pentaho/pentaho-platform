/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.plugin.services.connections;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PentahoSystemDriverTest {
  @Test
  public void testPentahoSystemDriversAreRegistered() throws Exception {
    final Driver mockDriver = Mockito.mock( Driver.class );
    final Driver mockDriver2 = Mockito.mock( Driver.class );
    final Connection mockConn = Mockito.mock( Connection.class );
    final Connection mockConn2 = Mockito.mock( Connection.class );
    DriverManager.registerDriver( new PentahoSystemDriver() {
      @Override List<Driver> getAllDrivers() {
        return Arrays.asList( mockDriver, mockDriver2 );
      }
    } );
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
  }
}
