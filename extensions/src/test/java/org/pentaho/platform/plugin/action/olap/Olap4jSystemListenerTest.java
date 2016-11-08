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
package org.pentaho.platform.plugin.action.olap;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.platform.util.PasswordHelper;

import java.util.Arrays;
import java.util.Properties;

public class Olap4jSystemListenerTest {
  @Test
  public void testStartupRegistersAndRemovesOlapConnections() throws Exception {
    final IOlapService mockOlapService = Mockito.mock( IOlapService.class );
    final IPentahoSession mockSession = Mockito.mock( IPentahoSession.class );
    Olap4jSystemListener listener = new Olap4jSystemListener() {
      @Override IOlapService getOlapService( IPentahoSession session ) {
        Assert.assertSame( mockSession, session );
        return mockOlapService;
      }

      @Override PasswordHelper getPasswordHelper() {
        return new PasswordHelper( new Base64PasswordService() );
      }
    };
    final IPentahoSession oSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession( mockSession );
    try {
      Properties properties1 = makeProperties(
        "aName", "idk", "jdbc:mongolap:host=remote;user=admin;Password=ENC:YWRtaW4=;port=1234",
        "aUser", "ENC:YVBhc3N3b3Jk" );
      Properties properties2 = makeProperties(
        "bName", "istilldk", "jdbc:mongolap:host=remoteb;user=admin;password=admin;port=1234", "bUser", "bPassword" );
      listener.setOlap4jConnectionList( Arrays.asList( properties1, properties2 ) );
      listener.setOlap4jConnectionRemoveList( Arrays.asList( "defunctConnection", "worthless" ) );
      listener.startup( mockSession );
      Mockito.verify( mockOlapService )
        .addOlap4jCatalog(
          "aName", "idk", "jdbc:mongolap:host=remote; user=admin; Password=admin; port=1234", "aUser", "aPassword",
          new Properties(), true, mockSession );
      Mockito.verify( mockOlapService )
        .addOlap4jCatalog(
          "bName", "istilldk", "jdbc:mongolap:host=remoteb; user=admin; password=admin; port=1234", "bUser",
          "bPassword", new Properties(), true, mockSession );
      Mockito.verify( mockOlapService ).removeCatalog( "defunctConnection", mockSession );
      Mockito.verify( mockOlapService ).removeCatalog( "worthless", mockSession );
    } finally {
      PentahoSessionHolder.setSession( oSession );
    }

  }

  @Test
  public void testExceptionsDoNotStopSystemLoad() throws Exception {
    final IOlapService mockOlapService = Mockito.mock( IOlapService.class );
    final IPentahoSession mockSession = Mockito.mock( IPentahoSession.class );
    Olap4jSystemListener listener = new Olap4jSystemListener() {
      @Override IOlapService getOlapService( IPentahoSession session ) {
        Assert.assertSame( mockSession, session );
        return mockOlapService;
      }

      @Override PasswordHelper getPasswordHelper() {
        return new PasswordHelper( new Base64PasswordService() );
      }
    };
    Properties properties1 = makeProperties( "aName", "idk", "jdbc:mongolap:host=remote", "aUser", "aPassword" );
    listener.setOlap4jConnectionList( Arrays.asList( properties1 ) );
    listener.setOlap4jConnectionRemoveList( Arrays.asList( "defunctConnection" ) );
    Mockito.doThrow( new RuntimeException( "something terrible happened" ) )
      .when( mockOlapService )
      .addOlap4jCatalog(
        "aName", "idk", "jdbc:mongolap:host=remote", "aUser", "aPassword", new Properties(), true, mockSession );
    Mockito.doThrow( new RuntimeException( "something amazing happend" ) )
      .when( mockOlapService )
      .removeCatalog( "defunctConnection", mockSession );
    Assert.assertTrue( listener.startup( mockSession ) );
  }

  private Properties makeProperties(
    String name, String className, String connectString, String user, String password )
  {
    Properties properties = new Properties();
    properties.setProperty( "name", name );
    properties.setProperty( "className", className );
    properties.setProperty( "connectString", connectString );
    properties.setProperty( "user", user );
    properties.setProperty( "password", password );
    return properties;
  }
}
