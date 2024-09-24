/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.olap;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.KettlePasswordService;
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
        assertSame( mockSession, session );
        return mockOlapService;
      }

      @Override PasswordHelper getPasswordHelper() {
        return new PasswordHelper( new KettlePasswordService() );
      }
    };
    final IPentahoSession oSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession( mockSession );
    try {
      Properties properties1 = makeProperties(
        "aName", "idk", "jdbc:mongolap:host=remote;user=admin;Password=ENC:Encrypted 2be98afc86aa7f2e4cb79ce71da9fa6d4;port=1234",
        "aUser", "ENC:Encrypted 2be98afc86aa7f2859b18bd63c99dbdde" );
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
        assertSame( mockSession, session );
        return mockOlapService;
      }

      @Override PasswordHelper getPasswordHelper() {
        return new PasswordHelper( new KettlePasswordService() );
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
    assertTrue( listener.startup( mockSession ) );
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
